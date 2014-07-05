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

package com.io7m.renderer.kernel;

import java.util.EnumSet;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KUnitQuadCacheType;
import com.io7m.renderer.kernel.types.KUnitQuadUsableType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCopierSourceEqualsTarget;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RTransformTextureType;

/**
 * The default implementation of the {@link KRegionCopierType} interface.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRegionCopier implements
  KRegionCopierType
{
  private static final EnumSet<FramebufferBlitBuffer> COLOR_ONLY;
  private static final EnumSet<FramebufferBlitBuffer> DEPTH_ONLY;

  private static final String                         SHADER_COPY_DEPTH4444;
  private static final String                         SHADER_COPY_RGBA;
  private static final String                         SHADER_COPY_RGBA_DEPTH;

  static {
    SHADER_COPY_RGBA_DEPTH = "copy_rgba_depth";
    SHADER_COPY_DEPTH4444 = "copy_depth4444";
    SHADER_COPY_RGBA = "copy_rgba";

    DEPTH_ONLY =
      NullCheck.notNull(EnumSet
        .of(FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH));
    COLOR_ONLY =
      NullCheck.notNull(EnumSet
        .of(FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR));
  }

  /**
   * <p>
   * Calculate the matrices required to transform the UV coordinates of a unit
   * quad_cache <code>[(0, 0), (1, 1)]</code> such that the quad_cache would
   * be textured with area <code>source_select_area</code> of an image
   * <code>source_image_area</code>.
   * </p>
   * <p>
   * Consequently, iff <code>source_image_area == source_select_area</code>,
   * then <code>matrix_uv</code> is the identity matrix.
   * </p>
   *
   * @param source_image_area
   *          The inclusive area of the source image
   * @param source_select_area
   *          The inclusive area selected from the source image
   * @param matrix_uv
   *          The resulting UV matrix
   */

  public static void calculateRegionMatrices(
    final AreaInclusive source_image_area,
    final AreaInclusive source_select_area,
    final RMatrixM3x3F<RTransformTextureType> matrix_uv)
  {
    MatrixM3x3F.setIdentity(matrix_uv);

    if (source_image_area.equals(source_select_area) == false) {
      final RangeInclusiveL si_rx = source_image_area.getRangeX();
      final RangeInclusiveL si_ry = source_image_area.getRangeY();
      final RangeInclusiveL ss_rx = source_select_area.getRangeX();
      final RangeInclusiveL ss_ry = source_select_area.getRangeY();

      final double uv_trans_x =
        (double) ss_rx.getLower() / (double) si_rx.getInterval();
      final double uv_trans_y =
        (double) ss_ry.getLower() / (double) si_ry.getInterval();

      final double uv_scale_x =
        (double) ss_rx.getInterval() / (double) si_rx.getInterval();
      final double uv_scale_y =
        (double) ss_ry.getInterval() / (double) si_ry.getInterval();

      matrix_uv.set(0, 0, (float) uv_scale_x);
      matrix_uv.set(1, 1, (float) uv_scale_y);
      matrix_uv.set(0, 2, (float) uv_trans_x);
      matrix_uv.set(1, 2, (float) uv_trans_y);
    }
  }

  private static
    <G extends JCGLFramebuffersGL3Type>
    void
    copyBlitDepthVarianceOnlyGL3(
      final G gc,
      final KFramebufferDepthVarianceUsableType source,
      final AreaInclusive source_area,
      final KFramebufferDepthVarianceUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    gc.framebufferReadBind(source
      .kFramebufferGetDepthVariancePassFramebuffer());
    try {
      gc.framebufferDrawBind(target
        .kFramebufferGetDepthVariancePassFramebuffer());
      try {
        gc.framebufferBlit(
          source_area,
          target_area,
          KRegionCopier.COLOR_ONLY,
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_LINEAR);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  private static
    <G extends JCGLFramebuffersGL3Type, F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    copyBlitRGBADepthGL3(
      final G gc,
      final F source,
      final AreaInclusive source_area,
      final F target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    assert source.kFramebufferGetDepthIsPackedColor() == false;
    assert source.kFramebufferGetColorFramebuffer() == source
      .kFramebufferGetDepthPassFramebuffer();
    assert target.kFramebufferGetDepthIsPackedColor() == false;
    assert target.kFramebufferGetColorFramebuffer() == target
      .kFramebufferGetDepthPassFramebuffer();

    gc.framebufferReadBind(source.kFramebufferGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.kFramebufferGetColorFramebuffer());
      try {
        gc.framebufferBlit(
          source_area,
          target_area,
          KRegionCopier.COLOR_ONLY,
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_LINEAR);
        gc.framebufferBlit(
          source_area,
          target_area,
          KRegionCopier.DEPTH_ONLY,
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  private static
    <G extends JCGLFramebuffersGL3Type>
    void
    copyBlitRGBAOnlyGL3(
      final G gc,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    gc.framebufferReadBind(source.kFramebufferGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.kFramebufferGetColorFramebuffer());
      try {
        gc.framebufferBlit(
          source_area,
          target_area,
          KRegionCopier.COLOR_ONLY,
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_LINEAR);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  /**
   * Construct a new region copier.
   *
   * @param g
   *          The OpenGL implementation
   * @param log
   *          A log handle
   * @param shader_cache
   *          A shader cache
   * @param quad_cache
   *          A unit quad_cache cache
   * @return A new region copier
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRegionCopierType newCopier(
    final JCGLImplementationType g,
    final LogUsableType log,
    final KShaderCachePostprocessingType shader_cache,
    final KUnitQuadCacheType quad_cache)
    throws RException
  {
    return new KRegionCopier(g, log, shader_cache, quad_cache);
  }

  private boolean                                   blit;
  private final JCGLImplementationType              g;
  private final LogUsableType                       log;
  private final RMatrixM3x3F<RTransformTextureType> matrix_uv;
  private final KUnitQuadCacheType                  quad_cache;
  private final KShaderCachePostprocessingType      shader_cache;
  private final KTextureUnitAllocator               texture_units;

  private KRegionCopier(
    final JCGLImplementationType in_g,
    final LogUsableType in_log,
    final KShaderCachePostprocessingType in_shader_cache,
    final KUnitQuadCacheType in_quad_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(in_g, "GL implementation");
      this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
      this.quad_cache = NullCheck.notNull(in_quad_cache, "Quad cache");
      this.log = NullCheck.notNull(in_log, "Log").with("region-copier");

      this.matrix_uv = new RMatrixM3x3F<RTransformTextureType>();
      this.blit = true;
      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());

      if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void copierCopyDepthVarianceOnly(
    final KFramebufferDepthVarianceUsableType source,
    final AreaInclusive source_area,
    final KFramebufferDepthVarianceUsableType target,
    final AreaInclusive target_area)
    throws RException
  {
    NullCheck.notNull(source, "Source");
    NullCheck.notNull(source_area, "Source area");
    NullCheck.notNull(target, "Target");
    NullCheck.notNull(target_area, "Target area");

    if (source == target) {
      throw new RExceptionCopierSourceEqualsTarget(
        "Source framebuffer must not be equal to target framebuffer");
    }

    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
            throws JCGLException,
              RException
          {
            KRegionCopier.this.copyDepthVarianceOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
            throws JCGLException,
              RException
          {
            KRegionCopier.this.copyDepthVarianceOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
            throws JCGLException,
              RException
          {
            KRegionCopier.this.texture_units
              .withContext(new KTextureUnitWithType() {
                @Override public void run(
                  final KTextureUnitContextType context)
                  throws JCGLException,
                    RException
                {
                  try {
                    KRegionCopier.this.copyDrawColorOnly(
                      gl,
                      context,
                      source.kFramebufferGetDepthVarianceTexture(),
                      source_area,
                      target.kFramebufferGetDepthVariancePassFramebuffer(),
                      target_area,
                      KRegionCopier.SHADER_COPY_RGBA);
                  } catch (final JCacheException e) {
                    throw new UnreachableCodeException(e);
                  }
                }
              });
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyDepthVarianceOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void copierCopyRGBAOnly(
    final KFramebufferRGBAUsableType source,
    final AreaInclusive source_area,
    final KFramebufferRGBAUsableType target,
    final AreaInclusive target_area)
    throws RException
  {
    NullCheck.notNull(source, "Source");
    NullCheck.notNull(source_area, "Source area");
    NullCheck.notNull(target, "Target");
    NullCheck.notNull(target_area, "Target area");

    if (source == target) {
      throw new RExceptionCopierSourceEqualsTarget(
        "Source framebuffer must not be equal to target framebuffer");
    }

    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.texture_units
              .withContext(new KTextureUnitWithType() {
                @Override public void run(
                  final KTextureUnitContextType context)
                  throws JCGLException,
                    RException
                {
                  try {
                    KRegionCopier.this.copyDrawColorOnly(
                      gl,
                      context,
                      source.kFramebufferGetRGBATexture(),
                      source_area,
                      target.kFramebufferGetColorFramebuffer(),
                      target_area,
                      KRegionCopier.SHADER_COPY_RGBA);
                  } catch (final JCacheException e) {
                    throw new UnreachableCodeException(e);
                  }
                }
              });
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    copierCopyRGBAWithDepth(
      final F source,
      final AreaInclusive source_area,
      final F target,
      final AreaInclusive target_area)
      throws RException
  {
    NullCheck.notNull(source, "Source");
    NullCheck.notNull(source_area, "Source area");
    NullCheck.notNull(target, "Target");
    NullCheck.notNull(target_area, "Target area");

    if (source == target) {
      throw new RExceptionCopierSourceEqualsTarget(
        "Source framebuffer must not be equal to target framebuffer");
    }

    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBADepthGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBADepthGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBADepthGLES2(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
            throws JCGLException,

              RException
          {
            KRegionCopier.this.copyRGBADepthGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public boolean copierIsBlittingEnabled()
  {
    return this.blit;
  }

  @Override public void copierSetBlittingEnabled(
    final boolean in_blit)
  {
    this.blit = in_blit;
  }

  private
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyDepthVarianceOnlyGL3(
      final G gl,
      final KFramebufferDepthVarianceUsableType source,
      final AreaInclusive source_area,
      final KFramebufferDepthVarianceUsableType target,
      final AreaInclusive target_area)
      throws RException,
        JCGLException
  {
    if (this.blit) {
      KRegionCopier.copyBlitDepthVarianceOnlyGL3(
        gl,
        source,
        source_area,
        target,
        target_area);
    } else {
      this.texture_units.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          try {
            KRegionCopier.this.copyDrawColorOnly(
              gl,
              context,
              source.kFramebufferGetDepthVarianceTexture(),
              source_area,
              target.kFramebufferGetDepthVariancePassFramebuffer(),
              target_area,
              KRegionCopier.SHADER_COPY_RGBA);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    }
  }

  private void copyDrawColorDepth(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_context,
    final Texture2DStaticUsableType source_color,
    final Texture2DStaticUsableType source_depth,
    final AreaInclusive source_select_area,
    final FramebufferUsableType target,
    final AreaInclusive target_select_area,
    final String shader_name)
    throws RException,
      JCacheException
  {
    assert source_color
      .textureGetArea()
      .equals(source_depth.textureGetArea());

    try {
      KRegionCopier.calculateRegionMatrices(
        source_color.textureGetArea(),
        source_select_area,
        this.matrix_uv);

      final KProgram kp = this.shader_cache.cacheGetLU(shader_name);
      gc.framebufferDrawBind(target);

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();
        gc.depthBufferTestDisable();
        gc.depthBufferWriteEnable();

        try {
          gc.scissorEnable(target_select_area);
          gc.viewportSet(target_select_area);

          final JCBExecutorType e = kp.getExecutable();
          e.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType p)
              throws JCGLException,
                RException
            {
              p.programUniformPutTextureUnit(
                "t_image",
                texture_context.withTexture2D(source_color));
              p.programUniformPutTextureUnit(
                "t_image_depth",
                texture_context.withTexture2D(source_depth));

              KRegionCopier.this.drawQuad(gc, p);
            }
          });

        } finally {
          gc.scissorDisable();
        }
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  private void copyDrawColorDepthPacked(
    final JCGLInterfaceGLES2Type gl,
    final KTextureUnitContextType texture_context,
    final Texture2DStaticUsableType rgba_texture,
    final Texture2DStaticUsableType depth_texture,
    final AreaInclusive source_area,
    final FramebufferUsableType color_framebuffer,
    final FramebufferUsableType depth_framebuffer,
    final AreaInclusive target_area)
    throws RException,
      JCacheException
  {
    this.copyDrawColorOnly(
      gl,
      texture_context,
      rgba_texture,
      source_area,
      color_framebuffer,
      target_area,
      KRegionCopier.SHADER_COPY_RGBA);

    this.copyDrawDepthOnly(
      gl,
      texture_context,
      depth_texture,
      source_area,
      depth_framebuffer,
      target_area,
      KRegionCopier.SHADER_COPY_DEPTH4444);
  }

  private void copyDrawColorOnly(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_context,
    final Texture2DStaticUsableType source,
    final AreaInclusive source_select_area,
    final FramebufferUsableType target,
    final AreaInclusive target_select_area,
    final String shader_name)
    throws RException,
      JCacheException
  {
    try {
      KRegionCopier.calculateRegionMatrices(
        source.textureGetArea(),
        source_select_area,
        this.matrix_uv);

      final KProgram kp = this.shader_cache.cacheGetLU(shader_name);
      gc.framebufferDrawBind(target);

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        try {
          gc.scissorEnable(target_select_area);
          gc.viewportSet(target_select_area);

          final JCBExecutorType e = kp.getExecutable();
          e.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType p)
              throws JCGLException,
                RException
            {
              final TextureUnitType unit =
                texture_context.withTexture2D(source);
              p.programUniformPutTextureUnit("t_image", unit);
              KRegionCopier.this.drawQuad(gc, p);
            }
          });

        } finally {
          gc.scissorDisable();
        }
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  private void copyDrawDepthOnly(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType texture_context,
    final Texture2DStaticUsableType source,
    final AreaInclusive source_select_area,
    final FramebufferUsableType target,
    final AreaInclusive target_select_area,
    final String shader_name)
    throws RException,
      JCacheException
  {
    try {
      KRegionCopier.calculateRegionMatrices(
        source.textureGetArea(),
        source_select_area,
        this.matrix_uv);

      final KProgram kp = this.shader_cache.cacheGetLU(shader_name);
      gc.framebufferDrawBind(target);

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();

        gc.depthBufferTestDisable();
        gc.depthBufferWriteDisable();

        try {
          gc.scissorEnable(target_select_area);
          gc.viewportSet(target_select_area);

          final JCBExecutorType e = kp.getExecutable();
          e.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType p)
              throws JCGLException,
                RException
            {
              final TextureUnitType unit =
                texture_context.withTexture2D(source);
              p.programUniformPutTextureUnit("t_image_depth", unit);
              KRegionCopier.this.drawQuad(gc, p);
            }
          });

        } finally {
          gc.scissorDisable();
        }
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  private
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type, F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    copyRGBADepthGL3(
      final G gl,
      final F source,
      final AreaInclusive source_area,
      final F target,
      final AreaInclusive target_area)
      throws RException,
        JCGLException
  {
    assert source.kFramebufferGetDepthIsPackedColor() == false;
    assert source.kFramebufferGetColorFramebuffer() == source
      .kFramebufferGetDepthPassFramebuffer();
    assert target.kFramebufferGetDepthIsPackedColor() == false;
    assert target.kFramebufferGetColorFramebuffer() == target
      .kFramebufferGetDepthPassFramebuffer();

    if (this.blit) {
      KRegionCopier.copyBlitRGBADepthGL3(
        gl,
        source,
        source_area,
        target,
        target_area);
    } else {
      this.texture_units.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          try {
            KRegionCopier.this.copyDrawColorDepth(
              gl,
              context,
              source.kFramebufferGetRGBATexture(),
              source.kFramebufferGetDepthTexture(),
              source_area,
              target.kFramebufferGetColorFramebuffer(),
              target_area,
              KRegionCopier.SHADER_COPY_RGBA_DEPTH);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    }
  }

  private
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    copyRGBADepthGLES2(
      final JCGLInterfaceGLES2Type gl,
      final F source,
      final AreaInclusive source_area,
      final F target,
      final AreaInclusive target_area)
      throws RException,
        JCGLException
  {
    assert source.kFramebufferGetDepthIsPackedColor() == target
      .kFramebufferGetDepthIsPackedColor();

    this.texture_units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType texture_context)
        throws JCGLException,
          RException
      {
        try {
          if (target.kFramebufferGetDepthIsPackedColor()) {
            assert source.kFramebufferGetColorFramebuffer() != source
              .kFramebufferGetDepthPassFramebuffer();
            assert target.kFramebufferGetColorFramebuffer() != target
              .kFramebufferGetDepthPassFramebuffer();

            KRegionCopier.this.copyDrawColorDepthPacked(
              gl,
              texture_context,
              source.kFramebufferGetRGBATexture(),
              source.kFramebufferGetDepthTexture(),
              source_area,
              target.kFramebufferGetColorFramebuffer(),
              target.kFramebufferGetDepthPassFramebuffer(),
              target_area);

          } else {
            assert source.kFramebufferGetColorFramebuffer() == source
              .kFramebufferGetDepthPassFramebuffer();
            assert target.kFramebufferGetColorFramebuffer() == target
              .kFramebufferGetDepthPassFramebuffer();

            KRegionCopier.this.copyDrawColorDepth(
              gl,
              texture_context,
              source.kFramebufferGetRGBATexture(),
              source.kFramebufferGetDepthTexture(),
              source_area,
              target.kFramebufferGetColorFramebuffer(),
              target_area,
              KRegionCopier.SHADER_COPY_RGBA_DEPTH);
          }
        } catch (final JCacheException e) {
          throw new UnreachableCodeException(e);
        }
      }
    });
  }

  private
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyRGBAOnlyGL3(
      final G gl,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws RException,
        JCGLException
  {
    if (this.blit) {
      KRegionCopier.copyBlitRGBAOnlyGL3(
        gl,
        source,
        source_area,
        target,
        target_area);
    } else {
      this.texture_units.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          try {
            KRegionCopier.this.copyDrawColorOnly(
              gl,
              context,
              source.kFramebufferGetRGBATexture(),
              source_area,
              target.kFramebufferGetColorFramebuffer(),
              target_area,
              KRegionCopier.SHADER_COPY_RGBA);
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    }
  }

  private void drawQuad(
    final JCGLInterfaceCommonType gc,
    final JCBProgramType p)
    throws JCGLException,
      JCGLException,
      RException
  {
    try {
      final KUnitQuadUsableType quad =
        this.quad_cache.cacheGetLU(Unit.unit());
      final ArrayBufferUsableType array = quad.getArray();
      final IndexBufferUsableType indices = quad.getIndices();

      gc.arrayBufferBind(array);

      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);
      KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
      KShadingProgramCommon.putMatrixUVUnchecked(
        p,
        KRegionCopier.this.matrix_uv);

      p.programExecute(new JCBProgramProcedureType<RException>() {
        @Override public void call()
          throws JCGLException,
            RException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.arrayBufferUnbind();
    }
  }
}
