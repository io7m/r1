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
import com.io7m.jcanephora.api.JCGLRenderbuffersGL3ES3Type;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RExceptionNotSupported;

/**
 * Provides the base implementation for {@link KFramebufferForward}.
 */

@EqualityReference abstract class KFramebufferForwardAbstract implements
  KFramebufferForwardType
{
  @EqualityReference private static final class KFramebufferForwardGL3ES3 extends
    KFramebufferForwardAbstract
  {
    private static Texture2DStaticType makeDepth(
      final KFramebufferDepthDescription desc_depth,
      final int width,
      final int height,
      final JCGLTextures2DStaticGL3ES3Type gl)
      throws JCGLException
    {
      switch (desc_depth.getDepthPrecision()) {
        case DEPTH_PRECISION_16:
        {
          return gl.texture2DStaticAllocateDepth16(
            "depth-16",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_depth.getFilterMinification(),
            desc_depth.getFilterMagnification());
        }
        case DEPTH_PRECISION_24:
        {
          return gl.texture2DStaticAllocateDepth24(
            "depth-24",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_depth.getFilterMinification(),
            desc_depth.getFilterMagnification());
        }
        case DEPTH_PRECISION_32F:
        {
          return gl.texture2DStaticAllocateDepth32f(
            "depth-32f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_depth.getFilterMinification(),
            desc_depth.getFilterMagnification());
        }
      }

      throw new UnreachableCodeException();
    }

    private static Texture2DStaticType makeRGBA(
      final KFramebufferRGBADescription desc_rgba,
      final int width,
      final int height,
      final JCGLTextures2DStaticGL3ES3Type gl)
      throws JCGLException
    {
      switch (desc_rgba.getRGBAPrecision()) {
        case RGBA_PRECISION_16F:
        {
          return gl.texture2DStaticAllocateRGBA16f(
            "color-16f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_rgba.getFilterMinification(),
            desc_rgba.getFilterMagnification());
        }
        case RGBA_PRECISION_32F:
        {
          return gl.texture2DStaticAllocateRGBA32f(
            "color-32f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_rgba.getFilterMinification(),
            desc_rgba.getFilterMagnification());
        }
        case RGBA_PRECISION_8:
        {
          return gl.texture2DStaticAllocateRGBA8(
            "color-8888",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            desc_rgba.getFilterMinification(),
            desc_rgba.getFilterMagnification());
        }
      }

      throw new UnreachableCodeException();
    }

    public static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type & JCGLRenderbuffersGL3ES3Type>
      KFramebufferForwardType
      newFramebuffer(
        final G gl,
        final KFramebufferForwardDescription description)
        throws JCGLException
    {
      final KFramebufferRGBADescription desc_rgba =
        description.getRGBADescription();
      final KFramebufferDepthDescription desc_depth =
        description.getDepthDescription();
      final AreaInclusive area = desc_rgba.getArea();
      assert area.equals(desc_depth.getArea());
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType c =
        KFramebufferForwardGL3ES3.makeRGBA(desc_rgba, width, height, gl);
      final Texture2DStaticType d =
        KFramebufferForwardGL3ES3.makeDepth(desc_depth, width, height, gl);

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
        gl.framebufferDrawAttachDepthTexture2D(fb, d);
        gl.framebufferDrawSetBuffers(fb, mappings);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGL3ES3(c, d, fb, description);
    }

    private final Texture2DStaticType            color;
    private final Texture2DStaticType            depth;
    private final KFramebufferForwardDescription description;
    private final FramebufferType                framebuffer;

    public KFramebufferForwardGL3ES3(
      final Texture2DStaticType c,
      final Texture2DStaticType d,
      final FramebufferType fb,
      final KFramebufferForwardDescription in_description)
    {
      super(c.textureGetArea(), c.resourceGetSizeBytes()
        + d.resourceGetSizeBytes());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
      this.description = in_description;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLException e) {
        throw RExceptionJCGL.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
    }

    @Override public
      FramebufferUsableType
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public Texture2DStaticUsableType kFramebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public
      KFramebufferForwardDescription
      kFramebufferGetForwardDescription()
    {
      return this.description;
    }

    @Override public
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description.getRGBADescription();
    }

    @Override public Texture2DStaticUsableType kFramebufferGetRGBATexture()
    {
      return this.color;
    }
  }

  static KFramebufferForwardType newFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferForwardDescription description)
    throws JCGLException,
      RException
  {
    NullCheck.notNull(gi, "GL implementation");
    NullCheck.notNull(description, "Description");

    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferForwardType, RException>() {
        @Override public KFramebufferForwardType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public KFramebufferForwardType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return KFramebufferForwardAbstract.KFramebufferForwardGL3ES3
            .newFramebuffer(gl, description);
        }

        @Override public KFramebufferForwardType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public KFramebufferForwardType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return KFramebufferForwardAbstract.KFramebufferForwardGL3ES3
            .newFramebuffer(gl, description);
        }
      });
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferForwardAbstract(
    final AreaInclusive in_area,
    final long in_size)
  {
    this.deleted = false;
    this.area = in_area;
    this.size = in_size;
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

  public final void setDeleted(
    final boolean b)
  {
    this.deleted = b;
  }
}
