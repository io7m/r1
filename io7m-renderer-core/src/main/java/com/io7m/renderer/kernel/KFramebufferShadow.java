/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import java.util.HashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferDrawBuffer;
import com.io7m.jcanephora.FramebufferReference;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExtensionDepthTexture;
import com.io7m.jcanephora.JCGLExtensionESDepthTexture;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureTypeMeta;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

public abstract class KFramebufferShadow implements KFramebuffer
{
  public static final class KFramebufferShadowBasic extends
    KFramebufferShadow
  {
    private final @Nonnull FramebufferReference framebuffer;
    private final @Nonnull Texture2DStatic      texture;
    private final @CheckForNull Renderbuffer<?> renderbuffer;

    @SuppressWarnings("synthetic-access") private KFramebufferShadowBasic(
      final @Nonnull Texture2DStatic texture,
      final @CheckForNull Renderbuffer<?> renderbuffer,
      final @Nonnull FramebufferReference framebuffer)
      throws ConstraintError
    {
      super(KShadow.Type.SHADOW_MAPPED_BASIC);

      this.texture = Constraints.constrainNotNull(texture, "Texture");
      this.renderbuffer = renderbuffer;
      this.framebuffer =
        Constraints.constrainNotNull(framebuffer, "Framebuffer");
    }

    public @Nonnull Texture2DStaticUsable getDepthTexture()
    {
      return this.texture;
    }

    @Override public long getSizeBytes()
    {
      final int w = this.texture.getWidth();
      final int h = this.texture.getHeight();
      final int b = this.texture.getType().getBytesPerPixel();
      return w * h * b;
    }

    @Override public void kframebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      gc.texture2DStaticDelete(this.texture);
      if (this.renderbuffer != null) {
        gc.renderbufferDelete(this.renderbuffer);
      }
    }

    @Override public @Nonnull AreaInclusive kframebufferGetArea()
    {
      return this.texture.getArea();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kframebufferGetFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.framebuffer.resourceIsDeleted();
    }
  }

  public static final class KFramebufferShadowVariance extends
    KFramebufferShadow
  {
    @SuppressWarnings("synthetic-access") private KFramebufferShadowVariance()
      throws ConstraintError
    {
      super(KShadow.Type.SHADOW_MAPPED_VARIANCE);
    }

    @Override public long getSizeBytes()
    {
      throw new UnimplementedCodeException();
    }

    public @Nonnull Texture2DStaticUsable getVarianceTexture()
    {
      throw new UnimplementedCodeException();
    }

    @Override public void kframebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      throw new UnimplementedCodeException();
    }

    @Override public @Nonnull AreaInclusive kframebufferGetArea()
    {
      throw new UnimplementedCodeException();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kframebufferGetFramebuffer()
    {
      throw new UnimplementedCodeException();
    }

    @Override public boolean resourceIsDeleted()
    {
      throw new UnimplementedCodeException();
    }
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KFramebufferShadowBasic
    newBasic(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull AreaInclusive size,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws JCGLUnsupportedException,
        JCGLException,
        ConstraintError
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(size, "Framebuffer size");
    Constraints.constrainNotNull(precision, "Precision");

    final JCGLInterfaceCommon g = gi.getGLCommon();
    final int width = (int) size.getRangeX().getInterval();
    final int height = (int) size.getRangeY().getInterval();

    final Pair<Texture2DStatic, Renderbuffer<RenderableDepth>> pair =
      KFramebufferShadow.newDepthTextureRenderbufferPair(
        gi,
        width,
        height,
        filter,
        precision);
    final Texture2DStatic texture = pair.first;

    final FramebufferReference fb = g.framebufferAllocate();
    g.framebufferDrawBind(fb);

    try {

      /**
       * If the allocated texture is depth renderable, then the shadow depth
       * will be rendered to the depth texture and the nonexistent color
       * buffer will not be written.
       */

      if (TextureTypeMeta.isDepthRenderable(texture.getType())) {
        g.framebufferDrawAttachDepthTexture2D(fb, texture);
        KFramebufferShadow.noDrawBuffer(gi, fb);
      } else {

        /**
         * Otherwise, the implementation does not support depth textures and
         * the shadow depth must be written to the color buffer.
         */

        g.framebufferDrawAttachColorTexture2D(fb, texture);
        g.framebufferDrawAttachDepthRenderbuffer(fb, pair.second);
      }

      final FramebufferStatus status = g.framebufferDrawValidate(fb);
      switch (status) {
        case FRAMEBUFFER_STATUS_COMPLETE:
          break;
        case FRAMEBUFFER_STATUS_ERROR_INCOMPLETE_ATTACHMENT:
        case FRAMEBUFFER_STATUS_ERROR_INCOMPLETE_DRAW_BUFFER:
        case FRAMEBUFFER_STATUS_ERROR_INCOMPLETE_READ_BUFFER:
        case FRAMEBUFFER_STATUS_ERROR_MISSING_IMAGE_ATTACHMENT:
        case FRAMEBUFFER_STATUS_ERROR_UNKNOWN:
        case FRAMEBUFFER_STATUS_ERROR_UNSUPPORTED:
          throw new JCGLUnsupportedException(
            "Could not initialize framebuffer: " + status);
      }

    } finally {
      g.framebufferDrawUnbind();
    }

    return new KFramebufferShadowBasic(pair.first, pair.second, fb);
  }

  private static @Nonnull
    Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
    newDepthTextureRenderbufferPair(
      final @Nonnull JCGLImplementation gi,
      final int width,
      final int height,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws JCGLException,
        ConstraintError
  {
    {
      final Option<JCGLInterfaceGL3> o = gi.getGL3();
      if (o.isSome()) {
        final JCGLInterfaceGL3 g = ((Option.Some<JCGLInterfaceGL3>) o).value;
        return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
          KFramebufferShadow.newDepthTextureGL3ES3(
            width,
            height,
            precision,
            filter,
            g), null);
      }
    }

    {
      final Option<JCGLInterfaceGLES3> o = gi.getGLES3();
      if (o.isSome()) {
        final JCGLInterfaceGLES3 g =
          ((Option.Some<JCGLInterfaceGLES3>) o).value;
        return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
          KFramebufferShadow.newDepthTextureGL3ES3(
            width,
            height,
            precision,
            filter,
            g), null);
      }
    }

    {
      final Option<JCGLInterfaceGL2> o = gi.getGL2();
      if (o.isSome()) {
        final JCGLInterfaceGL2 g = ((Option.Some<JCGLInterfaceGL2>) o).value;
        final Option<JCGLExtensionDepthTexture> e = g.extensionDepthTexture();

        if (e.isSome()) {
          final JCGLExtensionDepthTexture edt =
            ((Option.Some<JCGLExtensionDepthTexture>) e).value;

          switch (precision) {
            case SHADOW_PRECISION_16:
            {
              return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
                edt.texture2DStaticAllocateDepth16(
                  "depth-16",
                  width,
                  height,
                  TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  filter.getMinification(),
                  filter.getMagnification()), null);
            }
            case SHADOW_PRECISION_24:
            case SHADOW_PRECISION_32:
            {
              return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
                edt.texture2DStaticAllocateDepth24(
                  "depth-24",
                  width,
                  height,
                  TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                  filter.getMinification(),
                  filter.getMagnification()), null);
            }
          }

          throw new UnreachableCodeException();
        }
      }
    }

    {
      final Option<JCGLInterfaceGLES2> o = gi.getGLES2();

      if (o.isSome()) {
        final JCGLInterfaceGLES2 g =
          ((Option.Some<JCGLInterfaceGLES2>) o).value;

        final Option<JCGLExtensionESDepthTexture> e =
          g.extensionDepthTexture();

        if (e.isSome()) {
          final JCGLExtensionESDepthTexture edt =
            ((Option.Some<JCGLExtensionESDepthTexture>) e).value;

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            edt.texture2DStaticAllocateDepth16(
              "depth-16",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              filter.getMinification(),
              filter.getMagnification()), null);
        }

        final Renderbuffer<RenderableDepth> rb =
          g.renderbufferAllocateDepth16(width, height);

        final Texture2DStatic t =
          g.texture2DStaticAllocateRGB565(
            "color-565",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            filter.getMinification(),
            filter.getMagnification());

        return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(t, rb);
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull Texture2DStatic newDepthTextureGL3ES3(
    final int width,
    final int height,
    final @Nonnull KShadowPrecision precision,
    final @Nonnull KShadowFilter filter,
    final @Nonnull JCGLTextures2DStaticGL3ES3 g)
    throws ConstraintError,
      JCGLException
  {
    switch (precision) {
      case SHADOW_PRECISION_16:
        return g.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
      case SHADOW_PRECISION_24:
        return g.texture2DStaticAllocateDepth24(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
      case SHADOW_PRECISION_32:
        return g.texture2DStaticAllocateDepth32f(
          "depth-32",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
    }

    throw new UnreachableCodeException();
  }

  public static @Nonnull KFramebufferShadow newVariance(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull AreaInclusive size,
    final @Nonnull KShadowFilter filter,
    final @Nonnull KShadowPrecision precision)
    throws ConstraintError
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(size, "Framebuffer size");
    Constraints.constrainNotNull(precision, "Precision");

    final JCGLInterfaceCommon g = gi.getGLCommon();
    final int width = (int) size.getRangeX().getInterval();
    final int height = (int) size.getRangeY().getInterval();

    throw new UnimplementedCodeException();
  }

  private static void noDrawBuffer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull FramebufferReference fb)
    throws JCGLException,
      ConstraintError
  {
    final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
      new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();

    {
      final Option<JCGLInterfaceGL3> o = gi.getGL3();
      if (o.isSome()) {
        final JCGLInterfaceGL3 g = ((Option.Some<JCGLInterfaceGL3>) o).value;
        g.framebufferDrawSetBuffers(fb, empty);
        return;
      }
    }

    {
      final Option<JCGLInterfaceGLES3> o = gi.getGLES3();
      if (o.isSome()) {
        final JCGLInterfaceGLES3 g =
          ((Option.Some<JCGLInterfaceGLES3>) o).value;
        g.framebufferDrawSetBuffers(fb, empty);
      }
    }

    {
      final Option<JCGLInterfaceGL2> o = gi.getGL2();
      if (o.isSome()) {
        final JCGLInterfaceGL2 g = ((Option.Some<JCGLInterfaceGL2>) o).value;
        g.framebufferDrawSetBuffers(fb, empty);
      }
    }

    {
      final Option<JCGLInterfaceGLES2> o = gi.getGLES2();
      if (o.isSome()) {
        final JCGLInterfaceGLES2 g =
          ((Option.Some<JCGLInterfaceGLES2>) o).value;
        // XXX: ?
      }
    }
  }

  private final @Nonnull KShadow.Type type;

  private KFramebufferShadow(
    final @Nonnull KShadow.Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Shadow type");
  }

  /**
   * Retrieve the size in bytes of the framebuffer. This is the sum of the
   * sizes of all the buffers and textures that make up the framebuffer as a
   * whole.
   */

  abstract public long getSizeBytes();

  public @Nonnull KShadow.Type getType()
  {
    return this.type;
  }
}
