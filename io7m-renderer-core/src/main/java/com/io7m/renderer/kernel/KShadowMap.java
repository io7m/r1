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

import javax.annotation.Nonnull;

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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.renderer.RException;

public abstract class KShadowMap implements KShadowMapType
{
  public static abstract class KShadowMapBasic extends KShadowMap implements
    KFramebufferDepthUsable
  {
    private static final class KShadowMapBasicGL2 extends KShadowMapBasic
    {
      public static
        KShadowMapBasic
        newShadowMapBasic(
          final @Nonnull JCGLInterfaceGL2 gl,
          final int width,
          final int height,
          final @Nonnull KShadowFilter filter,
          @SuppressWarnings("unused") final @Nonnull KShadowPrecision precision)
          throws JCGLException,
            ConstraintError
      {
        final Texture2DStatic depth =
          gl.texture2DStaticAllocateDepth24Stencil8(
            "depth-24s8",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            filter.getMinification(),
            filter.getMagnification());

        final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
          new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();

        final FramebufferReference fb = gl.framebufferAllocate();
        gl.framebufferDrawBind(fb);

        try {
          gl.framebufferDrawAttachDepthTexture2D(fb, depth);
          gl.framebufferDrawSetBuffers(fb, empty);

          final FramebufferStatus status = gl.framebufferDrawValidate(fb);
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
          gl.framebufferDrawUnbind();
        }

        return new KShadowMapBasicGL2(depth, fb);
      }

      private final @Nonnull Texture2DStatic      depth;
      private final @Nonnull FramebufferReference framebuffer;
      private final int                           size;

      private KShadowMapBasicGL2(
        final @Nonnull Texture2DStatic depth,
        final @Nonnull FramebufferReference framebuffer)
      {
        this.depth = depth;
        this.framebuffer = framebuffer;
        this.size =
          this.depth.getHeight()
            * (this.depth.getWidth() * this.depth
              .getType()
              .getBytesPerPixel());
      }

      @Override public void kShadowMapDelete(
        final JCGLImplementation g)
        throws RException,
          ConstraintError
      {
        try {
          final JCGLInterfaceCommon gc = g.getGLCommon();
          gc.framebufferDelete(this.framebuffer);
          gc.texture2DStaticDelete(this.depth);
        } catch (final JCGLRuntimeException e) {
          throw RException.fromJCGLException(e);
        } finally {
          super.setDeleted(true);
        }
      }

      @Override public long kShadowMapGetSizeBytes()
      {
        return this.size;
      }

      @Override public
        FramebufferReferenceUsable
        kFramebufferGetDepthPassFramebuffer()
      {
        return this.framebuffer;
      }

      @Override public Texture2DStaticUsable kFramebufferGetDepthTexture()
      {
        return this.depth;
      }

      @Override public AreaInclusive kFramebufferGetArea()
      {
        return this.depth.getArea();
      }
    }

    private static final class KShadowMapBasicGL3ES3 extends KShadowMapBasic
    {
      public static
        <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3>
        KShadowMapBasic
        newShadowMapBasic(
          final @Nonnull G gl,
          final int width,
          final int height,
          final @Nonnull KShadowFilter filter,
          final @Nonnull KShadowPrecision precision)
          throws JCGLException,
            ConstraintError
      {
        Texture2DStatic depth = null;
        switch (precision) {
          case SHADOW_PRECISION_16:
          {
            depth =
              gl.texture2DStaticAllocateDepth16(
                "depth-16",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                filter.getMinification(),
                filter.getMagnification());
            break;
          }
          case SHADOW_PRECISION_24:
          {
            depth =
              gl.texture2DStaticAllocateDepth24(
                "depth-24",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                filter.getMinification(),
                filter.getMagnification());
            break;
          }
          case SHADOW_PRECISION_32:
          {
            depth =
              gl.texture2DStaticAllocateDepth32f(
                "depth-32f",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                filter.getMinification(),
                filter.getMagnification());
            break;
          }
        }

        assert depth != null;

        final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
          new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();

        final FramebufferReference fb = gl.framebufferAllocate();
        gl.framebufferDrawBind(fb);

        try {
          gl.framebufferDrawAttachDepthTexture2D(fb, depth);
          gl.framebufferDrawSetBuffers(fb, empty);

          final FramebufferStatus status = gl.framebufferDrawValidate(fb);
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
          gl.framebufferDrawUnbind();
        }

        return new KShadowMapBasicGL3ES3(depth, fb);
      }

      private final @Nonnull Texture2DStatic      depth;
      private final @Nonnull FramebufferReference framebuffer;
      private final int                           size;

      private KShadowMapBasicGL3ES3(
        final @Nonnull Texture2DStatic depth,
        final @Nonnull FramebufferReference framebuffer)
      {
        this.depth = depth;
        this.framebuffer = framebuffer;
        this.size =
          this.depth.getHeight()
            * (this.depth.getWidth() * this.depth
              .getType()
              .getBytesPerPixel());
      }

      @Override public
        FramebufferReferenceUsable
        kFramebufferGetDepthPassFramebuffer()
      {
        return this.framebuffer;
      }

      @Override public Texture2DStaticUsable kFramebufferGetDepthTexture()
      {
        return this.depth;
      }

      @Override public AreaInclusive kFramebufferGetArea()
      {
        return this.depth.getArea();
      }

      @Override public void kShadowMapDelete(
        final JCGLImplementation g)
        throws RException,
          ConstraintError
      {
        try {
          final JCGLInterfaceCommon gc = g.getGLCommon();
          gc.framebufferDelete(this.framebuffer);
          gc.texture2DStaticDelete(this.depth);
        } catch (final JCGLRuntimeException e) {
          throw RException.fromJCGLException(e);
        } finally {
          super.setDeleted(true);
        }
      }

      @Override public long kShadowMapGetSizeBytes()
      {
        return this.size;
      }
    }

    private static final class KShadowMapBasicGLES2WithDepthTexture extends
      KShadowMapBasic
    {
      public static
        KShadowMapBasic
        newShadowMapBasic(
          final @Nonnull JCGLInterfaceGLES2 gl,
          final int width,
          final int height,
          final @Nonnull KShadowFilter filter,
          @SuppressWarnings("unused") final @Nonnull KShadowPrecision precision,
          final @Nonnull JCGLExtensionESDepthTexture ext)
          throws JCGLException,
            ConstraintError
      {
        final Texture2DStatic t =
          ext.texture2DStaticAllocateDepth16(
            "depth-16",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            filter.getMinification(),
            filter.getMagnification());

        final FramebufferReference fb = gl.framebufferAllocate();
        gl.framebufferDrawBind(fb);

        try {
          gl.framebufferDrawAttachDepthTexture2D(fb, t);

          final FramebufferStatus status = gl.framebufferDrawValidate(fb);
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
          gl.framebufferDrawUnbind();
        }

        return new KShadowMapBasicGLES2WithDepthTexture(t, fb);
      }

      private final @Nonnull Texture2DStatic      depth;
      private final @Nonnull FramebufferReference framebuffer;
      private final int                           size;

      private KShadowMapBasicGLES2WithDepthTexture(
        final @Nonnull Texture2DStatic depth,
        final @Nonnull FramebufferReference framebuffer)
      {
        this.depth = depth;
        this.framebuffer = framebuffer;
        this.size =
          this.depth.getHeight()
            * (this.depth.getWidth() * this.depth
              .getType()
              .getBytesPerPixel());
      }

      @Override public
        FramebufferReferenceUsable
        kFramebufferGetDepthPassFramebuffer()
      {
        return this.framebuffer;
      }

      @Override public Texture2DStaticUsable kFramebufferGetDepthTexture()
      {
        return this.depth;
      }

      @Override public AreaInclusive kFramebufferGetArea()
      {
        return this.depth.getArea();
      }

      @Override public void kShadowMapDelete(
        final JCGLImplementation g)
        throws RException,
          ConstraintError
      {
        try {
          final JCGLInterfaceCommon gc = g.getGLCommon();
          gc.framebufferDelete(this.framebuffer);
          gc.texture2DStaticDelete(this.depth);
        } catch (final JCGLRuntimeException e) {
          throw RException.fromJCGLException(e);
        } finally {
          super.setDeleted(true);
        }
      }

      @Override public long kShadowMapGetSizeBytes()
      {
        return this.size;
      }
    }

    private static final class KShadowMapBasicGLES2WithoutDepthTexture extends
      KShadowMapBasic
    {
      public static
        KShadowMapBasic
        newShadowMapBasic(
          final @Nonnull JCGLInterfaceGLES2 gl,
          final int width,
          final int height,
          final @Nonnull KShadowFilter filter,
          @SuppressWarnings("unused") final @Nonnull KShadowPrecision precision)
          throws JCGLException,
            ConstraintError
      {
        final Renderbuffer<RenderableDepth> rb =
          gl.renderbufferAllocateDepth16(width, height);
        final Texture2DStatic t =
          gl.texture2DStaticAllocateRGBA4444(
            "color-4444",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            filter.getMinification(),
            filter.getMagnification());

        final FramebufferReference fb = gl.framebufferAllocate();
        gl.framebufferDrawBind(fb);

        try {
          gl.framebufferDrawAttachColorTexture2D(fb, t);
          gl.framebufferDrawAttachDepthRenderbuffer(fb, rb);

          final FramebufferStatus status = gl.framebufferDrawValidate(fb);
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
          gl.framebufferDrawUnbind();
        }

        return new KShadowMapBasicGLES2WithoutDepthTexture(rb, t, fb);
      }

      private final @Nonnull Texture2DStatic               depth;
      private final @Nonnull FramebufferReference          framebuffer;
      private final @Nonnull Renderbuffer<RenderableDepth> renderbuffer;
      private final int                                    size;

      private KShadowMapBasicGLES2WithoutDepthTexture(
        final @Nonnull Renderbuffer<RenderableDepth> rb,
        final @Nonnull Texture2DStatic depth,
        final @Nonnull FramebufferReference framebuffer)
      {
        this.depth = depth;
        this.renderbuffer = rb;
        this.framebuffer = framebuffer;

        final int w = this.depth.getWidth();
        final int h = this.depth.getHeight();

        final int rb_size = h * (w * rb.getType().getBytesPerPixel());
        final int tc_size = h * (w * depth.getType().getBytesPerPixel());
        this.size = rb_size + tc_size;
      }

      @Override public
        FramebufferReferenceUsable
        kFramebufferGetDepthPassFramebuffer()
      {
        return this.framebuffer;
      }

      @Override public Texture2DStaticUsable kFramebufferGetDepthTexture()
      {
        return this.depth;
      }

      @Override public AreaInclusive kFramebufferGetArea()
      {
        return this.depth.getArea();
      }

      @Override public void kShadowMapDelete(
        final JCGLImplementation g)
        throws RException,
          ConstraintError
      {
        try {
          final JCGLInterfaceCommon gc = g.getGLCommon();
          gc.framebufferDelete(this.framebuffer);
          gc.renderbufferDelete(this.renderbuffer);
          gc.texture2DStaticDelete(this.depth);
        } catch (final JCGLRuntimeException e) {
          throw RException.fromJCGLException(e);
        } finally {
          super.setDeleted(true);
        }
      }

      @Override public long kShadowMapGetSizeBytes()
      {
        return this.size;
      }
    }

    public static @Nonnull KShadowMapBasic newShadowMapBasic(
      final @Nonnull JCGLImplementation g,
      final int width,
      final int height,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws RException,
        ConstraintError
    {
      try {
        return g
          .implementationAccept(new JCGLImplementationVisitor<KShadowMapBasic, JCGLException>() {
            @Override public KShadowMapBasic implementationIsGL2(
              final JCGLInterfaceGL2 gl)
              throws JCGLException,
                ConstraintError
            {
              return KShadowMapBasicGL2.newShadowMapBasic(
                gl,
                width,
                height,
                filter,
                precision);
            }

            @Override public KShadowMapBasic implementationIsGL3(
              final JCGLInterfaceGL3 gl)
              throws JCGLException,
                ConstraintError
            {
              return KShadowMapBasicGL3ES3.newShadowMapBasic(
                gl,
                width,
                height,
                filter,
                precision);
            }

            @Override public KShadowMapBasic implementationIsGLES2(
              final JCGLInterfaceGLES2 gl)
              throws JCGLException,
                ConstraintError
            {
              final Option<JCGLExtensionESDepthTexture> ext =
                gl.extensionDepthTexture();
              if (ext.isSome()) {
                return KShadowMapBasicGLES2WithDepthTexture
                  .newShadowMapBasic(
                    gl,
                    width,
                    height,
                    filter,
                    precision,
                    ((Some<JCGLExtensionESDepthTexture>) ext).value);
              }
              return KShadowMapBasicGLES2WithoutDepthTexture
                .newShadowMapBasic(gl, width, height, filter, precision);
            }

            @Override public KShadowMapBasic implementationIsGLES3(
              final JCGLInterfaceGLES3 gl)
              throws JCGLException,
                ConstraintError
            {
              return KShadowMapBasicGL3ES3.newShadowMapBasic(
                gl,
                width,
                height,
                filter,
                precision);
            }
          });
      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      }
    }

    @Override public final
      <A, E extends Throwable, V extends KShadowMapVisitor<A, E>>
      A
      kShadowMapAccept(
        final @Nonnull V v)
        throws E,
          RException
    {
      return v.kShadowMapVisitBasic(this);
    }
  }

  private boolean deleted;

  protected KShadowMap()
  {
    this.deleted = false;
  }

  @Override public final boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  protected final void setDeleted(
    final boolean deleted)
  {
    this.deleted = deleted;
  }
}
