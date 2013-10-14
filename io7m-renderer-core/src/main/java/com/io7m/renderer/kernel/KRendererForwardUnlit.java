/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.programs.KSPF_U;
import com.io7m.renderer.kernel.programs.KSPF_U_E;
import com.io7m.renderer.kernel.programs.KSPF_U_T;
import com.io7m.renderer.kernel.programs.KSPF_U_T_E;

final class KRendererForwardUnlit implements KRenderer
{
  private final @Nonnull JCGLImplementation gl;
  private final @Nonnull KSPF_U_T           fwd_U_T;
  private final @Nonnull KSPF_U             fwd_U;
  private final @Nonnull KSPF_U_T_E         fwd_U_T_E;
  private final @Nonnull KSPF_U_E           fwd_U_E;
  private final @Nonnull Log                log;
  private final @Nonnull VectorM4F          background;
  private final @Nonnull VectorM2I          viewport_size;
  private final @Nonnull KMatrices          matrices;

  KRendererForwardUnlit(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      JCGLException,
      FilesystemError,
      IOException
  {
    this.log = new Log(log, "krenderer-flat-textured");
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = new KMatrices();
    this.viewport_size = new VectorM2I();

    this.fwd_U_T = KSPF_U_T.make(gl.getGLCommon(), fs, log);
    this.fwd_U = KSPF_U.make(gl.getGLCommon(), fs, log);
    this.fwd_U_T_E = KSPF_U_T_E.make(gl.getGLCommon(), fs, log);
    this.fwd_U_E = KSPF_U_E.make(gl.getGLCommon(), fs, log);
  }

  @Override public void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    this.matrices.matricesBegin();
    this.matrices.matricesMakeFromCamera(scene.getCamera());

    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    this.viewport_size.x = result.getWidth();
    this.viewport_size.y = result.getHeight();

    try {
      gc.framebufferDrawBind(result.getFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      final RMatrixReadable4x4F<RTransformProjection> mp =
        this.matrices.getMatrixProjection();

      this.fwd_U_T.ksPreparePass(gc, mp);
      this.fwd_U.ksPreparePass(gc, mp);
      this.fwd_U_T_E.ksPreparePass(gc, mp);
      this.fwd_U_E.ksPreparePass(gc, mp);

      for (final KMeshInstance mesh : scene.getMeshes()) {
        this.renderMesh(gc, mesh);
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError
  {
    this.matrices.matricesMakeFromTransform(instance.getTransform());

    try {
      final KRenderingCapabilities caps = instance.getCapabilities();

      switch (caps.getTexture()) {
        case TEXTURE_CAP_DIFFUSE:
        {
          switch (caps.getEnvironment()) {
            case ENVIRONMENT_MAPPED:
            {
              this.fwd_U_T_E.ksRenderWithMeshInstance(
                gc,
                this.matrices,
                instance);
              break;
            }
            case ENVIRONMENT_NONE:
            {
              this.fwd_U_T.ksRenderWithMeshInstance(
                gc,
                this.matrices,
                instance);
              break;
            }
          }
          break;
        }
        case TEXTURE_CAP_NONE:
        {
          switch (caps.getEnvironment()) {
            case ENVIRONMENT_MAPPED:
            {
              this.fwd_U_E.ksRenderWithMeshInstance(
                gc,
                this.matrices,
                instance);
              break;
            }
            case ENVIRONMENT_NONE:
            {
              this.fwd_U
                .ksRenderWithMeshInstance(gc, this.matrices, instance);
              break;
            }
          }
          break;
        }
      }

    } catch (final Exception e) {
      throw new UnreachableCodeException();
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @Override public void close()
  {
    // TODO Auto-generated method stub
  }
}
