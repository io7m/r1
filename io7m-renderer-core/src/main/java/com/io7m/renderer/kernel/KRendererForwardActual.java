/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.renderer.kernel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RException;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjective;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLight;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunction;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;
import com.io7m.renderer.kernel.KShadow.KShadowMappedVariance;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.KTranslucent.KTranslucentLit;
import com.io7m.renderer.kernel.KTranslucent.KTranslucentUnlit;

public final class KRendererForwardActual extends KAbstractRendererForward
{
  private static final @Nonnull String NAME;

  static {
    NAME = "forward";
  }

  private static @Nonnull String makeLitShaderName(
    final @Nonnull KLightLabelCache labels,
    final @Nonnull KLight light,
    final @Nonnull KMaterialForwardLabel label)
    throws ConstraintError
  {
    return String.format(
      "fwd_%s_%s",
      labels.getLightLabel(light),
      label.getCode());
  }

  private static @Nonnull String makeUnlitShaderName(
    final KMaterialForwardLabel label)
  {
    return String.format("fwd_U_%s", label.getCode());
  }

  protected static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialForwardLabel label)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixModelView(
      program,
      mwi.getMatrixModelView());

    if (label.impliesUV()) {
      KShadingProgramCommon.putMatrixUV(program, mwi.getMatrixUV());
    }

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        KShadingProgramCommon.putMatrixInverseView(
          program,
          mwi.getMatrixViewInverse());
        break;
      }
      case ENVIRONMENT_NONE:
      {
        break;
      }
    }
  }

  protected static void putInstanceTextures(
    final @Nonnull KTextureUnitContext units,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterial material)
    throws JCGLException,
      ConstraintError
  {
    switch (label.getAlbedo()) {
      case ALBEDO_COLOURED:
      {
        break;
      }
      case ALBEDO_TEXTURED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.getAlbedo().getTexture();
        KShadingProgramCommon.putTextureAlbedo(
          program,
          units.withTexture2D(some.value));
        break;
      }
    }

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.getNormal().getTexture();
        KShadingProgramCommon.putTextureNormal(
          program,
          units.withTexture2D(some.value));
        break;
      }
      case NORMAL_NONE:
      case NORMAL_VERTEX:
      {
        break;
      }
    }

    switch (label.getEmissive()) {
      case EMISSIVE_MAPPED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.getEmissive().getTexture();
        KShadingProgramCommon.putTextureEmissive(
          program,
          units.withTexture2D(some.value));
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
      {
        break;
      }
    }

    if (label.impliesSpecularMap()) {
      final Some<Texture2DStatic> some =
        (Some<Texture2DStatic>) material.getSpecular().getTexture();
      KShadingProgramCommon.putTextureSpecular(
        program,
        units.withTexture2D(some.value));
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        final Some<TextureCubeStatic> some =
          (Some<TextureCubeStatic>) material.getEnvironment().getTexture();
        KShadingProgramCommon.putTextureEnvironment(
          program,
          units.withTextureCube(some.value));
        break;
      }
    }
  }

  protected static void putMaterial(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterial material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterial(program, material);
  }

  private static void putShadow(
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull JCBProgram program,
    final @Nonnull KProjective light)
    throws ConstraintError,
      RException,
      JCGLException
  {
    final Some<KShadow> some = (Some<KShadow>) light.getShadow();

    switch (some.value.getType()) {
      case SHADOW_MAPPED_BASIC:
      {
        final KShadowMappedBasic shadow = (KShadowMappedBasic) some.value;
        final KShadowMap.KShadowMapBasic map =
          (KShadowMapBasic) shadow_context.getShadowMap(shadow);
        final TextureUnit unit =
          unit_context.withTexture2D(map.kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowBasic(program, shadow);
        KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
        break;
      }
      case SHADOW_MAPPED_VARIANCE:
      {
        final KShadowMappedVariance shadow =
          (KShadowMappedVariance) some.value;
        final KShadowMap.KShadowMapVariance map =
          (KShadowMapVariance) shadow_context.getShadowMap(shadow);
        final TextureUnit unit =
          unit_context.withTexture2D(map.kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowVariance(program, shadow);
        KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
        break;
      }
    }
  }

  private static void putShadowReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KProjective light)
    throws JCGLException,
      ConstraintError
  {
    final Some<KShadow> some = (Some<KShadow>) light.getShadow();

    switch (some.value.getType()) {
      case SHADOW_MAPPED_BASIC:
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        break;
      }
      case SHADOW_MAPPED_VARIANCE:
      {
        KShadingProgramCommon.putShadowVarianceReuse(program);
        KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
        break;
      }
    }
  }

  public static @Nonnull KRendererForwardActual rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShadowMapRenderer shadow_renderer,
    final @Nonnull KLabelDecider decider,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    return new KRendererForwardActual(
      g,
      shadow_renderer,
      decider,
      shader_cache,
      caps,
      log);
  }

  /**
   * Render a specific instance, assuming all program state for the current
   * light (if any) has been configured.
   */

  protected static void renderInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext units,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    units.withContext(new KTextureUnitWith() {
      @Override public void run(
        final KTextureUnitContext context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        final KMeshInstance actual = instance.getInstance();
        final KMesh mesh = actual.getMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();
        final KMaterial material = actual.getMaterial();

        KRendererForwardActual.putInstanceMatrices(program, mwi, label);
        KRendererForwardActual.putInstanceTextures(
          context,
          label,
          program,
          material);
        KRendererForwardActual.putMaterial(program, material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(program, array);

          switch (label.getNormal()) {
            case NORMAL_MAPPED:
            {
              KShadingProgramCommon.bindAttributeTangent4(program, array);
              KShadingProgramCommon.bindAttributeNormal(program, array);
              break;
            }
            case NORMAL_VERTEX:
            {
              KShadingProgramCommon.bindAttributeNormal(program, array);
              break;
            }
            case NORMAL_NONE:
            {
              break;
            }
          }

          if (label.impliesUV()) {
            KShadingProgramCommon.bindAttributeUV(program, array);
          }

          program.programExecute(new JCBProgramProcedure() {
            @Override public void call()
              throws ConstraintError,
                JCGLException,
                Throwable
            {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            }
          });

        } finally {
          gc.arrayBufferUnbind();
        }
      }
    });
  }

  /**
   * Render a specific opaque lit batch. Set any state that can be set on a
   * per-batch basis (typically the projection matrix and parameters
   * associated with the light).
   */

  protected static void renderOpaqueLitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitAllocator texture_units,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KLight light,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull List<KMeshInstanceTransformed> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    /**
     * Create a new texture unit context for per-light textures.
     */

    texture_units.withContext(new KTextureUnitWith() {
      @Override public void run(
        final @Nonnull KTextureUnitContext unit_context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        light.kLightAccept(new KLightVisitor<Unit, JCGLException>() {

          /**
           * Render the batch with a directional light.
           */

          @Override public Unit kLightVisitDirectional(
            final @Nonnull KDirectional l)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KRendererForwardActual
              .renderOpaqueLitBatchInstancesWithDirectional(
                gc,
                unit_context,
                mwo,
                label,
                instances,
                program,
                l);
            return Unit.unit();
          }

          /**
           * Render the batch with a projective light.
           */

          @Override public Unit kLightVisitProjective(
            final @Nonnull KProjective projective)
            throws ConstraintError,
              RException,
              JCGLException
          {
            return mwo.withProjectiveLight(
              projective,
              new MatricesProjectiveLightFunction<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesProjectiveLight mwp)
                  throws JCGLException,
                    ConstraintError,
                    RException
                {
                  KRendererForwardActual
                    .renderOpaqueLitBatchInstancesWithProjective(
                      gc,
                      shadow_context,
                      unit_context,
                      mwp,
                      label,
                      instances,
                      program,
                      projective);
                  return Unit.unit();
                }
              });
          }

          /**
           * Render the batch with a spherical light.
           */

          @Override public Unit kLightVisitSpherical(
            final @Nonnull KSphere l)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KRendererForwardActual
              .renderOpaqueLitBatchInstancesWithSpherical(
                gc,
                unit_context,
                mwo,
                label,
                instances,
                program,
                l);
            return Unit.unit();
          }
        });
      }
    });
  }

  /**
   * Render all instances in a specific batch when the current light is
   * directional.
   */

  protected static void renderOpaqueLitBatchInstancesWithDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull List<KMeshInstanceTransformed> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KDirectional l)
    throws JCGLException,
      RException,
      ConstraintError
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KMeshInstanceTransformed i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightDirectionalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererForwardActual.renderInstance(
              gc,
              unit_context,
              mwi,
              label,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  /**
   * Render all instances in a specific batch when the current light is
   * projective.
   */

  protected static void renderOpaqueLitBatchInstancesWithProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesProjectiveLight mwp,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull List<KMeshInstanceTransformed> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    if (light.hasShadow()) {
      KRendererForwardActual.putShadow(
        shadow_context,
        unit_context,
        program,
        light);
    }

    KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
      program,
      mwp.getMatrixContext(),
      mwp.getMatrixView(),
      light);

    KShadingProgramCommon.putMatrixProjectiveProjection(
      program,
      mwp.getMatrixProjectiveProjection());

    KShadingProgramCommon.putTextureProjection(
      program,
      unit_context.withTexture2D(light.getTexture()));

    for (final KMeshInstanceTransformed i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putMatrixProjectiveProjectionReuse(program);
      KShadingProgramCommon.putLightProjectiveWithoutTextureProjectionReuse(
        program,
        light);
      KShadingProgramCommon.putTextureProjectionReuse(program);

      if (light.hasShadow()) {
        KRendererForwardActual.putShadowReuse(program, light);
      }

      mwp.withInstance(
        i,
        new MatricesInstanceWithProjectiveFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceWithProjective mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KShadingProgramCommon.putMatrixProjectiveModelView(
              program,
              mwi.getMatrixProjectiveModelView());

            KRendererForwardActual.renderInstance(
              gc,
              unit_context,
              mwi,
              label,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  /**
   * Render all instances in a specific batch when the current light is
   * spherical.
   */

  protected static void renderOpaqueLitBatchInstancesWithSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull List<KMeshInstanceTransformed> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KSphere l)
    throws JCGLException,
      RException,
      ConstraintError
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KMeshInstanceTransformed i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightSphericalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererForwardActual.renderInstance(
              gc,
              unit_context,
              mwi,
              label,
              program,
              i);
            return Unit.unit();
          }
        });
    }
  }

  protected static void renderOpaqueUnlitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull List<KMeshInstanceTransformed> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    for (final KMeshInstanceTransformed instance : instances) {
      mwo.withInstance(
        instance,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            unit_allocator.withContext(new KTextureUnitWith() {
              @Override public void run(
                final @Nonnull KTextureUnitContext context)
                throws ConstraintError,
                  JCGLException,
                  RException
              {
                KRendererForwardActual.renderInstance(
                  gc,
                  context,
                  mwi,
                  label,
                  program,
                  instance);
              }
            });
            return Unit.unit();
          }
        });
    }
  }

  protected static void renderTranslucentLitInstanceWithLight(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext texture_units,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KLight light,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    light.kLightAccept(new KLightVisitor<Unit, JCGLException>() {
      @Override public Unit kLightVisitDirectional(
        final @Nonnull KDirectional l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        KRendererForwardActual
          .renderTranslucentLitInstanceWithLightDirectional(
            gc,
            texture_units,
            mwo,
            label,
            l,
            program,
            instance);
        return Unit.unit();
      }

      @Override public Unit kLightVisitProjective(
        final @Nonnull KProjective l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        return mwo.withProjectiveLight(
          l,
          new MatricesProjectiveLightFunction<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesProjectiveLight mwp)
              throws JCGLException,
                ConstraintError,
                RException
            {
              KRendererForwardActual
                .renderTranslucentLitInstanceWithLightProjective(
                  gc,
                  shadow_context,
                  texture_units,
                  mwp,
                  label,
                  l,
                  program,
                  instance);
              return Unit.unit();
            }
          });
      }

      @Override public Unit kLightVisitSpherical(
        final @Nonnull KSphere l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        KRendererForwardActual
          .renderTranslucentLitInstanceWithLightSpherical(
            gc,
            texture_units,
            mwo,
            label,
            l,
            program,
            instance);
        return Unit.unit();
      }
    });
  }

  protected static void renderTranslucentLitInstanceWithLightDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext texture_units,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KDirectional l,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new MatricesInstanceFunction<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstance mwi)
          throws JCGLException,
            ConstraintError,
            RException
        {
          KRendererForwardActual.renderInstance(
            gc,
            texture_units,
            mwi,
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  protected static void renderTranslucentLitInstanceWithLightProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext texture_units,
    final @Nonnull MatricesProjectiveLight mwp,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KProjective light,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    if (light.hasShadow()) {
      KRendererForwardActual.putShadow(
        shadow_context,
        texture_units,
        program,
        light);
    }

    KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
      program,
      mwp.getMatrixContext(),
      mwp.getMatrixView(),
      light);

    KShadingProgramCommon.putMatrixProjectiveProjection(
      program,
      mwp.getMatrixProjectiveProjection());

    KShadingProgramCommon.putTextureProjection(
      program,
      texture_units.withTexture2D(light.getTexture()));

    mwp.withInstance(
      instance,
      new MatricesInstanceWithProjectiveFunction<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstanceWithProjective mwi)
          throws JCGLException,
            ConstraintError,
            RException
        {
          KShadingProgramCommon.putMatrixProjectiveModelView(
            program,
            mwi.getMatrixProjectiveModelView());

          KRendererForwardActual.renderInstance(
            gc,
            texture_units,
            mwi,
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  protected static void renderTranslucentLitInstanceWithLightSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext texture_units,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KSphere l,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    mwo.withInstance(
      instance,
      new MatricesInstanceFunction<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstance mwi)
          throws JCGLException,
            ConstraintError,
            RException
        {
          KRendererForwardActual.renderInstance(
            gc,
            texture_units,
            mwi,
            label,
            program,
            instance);
          return Unit.unit();
        }
      });
  }

  private final @Nonnull VectorM4F                             background;
  private final @Nonnull KLabelDecider                         decider;
  private final @Nonnull KDepthRenderer                        depth;
  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull KShadowMapRenderer                    shadow_renderer;
  private final @Nonnull KTextureUnitAllocator                 texture_units;
  private final @Nonnull VectorM2I                             viewport_size;

  private KRendererForwardActual(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShadowMapRenderer shadow_renderer,
    final @Nonnull KLabelDecider decider,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    super(KRendererForwardActual.NAME);

    try {
      this.log =
        new Log(
          Constraints.constrainNotNull(log, "Log"),
          KRendererForwardActual.NAME);
      this.g = Constraints.constrainNotNull(g, "GL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(shader_cache, "Shader cache");
      this.shadow_renderer =
        Constraints.constrainNotNull(shadow_renderer, "Shadow renderer");
      this.decider = Constraints.constrainNotNull(decider, "Decider");
      this.matrices = KMutableMatrices.newMatrices();
      this.depth =
        KDepthRenderer.newDepthRenderer(g, shader_cache, caps, log);
      this.viewport_size = new VectorM2I();
      this.background = new VectorM4F();
      this.texture_units = KTextureUnitAllocator.newAllocator(g);

      this.log.debug("initialized");
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererClose()
    throws ConstraintError
  {
    // Nothing
  }

  @Override public KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(this.decider, this.decider, scene);

    final KCamera camera = scene.getCamera();
    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserver mwo)
            throws ConstraintError,
              JCGLException,
              RException
          {
            try {
              KRendererForwardActual.this.renderScene(
                camera,
                framebuffer,
                batched,
                mwo);
              return Unit.unit();
            } catch (final LUCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    VectorM4F.copy(rgba, this.background);
  }

  /**
   * Render opaque lit and unlit batches.
   */

  protected void renderOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws JCGLException,
      ConstraintError,
      RException,
      LUCacheException
  {
    this.renderOpaqueLitBatches(gc, shadow_context, batched, mwo);
    this.renderOpaqueUnlitBatches(gc, batched, mwo);
  }

  /**
   * Render opaque lit batches.
   */

  private void renderOpaqueLitBatches(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      JCGLException,
      RException,
      LUCacheException
  {
    final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> by_light =
      batched.getBatchesOpaqueLit();

    for (final KLight light : by_light.keySet()) {
      final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> by_label =
        by_light.get(light);

      for (final KMaterialForwardLabel label : by_label.keySet()) {
        final List<KMeshInstanceTransformed> instances = by_label.get(label);

        final String shader_name =
          KRendererForwardActual
            .makeLitShaderName(this.decider, light, label);

        final int required = label.kTexturesGetRequired();
        if (this.texture_units.hasEnoughUnits(required) == false) {
          throw RException.notEnoughTextureUnits(
            shader_name,
            required,
            this.texture_units.getUnitCount());
        }

        final KTextureUnitAllocator unit_allocator = this.texture_units;
        final KProgram kprogram = this.shader_cache.luCacheGet(shader_name);
        kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
          @Override public void call(
            final JCBProgram program)
            throws ConstraintError,
              JCGLException,
              RException
          {
            KRendererForwardActual.renderOpaqueLitBatch(
              gc,
              shadow_context,
              unit_allocator,
              mwo,
              light,
              label,
              instances,
              program);
          }
        });
      }
    }
  }

  /**
   * Render opaque unlit batches.
   */

  private void renderOpaqueUnlitBatches(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      RException,
      LUCacheException,
      JCGLException
  {
    final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> unlit =
      batched.getBatchesOpaqueUnlit();

    for (final KMaterialForwardLabel label : unlit.keySet()) {
      final List<KMeshInstanceTransformed> instances = unlit.get(label);

      final String shader_name =
        KRendererForwardActual.makeUnlitShaderName(label);

      final KTextureUnitAllocator units = this.texture_units;
      final KProgram kprogram = this.shader_cache.luCacheGet(shader_name);
      kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final JCBProgram program)
          throws ConstraintError,
            JCGLException,
            RException
        {
          KRendererForwardActual.renderOpaqueUnlitBatch(
            gc,
            units,
            mwo,
            label,
            instances,
            program);
        }
      });
    }
  }

  protected void renderScene(
    final @Nonnull KCamera camera,
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      RException,
      LUCacheException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    /**
     * Populate depth buffer with opaque objects.
     */

    this.depth.depthRendererEvaluate(
      new RMatrixI4x4F<RTransformView>(mwo.getMatrixView()),
      new RMatrixI4x4F<RTransformProjection>(mwo.getMatrixProjection()),
      batched.getBatchesDepth(),
      framebuffer,
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

    final VectorM2I viewport = this.viewport_size;
    final VectorM4F backdrop = this.background;

    /**
     * Render shadow maps.
     */

    this.shadow_renderer.shadowMapRendererEvaluate(
      camera,
      batched.getBatchedShadow(),
      new KShadowMapWith<Unit, LUCacheException>() {
        @Override public Unit withMaps(
          final @Nonnull KShadowMapContext shadow_context)
          throws ConstraintError,
            JCGLException,
            RException,
            LUCacheException
        {
          /**
           * Render scene with rendered shadow maps.
           */

          final FramebufferReferenceUsable fb =
            framebuffer.kFramebufferGetColorFramebuffer();

          gc.framebufferDrawBind(fb);
          try {
            final AreaInclusive area = framebuffer.kFramebufferGetArea();
            viewport.x = (int) area.getRangeX().getInterval();
            viewport.y = (int) area.getRangeY().getInterval();
            gc.viewportSet(VectorI2I.ZERO, viewport);

            /**
             * Render all opaque instances, blending additively, into the
             * framebuffer.
             */

            gc.cullingEnable(
              FaceSelection.FACE_BACK,
              FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClearV4f(backdrop);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
            gc.depthBufferWriteDisable();
            gc.blendingEnable(
              BlendFunction.BLEND_ONE,
              BlendFunction.BLEND_ONE);
            KRendererForwardActual.this.renderOpaque(
              gc,
              shadow_context,
              batched,
              mwo);

            /**
             * Render all translucent instances into the framebuffer.
             */

            gc.cullingDisable();
            gc.colorBufferMask(true, true, true, true);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
            gc.depthBufferWriteDisable();
            KRendererForwardActual.this.renderTranslucent(
              gc,
              shadow_context,
              batched,
              mwo);

          } finally {
            gc.framebufferDrawUnbind();
          }

          return Unit.unit();
        }
      });

  }

  protected void renderTranslucent(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws JCGLException,
      ConstraintError,
      RException,
      LUCacheException
  {
    final List<KTranslucent> translucents = batched.getBatchesTranslucent();

    for (final KTranslucent translucent : translucents) {
      translucent
        .translucentAccept(new KTranslucentVisitor<Unit, LUCacheException>() {
          @Override public Unit translucentVisitLit(
            final @Nonnull KTranslucentLit t)
            throws ConstraintError,
              JCGLException,
              RException,
              LUCacheException
          {
            KRendererForwardActual.this.renderTranslucentLit(
              gc,
              shadow_context,
              mwo,
              t);
            return Unit.unit();
          }

          @Override public Unit translucentVisitUnlit(
            final @Nonnull KTranslucentUnlit t)
            throws ConstraintError,
              JCGLException,
              RException,
              LUCacheException
          {
            KRendererForwardActual.this.renderTranslucentUnlit(gc, mwo, t);
            return Unit.unit();
          }
        });
    }
  }

  protected void renderTranslucentLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KTranslucentLit t)
    throws ConstraintError,
      RException,
      JCGLException,
      LUCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLight> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLight> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLight light = iter.next();
      final KMeshInstanceTransformed instance = t.translucentGetInstance();
      final KMaterialForwardLabel label =
        this.decider.getForwardLabel(instance.getInstance());

      final String shader_name =
        KRendererForwardActual.makeLitShaderName(this.decider, light, label);

      final int required = label.kTexturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnits(
          shader_name,
          required,
          unit_allocator.getUnitCount());
      }

      /**
       * The first light that falls upon a translucent surface essentially
       * provides the degree of opacity for that surface. Further light
       * contributions apply lighting to the object.
       */

      if (first) {
        gc.blendingEnable(
          BlendFunction.BLEND_ONE,
          BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
      } else {
        gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      }

      final KProgram kprogram = this.shader_cache.luCacheGet(shader_name);
      kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            RException
        {
          unit_allocator.withContext(new KTextureUnitWith() {
            @Override public void run(
              final @Nonnull KTextureUnitContext texture_unit_ctx)
              throws ConstraintError,
                JCGLException,
                RException
            {
              KRendererForwardActual.renderTranslucentLitInstanceWithLight(
                gc,
                shadow_context,
                texture_unit_ctx,
                mwo,
                label,
                light,
                program,
                instance);
            }
          });
        }
      });

      first = false;
    }
  }

  protected void renderTranslucentUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KTranslucentUnlit t)
    throws ConstraintError,
      RException,
      LUCacheException,
      JCGLException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final KMeshInstanceTransformed instance = t.translucentGetInstance();
    final KMaterialForwardLabel label =
      this.decider.getForwardLabel(instance.getInstance());

    final String shader_name =
      KRendererForwardActual.makeUnlitShaderName(label);

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KProgram kprogram = this.shader_cache.luCacheGet(shader_name);
    kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
      @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          RException
      {
        unit_allocator.withContext(new KTextureUnitWith() {
          @Override public void run(
            final @Nonnull KTextureUnitContext texture_unit_ctx)
            throws ConstraintError,
              JCGLException,
              RException
          {
            mwo.withInstance(
              instance,
              new MatricesInstanceFunction<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesInstance mwi)
                  throws JCGLException,
                    ConstraintError,
                    RException
                {
                  KShadingProgramCommon.putMatrixProjection(
                    program,
                    mwi.getMatrixProjection());

                  KRendererForwardActual.renderInstance(
                    gc,
                    texture_unit_ctx,
                    mwi,
                    label,
                    program,
                    instance);
                  return Unit.unit();
                }
              });
          }
        });
      }
    });
  }
}
