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
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceWithProjectiveFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesProjectiveLightFunctionType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightVisitorType;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRegularUnlitLabel;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialRegularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentVisitorType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The primary forward renderer.
 */

@SuppressWarnings("synthetic-access") public final class KRendererForwardActual extends
  KAbstractRendererForward
{
  private static final @Nonnull String NAME;

  static {
    NAME = "forward";
  }

  private static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label)
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
    final @Nonnull KTextureUnitContextType units,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialRegularType material)
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
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull KMaterialOpaqueType material)
    throws JCGLException,
      ConstraintError
  {
    KRendererForwardActual.putMaterialRegular(program, label, material);
  }

  private static void putMaterialRegular(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull KMaterialRegularType material)
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
    final @Nonnull KMaterialLabelRegularType label,
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
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
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
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

  /**
   * Construct a new forward renderer.
   * 
   * @param g
   *          The OpenGL interface
   * @param shadow_renderer
   *          The shadow map renderer
   * @param refraction_renderer
   *          The refraction renderer
   * @param decider
   *          The label cache
   * @param shader_cache
   *          The shader cache
   * @param caps
   *          The capabilities of the current OpenGL implementation
   * @param log
   *          A log handle
   * @return A new forward renderer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws RException
   *           If an error occurs
   */

  public static @Nonnull KRendererForwardActual rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShadowMapRendererType shadow_renderer,
    final @Nonnull KRefractionRendererType refraction_renderer,
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
    final @Nonnull KTextureUnitContextType units,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedOpaqueType instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        final KMesh mesh = instance.instanceGetMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();
        final KInstanceOpaqueType actual = instance.instanceGet();
        final KMaterialOpaqueType material = actual.instanceGetMaterial();

        KRendererCommon.renderConfigureFaceCulling(
          gc,
          actual.instanceGetFaces());

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
                JCGLException
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
    final @Nonnull KTextureUnitContextType units,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
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
                JCGLException
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardTranslucentRegularLitLabel label,
    final @Nonnull KLightType light,
    final @Nonnull JCBProgram program,
    final @Nonnull KInstanceTransformedTranslucentRegular instance)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    light.lightVisitableAccept(new KLightVisitorType<Unit, JCGLException>() {
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
          new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
            @Override public Unit run(
              final MatricesProjectiveLightType mwp)
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
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstanceType mwi)
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesProjectiveLightType mwp,
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
      new MatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstanceWithProjectiveType mwi)
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
    final @Nonnull KTextureUnitContextType texture_unit_ctx,
    final @Nonnull MatricesObserverType mwo,
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
      new MatricesInstanceFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final @Nonnull MatricesInstanceType mwi)
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KLightType light,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
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

    unit_allocator.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final @Nonnull KTextureUnitContextType unit_context)
        throws ConstraintError,
          JCGLException,
          RException
      {
        light.lightVisitableAccept(new KLightVisitorType<Unit, JCGLException>() {

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
              new MatricesProjectiveLightFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesProjectiveLightType mwp)
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
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
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

    for (final KInstanceTransformedOpaqueType i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightDirectionalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesProjectiveLightType mwp,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
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

    for (final KInstanceTransformedOpaqueType i : instances) {
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
        new MatricesInstanceWithProjectiveFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceWithProjectiveType mwi)
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
    final @Nonnull KTextureUnitContextType unit_context,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueLitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
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

    for (final KInstanceTransformedOpaqueType i : instances) {
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putLightSphericalReuse(program);
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
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
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull KMaterialForwardOpaqueUnlitLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> instances,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError,
      RException
  {
    KShadingProgramCommon.putMatrixProjection(
      program,
      mwo.getMatrixProjection());

    for (final KInstanceTransformedOpaqueType instance : instances) {
      mwo.withInstance(
        instance,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final @Nonnull KTextureUnitContextType context)
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
  private final @Nonnull KMutableMatricesType                      matrices;
  private final @Nonnull KRefractionRendererType                   refraction_renderer;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull KShadowMapRendererType                    shadow_renderer;
  private final @Nonnull KTextureUnitAllocator                 texture_units;

  private KRendererForwardActual(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull KShadowMapRendererType in_shadow_renderer,
    final @Nonnull KRefractionRendererType in_refraction_renderer,
    final @Nonnull KLabelDecider in_decider,
    final @Nonnull LUCache<String, KProgram, RException> in_shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    super(KRendererForwardActual.NAME);

    try {
      this.log =
        new Log(
          Constraints.constrainNotNull(in_log, "Log"),
          KRendererForwardActual.NAME);
      this.g = Constraints.constrainNotNull(in_g, "GL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.shadow_renderer =
        Constraints.constrainNotNull(in_shadow_renderer, "Shadow renderer");
      this.refraction_renderer =
        Constraints.constrainNotNull(
          in_refraction_renderer,
          "Refraction renderer");
      this.decider = Constraints.constrainNotNull(in_decider, "Decider");
      this.matrices = KMutableMatricesType.newMatrices();
      this.depth =
        KDepthRenderer.newDepthRenderer(in_g, in_shader_cache, caps, in_log);
      this.background = new VectorM4F();
      this.texture_units = KTextureUnitAllocator.newAllocator(in_g);

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

  @Override public KRendererDebuggingType rendererDebug()
  {
    // TODO: Implement debugging
    return null;
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsableType framebuffer,
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
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserverType mwo)
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
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
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCacheException,
      JCGLException
  {
    final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> by_light =
      batched.getBatchesOpaqueLit();

    for (final KLightType light : by_light.keySet()) {
      final Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>> by_label =
        by_light.get(light);

      for (final KMaterialForwardOpaqueLitLabel label : by_label.keySet()) {
        final List<KInstanceTransformedOpaqueType> instances =
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
    final @Nonnull MatricesObserverType mwo)
    throws RException,
      ConstraintError,
      JCacheException,
      JCGLException
  {
    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>> unlit =
      batched.getBatchesOpaqueUnlit();

    for (final KMaterialForwardOpaqueUnlitLabel label : unlit.keySet()) {
      final List<KInstanceTransformedOpaqueType> instances = unlit.get(label);

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
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final RMatrixI4x4F<RTransformViewType> m_view =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixView());
    final RMatrixI4x4F<RTransformProjectionType> m_proj =
      RMatrixI4x4F.newFromReadable(mwo.getMatrixProjection());

    /**
     * Populate depth buffer with opaque objects.
     */

    final Option<KFaceSelection> none = Option.none();
    this.depth.depthRendererEvaluate(
      m_view,
      m_proj,
      batched.getBatchesDepth(),
      framebuffer,
      none);

    /**
     * Render shadow maps.
     */

    this.shadow_renderer.shadowMapRendererEvaluate(
      camera,
      batched.getBatchedShadow(),
      new KShadowMapWithType<Unit, JCacheException>() {
        @Override public Unit withMaps(
          final @Nonnull KShadowMapContextType shadow_context)
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
            gc.viewportSet(framebuffer.kFramebufferGetArea());

            /**
             * Render all opaque instances, blending additively, into the
             * framebuffer. Only objects with depths exactly equal to that in
             * the depth buffer will be rendered.
             */

            gc.blendingEnable(
              BlendFunction.BLEND_ONE,
              BlendFunction.BLEND_ONE);

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClearV4f(KRendererForwardActual.this.background);

            gc.cullingEnable(
              FaceSelection.FACE_BACK,
              FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

            gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
            gc.depthBufferWriteDisable();

            KRendererForwardActual.this.renderOpaques(
              gc,
              shadow_context,
              batched,
              mwo);

            /**
             * Render all translucent instances into the framebuffer.
             */

            // Enabled by each translucent instance
            gc.blendingDisable();

            gc.colorBufferMask(true, true, true, true);

            // Enabled by each translucent instance
            gc.cullingDisable();

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
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KInstanceTransformedTranslucentRefractive t,
    final @Nonnull MatricesObserverType mwo)
    throws RException,
      ConstraintError
  {
    this.refraction_renderer.rendererRefractionEvaluate(framebuffer, mwo, t);
  }

  private void renderTranslucentRegularLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KTranslucentRegularLit t,
    final @Nonnull MatricesObserverType mwo)
    throws ConstraintError,
      RException,
      JCGLException,
      JCacheException
  {
    final KTextureUnitAllocator unit_allocator = this.texture_units;
    final Set<KLightType> lights = t.translucentGetLights();

    boolean first = true;
    final Iterator<KLightType> iter = lights.iterator();
    while (iter.hasNext()) {
      final KLightType light = iter.next();
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
          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final @Nonnull KTextureUnitContextType texture_unit_ctx)
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
    final @Nonnull MatricesObserverType mwo)
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
        unit_allocator.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final @Nonnull KTextureUnitContextType texture_unit_ctx)
            throws ConstraintError,
              JCGLException,
              RException
          {
            mwo.withInstance(
              t,
              new MatricesInstanceFunctionType<Unit, JCGLException>() {
                @Override public Unit run(
                  final @Nonnull MatricesInstanceType mwi)
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
    final @Nonnull KFramebufferForwardUsableType framebuffer,
    final @Nonnull KShadowMapContextType shadow_context,
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull MatricesObserverType mwo)
    throws JCGLException,
      RException,
      ConstraintError,
      JCacheException
  {
    final List<KTranslucentType> translucents = batched.getBatchesTranslucent();

    for (int index = 0; index < translucents.size(); ++index) {
      gc.cullingDisable();

      final KTranslucentType translucent = translucents.get(index);
      translucent
        .translucentAccept(new KTranslucentVisitorType<Unit, JCacheException>() {
          @Override public Unit translucentVisitRefractive(
            final @Nonnull KInstanceTransformedTranslucentRefractive t)
            throws JCGLException,
              RException,
              ConstraintError
          {
            KRendererForwardActual.this.renderTranslucentRefractive(
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
