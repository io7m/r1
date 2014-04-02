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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.JCGLVersion;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.types.RException;

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

abstract class KFramebufferForward implements KFramebufferForwardType
{
  private static final class KFramebufferForwardGL2 extends
    KFramebufferForward
  {
    public static KFramebufferForwardType newFramebuffer(
      final @Nonnull JCGLInterfaceGL2 gl,
      final @Nonnull KFramebufferForwardDescription description,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final KFramebufferRGBADescription desc_rgba =
        description.getRGBADescription();
      final KFramebufferDepthDescription desc_depth =
        description.getDepthDescription();
      final AreaInclusive area = desc_rgba.getArea();
      assert area.equals(desc_depth.getArea());
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA8(
          "color-8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStatic d =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

      final List<FramebufferColorAttachmentPoint> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
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

      return new KFramebufferForwardGL2(c, d, fb, version, description);
    }

    private final @Nonnull Texture2DStatic                color;
    private final @Nonnull Texture2DStatic                depth;
    private final @Nonnull KFramebufferForwardDescription description;
    private final @Nonnull FramebufferReference           framebuffer;

    public KFramebufferForwardGL2(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull JCGLVersion version,
      final @Nonnull KFramebufferForwardDescription in_description)
    {
      super(c.getArea(), c.resourceGetSizeBytes() + d.resourceGetSizeBytes());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
      this.description = in_description;
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
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
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
      KFramebufferForwardDescription
      kFramebufferGetForwardDescription()
    {
      return this.description;
    }

    @Override public @Nonnull
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description.getRGBADescription();
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
    private static @Nonnull Texture2DStatic makeDepth(
      final KFramebufferDepthDescription desc_depth,
      final int width,
      final int height,
      final @Nonnull JCGLTextures2DStaticGL3ES3 gl)
      throws JCGLRuntimeException,
        ConstraintError
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

    private static @Nonnull Texture2DStatic makeRGBA(
      final KFramebufferRGBADescription desc_rgba,
      final int width,
      final int height,
      final @Nonnull JCGLTextures2DStaticGL3ES3 gl)
      throws JCGLRuntimeException,
        ConstraintError
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

    public static @Nonnull
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3 & JCGLRenderbuffersGL3ES3>
      KFramebufferForwardType
      newFramebuffer(
        final @Nonnull G gl,
        final @Nonnull KFramebufferForwardDescription description,
        final @Nonnull JCGLVersion version)
        throws JCGLException,
          ConstraintError
    {
      final KFramebufferRGBADescription desc_rgba =
        description.getRGBADescription();
      final KFramebufferDepthDescription desc_depth =
        description.getDepthDescription();
      final AreaInclusive area = desc_rgba.getArea();
      assert area.equals(desc_depth.getArea());
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        KFramebufferForwardGL3ES3.makeRGBA(desc_rgba, width, height, gl);
      final Texture2DStatic d =
        KFramebufferForwardGL3ES3.makeDepth(desc_depth, width, height, gl);

      final List<FramebufferColorAttachmentPoint> points =
        gl.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
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

      return new KFramebufferForwardGL3ES3(c, d, fb, version, description);
    }

    private final @Nonnull Texture2DStatic                color;
    private final @Nonnull Texture2DStatic                depth;
    private final @Nonnull KFramebufferForwardDescription description;
    private final @Nonnull FramebufferReference           framebuffer;

    public KFramebufferForwardGL3ES3(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull JCGLVersion version,
      final @Nonnull KFramebufferForwardDescription in_description)
    {
      super(c.getArea(), c.resourceGetSizeBytes() + d.resourceGetSizeBytes());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
      this.description = in_description;
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
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
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
      KFramebufferForwardDescription
      kFramebufferGetForwardDescription()
    {
      return this.description;
    }

    @Override public @Nonnull
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description.getRGBADescription();
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
      final @Nonnull KFramebufferForwardDescription description,
      final @Nonnull JCGLExtensionESDepthTexture gldt,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final KFramebufferRGBADescription desc_rgba =
        description.getRGBADescription();
      final KFramebufferDepthDescription desc_depth =
        description.getDepthDescription();
      final AreaInclusive area = desc_rgba.getArea();
      assert area.equals(desc_depth.getArea());
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStatic d =
        gldt.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

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

      return new KFramebufferForwardGLES2WithDepthTexture(
        c,
        d,
        fb,
        description,
        version);
    }

    private final @Nonnull Texture2DStatic                color;
    private final @Nonnull Texture2DStatic                depth;
    private final @Nonnull KFramebufferForwardDescription description;
    private final @Nonnull FramebufferReference           framebuffer;

    public KFramebufferForwardGLES2WithDepthTexture(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic d,
      final @Nonnull FramebufferReference fb,
      final @Nonnull KFramebufferForwardDescription in_description,
      final @Nonnull JCGLVersion version)
    {
      super(c.getArea(), c.resourceGetSizeBytes() + d.resourceGetSizeBytes());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
      this.description = in_description;
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
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
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
      KFramebufferForwardDescription
      kFramebufferGetForwardDescription()
    {
      return this.description;
    }

    @Override public @Nonnull
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description.getRGBADescription();
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
      final @Nonnull KFramebufferForwardDescription description,
      final @Nonnull JCGLVersion version)
      throws JCGLException,
        ConstraintError
    {
      final KFramebufferRGBADescription desc_rgba =
        description.getRGBADescription();
      final KFramebufferDepthDescription desc_depth =
        description.getDepthDescription();
      final AreaInclusive area = desc_rgba.getArea();
      assert area.equals(desc_depth.getArea());
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic c =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_rgba.getFilterMinification(),
          desc_rgba.getFilterMagnification());

      final Texture2DStatic dt =
        gl.texture2DStaticAllocateRGBA4444(
          "color-depth-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          desc_depth.getFilterMinification(),
          desc_depth.getFilterMagnification());

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
        description,
        version);
    }

    private final @Nonnull Texture2DStatic                color;
    private final @Nonnull Texture2DStatic                depth;
    private final @Nonnull Renderbuffer<RenderableDepth>  depth_rb;
    private final @Nonnull KFramebufferForwardDescription description;
    private final @Nonnull FramebufferReference           fb_color;
    private final @Nonnull FramebufferReference           fb_depth;

    private KFramebufferForwardGLES2WithoutDepthTexture(
      final @Nonnull Texture2DStatic c,
      final @Nonnull Texture2DStatic dt,
      final @Nonnull Renderbuffer<RenderableDepth> dr,
      final @Nonnull FramebufferReference in_fb_depth,
      final @Nonnull FramebufferReference in_fb_color,
      final @Nonnull KFramebufferForwardDescription in_description,
      final @Nonnull JCGLVersion version)
    {
      super(c.getArea(), c.resourceGetSizeBytes()
        + dt.resourceGetSizeBytes()
        + dr.resourceGetSizeBytes());
      this.color = c;
      this.depth = dt;
      this.depth_rb = dr;
      this.fb_depth = in_fb_depth;
      this.fb_color = in_fb_color;
      this.description = in_description;
    }

    @Override public void kFramebufferDelete(
      final @Nonnull JCGLImplementation g)
      throws ConstraintError,
        RException
    {
      try {
        final JCGLInterfaceCommon gc = g.getGLCommon();
        gc.framebufferDelete(this.fb_color);
        gc.framebufferDelete(this.fb_depth);
        gc.renderbufferDelete(this.depth_rb);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public @Nonnull
      FramebufferReferenceUsable
      kFramebufferGetColorFramebuffer()
    {
      return this.fb_color;
    }

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description.getDepthDescription();
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

    @Override public
      KFramebufferForwardDescription
      kFramebufferGetForwardDescription()
    {
      return this.description;
    }

    @Override public @Nonnull
      KFramebufferRGBADescription
      kFramebufferGetRGBADescription()
    {
      return this.description.getRGBADescription();
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
    final @Nonnull KFramebufferForwardDescription description)
    throws ConstraintError,
      JCGLException
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(description, "Description");

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
            description,
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
            description,
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
              description,
              some.value,
              gl.metaGetVersion());
          }

          return KFramebufferForwardGLES2WithoutDepthTexture.newFramebuffer(
            gl,
            description,
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
            description,
            gl.metaGetVersion());
        }
      });
  }

  private final @Nonnull AreaInclusive area;
  private boolean                      deleted;
  private final long                   size;

  protected KFramebufferForward(
    final @Nonnull AreaInclusive in_area,
    final long in_size)
  {
    this.deleted = false;
    this.area = in_area;
    this.size = in_size;
  }

  @Override public final @Nonnull AreaInclusive kFramebufferGetArea()
  {
    return this.area;
  }

  @Override public final long kFramebufferGetSizeBytes()
  {
    return this.size;
  }

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
