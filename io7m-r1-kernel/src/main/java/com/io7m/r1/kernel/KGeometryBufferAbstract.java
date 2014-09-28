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
import java.util.List;
import java.util.Map;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FramebufferColorAttachmentPointType;
import com.io7m.jcanephora.FramebufferDrawBufferType;
import com.io7m.jcanephora.FramebufferStatus;
import com.io7m.jcanephora.FramebufferType;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionUnsupported;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLColorBufferType;
import com.io7m.jcanephora.api.JCGLDepthBufferType;
import com.io7m.jcanephora.api.JCGLFramebuffersCommonType;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.api.JCGLStencilBufferType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticGL3ES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RExceptionNotSupported;

@EqualityReference abstract class KGeometryBufferAbstract implements
  KGeometryBufferType
{
  @EqualityReference private static final class KGeometryBuffer_GL3ES3 extends
    KGeometryBufferAbstract
  {
    public static
      <G extends JCGLTextures2DStaticGL3ES3Type & JCGLFramebuffersGL3Type>
      KGeometryBufferAbstract
      newRGBA(
        final G gl,
        final AreaInclusive area)
        throws JCGLException
    {
      final int width = (int) area.getRangeX().getInterval();
      final int height = (int) area.getRangeY().getInterval();

      final Texture2DStaticType depth =
        gl.texture2DStaticAllocateDepth24Stencil8(
          "gbuffer-d24s8",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Texture2DStaticType albedo =
        gl.texture2DStaticAllocateRGBA8(
          "gbuffer-albedo",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final Texture2DStaticType normal =
        gl.texture2DStaticAllocateRG16f(
          "gbuffer-normal",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

      final Texture2DStaticType specular =
        gl.texture2DStaticAllocateRGBA8(
          "gbuffer-specular",
          width,
          height,
          TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final List<FramebufferColorAttachmentPointType> points =
        gl.framebufferGetColorAttachmentPoints();
      final FramebufferColorAttachmentPointType attach_0 = points.get(0);
      final FramebufferColorAttachmentPointType attach_1 = points.get(1);
      final FramebufferColorAttachmentPointType attach_2 = points.get(2);
      assert attach_0 != null;
      assert attach_1 != null;
      assert attach_2 != null;

      final List<FramebufferDrawBufferType> buffers =
        gl.framebufferGetDrawBuffers();

      final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> mappings =
        new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();
      mappings.put(buffers.get(0), attach_0);
      mappings.put(buffers.get(1), attach_1);
      mappings.put(buffers.get(2), attach_2);

      final FramebufferType fb = gl.framebufferAllocate();
      gl.framebufferDrawBind(fb);

      try {
        gl.framebufferDrawAttachDepthStencilTexture2D(fb, depth);
        gl.framebufferDrawAttachColorTexture2DAt(fb, attach_0, albedo);
        gl.framebufferDrawAttachColorTexture2DAt(fb, attach_1, normal);
        gl.framebufferDrawAttachColorTexture2DAt(fb, attach_2, specular);
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
            throw new JCGLExceptionUnsupported(
              "Could not initialize framebuffer: " + status);
        }

      } finally {
        gl.framebufferDrawUnbind();
      }

      return new KGeometryBuffer_GL3ES3(depth, albedo, normal, specular, fb);
    }

    private final Texture2DStaticType albedo;
    private final Texture2DStaticType depth;
    private final FramebufferType     fb;
    private final Texture2DStaticType normal;
    private final Texture2DStaticType specular;

    private KGeometryBuffer_GL3ES3(
      final Texture2DStaticType in_depth,
      final Texture2DStaticType in_albedo,
      final Texture2DStaticType in_normal,
      final Texture2DStaticType in_specular,
      final FramebufferType in_fb)
    {
      this.depth = NullCheck.notNull(in_depth, "Depth");
      this.albedo = NullCheck.notNull(in_albedo, "Albedo");
      this.normal = NullCheck.notNull(in_normal, "Normal");
      this.specular = NullCheck.notNull(in_specular, "Specular");
      this.fb = NullCheck.notNull(in_fb, "Framebuffer");
    }

    @Override public void geomDelete(
      final JCGLImplementationType g)
      throws RException
    {
      try {
        final JCGLInterfaceCommonType gc = g.getGLCommon();
        gc.framebufferDelete(this.fb);
        gc.texture2DStaticDelete(this.albedo);
        gc.texture2DStaticDelete(this.depth);
        gc.texture2DStaticDelete(this.normal);
        gc.texture2DStaticDelete(this.specular);
      } catch (final JCGLException e) {
        throw RExceptionJCGL.fromJCGLException(e);
      } finally {
        super.setDeleted(true);
      }
    }

    @Override public FramebufferUsableType geomGetFramebuffer()
    {
      return this.fb;
    }

    @Override public Texture2DStaticUsableType geomGetTextureAlbedo()
    {
      return this.albedo;
    }

    @Override public Texture2DStaticUsableType geomGetTextureDepthStencil()
    {
      return this.depth;
    }

    @Override public Texture2DStaticUsableType geomGetTextureNormal()
    {
      return this.normal;
    }

    @Override public Texture2DStaticUsableType geomGetTextureSpecular()
    {
      return this.specular;
    }

    @Override public
      <G extends JCGLColorBufferType & JCGLDepthBufferType & JCGLStencilBufferType & JCGLFramebuffersCommonType>
      void
      geomClear(
        final G gc)
        throws RException
    {
      try {
        try {
          gc.framebufferDrawBind(this.fb);
          gc.colorBufferMask(true, true, true, true);
          gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);
          gc.depthBufferWriteEnable();
          gc.depthBufferClear(1.0f);
          gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0xffffffff);
          gc.stencilBufferClear(0);
        } finally {
          gc.framebufferDrawUnbind();
        }
      } catch (final JCGLException e) {
        throw RExceptionJCGL.fromJCGLException(e);
      }
    }
  }

  static KGeometryBufferAbstract newRGBA(
    final JCGLImplementationType gi,
    final AreaInclusive area)
    throws JCGLException,
      RException
  {
    return gi
      .implementationAccept(new JCGLImplementationVisitorType<KGeometryBufferAbstract, RException>() {
        @Override public KGeometryBufferAbstract implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.deferredRenderingNotSupported();
        }

        @Override public KGeometryBufferAbstract implementationIsGL3(
          final JCGLInterfaceGL3Type gl)
          throws JCGLException
        {
          return KGeometryBuffer_GL3ES3.newRGBA(gl, area);
        }

        @Override public KGeometryBufferAbstract implementationIsGLES2(
          final JCGLInterfaceGLES2Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          throw RExceptionNotSupported.deferredRenderingNotSupported();
        }

        @Override public KGeometryBufferAbstract implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,
            RExceptionNotSupported
        {
          if (gl.hasColorBufferFloat() || gl.hasColorBufferHalfFloat()) {
            return KGeometryBuffer_GL3ES3.newRGBA(gl, area);
          }
          throw RExceptionNotSupported.deferredRenderingNotSupported();
        }
      });
  }

  private boolean deleted;

  public final boolean isDeleted()
  {
    return this.deleted;
  }

  final void setDeleted(
    final boolean b)
  {
    this.deleted = b;
  }
}
