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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStaticUsable;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixReadable4x4F;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapVariance;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KMaterialLabelRegularType;
import com.io7m.renderer.kernel.types.KMaterialRegularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;

/**
 * Functions common to all renderers.
 */

final class KRendererCommon
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
    final @Nonnull MatrixReadable4x4F m,
    final @Nonnull MatrixM3x3F mr)
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

  static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialLabelRegularType label)
    throws JCGLException,
      ConstraintError
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

  static void putInstanceTextures(
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
        final Some<Texture2DStaticUsable> some =
          (Some<Texture2DStaticUsable>) material
            .materialGetAlbedo()
            .getTexture();
        KShadingProgramCommon.putTextureAlbedoUnchecked(
          program,
          units.withTexture2D(some.value));
        break;
      }
    }

    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStaticUsable> some =
          (Some<Texture2DStaticUsable>) material
            .materialGetNormal()
            .getTexture();
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
        final Some<Texture2DStaticUsable> some =
          (Some<Texture2DStaticUsable>) material
            .materialGetEmissive()
            .getTexture();
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
      final Some<Texture2DStaticUsable> some =
        (Some<Texture2DStaticUsable>) material
          .materialGetSpecular()
          .getTexture();
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
        final Some<TextureCubeStaticUsable> some =
          (Some<TextureCubeStaticUsable>) material
            .materialGetEnvironment()
            .getTexture();
        KShadingProgramCommon.putTextureEnvironment(
          program,
          units.withTextureCube(some.value));
        break;
      }
    }
  }

  static void putMaterialRegular(
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

  static void putMaterialTranslucentRegular(
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialLabelRegularType label,
    final @Nonnull KMaterialTranslucentRegular material)
    throws JCGLException,
      ConstraintError
  {
    KRendererCommon.putMaterialRegular(program, label, material);
    KShadingProgramCommon.putMaterialAlphaOpacity(program, material
      .materialGetAlpha()
      .getOpacity());
  }

  static void putShadow(
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
      @Override public Unit shadowMappedBasic(
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

      @Override public Unit shadowMappedVariance(
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

  static void putShadowReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLightProjective light)
    throws JCGLException,
      RException,
      ConstraintError
  {
    final Some<KShadowType> some = (Some<KShadowType>) light.lightGetShadow();
    some.value.shadowAccept(new KShadowVisitorType<Unit, JCGLException>() {
      @Override public Unit shadowMappedBasic(
        final @Nonnull KShadowMappedBasic s)
        throws JCGLException,
          RException,
          ConstraintError
      {
        KShadingProgramCommon.putShadowBasicReuse(program);
        KShadingProgramCommon.putTextureShadowMapBasicReuse(program);
        return Unit.unit();
      }

      @Override public Unit shadowMappedVariance(
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
   * Configure culling such that <code>faces</code> will be rendered.
   */

  static void renderConfigureFaceCulling(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KFaceSelection faces)
    throws JCGLRuntimeException,
      ConstraintError
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
