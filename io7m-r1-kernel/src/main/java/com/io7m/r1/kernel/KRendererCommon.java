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

package com.io7m.r1.kernel;

import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable4x4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.r1.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KLightWithShadowVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialRegularType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialSpecularVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KShadowMappedBasic;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.kernel.types.KShadowType;
import com.io7m.r1.types.RException;

/**
 * Functions common to all renderers.
 */

@EqualityReference final class KRendererCommon
{
  /**
   * <p>
   * Produce a "normal" matrix from the given matrix <code>m</code>, writing
   * the result to <code>mr</code>.
   * </p>
   * <p>
   * The "normal" matrix is the original matrix with the translation elements
   * removed and any scaling inverted; it is the transpose of the inverse of
   * the upper left 3x3 matrix of <code>m</code>.
   * </p>
   */

  static void makeNormalMatrix(
    final MatrixReadable4x4FType m,
    final MatrixM3x3F mr)
  {
    mr.set(0, 0, m.getRowColumnF(0, 0));
    mr.set(1, 0, m.getRowColumnF(1, 0));
    mr.set(2, 0, m.getRowColumnF(2, 0));
    mr.set(0, 1, m.getRowColumnF(0, 1));
    mr.set(1, 1, m.getRowColumnF(1, 1));
    mr.set(2, 1, m.getRowColumnF(2, 1));
    mr.set(0, 2, m.getRowColumnF(0, 2));
    mr.set(1, 2, m.getRowColumnF(1, 2));
    mr.set(2, 2, m.getRowColumnF(2, 2));
    MatrixM3x3F.invertInPlace(mr);
    MatrixM3x3F.transposeInPlace(mr);
  }

  static void putInstanceMatricesRegular(
    final JCBProgramType program,
    final KMatricesInstanceValuesType mwi,
    final KMaterialRegularType material)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
    }

    material.materialRegularGetEnvironment().environmentAccept(
      new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMatrixInverseView(
            program,
            mwi.getMatrixViewInverse());
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMatrixInverseView(
            program,
            mwi.getMatrixViewInverse());
          return Unit.unit();
        }
      });
  }

  static void putInstanceMatricesSpecularOnly(
    final JCBProgramType program,
    final KMatricesInstanceValuesType mwi,
    final KMaterialTranslucentSpecularOnly material)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
    }
  }

  static void putInstanceTexturesRegular(
    final KTextureUnitContextType units,
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws JCGLException,
      RException
  {
    material.materialRegularGetAlbedo().albedoAccept(
      new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
        @Override public Unit textured(
          final KMaterialAlbedoTextured m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureAlbedoUnchecked(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit untextured(
          final KMaterialAlbedoUntextured m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureNormal(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.materialRegularGetSpecular().specularAccept(
      new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureSpecular(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.materialRegularGetEnvironment().environmentAccept(
      new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            units.withTextureCube(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            units.withTextureCube(m.getTexture()));
          return Unit.unit();
        }
      });
  }

  static void putInstanceTexturesSpecularOnly(
    final KTextureUnitContextType units,
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws JCGLException,
      RException
  {
    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureNormal(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.getSpecular().specularAccept(
      new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureSpecular(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });
  }

  static void putMaterialRegular(
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws JCGLException,
      RException
  {
    material.materialRegularGetAlbedo().albedoAccept(
      new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
        @Override public Unit textured(
          final KMaterialAlbedoTextured m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlbedoTextured(program, m);
          return Unit.unit();
        }

        @Override public Unit untextured(
          final KMaterialAlbedoUntextured m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlbedoUntextured(program, m);
          return Unit.unit();
        }
      });

    material.materialRegularGetEnvironment().environmentAccept(
      new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEnvironmentReflection(program, m);
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEnvironmentReflectionMapped(
            program,
            m);
          return Unit.unit();
        }
      });

    material.materialRegularGetSpecular().specularAccept(
      new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialSpecularConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialSpecularMapped(program, m);
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });
  }

  static void putMaterialTranslucentRegular(
    final JCBProgramType program,
    final KMaterialTranslucentRegular material)
    throws JCGLException,
      RException
  {
    KRendererCommon.putMaterialRegular(program, material);

    material.materialGetAlpha().alphaAccept(
      new KMaterialAlphaVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialAlphaConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit oneMinusDot(
          final KMaterialAlphaOneMinusDot m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaOneMinusDot(program, m);
          return Unit.unit();
        }
      });
  }

  static void putMaterialTranslucentSpecularOnly(
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws JCGLException,
      RException
  {
    material.getSpecular().specularAccept(
      new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialSpecularConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialSpecularMapped(program, m);
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    material.getAlpha().alphaAccept(
      new KMaterialAlphaVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialAlphaConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit oneMinusDot(
          final KMaterialAlphaOneMinusDot m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaOneMinusDot(program, m);
          return Unit.unit();
        }
      });
  }

  static void putShadow(
    final KShadowMapContextType shadow_context,
    final KTextureUnitContextType unit_context,
    final JCBProgramType program,
    final KLightWithShadowType light)
    throws JCGLException,
      RException
  {
    light
      .withShadowAccept(new KLightWithShadowVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithShadowBasic(
          final KLightProjectiveWithShadowBasic lp)
          throws RException,
            JCGLException
        {
          final KShadowMapBasic map =
            (KShadowMapBasic) shadow_context.getShadowMap(light);

          final TextureUnitType unit =
            unit_context.withTexture2D(map
              .getFramebuffer()
              .kFramebufferGetDepthTexture());

          KShadingProgramCommon.putShadowBasic(program, lp.lightGetShadow());
          KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVariance(
          final KLightProjectiveWithShadowVariance lp)
          throws RException,
            JCGLException
        {
          final KShadowMapVariance map =
            (KShadowMapVariance) shadow_context.getShadowMap(light);
          final TextureUnitType unit =
            unit_context.withTexture2D(map
              .getFramebuffer()
              .kFramebufferGetDepthVarianceTexture());

          KShadingProgramCommon.putShadowVariance(
            program,
            lp.lightGetShadow());
          KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
          return Unit.unit();
        }
      });
  }

  static void putShadow(
    final KTextureUnitContextType texture_unit_ctx,
    final JCBProgramType program,
    final KShadowType shadow,
    final KShadowMapUsableType map)
    throws JCGLException,
      RException
  {
    map.kShadowMapAccept(new KShadowMapVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMapVisitBasic(
        final KShadowMapBasic sm)
        throws JCGLException,
          RException
      {
        assert shadow instanceof KShadowMappedBasic;
        KRendererCommon.putShadowBasic(
          texture_unit_ctx,
          program,
          (KShadowMappedBasic) shadow,
          sm);
        return Unit.unit();
      }

      @Override public Unit shadowMapVisitVariance(
        final KShadowMapVariance sm)
        throws JCGLException,
          RException
      {
        assert shadow instanceof KShadowMappedVariance;
        KRendererCommon.putShadowVariance(
          texture_unit_ctx,
          program,
          (KShadowMappedVariance) shadow,
          sm);
        return Unit.unit();
      }
    });
  }

  static void putShadowBasic(
    final KTextureUnitContextType texture_unit_ctx,
    final JCBProgramType program,
    final KShadowMappedBasic shadow,
    final KShadowMapBasic map)
    throws JCGLException,
      RException
  {
    final KFramebufferDepth fb = map.getFramebuffer();
    final TextureUnitType unit =
      texture_unit_ctx.withTexture2D(fb.kFramebufferGetDepthTexture());
    KShadingProgramCommon.putShadowBasic(program, shadow);
    KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
  }

  static void putShadowReuse(
    final JCBProgramType program,
    final KLightWithShadowType light)
    throws JCGLException,
      RException
  {
    light
      .withShadowAccept(new KLightWithShadowVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithShadowBasic(
          final KLightProjectiveWithShadowBasic lp)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowBasicReuse(program);
          KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVariance(
          final KLightProjectiveWithShadowVariance lp)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putShadowVarianceReuse(program);
          KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
          return Unit.unit();
        }
      });
  }

  static void putShadowVariance(
    final KTextureUnitContextType texture_unit_ctx,
    final JCBProgramType program,
    final KShadowMappedVariance shadow,
    final KShadowMapVariance map)
    throws JCGLException,
      RException
  {
    final KFramebufferDepthVariance fb = map.getFramebuffer();
    final TextureUnitType unit =
      texture_unit_ctx
        .withTexture2D(fb.kFramebufferGetDepthVarianceTexture());
    KShadingProgramCommon.putShadowVariance(program, shadow);
    KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
  }

  /**
   * Configure culling such that <code>faces</code> will be rendered.
   */

  static void renderConfigureFaceCulling(
    final JCGLInterfaceCommonType gc,
    final KFaceSelection faces)
    throws JCGLException
  {
    switch (faces) {
      case FACE_RENDER_BACK:
      {
        gc.cullingEnable(
          FaceSelection.FACE_FRONT,
          FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
        break;
      }
      case FACE_RENDER_FRONT:
      {
        gc.cullingEnable(
          FaceSelection.FACE_BACK,
          FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
        break;
      }
      case FACE_RENDER_FRONT_AND_BACK:
      {
        gc.cullingDisable();
        break;
      }
      case FACE_RENDER_NONE:
      {
        gc.cullingEnable(
          FaceSelection.FACE_FRONT_AND_BACK,
          FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
        break;
      }
    }
  }

  private KRendererCommon()
  {
    throw new UnreachableCodeException();
  }
}
