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
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferReferenceUsable;
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
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformTextureProjection;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMeshInstanceMaterialLabel.Environment;
import com.io7m.renderer.kernel.KMeshInstanceMaterialLabel.Normal;
import com.io7m.renderer.kernel.KMeshInstanceMaterialLabel.Specular;

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
      SBRendererSpecific.setParametersEnvironment(
        gc,
        matrices,
        i,
        exec,
        texture_units);

    texture_units +=
      SBRendererSpecific.setParametersSpecular(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersEmissive(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersAlbedo(gc, i, exec, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersLight(
        gc,
        matrices,
        light,
        i,
        exec,
        texture_units);

    if (KShadingProgramCommon.existsAttributeUV(exec)) {
      KShadingProgramCommon.bindAttributeUV(gc, exec, i
        .getMesh()
        .getArrayBuffer());
    }

    if (KShadingProgramCommon.existsMatrixUV(exec)) {
      KShadingProgramCommon.putMatrixUV(gc, exec, matrices.getMatrixUV());
    }
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

    final KMaterialAlbedo albedo = i.getMaterial().getAlbedo();

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

          break;
        }
      }

      ++used_units;
    }

    return used_units;
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
          KShadingProgramCommon.putTextureEmissive(exec, gc, unit);
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

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private static int setParametersEnvironment(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMaterialEnvironment environment = i.getMaterial().getEnvironment();
    final Environment el = i.getMaterialLabel().getEnvironment();

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

    if (KShadingProgramCommon.existsMatrixInverseView(exec)) {
      KShadingProgramCommon.putMatrixInverseView(
        exec,
        gc,
        matrices.getMatrixViewInverse());
    }

    if (KShadingProgramCommon.existsTextureEnvironment(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      switch (el) {
        case ENVIRONMENT_NONE:
        {
          gc.textureCubeStaticUnbind(unit);
          KShadingProgramCommon.putTextureEnvironment(exec, gc, unit);
          break;
        }
        case ENVIRONMENT_REFLECTIVE:
        case ENVIRONMENT_REFRACTIVE:
        case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
        {
          if (environment.getTexture().isSome()) {
            final TextureCubeStatic t =
              ((Some<TextureCubeStatic>) environment.getTexture()).value;
            gc.textureCubeStaticBind(unit, t);
            KShadingProgramCommon.putTextureEnvironment(exec, gc, unit);
          } else {
            gc.textureCubeStaticUnbind(unit);
            KShadingProgramCommon.putTextureEnvironment(exec, gc, unit);
          }
          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private static int setParametersLight(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i,
    final @Nonnull JCCEExecutionCallable exec,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    if (KShadingProgramCommon.existsLightDirectional(exec)) {
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
        case LIGHT_PROJECTIVE:
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

    if (KShadingProgramCommon.existsLightSpherical(exec)) {
      switch (light.getType()) {
        case LIGHT_PROJECTIVE:
        case LIGHT_DIRECTIONAL:
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

    if (KShadingProgramCommon.existsLightProjective(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      ++used_units;

      switch (light.getType()) {
        case LIGHT_PROJECTIVE:
        {
          final KProjective l = (KProjective) light;

          gc.texture2DStaticBind(unit, l.getTexture());

          KShadingProgramCommon.putTextureProjection(exec, gc, unit);
          KShadingProgramCommon.putLightProjective(
            gc,
            exec,
            matrices,
            (KProjective) light);
          break;
        }
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          gc.texture2DStaticUnbind(unit);

          final Texture2DStaticUsable texture = null; // XXX: Ignored anyway
          final RVectorReadable3F<RSpaceWorld> position =
            new RVectorI3F<RSpaceWorld>(1.0f, 1.0f, -1.0f);
          final QuaternionI4F orientation = new QuaternionI4F();
          final RVectorReadable3F<RSpaceRGB> colour =
            new RVectorI3F<RSpaceRGB>(1.0f, 1.0f, 1.0f);
          final float intensity = 1.0f;
          final float distance = 1.0f;
          final float falloff = 1.0f;
          final RMatrixI4x4F<RTransformProjection> projection =
            new RMatrixI4x4F<RTransformProjection>();

          KShadingProgramCommon.putTextureProjection(exec, gc, unit);
          KShadingProgramCommon.putLightProjective(
            gc,
            exec,
            matrices,
            new KProjective(
              Integer.valueOf(0),
              texture,
              position,
              orientation,
              colour,
              intensity,
              distance,
              falloff,
              projection));
          break;
        }
      }
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
    int used_units = 0;

    final KMaterialNormal normal = i.getMaterial().getNormal();
    final Normal nl = i.getMaterialLabel().getNormal();

    if (KShadingProgramCommon.existsAttributeNormal(exec)) {
      switch (nl) {
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

    if (KShadingProgramCommon.existsAttributeTangent4(exec)) {
      switch (nl) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(gc, exec, array);
          break;
        }
        case NORMAL_VERTEX:
        case NORMAL_NONE:
        {
          KShadingProgramCommon.putAttributeTangent4(
            gc,
            exec,
            new RVectorI4F<RSpaceObject>(1, 0, 0, 1));
        }
      }
    }

    if (KShadingProgramCommon.existsTextureNormal(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      switch (nl) {
        case NORMAL_NONE:
        case NORMAL_VERTEX:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureNormal(exec, gc, unit);
          KShadingProgramCommon.putAttributeUV(
            gc,
            exec,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case NORMAL_MAPPED:
        {
          if (normal.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) normal.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureNormal(exec, gc, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureNormal(exec, gc, unit);
          }

          break;
        }
      }

      ++used_units;
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
    int used_units = 0;

    final Specular sl = i.getMaterialLabel().getSpecular();
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

    if (KShadingProgramCommon.existsTextureSpecular(exec)) {
      final TextureUnit[] units = gc.textureGetUnits();
      final TextureUnit unit = units[texture_units];

      switch (sl) {
        case SPECULAR_NONE:
        case SPECULAR_CONSTANT:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureSpecular(exec, gc, unit);
          KShadingProgramCommon.putAttributeUV(
            gc,
            exec,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case SPECULAR_MAPPED:
        {
          if (specular.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) specular.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureSpecular(exec, gc, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureSpecular(exec, gc, unit);
          }

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private final @Nonnull ProgramReference                          program;
  private final @Nonnull JCGLImplementation                        gl;
  private final @Nonnull Log                                       log;
  private final @Nonnull VectorM4F                                 background;
  private final @Nonnull ProgramReference                          program_depth;
  private final @Nonnull JCCEExecutionCallable                     exec_depth;
  private final @Nonnull KMatrices                                 matrices;
  private final @Nonnull JCCEExecutionCallable                     exec_program;
  private final @Nonnull VectorM2I                                 viewport_size;
  private final @Nonnull RMatrixM4x4F<RTransformTextureProjection> fake_texture_projection;
  private @Nonnull KFramebufferBasic                               framebuffer;

  public SBRendererSpecific(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log,
    final @Nonnull AreaInclusive size,
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
    this.viewport_size = new VectorM2I();

    this.program_depth =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth",
        log);
    this.exec_depth = new JCCEExecutionCallable(this.program_depth);

    this.fake_texture_projection =
      new RMatrixM4x4F<RTransformTextureProjection>();

    this.rendererFramebufferResize(size);
  }

  private void putTextureProjectionMatrixForLight(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light)
    throws JCGLException,
      ConstraintError
  {
    if (KShadingProgramCommon
      .existsMatrixTextureProjection(this.exec_program)) {
      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          KShadingProgramCommon.putMatrixTextureProjection(
            gc,
            this.exec_program,
            this.fake_texture_projection);
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          this.matrices.matricesMakeFromLight((KProjective) light);
          KShadingProgramCommon.putMatrixTextureProjection(
            gc,
            this.exec_program,
            this.matrices.getTextureProjection());
          break;
        }
      }
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
    this.exec_depth.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(
      this.exec_depth,
      gc,
      this.matrices.getMatrixProjection());
    this.exec_depth.execCancel();

    final KBatches batches = scene.getBatches();

    for (final KBatchOpaqueLit bl : batches.getBatchesOpaqueLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderDepthPassMesh(gc, this.exec_depth, i);
      }
    }

    for (final KBatchOpaqueUnlit bl : batches.getBatchesOpaqueUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderDepthPassMesh(gc, this.exec_depth, i);
      }
    }
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    // TODO Auto-generated method stub
  }

  @Override public void rendererEvaluate(
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    this.matrices.matricesBegin();
    this.matrices.matricesMakeFromCamera(scene.getCamera());

    final FramebufferReferenceUsable output_buffer =
      this.framebuffer.kframebufferGetOutputBuffer();

    final AreaInclusive area = this.framebuffer.kframebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();

    try {
      gc.framebufferDrawBind(output_buffer);
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

  @Override public @Nonnull KFramebufferBasicUsable rendererFramebufferGet()
  {
    return this.framebuffer;
  }

  @Override public void rendererFramebufferResize(
    final @Nonnull AreaInclusive size)
    throws JCGLException,
      ConstraintError,
      JCGLUnsupportedException
  {
    if (this.framebuffer != null) {
      this.framebuffer.kframebufferDelete(this.gl);
    }

    this.framebuffer = KFramebufferCommon.allocateBasicRGBA(this.gl, size);
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    // TODO Auto-generated method stub
  }

  /**
   * Render an opaque mesh to the color buffer.
   */

  private void renderOpaqueMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i)
    throws ConstraintError,
      JCGLException
  {
    this.matrices.matricesMakeFromTransform(i.getTransform());
    this.matrices.matricesMakeTextureFromInstance(i);

    /**
     * Upload matrices.
     */

    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(this.exec_program);
    KShadingProgramCommon.putMatrixModelView(
      this.exec_program,
      gc,
      this.matrices.getMatrixModelView());
    this.putTextureProjectionMatrixForLight(gc, light);
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

    for (final KBatchOpaqueLit bl : batches.getBatchesOpaqueLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        this.renderOpaqueMesh(gc, bl.getLight(), i);
      }
    }

    for (final KBatchOpaqueUnlit bl : batches.getBatchesOpaqueUnlit()) {
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
    this.matrices.matricesMakeFromTransform(i.getTransform());
    this.matrices.matricesMakeTextureFromInstance(i);

    /**
     * Upload matrices.
     */

    this.exec_program.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(this.exec_program);
    this.putTextureProjectionMatrixForLight(gc, light);
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
    final KBatches batches = scene.getBatches();

    for (final KBatchTranslucent bl : batches.getBatchesTranslucent()) {
      final KMeshInstance i = bl.getInstance();

      boolean first_light = true;
      for (final KLight light : bl.getLights()) {
        if (first_light) {
          gc.blendingEnable(
            BlendFunction.BLEND_ONE,
            BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
        } else {
          gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
        }

        this.renderTranslucentMesh(gc, light, i);
        first_light = false;
      }
    }
  }
}
