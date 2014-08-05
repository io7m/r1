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
import java.nio.ByteBuffer;
import java.util.List;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.google.common.util.concurrent.AtomicDouble;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.examples.ExampleImages;
import com.io7m.renderer.examples.ExampleRendererConstructorDebugType;
import com.io7m.renderer.examples.ExampleRendererConstructorDeferredType;
import com.io7m.renderer.examples.ExampleRendererConstructorForwardType;
import com.io7m.renderer.examples.ExampleRendererConstructorType;
import com.io7m.renderer.examples.ExampleRendererConstructorVisitorType;
import com.io7m.renderer.examples.ExampleRendererDebugType;
import com.io7m.renderer.examples.ExampleRendererDeferredType;
import com.io7m.renderer.examples.ExampleRendererForwardType;
import com.io7m.renderer.examples.ExampleRendererType;
import com.io7m.renderer.examples.ExampleRendererVisitorType;
import com.io7m.renderer.examples.ExampleSceneBuilder;
import com.io7m.renderer.examples.ExampleSceneType;
import com.io7m.renderer.examples.ExampleSceneUtilities;
import com.io7m.renderer.examples.ExampleViewType;
import com.io7m.renderer.examples.tools.EMeshCache;
import com.io7m.renderer.examples.tools.ETexture2DCache;
import com.io7m.renderer.examples.tools.ETextureCubeCache;
import com.io7m.renderer.kernel.KFramebufferDeferred;
import com.io7m.renderer.kernel.KFramebufferDeferredType;
import com.io7m.renderer.kernel.KFramebufferForward;
import com.io7m.renderer.kernel.KFramebufferForwardType;
import com.io7m.renderer.kernel.KFramebufferType;
import com.io7m.renderer.kernel.KProgramType;
import com.io7m.renderer.kernel.KRendererDebugType;
import com.io7m.renderer.kernel.KRendererDeferredType;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KRendererType;
import com.io7m.renderer.kernel.KShaderCachePostprocessingType;
import com.io7m.renderer.kernel.KShadingProgramCommon;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KRGBAPrecision;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBatchedDeferred;
import com.io7m.renderer.kernel.types.KSceneBatchedForward;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KUnitQuad;
import com.io7m.renderer.kernel.types.KUnitQuadUsableType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;

@SuppressWarnings("synthetic-access") final class VExample implements
  Callable<Unit>
{
  private static final class VExampleWindow extends JFrame
  {
    private static final long                               serialVersionUID;

    static {
      serialVersionUID = 2157518454345881373L;
    }

    private final GLJPanel                                  canvas;
    private final JLabel                                    canvas_label;
    private final ViewerConfig                              config;
    private final AtomicInteger                             current_view_index;
    private final ExampleSceneType                          example;
    private final VExpectedImage                            expected;
    private final JLabel                                    expected_label;
    private final JComboBox<Class<? extends KRendererType>> expected_selector;
    private final Timer                                     fps_update_timer;
    private final VExampleWindowGL                          gl_events;
    private final JLabel                                    label_example;
    private final JLabel                                    label_fps;
    private final JLabel                                    label_renderer;
    private final JLabel                                    label_view;
    private final LogUsableType                             log;
    private final JLabel                                    renderer_label;
    private final AtomicBoolean                             want_save;

    public VExampleWindow(
      final LogUsableType in_log,
      final ViewerConfig in_config,
      final ExampleImages<BufferedImage> in_example_images,
      final ExampleRendererConstructorType in_renderer_cons,
      final ExampleSceneType in_example)
      throws Exception
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
        new VExpectedImage(
          in_log,
          in_example_images,
          this.example,
          this.current_view_index);

      this.gl_events =
        new VExampleWindowGL(
          this,
          in_log,
          in_config,
          this.current_view_index,
          this.want_save,
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

      this.renderer_label = new JLabel("");
      this.expected_label = new JLabel("Expected");
      this.canvas_label = new JLabel("OpenGL");

      this.expected_selector =
        new JComboBox<Class<? extends KRendererType>>();

      {
        final List<Class<? extends KRendererType>> renderer_results =
          in_example_images.getSceneRenderers(in_example.getClass());
        for (final Class<? extends KRendererType> rc : renderer_results) {
          this.expected_selector.addItem(rc);
        }
        this.expected_selector.addActionListener(new ActionListener() {
          @Override public void actionPerformed(
            @Nullable final ActionEvent _)
          {
            try {
              @SuppressWarnings("unchecked") final Class<? extends KRendererType> i =
                (Class<? extends KRendererType>) VExampleWindow.this.expected_selector
                  .getSelectedItem();

              VExampleWindow.this.expected.update(i);

            } catch (final Exception e) {
              throw new UnreachableCodeException(e);
            }
          }
        });
      }

      final JPanel top_panel = new JPanel();
      final DesignGridLayout top_layout = new DesignGridLayout(top_panel);
      top_layout.row().grid().add(this.expected_label).add(this.canvas_label);
      top_layout
        .row()
        .grid()
        .add(this.expected_selector)
        .add(this.renderer_label);
      top_layout.row().grid().add(this.expected).add(this.canvas);

      this.canvas.setPreferredSize(new Dimension(
        VExample.EXAMPLE_RESULT_IMAGE_WIDTH,
        VExample.EXAMPLE_RESULT_IMAGE_HEIGHT));

      final JButton previous = new JButton("Previous view");
      previous.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          try {
            VExampleWindow.this.log.debug("Previous view requested");
            VExampleWindow.this.exampleViewPrevious();
          } catch (final Exception x) {
            throw new UnreachableCodeException(x);
          }
        }
      });

      final JButton next = new JButton("Next view");
      next.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          try {
            VExampleWindow.this.log.debug("Next view requested");
            VExampleWindow.this.exampleViewNext();
          } catch (final Exception x) {
            throw new UnreachableCodeException(x);
          }
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
      throws Exception
    {
      final int max = this.example.exampleViewpoints().size();
      final int current = this.current_view_index.get();
      final int next = ((current + 1) >= max) ? 0 : current + 1;
      this.current_view_index.set(next);
      this.expected.updateWithSameRenderer();
      this.label_view.setText(this.getViewText(next, max));
    }

    private void exampleViewPrevious()
      throws Exception
    {
      final int max = this.example.exampleViewpoints().size();
      final int current = this.current_view_index.get();
      final int next = ((current - 1) < 0) ? max - 1 : current - 1;
      this.current_view_index.set(next);
      this.expected.updateWithSameRenderer();
      this.label_view.setText(this.getViewText(next, max));
    }

    private void rendererInitialized(
      final Class<? extends KRendererType> i)
      throws Exception
    {
      this.log.debug("Renderer started");
      this.renderer_label.setText(i.getCanonicalName());
      this.expected_selector.setSelectedItem(i);
      this.expected.update(i);
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

    private @Nullable KGraphicsCapabilitiesType  caps;
    private final ViewerConfig                   config;
    private @Nullable ETextureCubeCache          cube_cache;
    private final AtomicInteger                  current_view_index;
    private final ExampleSceneType               example;
    private final VExampleWindow                 example_window;
    private @Nullable KFramebufferType           framebuffer;
    private @Nullable JCGLImplementationType     gi;
    private final LogUsableType                  glog;
    private @Nullable EMeshCache                 mesh_cache;
    private @Nullable KUnitQuad                  quad;
    private @Nullable ExampleRendererType        renderer;
    private final ExampleRendererConstructorType renderer_cons;
    private final JLabel                         renderer_label;
    private final VExpectedImage                 results_panel;
    private boolean                              running;
    private @Nullable VShaderCaches              shader_caches;
    private @Nullable TextureLoaderType          texture_loader;
    private @Nullable ETexture2DCache            texture2d_cache;
    private final AtomicDouble                   time_fps_estimate;
    private final AtomicLong                     time_last;
    private final AtomicBoolean                  want_save;

    public VExampleWindowGL(
      final VExampleWindow in_window,
      final LogUsableType in_log,
      final ViewerConfig in_config,
      final AtomicInteger in_view_id,
      final AtomicBoolean in_want_save,
      final ExampleSceneType in_example,
      final ExampleRendererConstructorType in_renderer_cons,
      final VExpectedImage in_results_panel,
      final JLabel in_renderer_label)
    {
      this.glog = in_log.with("gl");
      this.example_window = NullCheck.notNull(in_window, "Window");
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

        assert this.cube_cache != null;
        assert this.mesh_cache != null;
        assert this.texture2d_cache != null;
        assert this.caps != null;

        this.example.exampleScene(new ExampleSceneBuilder(
          scene_builder,
          this.caps,
          this.cube_cache,
          this.mesh_cache,
          this.texture2d_cache));

        final JCGLImplementationType g = this.gi;
        assert g != null;

        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.colorBufferClear4f(0.0f, 0.0f, 1.0f, 1.0f);

        final ExampleRendererType r = this.renderer;
        assert r != null;

        r.rendererAccept(new ExampleRendererVisitorType<Unit>() {
          @Override public Unit visitDebug(
            final ExampleRendererDebugType rd)
            throws RException
          {
            try {
              final KRendererDebugType dr = rd.rendererGetDebug();
              final KFramebufferForwardType fb =
                (KFramebufferForwardType) VExampleWindowGL.this.framebuffer;
              assert fb != null;

              final KScene sc = scene_builder.sceneCreate();

              dr.rendererDebugEvaluate(fb, sc);
              VExampleWindowGL.this.renderSceneResults(fb);

              if (VExampleWindowGL.this.want_save.get()) {
                VExampleWindowGL.this.want_save.set(false);
                VExampleWindowGL.this.saveImage(drawable, view_index);
              }

              return Unit.unit();
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }

          @Override public Unit visitDeferred(
            final ExampleRendererDeferredType rd)
            throws RException
          {
            try {
              final KRendererDeferredType dr = rd.rendererGetDeferred();
              final KFramebufferDeferredType fb =
                (KFramebufferDeferredType) VExampleWindowGL.this.framebuffer;
              assert fb != null;

              final KScene sc = scene_builder.sceneCreate();
              final KSceneBatchedDeferred batched =
                KSceneBatchedDeferred.fromScene(sc);

              dr.rendererDeferredEvaluate(fb, batched);
              VExampleWindowGL.this.renderSceneResults(fb);

              if (VExampleWindowGL.this.want_save.get()) {
                VExampleWindowGL.this.want_save.set(false);
                VExampleWindowGL.this.saveImage(drawable, view_index);
              }

              return Unit.unit();
            } catch (final JCGLException e) {
              throw RExceptionJCGL.fromJCGLException(e);
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }

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
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
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

    public ExampleRendererType getRenderer()
    {
      assert this.running;
      final ExampleRendererType r = this.renderer;
      assert r != null;
      return r;
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

        this.caps = KGraphicsCapabilities.getCapabilities(g);

        this.texture_loader =
          TextureLoaderImageIO
            .newTextureLoaderWithAlphaPremultiplication(this.glog);

        this.cube_cache =
          new ETextureCubeCache(g, this.texture_loader, this.glog);
        this.texture2d_cache =
          new ETexture2DCache(g, this.texture_loader, this.glog);
        this.mesh_cache = new EMeshCache(g, this.glog);

        this.shader_caches =
          VShaderCaches.newCachesFromArchives(g, this.config, this.glog);

        final JCGLInterfaceCommonType gc = g.getGLCommon();
        this.quad = KUnitQuad.newQuad(gc, this.glog);

        final VShaderCaches sc = this.shader_caches;
        assert sc != null;

        final ExampleRendererType r =
          this.renderer_cons
            .matchConstructor(new ExampleRendererConstructorVisitorType<ExampleRendererType, RException>() {
              @Override public ExampleRendererType debug(
                final ExampleRendererConstructorDebugType c)
                throws RException,
                  JCGLException
              {
                return c.newRenderer(
                  VExampleWindowGL.this.glog,
                  sc.getShaderDepthCache(),
                  sc.getShaderDebugCache(),
                  g);
              }

              @Override public ExampleRendererType deferred(
                final ExampleRendererConstructorDeferredType c)
                throws RException,
                  JCGLException
              {
                return c.newRenderer(
                  VExampleWindowGL.this.glog,
                  sc.getShaderDebugCache(),
                  sc.getShaderForwardTranslucentLitCache(),
                  sc.getShaderForwardTranslucentUnlitCache(),
                  sc.getShaderForwardOpaqueUnlitCache(),
                  sc.getShaderDepthCache(),
                  sc.getShaderDepthVarianceCache(),
                  sc.getShaderPostprocessingCache(),
                  sc.getShaderDeferredGeoCache(),
                  sc.getShaderDeferredLightCache(),
                  sc.getShaderDeferredLightTranslucentCache(),
                  g);
              }

              @Override public ExampleRendererType forward(
                final ExampleRendererConstructorForwardType c)
                throws RException,
                  JCGLException
              {
                return c.newRenderer(
                  VExampleWindowGL.this.glog,
                  sc.getShaderForwardOpaqueLitCache(),
                  sc.getShaderForwardOpaqueUnlitCache(),
                  sc.getShaderForwardTranslucentLitCache(),
                  sc.getShaderForwardTranslucentUnlitCache(),
                  sc.getShaderDepthCache(),
                  sc.getShaderDepthVarianceCache(),
                  sc.getShaderPostprocessingCache(),
                  g);
              }
            });

        this.renderer = r;
        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run()
          {
            try {
              VExampleWindowGL.this.example_window.rendererInitialized(r
                .rendererGetActualClass());
            } catch (final Exception e) {
              throw new UnreachableCodeException(e);
            }
          }
        });

        SwingUtilities.invokeLater(new Runnable() {
          @Override public void run()
          {
            VExampleWindowGL.this.renderer_label.setText(r.rendererGetName());
          }
        });

        this.framebuffer =
          r
            .rendererAccept(new ExampleRendererVisitorType<KFramebufferType>() {
              private AreaInclusive makeArea()
              {
                final RangeInclusiveL range_x =
                  new RangeInclusiveL(0, drawable.getWidth() - 1);
                final RangeInclusiveL range_y =
                  new RangeInclusiveL(0, drawable.getHeight() - 1);
                final AreaInclusive area =
                  new AreaInclusive(range_x, range_y);
                return area;
              }

              private KFramebufferType makeForward()
                throws RException
              {
                final AreaInclusive area = this.makeArea();

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

              @Override public KFramebufferType visitDebug(
                final ExampleRendererDebugType rd)
                throws RException
              {
                return this.makeForward();
              }

              @Override public KFramebufferType visitDeferred(
                final ExampleRendererDeferredType rd)
                throws RException
              {
                final AreaInclusive area = this.makeArea();

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

                return KFramebufferDeferred.newFramebuffer(
                  g,
                  KFramebufferForwardDescription.newDescription(
                    rgba_description,
                    depth_description));
              }

              @Override public KFramebufferType visitForward(
                final ExampleRendererForwardType rf)
                throws RException
              {
                return this.makeForward();
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
        RException,
        JCacheException
    {
      final JCGLImplementationType g = this.gi;
      assert g != null;
      final JCGLInterfaceCommonType gc = g.getGLCommon();

      final VShaderCaches scs = this.shader_caches;
      assert scs != null;
      final KShaderCachePostprocessingType sc =
        scs.getShaderPostprocessingCache();
      assert sc != null;
      final KProgramType kp = sc.cacheGetLU("copy_rgba");
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
  private final ExampleImages<BufferedImage>      example_images;
  private final Class<? extends ExampleSceneType> example_type;
  private final LogUsableType                     log;
  private final ExampleRendererConstructorType    renderer_cons;
  private @Nullable VExampleWindow                window;

  VExample(
    final LogUsableType in_log,
    final ViewerConfig in_config,
    final ExampleImages<BufferedImage> in_example_images,
    final ExampleRendererConstructorType in_renderer_cons,
    final Class<? extends ExampleSceneType> in_example_type)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.example_type = in_example_type;
    this.example_images = in_example_images;
    this.renderer_cons = in_renderer_cons;
    this.config = in_config;
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
        this.example_images,
        this.renderer_cons,
        this.example);
    assert w != null;

    w.pack();
    w.setVisible(true);
    this.window = w;

    return Unit.unit();
  }

}
