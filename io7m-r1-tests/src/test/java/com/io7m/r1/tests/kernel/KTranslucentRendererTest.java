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

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jcanephora.batchexec.JCGLExceptionExecution;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionResource;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepth;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepthCache;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepthCacheType;
import com.io7m.r1.kernel.KFramebufferRGBAWithDepthUsableType;
import com.io7m.r1.kernel.KMatricesObserverFunctionType;
import com.io7m.r1.kernel.KMatricesObserverType;
import com.io7m.r1.kernel.KMutableMatrices;
import com.io7m.r1.kernel.KRefractionRenderer;
import com.io7m.r1.kernel.KRefractionRendererType;
import com.io7m.r1.kernel.KRegionCopier;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.KTranslucentRenderer;
import com.io7m.r1.kernel.KTranslucentRendererType;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KInstanceTranslucentRegular;
import com.io7m.r1.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KMaterialSpecularConstant;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegularBuilderType;
import com.io7m.r1.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KRGBAPrecision;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KVisibleSetTranslucents;
import com.io7m.r1.kernel.types.KVisibleSetTranslucentsBuilderWithCreateType;
import com.io7m.r1.shaders.forward.RKFLightCases;
import com.io7m.r1.shaders.forward.RKFMaterialCases;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.TestShaderCaches;

@SuppressWarnings({ "null", "static-method" }) public final class KTranslucentRendererTest
{
  static @NonNull KMesh makeMesh(
    final JCGLInterfaceCommonType gc)
    throws Exception
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      gc.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        1,
        UsageHint.USAGE_STATIC_DRAW);

    final KMesh k = KMesh.newMesh(array, indices);
    return k;
  }

  private static KTranslucentRendererType makeRenderer(
    final JCGLImplementationType g)
    throws Exception
  {
    try {
      final LogUsableType in_log =
        Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");

      final KShaderCacheSetType tc =
        TestShaderCaches.newCachesFromArchives(g, in_log);

      final KRegionCopierType copier = KRegionCopier.newCopier(g, in_log);

      final BLUCacheConfig config =
        BLUCacheConfig
          .empty()
          .withMaximumBorrowsPerKey(BigInteger.TEN)
          .withMaximumCapacity(BigInteger.valueOf(640 * 480 * 4 * 128));
      final KFramebufferRGBAWithDepthCacheType rgba_cache =
        KFramebufferRGBAWithDepthCache.newCacheWithConfig(g, config, in_log);

      final KRefractionRendererType in_refraction_renderer =
        KRefractionRenderer.newRenderer(
          g,
          copier,
          tc.getShaderForwardTranslucentUnlitCache(),
          rgba_cache);

      final KTranslucentRendererType r =
        KTranslucentRenderer.newRenderer(
          g,
          tc.getShaderForwardTranslucentUnlitCache(),
          tc.getShaderForwardTranslucentLitCache(),
          in_refraction_renderer,
          in_log);
      return r;
    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }

  @Test public void testAllCases()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final KTranslucentRendererType r =
      KTranslucentRendererTest.makeRenderer(g);

    final KFramebufferRGBADescription rgba_desc =
      KFramebufferRGBADescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KRGBAPrecision.RGBA_PRECISION_8);
    final KFramebufferRGBAWithDepthUsableType framebuffer =
      KFramebufferRGBAWithDepth.newFramebuffer(g, rgba_desc);

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
      KTranslucentRendererTest.makeMesh(g.getGLCommon());
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
      PMatrixI3x3F.identity();

    final KCamera camera = KCamera.newCamera(view, projection);
    final KVisibleSetTranslucentsBuilderWithCreateType tb =
      KVisibleSetTranslucents.newBuilder(camera);

    final Texture2DStaticType t = RFakeTextures2DStatic.newWithName(g, "t2d");
    final TextureCubeStaticType tc = RFakeTexturesCubeStatic.newAnything(g);
    final RKFMaterialCases material_cases = new RKFMaterialCases(t, tc);
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

    final KVisibleSetTranslucents translucents =
      tb.visibleTranslucentsCreate();

    final KMutableMatrices m = KMutableMatrices.newMatrices();
    m.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, UnreachableCodeException>() {
        @Override public Unit run(
          final KMatricesObserverType mwo)
          throws RException
        {
          r.rendererEvaluateTranslucents(framebuffer, mwo, translucents);
          return Unit.unit();
        }
      });
  }

  @Test public void testBug_f1c93bc35b1()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final KTranslucentRendererType r =
      KTranslucentRendererTest.makeRenderer(g);

    final KFramebufferRGBADescription rgba_desc =
      KFramebufferRGBADescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KRGBAPrecision.RGBA_PRECISION_8);
    final KFramebufferRGBAWithDepthUsableType framebuffer =
      KFramebufferRGBAWithDepth.newFramebuffer(g, rgba_desc);

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.identity();
    final KProjectionType projection =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        100.0f);

    final KMaterialTranslucentRegularBuilderType mat_b =
      KMaterialTranslucentRegular.newBuilder();
    mat_b.setSpecular(KMaterialSpecularConstant.constant(
      new PVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f),
      64.0f));
    final KMaterialTranslucentRegular mat = mat_b.build();

    final PMatrixI4x4F<RSpaceObjectType, RSpaceWorldType> model =
      PMatrixI4x4F.identity();
    final KTransformType transform = KTransformMatrix4x4.newTransform(model);
    final KMeshReadableType mesh =
      KTranslucentRendererTest.makeMesh(g.getGLCommon());

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
      PMatrixI3x3F.identity();
    final KInstanceTranslucentRegular instance =
      KInstanceTranslucentRegular.newInstance(
        mesh,
        mat,
        transform,
        m_uv,
        KFaceSelection.FACE_RENDER_FRONT);

    final KCamera camera = KCamera.newCamera(view, projection);
    final KVisibleSetTranslucentsBuilderWithCreateType tb =
      KVisibleSetTranslucents.newBuilder(camera);
    tb.visibleTranslucentsAddUnlit(instance);

    final KVisibleSetTranslucents translucents =
      tb.visibleTranslucentsCreate();

    final KMutableMatrices m = KMutableMatrices.newMatrices();
    m.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, UnreachableCodeException>() {
        @Override public Unit run(
          final KMatricesObserverType mwo)
          throws RException
        {
          r.rendererEvaluateTranslucents(framebuffer, mwo, translucents);
          return Unit.unit();
        }
      });
  }

  @Test(expected = RExceptionResource.class) public void testOutOfUnits_1()
    throws Exception
  {
    try {
      final JCGLSoftRestrictionsType restrictor =
        new JCGLSoftRestrictionsType() {
          @Override public int restrictTextureUnitCount(
            final int count)
          {
            return 1;
          }

          @Override public boolean restrictExtensionVisibility(
            final String name)
          {
            return true;
          }
        };
      final OptionType<JCGLSoftRestrictionsType> restrict =
        Option.some(restrictor);

      final JCGLImplementationType g =
        RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), restrict);
      final KTranslucentRendererType r =
        KTranslucentRendererTest.makeRenderer(g);

      final KFramebufferRGBADescription rgba_desc =
        KFramebufferRGBADescription.newDescription(
          RFakeGL.SCREEN_AREA,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          KRGBAPrecision.RGBA_PRECISION_8);
      final KFramebufferRGBAWithDepthUsableType framebuffer =
        KFramebufferRGBAWithDepth.newFramebuffer(g, rgba_desc);

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
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      final KMeshReadableType mesh =
        KTranslucentRendererTest.makeMesh(g.getGLCommon());
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
        PMatrixI3x3F.identity();

      final KCamera camera = KCamera.newCamera(view, projection);
      final KVisibleSetTranslucentsBuilderWithCreateType tb =
        KVisibleSetTranslucents.newBuilder(camera);

      final Texture2DStaticType t =
        RFakeTextures2DStatic.newWithName(g, "t2d");
      final TextureCubeStaticType tc = RFakeTexturesCubeStatic.newAnything(g);
      final RKFMaterialCases material_cases = new RKFMaterialCases(t, tc);
      final RKFLightCases light_cases = new RKFLightCases();

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

      final KVisibleSetTranslucents translucents =
        tb.visibleTranslucentsCreate();

      final KMutableMatrices m = KMutableMatrices.newMatrices();
      m.withObserver(
        view,
        projection,
        new KMatricesObserverFunctionType<Unit, UnreachableCodeException>() {
          @Override public Unit run(
            final KMatricesObserverType mwo)
            throws RException
          {
            r.rendererEvaluateTranslucents(framebuffer, mwo, translucents);
            return Unit.unit();
          }
        });
    } catch (final JCGLExceptionExecution ee) {
      Assert.assertTrue(ee.getCause() instanceof RExceptionResource);
      throw (RExceptionResource) ee.getCause();
    }
  }

  @Test(expected = RExceptionResource.class) public void testOutOfUnits_0()
    throws Exception
  {
    try {
      final JCGLSoftRestrictionsType restrictor =
        new JCGLSoftRestrictionsType() {
          @Override public int restrictTextureUnitCount(
            final int count)
          {
            return 2;
          }

          @Override public boolean restrictExtensionVisibility(
            final String name)
          {
            return true;
          }
        };
      final OptionType<JCGLSoftRestrictionsType> restrict =
        Option.some(restrictor);

      final JCGLImplementationType g =
        RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), restrict);
      final KTranslucentRendererType r =
        KTranslucentRendererTest.makeRenderer(g);

      final KFramebufferRGBADescription rgba_desc =
        KFramebufferRGBADescription.newDescription(
          RFakeGL.SCREEN_AREA,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          KRGBAPrecision.RGBA_PRECISION_8);
      final KFramebufferRGBAWithDepthUsableType framebuffer =
        KFramebufferRGBAWithDepth.newFramebuffer(g, rgba_desc);

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
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      final KMeshReadableType mesh =
        KTranslucentRendererTest.makeMesh(g.getGLCommon());
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
        PMatrixI3x3F.identity();

      final KCamera camera = KCamera.newCamera(view, projection);
      final KVisibleSetTranslucentsBuilderWithCreateType tb =
        KVisibleSetTranslucents.newBuilder(camera);

      final Texture2DStaticType t =
        RFakeTextures2DStatic.newWithName(g, "t2d");
      final TextureCubeStaticType tc = RFakeTexturesCubeStatic.newAnything(g);
      final RKFMaterialCases material_cases = new RKFMaterialCases(t, tc);
      final RKFLightCases light_cases = new RKFLightCases();

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

      final KVisibleSetTranslucents translucents =
        tb.visibleTranslucentsCreate();

      final KMutableMatrices m = KMutableMatrices.newMatrices();
      m.withObserver(
        view,
        projection,
        new KMatricesObserverFunctionType<Unit, UnreachableCodeException>() {
          @Override public Unit run(
            final KMatricesObserverType mwo)
            throws RException
          {
            r.rendererEvaluateTranslucents(framebuffer, mwo, translucents);
            return Unit.unit();
          }
        });
    } catch (final JCGLExceptionExecution ee) {
      Assert.assertTrue(ee.getCause() instanceof RExceptionResource);
      throw (RExceptionResource) ee.getCause();
    }
  }
}
