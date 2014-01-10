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
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferDrawBuffer;
import com.io7m.jcanephora.FramebufferReference;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.JCGLException;
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
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

public abstract class KShadowMap implements KShadowMapType
{
  private static final class KShadowMapGL2 extends KShadowMap
  {
    public static KShadowMapGL2 newFramebuffer(
      final @Nonnull JCGLInterfaceGL2 gl,
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMinification min,
      final @Nonnull TextureFilterMagnification mag)
      throws JCGLException,
        ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA8(
          "shadow-light-rgba8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Renderbuffer<RenderableDepth> r =
        gl.renderbufferAllocateDepth24(width, height);

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
        gl.framebufferDrawAttachDepthRenderbuffer(fb, r);
        gl.framebufferDrawSetBuffers(fb, mappings);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KShadowMapGL2(c, r, fb);
    }

    private KShadowMapGL2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Renderbuffer<RenderableDepth> r,
      final @Nonnull FramebufferReference fb)
    {
      super(c, fb, r);
    }
  }

  private static long makeSize(
    final @Nonnull Texture2DStatic c,
    final @Nonnull Renderbuffer<RenderableDepth> r,
    final @Nonnull FramebufferReference fb)
  {
    final long cw = c.getWidth();
    final long ch = c.getHeight();
    final long cb = c.getType().getBytesPerPixel();
    final long cs = cw * ch * cb;

    final long rw = r.getWidth();
    final long rh = r.getHeight();
    final long rb = r.getType().getBytesPerPixel();
    final long rs = rw * rh * rb;

    return cs + rs;
  }

  private static final class KShadowMapGL3ES3 extends KShadowMap
  {
    public static final @Nonnull
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3 & JCGLRenderbuffersGL3ES3>
      KShadowMapGL3ES3
      newFramebuffer(
        final @Nonnull G gl,
        final @Nonnull AreaInclusive area,
        final @Nonnull TextureFilterMinification min,
        final @Nonnull TextureFilterMagnification mag)
        throws JCGLException,
          ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateR8(
          "shadow-light-r8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Renderbuffer<RenderableDepth> r =
        gl.renderbufferAllocateDepth24(width, height);

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
        gl.framebufferDrawAttachDepthRenderbuffer(fb, r);
        gl.framebufferDrawSetBuffers(fb, mappings);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KShadowMapGL3ES3(c, r, fb);
    }

    private KShadowMapGL3ES3(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Renderbuffer<RenderableDepth> r,
      final @Nonnull FramebufferReference fb)
    {
      super(c, fb, r);
    }
  }

  private static final class KShadowMapGLES2 extends KShadowMap
  {
    public static final @Nonnull KShadowMapGLES2 newFramebuffer(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull AreaInclusive area,
      final @Nonnull TextureFilterMinification min,
      final @Nonnull TextureFilterMagnification mag)
      throws JCGLException,
        ConstraintError
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGB565(
          "shadow-light-rgb565",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          min,
          mag);

      final Renderbuffer<RenderableDepth> r =
        gl.renderbufferAllocateDepth16(width, height);

      final FramebufferReference fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        gl.framebufferDrawAttachDepthRenderbuffer(fb, r);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KShadowMapGLES2(c, r, fb);
    }

    private KShadowMapGLES2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Renderbuffer<RenderableDepth> r,
      final @Nonnull FramebufferReference fb)
    {
      super(c, fb, r);
    }
  }

  public static @Nonnull KShadowMap newShadowMap(
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
      .implementationAccept(new JCGLImplementationVisitor<KShadowMap, JCGLException>() {
        @Override public KShadowMap implementationIsGL2(
          final @Nonnull JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KShadowMapGL2.newFramebuffer(gl, area, min, mag);
        }

        @Override public KShadowMap implementationIsGL3(
          final @Nonnull JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KShadowMapGL3ES3.newFramebuffer(gl, area, min, mag);
        }

        @Override public KShadowMap implementationIsGLES2(
          final @Nonnull JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KShadowMapGLES2.newFramebuffer(gl, area, min, mag);
        }

        @Override public KShadowMap implementationIsGLES3(
          final @Nonnull JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError,
            JCGLException
        {
          return KShadowMapGL3ES3.newFramebuffer(gl, area, min, mag);
        }
      });
  }

  private final @Nonnull Texture2DStatic               colour;
  private boolean                                      deleted;
  private final @Nonnull FramebufferReference          framebuffer;
  private final @Nonnull Renderbuffer<RenderableDepth> renderbuffer;
  private final long                                   size;

  protected KShadowMap(
    final @Nonnull Texture2DStatic colour,
    final @Nonnull FramebufferReference framebuffer,
    final @Nonnull Renderbuffer<RenderableDepth> renderbuffer)
  {
    this.colour = colour;
    this.framebuffer = framebuffer;
    this.renderbuffer = renderbuffer;
    this.deleted = false;
    this.size = KShadowMap.makeSize(colour, renderbuffer, framebuffer);
  }

  @Override public void kFramebufferDelete(
    final @Nonnull JCGLImplementation g)
    throws JCGLException,
      ConstraintError
  {
    try {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      gc.texture2DStaticDelete(this.colour);
      gc.renderbufferDelete(this.renderbuffer);
    } finally {
      this.setDeleted(true);
    }
  }

  @Override public final @Nonnull AreaInclusive kFramebufferGetArea()
  {
    return this.colour.getArea();
  }

  @Override public final @Nonnull
    FramebufferReferenceUsable
    kFramebufferGetShadowFramebuffer()
  {
    return this.framebuffer;
  }

  @Override public long kFramebufferGetShadowSizeBytes()
  {
    return this.size;
  }

  @Override public final @Nonnull
    Texture2DStaticUsable
    kFramebufferGetShadowTexture()
  {
    return this.colour;
  }

  @Override public final boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  private void setDeleted(
    final boolean deleted)
  {
    this.deleted = deleted;
  }
}
