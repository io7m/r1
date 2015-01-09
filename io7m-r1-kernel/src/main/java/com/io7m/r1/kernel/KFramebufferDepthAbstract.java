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
import java.util.Map;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPointType;
import com.io7m.jcanephora.FramebufferDrawBufferType;
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
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.r1.kernel.types.KFramebufferDepthDescription;
import com.io7m.r1.types.RException;

@EqualityReference abstract class KFramebufferDepthAbstract implements
  KFramebufferDepthType
{
  @EqualityReference static final class KFramebufferDepthGL2 extends
    KFramebufferDepthAbstract
  {
    public static KFramebufferDepthAbstract newDepthFramebuffer(
      final JCGLInterfaceGL2Type gl,
      final KFramebufferDepthDescription description)
      throws JCGLException
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType depth =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "depth-24s8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          description.getFilterMinification(),
          description.getFilterMagnification());

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> empty =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();

      final JCGLFramebufferBuilderGL3ES3Type fbb =
        gl.framebufferNewBuilderGL3ES3();
      fbb.attachDepthTexture2D(depth);
      fbb.setDrawBuffers(empty);

      final FramebufferType fb = gl.framebufferAllocate(fbb);
      return new KFramebufferDepthGL2(depth, fb, description);
    }

    private final Texture2DStaticType          depth;
    private final KFramebufferDepthDescription description;
    private final FramebufferType              framebuffer;

    private KFramebufferDepthGL2(
      final Texture2DStaticType in_depth,
      final FramebufferType in_framebuffer,
      final KFramebufferDepthDescription in_description)
    {
      super(in_depth.textureGetArea(), in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.framebuffer = in_framebuffer;
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
  }

  @EqualityReference static final class KFramebufferDepthGL3ES3 extends
    KFramebufferDepthAbstract
  {
    public static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type>
      KFramebufferDepthAbstract
      newDepthFramebuffer(
        final G gl,
        final KFramebufferDepthDescription description)
        throws JCGLException
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

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> empty =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();

      final JCGLFramebufferBuilderGL3ES3Type fbb =
        gl.framebufferNewBuilderGL3ES3();
      fbb.attachDepthTexture2D(depth);
      fbb.setDrawBuffers(empty);

      final FramebufferType fb = gl.framebufferAllocate(fbb);
      return new KFramebufferDepthGL3ES3(depth, fb, description);
    }

    private final Texture2DStaticType          depth;
    private final KFramebufferDepthDescription description;
    private final FramebufferType              framebuffer;

    private KFramebufferDepthGL3ES3(
      final Texture2DStaticType in_depth,
      final FramebufferType in_framebuffer,
      final KFramebufferDepthDescription d)
    {
      super(in_depth.textureGetArea(), in_depth.resourceGetSizeBytes());
      this.depth = in_depth;
      this.framebuffer = in_framebuffer;
      this.description = d;
    }

    @Override public void kFramebufferDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.depth);
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
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferDepthAbstract(
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
