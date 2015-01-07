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
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.r1.kernel.types.KFramebufferDistanceDescription;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionNotSupported;

/**
 * The default implementation of distance framebuffers.
 */

@SuppressWarnings("synthetic-access") public final class KFramebufferDistance implements
  KFramebufferDistanceType
{
  private static
    <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type>
    KFramebufferDistanceType
    make(
      final KFramebufferDistanceDescription description,
      final G gl)
  {
    final AreaInclusive area = description.getArea();
    final int width = (int) area.getRangeX().getInterval();
    final int height = (int) area.getRangeY().getInterval();

    Texture2DStaticType depth = null;
    switch (description.getDepthPrecision()) {
      case DEPTH_PRECISION_16:
      {
        depth =
          gl.texture2DStaticAllocateDepth16(
            "distance-depth-16",
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
            "distance-depth-24",
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
            "distance-depth-32f",
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

    Texture2DStaticType distance = null;
    switch (description.getDistancePrecision()) {
      case DISTANCE_PRECISION_16:
      {
        distance =
          gl.texture2DStaticAllocateR16f(
            "distance-16",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        break;
      }
      case DISTANCE_PRECISION_32:
      {
        distance =
          gl.texture2DStaticAllocateR32f(
            "distance-32",
            width,
            height,
            TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
            TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
            description.getFilterMinification(),
            description.getFilterMagnification());
        break;
      }
    }
    assert distance != null;

    final JCGLFramebufferBuilderGL3ES3Type fbb =
      gl.framebufferNewBuilderGL3ES3();
    fbb.attachDepthTexture2D(depth);
    fbb.attachColorTexture2D(distance);

    final FramebufferType fb = gl.framebufferAllocate(fbb);
    return new KFramebufferDistance(depth, distance, fb, description);
  }

  /**
   * Construct a new distance framebuffer.
   *
   * @param in_gi
   *          An OpenGL implementation
   * @param description
   *          A distance framebuffer description
   * @return A new framebuffer
   * @throws RException
   *           On errors
   */

  public static KFramebufferDistanceType newDistanceFramebuffer(
    final JCGLImplementationType in_gi,
    final KFramebufferDistanceDescription description)
    throws RException
  {
    return in_gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferDistanceType, RException>() {
        @Override public KFramebufferDistanceType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public KFramebufferDistanceType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws RException
        {
          return KFramebufferDistance.make(description, gl);
        }

        @Override public KFramebufferDistanceType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws RExceptionNotSupported
        {
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public KFramebufferDistanceType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
        {
          return KFramebufferDistance.make(description, gl);
        }
      });
  }

  private final AreaInclusive                   area;
  private boolean                               deleted;
  private final Texture2DStaticType             depth;
  private final KFramebufferDistanceDescription description;
  private final Texture2DStaticType             distance;
  private final FramebufferType                 framebuffer;
  private final long                            size;

  private KFramebufferDistance(
    final Texture2DStaticType in_depth,
    final Texture2DStaticType in_distance,
    final FramebufferType in_framebuffer,
    final KFramebufferDistanceDescription d)
  {
    final long s =
      in_depth.resourceGetSizeBytes() + in_distance.resourceGetSizeBytes();

    this.area = in_depth.textureGetArea();
    this.size = s;
    this.deleted = false;

    this.depth = in_depth;
    this.distance = in_distance;
    this.framebuffer = in_framebuffer;
    this.description = d;
  }

  @Override public AreaInclusive getArea()
  {
    return this.area;
  }

  @Override public Texture2DStaticUsableType getDistanceDepthTexture()
  {
    return this.depth;
  }

  @Override public KFramebufferDistanceDescription getDistanceDescription()
  {
    return this.description;
  }

  @Override public FramebufferUsableType getDistancePassFramebuffer()
  {
    return this.framebuffer;
  }

  @Override public Texture2DStaticUsableType getDistanceTexture()
  {
    return this.distance;
  }

  @Override public long getSizeBytes()
  {
    return this.size;
  }

  @Override public void deleteFramebuffer(
    final JCGLImplementationType g)
    throws RException
  {
    try {
      final JCGLInterfaceCommonType gc = g.getGLCommon();
      gc.texture2DStaticDelete(this.depth);
      gc.texture2DStaticDelete(this.distance);
      gc.framebufferDelete(this.framebuffer);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } finally {
      this.setDeleted(true);
    }
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  protected void setDeleted(
    final boolean in_deleted)
  {
    this.deleted = in_deleted;
  }
}
