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
import java.util.Map;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPointType;
import com.io7m.jcanephora.FramebufferDrawBufferType;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.FramebufferType;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

abstract class KFramebufferRGBAAbstract implements KFramebufferRGBAType
{
  private static final class KFramebufferRGBA_GL2 extends
    KFramebufferRGBAAbstract
  {
    public static KFramebufferRGBAAbstract newRGBA(
      final JCGLInterfaceGL2Type gl,
      final KFramebufferRGBADescription desc)
      throws JCGLException
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType c =
        gl.texture2DStaticAllocateRGBA8(
          "color-8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc.getFilterMinification(),
          desc.getFilterMagnification());

      final List<FramebufferColorAttachmentPointType> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBufferType> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> mappings =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferType fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GL2(c, fb, desc);
    }

    private final Texture2DStaticType         color;
    private final KFramebufferRGBADescription description;
    private final FramebufferType             framebuffer;

    private KFramebufferRGBA_GL2(
      final Texture2DStaticType c,
      final FramebufferType fb,
      final KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public boolean equals(
      final @Nullable Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KFramebufferRGBA_GL2 other = (KFramebufferRGBA_GL2) obj;
      return this.color.equals(other.color)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + this.framebuffer.hashCode();
      return result;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsableType kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferRGBA_GL3ES3 extends
    KFramebufferRGBAAbstract
  {

    public static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type>
      KFramebufferRGBAAbstract
      newRGBA(
        final G gl,
        final KFramebufferRGBADescription desc)
        throws JCGLException
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      Texture2DStaticType c = null;
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

      final List<FramebufferColorAttachmentPointType> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBufferType> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> mappings =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferType fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GL3ES3(c, fb, desc);
    }

    private final Texture2DStaticType         color;
    private final KFramebufferRGBADescription description;
    private final FramebufferType             framebuffer;

    private KFramebufferRGBA_GL3ES3(
      final Texture2DStaticType c,
      final FramebufferType fb,
      final KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public boolean equals(
      final @Nullable Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KFramebufferRGBA_GL3ES3 other = (KFramebufferRGBA_GL3ES3) obj;
      return this.color.equals(other.color)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + this.framebuffer.hashCode();
      return result;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsableType kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  private static final class KFramebufferRGBA_GLES2 extends
    KFramebufferRGBAAbstract
  {

    public static KFramebufferRGBA_GLES2 newRGBA(
      final JCGLInterfaceGLES2Type gl,
      final KFramebufferRGBADescription desc)
      throws JCGLException
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc.getFilterMinification(),
          desc.getFilterMagnification());

      final List<FramebufferColorAttachmentPointType> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBufferType> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> mappings =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferType fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBA_GLES2(c, fb, desc);
    }

    private final Texture2DStaticType         color;
    private final KFramebufferRGBADescription description;
    private final FramebufferType             framebuffer;

    private KFramebufferRGBA_GLES2(
      final Texture2DStaticType c,
      final FramebufferType fb,
      final KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
    }

    @Override public boolean equals(
      final @Nullable Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KFramebufferRGBA_GLES2 other = (KFramebufferRGBA_GLES2) obj;
      return this.color.equals(other.color)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + this.framebuffer.hashCode();
      return result;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsableType kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  static KFramebufferRGBAAbstract newRGBA(
    final JCGLImplementationType gi,
    final KFramebufferRGBADescription desc)
    throws JCGLException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferRGBAAbstract, UnreachableCodeException>() {
        @Override public KFramebufferRGBAAbstract implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException
        {
          return KFramebufferRGBA_GL2.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBAAbstract implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return KFramebufferRGBA_GL3ES3.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBAAbstract implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException
        {
          return KFramebufferRGBA_GLES2.newRGBA(gl, desc);
        }

        @Override public KFramebufferRGBAAbstract implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return KFramebufferRGBA_GL3ES3.newRGBA(gl, desc);
        }
      });
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferRGBAAbstract(
    final AreaInclusive in_area,
    final long in_size)
  {
    this.area = in_area;
    this.size = in_size;
    this.deleted = false;
  }

  @Override public final AreaInclusive kFramebufferGetArea()
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
