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
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RTransformModel;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KRenderingCapabilities.TextureCapability;
import com.io7m.renderer.kernel.programs.KSPF_U;
import com.io7m.renderer.kernel.programs.KSPF_U_T;

final class KRendererForwardUnlit implements KRenderer
{
  private final @Nonnull JCGLImplementation                 gl;
  private final @Nonnull KSPF_U_T                           fwd_U_t;
  private final @Nonnull KSPF_U                             fwd_U;
  private final @Nonnull Log                                log;
  private final @Nonnull VectorM4F                          background;
  private final @Nonnull VectorM2I                          viewport_size;

  private final @Nonnull KTransform.Context                 transform_context;
  private final @Nonnull MatrixM4x4F.Context                matrix_context;
  private final @Nonnull RMatrixM4x4F<RTransformModelView>  matrix_modelview;
  private final @Nonnull RMatrixM4x4F<RTransformProjection> matrix_projection;
  private final @Nonnull RMatrixM4x4F<RTransformModel>      matrix_model;
  private final @Nonnull RMatrixM4x4F<RTransformView>       matrix_view;
  private final @Nonnull RMatrixM3x3F<RTransformNormal>     matrix_normal;

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
    this.matrix_modelview = new RMatrixM4x4F<RTransformModelView>();
    this.matrix_projection = new RMatrixM4x4F<RTransformProjection>();
    this.matrix_model = new RMatrixM4x4F<RTransformModel>();
    this.matrix_view = new RMatrixM4x4F<RTransformView>();
    this.matrix_normal = new RMatrixM3x3F<RTransformNormal>();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.fwd_U_t = KSPF_U_T.make(gl.getGLCommon(), fs, log);
    this.fwd_U = KSPF_U.make(gl.getGLCommon(), fs, log);
  }

  @Override public void render(
    final @Nonnull Framebuffer result,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final KCamera camera = scene.getCamera();
    camera.getProjectionMatrix().makeMatrixM4x4F(this.matrix_projection);
    camera.getViewMatrix().makeMatrixM4x4F(this.matrix_view);

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

      this.fwd_U_t.ksPreparePass(gc, this.matrix_projection);
      this.fwd_U.ksPreparePass(gc, this.matrix_projection);

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
    final KTransform transform = instance.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    try {
      final KRenderingCapabilities caps = instance.getCapabilities();

      if (caps.getTexture() == TextureCapability.TEXTURE_CAP_DIFFUSE) {
        this.fwd_U_t.ksRenderWithMeshInstance(
          gc,
          this.matrix_modelview,
          this.matrix_normal,
          instance);
      } else {
        this.fwd_U.ksRenderWithMeshInstance(
          gc,
          this.matrix_modelview,
          this.matrix_normal,
          instance);
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
}
