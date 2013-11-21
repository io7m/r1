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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
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
import com.io7m.jcanephora.JCGLExtensionPackedDepthStencil;
import com.io7m.jcanephora.JCGLExtensionsGLES2;
import com.io7m.jcanephora.JCGLFramebuffersGL3;
import com.io7m.jcanephora.JCGLFramebuffersGLES2;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLRenderbuffersGL2;
import com.io7m.jcanephora.JCGLRenderbuffersGL3;
import com.io7m.jcanephora.JCGLRenderbuffersGLES2;
import com.io7m.jcanephora.JCGLRenderbuffersGLES3;
import com.io7m.jcanephora.JCGLTextures2DStaticGL2;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3;
import com.io7m.jcanephora.JCGLTextures2DStaticGLES2;
import com.io7m.jcanephora.JCGLTextures2DStaticGLES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.RenderableDepthStencil;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

@Immutable final class KFramebufferCommon
{
  private static final class KFramebufferBasicDepth implements
    KFramebufferDepth
  {
    static @Nonnull KFramebufferDepth newDepth(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull AreaInclusive size)
      throws ConstraintError,
        JCGLUnsupportedException,
        JCGLException
    {
      Constraints.constrainNotNull(gi, "GL implementation");
      Constraints.constrainNotNull(size, "Framebuffer size");

      final JCGLInterfaceCommon g = gi.getGLCommon();
      final FramebufferReference f = g.framebufferAllocate();
      g.framebufferDrawBind(f);

      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Texture2DStatic texture =
        KFramebufferBasicDepth.newDepthTexture(gi, width, height);

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachDepthTexture2D(f, texture);

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

      return new KFramebufferBasicDepth(size, fb, texture);
    }

    private static @Nonnull Texture2DStatic newDepthTexture(
      final @Nonnull JCGLImplementation gi,
      final int width,
      final int height)
      throws JCGLException,
        ConstraintError
    {
      {
        final Option<JCGLInterfaceGL3> o = gi.getGL3();
        switch (o.type) {
          case OPTION_SOME:
          {
            final JCGLInterfaceGL3 g =
              ((Option.Some<JCGLInterfaceGL3>) o).value;
            return g.texture2DStaticAllocateDepth16(
              "depth",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureFilterMinification.TEXTURE_FILTER_NEAREST,
              TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGLES3> o = gi.getGLES3();
        switch (o.type) {
          case OPTION_SOME:
          {
            final JCGLInterfaceGLES3 g =
              ((Option.Some<JCGLInterfaceGLES3>) o).value;
            return g.texture2DStaticAllocateDepth16(
              "depth",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureFilterMinification.TEXTURE_FILTER_NEAREST,
              TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGL2> o = gi.getGL2();
        switch (o.type) {
          case OPTION_SOME:
          {
            final JCGLInterfaceGL2 g =
              ((Option.Some<JCGLInterfaceGL2>) o).value;

            throw new UnimplementedCodeException();
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGLES2> o = gi.getGLES2();
        switch (o.type) {
          case OPTION_SOME:
          {
            final JCGLInterfaceGLES2 g =
              ((Option.Some<JCGLInterfaceGLES2>) o).value;
            return g.texture2DStaticAllocateDepth16(
              "depth",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureFilterMinification.TEXTURE_FILTER_NEAREST,
              TextureFilterMagnification.TEXTURE_FILTER_NEAREST);
          }
          case OPTION_NONE:
            break;
        }
      }

      throw new UnreachableCodeException();
    }

    private final @Nonnull AreaInclusive        area;
    private boolean                             deleted;
    private final @Nonnull Texture2DStatic      depth;
    private final @Nonnull FramebufferReference framebuffer;

    private KFramebufferBasicDepth(
      final @Nonnull AreaInclusive area,
      final @Nonnull FramebufferReference fb,
      final @Nonnull Texture2DStatic depth)
    {
      this.area = area;
      this.framebuffer = fb;
      this.depth = depth;
      this.deleted = false;
    }

    @Override public void kframebufferDelete(
      final JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      gc.texture2DStaticDelete(this.depth);
      this.deleted = true;
    }

    @Override public AreaInclusive kframebufferGetArea()
    {
      return this.area;
    }

    @Override public Texture2DStaticUsable kframebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public FramebufferReferenceUsable kframebufferGetFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.deleted;
    }
  }

  private static final class KFramebufferBasicDepthWithTexture implements
    KFramebufferDepth
  {
    static @Nonnull KFramebufferDepth newDepthWithTexture(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull Texture2DStaticUsable texture)
      throws ConstraintError,
        JCGLUnsupportedException,
        JCGLException
    {
      Constraints.constrainNotNull(gi, "GL implementation");
      Constraints.constrainNotNull(texture, "Texture");

      final JCGLInterfaceCommon g = gi.getGLCommon();
      final FramebufferReference f = g.framebufferAllocate();
      g.framebufferDrawBind(f);

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachDepthTexture2D(f, texture);

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

      return new KFramebufferBasicDepthWithTexture(fb, texture);
    }

    private boolean                              deleted;
    private final @Nonnull Texture2DStaticUsable depth;
    private final @Nonnull FramebufferReference  framebuffer;

    private KFramebufferBasicDepthWithTexture(
      final @Nonnull FramebufferReference fb,
      final @Nonnull Texture2DStaticUsable depth)
    {
      this.framebuffer = fb;
      this.depth = depth;
      this.deleted = false;
    }

    @Override public void kframebufferDelete(
      final JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      this.deleted = true;
    }

    @Override public AreaInclusive kframebufferGetArea()
    {
      return this.depth.getArea();
    }

    @Override public Texture2DStaticUsable kframebufferGetDepthTexture()
    {
      return this.depth;
    }

    @Override public FramebufferReferenceUsable kframebufferGetFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.deleted;
    }
  }

  private static final class KFramebufferBasicRGBA implements
    KFramebufferRGBA
  {
    static @Nonnull KFramebufferRGBA allocateBasicRGBA(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull AreaInclusive size)
      throws ConstraintError,
        JCGLException,
        JCGLUnsupportedException
    {
      Constraints.constrainNotNull(gi, "GL implementation");
      Constraints.constrainNotNull(size, "Framebuffer size");

      {
        final Option<JCGLInterfaceGL3> o = gi.getGL3();
        switch (o.type) {
          case OPTION_SOME:
          {
            final Some<JCGLInterfaceGL3> s =
              (Option.Some<JCGLInterfaceGL3>) o;
            return KFramebufferBasicRGBA.allocateBasicRGBA_GL3(s.value, size);
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGLES3> o = gi.getGLES3();
        switch (o.type) {
          case OPTION_SOME:
          {
            final Some<JCGLInterfaceGLES3> s =
              (Option.Some<JCGLInterfaceGLES3>) o;
            return KFramebufferBasicRGBA.allocateBasicRGBA_GLES3(
              s.value,
              size);
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGL2> o = gi.getGL2();
        switch (o.type) {
          case OPTION_SOME:
          {
            final Some<JCGLInterfaceGL2> s =
              (Option.Some<JCGLInterfaceGL2>) o;
            return KFramebufferBasicRGBA.allocateBasicRGBA_GL2(s.value, size);
          }
          case OPTION_NONE:
            break;
        }
      }

      {
        final Option<JCGLInterfaceGLES2> o = gi.getGLES2();
        switch (o.type) {
          case OPTION_SOME:
          {
            final Some<JCGLInterfaceGLES2> s =
              (Option.Some<JCGLInterfaceGLES2>) o;
            return KFramebufferBasicRGBA.allocateBasicRGBA_GLES2(
              s.value,
              size);
          }
          case OPTION_NONE:
            break;
        }
      }

      throw new UnreachableCodeException();
    }

    private static @Nonnull
      <R, G extends JCGLFramebuffersGL3 & JCGLTextures2DStaticGL2 & JCGLRenderbuffersGL2>
      KFramebufferRGBA
      allocateBasicRGBA_GL2(
        final @Nonnull G g,
        final @Nonnull AreaInclusive size)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final FramebufferReference f = g.framebufferAllocate();
      g.framebufferDrawBind(f);

      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Texture2DStatic color =
        g.texture2DStaticAllocateRGBA8(
          "framebuffer",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Renderbuffer<RenderableDepthStencil> depth =
        g.renderbufferAllocateDepth24Stencil8(width, height);

      final List<FramebufferColorAttachmentPoint> points =
        g.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        g.framebufferGetDrawBuffers();

      final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
        new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachColorTexture2D(fb, color);
        g.framebufferDrawAttachDepthStencilRenderbuffer(fb, depth);
        g.framebufferDrawSetBuffers(fb, mappings);

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

      return new KFramebufferBasicRGBA(size, fb, color, depth);
    }

    private static @Nonnull
      <R, G extends JCGLFramebuffersGL3 & JCGLTextures2DStaticGL3 & JCGLRenderbuffersGL3>
      KFramebufferRGBA
      allocateBasicRGBA_GL3(
        final @Nonnull G g,
        final @Nonnull AreaInclusive size)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final FramebufferReference f = g.framebufferAllocate();
      g.framebufferDrawBind(f);

      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Texture2DStatic color =
        g.texture2DStaticAllocateRGBA8(
          "framebuffer",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Renderbuffer<RenderableDepthStencil> depth =
        g.renderbufferAllocateDepth24Stencil8(width, height);

      final List<FramebufferColorAttachmentPoint> points =
        g.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        g.framebufferGetDrawBuffers();

      final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
        new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachColorTexture2D(fb, color);
        g.framebufferDrawAttachDepthStencilRenderbuffer(fb, depth);
        g.framebufferDrawSetBuffers(fb, mappings);

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

      return new KFramebufferBasicRGBA(size, fb, color, depth);
    }

    private static @Nonnull
      <R, G extends JCGLFramebuffersGLES2 & JCGLTextures2DStaticGLES2 & JCGLRenderbuffersGLES2 & JCGLExtensionsGLES2>
      KFramebufferRGBA
      allocateBasicRGBA_GLES2(
        final @Nonnull G g,
        final @Nonnull AreaInclusive size)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Texture2DStatic color =
        g.texture2DStaticAllocateRGBA4444(
          "framebuffer",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      Renderbuffer<?> saved_depth = null;
      try {
        g.framebufferDrawAttachColorTexture2D(fb, color);

        final Option<JCGLExtensionPackedDepthStencil> eopt =
          g.extensionPackedDepthStencil().extensionGetSupport();

        switch (eopt.type) {
          case OPTION_NONE:
          {
            final Renderbuffer<RenderableDepth> depth =
              g.renderbufferAllocateDepth16(width, height);
            g.framebufferDrawAttachDepthRenderbuffer(fb, depth);

            saved_depth = depth;
            break;
          }
          case OPTION_SOME:
          {
            final JCGLExtensionPackedDepthStencil ext =
              ((Option.Some<JCGLExtensionPackedDepthStencil>) eopt).value;
            final Renderbuffer<RenderableDepthStencil> depth =
              ext.renderbufferAllocateDepth24Stencil8(width, height);
            ext.framebufferDrawAttachDepthStencilRenderbuffer(fb, depth);

            saved_depth = depth;
            break;
          }
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

      return new KFramebufferBasicRGBA(size, fb, color, saved_depth);
    }

    private static @Nonnull
      <R, G extends JCGLFramebuffersGL3 & JCGLTextures2DStaticGLES3 & JCGLRenderbuffersGLES3>
      KFramebufferRGBA
      allocateBasicRGBA_GLES3(
        final @Nonnull G g,
        final @Nonnull AreaInclusive size)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final FramebufferReference f = g.framebufferAllocate();
      g.framebufferDrawBind(f);

      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Texture2DStatic color =
        g.texture2DStaticAllocateRGBA8(
          "framebuffer",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Renderbuffer<RenderableDepthStencil> depth =
        g.renderbufferAllocateDepth24Stencil8(width, height);

      final List<FramebufferColorAttachmentPoint> points =
        g.framebufferGetColorAttachmentPoints();
      final List<FramebufferDrawBuffer> buffers =
        g.framebufferGetDrawBuffers();

      final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> mappings =
        new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();
      mappings.put(buffers.get(0), points.get(0));

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachColorTexture2D(fb, color);
        g.framebufferDrawAttachDepthStencilRenderbuffer(fb, depth);
        g.framebufferDrawSetBuffers(fb, mappings);

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

      return new KFramebufferBasicRGBA(size, fb, color, depth);
    }

    private final @Nonnull AreaInclusive        area;
    private final @Nonnull Texture2DStatic      color;
    private boolean                             deleted;
    private final @Nonnull Renderbuffer<?>      depth_stencil;
    private final @Nonnull FramebufferReference framebuffer;

    private KFramebufferBasicRGBA(
      final @Nonnull AreaInclusive area,
      final @Nonnull FramebufferReference fb,
      final @Nonnull Texture2DStatic color,
      final @Nonnull Renderbuffer<?> depth_stencil)
    {
      this.area = area;
      this.framebuffer = fb;
      this.color = color;
      this.depth_stencil = depth_stencil;
      this.deleted = false;
    }

    @Override public void kframebufferDelete(
      final JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      gc.texture2DStaticDelete(this.color);
      gc.renderbufferDelete(this.depth_stencil);
      this.deleted = true;
    }

    @Override public AreaInclusive kframebufferGetArea()
    {
      return this.area;
    }

    @Override public FramebufferReferenceUsable kframebufferGetFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public Texture2DStaticUsable kframebufferGetRGBAOutputTexture()
    {
      return this.color;
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.deleted;
    }
  }

  public static @Nonnull KFramebufferRGBA newBasicRGBA(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull AreaInclusive size)
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    return KFramebufferBasicRGBA.allocateBasicRGBA(gi, size);
  }

  public static @Nonnull KFramebufferDepth newDepthFramebuffer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull AreaInclusive size)
    throws JCGLUnsupportedException,
      JCGLException,
      ConstraintError
  {
    return KFramebufferBasicDepth.newDepth(gi, size);
  }

  public static @Nonnull KFramebufferDepth newDepthFramebufferWithTexture(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Texture2DStaticUsable texture)
    throws JCGLUnsupportedException,
      JCGLException,
      ConstraintError
  {
    return KFramebufferBasicDepthWithTexture.newDepthWithTexture(gi, texture);
  }
}
