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
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3ES3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
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
import com.io7m.r1.kernel.types.KMaterialEnvironmentNone;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.r1.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.r1.kernel.types.KMaterialEnvironmentVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialRegularType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KProjectionType;

/**
 * Functions common to all renderers.
 */

@EqualityReference final class KRendererCommon
{
  @EqualityReference static final class GetGL3ES3 implements
    JCGLImplementationVisitorType<JCGLInterfaceGL3ES3Type, UnreachableCodeException>
  {
    GetGL3ES3()
    {
      // Nothing
    }

    @Override public JCGLInterfaceGL3ES3Type implementationIsGL2(
      final JCGLInterfaceGL2Type gl)
    {
      throw new UnreachableCodeException();
    }

    @Override public JCGLInterfaceGL3ES3Type implementationIsGL3(
      final JCGLInterfaceGL3Type gl)
    {
      return gl;
    }

    @Override public JCGLInterfaceGL3ES3Type implementationIsGLES2(
      final JCGLInterfaceGLES2Type gl)
    {
      throw new UnreachableCodeException();
    }

    @Override public JCGLInterfaceGL3ES3Type implementationIsGLES3(
      final JCGLInterfaceGLES3Type gl)
    {
      return gl;
    }
  }

  private static final GetGL3ES3 GET_GL3ES3;

  static {
    GET_GL3ES3 = new GetGL3ES3();
  }

  static float depthCoefficient(
    final KProjectionType projection)
  {
    final float far = projection.projectionGetZFar();
    return 2.0f / KRendererCommon.log2(far + 1.0f);
  }

  static GetGL3ES3 getGL3ES3Get()
  {
    return KRendererCommon.GET_GL3ES3;
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

  static void putInstanceMatricesRegular(
    final JCBProgramType program,
    final KMatricesInstanceValuesType mwi,
    final KMaterialRegularType material)
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());
    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
    KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());

    try {
      material.getEnvironment().environmentAccept(
        new KMaterialEnvironmentVisitorType<Unit, RException>() {
          @Override public Unit none(
            final KMaterialEnvironmentNone m)
          {
            return Unit.unit();
          }

          @Override public Unit reflection(
            final KMaterialEnvironmentReflection m)
          {
            KShadingProgramCommon.putMatrixInverseView(
              program,
              mwi.getMatrixViewInverse());
            return Unit.unit();
          }

          @Override public Unit reflectionMapped(
            final KMaterialEnvironmentReflectionMapped m)
          {
            KShadingProgramCommon.putMatrixInverseView(
              program,
              mwi.getMatrixViewInverse());
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
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

  static void putMaterialOpaqueRegular(
    final JCBProgramType program,
    final KTextureBindingsContextType units,
    final KMaterialOpaqueRegular material)
    throws RException
  {
    KShadingProgramCommon.putMaterialAlbedoTypeWithTextures(
      program,
      units,
      material);
    KShadingProgramCommon.putMaterialDepthType(program, material.getDepth());
    KShadingProgramCommon.putMaterialEmissiveTypeWithTextures(
      program,
      units,
      material);
    KShadingProgramCommon.putMaterialEnvironmentTypeWithTextures(
      program,
      units,
      material.getEnvironment());
    KShadingProgramCommon.putTextureNormal(
      program,
      units.withTexture2D(material.getNormalTexture()));
    KShadingProgramCommon.putMaterialSpecularTypeWithTextures(
      program,
      units,
      material);
  }

  static void putMaterialTranslucentRegularUnlitWithTextures(
    final JCBProgramType program,
    final KMaterialTranslucentRegular material)
    throws RException
  {
    KShadingProgramCommon.putMaterialAlphaType(program, material.getAlpha());
    KShadingProgramCommon.putMaterialAlbedoTypeWithoutTextures(
      program,
      material);
    KShadingProgramCommon.putMaterialEnvironmentTypeWithoutTextures(
      program,
      material.getEnvironment());
    KShadingProgramCommon.putMaterialSpecularTypeWithoutTextures(
      program,
      material);
  }

  static void putMaterialTranslucentRegularWithoutTextures(
    final JCBProgramType program,
    final KMaterialTranslucentRegular material)
    throws RException
  {
    KShadingProgramCommon.putMaterialAlphaType(program, material.getAlpha());
    KShadingProgramCommon.putMaterialAlbedoTypeWithoutTextures(
      program,
      material);
    KShadingProgramCommon.putMaterialEnvironmentTypeWithoutTextures(
      program,
      material.getEnvironment());
    KShadingProgramCommon.putMaterialSpecularTypeWithoutTextures(
      program,
      material);
  }

  static void putMaterialTranslucentSpecularOnlyWithoutTextures(
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws RException
  {
    KShadingProgramCommon.putMaterialAlphaType(program, material.getAlpha());
    KShadingProgramCommon.putMaterialSpecularTypeWithoutTextures(
      program,
      material);
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
      });
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

  static void enableDepthClampingIfSupported(
    final JCGLImplementationType gi)
  {
    gi
      .implementationAccept(new JCGLImplementationVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
        {
          gl.depthClampingEnable();
          return Unit.unit();
        }

        @Override public Unit implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
        {
          return Unit.unit();
        }
      });
  }
}
