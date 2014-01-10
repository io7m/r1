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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferColorAttachmentPoint;
import com.io7m.jcanephora.FramebufferDrawBuffer;
import com.io7m.jcanephora.FramebufferReference;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExtensionDepthTexture;
import com.io7m.jcanephora.JCGLExtensionESDepthTexture;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLTextures2DStaticGL3ES3;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.RenderableDepth;
import com.io7m.jcanephora.Renderbuffer;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureTypeMeta;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

public abstract class KShadowMapOld
{
  public static final class KShadowMapBasic extends KShadowMapOld
  {
    private final @Nonnull FramebufferReference framebuffer;
    private final @CheckForNull Renderbuffer<?> renderbuffer;
    private final @Nonnull Texture2DStatic      texture;

    @SuppressWarnings("synthetic-access") private KShadowMapBasic(
      final @Nonnull Texture2DStatic texture,
      final @CheckForNull Renderbuffer<?> renderbuffer,
      final @Nonnull FramebufferReference framebuffer)
      throws ConstraintError
    {
      super(KShadow.Type.SHADOW_MAPPED_BASIC);

      this.texture = Constraints.constrainNotNull(texture, "Texture");
      this.renderbuffer = renderbuffer;
      this.framebuffer =
        Constraints.constrainNotNull(framebuffer, "Framebuffer");
    }

    @Override public void mapDelete(
      final @Nonnull JCGLImplementation g)
      throws JCGLException,
        ConstraintError
    {
      final JCGLInterfaceCommon gc = g.getGLCommon();
      gc.framebufferDelete(this.framebuffer);
      gc.texture2DStaticDelete(this.texture);
      if (this.renderbuffer != null) {
        gc.renderbufferDelete(this.renderbuffer);
      }
    }

    @Override public FramebufferReferenceUsable mapGetDepthFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public @Nonnull Texture2DStaticUsable mapGetDepthTexture()
    {
      return this.texture;
    }

    @Override public long mapGetSizeBytes()
    {
      final int w = this.texture.getWidth();
      final int h = this.texture.getHeight();
      final int b = this.texture.getType().getBytesPerPixel();
      return w * h * b;
    }

    @Override public AreaInclusive mapGetDepthFramebufferArea()
    {
      return this.texture.getArea();
    }
  }

  public static final class KShadowMapSoft extends KShadowMapOld
  {
    @SuppressWarnings("synthetic-access") private KShadowMapSoft(
      final @Nonnull ShadowDepthFramebuffer depth_fb,
      final @Nonnull ShadowLightContributionFramebuffer light_fb)
      throws ConstraintError
    {
      super(KShadow.Type.SHADOW_MAPPED_SOFT);
    }

    @Override public void mapDelete(
      final @Nonnull JCGLImplementation gi)
      throws JCGLException,
        ConstraintError
    {
      // TODO:
      throw new UnimplementedCodeException();
    }

    @Override public FramebufferReferenceUsable mapGetDepthFramebuffer()
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public @Nonnull Texture2DStaticUsable mapGetDepthTexture()
    {
      // TODO:
      throw new UnimplementedCodeException();
    }

    public @Nonnull Texture2DStaticUsable mapGetLightContributionTexture()
    {
      // TODO:
      throw new UnimplementedCodeException();
    }

    @Override public long mapGetSizeBytes()
    {
      // TODO:
      throw new UnimplementedCodeException();
    }

    @Override public AreaInclusive mapGetDepthFramebufferArea()
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }
  }

  private static final class ShadowDepthFramebuffer
  {
    @SuppressWarnings("synthetic-access") public static @Nonnull
      ShadowDepthFramebuffer
      newShadowDepthFramebuffer(
        final @Nonnull JCGLImplementation gi,
        final @Nonnull AreaInclusive size,
        final @Nonnull KShadowFilter filter,
        final @Nonnull KShadowPrecision precision)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final JCGLInterfaceCommon g = gi.getGLCommon();
      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Pair<Texture2DStatic, Renderbuffer<RenderableDepth>> pair =
        KShadowMapOld.newDepthTextureRenderbufferPair(
          gi,
          width,
          height,
          filter,
          precision);
      final Texture2DStatic texture = pair.first;

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {

        /**
         * If the allocated texture is depth renderable, then the shadow depth
         * will be rendered to the depth texture and the nonexistent color
         * buffer will not be written.
         */

        if (TextureTypeMeta.isDepthRenderable(texture.getType())) {
          g.framebufferDrawAttachDepthTexture2D(fb, texture);
          KShadowMapOld.noDrawBuffer(gi, fb);
        } else {

          /**
           * Otherwise, the implementation does not support depth textures and
           * the shadow depth must be written to the color buffer.
           */

          g.framebufferDrawAttachColorTexture2D(fb, texture);
          g.framebufferDrawAttachDepthRenderbuffer(fb, pair.second);
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

      return new ShadowDepthFramebuffer(texture, pair.second, fb);
    }

    final @Nonnull FramebufferReference               framebuffer;
    final @CheckForNull Renderbuffer<RenderableDepth> renderbuffer;
    final @Nonnull Texture2DStatic                    texture;

    private ShadowDepthFramebuffer(
      final @Nonnull Texture2DStatic texture,
      final @CheckForNull Renderbuffer<RenderableDepth> renderbuffer,
      final @Nonnull FramebufferReference framebuffer)
    {
      this.texture = texture;
      this.renderbuffer = renderbuffer;
      this.framebuffer = framebuffer;
    }
  }

  private static final class ShadowLightContributionFramebuffer
  {
    @SuppressWarnings("synthetic-access") public static @Nonnull
      ShadowLightContributionFramebuffer
      newShadowLightContributionFramebuffer(
        final @Nonnull JCGLImplementation gi,
        final @Nonnull AreaInclusive size,
        final @Nonnull KShadowFilter filter,
        final @Nonnull KShadowPrecision precision)
        throws JCGLException,
          ConstraintError,
          JCGLUnsupportedException
    {
      final JCGLInterfaceCommon g = gi.getGLCommon();
      final int width = (int) size.getRangeX().getInterval();
      final int height = (int) size.getRangeY().getInterval();

      final Pair<Texture2DStatic, Renderbuffer<RenderableDepth>> pair =
        KShadowMapOld.newLightContributionTexture(
          gi,
          width,
          height,
          filter,
          precision);

      final FramebufferReference fb = g.framebufferAllocate();
      g.framebufferDrawBind(fb);

      try {
        g.framebufferDrawAttachColorTexture2D(fb, pair.first);
        g.framebufferDrawAttachDepthRenderbuffer(fb, pair.second);

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

      return new ShadowLightContributionFramebuffer(
        pair.first,
        pair.second,
        fb);
    }

    final @Nonnull FramebufferReference          framebuffer;
    final @Nonnull Renderbuffer<RenderableDepth> renderbuffer;
    final @Nonnull Texture2DStatic               texture;

    private ShadowLightContributionFramebuffer(
      final @Nonnull Texture2DStatic texture,
      final @Nonnull Renderbuffer<RenderableDepth> renderbuffer,
      final @Nonnull FramebufferReference framebuffer)
    {
      this.texture = texture;
      this.renderbuffer = renderbuffer;
      this.framebuffer = framebuffer;
    }
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMapBasic
    newBasic(
      final @Nonnull JCGLImplementation gi,
      final @Nonnull AreaInclusive size,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws JCGLUnsupportedException,
        JCGLException,
        ConstraintError
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(size, "Framebuffer size");
    Constraints.constrainNotNull(precision, "Precision");

    final ShadowDepthFramebuffer sdfb =
      ShadowDepthFramebuffer.newShadowDepthFramebuffer(
        gi,
        size,
        filter,
        precision);

    return new KShadowMapBasic(
      sdfb.texture,
      sdfb.renderbuffer,
      sdfb.framebuffer);
  }

  protected static @Nonnull Texture2DStatic newDepthTextureGL3ES3(
    final int width,
    final int height,
    final @Nonnull KShadowPrecision precision,
    final @Nonnull KShadowFilter filter,
    final @Nonnull JCGLTextures2DStaticGL3ES3 g)
    throws ConstraintError,
      JCGLException
  {
    switch (precision) {
      case SHADOW_PRECISION_16:
        return g.texture2DStaticAllocateDepth16(
          "depth-16",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
      case SHADOW_PRECISION_24:
        return g.texture2DStaticAllocateDepth24(
          "depth-24",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
      case SHADOW_PRECISION_32:
        return g.texture2DStaticAllocateDepth32f(
          "depth-32",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          filter.getMinification(),
          filter.getMagnification());
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull
    Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
    newDepthTextureRenderbufferPair(
      final @Nonnull JCGLImplementation gi,
      final int width,
      final int height,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws JCGLException,
        ConstraintError
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitor<Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>, JCGLException>() {

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGL2(
            final @Nonnull JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError
        {
          final Option<JCGLExtensionDepthTexture> e =
            gl.extensionDepthTexture();

          if (e.isSome()) {
            final JCGLExtensionDepthTexture edt =
              ((Option.Some<JCGLExtensionDepthTexture>) e).value;

            switch (precision) {
              case SHADOW_PRECISION_16:
              {
                return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
                  edt.texture2DStaticAllocateDepth16(
                    "depth-16",
                    width,
                    height,
                    TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                    TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                    filter.getMinification(),
                    filter.getMagnification()), null);
              }
              case SHADOW_PRECISION_24:
              case SHADOW_PRECISION_32:
              {
                return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
                  edt.texture2DStaticAllocateDepth24(
                    "depth-24",
                    width,
                    height,
                    TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                    TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                    filter.getMinification(),
                    filter.getMagnification()), null);
              }
            }
          }

          throw new UnreachableCodeException();
        }

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGL3(
            final @Nonnull JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError
        {
          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            KShadowMapOld.newDepthTextureGL3ES3(
              width,
              height,
              precision,
              filter,
              gl), null);
        }

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGLES2(
            final @Nonnull JCGLInterfaceGLES2 gl)
            throws JCGLException,
              ConstraintError
        {
          final Option<JCGLExtensionESDepthTexture> e =
            gl.extensionDepthTexture();

          if (e.isSome()) {
            final JCGLExtensionESDepthTexture edt =
              ((Option.Some<JCGLExtensionESDepthTexture>) e).value;

            return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
              edt.texture2DStaticAllocateDepth16(
                "depth-16",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                filter.getMinification(),
                filter.getMagnification()), null);
          }

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

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            t,
            rb);
        }

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGLES3(
            final @Nonnull JCGLInterfaceGLES3 gl)
            throws JCGLException,
              ConstraintError
        {
          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            KShadowMapOld.newDepthTextureGL3ES3(
              width,
              height,
              precision,
              filter,
              gl), null);
        }
      });
  }

  private static @Nonnull
    Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
    newLightContributionTexture(
      final @Nonnull JCGLImplementation gi,
      final int width,
      final int height,
      final @Nonnull KShadowFilter filter,
      final @Nonnull KShadowPrecision precision)
      throws JCGLException,
        ConstraintError
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitor<Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>, JCGLException>() {

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGL2(
            final @Nonnull JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError
        {
          final Renderbuffer<RenderableDepth> rb =
            gl.renderbufferAllocateDepth24(width, height);

          final Texture2DStatic t =
            gl.texture2DStaticAllocateRGBA8(
              "light-rgba8",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              filter.getMinification(),
              filter.getMagnification());

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            t,
            rb);
        }

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGL3(
            final @Nonnull JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError
        {
          final Renderbuffer<RenderableDepth> rb =
            gl.renderbufferAllocateDepth24(width, height);

          final Texture2DStatic t =
            gl.texture2DStaticAllocateR16f(
              "light-r16f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              filter.getMinification(),
              filter.getMagnification());

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            t,
            rb);
        }

        /**
         * On ES2, light contribution values in the range [0.0, 1.0] will be
         * packed into a 4444 RGBA texture.
         */

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGLES2(
            final @Nonnull JCGLInterfaceGLES2 gl)
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

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            t,
            rb);
        }

        @Override public
          Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>
          implementationIsGLES3(
            final @Nonnull JCGLInterfaceGLES3 gl)
            throws JCGLException,
              ConstraintError
        {
          final Renderbuffer<RenderableDepth> rb =
            gl.renderbufferAllocateDepth24(width, height);

          final Texture2DStatic t =
            gl.texture2DStaticAllocateR16f(
              "light-r16f",
              width,
              height,
              TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
              TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
              filter.getMinification(),
              filter.getMagnification());

          return new Pair<Texture2DStatic, Renderbuffer<RenderableDepth>>(
            t,
            rb);
        }
      });
  }

  public static @Nonnull KShadowMapOld newSoft(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull AreaInclusive size,
    final @Nonnull KShadowFilter filter,
    final @Nonnull KShadowPrecision precision)
    throws ConstraintError,
      JCGLException,
      JCGLUnsupportedException
  {
    Constraints.constrainNotNull(gi, "GL implementation");
    Constraints.constrainNotNull(size, "Framebuffer size");
    Constraints.constrainNotNull(precision, "Precision");

    final ShadowDepthFramebuffer sdfb =
      ShadowDepthFramebuffer.newShadowDepthFramebuffer(
        gi,
        size,
        filter,
        precision);

    final ShadowLightContributionFramebuffer slcfb =
      ShadowLightContributionFramebuffer
        .newShadowLightContributionFramebuffer(gi, size, filter, precision);

    return new KShadowMapSoft(sdfb, slcfb);
  }

  private static void noDrawBuffer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull FramebufferReference fb)
    throws JCGLException,
      ConstraintError
  {
    final HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint> empty =
      new HashMap<FramebufferDrawBuffer, FramebufferColorAttachmentPoint>();

    gi
      .implementationAccept(new JCGLImplementationVisitor<Unit, JCGLException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2 gl)
          throws JCGLException,
            ConstraintError
        {
          gl.framebufferDrawSetBuffers(fb, empty);
          return Unit.unit();
        }

        @Override public Unit implementationIsGL3(
          final JCGLInterfaceGL3 gl)
          throws JCGLException,
            ConstraintError
        {
          gl.framebufferDrawSetBuffers(fb, empty);
          return Unit.unit();
        }

        @Override public Unit implementationIsGLES2(
          final JCGLInterfaceGLES2 gl)
          throws JCGLException,
            ConstraintError
        {
          // TODO: ???
          return Unit.unit();
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3 gl)
          throws JCGLException,
            ConstraintError
        {
          gl.framebufferDrawSetBuffers(fb, empty);
          return Unit.unit();
        }
      });
  }

  private final @Nonnull KShadow.Type type;

  private KShadowMapOld(
    final @Nonnull KShadow.Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Shadow type");
  }

  public @Nonnull KShadow.Type getType()
  {
    return this.type;
  }

  /**
   * Delete all resources associated with the shadow map.
   */

  abstract public void mapDelete(
    final @Nonnull JCGLImplementation gi)
    throws JCGLException,
      ConstraintError;

  /**
   * Retrieve the framebuffer that will receive depth values used to calculate
   * shadows.
   */

  abstract public @Nonnull
    FramebufferReferenceUsable
    mapGetDepthFramebuffer();

  /**
   * Retrieve the size of the depth value framebuffer.
   */

  abstract public @Nonnull AreaInclusive mapGetDepthFramebufferArea();

  /**
   * Retrieve the depth texture for the shadow map.
   */

  abstract public @Nonnull Texture2DStaticUsable mapGetDepthTexture();

  /**
   * Retrieve the size in bytes of the shadow map. This is the sum of the
   * sizes of all the buffers and textures that make up the shadow map as a
   * whole.
   */

  abstract public long mapGetSizeBytes();
}
