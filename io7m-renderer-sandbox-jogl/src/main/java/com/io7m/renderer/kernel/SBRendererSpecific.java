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
import com.io7m.jaux.functional.Option.Some;
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
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;

public final class SBRendererSpecific implements KRenderer
{
  private static void setParameters(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    int texture_units = 1;

    texture_units +=
      SBRendererSpecific.setParametersAlpha(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersNormal(
        gc,
        matrices,
        i,
        array,
        exec,
        texture_units);

    texture_units +=
      SBRendererSpecific.setParametersEnvironment(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersSpecular(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersEmissive(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersAlbedo(gc, i, exec, texture_units);

    SBRendererSpecific.setParametersLight(gc, matrices, light, i, exec);
  }

  private static void setParametersLight(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec)
    throws JCGLException,
      ConstraintError
  {
    if (KShadingProgramCommon.existsLightDirection(exec)) {
      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        {
          KShadingProgramCommon.putLightDirectional(
            gc,
            exec,
            matrices,
            (KDirectional) light);
          break;
        }
        case LIGHT_CONE:
        case LIGHT_SPHERE:
        {
          final RVectorI3F<RSpaceRGB> lc = new RVectorI3F<RSpaceRGB>(1, 1, 1);
          final RVectorI3F<RSpaceWorld> ld =
            new RVectorI3F<RSpaceWorld>(0, 0, 0);
          KShadingProgramCommon.putLightDirectional(
            gc,
            exec,
            matrices,
            new KDirectional(Integer.valueOf(0), ld, lc, 1.0f));
          break;
        }
      }
    }

    if (KShadingProgramCommon.existsLightPosition(exec)) {
      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_CONE:
        {
          KShadingProgramCommon.putLightSpherical(
            gc,
            exec,
            matrices,
            new KSphere(
              Integer.valueOf(0),
              new RVectorI3F<RSpaceRGB>(1, 1, 1),
              1.0f,
              new RVectorI3F<RSpaceWorld>(0, 0, 0),
              10f,
              2));
          break;
        }
        case LIGHT_SPHERE:
        {
          KShadingProgramCommon.putLightSpherical(
            gc,
            exec,
            matrices,
            (KSphere) light);
          break;
        }
      }
    }
  }

  private static int setParametersAlpha(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    final KMaterialAlpha alpha = i.getMaterial().getAlpha();

    if (KShadingProgramCommon.existsMaterialAlphaOpacity(exec)) {
      KShadingProgramCommon.putMaterialAlphaOpacity(
        exec,
        gc,
        alpha.getOpacity());
    }

    return 0;
  }

  private static int setParametersAlbedo(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

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

    if (KShadingProgramCommon.existsTextureAlbedo(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      switch (i.getMaterialLabel().getAlbedo()) {
        case ALBEDO_COLOURED:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureAlbedo(exec, gc, unit);
          KShadingProgramCommon.putAttributeUV(
            gc,
            exec,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case ALBEDO_TEXTURED:
        {
          if (albedo.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) albedo.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureAlbedo(exec, gc, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureAlbedo(exec, gc, unit);
          }

          KShadingProgramCommon.bindAttributeUV(gc, exec, i
            .getMesh()
            .getArrayBuffer());

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private static int setParametersEmissive(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMaterialEmissive emissive = i.getMaterial().getEmissive();

    if (KShadingProgramCommon.existsMaterialEmissiveLevel(exec)) {
      KShadingProgramCommon.putMaterialEmissiveLevel(
        exec,
        gc,
        emissive.getEmission());
    }

    if (KShadingProgramCommon.existsTextureEmissive(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      switch (i.getMaterialLabel().getEmissive()) {
        case EMISSIVE_NONE:
        case EMISSIVE_CONSTANT:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureAlbedo(exec, gc, unit);
          KShadingProgramCommon.putAttributeUV(
            gc,
            exec,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case EMISSIVE_MAPPED:
        {
          if (emissive.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) emissive.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureEmissive(exec, gc, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureEmissive(exec, gc, unit);
          }

          KShadingProgramCommon.bindAttributeUV(gc, exec, i
            .getMesh()
            .getArrayBuffer());

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private static int setParametersEnvironment(
    final JCGLInterfaceCommon gc,
    final KMeshInstance i,
    final JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    final int used_units = 0;

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

    return used_units;
  }

  private static int setParametersNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KMeshInstance i,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    final int used_units = 0;

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

    return used_units;
  }

  private static int setParametersSpecular(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    final int used_units = 0;

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

    return used_units;
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

    this.log = new Log(log, "sb-renderer-specific");
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
   * 
   * @param kLight
   */

  private void renderOpaqueMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light,
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

    if (KShadingProgramCommon.existsMatrixNormal(this.exec_program)) {
      KShadingProgramCommon.putMatrixNormal(
        this.exec_program,
        gc,
        this.matrices.getMatrixNormal());
    }

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

      SBRendererSpecific.setParameters(
        gc,
        this.matrices,
        light,
        i,
        array,
        this.exec_program);

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
        this.renderOpaqueMesh(gc, bl.getLight(), i);
      }
    }

    for (final KBatchUnlit bl : batches.getBatchesOpaqueUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderOpaqueMesh(gc, null, i);
      }
    }
  }

  private void renderTranslucentMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light,
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

    if (KShadingProgramCommon.existsMatrixNormal(this.exec_program)) {
      KShadingProgramCommon.putMatrixNormal(
        this.exec_program,
        gc,
        this.matrices.getMatrixNormal());
    }

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

      SBRendererSpecific.setParameters(
        gc,
        this.matrices,
        light,
        i,
        array,
        this.exec_program);

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
        this.renderTranslucentMesh(gc, bl.getLight(), i);
      }
    }

    for (final KBatchUnlit bl : batches.getBatchesTranslucentUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderTranslucentMesh(gc, null, i);
      }
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    // TODO Auto-generated method stub
  }
}