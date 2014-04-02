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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLFramebuffersGL3;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.types.RException;

final class KRegionCopier
{
  private static final EnumSet<FramebufferBlitBuffer> COLOUR;
  private static final EnumSet<FramebufferBlitBuffer> DEPTH_COLOUR;

  static {
    COLOUR = EnumSet.of(FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR);
    DEPTH_COLOUR =
      EnumSet.of(
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR,
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH);
  }

  private static void copyFramebufferRegionDepthVarianceGL3(
    final @Nonnull JCGLFramebuffersGL3 gc,
    final @Nonnull KFramebufferDepthVarianceUsable source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferDepthVarianceUsable target,
    final @Nonnull AreaInclusive target_area,
    final @Nonnull FramebufferBlitFilter blit_filter)
    throws JCGLRuntimeException,
      ConstraintError
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
          KRegionCopier.DEPTH_COLOUR,
          blit_filter);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  private static void copyFramebufferRegionRGBAGL3(
    final @Nonnull JCGLFramebuffersGL3 gc,
    final @Nonnull KFramebufferRGBAUsable source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferRGBAUsable target,
    final @Nonnull AreaInclusive target_area,
    final @Nonnull FramebufferBlitFilter blit_filter)
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
          KRegionCopier.COLOUR,
          blit_filter);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  public static @Nonnull KRegionCopier newCopier(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
    throws RException,
      ConstraintError
  {
    return new KRegionCopier(gi, shader_cache, log);
  }

  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull Log                                   log;
  private final @Nonnull KUnitQuad                             quad;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;

  private KRegionCopier(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull LUCache<String, KProgram, RException> in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    try {
      this.g = Constraints.constrainNotNull(gi, "OpenGL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.log =
        new Log(Constraints.constrainNotNull(in_log, "Log"), "copier");
      this.quad = KUnitQuad.newQuad(gi.getGLCommon(), in_log);
    } catch (final JCGLException x) {
      throw RException.fromJCGLException(x);
    }
  }

  public void close()
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

  @SuppressWarnings("synthetic-access") public
    void
    copyFramebufferRegionDepthVariance(
      final @Nonnull KFramebufferDepthVarianceUsable source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull KFramebufferDepthVarianceUsable target,
      final @Nonnull AreaInclusive target_area,
      final @Nonnull FramebufferBlitFilter blit_filter)
      throws RException,
        ConstraintError
  {
    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitor<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionDepthVarianceGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionDepthVarianceGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            throw new UnreachableCodeException();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionDepthVarianceGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @SuppressWarnings("synthetic-access") public
    void
    copyFramebufferRegionRGBA(
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull AreaInclusive target_area,
      final @Nonnull FramebufferBlitFilter blit_filter)
      throws RException,
        ConstraintError
  {
    try {
      this.g
        .implementationAccept(new JCGLImplementationVisitor<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionRGBAGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionRGBAGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRegionCopier.this.copyFramebufferRegionRGBAGLES2(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3 gl)
            throws JCGLException,
              ConstraintError
          {
            KRegionCopier.copyFramebufferRegionRGBAGL3(
              gl,
              source,
              source_area,
              target,
              target_area,
              blit_filter);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @SuppressWarnings("static-method") protected
    void
    copyFramebufferRegionRGBAGLES2(
      final @Nonnull JCGLInterfaceGLES2 gl,
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull AreaInclusive target_area,
      final @Nonnull FramebufferBlitFilter blit_filter)
      throws JCGLRuntimeException,
        ConstraintError
  {
    gl.framebufferDrawBind(source.kFramebufferGetColorFramebuffer());
    try {
      // XXX: Not yet implemented on GLES2
      throw new UnimplementedCodeException();
    } finally {
      gl.framebufferDrawUnbind();
    }
  }
}
