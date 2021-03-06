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

import java.util.List;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * A source that mixes two arbitrary textures.
 */

@EqualityReference public final class KImageSourceRGBAMix implements
  KImageSourceRGBAType<KTextureMixParameters>
{
  /**
   * Construct a new mixing source.
   *
   * @param gi
   *          An OpenGL interface.
   * @param quad_cache
   *          A unit quad cache.
   * @param shader_cache
   *          A postprocessing shader cache.
   *
   * @return A new filter.
   */

  public static KImageSourceRGBAType<KTextureMixParameters> sourceNew(
    final JCGLImplementationType gi,
    final KUnitQuadCacheType quad_cache,
    final KShaderCacheImageType shader_cache)
  {
    return new KImageSourceRGBAMix(gi, quad_cache, shader_cache);
  }

  private final JCGLImplementationType                             gi;
  private final KUnitQuadCacheType                                 quad_cache;
  private final KShaderCacheImageType                              shader_cache;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv;

  private KImageSourceRGBAMix(
    final JCGLImplementationType in_gi,
    final KUnitQuadCacheType in_quad_cache,
    final KShaderCacheImageType in_shader_cache)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.uv = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
  }

  private void evaluate(
    final JCGLInterfaceCommonType gc,
    final KTextureMixParameters config,
    final JCBProgramType p)
    throws RException
  {
    try {
      final List<TextureUnitType> units = gc.textureGetUnits();

      final KUnitQuadUsableType quad =
        KImageSourceRGBAMix.this.quad_cache.cacheGetLU(Unit.unit());

      final ArrayBufferUsableType array = quad.getArray();
      final IndexBufferUsableType indices = quad.getIndices();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
      KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

      final TextureUnitType texture_0 = units.get(0);
      assert texture_0 != null;
      final TextureUnitType texture_1 = units.get(1);
      assert texture_1 != null;

      gc.texture2DStaticBind(texture_0, config.getLeftTexture());
      gc.texture2DStaticBind(texture_1, config.getRightTexture());

      try {
        final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> m_uv =
          KImageSourceRGBAMix.this.uv;

        config.getLeftMatrix().makeMatrixM3x3F(m_uv);
        p.programUniformPutMatrix3x3f("m_uv_0", m_uv);
        config.getRightMatrix().makeMatrixM3x3F(m_uv);
        p.programUniformPutMatrix3x3f("m_uv_1", m_uv);

        p.programUniformPutFloat("alpha", config.getMix());

        p.programUniformPutTextureUnit("t_image_0", texture_0);
        p.programUniformPutTextureUnit("t_image_1", texture_1);
        p.programExecute(new JCBProgramProcedureType<JCGLException>() {
          @Override public void call()
            throws JCGLException
          {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          }
        });
      } finally {
        gc.texture2DStaticUnbind(texture_0);
        gc.texture2DStaticUnbind(texture_1);
      }

    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.arrayBufferUnbind();
    }
  }

  @Override public <A, E extends Throwable> A sourceAccept(
    final KImageSourceVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.rgba(this);
  }

  @Override public void sourceEvaluateRGBA(
    final KTextureMixParameters config,
    final KFramebufferRGBAUsableType output)
    throws RException
  {
    NullCheck.notNull(config, "Configuration");
    NullCheck.notNull(output, "Output");

    try {
      final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
      final KProgramType p = this.shader_cache.cacheGetLU("mix");

      try {
        gc.framebufferDrawBind(output.getRGBAColorFramebuffer());

        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();
        gc.viewportSet(output.getArea());

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        if (gc.stencilBufferGetBits() > 0) {
          gc.stencilBufferDisable();
          gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
        }

        final JCBExecutorType e = p.getExecutable();
        e.execRun(new JCBExecutorProcedureType<RException>() {
          @SuppressWarnings("synthetic-access") @Override public void call(
            final JCBProgramType jp)
            throws JCGLException,
              RException
          {
            KImageSourceRGBAMix.this.evaluate(gc, config, jp);
          }
        });
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public String sourceGetName()
  {
    return "source-mix";
  }
}
