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
import com.io7m.jcanephora.RenderableDepthKind;
import com.io7m.jcanephora.RenderbufferType;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLExtensionESDepthTextureType;
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
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * Provides the base implementation for {@link KFramebufferForward}.
 */

abstract class KFramebufferForwardAbstract implements KFramebufferForwardType
{
  private static final class KFramebufferForwardGL2 extends
    KFramebufferForwardAbstract
  {

    public static KFramebufferForwardType newFramebuffer(
      final JCGLInterfaceGL2Type gl,
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
        gl.texture2DStaticAllocateRGBA8(
          "color-8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStaticType d =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

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
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGL2(c, d, fb, description);
    }

    private final Texture2DStaticType            color;
    private final Texture2DStaticType            depth;
    private final KFramebufferForwardDescription description;
    private final FramebufferType                framebuffer;

    public KFramebufferForwardGL2(
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
      final KFramebufferForwardGL2 other = (KFramebufferForwardGL2) obj;
      return this.color.equals(other.color)
        && this.depth.equals(other.depth)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.depth.hashCode();
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

    @Override public boolean kFramebufferGetDepthIsPackedColor()
    {
      return false;
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

  private static final class KFramebufferForwardGL3ES3 extends
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
      final KFramebufferForwardGL3ES3 other = (KFramebufferForwardGL3ES3) obj;
      return this.color.equals(other.color)
        && this.depth.equals(other.depth)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.depth.hashCode();
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

    @Override public boolean kFramebufferGetDepthIsPackedColor()
    {
      return false;
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

  private static final class KFramebufferForwardGLES2WithDepthTexture extends
    KFramebufferForwardAbstract
  {

    public static KFramebufferForwardType newFramebuffer(
      final JCGLInterfaceGLES2Type gl,
      final KFramebufferForwardDescription description,
      final JCGLExtensionESDepthTextureType gldt)
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
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStaticType d =
        gldt.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

      final FramebufferType fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb, c);
        gl.framebufferDrawAttachDepthTexture2D(fb, d);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGLES2WithDepthTexture(
        c,
        d,
        fb,
        description);
    }

    private final Texture2DStaticType            color;
    private final Texture2DStaticType            depth;
    private final KFramebufferForwardDescription description;
    private final FramebufferType                framebuffer;

    public KFramebufferForwardGLES2WithDepthTexture(
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
      final KFramebufferForwardGLES2WithDepthTexture other =
        (KFramebufferForwardGLES2WithDepthTexture) obj;
      return this.color.equals(other.color)
        && this.depth.equals(other.depth)
        && this.description.equals(other.description)
        && this.framebuffer.equals(other.framebuffer);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.depth.hashCode();
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

    @Override public boolean kFramebufferGetDepthIsPackedColor()
    {
      return false;
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

  private static final class KFramebufferForwardGLES2WithoutDepthTexture extends
    KFramebufferForwardAbstract
  {

    public static KFramebufferForwardType newFramebuffer(
      final JCGLInterfaceGLES2Type gl,
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
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStaticType dt =
        gl.texture2DStaticAllocateRGBA4444(
          "color-depth-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

      final RenderbufferType<RenderableDepthKind> dr =
        gl.renderbufferAllocateDepth16(width, height);

      final FramebufferType fb_depth = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb_depth);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb_depth, dt);
        gl.framebufferDrawAttachDepthRenderbuffer(fb_depth, dr);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb_depth);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      final FramebufferType fb_color = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb_color);
      try {
        gl.framebufferDrawAttachColorTexture2D(fb_color, c);
        gl.framebufferDrawAttachDepthRenderbuffer(fb_color, dr);
        final FramebufferStatus status = gl.framebufferDrawValidate(fb_color);
        KFramebufferCommon.checkFramebufferStatus(status);
      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KFramebufferForwardGLES2WithoutDepthTexture(
        c,
        dt,
        dr,
        fb_depth,
        fb_color,
        description);
    }

    private final Texture2DStaticType                   color;
    private final Texture2DStaticType                   depth;
    private final RenderbufferType<RenderableDepthKind> depth_rb;
    private final KFramebufferForwardDescription        description;
    private final FramebufferType                       fb_color;
    private final FramebufferType                       fb_depth;

    private KFramebufferForwardGLES2WithoutDepthTexture(
      final Texture2DStaticType c,
      final Texture2DStaticType dt,
      final RenderbufferType<RenderableDepthKind> dr,
      final FramebufferType in_fb_depth,
      final FramebufferType in_fb_color,
      final KFramebufferForwardDescription in_description)
    {
      super(c.textureGetArea(), c.resourceGetSizeBytes()
        + dt.resourceGetSizeBytes()
        + dr.resourceGetSizeBytes());
      this.color = c;
      this.depth = dt;
      this.depth_rb = dr;
      this.fb_depth = in_fb_depth;
      this.fb_color = in_fb_color;
      this.description = in_description;
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
      final KFramebufferForwardGLES2WithoutDepthTexture other =
        (KFramebufferForwardGLES2WithoutDepthTexture) obj;
      return this.color.equals(other.color)
        && this.depth.equals(other.depth)
        && this.depth_rb.equals(other.depth_rb)
        && this.description.equals(other.description)
        && this.fb_color.equals(other.fb_color)
        && this.fb_depth.equals(other.fb_depth);
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.color.hashCode();
      result = (prime * result) + this.depth.hashCode();
      result = (prime * result) + this.depth_rb.hashCode();
      result = (prime * result) + this.description.hashCode();
      result = (prime * result) + this.fb_color.hashCode();
      result = (prime * result) + this.fb_depth.hashCode();
      return result;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.fb_color);
        gc.framebufferDelete(this.fb_depth);
        gc.renderbufferDelete(this.depth_rb);
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
      return this.fb_color;
    }

    @Override public
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
    }

    @Override public boolean kFramebufferGetDepthIsPackedColor()
    {
      return true;
    }

    @Override public
      FramebufferUsableType
      kFramebufferGetDepthPassFramebuffer()
    {
      return this.fb_depth;
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
    throws JCGLException
  {
    NullCheck.notNull(gi, "GL implementation");
    NullCheck.notNull(description, "Description");

    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferForwardType, JCGLException>() {
        @Override public KFramebufferForwardType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException
        {
          return KFramebufferForwardAbstract.KFramebufferForwardGL2
            .newFramebuffer(gl, description);
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
          throws JCGLException
        {
          final OptionType<JCGLExtensionESDepthTextureType> edt =
            gl.extensionDepthTexture();

          if (edt.isSome()) {
            final Some<JCGLExtensionESDepthTextureType> some =
              (Some<JCGLExtensionESDepthTextureType>) edt;
            return KFramebufferForwardAbstract.KFramebufferForwardGLES2WithDepthTexture
              .newFramebuffer(gl, description, some.get());
          }

          return KFramebufferForwardAbstract.KFramebufferForwardGLES2WithoutDepthTexture
            .newFramebuffer(gl, description);
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
