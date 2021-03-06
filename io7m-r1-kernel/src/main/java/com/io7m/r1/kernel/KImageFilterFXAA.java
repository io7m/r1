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

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM2F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionCache;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;

/**
 * A filter implementing "Fast Approximate Anti-Aliasing".
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KImageFilterFXAA implements
  KImageFilterRGBAType<KFXAAParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-fxaa";
  }

  /**
   * Construct a new filter.
   *
   * @param gi
   *          The OpenGL implementation
   * @param in_texture_bindings
   *          A texture bindings controller
   * @param copier
   *          A region copier
   * @param quad_cache
   *          A unit quad cache
   * @param rgba_cache
   *          An RGBA cache
   * @param shader_cache
   *          A postprocessing shader cache
   * @return A new filter
   */

  public static KImageFilterRGBAType<KFXAAParameters> filterNew(
    final JCGLImplementationType gi,
    final KTextureBindingsControllerType in_texture_bindings,
    final KRegionCopierType copier,
    final KUnitQuadCacheType quad_cache,
    final KFramebufferRGBACacheType rgba_cache,
    final KShaderCacheImageType shader_cache)
  {
    return new KImageFilterFXAA(
      gi,
      in_texture_bindings,
      copier,
      quad_cache,
      rgba_cache,
      shader_cache);
  }

  private final KRegionCopierType              copier;
  private final JCGLImplementationType         gi;
  private final KUnitQuadCacheType             quad_cache;
  private final KFramebufferRGBACacheType      rgba_cache;
  private final VectorM2F                      screen;
  private final KShaderCacheImageType          shader_cache;
  private final KTextureBindingsControllerType texture_bindings;

  private KImageFilterFXAA(
    final JCGLImplementationType in_gi,
    final KTextureBindingsControllerType in_texture_bindings,
    final KRegionCopierType in_copier,
    final KUnitQuadCacheType in_quad_cache,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KShaderCacheImageType in_shader_cache)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.texture_bindings =
      NullCheck.notNull(in_texture_bindings, "Texture bindings");
    this.copier = NullCheck.notNull(in_copier, "Copier");
    this.rgba_cache =
      NullCheck.notNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.screen = new VectorM2F();
  }

  private <F extends KFramebufferRGBAUsableType> void evaluateFXAA(
    final F input,
    final KFXAAParameters config,
    final KFramebufferRGBAUsableType output)
    throws RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
    final KProgramType program = this.getProgram(config);

    this.texture_bindings
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType units)
          throws RException
        {
          try {
            gc.framebufferDrawBind(output.getRGBAColorFramebuffer());

            gc.blendingDisable();
            gc.colorBufferMask(true, true, true, true);
            gc.cullingDisable();

            if (gc.depthBufferGetBits() > 0) {
              gc.depthBufferTestDisable();
              gc.depthBufferWriteDisable();
            }

            gc.viewportSet(output.getArea());

            final JCBExecutorType e = program.getExecutable();
            e.execRun(new JCBExecutorProcedureType<RException>() {
              @Override public void call(
                final JCBProgramType p)
                throws JCGLException,
                  RException
              {
                try {
                  final KUnitQuadUsableType quad =
                    KImageFilterFXAA.this.quad_cache.cacheGetLU(Unit.unit());

                  final ArrayBufferUsableType array = quad.getArray();
                  final IndexBufferUsableType indices = quad.getIndices();

                  gc.arrayBufferBind(array);
                  KShadingProgramCommon.bindAttributePositionUnchecked(
                    p,
                    array);

                  final AreaInclusive area = output.getArea();
                  final long width = area.getRangeX().getInterval();
                  final long height = area.getRangeY().getInterval();
                  KImageFilterFXAA.this.screen.set2F(
                    1.0f / width,
                    1.0f / height);

                  p.programUniformPutVector2f(
                    "fxaa_screen_inverse",
                    KImageFilterFXAA.this.screen);
                  p.programUniformPutFloat(
                    "fxaa_subpixel",
                    config.getSubpixelAliasingRemoval());
                  p.programUniformPutFloat(
                    "fxaa_edge_threshold",
                    config.getEdgeThreshold());
                  p.programUniformPutFloat(
                    "fxaa_edge_threshold_min",
                    config.getEdgeThresholdMinimum());

                  p.programUniformPutTextureUnit(
                    "t_image",
                    units.withTexture2D(input.getRGBATexture()));
                  p
                    .programExecute(new JCBProgramProcedureType<JCGLException>() {
                      @Override public void call()
                        throws JCGLException
                      {
                        gc.drawElements(
                          Primitives.PRIMITIVE_TRIANGLES,
                          indices);
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
        }
      });
  }

  private KProgramType getProgram(
    final KFXAAParameters config)
    throws RException
  {
    switch (config.getQuality()) {
      case QUALITY_10:
      {
        return this.shader_cache.cacheGetLU("fxaa_10");
      }
      case QUALITY_15:
      {
        return this.shader_cache.cacheGetLU("fxaa_15");
      }
      case QUALITY_20:
      {
        return this.shader_cache.cacheGetLU("fxaa_20");
      }
      case QUALITY_25:
      {
        return this.shader_cache.cacheGetLU("fxaa_25");
      }
      case QUALITY_29:
      {
        return this.shader_cache.cacheGetLU("fxaa_29");
      }
      case QUALITY_39:
      {
        return this.shader_cache.cacheGetLU("fxaa_39");
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public void filterEvaluateRGBA(
    final KFXAAParameters config,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws RException
  {
    NullCheck.notNull(config, "Configuration");
    NullCheck.notNull(input, "Input");
    NullCheck.notNull(output, "Output");

    try {
      final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAUsableType> receipt =
        this.rgba_cache.bluCacheGet(input.getRGBADescription());

      try {
        final KFramebufferRGBAUsableType temp = receipt.getValue();
        this.evaluateFXAA(input, config, temp);
        this.copier.copierCopyRGBAOnly(
          temp,
          temp.getArea(),
          output,
          output.getArea());
      } finally {
        receipt.returnToCache();
      }
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public String filterGetName()
  {
    return KImageFilterFXAA.NAME;
  }

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.rgba(this);
  }
}
