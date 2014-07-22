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

import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable4x4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.renderer.kernel.types.KMaterialAlphaConstant;
import com.io7m.renderer.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.renderer.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveConstant;
import com.io7m.renderer.kernel.types.KMaterialEmissiveMapped;
import com.io7m.renderer.kernel.types.KMaterialEmissiveNone;
import com.io7m.renderer.kernel.types.KMaterialEmissiveVisitorType;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentNone;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialRegularType;
import com.io7m.renderer.kernel.types.KMaterialSpecularConstant;
import com.io7m.renderer.kernel.types.KMaterialSpecularMapped;
import com.io7m.renderer.kernel.types.KMaterialSpecularNone;
import com.io7m.renderer.kernel.types.KMaterialSpecularVisitorType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;

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
    final MatricesInstanceType mwi,
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
    final MatricesInstanceType mwi,
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

    material.materialRegularGetEmissive().emissiveAccept(
      new KMaterialEmissiveVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialEmissiveConstant m)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialEmissiveMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureEmissive(
            program,
            units.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialEmissiveNone m)
          throws RException
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

    material.materialRegularGetEmissive().emissiveAccept(
      new KMaterialEmissiveVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialEmissiveConstant m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEmissiveConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialEmissiveMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialEmissiveMapped(program, m);
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialEmissiveNone m)
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
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.get().shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final KShadowMappedBasic s)
        throws JCGLException,
          RException
      {
        final KShadowMapBasic map =
          (KShadowMapBasic) shadow_context.getShadowMap(s);

        final TextureUnitType unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthTexture());

        KShadingProgramCommon.putShadowBasic(program, s);
        KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final KShadowMappedVariance s)
        throws JCGLException,
          RException
      {
        final KShadowMapVariance map =
          (KShadowMapVariance) shadow_context.getShadowMap(s);
        final TextureUnitType unit =
          unit_context.withTexture2D(map
            .getFramebuffer()
            .kFramebufferGetDepthVarianceTexture());

        KShadingProgramCommon.putShadowVariance(program, s);
        KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
        return Unit.unit();
      }
    });
  }

  static void putShadowReuse(
    final JCBProgramType program,
    final KLightProjective light)
    throws JCGLException,
      RException
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.get().shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final KShadowMappedBasic s)
        throws JCGLException,
          RException
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
        final KShadowMappedVariance s)
        throws JCGLException,
          RException
      {
        KShadingProgramCommon.putShadowVarianceReuse(program);
        KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
        return Unit.unit();
      }
    });
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
