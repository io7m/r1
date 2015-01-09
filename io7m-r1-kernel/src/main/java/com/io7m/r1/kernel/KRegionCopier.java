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

import java.util.EnumSet;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLFramebuffersGL3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCopierSourceEqualsTarget;
import com.io7m.r1.types.RExceptionNotSupported;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * The default implementation of the {@link KRegionCopierType} interface.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRegionCopier implements
  KRegionCopierType
{
  private static final EnumSet<FramebufferBlitBuffer> COLOR_ONLY;
  private static final EnumSet<FramebufferBlitBuffer> DEPTH_ONLY;

  static {
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
    final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> matrix_uv)
  {
    PMatrixM3x3F.setIdentity(matrix_uv);

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
    <G extends JCGLFramebuffersGL3Type>
    void
    copyBlitRGBADepthGL3(
      final G gc,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    gc.framebufferReadBind(source.rgbaGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.rgbaGetColorFramebuffer());
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

  private static <G extends JCGLFramebuffersGL3Type> void copyBlitDepthGL3(
    final G gc,
    final KFramebufferRGBAUsableType source,
    final AreaInclusive source_area,
    final KFramebufferRGBAUsableType target,
    final AreaInclusive target_area)
    throws JCGLException
  {
    gc.framebufferReadBind(source.rgbaGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.rgbaGetColorFramebuffer());
      try {
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
    gc.framebufferReadBind(source.rgbaGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.rgbaGetColorFramebuffer());
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
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyDepthVarianceOnlyGL3(
      final G gl,
      final KFramebufferDepthVarianceUsableType source,
      final AreaInclusive source_area,
      final KFramebufferDepthVarianceUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    KRegionCopier.copyBlitDepthVarianceOnlyGL3(
      gl,
      source,
      source_area,
      target,
      target_area);
  }

  private static
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyRGBADepthGL3(
      final G gl,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    KRegionCopier.copyBlitRGBADepthGL3(
      gl,
      source,
      source_area,
      target,
      target_area);
  }

  private static
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyDepthGL3(
      final G gl,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    KRegionCopier.copyBlitDepthGL3(
      gl,
      source,
      source_area,
      target,
      target_area);
  }

  private static
    <G extends JCGLInterfaceCommonType & JCGLFramebuffersGL3Type>
    void
    copyRGBAOnlyGL3(
      final G gl,
      final KFramebufferRGBAUsableType source,
      final AreaInclusive source_area,
      final KFramebufferRGBAUsableType target,
      final AreaInclusive target_area)
      throws JCGLException
  {
    KRegionCopier.copyBlitRGBAOnlyGL3(
      gl,
      source,
      source_area,
      target,
      target_area);
  }

  /**
   * Construct a new region copier.
   *
   * @param g
   *          The OpenGL implementation
   * @param log
   *          A log handle
   * @return A new region copier
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRegionCopierType newCopier(
    final JCGLImplementationType g,
    final LogUsableType log)
    throws RException
  {
    return new KRegionCopier(g, log);
  }

  private final JCGLImplementationType g;

  private final LogUsableType          log;

  private KRegionCopier(
    final JCGLImplementationType in_g,
    final LogUsableType in_log)
  {
    this.g = NullCheck.notNull(in_g, "GL implementation");
    this.log = NullCheck.notNull(in_log, "Log").with("region-copier");

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
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

    this.g
      .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyDepthVarianceOnlyGL3(
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
          KRegionCopier.copyDepthVarianceOnlyGL3(
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
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,

            RException
        {
          KRegionCopier.copyDepthVarianceOnlyGL3(
            gl,
            source,
            source_area,
            target,
            target_area);
          return Unit.unit();
        }
      });
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

    this.g
      .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyRGBAOnlyGL3(
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
          KRegionCopier.copyRGBAOnlyGL3(
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
          throw RExceptionNotSupported.versionNotSupported(gl
            .metaGetVersion());
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,

            RException
        {
          KRegionCopier.copyRGBAOnlyGL3(
            gl,
            source,
            source_area,
            target,
            target_area);
          return Unit.unit();
        }
      });
  }

  @Override public void copierCopyRGBAWithDepth(
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

    this.g
      .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyRGBADepthGL3(
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
          KRegionCopier.copyRGBADepthGL3(
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
          throw new UnreachableCodeException();
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyRGBADepthGL3(
            gl,
            source,
            source_area,
            target,
            target_area);
          return Unit.unit();
        }
      });
  }

  @Override public void copierCopyDepthOnly(
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

    this.g
      .implementationAccept(new JCGLImplementationVisitorType<Unit, RException>() {
        @Override public Unit implementationIsGL2(
          final JCGLInterfaceGL2Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyDepthGL3(
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
          KRegionCopier.copyDepthGL3(
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
          throw new UnreachableCodeException();
        }

        @Override public Unit implementationIsGLES3(
          final JCGLInterfaceGLES3Type gl)
          throws JCGLException,
            RException
        {
          KRegionCopier.copyDepthGL3(
            gl,
            source,
            source_area,
            target,
            target_area);
          return Unit.unit();
        }
      });
  }
}
