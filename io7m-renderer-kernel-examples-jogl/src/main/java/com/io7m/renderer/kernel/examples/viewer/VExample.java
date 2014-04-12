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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
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
import nu.xom.Document;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LRUCacheConfig;
import com.io7m.jcache.LRUCacheTrivial;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementationJOGL;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.renderer.kernel.KFramebufferForward;
import com.io7m.renderer.kernel.KFramebufferForwardType;
import com.io7m.renderer.kernel.KFramebufferType;
import com.io7m.renderer.kernel.KProgram;
import com.io7m.renderer.kernel.KRendererForwardType;
import com.io7m.renderer.kernel.KShaderCache;
import com.io7m.renderer.kernel.KShaderCacheLoader;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KShadingProgramCommon;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.KUnitQuadUsableType;
import com.io7m.renderer.kernel.examples.ExampleData;
import com.io7m.renderer.kernel.examples.ExampleRendererConstructorType;
import com.io7m.renderer.kernel.examples.ExampleRendererForwardType;
import com.io7m.renderer.kernel.examples.ExampleRendererType;
import com.io7m.renderer.kernel.examples.ExampleRendererVisitorType;
import com.io7m.renderer.kernel.examples.ExampleSceneBuilderType;
import com.io7m.renderer.kernel.examples.ExampleSceneType;
import com.io7m.renderer.kernel.examples.ExampleSceneUtilities;
import com.io7m.renderer.kernel.examples.ExampleViewType;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KRGBAPrecision;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.Animator;

final class VExample implements Callable<Unit>
{
  private static final class VExampleWindow extends JFrame
  {
    @SuppressWarnings({ "static-method", "boxing" }) private @Nonnull
      String
      getViewText(
        final int view_index,
        final int max_views)
    {
      return String.format("%d of %d", view_index + 1, max_views);
    }

    private static final long               serialVersionUID;

    static {
      serialVersionUID = 2157518454345881373L;
    }

    private final @Nonnull GLJPanel         canvas;
    private final @Nonnull ViewerConfig     config;
    private final @Nonnull AtomicInteger    current_view_index;
    private final ExampleSceneType          example;
    private final @Nonnull VExampleWindowGL gl_events;
    private final @Nonnull JLabel           label_example;
    private final @Nonnull JLabel           label_renderer;
    private final @Nonnull JLabel           label_view;
    private final @Nonnull Log              log;
    private final @Nonnull VExpectedImage   expected;
    private final @Nonnull AtomicBoolean    want_save;
    private final JLabel                    expected_label;
    private final JLabel                    canvas_label;

    @SuppressWarnings("synthetic-access") public VExampleWindow(
      final @Nonnull Log in_log,
      final @Nonnull ViewerConfig in_config,
      final @Nonnull FSCapabilityRead in_filesystem,
      final @Nonnull ExampleRendererConstructorType in_renderer_cons,
      final @Nonnull ExampleSceneType in_example)
    {
      this.log = new Log(in_log, "window");
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
          final @Nonnull ActionEvent e)
        {
          VExampleWindow.this.log.debug("Previous view requested");
          VExampleWindow.this.exampleViewPrevious();
        }
      });

      final JButton next = new JButton("Next view");
      next.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          VExampleWindow.this.log.debug("Next view requested");
          VExampleWindow.this.exampleViewNext();
        }
      });

      final JButton save = new JButton("Save view");
      save.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
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

      final Container pane = this.getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(top_panel);
      pane.add(controls);

      final Animator animator = new Animator(this.canvas);
      animator.start();
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
  }

  private static final class VExampleWindowGL implements GLEventListener
  {
    private static void drawQuad(
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull KUnitQuadUsableType quad,
      final @Nonnull JCBProgram p)
      throws JCGLRuntimeException,
        ConstraintError,
        JCGLException,
        JCBExecutionException
    {
      final ArrayBufferUsable array = quad.getArray();
      final IndexBufferUsable indices = quad.getIndices();

      gc.arrayBufferBind(array);

      try {
        KShadingProgramCommon.bindAttributePosition(p, array);
        KShadingProgramCommon.bindAttributeUV(p, array);
        KShadingProgramCommon.putMatrixUV(
          p,
          ExampleSceneUtilities.IDENTITY_UV_M);

        p.programExecute(new JCBProgramProcedure() {
          @Override public void call()
            throws ConstraintError,
              JCGLException,
              Exception
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          }
        });

      } finally {
        gc.arrayBufferUnbind();
      }
    }

    private final @Nonnull ViewerConfig                   config;
    private final @Nonnull AtomicInteger                  current_view_index;
    private final ExampleSceneType                        example;
    private final @Nonnull FSCapabilityRead               filesystem;
    private KFramebufferType                              framebuffer;
    private JCGLImplementationJOGL                        gi;
    private final @Nonnull Log                            glog;
    private final @Nonnull Map<String, KMesh>             meshes;
    private KUnitQuad                                     quad;
    private ExampleRendererType                           renderer;
    private final @Nonnull ExampleRendererConstructorType renderer_cons;
    private final @Nonnull VExpectedImage                 results_panel;
    private boolean                                       running;
    private KShaderCacheType                              shader_cache;
    private LRUCacheConfig                                shader_cache_config;
    private final @Nonnull AtomicBoolean                  want_save;
    private final @Nonnull JLabel                         renderer_label;

    public VExampleWindowGL(
      final @Nonnull Log in_log,
      final @Nonnull ViewerConfig in_config,
      final @Nonnull AtomicInteger in_view_id,
      final @Nonnull AtomicBoolean in_want_save,
      final @Nonnull FSCapabilityRead in_filesystem,
      final @Nonnull ExampleSceneType in_example,
      final @Nonnull ExampleRendererConstructorType in_renderer_cons,
      final @Nonnull VExpectedImage in_results_panel,
      final @Nonnull JLabel in_renderer_label)
    {
      this.glog = new Log(in_log, "gl");
      this.meshes = new HashMap<String, KMesh>();
      this.filesystem = in_filesystem;
      this.renderer_cons = in_renderer_cons;
      this.example = in_example;
      this.current_view_index = in_view_id;
      this.config = in_config;
      this.want_save = in_want_save;
      this.results_panel = in_results_panel;
      this.renderer_label = in_renderer_label;
    }

    @Override public void display(
      final GLAutoDrawable drawable)
    {
      try {
        if (this.running == false) {
          return;
        }

        final List<ExampleViewType> views = this.example.exampleViewpoints();
        assert views != null;
        assert views.size() > 0;

        final int view_index = this.current_view_index.get();
        final ExampleViewType view = views.get(view_index);
        assert view != null;

        final KSceneBuilderWithCreateType scene_builder =
          KScene.newBuilder(view.getCamera());

        this.example.exampleScene(new ExampleSceneBuilderType() {
          @SuppressWarnings("synthetic-access") @Override public
            KMeshReadableType
            mesh(
              final @Nonnull String name)
              throws RException
          {
            return VExampleWindowGL.this.loadMesh(name);
          }

          @Override public void sceneAddInvisibleWithShadow(
            final @Nonnull KLightType light,
            final @Nonnull KInstanceTransformedOpaqueType instance)
            throws ConstraintError
          {
            scene_builder.sceneAddInvisibleWithShadow(light, instance);
          }

          @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
            final @Nonnull KLightType light,
            final @Nonnull KInstanceTransformedOpaqueType instance)
            throws ConstraintError
          {
            scene_builder.sceneAddOpaqueLitVisibleWithoutShadow(
              light,
              instance);
          }

          @Override public void sceneAddOpaqueLitVisibleWithShadow(
            final @Nonnull KLightType light,
            final @Nonnull KInstanceTransformedOpaqueType instance)
            throws ConstraintError
          {
            scene_builder.sceneAddOpaqueLitVisibleWithShadow(light, instance);
          }

          @Override public void sceneAddOpaqueUnlit(
            final @Nonnull KInstanceTransformedOpaqueType instance)
            throws ConstraintError
          {
            scene_builder.sceneAddOpaqueUnlit(instance);
          }

          @Override public void sceneAddTranslucentLit(
            final @Nonnull KInstanceTransformedTranslucentRegular instance,
            final @Nonnull Set<KLightType> lights)
            throws ConstraintError
          {
            scene_builder.sceneAddTranslucentLit(instance, lights);
          }

          @Override public void sceneAddTranslucentUnlit(
            final @Nonnull KInstanceTransformedTranslucentType instance)
            throws ConstraintError
          {
            scene_builder.sceneAddTranslucentUnlit(instance);
          }
        });

        final JCGLInterfaceCommon gc = this.gi.getGLCommon();
        gc.colorBufferClear4f(0.0f, 0.0f, 1.0f, 1.0f);

        this.renderer.rendererAccept(new ExampleRendererVisitorType<Unit>() {
          @SuppressWarnings("synthetic-access") @Override public
            Unit
            visitForward(
              final @Nonnull ExampleRendererForwardType r)
              throws RException,
                ConstraintError
          {
            try {
              final KRendererForwardType fr = r.rendererGetForward();
              final KFramebufferForwardType fb =
                (KFramebufferForwardType) VExampleWindowGL.this.framebuffer;

              fr.rendererForwardEvaluate(fb, scene_builder.sceneCreate());
              VExampleWindowGL.this.renderSceneResults(fb);

              if (VExampleWindowGL.this.want_save.get()) {
                VExampleWindowGL.this.want_save.set(false);
                VExampleWindowGL.this.saveImage(drawable, view_index);
              }

              return Unit.unit();
            } catch (final JCacheException e) {
              throw RException.fromJCacheException(e);
            } catch (final JCGLRuntimeException e) {
              throw RException.fromJCGLException(e);
            }
          }
        });

      } catch (final JCGLException e) {
        this.failed(e);
      } catch (final ConstraintError e) {
        this.failed(e);
      } catch (final RException e) {
        this.failed(e);
      }
    }

    @Override public void dispose(
      final GLAutoDrawable drawable)
    {
      this.glog.debug("dispose");
    }

    private void failed(
      final Throwable e)
    {
      VErrorBox.showErrorLater(this.glog, e);
      this.running = false;
    }

    @Override public void init(
      final GLAutoDrawable drawable)
    {
      try {
        this.glog.debug("init");

        this.running = true;
        drawable.getContext().setSwapInterval(1);

        this.gi =
          JCGLImplementationJOGL.newImplementation(
            drawable.getContext(),
            this.glog);

        this.shader_cache_config =
          LRUCacheConfig
            .empty()
            .withMaximumCapacity(BigInteger.valueOf(1024));
        this.shader_cache =
          KShaderCache
            .wrap(LRUCacheTrivial.newCache(KShaderCacheLoader.newLoader(
              this.gi,
              this.filesystem,
              this.glog), this.shader_cache_config));

        final JCGLInterfaceCommon gc = this.gi.getGLCommon();
        this.quad = KUnitQuad.newQuad(gc, this.glog);

        this.renderer =
          this.renderer_cons.newRenderer(
            this.glog,
            this.shader_cache,
            this.gi);

        SwingUtilities.invokeLater(new Runnable() {
          @SuppressWarnings("synthetic-access") @Override public void run()
          {
            VExampleWindowGL.this.renderer_label
              .setText(VExampleWindowGL.this.renderer.rendererGetName());
          }
        });

        this.results_panel.loadImages(this.renderer);

        this.framebuffer =
          this.renderer
            .rendererAccept(new ExampleRendererVisitorType<KFramebufferType>() {
              @SuppressWarnings("synthetic-access") @Override public
                KFramebufferType
                visitForward(
                  final ExampleRendererForwardType r)
                  throws ConstraintError,
                    RException
              {
                final RangeInclusive range_x =
                  new RangeInclusive(0, drawable.getWidth() - 1);
                final RangeInclusive range_y =
                  new RangeInclusive(0, drawable.getHeight() - 1);
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
                  VExampleWindowGL.this.gi,
                  KFramebufferForwardDescription.newDescription(
                    rgba_description,
                    depth_description));
              }
            });

        gc.colorBufferClear4f(0.0f, 0.0f, 1.0f, 1.0f);

      } catch (final JCGLRuntimeException e) {
        this.failed(e);
      } catch (final JCGLUnsupportedException e) {
        this.failed(e);
      } catch (final ConstraintError e) {
        this.failed(e);
      } catch (final JCGLException e) {
        this.failed(e);
      } catch (final RException e) {
        this.failed(e);
      }
    }

    private @Nonnull KMesh loadMesh(
      final @Nonnull String name)
      throws RException
    {
      if (this.meshes.containsKey(name)) {
        return this.meshes.get(name);
      }

      final StringBuilder message = new StringBuilder();
      message.setLength(0);
      message.append("Loading mesh from ");
      message.append(name);
      this.glog.debug(message.toString());

      final InputStream stream =
        VExample.class.getResourceAsStream(String.format(
          "/com/io7m/renderer/kernel/examples/%s",
          name));

      final JCGLInterfaceCommon gl = this.gi.getGLCommon();

      try {
        final Document document =
          RXMLMeshDocument.parseFromStreamValidating(stream);

        final RXMLMeshParserVBO<JCGLInterfaceCommon> p =
          RXMLMeshParserVBO.parseFromDocument(
            document,
            gl,
            UsageHint.USAGE_STATIC_DRAW);

        final ArrayBuffer array = p.getArrayBuffer();
        final IndexBuffer index = p.getIndexBuffer();
        final RVectorI3F<RSpaceObjectType> lower = p.getBoundsLower();
        final RVectorI3F<RSpaceObjectType> upper = p.getBoundsUpper();
        final KMesh km = KMesh.newMesh(array, index, lower, upper);
        this.meshes.put(name, km);

        message.setLength(0);
        message.append("Loaded mesh ");
        message.append(name);
        this.glog.debug(message.toString());

        message.setLength(0);
        message.append("Mesh lower bound: ");
        message.append(km.getBoundsLower());
        this.glog.debug(message.toString());

        message.setLength(0);
        message.append("Mesh upper bound: ");
        message.append(km.getBoundsUpper());
        this.glog.debug(message.toString());

        return km;
      } catch (final ConstraintError e) {
        throw new UnreachableCodeException();
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      } catch (final IOException e) {
        throw RException.fromIOException(e);
      } finally {
        try {
          stream.close();
        } catch (final IOException e) {
          throw RException.fromIOException(e);
        }
      }
    }

    private void renderSceneResults(
      final @Nonnull KFramebufferForwardType fb)
      throws JCGLRuntimeException,
        RException,
        ConstraintError,
        JCacheException
    {
      final JCGLInterfaceCommon gc = this.gi.getGLCommon();

      final KProgram kp =
        this.shader_cache.cacheGetLU("postprocessing_copy_rgba");
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

        final JCBExecutionAPI e = kp.getExecutable();
        e.execRun(new JCBExecutorProcedure() {
          @SuppressWarnings("synthetic-access") @Override public void call(
            final @Nonnull JCBProgram p)
            throws ConstraintError,
              JCGLException,
              Exception
          {
            final List<TextureUnit> units = gc.textureGetUnits();
            final TextureUnit unit = units.get(0);

            gc.texture2DStaticBind(unit, fb.kFramebufferGetRGBATexture());
            p.programUniformPutTextureUnit("t_image", unit);
            VExampleWindowGL.drawQuad(gc, VExampleWindowGL.this.quad, p);
          }
        });

      } catch (final JCBExecutionException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }
    }

    @SuppressWarnings("boxing") @Override public void reshape(
      final GLAutoDrawable drawable,
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

    @SuppressWarnings("synthetic-access") private void saveImage(
      final @Nonnull GLAutoDrawable drawable,
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

      final File out =
        VExample.getOutputImageName(
          this.config,
          this.example,
          this.renderer,
          view_index);

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
            VExampleWindowGL.this.glog.debug(String.format(
              "image %s written successfully",
              out));
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
    private static final long              serialVersionUID =
                                                              1636894751686016138L;

    private final @Nonnull AtomicInteger   current_view_index;
    private final @Nonnull List<ImageIcon> images;
    private volatile boolean               images_loaded;
    private final @Nonnull Log             rlog;
    private final ExampleSceneType         scene;

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

    public VExpectedImage(
      final @Nonnull Log in_log,
      final @Nonnull ExampleSceneType in_scene,
      final @Nonnull AtomicInteger in_current_view_index)
    {
      this.rlog = new Log(in_log, "results");
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
      final @Nonnull ExampleRendererType actual_renderer)
    {
      final List<ExampleViewType> views = this.scene.exampleViewpoints();

      final SwingWorker<Unit, Unit> worker = new SwingWorker<Unit, Unit>() {
        @SuppressWarnings("synthetic-access") @Override protected
          Unit
          doInBackground()
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

        @SuppressWarnings("synthetic-access") @Override protected void done()
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
  }

  private static final int EXAMPLE_RESULT_IMAGE_HEIGHT;
  private static final int EXAMPLE_RESULT_IMAGE_WIDTH;

  static {
    EXAMPLE_RESULT_IMAGE_HEIGHT = 480;
    EXAMPLE_RESULT_IMAGE_WIDTH = 640;
  }

  @SuppressWarnings("boxing") private static @Nonnull
    File
    getOutputImageName(
      final @Nonnull ViewerConfig config,
      final @Nonnull ExampleSceneType example,
      final @Nonnull ExampleRendererType renderer,
      final int view_index)
  {
    final File f0 = config.getReplacementResultsDirectory();
    final File f1 = new File(f0, example.exampleGetName());
    final File f2 = new File(f1, renderer.rendererGetName());
    final File f3 = new File(f2, String.format("%d.png", view_index));
    return f3;
  }

  private final @Nonnull ViewerConfig                      config;
  private ExampleSceneType                                 example;
  private final @Nonnull Class<? extends ExampleSceneType> example_type;
  private final @Nonnull FSCapabilityRead                  filesystem;
  private final @Nonnull Log                               log;
  private final @Nonnull ExampleRendererConstructorType    renderer_cons;
  private @Nonnull VExampleWindow                          window;

  VExample(
    final @Nonnull Log in_log,
    final @Nonnull ViewerConfig in_config,
    final @Nonnull FSCapabilityRead in_filesystem,
    final @Nonnull ExampleRendererConstructorType in_renderer_cons,
    final @Nonnull Class<? extends ExampleSceneType> in_example_type)
  {
    this.log = new Log(in_log, "example");
    this.example_type = in_example_type;
    this.renderer_cons = in_renderer_cons;
    this.config = in_config;
    this.filesystem = in_filesystem;
  }

  @Override public Unit call()
    throws Exception
  {
    this.log.info(String.format(
      "Running %s",
      this.example_type.getCanonicalName()));

    this.example = this.example_type.newInstance();

    this.window =
      new VExampleWindow(
        this.log,
        this.config,
        this.filesystem,
        this.renderer_cons,
        this.example);
    this.window.pack();
    this.window.setVisible(true);

    return Unit.unit();
  }

}
