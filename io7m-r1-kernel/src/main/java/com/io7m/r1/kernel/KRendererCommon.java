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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable4x4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasic;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoft;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowBasicSSSoftDiffuseOnly;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVariance;
import com.io7m.r1.kernel.types.KLightProjectiveWithShadowVarianceDiffuseOnly;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowType;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowVisitorType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KLightWithShadowVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoType;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlphaConstant;
import com.io7m.r1.kernel.types.KMaterialAlphaOneMinusDot;
import com.io7m.r1.kernel.types.KMaterialAlphaType;
import com.io7m.r1.kernel.types.KMaterialAlphaVisitorType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentType;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalType;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialRegularType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialSpecularMapped;
import com.io7m.r1.kernel.types.KMaterialSpecularNone;
import com.io7m.r1.kernel.types.KMaterialSpecularType;
import com.io7m.r1.kernel.types.KMaterialSpecularVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KProjectionType;

/**
 * Functions common to all renderers.
 */

@EqualityReference final class KRendererCommon
{
  static float depthCoefficient(
    final KProjectionType projection)
  {
    final float far = projection.projectionGetZFar();
    return 2.0f / KRendererCommon.log2(far + 1.0f);
  }

  private static float log2(
    final float x)
  {
    return (float) (Math.log(x) / Math.log(2.0));
  }

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
    throws RException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
    KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());

    material.materialRegularGetEnvironment().environmentAccept(
      new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException
        {
          KShadingProgramCommon.putMatrixInverseView(
            program,
            mwi.getMatrixViewInverse());
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException
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
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
    KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
  }

  static void putInstanceTexturesAlbedo(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialAlbedoType material)
    throws RException
  {
    material
      .albedoAccept(new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
        @Override public Unit textured(
          final KMaterialAlbedoTextured m)
          throws RException
        {
          KShadingProgramCommon.putTextureAlbedoUnchecked(
            program,
            texture_unit_context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit untextured(
          final KMaterialAlbedoUntextured m)
          throws RException
        {
          return Unit.unit();
        }
      });
  }

  static void putInstanceTexturesEnvironment(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialEnvironmentType material)
    throws RException
  {
    material
      .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            texture_unit_context.withTextureCube(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException
        {
          KShadingProgramCommon.putTextureEnvironment(
            program,
            texture_unit_context.withTextureCube(m.getTexture()));
          return Unit.unit();
        }
      });
  }

  static void putInstanceTexturesNormal(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialNormalType material)
    throws RException
  {
    material
      .normalAccept(new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException
        {
          KShadingProgramCommon.putTextureNormal(
            program,
            texture_unit_context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException
        {
          return Unit.unit();
        }
      });
  }

  static void putInstanceTexturesRegularLit(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws RException
  {
    KRendererCommon.putInstanceTexturesAlbedo(
      texture_unit_context,
      program,
      material.materialRegularGetAlbedo());
    KRendererCommon.putInstanceTexturesNormal(
      texture_unit_context,
      program,
      material.materialGetNormal());
    KRendererCommon.putInstanceTexturesSpecular(
      texture_unit_context,
      program,
      material.materialRegularGetSpecular());
    KRendererCommon.putInstanceTexturesEnvironment(
      texture_unit_context,
      program,
      material.materialRegularGetEnvironment());
  }

  static void putInstanceTexturesRegularUnlit(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws RException
  {
    KRendererCommon.putInstanceTexturesAlbedo(
      texture_unit_context,
      program,
      material.materialRegularGetAlbedo());
    KRendererCommon.putInstanceTexturesNormal(
      texture_unit_context,
      program,
      material.materialGetNormal());

    final KMaterialEnvironmentType env =
      material.materialRegularGetEnvironment();
    KRendererCommon.putInstanceTexturesEnvironment(
      texture_unit_context,
      program,
      env);

    if (env instanceof KMaterialEnvironmentReflectionMapped) {
      KRendererCommon.putInstanceTexturesSpecular(
        texture_unit_context,
        program,
        material.materialRegularGetSpecular());
    }
  }

  static void putInstanceTexturesSpecular(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialSpecularType material)
    throws RException
  {
    material
      .specularAccept(new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException
        {
          KShadingProgramCommon.putTextureSpecular(
            program,
            texture_unit_context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException
        {
          return Unit.unit();
        }
      });
  }

  static void putInstanceTexturesSpecularOnly(
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws RException
  {
    KRendererCommon.putInstanceTexturesNormal(
      texture_unit_context,
      program,
      material.materialGetNormal());
    KRendererCommon.putInstanceTexturesSpecular(
      texture_unit_context,
      program,
      material.getSpecular());
  }

  static void putMaterialAlbedo(
    final JCBProgramType program,
    final KMaterialAlbedoType material)
    throws RException
  {
    material
      .albedoAccept(new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
        @Override public Unit textured(
          final KMaterialAlbedoTextured m)
          throws RException
        {
          KShadingProgramCommon.putMaterialAlbedoTextured(program, m);
          return Unit.unit();
        }

        @Override public Unit untextured(
          final KMaterialAlbedoUntextured m)
          throws RException
        {
          KShadingProgramCommon.putMaterialAlbedoUntextured(program, m);
          return Unit.unit();
        }
      });
  }

  static void putMaterialEnvironment(
    final JCBProgramType program,
    final KMaterialEnvironmentType material)
    throws RException
  {
    material
      .environmentAccept(new KMaterialEnvironmentVisitorType<Unit, JCGLException>() {
        @Override public Unit none(
          final KMaterialEnvironmentNone m)
          throws RException
        {
          return Unit.unit();
        }

        @Override public Unit reflection(
          final KMaterialEnvironmentReflection m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEnvironmentReflection(program, m);
          return Unit.unit();
        }

        @Override public Unit reflectionMapped(
          final KMaterialEnvironmentReflectionMapped m)
          throws RException
        {
          KShadingProgramCommon.putMaterialEnvironmentReflectionMapped(
            program,
            m);
          return Unit.unit();
        }
      });
  }

  static void putMaterialRegularLit(
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws RException
  {
    KRendererCommon.putMaterialAlbedo(
      program,
      material.materialRegularGetAlbedo());
    KRendererCommon.putMaterialEnvironment(
      program,
      material.materialRegularGetEnvironment());
    KRendererCommon.putMaterialSpecular(
      program,
      material.materialRegularGetSpecular());
  }

  static void putMaterialRegularUnlit(
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws RException
  {
    KRendererCommon.putMaterialAlbedo(
      program,
      material.materialRegularGetAlbedo());

    final KMaterialEnvironmentType env =
      material.materialRegularGetEnvironment();
    KRendererCommon.putMaterialEnvironment(program, env);
    if (env instanceof KMaterialEnvironmentReflectionMapped) {
      KRendererCommon.putMaterialSpecular(
        program,
        material.materialRegularGetSpecular());
    }
  }

  static void putMaterialSpecular(
    final JCBProgramType program,
    final KMaterialSpecularType material)
    throws RException
  {
    material
      .specularAccept(new KMaterialSpecularVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialSpecularConstant m)
          throws RException
        {
          KShadingProgramCommon.putMaterialSpecularConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit mapped(
          final KMaterialSpecularMapped m)
          throws RException
        {
          KShadingProgramCommon.putMaterialSpecularMapped(program, m);
          return Unit.unit();
        }

        @Override public Unit none(
          final KMaterialSpecularNone m)
          throws RException
        {
          return Unit.unit();
        }
      });
  }

  static void putMaterialTranslucentAlpha(
    final JCBProgramType program,
    final KMaterialAlphaType material)
    throws RException
  {
    material
      .alphaAccept(new KMaterialAlphaVisitorType<Unit, JCGLException>() {
        @Override public Unit constant(
          final KMaterialAlphaConstant m)
          throws RException
        {
          KShadingProgramCommon.putMaterialAlphaConstant(program, m);
          return Unit.unit();
        }

        @Override public Unit oneMinusDot(
          final KMaterialAlphaOneMinusDot m)
          throws RException
        {
          KShadingProgramCommon.putMaterialAlphaOneMinusDot(program, m);
          return Unit.unit();
        }
      });
  }

  static void putMaterialTranslucentRegularLit(
    final JCBProgramType program,
    final KMaterialTranslucentRegular material)
    throws RException
  {
    KRendererCommon.putMaterialRegularLit(program, material);
    KRendererCommon.putMaterialTranslucentAlpha(
      program,
      material.materialGetAlpha());
  }

  static void putMaterialTranslucentRegularUnlit(
    final JCBProgramType program,
    final KMaterialTranslucentRegular material)
    throws RException
  {
    KRendererCommon.putMaterialRegularUnlit(program, material);
    KRendererCommon.putMaterialTranslucentAlpha(
      program,
      material.materialGetAlpha());
  }

  static void putMaterialTranslucentSpecularOnly(
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws RException
  {
    KRendererCommon.putMaterialSpecular(program, material.getSpecular());
    KRendererCommon.putMaterialTranslucentAlpha(program, material.getAlpha());
  }

  static void putShadowScreenSpacePrePass(
    final KShadowMapContextType shadow_context,
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KLightWithScreenSpaceShadowType light)
    throws RException
  {
    light
      .withScreenSpaceShadowAccept(new KLightWithScreenSpaceShadowVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithShadowBasicSSSoft(
          final KLightProjectiveWithShadowBasicSSSoft lp)
          throws RException
        {
          final KShadowMapBasicSSSoft map =
            (KShadowMapBasicSSSoft) shadow_context.getShadowMap(lp);
          final KFramebufferDepthType framebuffer = map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer.getDepthTexture());

          KShadingProgramCommon.putShadowBasicSSSoftDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowBasicSSSoft(
            program,
            lp.lightGetShadowBasicSSSoft());
          KShadingProgramCommon.putTextureShadowMapBasicSSSoft(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoftDiffuseOnly(
          final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lp)
          throws RException
        {
          final KShadowMapBasicSSSoft map =
            (KShadowMapBasicSSSoft) shadow_context.getShadowMap(lp);
          final KFramebufferDepthType framebuffer = map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer.getDepthTexture());

          KShadingProgramCommon.putShadowBasicSSSoftDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowBasicSSSoft(
            program,
            lp.lightGetShadowBasicSSSoft());
          KShadingProgramCommon.putTextureShadowMapBasicSSSoft(program, unit);
          return Unit.unit();
        }
      });
  }

  static void putShadow(
    final KShadowMapContextType shadow_context,
    final KTextureBindingsContextType texture_unit_context,
    final JCBProgramType program,
    final KLightWithShadowType light)
    throws RException
  {
    light
      .withShadowAccept(new KLightWithShadowVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithShadowBasic(
          final KLightProjectiveWithShadowBasic lp)
          throws RException
        {
          final KShadowMapBasic map =
            (KShadowMapBasic) shadow_context.getShadowMap(lp);
          final KFramebufferDepthType framebuffer = map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer.getDepthTexture());

          KShadingProgramCommon.putShadowBasicDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowBasic(
            program,
            lp.lightGetShadowBasic());
          KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicDiffuseOnly(
          final KLightProjectiveWithShadowBasicDiffuseOnly lp)
          throws RException
        {
          final KShadowMapBasic map =
            (KShadowMapBasic) shadow_context.getShadowMap(lp);
          final KFramebufferDepthType framebuffer = map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer.getDepthTexture());

          KShadingProgramCommon.putShadowBasicDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowBasic(
            program,
            lp.lightGetShadowBasic());
          KShadingProgramCommon.putTextureShadowMapBasic(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVariance(
          final KLightProjectiveWithShadowVariance lp)
          throws RException
        {
          final KShadowMapVariance map =
            (KShadowMapVariance) shadow_context.getShadowMap(lp);
          final KFramebufferDepthVarianceType framebuffer =
            map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer
              .getDepthVarianceTexture());

          KShadingProgramCommon.putShadowVarianceDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowVariance(
            program,
            lp.lightGetShadowVariance());
          KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVarianceDiffuseOnly(
          final KLightProjectiveWithShadowVarianceDiffuseOnly lp)
          throws RException
        {
          final KShadowMapVariance map =
            (KShadowMapVariance) shadow_context.getShadowMap(lp);
          final KFramebufferDepthVarianceType framebuffer =
            map.getFramebuffer();
          final TextureUnitType unit =
            texture_unit_context.withTexture2D(framebuffer
              .getDepthVarianceTexture());

          KShadingProgramCommon.putShadowVarianceDepthCoefficient(
            program,
            KRendererCommon.depthCoefficient(lp
              .lightProjectiveGetProjection()));
          KShadingProgramCommon.putShadowVariance(
            program,
            lp.lightGetShadowVariance());
          KShadingProgramCommon.putTextureShadowMapVariance(program, unit);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoft(
          final KLightProjectiveWithShadowBasicSSSoft lp)
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoftDiffuseOnly(
          final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lp)
        {
          return Unit.unit();
        }
      });
  }

  static void putShadowReuse(
    final JCBProgramType program,
    final KLightWithShadowType light)
    throws RException
  {
    light
      .withShadowAccept(new KLightWithShadowVisitorType<Unit, JCGLException>() {
        @Override public Unit projectiveWithShadowBasic(
          final KLightProjectiveWithShadowBasic _)
          throws RException
        {
          KShadingProgramCommon.putShadowBasicReuse(program);
          KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicDiffuseOnly(
          final KLightProjectiveWithShadowBasicDiffuseOnly _)
          throws RException
        {
          KShadingProgramCommon.putShadowBasicReuse(program);
          KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVariance(
          final KLightProjectiveWithShadowVariance _)
          throws RException
        {
          KShadingProgramCommon.putShadowVarianceReuse(program);
          KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowVarianceDiffuseOnly(
          final KLightProjectiveWithShadowVarianceDiffuseOnly _)
          throws RException
        {
          KShadingProgramCommon.putShadowVarianceReuse(program);
          KShadingProgramCommon.putTextureShadowMapVarianceReuse(program);
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoft(
          final KLightProjectiveWithShadowBasicSSSoft lp)
        {
          return Unit.unit();
        }

        @Override public Unit projectiveWithShadowBasicSSSoftDiffuseOnly(
          final KLightProjectiveWithShadowBasicSSSoftDiffuseOnly lp)
        {
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

  static void putFramebufferScreenSize(
    final KFramebufferUsableType framebuffer,
    final JCBProgramType program)
  {
    final AreaInclusive area = framebuffer.getArea();
    final RangeInclusiveL range_x = area.getRangeX();
    final RangeInclusiveL range_y = area.getRangeY();

    KShadingProgramCommon.putViewport(
      program,
      1.0f / range_x.getInterval(),
      1.0f / range_y.getInterval());
  }
}
