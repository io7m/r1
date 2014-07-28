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

package com.io7m.renderer.tests.kernel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcache.BLUCacheConfig;
import com.io7m.jcache.JCacheEventsType;
import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcache.LRUCacheType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.FragmentShaderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionProgramCompileError;
import com.io7m.jcanephora.JCGLType;
import com.io7m.jcanephora.ProgramAttributeType;
import com.io7m.jcanephora.ProgramType;
import com.io7m.jcanephora.ProgramUniformType;
import com.io7m.jcanephora.ProgramUsableType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.VertexShaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutor;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.fake.FakeProgram;
import com.io7m.jcanephora.fake.FakeProgramAttribute;
import com.io7m.jcanephora.fake.FakeProgramUniform;
import com.io7m.jcanephora.fake.FakeShaderControlType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jparasol.core.JPFragmentShaderMetaType;
import com.io7m.jparasol.core.JPUncompactedProgramShaderMeta;
import com.io7m.jparasol.core.JPVertexShaderMetaType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KDepthRenderer;
import com.io7m.renderer.kernel.KDepthRendererType;
import com.io7m.renderer.kernel.KDepthVarianceRenderer;
import com.io7m.renderer.kernel.KDepthVarianceRendererType;
import com.io7m.renderer.kernel.KFramebufferDepthVarianceUsableType;
import com.io7m.renderer.kernel.KPostprocessorBlurDepthVarianceType;
import com.io7m.renderer.kernel.KProgramType;
import com.io7m.renderer.kernel.KShaderCache;
import com.io7m.renderer.kernel.KShaderCacheDepthType;
import com.io7m.renderer.kernel.KShaderCacheDepthVarianceType;
import com.io7m.renderer.kernel.KShadowMapCache;
import com.io7m.renderer.kernel.KShadowMapCacheType;
import com.io7m.renderer.kernel.KShadowMapContextType;
import com.io7m.renderer.kernel.KShadowMapRenderer;
import com.io7m.renderer.kernel.KShadowMapRendererType;
import com.io7m.renderer.kernel.KShadowMapType;
import com.io7m.renderer.kernel.KShadowMapUsableType;
import com.io7m.renderer.kernel.KShadowMapWithType;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KProjectionFOV;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBatchedDeferred;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMapDescriptionType;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KTransformMatrix4x4;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.tests.RFakeGL;
import com.io7m.renderer.tests.RFakeTextures2DStatic;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.renderer.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.renderer.types.RExceptionMeshMissingNormals;
import com.io7m.renderer.types.RExceptionMeshMissingPositions;
import com.io7m.renderer.types.RExceptionMeshMissingTangents;
import com.io7m.renderer.types.RExceptionMeshMissingUVs;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public class KShadowMapRendererTest
{
  private KInstanceOpaqueType getOpaque(
    final JCGLImplementationType g)
  {
    try {
      final JCGLInterfaceCommonType gc = g.getGLCommon();

      final RMatrixI3x3F<RTransformTextureType> uv = RMatrixI3x3F.identity();
      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType trans = KTransformMatrix4x4.newTransform(model);
      final KMaterialOpaqueRegular in_material =
        KMaterialOpaqueRegular.newBuilder().build();

      final ArrayDescriptor type = KMesh.getStandardDescriptor();

      final ArrayBufferType array =
        gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);

      final IndexBufferType indices =
        gc.indexBufferAllocate(array, 1, UsageHint.USAGE_STATIC_DRAW);
      final RVectorI3F<RSpaceObjectType> bounds_low = RVectorI3F.zero();
      final RVectorI3F<RSpaceObjectType> bounds_hi = RVectorI3F.one();
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_low, bounds_hi);
      return KInstanceOpaqueRegular.newInstance(
        mesh,
        in_material,
        trans,
        uv,
        KFaceSelection.FACE_RENDER_FRONT);
    } catch (final RExceptionMaterialMissingAlbedoTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingSpecularTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingUVs e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingNormals e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingTangents e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingPositions e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Test public void testShadowMapRenderer()
    throws Exception
  {
    final FakeShaderControlType shader_control = new FakeShaderControlType() {
      @Override public void onFragmentShaderCompile(
        final String name,
        final FragmentShaderType v)
        throws JCGLException
      {

      }

      @Override public void onProgramCreate(
        final String name,
        final ProgramUsableType program,
        final Map<String, ProgramUniformType> uniforms,
        final Map<String, ProgramAttributeType> attributes)
        throws JCGLException
      {
        final FakeProgram fp = (FakeProgram) program;

        if ("DepC".equals(name)) {
          uniforms.put("m_projection", FakeProgramUniform.newUniform(
            fp.getContext(),
            program,
            0,
            0,
            "m_projection",
            JCGLType.TYPE_FLOAT_MATRIX_4));
          uniforms.put("m_modelview", FakeProgramUniform.newUniform(
            fp.getContext(),
            program,
            0,
            0,
            "m_modelview",
            JCGLType.TYPE_FLOAT_MATRIX_4));
          attributes.put("v_position", FakeProgramAttribute.newAttribute(
            fp.getContext(),
            program,
            0,
            0,
            "v_position",
            JCGLType.TYPE_FLOAT_VECTOR_3));
        }
      }

      @Override public void onVertexShaderCompile(
        final String name,
        final VertexShaderType v)
        throws JCGLException
      {

      }
    };

    final LogUsableType log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30WithLog(log, shader_control);
    final KGraphicsCapabilitiesType caps =
      KGraphicsCapabilities.getCapabilities(g);
    final JCGLInterfaceCommonType gc = g.getGLCommon();

    final LRUCacheConfig config =
      LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));
    final LRUCacheType<String, KProgramType, KProgramType, RException> dc =
      LRUCacheTrivial.newCache(
        new JCacheLoaderType<String, KProgramType, RException>() {
          @Override public void cacheValueClose(
            final KProgramType v)
            throws RException
          {

          }

          @Override public KProgramType cacheValueLoad(
            final String key)
            throws RException
          {
            try {
              System.out.println("key: " + key);

              final List<String> lines = new ArrayList<String>();
              lines.add("Nothing!");
              final VertexShaderType v = gc.vertexShaderCompile(key, lines);
              final FragmentShaderType f =
                gc.fragmentShaderCompile(key, lines);
              final ProgramType p = gc.programCreateCommon(key, v, f);

              final JCBExecutorType exec =
                JCBExecutor.newExecutorWithoutDeclarations(gc, p, log);

              return new KProgramType() {
                @Override public JPVertexShaderMetaType getVertexShaderMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public ProgramType getProgram()
                {
                  return p;
                }

                @Override public JPUncompactedProgramShaderMeta getMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public
                  JPFragmentShaderMetaType
                  getFragmentShaderMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public JCBExecutorType getExecutable()
                {
                  return exec;
                }
              };
            } catch (final JCGLExceptionProgramCompileError e) {
              throw RExceptionJCGL.fromJCGLException(e);
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KProgramType v)
          {
            return BigInteger.ONE;
          }
        }, config);
    final KShaderCacheDepthType depth_shader_cache =
      KShaderCache.wrapDepth(dc);

    final LRUCacheType<String, KProgramType, KProgramType, RException> dvc =
      LRUCacheTrivial.newCache(
        new JCacheLoaderType<String, KProgramType, RException>() {
          @Override public void cacheValueClose(
            final KProgramType v)
            throws RException
          {

          }

          @Override public KProgramType cacheValueLoad(
            final String key)
            throws RException
          {
            try {
              System.out.println("key: " + key);

              final List<String> lines = new ArrayList<String>();
              lines.add("Nothing!");
              final VertexShaderType v = gc.vertexShaderCompile(key, lines);
              final FragmentShaderType f =
                gc.fragmentShaderCompile(key, lines);
              final ProgramType p = gc.programCreateCommon(key, v, f);

              return new KProgramType() {
                @Override public JPVertexShaderMetaType getVertexShaderMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public ProgramType getProgram()
                {
                  return p;
                }

                @Override public JPUncompactedProgramShaderMeta getMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public
                  JPFragmentShaderMetaType
                  getFragmentShaderMeta()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }

                @Override public JCBExecutorType getExecutable()
                {
                  // TODO Auto-generated method stub
                  throw new UnimplementedCodeException();
                }
              };
            } catch (final JCGLExceptionProgramCompileError e) {
              throw RExceptionJCGL.fromJCGLException(e);
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            }
          }

          @Override public BigInteger cacheValueSizeOf(
            final KProgramType v)
          {
            return BigInteger.ONE;
          }
        }, config);
    final KShaderCacheDepthVarianceType depth_variance_shader_cache =
      KShaderCache.wrapDepthVariance(dvc);

    final KDepthRendererType depth_renderer =
      KDepthRenderer.newRenderer(g, caps, depth_shader_cache, log);
    final KDepthVarianceRendererType depth_variance_renderer =
      KDepthVarianceRenderer.newRenderer(g, depth_variance_shader_cache);

    final KPostprocessorBlurDepthVarianceType blur =
      new KPostprocessorBlurDepthVarianceType() {
        @Override public void postprocessorEvaluateDepthVariance(
          final KBlurParameters blur_config,
          final KFramebufferDepthVarianceUsableType input,
          final KFramebufferDepthVarianceUsableType output)
          throws RException
        {
          // Nothing
        }

        @Override public String postprocessorGetName()
        {
          return "pseudo-blur";
        }
      };

    final BLUCacheConfig shadow_cache_config =
      BLUCacheConfig
        .empty()
        .withMaximumBorrowsPerKey(BigInteger.valueOf(32))
        .withMaximumCapacity(BigInteger.valueOf(128 * 32));
    final KShadowMapCacheType shadow_cache =
      KShadowMapCache.newCacheWithConfig(g, shadow_cache_config, log);

    final AtomicInteger loads = new AtomicInteger(0);
    final AtomicInteger retrieves = new AtomicInteger(0);
    shadow_cache
      .cacheEventsSubscribe(new JCacheEventsType<KShadowMapDescriptionType, KShadowMapType>() {
        @Override public void cacheEventValueRetrieved(
          final KShadowMapDescriptionType key,
          final KShadowMapType value,
          final BigInteger size)
        {
          System.out.printf(
            "Cache retrieved: %s → %s (%s)\n",
            key,
            value,
            size);
          retrieves.incrementAndGet();
        }

        @Override public void cacheEventValueLoaded(
          final KShadowMapDescriptionType key,
          final KShadowMapType value,
          final BigInteger size)
        {
          System.out.printf("Cache loaded: %s → %s (%s)\n", key, value, size);
          loads.incrementAndGet();
        }

        @Override public void cacheEventValueEvicted(
          final KShadowMapDescriptionType key,
          final KShadowMapType value,
          final BigInteger size)
        {
          System.out
            .printf("Cache evicted: %s → %s (%s)\n", key, value, size);
        }

        @Override public void cacheEventValueCloseError(
          final KShadowMapDescriptionType key,
          final KShadowMapType value,
          final BigInteger size,
          final Throwable x)
        {

        }
      });

    final KShadowMapRendererType sm =
      KShadowMapRenderer.newRenderer(
        g,
        depth_renderer,
        depth_variance_renderer,
        blur,
        shadow_cache,
        log);

    final MatrixM4x4F temp = new MatrixM4x4F();
    final RMatrixI4x4F<RTransformViewType> id = RMatrixI4x4F.identity();
    final KProjectionFOV proj =
      KProjectionFOV.newProjection(temp, 90.0f, 1.0f, 1.0f, 100.0f);
    final KCamera camera = KCamera.newCamera(id, proj);

    final KSceneBuilderWithCreateType sb = KScene.newBuilder(camera);
    final Texture2DStaticUsableType lt = RFakeTextures2DStatic.newAnything(g);
    final KLightProjectiveBuilderType sl0b =
      KLightProjective.newBuilder(lt, proj);
    final KFramebufferDepthDescription fb_desc =
      KFramebufferDepthDescription.newDescription(
        new AreaInclusive(
          new RangeInclusiveL(0, 3),
          new RangeInclusiveL(0, 3)),
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_16);
    final KShadowMapBasicDescription map_desc =
      KShadowMapBasicDescription.newDescription(fb_desc, 2);
    sl0b.setShadow(KShadowMappedBasic.newMappedBasic(0.001f, 0.0f, map_desc));
    final KLightProjective sl0 = sl0b.build(caps);
    final KLightProjective sl1 = sl0b.build(caps);
    final KLightProjective sl2 = sl0b.build(caps);

    final KInstanceOpaqueType o1 = this.getOpaque(g);
    sb.sceneAddShadowCaster(sl0, o1);
    sb.sceneAddShadowCaster(sl1, o1);
    sb.sceneAddShadowCaster(sl2, o1);

    final KScene s = sb.sceneCreate();
    final KSceneBatchedDeferred sbd = KSceneBatchedDeferred.fromScene(s);

    sm.rendererEvaluateShadowMaps(
      camera,
      sbd.getShadows(),
      new KShadowMapWithType<Unit, UnreachableCodeException>() {
        @Override public Unit withMaps(
          final KShadowMapContextType context)
          throws JCGLException,
            RException
        {
          final KShadowMapUsableType m0 = context.getShadowMap(sl0);
          final KShadowMapUsableType m1 = context.getShadowMap(sl1);
          final KShadowMapUsableType m2 = context.getShadowMap(sl2);

          Assert.assertNotNull(m0);
          Assert.assertNotNull(m1);
          Assert.assertNotNull(m2);

          Assert.assertNotEquals(m0, m1);
          Assert.assertNotEquals(m1, m2);
          Assert.assertNotEquals(m0, m2);
          return Unit.unit();
        }
      });

    sm.rendererEvaluateShadowMaps(
      camera,
      sbd.getShadows(),
      new KShadowMapWithType<Unit, UnreachableCodeException>() {
        @Override public Unit withMaps(
          final KShadowMapContextType context)
          throws JCGLException,
            RException
        {
          final KShadowMapUsableType m0 = context.getShadowMap(sl0);
          final KShadowMapUsableType m1 = context.getShadowMap(sl1);
          final KShadowMapUsableType m2 = context.getShadowMap(sl2);

          Assert.assertNotNull(m0);
          Assert.assertNotNull(m1);
          Assert.assertNotNull(m2);

          Assert.assertNotEquals(m0, m1);
          Assert.assertNotEquals(m1, m2);
          Assert.assertNotEquals(m0, m2);
          return Unit.unit();
        }
      });

    Assert.assertEquals(3, loads.intValue());
    Assert.assertEquals(9, retrieves.intValue());
  }
}
