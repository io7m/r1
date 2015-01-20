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

package com.io7m.r1.examples.viewer_new;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureLoaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.texload.imageio.TextureLoaderImageIO;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.r1.examples.ExampleImageBuilderType;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererType;
import com.io7m.r1.examples.ExampleSceneUtilities;
import com.io7m.r1.examples.ExampleTypeEnum;
import com.io7m.r1.examples.tools.ETexture2DCache;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionIO;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageSinkBlitRGBA;
import com.io7m.r1.kernel.KImageSinkRGBAType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.KTextureBindingsController;
import com.io7m.r1.kernel.KTextureBindingsControllerType;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.main.R1Type;
import com.jogamp.newt.opengl.GLWindow;

/**
 * Basic example runner for image types.
 */

public final class VExampleRunnerImage implements VExampleRunnerImageType
{
  private @Nullable ETexture2DCache                   cache_2d;
  private @Nullable KUnitQuadCacheType                cache_quad;
  private final VExampleConfig                        config;
  private final ExampleImageType                      example;
  private @Nullable KFramebufferDeferredType          framebuffer;
  private @Nullable JCGLImplementationType            gi;
  private @Nullable GL                                gl;
  private @Nullable TextureLoaderType                 loader;
  private final LogUsableType                         log;
  private @Nullable ExampleRendererType               renderer;
  private final ExampleRendererConstructorType        renderer_cons;
  private @Nullable AreaInclusive                     screen_area;
  private @Nullable KShaderCacheSetType               shader_cache;
  private @Nullable KImageSinkRGBAType<AreaInclusive> sink;
  private final AtomicBoolean                         want_save;
  private final @Nullable GLWindow                    window;

  /**
   * Construct an example runner.
   *
   * @param in_config
   *          The configuration
   * @param in_example
   *          The example
   * @param in_renderer_cons
   *          The renderer
   * @param in_window
   *          The window, if one is in use
   * @param in_log
   *          A log interface
   */

  public VExampleRunnerImage(
    final VExampleConfig in_config,
    final ExampleImageType in_example,
    final ExampleRendererConstructorType in_renderer_cons,
    final @Nullable GLWindow in_window,
    final LogUsableType in_log)
  {
    this.example = NullCheck.notNull(in_example, "Example");
    this.renderer_cons = NullCheck.notNull(in_renderer_cons);
    this.log = NullCheck.notNull(in_log);
    this.config = NullCheck.notNull(in_config, "Config");
    this.want_save = new AtomicBoolean(false);
    this.window = in_window;
  }

  @Override public void display(
    final @Nullable GLAutoDrawable drawable)
  {
    assert drawable != null;
    final ETexture2DCache c2d = VExampleRunnerImage.this.cache_2d;
    assert c2d != null;
    final KUnitQuadCacheType qc = this.cache_quad;
    assert qc != null;
    final JCGLImplementationType g = this.gi;
    assert g != null;
    final KShaderCacheSetType sc = this.shader_cache;
    assert sc != null;

    try {
      final KFramebufferDeferredType fb =
        VExampleRunnerImage.this.framebuffer;
      assert fb != null;
      final ExampleRendererType r = this.renderer;
      assert r != null;

      fb.deferredFramebufferClear(
        g.getGLCommon(),
        ExampleSceneUtilities.RGBA_NOTHING,
        1.0f,
        0);

      final ExampleImageBuilderType builder = new ExampleImageBuilderType() {
        @Override public R1Type getR1()
        {
          return r.getR1();
        }

        @Override public Texture2DStaticUsableType texture(
          final String name)
          throws RException
        {
          try {
            return c2d.loadTexture(name);
          } catch (final IOException e) {
            throw RExceptionIO.fromIOException(e);
          }
        }
      };

      this.example.exampleImage(builder, fb);

      assert this.sink != null;
      assert this.screen_area != null;
      this.sink.sinkEvaluateRGBA(this.screen_area, fb);
    } catch (final RException e) {
      throw new RuntimeException(e);
    }

    if (this.want_save.get()) {
      this.doSaveImage();
      this.want_save.set(false);
    }
  }

  @Override public void dispose(
    final @Nullable GLAutoDrawable drawable)
  {
    assert drawable != null;

    this.log.debug("disposing");

    try {
      final JCGLImplementationType g = this.gi;
      if (g != null) {
        if (this.framebuffer != null) {
          this.framebuffer.delete(g);
        }
      }
    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }

  private void doSaveImage()
  {
    final KFramebufferDeferredType fb = this.framebuffer;
    if (fb != null) {
      final AreaInclusive area = fb.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final GL g = this.gl;
      assert g != null;
      final ExampleRendererType r = this.renderer;
      assert r != null;

      VSaveFramebuffer.saveSceneImage(
        this.config,
        g,
        width,
        height,
        ExampleTypeEnum.EXAMPLE_IMAGE,
        this.example.exampleGetName(),
        r.exampleRendererGetName(),
        0,
        this.log);
    }
  }

  @Override public void init(
    final @Nullable GLAutoDrawable drawable)
  {
    try {
      this.log.debug("initializing");

      assert drawable != null;
      final GLContext ctx = drawable.getContext();
      assert ctx != null;
      final JCGLImplementationType g =
        VGLImplementation.makeGLImplementation(ctx, this.log);
      assert g != null;

      this.gl = drawable.getGL();
      this.gi = g;
      this.loader =
        TextureLoaderImageIO
          .newTextureLoaderWithAlphaPremultiplication(this.log);
      this.cache_2d = new ETexture2DCache(g, this.loader, this.log);

      final KUnitQuadCacheType cq =
        KUnitQuadCache.newCache(g.getGLCommon(), this.log);
      this.cache_quad = cq;
      final KShaderCacheSetType sc =
        VShaderCaches.newCachesFromArchives(g, this.log);
      this.shader_cache = sc;

      final KTextureBindingsControllerType tc =
        KTextureBindingsController.newBindings(g.getGLCommon());

      this.renderer = this.renderer_cons.newRenderer(this.log, sc, g);

      this.sink =
        KImageSinkBlitRGBA.newSink(g, tc, sc.getShaderImageCache(), cq);
      this.screen_area =
        new AreaInclusive(
          new RangeInclusiveL(0, drawable.getWidth() - 1),
          new RangeInclusiveL(0, drawable.getHeight() - 1));

    } catch (final RException e) {
      throw new RuntimeException(e);
    } catch (final FilesystemError e) {
      throw new RuntimeException(e);
    }
  }

  @Override public void requestImageSave()
  {
    this.want_save.set(true);
  }

  @Override public void reshape(
    final @Nullable GLAutoDrawable drawable,
    final int x,
    final int y,
    final int width,
    final int height)
  {
    try {
      this.log.debug("reshaping");

      final JCGLImplementationType g = this.gi;
      if (g != null) {
        final KFramebufferDeferredType old_fb = this.framebuffer;
        final KFramebufferDeferredType new_fb =
          VExampleFramebuffer.makeFramebuffer(this.config, g, width, height);
        this.framebuffer = new_fb;

        if (old_fb != null) {
          old_fb.delete(g);
        }
      }

      this.screen_area =
        new AreaInclusive(
          new RangeInclusiveL(0, width - 1),
          new RangeInclusiveL(0, height - 1));

    } catch (final RException e) {
      throw new RuntimeException(e);
    }
  }
}
