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

package com.io7m.r1.tests.kernel;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageFilterDeferredType;
import com.io7m.r1.kernel.KImageFilterRGBAType;
import com.io7m.r1.kernel.KMaterialDefaults;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescriptionBuilderType;
import com.io7m.r1.kernel.types.KGlowParameters;
import com.io7m.r1.kernel.types.KGlowParametersBuilderType;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KLightPropertiesType;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMaterialDefaultsType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.shaders.deferred.RKDLightCases;
import com.io7m.r1.shaders.deferred.RKDMaterialCases;
import com.io7m.r1.shaders.forward.RKFLightCases;
import com.io7m.r1.shaders.forward.RKFMaterialCases;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.TestShaderCaches;

@SuppressWarnings("static-method") public final class KRendererTest
{
  @Test public void testRenderer()
    throws Exception
  {
    final LogUsableType log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType gi =
      RFakeGL.newFakeGL30WithLog(log, RFakeShaderControllers.newNull(), none);

    final R1BuilderType r1b = R1.newBuilder(gi, log);
    r1b.setShaderCacheSet(TestShaderCaches.newCachesFromArchives(gi, log));
    final R1Type r1 = r1b.build();
    final KRendererDeferredType rd = r1.getRendererDeferred();

    final KFramebufferDeferredDescriptionBuilderType fbb =
      KFramebufferDeferredDescription.newBuilder(RFakeGL.SCREEN_AREA);
    final KFramebufferDeferredDescription fb_desc = fbb.build();
    final KFramebufferDeferredType fb =
      KFramebufferDeferred.newFramebuffer(gi, fb_desc);

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();
    final KProjectionType projection =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        100.0f);

    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType transform = KTransformMatrix4x4.newTransform(model);
    final KMeshReadableType mesh =
      KRendererDeferredOpaqueTest.makeMesh(gi.getGLCommon());
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
      PMatrixI3x3F.identity();

    final KCamera camera = KCamera.newCamera(view, projection);
    final KVisibleSetBuilderWithCreateType tb =
      KVisibleSet.newBuilder(camera);

    final Texture2DStaticType t2d =
      RFakeTextures2DStatic.newWithName(gi, "t2d");
    final TextureCubeStaticType tcube =
      RFakeTexturesCubeStatic.newAnything(gi);

    {
      final RKDMaterialCases material_cases =
        new RKDMaterialCases(t2d, tcube);
      final RKDLightCases light_cases = new RKDLightCases(t2d, tcube);

      final KVisibleSetLightGroupBuilderType lg =
        tb.visibleOpaqueNewLightGroup("g0");

      for (final KLightType l : light_cases.getCases()) {
        assert l != null;
        lg.groupAddLight(l);
      }

      for (final KMaterialOpaqueRegular c : material_cases
        .getCasesGeometryOpaqueRegular()) {
        assert c != null;

        final KInstanceOpaqueRegular o =
          KInstanceOpaqueRegular.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);

        lg.groupAddInstance(o);

        for (final KLightPropertiesType l : light_cases.getCases()) {
          if (l instanceof KLightWithShadowType) {
            tb.visibleShadowsAddCaster((KLightWithShadowType) l, o);
          }
        }
      }

      for (final KMaterialOpaqueRegular c : material_cases
        .getCasesGeometryOpaqueRegular()) {
        assert c != null;

        final KInstanceOpaqueRegular o =
          KInstanceOpaqueRegular.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);
        tb.visibleOpaqueAddUnlit(o);
      }
    }

    {
      final RKFMaterialCases material_cases =
        new RKFMaterialCases(t2d, tcube);
      final RKFLightCases light_cases = new RKFLightCases();

      for (final KMaterialTranslucentRefractive c : material_cases
        .getCasesUnlitTranslucentRefractive()) {
        assert c != null;

        final KInstanceTranslucentRefractive instance =
          KInstanceTranslucentRefractive.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);
        tb.visibleTranslucentsAddUnlit(instance);
      }

      for (final KMaterialTranslucentRegular c : material_cases
        .getCasesUnlitTranslucentRegular()) {
        assert c != null;

        final KInstanceTranslucentRegular instance =
          KInstanceTranslucentRegular.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);
        tb.visibleTranslucentsAddUnlit(instance);
      }

      for (final KMaterialTranslucentRegular c : material_cases
        .getCasesLitTranslucentRegular()) {
        assert c != null;

        final KInstanceTranslucentRegular instance =
          KInstanceTranslucentRegular.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);
        tb.visibleTranslucentsAddUnlit(instance);
      }

      for (final KMaterialTranslucentRegular c : material_cases
        .getCasesLitTranslucentRegular()) {
        assert c != null;

        final KInstanceTranslucentRegular instance =
          KInstanceTranslucentRegular.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);

        final Set<KLightTranslucentType> ls =
          new HashSet<KLightTranslucentType>(light_cases.getCases());

        tb.visibleTranslucentsAddLit(instance, ls);
      }

      for (final KMaterialTranslucentSpecularOnly c : material_cases
        .getCasesLitTranslucentSpecularOnly()) {
        assert c != null;

        final KInstanceTranslucentSpecularOnly instance =
          KInstanceTranslucentSpecularOnly.newInstance(
            mesh,
            c,
            transform,
            m_uv,
            KFaceSelection.FACE_RENDER_FRONT);

        final Set<KLightTranslucentType> ls =
          new HashSet<KLightTranslucentType>(light_cases.getCases());

        tb.visibleTranslucentsAddLit(instance, ls);
      }
    }

    final KImageFilterDeferredType<KGlowParameters> glow =
      r1.getFilterEmissionGlow();
    final KImageFilterRGBAType<KFXAAParameters> fxaa = r1.getFilterFXAA();

    final KVisibleSet visible = tb.visibleCreate();
    rd.rendererDeferredEvaluateFull(fb, visible);
    {
      final KGlowParameters glow_config = KGlowParameters.getDefault();
      glow.filterEvaluateDeferred(glow_config, fb, fb);
    }
    final KFXAAParameters fxaa_config = KFXAAParameters.getDefault();
    fxaa.filterEvaluateRGBA(fxaa_config, fb, fb);

    rd.rendererDeferredEvaluateFull(fb, visible);
    {
      final KGlowParametersBuilderType gbb = KGlowParameters.newBuilder();
      gbb.setScale(0.5f);
      gbb.setPasses(3);
      gbb.setFactor(2.0f);
      glow.filterEvaluateDeferred(gbb.build(), fb, fb);
    }
    fxaa.filterEvaluateRGBA(fxaa_config, fb, fb);
  }

  @Test public void testShadowMapBorrows()
    throws Exception
  {
    final LogUsableType log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType gi =
      RFakeGL.newFakeGL30WithLog(log, RFakeShaderControllers.newNull(), none);
    final KMaterialDefaultsType defaults = KMaterialDefaults.newResources(gi);

    final R1BuilderType r1b = R1.newBuilder(gi, log);
    r1b.setShaderCacheSet(TestShaderCaches.newCachesFromArchives(gi, log));
    final R1Type r1 = r1b.build();
    final KRendererDeferredType rd = r1.getRendererDeferred();

    final KFramebufferDeferredDescriptionBuilderType fbb =
      KFramebufferDeferredDescription.newBuilder(RFakeGL.SCREEN_AREA);
    final KFramebufferDeferredDescription fb_desc = fbb.build();
    final KFramebufferDeferredType fb =
      KFramebufferDeferred.newFramebuffer(gi, fb_desc);

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();
    final KProjectionType projection =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        100.0f);

    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType transform = KTransformMatrix4x4.newTransform(model);
    final KMeshReadableType mesh =
      KRendererDeferredOpaqueTest.makeMesh(gi.getGLCommon());
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
      PMatrixI3x3F.identity();

    final KCamera camera = KCamera.newCamera(view, projection);
    final KVisibleSetBuilderWithCreateType tb =
      KVisibleSet.newBuilder(camera);

    final Texture2DStaticType t2d =
      RFakeTextures2DStatic.newWithName(gi, "t2d");
    final TextureCubeStaticType tcube =
      RFakeTexturesCubeStatic.newAnything(gi);

    {
      final RKDLightCases light_cases = new RKDLightCases(t2d, tcube);

      final KVisibleSetLightGroupBuilderType lg =
        tb.visibleOpaqueNewLightGroup("g0");

      for (final KLightType l : light_cases.getCases()) {
        assert l != null;
        lg.groupAddLight(l);
      }

      final KMaterialOpaqueRegular mat =
        KMaterialOpaqueRegular.newBuilder(defaults).build();
      final KInstanceOpaqueRegular o =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          mat,
          transform,
          m_uv,
          KFaceSelection.FACE_RENDER_FRONT);

      lg.groupAddInstance(o);
    }

    rd.rendererDeferredEvaluateFull(fb, tb.visibleCreate());
  }
}
