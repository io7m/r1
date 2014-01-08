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

package com.io7m.renderer.kernel;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferDrawBuffer;
import com.io7m.jcanephora.FramebufferReference;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExtensionESDepthTexture;
import com.io7m.jcanephora.JCGLFramebuffersGL3;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLRenderbuffersGL3ES3;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.JCGLVersion;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureTypeMeta;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

/**
 * <p>
 * A framebuffer configuration suitable for forward rendering, with a depth
 * buffer that can be sampled (for shadow mapping and similar techniques).
 * </p>
 * <p>
 * Implementation note: On platforms that support depth textures, a single
 * framebuffer will be allocated with an RGBA texture color attachment, and a
 * depth texture depth attachment. In this case,
 * {@link #kfGetColorFramebuffer()} == {@link #kfGetDepthPassFramebuffer()}.
 * On platforms that do not support depth textures, two framebuffers
 * <tt>F0</tt> and <tt>F1</tt> will be allocated. <tt>F0</tt> will consist of
 * an RGBA texture color attachment and a depth renderbuffer <tt>R</tt>, and
 * <tt>F1</tt> will consist of an RGBA texture to which packed depth values
 * will be encoded, and the same depth renderbuffer <tt>R</tt>.
 * </p>
 */

public abstract class KFramebufferForward implements KFramebufferForwardType
{
  private static final class KFramebufferForwardGL2 extends
    KFramebufferForward
  {
    public static KFramebufferForwardType newFramebuffer(
      final @Nonnull JCGLInterfaceGL2 gl,
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMinification min,
      final @Nonnull TextureFilterMagnification mag,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA8(
          "color-8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Texture2DStatic d =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final List<FramebufferColorAttachmentPoint> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        gl.framebufferGetDrawBuffers();

      final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
        new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferReference fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        gl.framebufferDrawAttachDepthTexture2D(fb, d);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGL2(c, d, fb, version);
    }

    private final @Nonnull Texture2DStatic      color;
    private final @Nonnull Texture2DStatic      depth;
    private final @Nonnull FramebufferReference framebuffer;

    public KFramebufferForwardGL2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull JCGLVersion version)
    {
      assert TextureTypeMeta.isColourRenderable(c.getType(), version);
      assert TextureTypeMeta.isDepthRenderable(d.getType());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull AreaInclusive kFramebufferGetArea()
    {
      return this.color.getArea();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferForwardGL3ES3 extends
    KFramebufferForward
  {
    public static @Nonnull
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3 & JCGLRenderbuffersGL3ES3>
      KFramebufferForwardType
      newFramebuffer(
        final @Nonnull G gl,
        final @Nonnull AreaInclusive area,
        final @Nonnull TextureFilterMinification min,
        final @Nonnull TextureFilterMagnification mag,
        final @Nonnull JCGLVersion version)
        throws JCGLException,
          ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA8(
          "color-8888",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Texture2DStatic d =
        gl.texture2DStaticAllocateDepth24(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final List<FramebufferColorAttachmentPoint> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        gl.framebufferGetDrawBuffers();

      final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
        new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferReference fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        gl.framebufferDrawAttachDepthTexture2D(fb, d);
        gl.framebufferDrawSetBuffers(fb, mappings);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGL3ES3(c, d, fb, version);
    }

    private final @Nonnull Texture2DStatic      color;
    private final @Nonnull Texture2DStatic      depth;
    private final @Nonnull FramebufferReference framebuffer;

    public KFramebufferForwardGL3ES3(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull JCGLVersion version)
    {
      assert TextureTypeMeta.isColourRenderable(c.getType(), version);
      assert TextureTypeMeta.isDepthRenderable(d.getType());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull AreaInclusive kFramebufferGetArea()
    {
      return this.color.getArea();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferForwardGLES2WithDepthTexture extends
    KFramebufferForward
  {
    public static @Nonnull KFramebufferForwardType newFramebuffer(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMinification min,
      final @Nonnull TextureFilterMagnification mag,
      final @Nonnull JCGLExtensionESDepthTexture depth,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Texture2DStatic d =
        depth.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final FramebufferReference fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        gl.framebufferDrawAttachDepthTexture2D(fb, d);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGLES2WithDepthTexture(c, d, fb, version);
    }

    private final @Nonnull Texture2DStatic      color;
    private final @Nonnull Texture2DStatic      depth;
    private final @Nonnull FramebufferReference framebuffer;

    public KFramebufferForwardGLES2WithDepthTexture(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull JCGLVersion version)
    {
      assert TextureTypeMeta.isColourRenderable(c.getType(), version);
      assert TextureTypeMeta.isDepthRenderable(d.getType());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull AreaInclusive kFramebufferGetArea()
    {
      return this.color.getArea();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferForwardGLES2WithoutDepthTexture extends
    KFramebufferForward
  {
    public static @Nonnull KFramebufferForwardType newFramebuffer(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMinification min,
      final @Nonnull TextureFilterMagnification mag,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Texture2DStatic dt =
        gl.texture2DStaticAllocateRGBA4444(
          "color-depth-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Renderbuffer<RenderableDepth> dr =
        gl.renderbufferAllocateDepth16(width, height);

      final FramebufferReference fb_depth = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb_depth);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb_depth, dt);
        gl.framebufferDrawAttachDepthRenderbuffer(fb_depth, dr);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb_depth);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      final FramebufferReference fb_color = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb_color);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb_color, c);
        gl.framebufferDrawAttachDepthRenderbuffer(fb_color, dr);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb_color);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGLES2WithoutDepthTexture(
        c,
        dt,
        dr,
        fb_depth,
        fb_color,
        version);
    }

    private final @Nonnull Texture2DStatic               color;
    private final @Nonnull Texture2DStatic               depth;
    private final @Nonnull Renderbuffer<RenderableDepth> depth_rb;
    private final @Nonnull FramebufferReference          fb_color;
    private final @Nonnull FramebufferReference          fb_depth;

    private KFramebufferForwardGLES2WithoutDepthTexture(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic dt,
      final @Nonnull Renderbuffer<RenderableDepth> dr,
      final @Nonnull FramebufferReference fb_depth,
      final @Nonnull FramebufferReference fb_color,
      final @Nonnull JCGLVersion version)
    {
      assert TextureTypeMeta.isColourRenderable(c.getType(), version);
      assert TextureTypeMeta.isColourRenderable(dt.getType(), version);
      this.color = c;
      this.depth = dt;
      this.depth_rb = dr;
      this.fb_depth = fb_depth;
      this.fb_color = fb_color;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.fb_color);
        gc.framebufferDelete(this.fb_depth);
        gc.renderbufferDelete(this.depth_rb);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull AreaInclusive kFramebufferGetArea()
    {
      return this.color.getArea();
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.fb_color;
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.fb_depth;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public @Nonnull
      Texture2DStaticUsable
      kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  protected final static void checkFramebufferStatus(
    final @Nonnull FramebufferStatus status)
    throws JCGLUnsupportedException
  {
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
  }

  public static @Nonnull KFramebufferForwardType newFramebuffer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull AreaInclusive area,
    final @Nonnull TextureFilterMinification min,
    final @Nonnull TextureFilterMagnification mag)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(area, "Framebuffer area");
    Constraints.constrainNotNull(min, "Minification filter");
    Constraints.constrainNotNull(mag, "Magnification filter");

    return gi
      .implementationAccept(new JCGLImplementationVisitor<KFramebufferForwardType, JCGLException>() {
        @Override public KFramebufferForwardType implementationIsGL2(
          final @Nonnull JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KFramebufferForwardGL2.newFramebuffer(
            gl,
            area,
            min,
            mag,
            gl.metaGetVersion());
        }

        @Override public KFramebufferForwardType implementationIsGL3(
          final @Nonnull JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KFramebufferForwardGL3ES3.newFramebuffer(
            gl,
            area,
            min,
            mag,
            gl.metaGetVersion());
        }

        @Override public KFramebufferForwardType implementationIsGLES2(
          final @Nonnull JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          final Option<JCGLExtensionESDepthTexture> edt =
            gl.extensionDepthTexture();

          if (edt.isSome()) {
            final Some<JCGLExtensionESDepthTexture> some =
              (Option.Some<JCGLExtensionESDepthTexture>) edt;
            return KFramebufferForwardGLES2WithDepthTexture.newFramebuffer(
              gl,
              area,
              min,
              mag,
              some.value,
              gl.metaGetVersion());
          }

          return KFramebufferForwardGLES2WithoutDepthTexture.newFramebuffer(
            gl,
            area,
            min,
            mag,
            gl.metaGetVersion());
        }

        @Override public KFramebufferForwardType implementationIsGLES3(
          final @Nonnull JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KFramebufferForwardGL3ES3.newFramebuffer(
            gl,
            area,
            min,
            mag,
            gl.metaGetVersion());
        }
      });
  }

  private boolean deleted = false;

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  public void setDeleted(
    final boolean b)
  {
    this.deleted = b;
  }
}
