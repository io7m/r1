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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferType;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionRuntime;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLFramebufferBuilderGL3ES3Type;
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
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionNotSupported;

@EqualityReference abstract class KFramebufferDepthVariance implements
  KFramebufferDepthVarianceType
{
  @EqualityReference private static class KFramebufferDepthVarianceGL3ES3 extends
    KFramebufferDepthVariance
  {
    private static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLRenderbuffersGL3ES3Type & JCGLFramebuffersGL3Type>
      Texture2DStaticType
      allocateDepthTexture(
        final G gl,
        final KFramebufferDepthVarianceDescription description,
        final int width,
        final int height)
        throws JCGLExceptionRuntime
    {
      switch (description.getDepthPrecision()) {
        case DEPTH_PRECISION_16:
        {
          return gl.texture2DStaticAllocateDepth16(
            "depth-16",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        }
        case DEPTH_PRECISION_24:
        {
          return gl.texture2DStaticAllocateDepth24(
            "depth-24",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        }
        case DEPTH_PRECISION_32F:
        {
          return gl.texture2DStaticAllocateDepth32f(
            "depth-32f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        }
      }

      throw new UnreachableCodeException();
    }

    private static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLRenderbuffersGL3ES3Type & JCGLFramebuffersGL3Type>
      Texture2DStaticType
      allocateVariance(
        final G gl,
        final KFramebufferDepthVarianceDescription description,
        final int width,
        final int height,
        final int precision)
        throws JCGLExceptionRuntime
    {
      switch (precision) {
        case 16:
        {
          return gl.texture2DStaticAllocateRG16f(
            "variance-rg16f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        }
        case 32:
        {
          return gl.texture2DStaticAllocateRG32f(
            "variance-rg32f",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        }
      }

      throw new UnreachableCodeException();
    }

    static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLRenderbuffersGL3ES3Type & JCGLFramebuffersGL3Type>
      KFramebufferDepthVariance
      newDepthVarianceFramebuffer(
        final G gl,
        final KFramebufferDepthVarianceDescription description,
        final boolean allow_16f,
        final boolean allow_32f)
        throws JCGLException
    {
      assert allow_16f || allow_32f;

      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType depth =
        KFramebufferDepthVarianceGL3ES3.allocateDepthTexture(
          gl,
          description,
          width,
          height);

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

      final Texture2DStaticType variance =
        KFramebufferDepthVarianceGL3ES3.allocateVariance(
          gl,
          description,
          width,
          height,
          precision);

      final JCGLFramebufferBuilderGL3ES3Type fbb =
        gl.framebufferNewBuilderGL3ES3();
      fbb.attachDepthTexture2D(depth);
      fbb.attachColorTexture2D(variance);
      final FramebufferType fb = gl.framebufferAllocate(fbb);

      return new KFramebufferDepthVarianceGL3ES3(
        depth,
        variance,
        fb,
        description);
    }

    private final Texture2DStaticType                  depth;
    private final KFramebufferDepthVarianceDescription description;
    private final FramebufferType                      framebuffer;
    private final Texture2DStaticType                  variance;

    public KFramebufferDepthVarianceGL3ES3(
      final Texture2DStaticType in_depth,
      final Texture2DStaticType in_variance,
      final FramebufferType fb,
      final KFramebufferDepthVarianceDescription in_description)
    {
      super(in_depth.textureGetArea(), in_depth.resourceGetSizeBytes()
        + in_variance.resourceGetSizeBytes());
      this.depth = in_depth;
      this.variance = in_variance;
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
        gc.texture2DStaticDelete(this.depth);
        gc.texture2DStaticDelete(this.variance);
      } catch (final JCGLException e) {
        throw RExceptionJCGL.fromJCGLException(e);
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
      FramebufferUsableType
      kFramebufferGetDepthVariancePassFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public
      Texture2DStaticUsableType
      kFramebufferGetDepthVarianceTexture()
    {
      return this.variance;
    }
  }

  public static KFramebufferDepthVariance newDepthVarianceFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferDepthVarianceDescription description)
    throws RException,
      JCGLException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferDepthVariance, RException>() {
        @Override public KFramebufferDepthVariance implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVariance implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException,
            RException
        {
          return KFramebufferDepthVarianceGL3ES3.newDepthVarianceFramebuffer(
            gl,
            description,
            true,
            true);
        }

        @Override public KFramebufferDepthVariance implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException,
            RException
        {
          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }

        @Override public KFramebufferDepthVariance implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,
            RException
        {
          if (gl.hasColorBufferFloat() || gl.hasColorBufferHalfFloat()) {
            return KFramebufferDepthVarianceGL3ES3
              .newDepthVarianceFramebuffer(
                gl,
                description,
                gl.hasColorBufferHalfFloat(),
                gl.hasColorBufferFloat());
          }

          throw RExceptionNotSupported.varianceShadowMapsNotSupported();
        }
      });
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferDepthVariance(
    final AreaInclusive in_area,
    final long in_size)
  {
    this.area = NullCheck.notNull(in_area, "Area");
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

  protected final void setDeleted(
    final boolean in_deleted)
  {
    this.deleted = in_deleted;
  }
}
