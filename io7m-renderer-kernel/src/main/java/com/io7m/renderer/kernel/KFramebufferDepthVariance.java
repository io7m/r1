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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.types.RException;

abstract class KFramebufferDepthVariance implements
  KFramebufferDepthVarianceType
{
  private static class KFramebufferDepthVarianceGL3ES3 extends
    KFramebufferDepthVariance
  {
    static @Nonnull
      <G extends JCGLTextures2DStaticGL3ES3 & JCGLRenderbuffersGL3ES3 & JCGLFramebuffersGL3>
      KFramebufferDepthVariance
      newDepthVarianceFramebuffer(
        final @Nonnull G gl,
        final @Nonnull KFramebufferDepthVarianceDescription description,
        final boolean allow_16f,
        final boolean allow_32f)
        throws ConstraintError,
          JCGLException
    {
      assert allow_16f || allow_32f;

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

      int precision = 16;
      switch (description.getDepthVariancePrecision()) {
        case DEPTH_VARIANCE_PRECISION_16F:
        {
          if (allow_16f) {
            precision = 16;
          } else {
            precision = 32;
          }
          break;
        }
        case DEPTH_VARIANCE_PRECISION_32F:
        {
          if (allow_32f) {
            precision = 32;
          } else {
            precision = 16;
          }
          break;
        }
      }

      Texture2DStatic variance = null;
      switch (precision) {
        case 16:
        {
          variance =
            gl.texture2DStaticAllocateRG16f(
              "variance-rg16f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              description.getFilterMinification(),
              description.getFilterMagnification());
          break;
        }
        case 32:
        {
          variance =
            gl.texture2DStaticAllocateRG32f(
              "variance-rg32f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              description.getFilterMinification(),
              description.getFilterMagnification());
          break;
        }
      }

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
        gl.framebufferDrawAttachDepthTexture2D(fb, depth);
        gl.framebufferDrawAttachColorTexture2D(fb, variance);
        gl.framebufferDrawSetBuffers(fb, mappings);

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

      return new KFramebufferDepthVarianceGL3ES3(
        depth,
        variance,
        fb,
        description);
    }

    private final @Nonnull Texture2DStatic                      depth;
    private final @Nonnull KFramebufferDepthVarianceDescription description;
    private final @Nonnull FramebufferReference                 framebuffer;
    private final @Nonnull Texture2DStatic                      variance;

    public KFramebufferDepthVarianceGL3ES3(
      final @Nonnull Texture2DStatic in_depth,
      final @Nonnull Texture2DStatic in_variance,
      final @Nonnull FramebufferReference fb,
      final @Nonnull KFramebufferDepthVarianceDescription in_description)
      throws ConstraintError
    {
      super(in_depth.getArea(), in_depth.resourceGetSizeBytes()
        + in_variance.resourceGetSizeBytes());
      this.depth = in_depth;
      this.variance = in_variance;
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
        gc.texture2DStaticDelete(this.depth);
        gc.texture2DStaticDelete(this.variance);
      } catch (final JCGLRuntimeException e) {
        throw RException.fromJCGLException(e);
      } finally {
        this.setDeleted(true);
      }
    }

    @Override public
      KFramebufferDepthVarianceDescription
      kFramebufferGetDepthVarianceDescription()
    {
      return this.description;
    }

    @Override public
      FramebufferReferenceUsable
      kFramebufferGetDepthVariancePassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      Texture2DStaticUsable
      kFramebufferGetDepthVarianceTexture()
    {
      return this.variance;
    }
  }

  public static @Nonnull
    KFramebufferDepthVariance
    newDepthVarianceFramebuffer(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull KFramebufferDepthVarianceDescription description)
      throws RException,
        ConstraintError,
        JCGLException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitor<KFramebufferDepthVariance, RException>() {
        @Override public KFramebufferDepthVariance implementationIsGL2(
          final @Nonnull JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError,
            RException
        {
          throw RException.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVariance implementationIsGL3(
          final @Nonnull JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError,
            RException
        {
          return KFramebufferDepthVarianceGL3ES3.newDepthVarianceFramebuffer(
            gl,
            description,
            true,
            true);
        }

        @Override public KFramebufferDepthVariance implementationIsGLES2(
          final @Nonnull JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError,
            RException
        {
          throw RException.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVariance implementationIsGLES3(
          final @Nonnull JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError,
            RException
        {
          if (gl.hasColourBufferFloat() || gl.hasColourBufferHalfFloat()) {
            return KFramebufferDepthVarianceGL3ES3
              .newDepthVarianceFramebuffer(
                gl,
                description,
                gl.hasColourBufferHalfFloat(),
                gl.hasColourBufferFloat());
          }

          throw RException.varianceShadowMapsNotSupported();
        }
      });
  }

  private final @Nonnull AreaInclusive area;
  private boolean                      deleted;
  private final long                   size;

  protected KFramebufferDepthVariance(
    final @Nonnull AreaInclusive in_area,
    final long in_size)
    throws ConstraintError
  {
    this.area = Constraints.constrainNotNull(in_area, "Area");
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

  protected final void setDeleted(
    final boolean in_deleted)
  {
    this.deleted = in_deleted;
  }
}
