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
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
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
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RVectorI3F;

public final class SBRendererSpecific implements KRenderer
{
  private static void setParameters(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    SBRendererSpecific.setParametersNormal(gc, i, array, exec);
    SBRendererSpecific.setParametersEnvironment(gc, i, exec);
    SBRendererSpecific.setParametersSpecular(gc, i, exec);
    SBRendererSpecific.setParametersEmissive(gc, i, exec);
    SBRendererSpecific.setParametersAlbedo(gc, i, exec);
  }

  private static void setParametersAlbedo(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    final KMaterialAlbedo albedo = i.getMaterial().getDiffuse();

    if (KShadingProgramCommon.existsMaterialAlbedoColour(exec)) {
      KShadingProgramCommon.putMaterialAlbedoColour(
        exec,
        gc,
        albedo.getColour());
    }
    if (KShadingProgramCommon.existsMaterialAlbedoMix(exec)) {
      KShadingProgramCommon.putMaterialAlbedoMix(exec, gc, albedo.getMix());
    }
  }

  private static void setParametersEmissive(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    if (KShadingProgramCommon.existsMaterialEmissiveLevel(exec)) {
      KShadingProgramCommon.putMaterialEmissiveLevel(exec, gc, i
        .getMaterial()
        .getEmissive()
        .getEmission());
    }
  }

  private static void setParametersEnvironment(
    final JCGLInterfaceCommon gc,
    final KMeshInstance i,
    final JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    final KMaterialEnvironment environment = i.getMaterial().getEnvironment();
    if (KShadingProgramCommon.existsMaterialEnvironmentMix(exec)) {
      KShadingProgramCommon.putMaterialEnvironmentMix(
        exec,
        gc,
        environment.getMix());
    }
    if (KShadingProgramCommon.existsMaterialEnvironmentReflectionMix(exec)) {
      KShadingProgramCommon.putMaterialEnvironmentReflectionMix(
        exec,
        gc,
        environment.getReflectionMix());
    }
    if (KShadingProgramCommon.existsMaterialEnvironmentRefractionIndex(exec)) {
      KShadingProgramCommon.putMaterialEnvironmentRefractionIndex(
        exec,
        gc,
        environment.getRefractionIndex());
    }
  }

  private static void setParametersNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    if (KShadingProgramCommon.existsAttributeNormal(exec)) {
      switch (i.getMaterialLabel().getNormal()) {
        case NORMAL_MAPPED:
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(gc, exec, array);
          break;
        }
        case NORMAL_NONE:
        {
          KShadingProgramCommon.putAttributeNormal(
            gc,
            exec,
            new RVectorI3F<RSpaceObject>(0, 0, 0));
        }
      }
    }
  }

  private static void setParametersSpecular(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    final KMaterialSpecular specular = i.getMaterial().getSpecular();
    if (KShadingProgramCommon.existsMaterialSpecularIntensity(exec)) {
      KShadingProgramCommon.putMaterialSpecularIntensity(
        exec,
        gc,
        specular.getIntensity());
    }
    if (KShadingProgramCommon.existsMaterialSpecularExponent(exec)) {
      KShadingProgramCommon.putMaterialSpecularExponent(
        exec,
        gc,
        specular.getExponent());
    }
  }

  private final @Nonnull ProgramReference      program;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull ProgramReference      program_depth;
  private final @Nonnull JCCEExecutionCallable exec_depth;
  private final @Nonnull KMatrices             matrices;
  private final @Nonnull JCCEExecutionCallable exec_program;

  public SBRendererSpecific(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log,
    final @Nonnull ProgramReference program)
    throws ConstraintError,
      JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException
  {
    this.program = Constraints.constrainNotNull(program, "Program");
    this.exec_program = new JCCEExecutionCallable(program);

    this.log = new Log(log, "krenderer-forward-diffuse");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = new KMatrices();

    this.program_depth =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth",
        log);
    this.exec_depth = new JCCEExecutionCallable(this.program_depth);
  }

  @Override public void close()
    throws JCGLException,
      ConstraintError
  {
    // TODO Auto-generated method stub
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

    try {
      gc.framebufferDrawBind(result.getFramebuffer());

      gc.colorBufferClearV4f(this.background);

      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      /**
       * Render all opaque meshes into the depth buffer, without touching the
       * color buffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(false, false, false, false);
      gc.blendingDisable();
      this.renderDepthPassMeshes(scene, gc);

      /**
       * Render all opaque meshes, blending additively, into the framebuffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
      this.renderOpaqueMeshes(scene, gc);

      /**
       * Render all translucent meshes into the framebuffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(
        BlendFunction.BLEND_SOURCE_ALPHA,
        BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
      this.renderTranslucentMeshes(scene, gc);

    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  /**
   * Render the given mesh into the depth buffer, without touching the color
   * buffer.
   */

  private void renderDepthPassMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    this.matrices.matricesMakeFromTransform(instance.getTransform());

    /**
     * Upload matrices.
     */

    e.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(e);
    KShadingProgramCommon.putMatrixModelView(
      e,
      gc,
      this.matrices.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(gc, e, array);

      e.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });

      try {
        e.execRun(gc);
      } catch (final Exception x) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  /**
   * Render all meshes into the depth buffer, without writing anything to the
   * color buffer.
   */

  private void renderDepthPassMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    this.log.debug("Render depth pass");

    this.exec_depth.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(
      this.exec_depth,
      gc,
      this.matrices.getMatrixProjection());
    this.exec_depth.execCancel();

    final KBatches batches = scene.getBatches();

    for (final KBatchLit bl : batches.getBatchesOpaqueLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderDepthPassMesh(gc, this.exec_depth, i);
      }
    }

    for (final KBatchUnlit bl : batches.getBatchesOpaqueUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderDepthPassMesh(gc, this.exec_depth, i);
      }
    }
  }

  /**
   * Render an opaque mesh to the color buffer.
   */

  private void renderOpaqueMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i)
    throws ConstraintError,
      JCGLException
  {
    this.log.debug("Render opaque " + i);

    this.matrices.matricesMakeFromTransform(i.getTransform());

    /**
     * Upload matrices.
     */

    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(this.exec_program);
    KShadingProgramCommon.putMatrixModelView(
      this.exec_program,
      gc,
      this.matrices.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(
        gc,
        this.exec_program,
        array);

      SBRendererSpecific.setParameters(gc, i, array, this.exec_program);

      this.exec_program.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });

      try {
        this.exec_program.execRun(gc);
      } catch (final Exception x) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderOpaqueMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws JCGLException,
      ConstraintError
  {
    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(
      this.exec_program,
      gc,
      this.matrices.getMatrixProjection());
    this.exec_program.execCancel();

    final KBatches batches = scene.getBatches();

    for (final KBatchLit bl : batches.getBatchesOpaqueLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderOpaqueMesh(gc, i);
      }
    }

    for (final KBatchUnlit bl : batches.getBatchesOpaqueUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderOpaqueMesh(gc, i);
      }
    }
  }

  private void renderTranslucentMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i)
    throws ConstraintError,
      JCGLException
  {
    this.log.debug("Render translucent " + i);

    this.matrices.matricesMakeFromTransform(i.getTransform());

    /**
     * Upload matrices.
     */

    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(this.exec_program);
    KShadingProgramCommon.putMatrixModelView(
      this.exec_program,
      gc,
      this.matrices.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(
        gc,
        this.exec_program,
        array);

      SBRendererSpecific.setParameters(gc, i, array, this.exec_program);

      this.exec_program.execSetCallable(new Callable<Void>() {
        @Override public Void call()
          throws Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException();
          }
          return null;
        }
      });

      try {
        this.exec_program.execRun(gc);
      } catch (final Exception x) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderTranslucentMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws JCGLException,
      ConstraintError
  {
    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(
      this.exec_program,
      gc,
      this.matrices.getMatrixProjection());
    this.exec_program.execCancel();

    final KBatches batches = scene.getBatches();

    for (final KBatchLit bl : batches.getBatchesTranslucentLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderTranslucentMesh(gc, i);
      }
    }

    for (final KBatchUnlit bl : batches.getBatchesTranslucentUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderTranslucentMesh(gc, i);
      }
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    // TODO Auto-generated method stub
  }
}
