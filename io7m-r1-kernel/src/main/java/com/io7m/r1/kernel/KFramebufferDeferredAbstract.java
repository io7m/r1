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
import com.io7m.jcanephora.ClearSpecification;
import com.io7m.jcanephora.ClearSpecificationBuilderType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FramebufferType;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLClearType;
import com.io7m.jcanephora.api.JCGLColorBufferType;
import com.io7m.jcanephora.api.JCGLDepthBufferType;
import com.io7m.jcanephora.api.JCGLFramebufferBuilderGL3ES3Type;
import com.io7m.jcanephora.api.JCGLFramebuffersCommonType;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.api.JCGLRenderbuffersGL3ES3Type;
import com.io7m.jcanephora.api.JCGLStencilBufferType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionNotSupported;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;

/**
 * Provides the base implementation for {@link KFramebufferDeferred}.
 */

@EqualityReference abstract class KFramebufferDeferredAbstract implements
  KFramebufferDeferredType
{
  static final ClearSpecification CLEAR_SPEC;

  static {
    final ClearSpecificationBuilderType b = ClearSpecification.newBuilder();
    b.enableColorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);
    b.enableDepthBufferClear(1.0f);
    b.enableStencilBufferClear(0);
    b.setStrictChecking(true);
    CLEAR_SPEC = b.build();
  }

  @EqualityReference private static final class KFramebufferDeferredGL3ES3 extends
    KFramebufferDeferredAbstract
  {
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
            "render-color-16f",
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
            "render-color-32f",
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
            "render-color-8888",
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
      KFramebufferDeferredType
      newFramebuffer(
        final G gl,
        final KFramebufferDeferredDescription description,
        final KGeometryBufferType gbuffer)
        throws JCGLException
    {
      final AreaInclusive area = description.getArea();
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType c =
        KFramebufferDeferredGL3ES3.makeRGBA(
          description.getRGBADescription(),
          width,
          height,
          gl);

      final Texture2DStaticType d =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "render-d24s8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final JCGLFramebufferBuilderGL3ES3Type fbb =
        gl.framebufferNewBuilderGL3ES3();
      fbb.attachColorTexture2D(c);
      fbb.attachDepthStencilTexture2D(d);

      final FramebufferType fb = gl.framebufferAllocate(fbb);
      return new KFramebufferDeferredGL3ES3(c, d, fb, description, gbuffer);
    }

    private final Texture2DStaticType             color;
    private final Texture2DStaticType             depth;
    private final KFramebufferDeferredDescription description;
    private final FramebufferType                 framebuffer;
    private final KGeometryBufferType             gbuffer;

    public KFramebufferDeferredGL3ES3(
      final Texture2DStaticType c,
      final Texture2DStaticType d,
      final FramebufferType fb,
      final KFramebufferDeferredDescription in_description,
      final KGeometryBufferType in_gbuffer)
    {
      super(c.textureGetArea(), c.resourceGetSizeBytes()
        + d.resourceGetSizeBytes());
      this.color = c;
      this.depth = d;
      this.framebuffer = fb;
      this.description = in_description;
      this.gbuffer = in_gbuffer;
    }

    @Override public void delete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.framebuffer);
        gc.texture2DStaticDelete(this.color);
        gc.texture2DStaticDelete(this.depth);
        this.gbuffer.geomDelete(g);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType getRGBAColorFramebuffer()
    {
      return this.framebuffer;
    }

    @Override public KGeometryBufferUsableType deferredGetGeometryBuffer()
    {
      return this.gbuffer;
    }

    @Override public KFramebufferRGBADescription getRGBADescription()
    {
      return this.description.getRGBADescription();
    }

    @Override public Texture2DStaticUsableType getRGBATexture()
    {
      return this.color;
    }

    @Override public
      <G extends JCGLColorBufferType & JCGLClearType & JCGLDepthBufferType & JCGLStencilBufferType & JCGLFramebuffersCommonType>
      void
      deferredFramebufferClear(
        final G in_gc,
        final VectorReadable4FType in_color,
        final float in_depth,
        final int in_stencil)
        throws RException
    {
      NullCheck.notNull(in_gc, "OpenGL");
      NullCheck.notNull(in_color, "Color");

      try {
        this.gbuffer.geomClear(in_gc);
        in_gc.framebufferDrawBind(this.framebuffer);
        in_gc.colorBufferMask(true, true, true, true);
        in_gc.depthBufferWriteEnable();
        in_gc
          .stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0xffffffff);
        in_gc.clear(KGeometryBufferAbstract.CLEAR_SPEC);
      } finally {
        in_gc.framebufferDrawUnbind();
      }
    }
  }

  static KFramebufferDeferredType newFramebuffer(
    final JCGLImplementationType gi,
    final KFramebufferDeferredDescription description)
    throws JCGLException,
      RException
  {
    NullCheck.notNull(gi, "GL implementation");
    NullCheck.notNull(description, "Description");

    final KGeometryBufferType gbuffer =
      KGeometryBuffer.newGeometryBuffer(
        gi,
        description.getGeometryBufferDescription());

    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KFramebufferDeferredType, RException>() {
        @Override public KFramebufferDeferredType implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.deferredRenderingNotSupported();
        }

        @Override public KFramebufferDeferredType implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return KFramebufferDeferredAbstract.KFramebufferDeferredGL3ES3
            .newFramebuffer(gl, description, gbuffer);
        }

        @Override public KFramebufferDeferredType implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.deferredRenderingNotSupported();
        }

        @Override public KFramebufferDeferredType implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException
        {
          return KFramebufferDeferredAbstract.KFramebufferDeferredGL3ES3
            .newFramebuffer(gl, description, gbuffer);
        }
      });
  }

  private final AreaInclusive area;
  private boolean             deleted;
  private final long          size;

  protected KFramebufferDeferredAbstract(
    final AreaInclusive in_area,
    final long in_size)
  {
    this.deleted = false;
    this.area = in_area;
    this.size = in_size;
  }

  @Override public final AreaInclusive getArea()
  {
    return this.area;
  }

  @Override public final long getSizeInBytes()
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
