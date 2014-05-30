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

package com.io7m.renderer.kernel.examples.viewer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGL;
import com.io7m.jcanephora.texload.imageio.TextureLoaderImageIO;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.renderer.kernel.KFramebufferForward;
import com.io7m.renderer.kernel.KFramebufferForwardType;
import com.io7m.renderer.kernel.KFramebufferType;
import com.io7m.renderer.kernel.KProgram;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KShaderCacheFilesystem;
import com.io7m.renderer.kernel.KShaderCacheFilesystemLoader;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KShadingProgramCommon;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.KUnitQuadUsableType;
import com.io7m.renderer.kernel.examples.ExampleClasses;
import com.io7m.renderer.kernel.examples.ExampleRendererConstructorType;
import com.io7m.renderer.kernel.examples.ExampleRendererForwardDefault;
import com.io7m.renderer.kernel.examples.ExampleRendererForwardType;
import com.io7m.renderer.kernel.examples.ExampleRendererType;
import com.io7m.renderer.kernel.examples.ExampleRendererVisitorType;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.examples.tools.EMeshCache;
import com.io7m.renderer.kernel.examples.tools.ETexture2DCache;
import com.io7m.renderer.kernel.examples.tools.ETextureCubeCache;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentLitType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KRGBAPrecision;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBatchedForward;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyShadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnshadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyVisible;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RExceptionLightMissingShadow;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

final class ViewerSingleMainWindow implements Runnable
{
  private static final class Runner
  {
    private static void drawQuad(
      final JCGLInterfaceCommonType gc,
      final KUnitQuadUsableType quad,
      final JCBProgramType p)
      throws JCGLException,
        JCGLException
    {
      final ArrayBufferUsableType array = quad.getArray();
      final IndexBufferUsableType indices = quad.getIndices();

      gc.arrayBufferBind(array);

      try {
        KShadingProgramCommon.bindAttributePosition(p, array);
        KShadingProgramCommon.bindAttributeUV(p, array);
        KShadingProgramCommon.putMatrixUV(
          p,
          ExampleSceneUtilities.IDENTITY_UV_M);

        p.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          }
        });

      } finally {
        gc.arrayBufferUnbind();
      }
    }

    private final ETexture2DCache        cache_2d;
    private final ETextureCubeCache      cache_cube;
    private final EMeshCache             cache_mesh;
    private final ExampleSceneType       example;
    private final KFramebufferType       framebuffer;
    private final JCGLImplementationType gi;
    private final TextureLoaderType      loader;
    private final KUnitQuad              quad;
    private final ExampleRendererType    renderer;
    private final KShaderCacheType       shader_cache;
    private int                          view_index;

    Runner(
      final GLAutoDrawable drawable,
      final JCGLImplementationType in_gi,
      final FilesystemType in_filesystem,
      final ExampleSceneType in_example,
      final ExampleRendererConstructorType in_renderer_cons,
      final LogUsableType in_log)
      throws JCGLException,
        RException
    {
      this.gi = in_gi;
      this.example = in_example;
      this.loader =
        TextureLoaderImageIO
          .newTextureLoaderWithAlphaPremultiplication(in_log);
      this.cache_cube = new ETextureCubeCache(in_gi, this.loader, in_log);
      this.cache_2d = new ETexture2DCache(in_gi, this.loader, in_log);
      this.cache_mesh = new EMeshCache(in_gi, in_log);
      this.quad = KUnitQuad.newQuad(in_gi.getGLCommon(), in_log);

      final LRUCacheConfig scc =
        LRUCacheConfig.empty().withMaximumCapacity(BigInteger.valueOf(1024));

      this.shader_cache =
        KShaderCacheFilesystem.wrap(LRUCacheTrivial.newCache(
          KShaderCacheFilesystemLoader.newLoader(
            this.gi,
            in_filesystem,
            in_log),
          scc));

      this.renderer =
        in_renderer_cons.newRenderer(in_log, this.shader_cache, this.gi);

      this.framebuffer =
        this.renderer
          .rendererAccept(new ExampleRendererVisitorType<KFramebufferType>() {
            @Override public KFramebufferType visitForward(
              final ExampleRendererForwardType rf)
              throws RException
            {
              final RangeInclusiveL range_x =
                new RangeInclusiveL(0, drawable.getWidth() - 1);
              final RangeInclusiveL range_y =
                new RangeInclusiveL(0, drawable.getHeight() - 1);
              final AreaInclusive area = new AreaInclusive(range_x, range_y);

              final KFramebufferRGBADescription rgba_description =
                KFramebufferRGBADescription.newDescription(
                  area,
                  TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
                  TextureFilterMinification.TEXTURE_FILTER_LINEAR,
                  KRGBAPrecision.RGBA_PRECISION_8);

              final KFramebufferDepthDescription depth_description =
                KFramebufferDepthDescription.newDescription(
                  area,
                  TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
                  TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                  KDepthPrecision.DEPTH_PRECISION_24);

              return KFramebufferForward.newFramebuffer(
                Runner.this.gi,
                KFramebufferForwardDescription.newDescription(
                  rgba_description,
                  depth_description));
            }
          });
    }

    private void renderSceneResults(
      final KFramebufferForwardType fb)
      throws JCGLException,
        RException
    {
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final JCGLInterfaceCommonType gc = g.getGLCommon();

      final KShaderCacheType sc = this.shader_cache;
      assert sc != null;
      final KProgram kp = sc.getPostprocessing("copy_rgba");
      gc.framebufferDrawUnbind();

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        gc.viewportSet(fb.kFramebufferGetArea());

        final JCBExecutorType e = kp.getExecutable();
        e.execRun(new JCBExecutorProcedureType<JCGLException>() {
          @Override public void call(
            final JCBProgramType p)
            throws JCGLException
          {
            final List<TextureUnitType> units = gc.textureGetUnits();
            final TextureUnitType unit = units.get(0);

            gc.texture2DStaticBind(unit, fb.kFramebufferGetRGBATexture());
            p.programUniformPutTextureUnit("t_image", unit);
            Runner.drawQuad(gc, Runner.this.quad, p);
          }
        });

      } finally {
        gc.framebufferDrawUnbind();
      }
    }

    public void run()
      throws RException
    {
      final ExampleViewType view =
        this.example.exampleViewpoints().get(this.view_index);
      final KSceneBuilderWithCreateType scene_builder =
        KScene.newBuilder(view.getCamera());

      final ExampleSceneBuilderType b = new ExampleSceneBuilderType() {
        @Override public TextureCubeStaticUsableType cubeTexture(
          final String name)
          throws RException
        {
          try {
            return Runner.this.cache_cube.loadCube(name);
          } catch (final ValidityException e) {
            throw new RuntimeException(e);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          } catch (final ParsingException e) {
            throw new RuntimeException(e);
          }
        }

        @Override public KMeshReadableType mesh(
          final String name)
          throws RException
        {
          return Runner.this.cache_mesh.loadMesh(name);
        }

        @Override public void sceneAddInvisibleWithShadow(
          final KLightType light,
          final KInstanceOpaqueType instance)
          throws RExceptionInstanceAlreadyVisible,
            RExceptionLightMissingShadow
        {
          scene_builder.sceneAddInvisibleWithShadow(light, instance);
        }

        @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
          final KLightType light,
          final KInstanceOpaqueType instance)
          throws RExceptionInstanceAlreadyUnlit,
            RExceptionInstanceAlreadyShadowed
        {
          scene_builder
            .sceneAddOpaqueLitVisibleWithoutShadow(light, instance);
        }

        @Override public void sceneAddOpaqueLitVisibleWithShadow(
          final KLightType light,
          final KInstanceOpaqueType instance)
          throws RExceptionInstanceAlreadyUnlit,
            RExceptionLightMissingShadow,
            RExceptionInstanceAlreadyUnshadowed
        {
          scene_builder.sceneAddOpaqueLitVisibleWithShadow(light, instance);
        }

        @Override public void sceneAddOpaqueUnlit(
          final KInstanceOpaqueType instance)
          throws RExceptionInstanceAlreadyLit
        {
          scene_builder.sceneAddOpaqueUnlit(instance);
        }

        @Override public void sceneAddTranslucentLit(
          final KInstanceTranslucentLitType instance,
          final Set<KLightType> lights)
        {
          scene_builder.sceneAddTranslucentLit(instance, lights);
        }

        @Override public void sceneAddTranslucentUnlit(
          final KInstanceTranslucentUnlitType instance)
        {
          scene_builder.sceneAddTranslucentUnlit(instance);
        }

        @Override public KCamera sceneGetCamera()
        {
          return scene_builder.sceneGetCamera();
        }

        @Override public Set<KInstanceType> sceneGetInstances()
        {
          return scene_builder.sceneGetInstances();
        }

        @Override public
          Set<KInstanceOpaqueType>
          sceneGetInstancesOpaqueLitVisible()
        {
          return scene_builder.sceneGetInstancesOpaqueLitVisible();
        }

        @Override public
          Map<KLightType, Set<KInstanceOpaqueType>>
          sceneGetInstancesOpaqueLitVisibleByLight()
        {
          return scene_builder.sceneGetInstancesOpaqueLitVisibleByLight();
        }

        @Override public
          Map<KLightType, Set<KInstanceOpaqueType>>
          sceneGetInstancesOpaqueShadowCastingByLight()
        {
          return scene_builder.sceneGetInstancesOpaqueShadowCastingByLight();
        }

        @Override public
          Set<KInstanceOpaqueType>
          sceneGetInstancesOpaqueUnlit()
        {
          return scene_builder.sceneGetInstancesOpaqueUnlit();
        }

        @Override public Set<KLightType> sceneGetLights()
        {
          return scene_builder.sceneGetLights();
        }

        @Override public Set<KLightType> sceneGetLightsShadowCasting()
        {
          return scene_builder.sceneGetLightsShadowCasting();
        }

        @Override public List<KTranslucentType> sceneGetTranslucents()
        {
          return scene_builder.sceneGetTranslucents();
        }

        @Override public Texture2DStaticUsableType texture(
          final String name)
          throws RException
        {
          try {
            return Runner.this.cache_2d.loadTexture(name);
          } catch (final IOException e) {
            throw new RuntimeException(e);
          } catch (final JCGLException e) {
            throw new RuntimeException(e);
          }
        }
      };

      this.example.exampleScene(b);

      this.renderer.rendererAccept(new ExampleRendererVisitorType<Unit>() {
        @Override public Unit visitForward(
          final ExampleRendererForwardType rf)
          throws RException
        {
          try {
            final KRendererForwardType fr = rf.rendererGetForward();
            final KFramebufferForwardType fb =
              (KFramebufferForwardType) Runner.this.framebuffer;
            assert fb != null;

            final KScene sc = scene_builder.sceneCreate();
            final KSceneBatchedForward batched =
              KSceneBatchedForward.fromScene(sc);

            fr.rendererForwardEvaluate(fb, batched);
            Runner.this.renderSceneResults(fb);

            return Unit.unit();
          } catch (final JCGLException e) {
            throw RExceptionJCGL.fromJCGLException(e);
          }
        }
      });
    }

    public void setViewIndex(
      final int index)
    {
      final List<ExampleViewType> views = this.example.exampleViewpoints();
      this.view_index = index % views.size();
    }
  }

  private final ViewerConfig     config;
  private final ExampleSceneType example;
  private final FilesystemType   fs;
  private final LogType          log;

  public ViewerSingleMainWindow(
    final ViewerConfig in_config,
    final LogType in_log,
    final FilesystemType in_fs,
    final String example_name)
    throws ClassNotFoundException,
      InstantiationException,
      IllegalAccessException
  {
    this.config = in_config;
    this.fs = in_fs;
    this.log = in_log;
    this.example = ExampleClasses.getScene(example_name);
  }

  @Override public void run()
  {
    final GLProfile pro = GLProfile.getDefault();
    final GLCapabilities caps = new GLCapabilities(pro);
    final GLWindow window = GLWindow.create(caps);

    window.setSize(640, 480);
    window.addGLEventListener(new GLEventListener() {
      private int              frames;
      private @Nullable Runner runner;

      @Override public void display(
        @Nullable final GLAutoDrawable drawable)
      {
        try {
          final Runner r = this.runner;
          assert r != null;
          r.run();
          ++this.frames;
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override public void dispose(
        @Nullable final GLAutoDrawable drawable)
      {

      }

      @Override public void init(
        @Nullable final GLAutoDrawable drawable)
      {
        try {
          assert drawable != null;
          final GLContext ctx = drawable.getContext();
          assert ctx != null;
          final JCGLImplementationType gi =
            JCGLImplementationJOGL.newImplementation(
              ctx,
              ViewerSingleMainWindow.this.log);

          this.runner =
            new Runner(
              drawable,
              gi,
              ViewerSingleMainWindow.this.fs,
              ViewerSingleMainWindow.this.example,
              ExampleRendererForwardDefault.get(),
              ViewerSingleMainWindow.this.log);
        } catch (final JCGLException e) {
          throw new RuntimeException(e);
        } catch (final RException e) {
          throw new RuntimeException(e);
        }
      }

      @Override public void reshape(
        @Nullable final GLAutoDrawable drawable,
        final int x,
        final int y,
        final int width,
        final int height)
      {

      }
    });

    window.addWindowListener(new WindowAdapter() {
      @Override public void windowDestroyed(
        final @Nullable WindowEvent e)
      {
        ViewerSingleMainWindow.this.log.info("Exiting...");
        System.exit(0);
      }
    });

    window.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
    window.setVisible(true);

    final int fps = 30;
    final FPSAnimator anim = new FPSAnimator(fps);
    anim.setUpdateFPSFrames(fps, System.err);
    anim.add(window);
    anim.start();
  }
}
