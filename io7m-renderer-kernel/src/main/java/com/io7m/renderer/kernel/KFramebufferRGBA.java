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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

/**
 * A simple RGBA "image-only" framebuffer with no depth or stencil
 * attachments.
 */

abstract class KFramebufferRGBA implements KFramebufferRGBAType
{
  private static final class KFramebufferRGBA_GL2 extends KFramebufferRGBA
  {
    public static KFramebufferRGBA newRGBA(
      final @Nonnull JCGLInterfaceGL2 gl,
      final @Nonnull KFramebufferRGBADescription desc)
      throws JCGLException,
        ConstraintError
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA8(
          "color-8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc.getFilterMinification(),
          desc.getFilterMagnification());

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
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GL2(c, fb, desc);
    }

    private final @Nonnull Texture2DStatic             color;
    private final @Nonnull KFramebufferRGBADescription description;
    private final @Nonnull FramebufferReference        framebuffer;

    private KFramebufferRGBA_GL2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull FramebufferReference fb,
      final @Nonnull KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws ConstraintError,
        RException
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsable kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferRGBA_GL3ES3 extends KFramebufferRGBA
  {
    public static
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3>
      KFramebufferRGBA
      newRGBA(
        final @Nonnull G gl,
        final @Nonnull KFramebufferRGBADescription desc)
        throws JCGLException,
          ConstraintError
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      Texture2DStatic c = null;
      switch (desc.getRGBAPrecision()) {
        case RGBA_PRECISION_16F:
        {
          c =
            gl.texture2DStaticAllocateRGBA16f(
              "color-16f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              desc.getFilterMinification(),
              desc.getFilterMagnification());
          break;
        }
        case RGBA_PRECISION_32F:
        {
          c =
            gl.texture2DStaticAllocateRGBA32f(
              "color-32f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              desc.getFilterMinification(),
              desc.getFilterMagnification());
          break;
        }
        case RGBA_PRECISION_8:
        {
          c =
            gl.texture2DStaticAllocateRGBA8(
              "color-8",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              desc.getFilterMinification(),
              desc.getFilterMagnification());
          break;
        }
      }

      assert c != null;

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
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GL3ES3(c, fb, desc);
    }

    private final @Nonnull Texture2DStatic             color;
    private final @Nonnull KFramebufferRGBADescription description;
    private final @Nonnull FramebufferReference        framebuffer;

    private KFramebufferRGBA_GL3ES3(
      final @Nonnull Texture2DStatic c,
      final @Nonnull FramebufferReference fb,
      final @Nonnull KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws ConstraintError,
        RException
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsable kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferRGBA_GLES2 extends KFramebufferRGBA
  {
    public static KFramebufferRGBA newRGBA(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull KFramebufferRGBADescription desc)
      throws JCGLException,
        ConstraintError
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc.getFilterMinification(),
          desc.getFilterMagnification());

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
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferForward.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GLES2(c, fb, desc);
    }

    private final @Nonnull Texture2DStatic             color;
    private final @Nonnull KFramebufferRGBADescription description;
    private final @Nonnull FramebufferReference        framebuffer;

    private KFramebufferRGBA_GLES2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull FramebufferReference fb,
      final @Nonnull KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws ConstraintError,
        RException
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsable kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  static @Nonnull KFramebufferRGBA newRGBA(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull KFramebufferRGBADescription desc)
    throws JCGLException,
      ConstraintError
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitor<KFramebufferRGBA, ConstraintError>() {
        @Override public KFramebufferRGBA implementationIsGL2(
          final @Nonnull JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError
        {
          return KFramebufferRGBA_GL2.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBA implementationIsGL3(
          final @Nonnull JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError
        {
          return KFramebufferRGBA_GL3ES3.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBA implementationIsGLES2(
          final @Nonnull JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError
        {
          return KFramebufferRGBA_GLES2.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBA implementationIsGLES3(
          final @Nonnull JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError
        {
          return KFramebufferRGBA_GL3ES3.newRGBA(gl, desc);
        }
      });
  }

  private final @Nonnull AreaInclusive area;
  private boolean                      deleted;
  private final long                   size;

  protected KFramebufferRGBA(
    final @Nonnull AreaInclusive in_area,
    final long in_size)
  {
    this.area = in_area;
    this.size = in_size;
    this.deleted = false;
  }

  @Override public final @Nonnull AreaInclusive kFramebufferGetArea()
  {
    return this.area;
  }

  @Override public final long kFramebufferGetSizeBytes()
  {
    return this.size;
  }

  @Override public final boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  protected void setDeleted(
    final boolean in_deleted)
  {
    this.deleted = in_deleted;
  }
}
