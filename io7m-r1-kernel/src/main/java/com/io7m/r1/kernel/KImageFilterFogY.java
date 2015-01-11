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

import com.io7m.jcache.BLUCacheReceiptType;
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
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KUnitQuadUsableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * A fog filter based on the global Y axis.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KImageFilterFogY implements
  KImageFilterDeferredType<KFogYParameters>
{
  private static final String NAME;

  static {
    NAME = "filter-fog-y";
  }

  /**
   * Construct a new fog filter.
   *
   * @param gi
   *          The OpenGL implementation
   * @param copier
   *          A region copier
   * @param quad_cache
   *          A unit quad cache
   * @param rgba_cache
   *          An RGBA cache
   * @param shader_cache
   *          An image shader cache
   * @param in_ray_cache
   *          A view ray cache
   * @return A new filter
   */

  public static KImageFilterFogY filterNew(
    final JCGLImplementationType gi,
    final KRegionCopierType copier,
    final KUnitQuadCacheType quad_cache,
    final KFramebufferRGBACacheType rgba_cache,
    final KViewRaysCacheType in_ray_cache,
    final KShaderCacheImageType shader_cache)
  {
    return new KImageFilterFogY(
      gi,
      copier,
      quad_cache,
      rgba_cache,
      in_ray_cache,
      shader_cache);
  }

  private final KRegionCopierType                                  copier;
  private final JCGLImplementationType                             gi;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> matrix_uv;
  private final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>        projection;
  private final KUnitQuadCacheType                                 quad_cache;
  private final KFramebufferRGBACacheType                          rgba_cache;
  private final KShaderCacheImageType                              shader_cache;
  private final PMatrixM4x4F<RSpaceEyeType, RSpaceWorldType>       view_inverse;
  private final KViewRaysCacheType                                 ray_cache;
  private final PMatrixM4x4F.Context                               context;

  private KImageFilterFogY(
    final JCGLImplementationType in_gi,
    final KRegionCopierType in_copier,
    final KUnitQuadCacheType in_quad_cache,
    final KFramebufferRGBACacheType in_rgba_cache,
    final KViewRaysCacheType in_ray_cache,
    final KShaderCacheImageType in_shader_cache)
  {
    this.gi = NullCheck.notNull(in_gi, "GL implementation");
    this.copier = NullCheck.notNull(in_copier, "Copier");
    this.rgba_cache =
      NullCheck.notNull(in_rgba_cache, "RGBA framebuffer cache");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
    this.matrix_uv = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
    this.projection = new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
    this.view_inverse = new PMatrixM4x4F<RSpaceEyeType, RSpaceWorldType>();
    this.ray_cache = NullCheck.notNull(in_ray_cache, "Ray cache");
    this.context = new PMatrixM4x4F.Context();
  }

  private void evaluateFog(
    final KFogYParameters config,
    final KGeometryBufferUsableType gbuffer,
    final KFramebufferRGBAUsableType input,
    final KFramebufferRGBAUsableType output)
    throws JCGLException,
      RException,
      JCacheException
  {
    @SuppressWarnings("unchecked") final PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType> vi_temp =
      (PMatrixM4x4F<RSpaceWorldType, RSpaceEyeType>) (PMatrixM4x4F<?, ?>) this.view_inverse;

    config.getView().makeMatrixM4x4F(vi_temp);
    PMatrixM4x4F.invertWithContext(this.context, vi_temp, this.view_inverse);

    final KProjectionType c_proj = config.getProjection();
    final KViewRays vr = this.ray_cache.cacheGetLU(c_proj);
    c_proj.projectionGetMatrix().makeMatrixM4x4F(this.projection);

    final JCGLInterfaceCommonType gc = this.gi.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KProgramType fog = this.getFogProgram(config);

    try {
      gc.framebufferDrawBind(output.getRGBAColorFramebuffer());

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingDisable();
      gc.viewportSet(output.kFramebufferGetArea());

      if (gc.depthBufferGetBits() > 0) {
        gc.depthBufferTestDisable();
        gc.depthBufferWriteDisable();
      }

      if (gc.stencilBufferGetBits() > 0) {
        gc.stencilBufferDisable();
        gc.stencilBufferMask(FaceSelection.FACE_FRONT_AND_BACK, 0);
      }

      final JCBExecutorType e = fog.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          try {
            final KUnitQuadUsableType quad =
              KImageFilterFogY.this.quad_cache.cacheGetLU(Unit.unit());

            final ArrayBufferUsableType array = quad.getArray();
            final IndexBufferUsableType indices = quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
            KShadingProgramCommon.bindAttributeUVUnchecked(p, array);

            final TextureUnitType t_image = units.get(0);
            assert t_image != null;
            final TextureUnitType t_depth = units.get(1);
            assert t_depth != null;

            try {
              gc.texture2DStaticBind(t_image, input.getRGBATexture());
              gc.texture2DStaticBind(
                t_depth,
                gbuffer.geomGetTextureDepthStencil());

              p.programUniformPutTextureUnit("t_map_depth", t_depth);
              p.programUniformPutTextureUnit("t_image", t_image);
              p.programUniformPutVector3f("fog.color", config.getColor());
              p.programUniformPutFloat("fog.upper_y", config.getUpperY());
              p.programUniformPutFloat("fog.lower_y", config.getLowerY());

              KShadingProgramCommon.putMatrixInverseView(
                p,
                KImageFilterFogY.this.view_inverse);
              KShadingProgramCommon.putViewRays(p, vr);
              KShadingProgramCommon.putMatrixUVUnchecked(
                p,
                KImageFilterFogY.this.matrix_uv);
              KShadingProgramCommon.putMatrixProjectionUnchecked(
                p,
                KImageFilterFogY.this.projection);
              KShadingProgramCommon.putDepthCoefficient(
                p,
                KRendererCommon.depthCoefficient(c_proj));

              p.programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                }
              });

            } finally {
              gc.texture2DStaticUnbind(t_depth);
              gc.texture2DStaticUnbind(t_image);
            }

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

  @Override public <A, E extends Throwable> A filterAccept(
    final KImageFilterVisitorType<A, E> v)
    throws RException,
      E
  {
    return v.deferred(this);
  }

  @Override public void filterEvaluateDeferred(
    final KFogYParameters config,
    final KFramebufferDeferredUsableType input,
    final KFramebufferDeferredUsableType output)
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
        this.copier.copierCopyRGBAOnly(
          input,
          input.kFramebufferGetArea(),
          temp,
          temp.kFramebufferGetArea());
        this.evaluateFog(
          config,
          input.deferredGetGeometryBuffer(),
          temp,
          output);
      } finally {
        receipt.returnToCache();
      }

    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  @Override public String filterGetName()
  {
    return KImageFilterFogY.NAME;
  }

  private KProgramType getFogProgram(
    final KFogYParameters config)
    throws RException
  {
    switch (config.getProgression()) {
      case FOG_EXPONENTIAL:
      {
        return this.shader_cache.cacheGetLU("fog_exponential_y_floor");
      }
      case FOG_LINEAR:
      {
        return this.shader_cache.cacheGetLU("fog_linear_y_floor");
      }
      case FOG_LOGARITHMIC:
      {
        return this.shader_cache.cacheGetLU("fog_logarithmic_y_floor");
      }
    }

    throw new UnreachableCodeException();
  }
}
