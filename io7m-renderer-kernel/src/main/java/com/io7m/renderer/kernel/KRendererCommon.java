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
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
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
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialLabelSpecularOnlyType;
import com.io7m.renderer.kernel.types.KMaterialRegularType;
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

  static void putInstanceMatricesSpecularOnly(
    final JCBProgramType program,
    final MatricesInstanceType mwi,
    final KMaterialLabelSpecularOnlyType label)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());

    if (label.labelImpliesUV()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
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
  }

  static void putInstanceMatricesRegular(
    final JCBProgramType program,
    final MatricesInstanceType mwi,
    final KMaterialLabelRegularType label)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());

    if (label.labelImpliesUV()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
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

  static void putInstanceTexturesSpecularOnly(
    final KTextureUnitContextType units,
    final KMaterialLabelSpecularOnlyType label,
    final JCBProgramType program,
    final KMaterialTranslucentSpecularOnly material)
    throws JCGLException,
      RException
  {
    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStaticUsableType> some =
          (Some<Texture2DStaticUsableType>) material
            .materialGetNormal()
            .getTexture();
        KShadingProgramCommon.putTextureNormal(
          program,
          units.withTexture2D(some.get()));
        break;
      }
      case NORMAL_NONE:
      case NORMAL_VERTEX:
      {
        break;
      }
    }

    if (label.labelImpliesSpecularMap()) {
      final Some<Texture2DStaticUsableType> some =
        (Some<Texture2DStaticUsableType>) material.getSpecular().getTexture();
      KShadingProgramCommon.putTextureSpecular(
        program,
        units.withTexture2D(some.get()));
    }
  }

  static void putInstanceTexturesRegular(
    final KTextureUnitContextType units,
    final KMaterialLabelRegularType label,
    final JCBProgramType program,
    final KMaterialRegularType material)
    throws JCGLException,
      RException
  {
    switch (label.labelGetAlbedo()) {
      case ALBEDO_COLOURED:
      {
        break;
      }
      case ALBEDO_TEXTURED:
      {
        final Some<Texture2DStaticUsableType> some =
          (Some<Texture2DStaticUsableType>) material
            .materialGetAlbedo()
            .getTexture();
        KShadingProgramCommon.putTextureAlbedoUnchecked(
          program,
          units.withTexture2D(some.get()));
        break;
      }
    }

    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStaticUsableType> some =
          (Some<Texture2DStaticUsableType>) material
            .materialGetNormal()
            .getTexture();
        KShadingProgramCommon.putTextureNormal(
          program,
          units.withTexture2D(some.get()));
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
        final Some<Texture2DStaticUsableType> some =
          (Some<Texture2DStaticUsableType>) material
            .materialGetEmissive()
            .getTexture();
        KShadingProgramCommon.putTextureEmissive(
          program,
          units.withTexture2D(some.get()));
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
      {
        break;
      }
    }

    if (label.labelImpliesSpecularMap()) {
      final Some<Texture2DStaticUsableType> some =
        (Some<Texture2DStaticUsableType>) material
          .materialGetSpecular()
          .getTexture();
      KShadingProgramCommon.putTextureSpecular(
        program,
        units.withTexture2D(some.get()));
    }

    switch (label.labelGetEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      {
        final Some<TextureCubeStaticUsableType> some =
          (Some<TextureCubeStaticUsableType>) material
            .materialGetEnvironment()
            .getTexture();
        KShadingProgramCommon.putTextureEnvironment(
          program,
          units.withTextureCube(some.get()));
        break;
      }
    }
  }

  static void putMaterialRegular(
    final JCBProgramType program,
    final KMaterialLabelRegularType label,
    final KMaterialRegularType material)
    throws JCGLException
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

  static void putMaterialTranslucentSpecularOnly(
    final JCBProgramType program,
    final KMaterialLabelSpecularOnlyType label,
    final KMaterialTranslucentSpecularOnly material)
    throws JCGLException
  {
    switch (label.labelGetSpecular()) {
      case SPECULAR_CONSTANT:
      case SPECULAR_MAPPED:
      {
        KShadingProgramCommon.putMaterialSpecular(
          program,
          material.getSpecular());
        break;
      }
      case SPECULAR_NONE:
      {
        break;
      }
    }

    KShadingProgramCommon.putMaterialAlphaOpacity(program, material
      .getAlpha()
      .getOpacity());
  }

  static void putMaterialTranslucentRegular(
    final JCBProgramType program,
    final KMaterialLabelRegularType label,
    final KMaterialTranslucentRegular material)
    throws JCGLException
  {
    KRendererCommon.putMaterialRegular(program, label, material);
    KShadingProgramCommon.putMaterialAlphaOpacity(program, material
      .materialGetAlpha()
      .getOpacity());
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
