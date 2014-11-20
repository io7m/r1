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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The default implementation of a depth-displaying filter.
 */

@EqualityReference public final class KImageFilterDepthDisplay implements
  KImageFilterDepthRGBAType<Unit>
{
  private static final String NAME;

  static {
    NAME = "filter-depth-display";
  }

  /**
   * Construct a new emission filter.
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

  public static KImageFilterDepthDisplay filterNew(
    final JCGLImplementationType gi,
    final KUnitQuadCacheType quad_cache,
    final KShaderCacheImageType shader_cache)
  {
    return new KImageFilterDepthDisplay(gi, quad_cache, shader_cache);
  }

  private final JCGLImplementationType              gi;
  private final KUnitQuadCacheType                  quad_cache;
  private final KShaderCacheImageType               shader_cache;
  private final RMatrixM3x3F<RTransformTextureType> uv;

  private KImageFilterDepthDisplay(
    final JCGLImplementationType in_gi,
    final KUnitQuadCacheType in_quad_cache,
    final KShaderCacheImageType in_shader_cache)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.uv = new RMatrixM3x3F<RTransformTextureType>();
  }

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.depthRGBA(this);
  }

  @Override public void filterEvaluateDepthRGBA(
    final Unit config,
    final KFramebufferDepthUsableType input,
    final KFramebufferRGBAUsableType output)
    throws RException
  {
    NullCheck.notNull(config, "Config");
    NullCheck.notNull(input, "Input");
    NullCheck.notNull(output, "Output");

    try {
      final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
      final List<TextureUnitType> units = gc.textureGetUnits();

      final KProgramType copy =
        this.shader_cache.cacheGetLU("copy_depth_to_rgba");

      try {
        gc.framebufferDrawBind(output.rgbaGetColorFramebuffer());

        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();
        gc.viewportSet(output.kFramebufferGetArea());

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        final JCBExecutorType e = copy.getExecutable();
        e.execRun(new JCBExecutorProcedureType<RException>() {
          @SuppressWarnings("synthetic-access") @Override public void call(
            final JCBProgramType p)
            throws JCGLException,
              RException
          {
            try {
              final KUnitQuadUsableType quad =
                KImageFilterDepthDisplay.this.quad_cache.cacheGetLU(Unit
                  .unit());

              final ArrayBufferUsableType array = quad.getArray();
              final IndexBufferUsableType indices = quad.getIndices();

              gc.arrayBufferBind(array);
              KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
              KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

              final TextureUnitType depth_unit = units.get(0);
              assert depth_unit != null;

              gc.texture2DStaticBind(
                depth_unit,
                input.kFramebufferGetDepthTexture());

              p.programUniformPutMatrix3x3f(
                "m_uv",
                KImageFilterDepthDisplay.this.uv);
              p.programUniformPutTextureUnit("t_image_depth", depth_unit);
              p.programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                }
              });

            } catch (final JCacheException x) {
              throw new UnreachableCodeException(x);
            } finally {
              gc.arrayBufferUnbind();
            }
          }
        });
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String filterGetName()
  {
    return KImageFilterDepthDisplay.NAME;
  }

}
