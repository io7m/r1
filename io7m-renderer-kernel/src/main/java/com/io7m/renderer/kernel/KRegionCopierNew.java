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
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLFramebuffersGL3;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixM3x3F;
import com.io7m.renderer.types.RTransformTexture;

final class KRegionCopierNew implements KRegionCopierType
{
  /**
   * <p>
   * Calculate the matrices required to transform the UV coordinates of a unit
   * quad <code>[(0, 0), (1, 1)]</code> such that the quad would be textured
   * with area <code>source_select_area</code> of an image
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
    final @Nonnull AreaInclusive source_image_area,
    final @Nonnull AreaInclusive source_select_area,
    final @Nonnull RMatrixM3x3F<RTransformTexture> matrix_uv)
  {
    MatrixM3x3F.setIdentity(matrix_uv);

    if (source_image_area.equals(source_select_area) == false) {
      final RangeInclusive si_rx = source_image_area.getRangeX();
      final RangeInclusive si_ry = source_image_area.getRangeY();
      final RangeInclusive ss_rx = source_select_area.getRangeX();
      final RangeInclusive ss_ry = source_select_area.getRangeY();

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

  private static <G extends JCGLFramebuffersGL3> void copyBlitRGBAOnlyGL3(
    final @Nonnull G gc,
    final @Nonnull KFramebufferRGBAUsable source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferRGBAUsable target,
    final @Nonnull AreaInclusive target_area)
    throws JCGLRuntimeException,
      ConstraintError
  {
    gc.framebufferReadBind(source.kFramebufferGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.kFramebufferGetColorFramebuffer());
      try {
        gc.framebufferBlit(
          source_area,
          target_area,
          EnumSet.of(FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR),
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_LINEAR);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }
  private boolean                                              blit;
  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull RMatrixM3x3F<RTransformTexture>       matrix_uv;
  private final @Nonnull KUnitQuad                             quad;

  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;

  KRegionCopierNew(
    final @Nonnull JCGLImplementation in_g,
    final @Nonnull Log log,
    final @Nonnull LUCache<String, KProgram, RException> in_shader_cache)
    throws ConstraintError,
      RException
  {
    try {
      this.g = Constraints.constrainNotNull(in_g, "GL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.matrix_uv = new RMatrixM3x3F<RTransformTexture>();
      this.blit = true;
      this.quad = KUnitQuad.newQuad(this.g.getGLCommon(), log);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void copierClose()
    throws RException
  {
    try {
      this.quad.delete(this.g.getGLCommon());
    } catch (final JCGLRuntimeException e) {
      throw RException.fromJCGLException(e);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @SuppressWarnings("synthetic-access") @Override public
    void
    copierCopyRGBAOnly(
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull AreaInclusive target_area)
      throws ConstraintError,
        RException
  {
    Constraints.constrainNotNull(source, "Source");
    Constraints.constrainNotNull(source_area, "Source area");
    Constraints.constrainNotNull(target, "Target");
    Constraints.constrainNotNull(target_area, "Target area");
    Constraints.constrainArbitrary(source != target, "Source != Target");

    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitor<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final @Nonnull JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRegionCopierNew.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final @Nonnull JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRegionCopierNew.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final @Nonnull JCGLInterfaceGLES2 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRegionCopierNew.this.copyDrawRGBAOnly(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final @Nonnull JCGLInterfaceGLES3 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRegionCopierNew.this.copyRGBAOnlyGL3(
              gl,
              source,
              source_area,
              target,
              target_area);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  public boolean copierIsBlittingEnabled()
  {
    return this.blit;
  }

  public void copierSetBlittingEnabled(
    final boolean in_blit)
  {
    this.blit = in_blit;
  }

  @SuppressWarnings("synthetic-access") private void copyDrawRGBAOnly(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KFramebufferRGBAUsable source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferRGBAUsable target,
    final @Nonnull AreaInclusive target_area)
    throws RException,
      ConstraintError
  {
    try {
      KRegionCopierNew.calculateRegionMatrices(
        source.kFramebufferGetArea(),
        source_area,
        this.matrix_uv);

      final KProgram kp =
        this.shader_cache.cacheGetLU("postprocessing_copy_rgba");

      final List<TextureUnit> units = gc.textureGetUnits();

      final FramebufferReferenceUsable output =
        target.kFramebufferGetColorFramebuffer();

      gc.framebufferDrawBind(output);

      try {
        gc.blendingDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.cullingDisable();

        if (gc.depthBufferGetBits() > 0) {
          gc.depthBufferTestDisable();
          gc.depthBufferWriteDisable();
        }

        try {
          gc.scissorEnable(target_area);
          gc.viewportSet(target_area);

          final JCBExecutionAPI e = kp.getExecutable();
          e.execRun(new JCBExecutorProcedure() {
            @Override public void call(
              final @Nonnull JCBProgram p)
              throws ConstraintError,
                JCGLException,
                Exception
            {
              final TextureUnit unit = units.get(0);
              gc.texture2DStaticBind(
                unit,
                source.kFramebufferGetRGBATexture());
              p.programUniformPutTextureUnit("t_image", unit);

              KRegionCopierNew.this.drawQuad(gc, p);
            }
          });

        } finally {
          gc.scissorDisable();
        }

      } catch (final JCBExecutionException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }

    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    } catch (final JCGLException x) {
      throw RException.fromJCGLException(x);
    }
  }

  private
    <G extends JCGLInterfaceCommon & JCGLFramebuffersGL3>
    void
    copyRGBAOnlyGL3(
      final @Nonnull G gl,
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull AreaInclusive target_area)
      throws RException,
        ConstraintError,
        JCGLRuntimeException
  {
    if (this.blit) {
      KRegionCopierNew.copyBlitRGBAOnlyGL3(
        gl,
        source,
        source_area,
        target,
        target_area);
    } else {
      this.copyDrawRGBAOnly(gl, source, source_area, target, target_area);
    }
  }

  private void drawQuad(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p)
    throws JCGLRuntimeException,
      ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final ArrayBufferUsable array = KRegionCopierNew.this.quad.getArray();
    final IndexBufferUsable indices = KRegionCopierNew.this.quad.getIndices();

    gc.arrayBufferBind(array);

    try {
      KShadingProgramCommon.bindAttributePosition(p, array);
      KShadingProgramCommon.bindAttributeUV(p, array);
      KShadingProgramCommon.putMatrixUV(p, KRegionCopierNew.this.matrix_uv);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException,
            Exception
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }
}
