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

package com.io7m.r1.kernel;

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
import com.io7m.jcanephora.RenderableDepthStencilKind;
import com.io7m.jcanephora.RenderbufferType;
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
import com.io7m.jcanephora.api.JCGLRenderbuffersGL3ES3Type;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionNotSupported;

@EqualityReference abstract class KFramebufferRGBAWithDepthAbstract implements
  KFramebufferRGBAWithDepthType
{
  @EqualityReference private static final class KFramebufferRGBAWithDepth_GL2 extends
    KFramebufferRGBAWithDepthAbstract
  {
    public static KFramebufferRGBAWithDepthAbstract newRGBA(
      final JCGLInterfaceGL2Type gl,
      final KFramebufferRGBADescription desc)
      throws JCGLException
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final RenderbufferType<RenderableDepthStencilKind> d =
        gl.renderbufferAllocateDepth24Stencil8(width, height);

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
        gl.framebufferDrawAttachDepthStencilRenderbuffer(fb, d);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBAWithDepth_GL2(c, fb, desc);
    }

    private final Texture2DStaticType         color;
    private final KFramebufferRGBADescription description;
    private final FramebufferType             framebuffer;

    private KFramebufferRGBAWithDepth_GL2(
      final Texture2DStaticType c,
      final FramebufferType fb,
      final KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
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
        throw RExceptionJCGL.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType rgbaGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public KFramebufferRGBADescription rgbaGetDescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsableType rgbaGetTexture()
    {
      return this.color;
    }
  }

  @EqualityReference private static final class KFramebufferRGBAWithDepth_GL3ES3 extends
    KFramebufferRGBAWithDepthAbstract
  {

    public static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type & JCGLRenderbuffersGL3ES3Type>
      KFramebufferRGBAWithDepthAbstract
      newRGBA(
        final G gl,
        final KFramebufferRGBADescription desc)
        throws JCGLException
    {
      final AreaInclusive area = desc.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final RenderbufferType<RenderableDepthStencilKind> d =
        gl.renderbufferAllocateDepth24Stencil8(width, height);

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
        gl.framebufferDrawAttachDepthStencilRenderbuffer(fb, d);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferRGBAWithDepth_GL3ES3(c, fb, desc);
    }

    private final Texture2DStaticType         color;
    private final KFramebufferRGBADescription description;
    private final FramebufferType             framebuffer;

    private KFramebufferRGBAWithDepth_GL3ES3(
      final Texture2DStaticType c,
      final FramebufferType fb,
      final KFramebufferRGBADescription desc)
    {
      super(desc.getArea(), c.resourceGetSizeBytes());
      this.color = c;
      this.framebuffer = fb;
      this.description = desc;
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
        throw RExceptionJCGL.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType rgbaGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public KFramebufferRGBADescription rgbaGetDescription()
    {
      return this.description;
    }

    @Override public Texture2DStaticUsableType rgbaGetTexture()
    {
      return this.color;
    }
  }

  static KFramebufferRGBAWithDepthAbstract newRGBA(
    final JCGLImplementationType gi,
    final KFramebufferRGBADescription desc)
    throws JCGLException,
      RException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferRGBAWithDepthAbstract, RException>() {
        @Override public
          KFramebufferRGBAWithDepthAbstract
          implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
            throws JCGLException
        {
          return KFramebufferRGBAWithDepth_GL2.newRGBA(gl, desc);
        }

        @Override public
          KFramebufferRGBAWithDepthAbstract
          implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
            throws JCGLException
        {
          return KFramebufferRGBAWithDepth_GL3ES3.newRGBA(gl, desc);
        }

        @Override public
          KFramebufferRGBAWithDepthAbstract
          implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
            throws JCGLException,
              RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public
          KFramebufferRGBAWithDepthAbstract
          implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
            throws JCGLException
        {
          return KFramebufferRGBAWithDepth_GL3ES3.newRGBA(gl, desc);
        }
      });
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferRGBAWithDepthAbstract(
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
