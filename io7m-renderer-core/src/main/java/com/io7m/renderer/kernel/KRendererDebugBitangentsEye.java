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

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererDebug;

@Immutable final class KRendererDebugBitangentsEye extends
  KAbstractRendererDebug
{
  private static final @Nonnull String NAME = "debug-bitangents-eye";

  public static KRendererDebugBitangentsEye rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError,
      KXMLException
  {
    return new KRendererDebugBitangentsEye(g, fs, log);
  }

  private final @Nonnull VectorM4F          background;
  private final @Nonnull JCGLImplementation gl;
  private final @Nonnull Log                log;
  private final @Nonnull KMutableMatrices   matrices;
  private final @Nonnull KProgram           program;
  private final @Nonnull KTransform.Context transform_context;
  private final @Nonnull VectorM2I          viewport_size;

  private KRendererDebugBitangentsEye(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      KXMLException
  {
    super(KRendererDebugBitangentsEye.NAME);

    this.log = new Log(log, KRendererDebugBitangentsEye.NAME);
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.program =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "debug_bitangents_vertex_eye",
        log);
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();
    gc.programDelete(this.program.getProgram());
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KCamera camera = scene.getCamera();
    final KMutableMatrices.WithObserver mwc =
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix());

    try {
      final FramebufferReferenceUsable output_buffer =
        framebuffer.kFramebufferGetColorFramebuffer();
      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();

      try {
        gc.framebufferDrawBind(output_buffer);
        gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferClear(1.0f);
        gc.colorBufferClearV4f(this.background);
        gc.blendingDisable();

        final JCBExecutionAPI e = this.program.getExecutable();
        e.execRun(new JCBExecutorProcedure() {
          @SuppressWarnings("synthetic-access") @Override public void call(
            final @Nonnull JCBProgram p)
            throws ConstraintError,
              JCGLException,
              Exception
          {
            KShadingProgramCommon.putMatrixProjection(
              p,
              mwc.getMatrixProjection());

            for (final KMeshInstanceTransformed mesh : scene
              .getVisibleInstances()) {
              KRendererDebugBitangentsEye.this.renderMesh(gc, p, mwc, mesh);
            }
          }
        });
      } catch (final JCBExecutionException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      mwc.observerFinish();
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @SuppressWarnings("static-method") private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KMutableMatrices.WithObserver mwc,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);

    try {
      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixProjectionReuse(p);
      KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());
      KShadingProgramCommon.putMatrixNormal(p, mwi.getMatrixNormal());

      /**
       * Associate array attributes with program attributes, and draw mesh.
       */

      try {
        final KMesh mesh = instance.getInstance().getMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();

        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePosition(p, array);
        KShadingProgramCommon.bindAttributeNormal(p, array);
        KShadingProgramCommon.bindAttributeTangent4(p, array);

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
    } finally {
      mwi.instanceFinish();
    }
  }
}
