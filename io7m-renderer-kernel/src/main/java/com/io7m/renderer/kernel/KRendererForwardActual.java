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
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
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
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjective;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceWithProjectiveFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLight;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesProjectiveLightFunction;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceOpaque;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightVisitor;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaque;
import com.io7m.renderer.kernel.types.KMaterialRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KShadow;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowVisitor;
import com.io7m.renderer.kernel.types.KTranslucent;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentVisitor;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformView;

@SuppressWarnings("synthetic-access") public final class KRendererForwardActual extends
  KAbstractRendererForward
{
  private static final @Nonnull String NAME;

  static {
    NAME = "forward";
  }

  private static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialLabelRegular label)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixModelView(
      program,
      mwi.getMatrixModelView());

    if (label.labelImpliesUV()) {
      KShadingProgramCommon.putMatrixUV(program, mwi.getMatrixUV());
    }

    switch (label.labelGetNormal()) {
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

    switch (label.labelGetEnvironment()) {
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

  private static void putInstanceTextures(
    final @Nonnull KTextureUnitContext units,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialRegular material)
    throws JCGLException,
      ConstraintError
  {
    switch (label.labelGetAlbedo()) {
      case ALBEDO_COLOURED:
      {
        break;
      }
      case ALBEDO_TEXTURED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.materialGetAlbedo().getTexture();
        KShadingProgramCommon.putTextureAlbedo(
          program,
          units.withTexture2D(some.value));
        break;
      }
    }

    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.materialGetNormal().getTexture();
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

    switch (label.labelGetEmissive()) {
      case EMISSIVE_MAPPED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.materialGetEmissive().getTexture();
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

    if (label.labelImpliesSpecularMap()) {
      final Some<Texture2DStatic> some =
        (Some<Texture2DStatic>) material.materialGetSpecular().getTexture();
      KShadingProgramCommon.putTextureSpecular(
        program,
        units.withTexture2D(some.value));
    }

    switch (label.labelGetEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        final Some<TextureCubeStatic> some =
          (Some<TextureCubeStatic>) material
            .materialGetEnvironment()
            .getTexture();
        KShadingProgramCommon.putTextureEnvironment(
          program,
          units.withTextureCube(some.value));
        break;
      }
    }
  }

  private static void putMaterialOpaque(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull KMaterialOpaque material)
    throws JCGLException,
      ConstraintError
  {
    KRendererForwardActual.putMaterialRegular(program, label, material);
  }

  private static void putMaterialRegular(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull KMaterialRegular material)
    throws ConstraintError,
      JCGLException
  {
    KShadingProgramCommon.putMaterialAlbedo(
      program,
      material.materialGetAlbedo());

    switch (label.labelGetEmissive()) {
      case EMISSIVE_CONSTANT:
      case EMISSIVE_MAPPED:
      {
        KShadingProgramCommon.putMaterialEmissive(
          program,
          material.materialGetEmissive());
        break;
      }
      case EMISSIVE_NONE:
      {
        break;
      }
    }

    switch (label.labelGetEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        KShadingProgramCommon.putMaterialEnvironment(
          program,
          material.materialGetEnvironment());
        break;
      }
    }

    switch (label.labelGetSpecular()) {
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        KShadingProgramCommon.putMaterialSpecular(
          program,
          material.materialGetSpecular());
        break;
      }
      case SPECULAR_NONE:
      {
        break;
      }
    }
  }

  private static void putMaterialTranslucentRegular(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull KMaterialTranslucentRegular material)
    throws JCGLException,
      ConstraintError
  {
    KRendererForwardActual.putMaterialRegular(program, label, material);
    KShadingProgramCommon.putMaterialAlphaOpacity(program, material
      .materialGetAlpha()
      .getOpacity());
  }

  private static void putShadow(
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadow> some = (Some<KShadow>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitor<Unit, JCGLException>() {
      @Override public Unit shadowVisitMappedBasic(
        final @Nonnull KShadowMappedBasic s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        final KShadowMapBasic map =
          (KShadowMapBasic) shadow_context.getShadowMap(s);

        final TextureUnit unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowBasic(program, s);
        KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
        return Unit.unit();
      }

      @Override public Unit shadowVisitMappedVariance(
        final @Nonnull KShadowMappedVariance s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        final KShadowMapVariance map =
          (KShadowMapVariance) shadow_context.getShadowMap(s);
        final TextureUnit unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthVarianceTexture());

        KShadingProgramCommon.putShadowVariance(program, s);
        KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
        return Unit.unit();
      }
    });
  }

  private static void putShadowReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadow> some = (Some<KShadow>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitor<Unit, JCGLException>() {
      @Override public Unit shadowVisitMappedBasic(
        final @Nonnull KShadowMappedBasic s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        return Unit.unit();
      }

      @Override public Unit shadowVisitMappedVariance(
        final @Nonnull KShadowMappedVariance s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        KShadingProgramCommon.putShadowVarianceReuse(program);
        KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
        return Unit.unit();
      }
    });
  }

  public static @Nonnull KRendererForwardActual rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShadowMapRenderer shadow_renderer,
    final @Nonnull KRefractionRenderer refraction_renderer,
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
      refraction_renderer,
      decider,
      shader_cache,
      caps,
      log);
  }

  /**
   * Render a specific opaque instance, assuming all program state for the
   * current light (if any) has been configured.
   */

  private static void renderInstanceOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext units,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedOpaque instance)
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
        final KMesh mesh = instance.instanceGetMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();
        final KInstanceOpaque actual = instance.instanceGet();
        final KMaterialOpaque material = actual.instanceGetMaterial();

        KRendererForwardActual.putInstanceMatrices(program, mwi, label);
        KRendererForwardActual.putInstanceTextures(
          context,
          label,
          program,
          material);
        KRendererForwardActual.putMaterialOpaque(program, label, material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(program, array);

          switch (label.labelGetNormal()) {
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

          if (label.labelImpliesUV()) {
            KShadingProgramCommon.bindAttributeUV(program, array);
          }

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            actual.instanceGetFaces());

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
   * Render a specific regular translucent instance, assuming all program
   * state for the current light (if any) has been configured.
   */

  private static void renderInstanceTranslucentRegular(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext units,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialLabelRegular label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
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
        final KMesh mesh = instance.instanceGetMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();
        final KInstanceTranslucentRegular actual = instance.getInstance();
        final KMaterialTranslucentRegular material =
          actual.instanceGetMaterial();

        KRendererForwardActual.putInstanceMatrices(program, mwi, label);
        KRendererForwardActual.putInstanceTextures(
          context,
          label,
          program,
          material);
        KRendererForwardActual.putMaterialTranslucentRegular(
          program,
          label,
          material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(program, array);

          switch (label.labelGetNormal()) {
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

          if (label.labelImpliesUV()) {
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

  private static void renderInstanceTranslucentRegularLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext texture_unit_ctx,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLight light,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    light.lightVisitableAccept(new KLightVisitor<Unit, JCGLException>() {
      @Override public Unit lightVisitDirectional(
        final @Nonnull KLightDirectional l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        KRendererForwardActual
          .renderInstanceTranslucentRegularLitDirectional(
            gc,
            texture_unit_ctx,
            mwo,
            label,
            l,
            program,
            instance);
        return Unit.unit();
      }

      @Override public Unit lightVisitProjective(
        final @Nonnull KLightProjective l)
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
                .renderInstanceTranslucentRegularLitProjective(
                  gc,
                  shadow_context,
                  texture_unit_ctx,
                  mwp,
                  label,
                  l,
                  program,
                  instance);
              return Unit.unit();
            }
          });
      }

      @Override public Unit lightVisitSpherical(
        final @Nonnull KLightSphere l)
        throws ConstraintError,
          RException,
          JCGLException
      {
        KRendererForwardActual.renderInstanceTranslucentRegularLitSpherical(
          gc,
          texture_unit_ctx,
          mwo,
          label,
          l,
          program,
          instance);
        return Unit.unit();
      }
    });
  }

  private static void renderInstanceTranslucentRegularLitDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext texture_unit_ctx,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightDirectional l,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
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
          KRendererForwardActual.renderInstanceTranslucentRegular(
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

  private static void renderInstanceTranslucentRegularLitProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext texture_unit_ctx,
    final @Nonnull MatricesProjectiveLight mwp,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightProjective light,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      RException,
      ConstraintError
  {
    if (light.lightHasShadow()) {
      KRendererForwardActual.putShadow(
        shadow_context,
        texture_unit_ctx,
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
      texture_unit_ctx.withTexture2D(light.getTexture()));

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

          KRendererForwardActual.renderInstanceTranslucentRegular(
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

  private static void renderInstanceTranslucentRegularLitSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext texture_unit_ctx,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightSphere l,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
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
          KRendererForwardActual.renderInstanceTranslucentRegular(
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

  private static void renderOpaqueLitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KLight light,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> instances,
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

    unit_allocator.withContext(new KTextureUnitWith() {
      @Override public void run(
        final @Nonnull KTextureUnitContext unit_context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        light.lightVisitableAccept(new KLightVisitor<Unit, JCGLException>() {

          /**
           * Render the batch with a directional light.
           */

          @Override public Unit lightVisitDirectional(
            final @Nonnull KLightDirectional l)
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

          @Override public Unit lightVisitProjective(
            final @Nonnull KLightProjective projective)
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

          @Override public Unit lightVisitSpherical(
            final @Nonnull KLightSphere l)
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

  private static void renderOpaqueLitBatchInstancesWithDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightDirectional l)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightDirectional(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceTransformedOpaque i : instances) {
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
            KRendererForwardActual.renderInstanceOpaque(
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

  private static void renderOpaqueLitBatchInstancesWithProjective(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesProjectiveLight mwp,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws RException,
      JCGLException,
      ConstraintError
  {
    if (light.lightHasShadow()) {
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

    for (final KInstanceTransformedOpaque i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putMatrixProjectiveProjectionReuse(program);
      KShadingProgramCommon.putLightProjectiveWithoutTextureProjectionReuse(
        program,
        light);
      KShadingProgramCommon.putTextureProjectionReuse(program);

      if (light.lightHasShadow()) {
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

            KRendererForwardActual.renderInstanceOpaque(
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

  private static void renderOpaqueLitBatchInstancesWithSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitContext unit_context,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> instances,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightSphere l)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putLightSpherical(
      program,
      mwo.getMatrixContext(),
      mwo.getMatrixView(),
      l);

    for (final KInstanceTransformedOpaque i : instances) {
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
            KRendererForwardActual.renderInstanceOpaque(
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

  private static void renderOpaqueUnlitBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KTextureUnitAllocator units,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull KMaterialForwardOpaqueUnlitLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    for (final KInstanceTransformedOpaque instance : instances) {
      mwo.withInstance(
        instance,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            units.withContext(new KTextureUnitWith() {
              @Override public void run(
                final @Nonnull KTextureUnitContext context)
                throws ConstraintError,
                  JCGLException,
                  RException
              {
                KRendererForwardActual.renderInstanceOpaque(
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

  private final @Nonnull VectorM4F                             background;

  private final @Nonnull KLabelDecider                         decider;
  private final @Nonnull KDepthRenderer                        depth;
  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull KRefractionRenderer                   refraction_renderer;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull KShadowMapRenderer                    shadow_renderer;
  private final @Nonnull KTextureUnitAllocator                 texture_units;
  private final @Nonnull VectorM2I                             viewport_size;

  private KRendererForwardActual(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShadowMapRenderer shadow_renderer,
    final @Nonnull KRefractionRenderer refraction_renderer,
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
      this.refraction_renderer =
        Constraints.constrainNotNull(
          refraction_renderer,
          "Refraction renderer");
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
    throws RException,
      ConstraintError
  {
    // Nothing
  }

  @Override public KRendererDebugging rendererDebug()
  {
    // TODO: Implement debugging
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
            } catch (final JCacheException e) {
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

  private void renderOpaques(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws RException,
      JCGLException,
      ConstraintError,
      JCacheException
  {
    this.renderOpaquesLit(gc, shadow_context, batched, mwo);
    this.renderOpaquesUnlit(gc, batched, mwo);
  }

  private void renderOpaquesLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      RException,
      JCacheException,
      JCGLException
  {
    final Map<KLight, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaque>>> by_light =
      batched.getBatchesOpaqueLit();

    for (final KLight light : by_light.keySet()) {
      final Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaque>> by_label =
        by_light.get(light);

      for (final KMaterialForwardOpaqueLitLabel label : by_label.keySet()) {
        final List<KInstanceTransformedOpaque> instances =
          by_label.get(label);

        final int required = label.texturesGetRequired();
        if (this.texture_units.hasEnoughUnits(required) == false) {
          throw RException.notEnoughTextureUnits(
            label.labelGetCode(),
            required,
            this.texture_units.getUnitCount());
        }

        final KTextureUnitAllocator unit_allocator = this.texture_units;
        final KProgram kprogram =
          this.shader_cache.cacheGetLU(label.labelGetCode());
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

  private void renderOpaquesUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws RException,
      ConstraintError,
      JCacheException,
      JCGLException
  {
    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaque>> unlit =
      batched.getBatchesOpaqueUnlit();

    for (final KMaterialForwardOpaqueUnlitLabel label : unlit.keySet()) {
      final List<KInstanceTransformedOpaque> instances = unlit.get(label);

      final KTextureUnitAllocator units = this.texture_units;
      final KProgram kprogram =
        this.shader_cache.cacheGetLU(label.labelGetCode());
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

  private void renderScene(
    final @Nonnull KCamera camera,
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    /**
     * Populate depth buffer with opaque objects.
     */

    final RMatrixI4x4F<RTransformView> m_view =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixView());
    final RMatrixI4x4F<RTransformProjection> m_proj =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixProjection());

    final Option<KFaceSelection> none = Option.none();
    this.depth.depthRendererEvaluate(
      m_view,
      m_proj,
      batched.getBatchesDepth(),
      framebuffer,
      none);

    final VectorM2I viewport = this.viewport_size;
    final VectorM4F backdrop = this.background;

    /**
     * Render shadow maps.
     */

    this.shadow_renderer.shadowMapRendererEvaluate(
      camera,
      batched.getBatchedShadow(),
      new KShadowMapWith<Unit, JCacheException>() {
        @Override public Unit withMaps(
          final @Nonnull KShadowMapContext shadow_context)
          throws ConstraintError,
            JCGLException,
            RException,
            JCacheException
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
            KRendererForwardActual.this.renderOpaques(
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
            KRendererForwardActual.this.renderTranslucents(
              gc,
              framebuffer,
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

  private void renderTranslucentRefractive(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KInstanceTransformedTranslucentRefractive t,
    final @Nonnull MatricesObserver mwo)
    throws RException,
      ConstraintError
  {
    this.refraction_renderer.rendererRefractionEvaluate(framebuffer, mwo, t);
  }

  private void renderTranslucentRegularLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KTranslucentRegularLit t,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLight> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLight> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLight light = iter.next();
      final KInstanceTransformedTranslucentRegular instance =
        t.translucentGetInstance();

      final KMaterialForwardTranslucentRegularLitLabel label =
        this.decider.getForwardLabelTranslucentRegularLit(
          light,
          instance.getInstance());

      final int required = label.texturesGetRequired();
      if (unit_allocator.hasEnoughUnits(required) == false) {
        throw RException.notEnoughTextureUnits(
          label.labelGetCode(),
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

      final KProgram kprogram =
        this.shader_cache.cacheGetLU(label.labelGetCode());
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
              KRendererForwardActual.renderInstanceTranslucentRegularLit(
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

  private void renderTranslucentRegularUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KInstanceTransformedTranslucentRegular t,
    final @Nonnull MatricesObserver mwo)
    throws ConstraintError,
      JCGLException,
      RException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final KInstanceTranslucentRegular instance = t.getInstance();
    final KMaterialForwardTranslucentRegularUnlitLabel label =
      this.decider.getForwardLabelTranslucentRegularUnlit(instance);

    gc.blendingEnable(
      BlendFunction.BLEND_ONE,
      BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);

    final KProgram kprogram =
      this.shader_cache.cacheGetLU(label.labelGetCode());
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
              t,
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

                  KRendererForwardActual.renderInstanceTranslucentRegular(
                    gc,
                    texture_unit_ctx,
                    mwi,
                    label,
                    program,
                    t);
                  return Unit.unit();
                }
              });
          }
        });
      }
    });
  }

  private void renderTranslucents(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KShadowMapContext shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserver mwo)
    throws JCGLException,
      RException,
      ConstraintError,
      JCacheException
  {
    final List<KTranslucent> translucents = batched.getBatchesTranslucent();

    for (int index = 0; index < translucents.size(); ++index) {
      gc.cullingDisable();

      final KTranslucent translucent = translucents.get(index);
      translucent
        .translucentAccept(new KTranslucentVisitor<Unit, JCacheException>() {
          @Override public Unit translucentVisitRefractive(
            final @Nonnull KInstanceTransformedTranslucentRefractive t)
            throws JCGLException,
              RException,
              ConstraintError
          {
            KRendererForwardActual.this.renderTranslucentRefractive(
              gc,
              framebuffer,
              t,
              mwo);
            return Unit.unit();
          }

          @Override public Unit translucentVisitRegularLit(
            final @Nonnull KTranslucentRegularLit t)
            throws JCGLException,
              RException,
              ConstraintError,
              JCacheException
          {
            KRendererCommon.renderConfigureFaceCulling(gc, t
              .translucentGetInstance()
              .getInstance()
              .instanceGetFaces());

            KRendererForwardActual.this.renderTranslucentRegularLit(
              gc,
              shadow_context,
              t,
              mwo);
            return Unit.unit();
          }

          @Override public Unit translucentVisitRegularUnlit(
            final @Nonnull KInstanceTransformedTranslucentRegular t)
            throws JCGLException,
              RException,
              ConstraintError,
              JCacheException
          {
            KRendererCommon.renderConfigureFaceCulling(gc, t
              .getInstance()
              .instanceGetFaces());

            KRendererForwardActual.this.renderTranslucentRegularUnlit(
              gc,
              t,
              mwo);
            return Unit.unit();
          }
        });
    }
  }
}
