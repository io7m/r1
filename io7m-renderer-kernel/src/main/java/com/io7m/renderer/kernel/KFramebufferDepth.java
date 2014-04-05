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
import java.util.Map;

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
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.types.RException;

abstract class KFramebufferDepth implements KFramebufferDepthType
{
  private static final class KFramebufferDepthGL2 extends KFramebufferDepth
  {
    public static KFramebufferDepth newDepthFramebuffer(
      final @Nonnull JCGLInterfaceGL2 gl,
      final @Nonnull KFramebufferDepthDescription description)
      throws JCGLException,
        ConstraintError
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic depth =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "depth-24s8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          description.getFilterMinification(),
          description.getFilterMagnification());

      final Map<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
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

      return new KFramebufferDepthGL2(depth, fb, description);
    }

    private final @Nonnull Texture2DStatic              depth;
    private final @Nonnull KFramebufferDepthDescription description;
    private final @Nonnull FramebufferReference         framebuffer;

    private KFramebufferDepthGL2(
      final @Nonnull Texture2DStatic in_depth,
      final @Nonnull FramebufferReference in_framebuffer,
      final @Nonnull KFramebufferDepthDescription in_description)
    {
      super(in_depth.getArea(), in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.framebuffer = in_framebuffer;
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
        gc.texture2DStaticDelete(this.depth);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description;
    }

    @Override public boolean kFramebufferGetDepthIsPackedColour()
    {
      return false;
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
  }

  private static final class KFramebufferDepthGL3ES3 extends
    KFramebufferDepth
  {
    public static
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLFramebuffersGL3>
      KFramebufferDepth
      newDepthFramebuffer(
        final @Nonnull G gl,
        final @Nonnull KFramebufferDepthDescription description)
        throws JCGLException,
          ConstraintError
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      Texture2DStatic depth = null;
      switch (description.getDepthPrecision()) {
        case DEPTH_PRECISION_16:
        {
          depth =
            gl.texture2DStaticAllocateDepth16(
              "depth-16",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              description.getFilterMinification(),
              description.getFilterMagnification());
          break;
        }
        case DEPTH_PRECISION_24:
        {
          depth =
            gl.texture2DStaticAllocateDepth24(
              "depth-24",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              description.getFilterMinification(),
              description.getFilterMagnification());
          break;
        }
        case DEPTH_PRECISION_32F:
        {
          depth =
            gl.texture2DStaticAllocateDepth32f(
              "depth-32f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              description.getFilterMinification(),
              description.getFilterMagnification());
          break;
        }
      }

      assert depth != null;

      final Map<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
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

      return new KFramebufferDepthGL3ES3(depth, fb, description);
    }

    private final @Nonnull Texture2DStatic              depth;
    private final @Nonnull KFramebufferDepthDescription description;
    private final @Nonnull FramebufferReference         framebuffer;

    private KFramebufferDepthGL3ES3(
      final @Nonnull Texture2DStatic in_depth,
      final @Nonnull FramebufferReference in_framebuffer,
      final @Nonnull KFramebufferDepthDescription d)
    {
      super(in_depth.getArea(), in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.framebuffer = in_framebuffer;
      this.description = d;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementation g)
      throws ConstraintError,
        RException
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

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description;
    }

    @Override public boolean kFramebufferGetDepthIsPackedColour()
    {
      return false;
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
  }

  private static final class KFramebufferDepthGLES2WithDepthTexture extends
    KFramebufferDepth
  {
    public static KFramebufferDepth newDepthFramebuffer(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull KFramebufferDepthDescription description,
      final @Nonnull JCGLExtensionESDepthTexture ext)
      throws JCGLException,
        ConstraintError
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStatic t =
        ext.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          description.getFilterMinification(),
          description.getFilterMagnification());

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

      return new KFramebufferDepthGLES2WithDepthTexture(t, fb, description);
    }

    private final @Nonnull Texture2DStatic              depth;
    private final @Nonnull KFramebufferDepthDescription description;
    private final @Nonnull FramebufferReference         framebuffer;

    private KFramebufferDepthGLES2WithDepthTexture(
      final @Nonnull Texture2DStatic in_depth,
      final @Nonnull FramebufferReference in_framebuffer,
      final @Nonnull KFramebufferDepthDescription in_description)
    {
      super(in_depth.getArea(), in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.framebuffer = in_framebuffer;
      this.description = in_description;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementation g)
      throws ConstraintError,
        RException
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

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description;
    }

    @Override public boolean kFramebufferGetDepthIsPackedColour()
    {
      return false;
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
  }

  private static final class KFramebufferDepthGLES2WithoutDepthTexture extends
    KFramebufferDepth
  {
    public static KFramebufferDepth newDepthFramebuffer(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull KFramebufferDepthDescription description)
      throws JCGLException,
        ConstraintError
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Renderbuffer<RenderableDepth> rb =
        gl.renderbufferAllocateDepth16(width, height);
      final Texture2DStatic t =
        gl.texture2DStaticAllocateRGBA4444(
          "color-4444",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          description.getFilterMinification(),
          description.getFilterMagnification());

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

      return new KFramebufferDepthGLES2WithoutDepthTexture(
        rb,
        t,
        description,
        fb);
    }

    private final @Nonnull Texture2DStatic               depth;
    private final @Nonnull KFramebufferDepthDescription  description;
    private final @Nonnull FramebufferReference          framebuffer;
    private final @Nonnull Renderbuffer<RenderableDepth> renderbuffer;

    private KFramebufferDepthGLES2WithoutDepthTexture(
      final @Nonnull Renderbuffer<RenderableDepth> rb,
      final @Nonnull Texture2DStatic in_depth,
      final @Nonnull KFramebufferDepthDescription in_description,
      final @Nonnull FramebufferReference in_framebuffer)
    {
      super(in_depth.getArea(), rb.resourceGetSizeBytes()
        + in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.renderbuffer = rb;
      this.framebuffer = in_framebuffer;
      this.description = in_description;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementation g)
      throws ConstraintError,
        RException
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

    @Override public @Nonnull
      KFramebufferDepthDescription
      kFramebufferGetDepthDescription()
    {
      return this.description;
    }

    @Override public boolean kFramebufferGetDepthIsPackedColour()
    {
      return true;
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
  }

  public static @Nonnull KFramebufferDepth newDepthFramebuffer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KFramebufferDepthDescription description)
    throws RException,
      ConstraintError
  {
    try {
      return gl
        .implementationAccept(new JCGLImplementationVisitor<KFramebufferDepth, JCGLException>() {
          @Override public KFramebufferDepth implementationIsGL2(
            final JCGLInterfaceGL2 gl2)
            throws JCGLException,
              ConstraintError
          {
            return KFramebufferDepthGL2.newDepthFramebuffer(gl2, description);
          }

          @Override public KFramebufferDepth implementationIsGL3(
            final JCGLInterfaceGL3 gl3)
            throws JCGLException,
              ConstraintError
          {
            return KFramebufferDepthGL3ES3.newDepthFramebuffer(
              gl3,
              description);
          }

          @Override public KFramebufferDepth implementationIsGLES2(
            final JCGLInterfaceGLES2 gles2)
            throws JCGLException,
              ConstraintError
          {
            final Option<JCGLExtensionESDepthTexture> ext =
              gles2.extensionDepthTexture();
            if (ext.isSome()) {
              return KFramebufferDepthGLES2WithDepthTexture
                .newDepthFramebuffer(
                  gles2,
                  description,
                  ((Some<JCGLExtensionESDepthTexture>) ext).value);
            }
            return KFramebufferDepthGLES2WithoutDepthTexture
              .newDepthFramebuffer(gles2, description);
          }

          @Override public KFramebufferDepth implementationIsGLES3(
            final JCGLInterfaceGLES3 gl3)
            throws JCGLException,
              ConstraintError
          {
            return KFramebufferDepthGL3ES3.newDepthFramebuffer(
              gl3,
              description);
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private final @Nonnull AreaInclusive area;
  private boolean                      deleted;
  private final long                   size;

  protected KFramebufferDepth(
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
