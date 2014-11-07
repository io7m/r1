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

import org.junit.Test;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jtensors.VectorI4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.KDepthRenderer;
import com.io7m.r1.kernel.KDepthRendererType;
import com.io7m.r1.kernel.KDepthVarianceRenderer;
import com.io7m.r1.kernel.KDepthVarianceRendererType;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCache;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KMatricesObserverFunctionType;
import com.io7m.r1.kernel.KMatricesObserverType;
import com.io7m.r1.kernel.KMutableMatrices;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVariance;
import com.io7m.r1.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.r1.kernel.KRefractionRendererType;
import com.io7m.r1.kernel.KRegionCopier;
import com.io7m.r1.kernel.KRegionCopierType;
import com.io7m.r1.kernel.KRendererDeferredOpaque;
import com.io7m.r1.kernel.KRendererDeferredOpaqueType;
import com.io7m.r1.kernel.KShadowMapCache;
import com.io7m.r1.kernel.KShadowMapCacheType;
import com.io7m.r1.kernel.KShadowMapContextType;
import com.io7m.r1.kernel.KShadowMapRenderer;
import com.io7m.r1.kernel.KShadowMapRendererType;
import com.io7m.r1.kernel.KShadowMapWithType;
import com.io7m.r1.kernel.KViewRaysCache;
import com.io7m.r1.kernel.KViewRaysCacheType;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescriptionBuilderType;
import com.io7m.r1.kernel.types.KFrustumMeshCache;
import com.io7m.r1.kernel.types.KFrustumMeshCacheType;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.kernel.types.KLightWithShadowType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KTransformMatrix4x4;
import com.io7m.r1.kernel.types.KTransformType;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitSphereCacheType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetBuilderWithCreateType;
import com.io7m.r1.kernel.types.KVisibleSetLightGroupBuilderType;
import com.io7m.r1.rmb.RBUnitSphereResourceCache;
import com.io7m.r1.shaders.deferred.RKDLightCases;
import com.io7m.r1.shaders.deferred.RKDMaterialCases;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;
import com.io7m.r1.tests.TestShaderCaches;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformModelType;
import com.io7m.r1.types.RTransformTextureType;
import com.io7m.r1.types.RTransformViewType;

@SuppressWarnings({ "null", "static-method" }) public final class KRendererDeferredOpaqueTest
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

  private static KRendererDeferredOpaqueType makeRenderer(
    final JCGLImplementationType g,
    final TestShaderCaches tc,
    final LogUsableType in_log,
    final KUnitQuadCacheType qc)
    throws Exception
  {
    try {

      final KUnitSphereCacheType sc =
        RBUnitSphereResourceCache.newCache(
          g.getGLCommon(),
          KRefractionRendererType.class,
          BigInteger.ONE,
          in_log);
      final KFrustumMeshCacheType fc =
        KFrustumMeshCache.newCacheWithCapacity(
          g.getGLCommon(),
          ArrayBufferUpdateUnmapped.newConstructor(),
          IndexBufferUpdateUnmapped.newConstructor(),
          BigInteger.valueOf(250),
          in_log);

      final LRUCacheConfig view_rays_cache_config =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(60));
      final KViewRaysCacheType vrc =
        KViewRaysCache.newCacheWithConfig(
          new Context(),
          view_rays_cache_config);

      final KRendererDeferredOpaqueType r =
        KRendererDeferredOpaque.newRenderer(
          g,
          qc,
          sc,
          fc,
          tc.getShaderDebugCache(),
          tc.getShaderDeferredGeoCache(),
          tc.getShaderDeferredLightCache(),
          vrc);

      return r;
    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }

  @Test public void testAllCases()
    throws Exception
  {
    final LogUsableType in_log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final KUnitQuadCacheType qc =
      KUnitQuadCache.newCache(g.getGLCommon(), in_log);
    final TestShaderCaches tc =
      TestShaderCaches.newCachesFromArchives(g, in_log);

    final KFramebufferDeferredDescriptionBuilderType framebuffer_db =
      KFramebufferDeferredDescription.newBuilder(RFakeGL.SCREEN_AREA);
    final KFramebufferDeferredDescription framebuffer_desc =
      framebuffer_db.build();
    final KFramebufferDeferredUsableType framebuffer =
      KFramebufferDeferred.newFramebuffer(g, framebuffer_desc);

    final KRendererDeferredOpaqueType r =
      KRendererDeferredOpaqueTest.makeRenderer(g, tc, in_log, qc);

    final BLUCacheConfig depth_variance_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.TEN)
        .withMaximumCapacity(BigInteger.valueOf(1024 * 1024 * 8 * 128));
    final KFramebufferDepthVarianceCacheType depth_variance_cache =
      KFramebufferDepthVarianceCache.newCacheWithConfig(
        g,
        depth_variance_cache_config,
        in_log);

    final KRegionCopierType copier = KRegionCopier.newCopier(g, in_log);
    final KDepthRendererType dr =
      KDepthRenderer.newRenderer(g, tc.getShaderDepthCache(), in_log);
    final KDepthVarianceRendererType dvr =
      KDepthVarianceRenderer.newRenderer(g, tc.getShaderDepthVarianceCache());
    final KPostprocessorBlurDepthVarianceType pbdv =
      KPostprocessorBlurDepthVariance.postprocessorNew(
        g,
        copier,
        depth_variance_cache,
        tc.getShaderPostprocessingCache(),
        qc,
        in_log);

    final BLUCacheConfig shadow_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.valueOf(256))
        .withMaximumCapacity(BigInteger.valueOf(1024 * 1024 * 8 * 128));

    final KShadowMapCacheType sc =
      KShadowMapCache.newCacheWithConfig(g, shadow_cache_config, in_log);
    final KShadowMapRendererType sr =
      KShadowMapRenderer.newRenderer(g, dr, dvr, pbdv, sc, in_log);

    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();
    final KProjectionType projection =
      KProjectionFOV.newProjection(
        new MatrixM4x4F(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        100.0f);

    final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
    final KTransformType transform = KTransformMatrix4x4.newTransform(model);
    final KMeshReadableType mesh =
      KRendererDeferredOpaqueTest.makeMesh(g.getGLCommon());
    final RMatrixI3x3F<RTransformTextureType> m_uv = RMatrixI3x3F.identity();

    final KCamera camera = KCamera.newCamera(view, projection);
    final KVisibleSetBuilderWithCreateType tb =
      KVisibleSet.newBuilder(camera);

    final Texture2DStaticType t2d =
      RFakeTextures2DStatic.newWithName(g, "t2d");
    final TextureCubeStaticType tcube =
      RFakeTexturesCubeStatic.newAnything(g);
    final RKDMaterialCases material_cases = new RKDMaterialCases(t2d, tcube);
    final RKDLightCases light_cases = new RKDLightCases(t2d, tcube);

    final KVisibleSetLightGroupBuilderType lg =
      tb.visibleOpaqueNewLightGroup("g0");

    for (final KLightType l : light_cases.getCases()) {
      lg.groupAddLight(l);
    }

    for (final KMaterialOpaqueRegular c : material_cases
      .getCasesGeometryOpaqueRegular()) {

      final KInstanceOpaqueRegular o =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          c,
          transform,
          m_uv,
          KFaceSelection.FACE_RENDER_FRONT);

      lg.groupAddInstance(o);

      for (final KLightType l : light_cases.getCases()) {
        if (l instanceof KLightWithShadowType) {
          tb.visibleShadowsAddCaster((KLightWithShadowType) l, o);
        }
      }
    }

    for (final KMaterialOpaqueRegular c : material_cases
      .getCasesGeometryOpaqueRegular()) {
      final KInstanceOpaqueRegular o =
        KInstanceOpaqueRegular.newInstance(
          mesh,
          c,
          transform,
          m_uv,
          KFaceSelection.FACE_RENDER_FRONT);
      tb.visibleOpaqueAddUnlit(o);
    }

    final KVisibleSet visible = tb.visibleCreate();

    framebuffer.deferredFramebufferClear(g.getGLCommon(), new VectorI4F(
      1.0f,
      1.0f,
      1.0f,
      1.0f), 1.0f, 0);

    final KMutableMatrices m = KMutableMatrices.newMatrices();
    sr.rendererEvaluateShadowMaps(
      camera,
      visible.getShadows(),
      new KShadowMapWithType<Unit, RException>() {
        @Override public Unit withMaps(
          final KShadowMapContextType shadow_context)
          throws RException
        {
          return m
            .withObserver(
              view,
              projection,
              new KMatricesObserverFunctionType<Unit, UnreachableCodeException>() {
                @Override public Unit run(
                  final KMatricesObserverType mwo)
                  throws RException
                {
                  final OptionType<DepthFunction> depth_function =
                    Option.none();
                  r.rendererEvaluateOpaqueLit(
                    framebuffer,
                    shadow_context,
                    depth_function,
                    mwo,
                    visible.getOpaques());
                  r.rendererEvaluateOpaqueUnlit(
                    framebuffer,
                    shadow_context,
                    depth_function,
                    mwo,
                    visible.getOpaques());
                  return Unit.unit();
                }
              });
        }
      });
  }
}
