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
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.Framebuffer;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;

final class KRendererDebugBitangentsEye implements KRenderer
{
  private final @Nonnull MatrixM4x4F           matrix_modelview;
  private final @Nonnull MatrixM4x4F           matrix_model;
  private final @Nonnull MatrixM4x4F           matrix_view;
  private final @Nonnull MatrixM3x3F           matrix_normal;
  private final @Nonnull MatrixM4x4F           matrix_projection;
  private final @Nonnull MatrixM4x4F.Context   matrix_context;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull VectorM2I             viewport_size;
  private final @Nonnull ProgramReference      program_computed;
  private final @Nonnull ProgramReference      program_provided;
  private final @Nonnull JCCEExecutionCallable exec_computed;
  private final @Nonnull JCCEExecutionCallable exec_provided;

  KRendererDebugBitangentsEye(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException
  {
    this.log = new Log(log, "krenderer-debug-bitangents-eye");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_normal = new MatrixM3x3F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.program_computed =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "debug_bitangents_computed_eye",
        log);

    this.program_provided =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "debug_bitangents_provided_eye",
        log);

    this.exec_computed = new JCCEExecutionCallable(this.program_computed);
    this.exec_provided = new JCCEExecutionCallable(this.program_provided);
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

      this.exec_computed.execPrepare(gc);
      this.exec_computed.execUniformPutMatrix4x4F(
        gc,
        "m_projection",
        this.matrix_projection);
      this.exec_computed.execCancel();

      this.exec_provided.execPrepare(gc);
      this.exec_provided.execUniformPutMatrix4x4F(
        gc,
        "m_projection",
        this.matrix_projection);
      this.exec_provided.execCancel();

      for (final KMeshInstance mesh : scene.getMeshes()) {
        this.renderMesh(gc, mesh);
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderMeshWithComputedBitangent(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KTransform transform = instance.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    KRendererCommon.makeNormalMatrix(
      this.matrix_modelview,
      this.matrix_normal);

    /**
     * Upload matrices.
     */

    this.exec_computed.execPrepare(gc);
    this.exec_computed.execUniformUseExisting("m_projection");
    this.exec_computed.execUniformPutMatrix4x4F(
      gc,
      "m_modelview",
      this.matrix_modelview);
    this.exec_computed.execUniformPutMatrix3x3F(
      gc,
      "m_normal",
      this.matrix_normal);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);

      final ArrayBufferAttribute a_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      this.exec_computed.execAttributeBind(gc, "v_position", a_pos);

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        this.exec_computed.execAttributeBind(gc, "v_normal", a);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
        this.exec_computed.execAttributeBind(gc, "v_tangent4", a);
      }

      this.exec_computed.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError e) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });

      try {
        this.exec_computed.execRun(gc);
      } catch (final Exception e) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderMeshWithProvidedBitangent(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KTransform transform = instance.getTransform();
    transform.makeMatrix4x4F(this.transform_context, this.matrix_model);

    MatrixM4x4F.multiply(
      this.matrix_view,
      this.matrix_model,
      this.matrix_modelview);

    KRendererCommon.makeNormalMatrix(
      this.matrix_modelview,
      this.matrix_normal);

    /**
     * Upload matrices.
     */

    this.exec_provided.execPrepare(gc);
    this.exec_provided.execUniformUseExisting("m_projection");
    this.exec_provided.execUniformPutMatrix4x4F(
      gc,
      "m_modelview",
      this.matrix_modelview);
    this.exec_provided.execUniformPutMatrix3x3F(
      gc,
      "m_normal",
      this.matrix_normal);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);

      final ArrayBufferAttribute a_pos =
        array.getAttribute(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      this.exec_provided.execAttributeBind(gc, "v_position", a_pos);

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_BITANGENT.getName());
        this.exec_provided.execAttributeBind(gc, "v_bitangent", a);
      }

      this.exec_provided.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError e) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });

      try {
        this.exec_provided.execRun(gc);
      } catch (final Exception e) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KMesh mesh = instance.getMesh();
    final ArrayBuffer array = mesh.getArrayBuffer();

    /**
     * If the mesh has a bitangent attribute, then it must also have a
     * tangent3 attribute. Otherwise, it must have a tangent4 attribute and
     * the bitangent is computed by the fragment shader.
     */

    if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_BITANGENT.getName())) {
      Constraints.constrainArbitrary(
        array.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT3.getName()),
        "Mesh has tangent3");
      this.renderMeshWithProvidedBitangent(gc, instance);
    } else {
      Constraints.constrainArbitrary(
        array.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName()),
        "Mesh has tangent4");
      this.renderMeshWithComputedBitangent(gc, instance);
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
