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

package com.io7m.renderer.examples.viewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.java.dev.designgridlayout.DesignGridLayout;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.google.common.util.concurrent.AtomicDouble;
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
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.renderer.examples.ExampleData;
import com.io7m.renderer.examples.ExampleRendererConstructorType;
import com.io7m.renderer.examples.ExampleRendererForwardType;
import com.io7m.renderer.examples.ExampleRendererType;
import com.io7m.renderer.examples.ExampleRendererVisitorType;
import com.io7m.renderer.examples.ExampleSceneBuilderType;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.examples.tools.EMeshCache;
import com.io7m.renderer.examples.tools.ETexture2DCache;
import com.io7m.renderer.examples.tools.ETextureCubeCache;
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
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.renderer.types.RExceptionLightMissingShadow;
import com.io7m.renderer.types.RXMLException;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;

@SuppressWarnings("synthetic-access") final class VExample implements
  Callable<Unit>
{
  private static final class VExampleWindow extends JFrame
  {
    private static final long      serialVersionUID;

    static {
      serialVersionUID = 2157518454345881373L;
    }

    private final GLJPanel         canvas;
    private final JLabel           canvas_label;
    private final ViewerConfig     config;
    private final AtomicInteger    current_view_index;
    private final ExampleSceneType example;
    private final VExpectedImage   expected;
    private final JLabel           expected_label;
    private final VExampleWindowGL gl_events;
    private final JLabel           label_example;
    private final JLabel           label_fps;
    private final JLabel           label_renderer;
    private final JLabel           label_view;
    private final LogUsableType    log;
    private final AtomicBoolean    want_save;
    private final Timer            fps_update_timer;

    public VExampleWindow(
      final LogUsableType in_log,
      final ViewerConfig in_config,
      final FSCapabilityReadType in_filesystem,
      final ExampleRendererConstructorType in_renderer_cons,
      final ExampleSceneType in_example)
    {
      this.log = in_log.with("window");
      this.config = in_config;
      this.example = in_example;
      this.current_view_index = new AtomicInteger(0);
      this.want_save = new AtomicBoolean(false);

      this.label_renderer = new JLabel("");
      this.label_view =
        new JLabel(this.getViewText(this.current_view_index.get(), in_example
          .exampleViewpoints()
          .size()));

      this.label_example = new JLabel(in_example.exampleGetName());
      this.label_fps = new JLabel("");

      this.expected =
        new VExpectedImage(in_log, this.example, this.current_view_index);

      this.gl_events =
        new VExampleWindowGL(
          in_log,
          in_config,
          this.current_view_index,
          this.want_save,
          in_filesystem,
          in_example,
          in_renderer_cons,
          this.expected,
          this.label_renderer);

      final GLProfile profile = GLProfile.getDefault();
      final GLCapabilities caps = new GLCapabilities(profile);

      this.canvas = new GLJPanel(caps);
      this.canvas.addGLEventListener(this.gl_events);

      this.expected.setPreferredSize(new Dimension(
        VExample.EXAMPLE_RESULT_IMAGE_WIDTH,
        VExample.EXAMPLE_RESULT_IMAGE_HEIGHT));

      this.expected_label = new JLabel("Expected");
      this.canvas_label = new JLabel("OpenGL");

      final JPanel top_panel = new JPanel();
      final DesignGridLayout top_layout = new DesignGridLayout(top_panel);
      top_layout.row().grid().add(this.expected_label).add(this.canvas_label);
      top_layout.row().grid().add(this.expected).add(this.canvas);

      this.canvas.setPreferredSize(new Dimension(
        VExample.EXAMPLE_RESULT_IMAGE_WIDTH,
        VExample.EXAMPLE_RESULT_IMAGE_HEIGHT));

      final JButton previous = new JButton("Previous view");
      previous.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          VExampleWindow.this.log.debug("Previous view requested");
          VExampleWindow.this.exampleViewPrevious();
        }
      });

      final JButton next = new JButton("Next view");
      next.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          VExampleWindow.this.log.debug("Next view requested");
          VExampleWindow.this.exampleViewNext();
        }
      });

      final JButton save = new JButton("Save view");
      save.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          VExampleWindow.this.log.debug("Save requested");
          VExampleWindow.this.want_save.set(true);
        }
      });

      final JPanel controls = new JPanel();
      final DesignGridLayout controls_layout = new DesignGridLayout(controls);
      controls_layout.row().left().add(previous).add(next).add(save);
      controls_layout.emptyRow();
      controls_layout
        .row()
        .grid(new JLabel("Renderer"))
        .add(this.label_renderer);
      controls_layout
        .row()
        .grid(new JLabel("Example"))
        .add(this.label_example);
      controls_layout.row().grid(new JLabel("View")).add(this.label_view);
      controls_layout
        .row()
        .grid(new JLabel("Estimated FPS"))
        .add(this.label_fps);

      final Container pane = this.getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(top_panel);
      pane.add(controls);

      final GLAnimatorControl animator = new FPSAnimator(this.canvas, 60);
      animator.start();

      this.fps_update_timer = new Timer();
      this.fps_update_timer.scheduleAtFixedRate(new TimerTask() {
        @Override public void run()
        {
          SwingUtilities.invokeLater(new Runnable() {
            @Override public void run()
            {
              final double x = VExampleWindow.this.gl_events.getFPSEstimate();
              VExampleWindow.this.label_fps.setText(Double.toString(x));
            }
          });
        }
      },
        1000,
        1000);
    }

    private void exampleViewNext()
    {
      final int max = this.example.exampleViewpoints().size();
      final int current = this.current_view_index.get();
      final int next = ((current + 1) >= max) ? 0 : current + 1;
      this.current_view_index.set(next);
      this.expected.viewChanged();
      this.label_view.setText(this.getViewText(next, max));
    }

    private void exampleViewPrevious()
    {
      final int max = this.example.exampleViewpoints().size();
      final int current = this.current_view_index.get();
      final int next = ((current - 1) < 0) ? max - 1 : current - 1;
      this.current_view_index.set(next);
      this.expected.viewChanged();
      this.label_view.setText(this.getViewText(next, max));
    }

    @SuppressWarnings({ "static-method", "boxing" }) private
      String
      getViewText(
        final int view_index,
        final int max_views)
    {
      final String text =
        String.format("%d of %d", view_index + 1, max_views);
      assert text != null;
      return text;
    }
  }

  private static final class VExampleWindowGL implements GLEventListener
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

    private final ViewerConfig                   config;
    private final AtomicInteger                  current_view_index;
    private final ExampleSceneType               example;
    private final FSCapabilityReadType           filesystem;
    private @Nullable KFramebufferType           framebuffer;
    private @Nullable JCGLImplementationType     gi;
    private final LogUsableType                  glog;
    private @Nullable KUnitQuad                  quad;
    private @Nullable ExampleRendererType        renderer;
    private final ExampleRendererConstructorType renderer_cons;
    private final JLabel                         renderer_label;
    private final VExpectedImage                 results_panel;
    private boolean                              running;
    private @Nullable KShaderCacheType           shader_cache;
    private @Nullable TextureLoaderType          texture_loader;
    private final AtomicDouble                   time_fps_estimate;
    private final AtomicLong                     time_last;
    private final AtomicBoolean                  want_save;
    private @Nullable ETextureCubeCache          cube_cache;
    private @Nullable ETexture2DCache            texture2d_cache;
    private @Nullable EMeshCache                 mesh_cache;

    public VExampleWindowGL(
      final LogUsableType in_log,
      final ViewerConfig in_config,
      final AtomicInteger in_view_id,
      final AtomicBoolean in_want_save,
      final FSCapabilityReadType in_filesystem,
      final ExampleSceneType in_example,
      final ExampleRendererConstructorType in_renderer_cons,
      final VExpectedImage in_results_panel,
      final JLabel in_renderer_label)
    {
      this.glog = in_log.with("gl");
      this.filesystem = in_filesystem;
      this.renderer_cons = in_renderer_cons;
      this.example = in_example;
      this.current_view_index = in_view_id;
      this.config = in_config;
      this.want_save = in_want_save;
      this.results_panel = in_results_panel;
      this.renderer_label = in_renderer_label;
      this.time_last = new AtomicLong(0);
      this.time_fps_estimate = new AtomicDouble(0.0);
    }

    @Override public void display(
      final @Nullable GLAutoDrawable drawable)
    {
      try {
        if (this.running == false) {
          return;
        }

        assert drawable != null;
        this.estimateFPS();

        final List<ExampleViewType> views = this.example.exampleViewpoints();
        assert views != null;
        assert views.size() > 0;

        final int view_index = this.current_view_index.get();
        final ExampleViewType view = views.get(view_index);
        assert view != null;

        final KSceneBuilderWithCreateType scene_builder =
          KScene.newBuilder(view.getCamera());

        this.example.exampleScene(new ExampleSceneBuilderType() {
          @Override public TextureCubeStaticUsableType cubeTexture(
            final String name)
            throws RException
          {
            try {
              final ETextureCubeCache cc = VExampleWindowGL.this.cube_cache;
              assert cc != null;
              return cc.loadCube(name);
            } catch (final ValidityException e) {
              throw RXMLException.validityException(e);
            } catch (final IOException e) {
              throw RExceptionIO.fromIOException(e);
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            } catch (final ParsingException e) {
              throw RXMLException.parsingException(e);
            }
          }

          @Override public KMeshReadableType mesh(
            final String name)
            throws RException
          {
            final EMeshCache mc = VExampleWindowGL.this.mesh_cache;
            assert mc != null;
            return mc.loadMesh(name);
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

          @Override public Texture2DStaticUsableType texture(
            final String name)
            throws RException
          {
            try {
              final ETexture2DCache tc =
                VExampleWindowGL.this.texture2d_cache;
              assert tc != null;
              return tc.loadTexture(name);
            } catch (final IOException e) {
              throw RExceptionIO.fromIOException(e);
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            }
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
            sceneGetInstancesOpaqueShadowCastingByLight()
          {
            return scene_builder
              .sceneGetInstancesOpaqueShadowCastingByLight();
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

          @Override public void sceneAddShadowCaster(
            final KLightType light,
            final KInstanceOpaqueType instance)
            throws RExceptionLightMissingShadow
          {
            scene_builder.sceneAddShadowCaster(light, instance);
          }

          @Override public KSceneLightGroupBuilderType sceneNewLightGroup(
            final String name)
            throws RExceptionLightGroupAlreadyAdded
          {
            return scene_builder.sceneNewLightGroup(name);
          }
        });

        final JCGLImplementationType g = this.gi;
        assert g != null;

        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.colorBufferClear4f(0.0f, 0.0f, 1.0f, 1.0f);

        final ExampleRendererType r = this.renderer;
        assert r != null;

        r.rendererAccept(new ExampleRendererVisitorType<Unit>() {
          @Override public Unit visitForward(
            final ExampleRendererForwardType rf)
            throws RException
          {
            try {
              final KRendererForwardType fr = rf.rendererGetForward();
              final KFramebufferForwardType fb =
                (KFramebufferForwardType) VExampleWindowGL.this.framebuffer;
              assert fb != null;

              final KScene sc = scene_builder.sceneCreate();
              final KSceneBatchedForward batched =
                KSceneBatchedForward.fromScene(sc);

              fr.rendererForwardEvaluate(fb, batched);
              VExampleWindowGL.this.renderSceneResults(fb);

              if (VExampleWindowGL.this.want_save.get()) {
                VExampleWindowGL.this.want_save.set(false);
                VExampleWindowGL.this.saveImage(drawable, view_index);
              }

              return Unit.unit();
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            }
          }
        });

      } catch (final Throwable e) {
        this.failed(e);
      }
    }

    @Override public void dispose(
      final @Nullable GLAutoDrawable drawable)
    {
      this.glog.debug("dispose");
    }

    private void estimateFPS()
    {
      final long prev = this.time_last.get();
      final long now = System.nanoTime();
      this.time_last.set(now);

      /**
       * The time taken since the start of the previous frame, in
       * milliseconds.
       */

      final double diff = now - prev;
      final double diff_mili = diff / 1000000.0;

      /**
       * The number of frames that would be rendered per second if all frames
       * took that long.
       */

      this.time_fps_estimate.set(1000.0 / diff_mili);
    }

    private void failed(
      final Throwable e)
    {
      VErrorBox.showErrorLater(this.glog, e);
      this.running = false;
    }

    public double getFPSEstimate()
    {
      return this.time_fps_estimate.get();
    }

    @Override public void init(
      final @Nullable GLAutoDrawable drawable)
    {
      try {
        this.glog.debug("init");

        this.running = true;

        assert drawable != null;
        drawable.getContext().setSwapInterval(1);

        final JCGLImplementationType g =
          JCGLImplementationJOGL.newImplementation(
            drawable.getContext(),
            this.glog);
        assert g != null;
        this.gi = g;

        this.texture_loader =
          TextureLoaderImageIO
            .newTextureLoaderWithAlphaPremultiplication(this.glog);

        this.cube_cache =
          new ETextureCubeCache(g, this.texture_loader, this.glog);
        this.texture2d_cache =
          new ETexture2DCache(g, this.texture_loader, this.glog);
        this.mesh_cache = new EMeshCache(g, this.glog);

        final LRUCacheConfig scc =
          LRUCacheConfig
            .empty()
            .withMaximumCapacity(BigInteger.valueOf(1024));

        final KShaderCacheType sc =
          KShaderCacheFilesystem.wrap(LRUCacheTrivial.newCache(
            KShaderCacheFilesystemLoader.newLoader(
              g,
              this.filesystem,
              this.glog),
            scc));
        assert sc != null;
        this.shader_cache = sc;

        final JCGLInterfaceCommonType gc = g.getGLCommon();
        this.quad = KUnitQuad.newQuad(gc, this.glog);

        final ExampleRendererType r =
          this.renderer_cons.newRenderer(this.glog, sc, g);
        assert r != null;
        this.renderer = r;

        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run()
          {
            VExampleWindowGL.this.renderer_label.setText(r.rendererGetName());
          }
        });

        this.results_panel.loadImages(r);

        this.framebuffer =
          r
            .rendererAccept(new ExampleRendererVisitorType<KFramebufferType>() {
              @Override public KFramebufferType visitForward(
                final ExampleRendererForwardType rf)
                throws RException
              {
                final RangeInclusiveL range_x =
                  new RangeInclusiveL(0, drawable.getWidth() - 1);
                final RangeInclusiveL range_y =
                  new RangeInclusiveL(0, drawable.getHeight() - 1);
                final AreaInclusive area =
                  new AreaInclusive(range_x, range_y);

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
                  g,
                  KFramebufferForwardDescription.newDescription(
                    rgba_description,
                    depth_description));
              }
            });

        gc.colorBufferClear4f(0.0f, 0.0f, 1.0f, 1.0f);

      } catch (final Throwable e) {
        this.failed(e);
      }
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
            final KUnitQuad q = VExampleWindowGL.this.quad;
            assert q != null;
            VExampleWindowGL.drawQuad(gc, q, p);
          }
        });

      } finally {
        gc.framebufferDrawUnbind();
      }
    }

    @SuppressWarnings("boxing") @Override public void reshape(
      final @Nullable GLAutoDrawable drawable,
      final int x,
      final int y,
      final int width,
      final int height)
    {
      this.glog.debug(String.format(
        "reshape (%d, %d) %dx%d",
        x,
        y,
        width,
        height));
    }

    private void saveImage(
      final GLAutoDrawable drawable,
      final int view_index)
    {
      this.glog.debug("downloading framebuffer image");

      final int width = drawable.getWidth();
      final int height = drawable.getHeight();
      final ByteBuffer pixels =
        Buffers.newDirectByteBuffer(width * 4 * height);
      final GL gl = drawable.getGL();

      gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
      gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
      gl.glReadPixels(
        0,
        0,
        width,
        height,
        GL.GL_RGBA,
        GL.GL_UNSIGNED_BYTE,
        pixels);

      final ExampleRendererType er = this.renderer;
      assert er != null;
      final File out =
        VExample
          .getOutputImageName(this.config, this.example, er, view_index);

      final SwingWorker<Unit, Unit> worker = new SwingWorker<Unit, Unit>() {
        @Override protected Unit doInBackground()
          throws Exception
        {
          VExampleWindowGL.this.glog.debug(String.format(
            "writing image %s",
            out));

          final BufferedImage image =
            new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
          final Graphics2D gr = image.createGraphics();

          /**
           * Flip image.
           */

          final int span = width * 4;
          for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
              final int index = (x * 4) + (y * span);
              final int pixel = pixels.getInt(index);

              final float r = ((pixel >> 0) & 0xFF) / 256.0f;
              final float g = ((pixel >> 8) & 0xFF) / 256.0f;
              final float b = ((pixel >> 16) & 0xFF) / 256.0f;
              final float a = ((pixel >> 24) & 0xFF) / 256.0f;

              gr.setColor(new Color(r, g, b, a));
              gr.drawLine(x, height - y, x, height - y);
            }
          }

          out.getParentFile().mkdirs();
          final FileOutputStream stream = new FileOutputStream(out);
          ImageIO.write(image, "PNG", stream);
          stream.close();
          return Unit.unit();
        }

        @Override protected void done()
        {
          try {
            this.get();
            final String m =
              String.format("image %s written successfully", out);
            assert m != null;
            VExampleWindowGL.this.glog.debug(m);
          } catch (final InterruptedException e) {
            VErrorBox.showErrorLater(VExampleWindowGL.this.glog, e);
          } catch (final ExecutionException e) {
            VErrorBox.showErrorLater(VExampleWindowGL.this.glog, e);
          }
        }
      };

      worker.execute();
    }
  }

  private static final class VExpectedImage extends JLabel
  {
    private static final long      serialVersionUID;

    static {
      serialVersionUID = 1636894751686016138L;
    }

    private final AtomicInteger    current_view_index;
    private final List<ImageIcon>  images;
    private volatile boolean       images_loaded;
    private final LogUsableType    rlog;
    private final ExampleSceneType scene;

    public VExpectedImage(
      final LogUsableType in_log,
      final ExampleSceneType in_scene,
      final AtomicInteger in_current_view_index)
    {
      this.rlog = in_log.with("results");
      this.scene = in_scene;
      this.current_view_index = in_current_view_index;

      this.setText("Waiting for images...");

      final List<ExampleViewType> views = this.scene.exampleViewpoints();
      this.images = new ArrayList<ImageIcon>(views.size());
      this.images_loaded = false;

      for (int index = 0; index < views.size(); ++index) {
        this.images.add(index, null);
      }
    }

    void loadImages(
      final ExampleRendererType actual_renderer)
    {
      final List<ExampleViewType> views = this.scene.exampleViewpoints();

      final SwingWorker<Unit, Unit> worker = new SwingWorker<Unit, Unit>() {
        @Override protected Unit doInBackground()
          throws Exception
        {
          for (int index = 0; index < views.size(); ++index) {
            final String file =
              ExampleData.getResultFile(
                actual_renderer,
                VExpectedImage.this.scene,
                index);
            VExpectedImage.this.rlog.debug(String.format(
              "Attempting to load %s",
              file));

            final InputStream stream =
              ExampleData.getResultImageStream(
                actual_renderer,
                VExpectedImage.this.scene,
                index);

            try {
              if (stream == null) {
                VExpectedImage.this.rlog.debug(String.format(
                  "Nonexistent %s",
                  file));
                continue;
              }

              final BufferedImage i = ImageIO.read(stream);
              if (i == null) {
                VExpectedImage.this.rlog.debug(String.format(
                  "Corrupt %s",
                  file));
                continue;
              }

              final ImageIcon icon = new ImageIcon(i);
              VExpectedImage.this.images.set(index, icon);
            } finally {
              if (stream != null) {
                stream.close();
              }
            }
          }
          return Unit.unit();
        }

        @Override protected void done()
        {
          try {
            this.get();
            VExpectedImage.this.rlog.debug("Finished loading images");
            VExpectedImage.this.images_loaded = true;
            VExpectedImage.this.viewChanged();
          } catch (final InterruptedException e) {
            VErrorBox.showErrorLater(VExpectedImage.this.rlog, e);
          } catch (final ExecutionException e) {
            VErrorBox.showErrorLater(VExpectedImage.this.rlog, e);
          }
        }
      };

      worker.execute();
    }

    void viewChanged()
    {
      final int view_index = this.current_view_index.get();
      if (this.images_loaded) {
        this.setText("");
        if (view_index < this.images.size()) {
          final ImageIcon icon = this.images.get(view_index);
          if (icon != null) {
            this.setIcon(icon);
          }
        }
      }
    }
  }

  private static final int EXAMPLE_RESULT_IMAGE_HEIGHT;
  private static final int EXAMPLE_RESULT_IMAGE_WIDTH;

  static {
    EXAMPLE_RESULT_IMAGE_HEIGHT = 480;
    EXAMPLE_RESULT_IMAGE_WIDTH = 640;
  }

  @SuppressWarnings("boxing") private static File getOutputImageName(
    final ViewerConfig config,
    final ExampleSceneType example,
    final ExampleRendererType renderer,
    final int view_index)
  {
    final File f0 = config.getReplacementResultsDirectory();
    final File f1 = new File(f0, example.exampleGetName());
    final File f2 = new File(f1, renderer.rendererGetName());
    final File f3 = new File(f2, String.format("%d.png", view_index));
    return f3;
  }

  private final ViewerConfig                      config;
  private @Nullable ExampleSceneType              example;
  private final Class<? extends ExampleSceneType> example_type;
  private final FSCapabilityReadType              filesystem;
  private final LogUsableType                     log;
  private final ExampleRendererConstructorType    renderer_cons;
  private @Nullable VExampleWindow                window;

  VExample(
    final LogUsableType in_log,
    final ViewerConfig in_config,
    final FSCapabilityReadType in_filesystem,
    final ExampleRendererConstructorType in_renderer_cons,
    final Class<? extends ExampleSceneType> in_example_type)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.example_type = in_example_type;
    this.renderer_cons = in_renderer_cons;
    this.config = in_config;
    this.filesystem = in_filesystem;
  }

  @Override public Unit call()
    throws Exception
  {
    final String message =
      String.format("Running %s", this.example_type.getCanonicalName());
    assert message != null;
    this.log.info(message);

    this.example =
      NullCheck.notNull(this.example_type.newInstance(), "New instance");

    final VExampleWindow w =
      new VExampleWindow(
        this.log,
        this.config,
        this.filesystem,
        this.renderer_cons,
        this.example);
    assert w != null;

    w.pack();
    w.setVisible(true);
    this.window = w;

    return Unit.unit();
  }

}
