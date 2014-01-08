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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option.None;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.BlendFunction;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
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
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureUnit;
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
import com.io7m.renderer.RTransformProjectiveModelView;
import com.io7m.renderer.RTransformProjectiveProjection;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMutableMatrices.WithInstance;
import com.io7m.renderer.kernel.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.KSceneBatchedForward.BatchTranslucent;
import com.io7m.renderer.kernel.KSceneBatchedForward.BatchTranslucentLit;

public final class SBRendererSpecific implements KRendererForward
{
  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    return null;
  }

  private static void setParameters(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLabelDecider decider,
    final @CheckForNull KLight light,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCBProgram p)
    throws JCGLException,
      ConstraintError
  {
    int texture_units = 1;

    SBRendererSpecific.setParametersAlpha(i, p);

    texture_units +=
      SBRendererSpecific.setParametersNormal(
        gc,
        i,
        array,
        decider,
        p,
        texture_units);

    texture_units +=
      SBRendererSpecific.setParametersEnvironment(
        gc,
        decider,
        i,
        mwi,
        p,
        texture_units);

    texture_units +=
      SBRendererSpecific.setParametersSpecular(
        gc,
        decider,
        i,
        p,
        texture_units);

    texture_units +=
      SBRendererSpecific.setParametersEmissive(
        gc,
        decider,
        i,
        p,
        texture_units);

    texture_units +=
      SBRendererSpecific
        .setParametersAlbedo(gc, decider, i, p, texture_units);

    texture_units +=
      SBRendererSpecific.setParametersLight(gc, light, mwi, p, texture_units);

    if (KShadingProgramCommon.existsAttributeUV(p)) {
      KShadingProgramCommon.bindAttributeUV(p, array);
    }

    if (KShadingProgramCommon.existsMatrixUV(p)) {
      KShadingProgramCommon.putMatrixUV(p, mwi.getMatrixUV());
    }
  }

  private static int setParametersAlbedo(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLabelDecider decider,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMeshInstance actual = i.getInstance();
    final KMaterialAlbedo albedo = actual.getMaterial().getAlbedo();

    if (KShadingProgramCommon.existsMaterialAlbedoColour(p)) {
      KShadingProgramCommon.putMaterialAlbedoColour(p, albedo.getColour());
    }
    if (KShadingProgramCommon.existsMaterialAlbedoMix(p)) {
      KShadingProgramCommon.putMaterialAlbedoMix(p, albedo.getMix());
    }

    if (KShadingProgramCommon.existsTextureAlbedo(p)) {
      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);
      final KMaterialAlbedoLabel label = decider.getAlbedoLabel(actual);

      switch (label) {
        case ALBEDO_COLOURED:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureAlbedo(p, unit);
          KShadingProgramCommon.putAttributeUV(
            p,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case ALBEDO_TEXTURED:
        {
          if (albedo.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) albedo.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureAlbedo(p, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureAlbedo(p, unit);
          }

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private static void setParametersAlpha(
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull JCBProgram p)
    throws JCGLException,
      ConstraintError
  {
    final KMaterialAlpha alpha = i.getInstance().getMaterial().getAlpha();
    if (KShadingProgramCommon.existsMaterialAlphaOpacity(p)) {
      KShadingProgramCommon.putMaterialAlphaOpacity(p, alpha.getOpacity());
    }
  }

  private static int setParametersEmissive(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLabelDecider decider,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMeshInstance actual = i.getInstance();
    final KMaterialEmissive emissive = actual.getMaterial().getEmissive();

    if (KShadingProgramCommon.existsMaterialEmissiveLevel(p)) {
      KShadingProgramCommon.putMaterialEmissiveLevel(
        p,
        emissive.getEmission());
    }

    if (KShadingProgramCommon.existsTextureEmissive(p)) {
      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);
      final KMaterialEmissiveLabel label = decider.getEmissiveLabel(actual);

      switch (label) {
        case EMISSIVE_NONE:
        case EMISSIVE_CONSTANT:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureEmissive(p, unit);
          KShadingProgramCommon.putAttributeUV(
            p,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case EMISSIVE_MAPPED:
        {
          if (emissive.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) emissive.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureEmissive(p, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureEmissive(p, unit);
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
    final @Nonnull KLabelDecider decider,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstanceMatrices mwi,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMeshInstance actual = i.getInstance();
    final KMaterialEnvironment environment =
      actual.getMaterial().getEnvironment();
    final KMaterialEnvironmentLabel label =
      decider.getEnvironmentLabel(actual);

    if (KShadingProgramCommon.existsMaterialEnvironmentMix(p)) {
      KShadingProgramCommon
        .putMaterialEnvironmentMix(p, environment.getMix());
    }

    if (KShadingProgramCommon.existsMaterialEnvironmentReflectionMix(p)) {
      KShadingProgramCommon.putMaterialEnvironmentReflectionMix(
        p,
        environment.getReflectionMix());
    }

    if (KShadingProgramCommon.existsMaterialEnvironmentRefractionIndex(p)) {
      KShadingProgramCommon.putMaterialEnvironmentRefractionIndex(
        p,
        environment.getRefractionIndex());
    }

    if (KShadingProgramCommon.existsMatrixInverseView(p)) {
      KShadingProgramCommon.putMatrixInverseView(
        p,
        mwi.getMatrixViewInverse());
    }

    if (KShadingProgramCommon.existsTextureEnvironment(p)) {
      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);

      switch (label) {
        case ENVIRONMENT_NONE:
        {
          gc.textureCubeStaticUnbind(unit);
          KShadingProgramCommon.putTextureEnvironment(p, unit);
          break;
        }
        case ENVIRONMENT_REFLECTIVE:
        case ENVIRONMENT_REFRACTIVE:
        case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
        case ENVIRONMENT_REFLECTIVE_MAPPED:
        case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
        case ENVIRONMENT_REFRACTIVE_MAPPED:
        {
          if (environment.getTexture().isSome()) {
            final TextureCubeStatic t =
              ((Some<TextureCubeStatic>) environment.getTexture()).value;
            gc.textureCubeStaticBind(unit, t);
            KShadingProgramCommon.putTextureEnvironment(p, unit);
          } else {
            gc.textureCubeStaticUnbind(unit);
            KShadingProgramCommon.putTextureEnvironment(p, unit);
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
    final @CheckForNull KLight light,
    final @Nonnull KMutableMatrices.WithInstance mwi,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    if (KShadingProgramCommon.existsLightDirectional(p)) {
      if ((light == null)
        || (light.getType() != KLight.Type.LIGHT_DIRECTIONAL)) {
        final RVectorI3F<RSpaceRGB> lc = new RVectorI3F<RSpaceRGB>(1, 1, 1);
        final RVectorI3F<RSpaceWorld> ld =
          new RVectorI3F<RSpaceWorld>(0, 0, 0);
        KShadingProgramCommon.putLightDirectional(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          KDirectional.make(Integer.valueOf(0), ld, lc, 1.0f));
      } else {
        KShadingProgramCommon.putLightDirectional(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          (KDirectional) light);
      }
    }

    if (KShadingProgramCommon.existsLightSpherical(p)) {
      if ((light == null) || (light.getType() != KLight.Type.LIGHT_SPHERE)) {
        final KSphere sphere =
          KSphere.make(
            Integer.valueOf(0),
            new RVectorI3F<RSpaceRGB>(1, 1, 1),
            1.0f,
            new RVectorI3F<RSpaceWorld>(0, 0, 0),
            10f,
            2);

        KShadingProgramCommon.putLightSpherical(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          sphere);
      } else {
        KShadingProgramCommon.putLightSpherical(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          (KSphere) light);
      }
    }

    if (KShadingProgramCommon.existsLightProjective(p)) {

      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);
      ++used_units;

      if ((light == null)
        || (light.getType() != KLight.Type.LIGHT_PROJECTIVE)) {
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

        KShadingProgramCommon.putTextureProjection(p, unit);
        final KProjective projective =
          KProjective.make(
            Integer.valueOf(0),
            texture,
            position,
            orientation,
            colour,
            intensity,
            distance,
            falloff,
            projection,
            new None<KShadow>());

        KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          projective);
      } else {
        final KProjective l = (KProjective) light;
        gc.texture2DStaticBind(unit, l.getTexture());
        KShadingProgramCommon.putTextureProjection(p, unit);
        KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
          p,
          mwi.getMatrixContext(),
          mwi.getMatrixView(),
          (KProjective) light);
      }
    }

    return used_units;
  }

  private static int setParametersNormal(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull ArrayBuffer array,
    final @Nonnull KLabelDecider decider,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMeshInstance actual = i.getInstance();
    final KMaterialNormal normal = actual.getMaterial().getNormal();
    final KMaterialNormalLabel nl = decider.getNormalLabel(actual);

    if (KShadingProgramCommon.existsAttributeNormal(p)) {
      switch (nl) {
        case NORMAL_MAPPED:
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(p, array);
          break;
        }
        case NORMAL_NONE:
        {
          KShadingProgramCommon.putAttributeNormal(
            p,
            new RVectorI3F<RSpaceObject>(0, 0, 0));
          break;
        }
      }
    }

    if (KShadingProgramCommon.existsAttributeTangent4(p)) {
      switch (nl) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(p, array);
          break;
        }
        case NORMAL_VERTEX:
        case NORMAL_NONE:
        {
          KShadingProgramCommon.putAttributeTangent4(
            p,
            new RVectorI4F<RSpaceObject>(1, 0, 0, 1));
        }
      }
    }

    if (KShadingProgramCommon.existsTextureNormal(p)) {
      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);

      switch (nl) {
        case NORMAL_NONE:
        case NORMAL_VERTEX:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureNormal(p, unit);
          KShadingProgramCommon.putAttributeUV(
            p,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case NORMAL_MAPPED:
        {
          if (normal.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) normal.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureNormal(p, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureNormal(p, unit);
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
    final @Nonnull KLabelDecider decider,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull JCBProgram p,
    final int texture_units)
    throws JCGLException,
      ConstraintError
  {
    int used_units = 0;

    final KMeshInstance actual = i.getInstance();
    final KMaterialSpecularLabel label = decider.getSpecularLabel(actual);
    final KMaterialSpecular specular = actual.getMaterial().getSpecular();

    if (KShadingProgramCommon.existsMaterialSpecularIntensity(p)) {
      KShadingProgramCommon.putMaterialSpecularIntensity(
        p,
        specular.getIntensity());
    }
    if (KShadingProgramCommon.existsMaterialSpecularExponent(p)) {
      KShadingProgramCommon.putMaterialSpecularExponent(
        p,
        specular.getExponent());
    }

    if (KShadingProgramCommon.existsTextureSpecular(p)) {
      final List<TextureUnit> units = gc.textureGetUnits();
      final TextureUnit unit = units.get(texture_units);

      switch (label) {
        case SPECULAR_NONE:
        case SPECULAR_CONSTANT:
        {
          gc.texture2DStaticUnbind(unit);
          KShadingProgramCommon.putTextureSpecular(p, unit);
          KShadingProgramCommon.putAttributeUV(
            p,
            new RVectorI2F<RSpaceTexture>(0.0f, 0.0f));
          break;
        }
        case SPECULAR_MAPPED:
        {
          if (specular.getTexture().isSome()) {
            final Texture2DStatic t =
              ((Some<Texture2DStatic>) specular.getTexture()).value;
            gc.texture2DStaticBind(unit, t);
            KShadingProgramCommon.putTextureSpecular(p, unit);
          } else {
            gc.texture2DStaticUnbind(unit);
            KShadingProgramCommon.putTextureSpecular(p, unit);
          }

          break;
        }
      }

      ++used_units;
    }

    return used_units;
  }

  private final @Nonnull KProgram                                     program;
  private final @Nonnull JCGLImplementation                           gl;
  private final @Nonnull Log                                          log;
  private final @Nonnull VectorM4F                                    background;
  private final @Nonnull KProgram                                     program_depth;
  private final @Nonnull KMutableMatrices                             matrices;
  private final @Nonnull VectorM2I                                    viewport_size;
  private final @Nonnull RMatrixM4x4F<RTransformProjectiveModelView>  fake_projective_modelview;
  private final @Nonnull RMatrixM4x4F<RTransformProjectiveProjection> fake_projective_projection;
  private final @Nonnull KLabelDecider                                decider;

  public static SBRendererSpecific rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull KLabelDecider decider,
    final @Nonnull Log log,
    final @Nonnull KProgram p)
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError,
      KXMLException
  {
    return new SBRendererSpecific(g, fs, decider, log, p);
  }

  public SBRendererSpecific(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull KLabelDecider decider,
    final @Nonnull Log log,
    final @Nonnull KProgram p)
    throws ConstraintError,
      JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      KXMLException
  {
    this.program = Constraints.constrainNotNull(p, "Program");
    this.decider = Constraints.constrainNotNull(decider, "Decider");
    this.log = new Log(log, "sb-renderer-specific");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.viewport_size = new VectorM2I();

    this.program_depth =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth",
        log);

    this.fake_projective_modelview =
      new RMatrixM4x4F<RTransformProjectiveModelView>();
    this.fake_projective_projection =
      new RMatrixM4x4F<RTransformProjectiveProjection>();
  }

  private void putTextureProjectionMatrixForLight(
    final @Nonnull JCBProgram p,
    final @CheckForNull KLight light,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws JCGLException,
      ConstraintError
  {
    if (KShadingProgramCommon.existsMatrixTextureProjection(p)) {
      if (light == null) {
        KShadingProgramCommon.putMatrixProjectiveProjection(
          p,
          this.fake_projective_projection);
        return;
      }

      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
        {
          KShadingProgramCommon.putMatrixProjectiveProjection(
            p,
            this.fake_projective_projection);
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          final KMutableMatrices.WithProjectiveLight mwp =
            mwi.withProjectiveLight((KProjective) light);
          try {
            KShadingProgramCommon.putMatrixProjectiveProjection(
              p,
              mwp.getMatrixProjectiveProjection());
          } finally {
            mwp.projectiveLightFinish();
          }
          break;
        }
      }
    }
  }

  /**
   * Render the given mesh into the depth buffer, without touching the color
   * buffer.
   */

  @SuppressWarnings("static-method") private void renderDepthPassMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    final KMeshInstance actual = i.getInstance();
    final KMesh mesh = actual.getMesh();
    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    gc.arrayBufferBind(array);
    try {
      KShadingProgramCommon.bindAttributePosition(p, array);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException,
            Throwable
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });
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
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final JCBExecutionAPI e = this.program_depth.getExecutable();
    e.execRun(new JCBExecutorProcedure() {
      @SuppressWarnings("synthetic-access") @Override public void call(
        final @Nonnull JCBProgram p)
        throws ConstraintError,
          JCGLException,
          JCBExecutionException
      {
        KShadingProgramCommon.putMatrixProjection(
          p,
          mwc.getMatrixProjection());

        final KSceneOpaques opaques = scene.getOpaques();
        final Set<KMeshInstanceTransformed> all_opaque = opaques.getAll();

        for (final KMeshInstanceTransformed instance : all_opaque) {
          final KMutableMatrices.WithInstance mwi =
            mwc.withInstance(instance);
          try {
            SBRendererSpecific.this.renderDepthPassMesh(gc, p, instance, mwi);
          } finally {
            mwi.instanceFinish();
          }
        }
      }
    });
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    // TODO Auto-generated method stub
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(
        this.decider,
        this.decider,
        this.decider,
        scene);

    final KMutableMatrices.WithCamera mwc =
      this.matrices.withCamera(scene.getCamera());

    try {
      final FramebufferReferenceUsable output_buffer =
        framebuffer.kFramebufferGetColorFramebuffer();

      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();

      try {
        gc.framebufferDrawBind(output_buffer);
        gc.colorBufferClearV4f(this.background);

        gc.cullingEnable(
          FaceSelection.FACE_BACK,
          FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

        /**
         * Render all opaque meshes into the depth buffer, without touching
         * the color buffer.
         */

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferWriteEnable();
        gc.depthBufferClear(1.0f);
        gc.colorBufferMask(false, false, false, false);
        gc.blendingDisable();
        this.renderDepthPassMeshes(scene, gc, mwc);

        /**
         * Render all opaque meshes, blending additively, into the
         * framebuffer.
         */

        gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
        gc.depthBufferWriteDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
        this.renderOpaqueMeshes(batched, gc, mwc);

        /**
         * Render all translucent meshes into the framebuffer.
         */

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
        gc.depthBufferWriteDisable();
        gc.colorBufferMask(true, true, true, true);
        gc.blendingEnable(
          BlendFunction.BLEND_SOURCE_ALPHA,
          BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
        this.renderTranslucentMeshes(batched, gc, mwc);

      } catch (final JCBExecutionException e) {
        throw new UnreachableCodeException(e);
      } finally {
        gc.framebufferDrawUnbind();
      }

    } finally {
      mwc.cameraFinish();
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    // TODO Auto-generated method stub
  }

  /**
   * Render an opaque mesh to the color buffer.
   * 
   * @param caps
   */

  private void renderOpaqueMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

    this.putTextureProjectionMatrixForLight(p, light, mwi);
    if (KShadingProgramCommon.existsMatrixNormal(p)) {
      KShadingProgramCommon.putMatrixNormal(p, mwi.getMatrixNormal());
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshInstance actual = i.getInstance();
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(p, array);
      SBRendererSpecific.setParameters(
        gc,
        this.decider,
        light,
        i,
        mwi,
        array,
        p);

      p.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private void renderOpaqueMeshes(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws JCGLException,
      ConstraintError,
      JCBExecutionException
  {
    final JCBExecutionAPI e = this.program.getExecutable();
    e.execRun(new JCBExecutorProcedure() {
      @SuppressWarnings("synthetic-access") @Override public void call(
        final @Nonnull JCBProgram p)
        throws ConstraintError,
          JCGLException,
          JCBExecutionException
      {
        KShadingProgramCommon.putMatrixProjection(
          p,
          mwc.getMatrixProjection());

        final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches =
          batched.getBatchesOpaqueLit();

        for (final KLight light : batches.keySet()) {
          final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> by_material =
            batches.get(light);
          for (final KMaterialForwardLabel label : by_material.keySet()) {
            final List<KMeshInstanceTransformed> instances =
              by_material.get(label);

            for (final KMeshInstanceTransformed i : instances) {
              final KMutableMatrices.WithInstance mwi = mwc.withInstance(i);
              try {
                SBRendererSpecific.this
                  .renderOpaqueMesh(gc, p, light, i, mwi);
              } finally {
                mwi.instanceFinish();
              }
            }
          }
        }
      }
    });
  }

  private void renderTranslucentMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    /**
     * Upload matrices.
     */

    final JCBExecutionAPI e = this.program.getExecutable();
    e.execRun(new JCBExecutorProcedure() {
      @SuppressWarnings("synthetic-access") @Override public void call(
        final @Nonnull JCBProgram p)
        throws ConstraintError,
          JCGLException,
          JCBExecutionException,
          Throwable
      {
        KShadingProgramCommon.putMatrixProjectionReuse(p);
        SBRendererSpecific.this.putTextureProjectionMatrixForLight(
          p,
          light,
          mwi);
        KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());
        if (KShadingProgramCommon.existsMatrixNormal(p)) {
          KShadingProgramCommon.putMatrixNormal(p, mwi.getMatrixNormal());
        }

        /**
         * Associate array attributes with program attributes, and draw mesh.
         */

        try {
          final KMeshInstance actual = i.getInstance();
          final KMesh mesh = actual.getMesh();
          final ArrayBuffer array = mesh.getArrayBuffer();
          final IndexBuffer indices = mesh.getIndexBuffer();

          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(p, array);
          SBRendererSpecific.setParameters(
            gc,
            SBRendererSpecific.this.decider,
            light,
            i,
            mwi,
            array,
            p);

          p.programExecute(new JCBProgramProcedure() {
            @Override public void call()
              throws ConstraintError,
                JCGLException,
                Throwable
            {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            }
          });
        } finally {
          gc.arrayBufferUnbind();
        }
      }
    });
  }

  private void renderTranslucentMeshes(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws JCGLException,
      ConstraintError,
      JCBExecutionException
  {
    final List<BatchTranslucent> translucents =
      batched.getBatchesTranslucent();

    for (int index = 0; index < translucents.size(); ++index) {
      final BatchTranslucent batch = translucents.get(index);
      final KMeshInstanceTransformed i = batch.getInstance();

      final WithInstance mwi = mwc.withInstance(i);
      try {
        if (batch instanceof BatchTranslucentLit) {
          final List<KLight> lights =
            ((BatchTranslucentLit) batch).getLights();

          boolean first_light = true;
          for (final KLight light : lights) {
            if (first_light) {
              gc.blendingEnable(
                BlendFunction.BLEND_ONE,
                BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
            } else {
              gc.blendingEnable(
                BlendFunction.BLEND_ONE,
                BlendFunction.BLEND_ONE);
            }

            this.renderTranslucentMesh(gc, light, i, mwi);
            first_light = false;
          }
        } else {
          gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

          this.renderTranslucentMeshUnlit(gc, i, mwi);
        }
      } finally {
        mwi.instanceFinish();
      }
    }
  }

  private void renderTranslucentMeshUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws JCGLException,
      JCBExecutionException,
      ConstraintError
  {
    /**
     * Upload matrices.
     */

    final JCBExecutionAPI e = this.program.getExecutable();
    e.execRun(new JCBExecutorProcedure() {
      @SuppressWarnings("synthetic-access") @Override public void call(
        final @Nonnull JCBProgram p)
        throws ConstraintError,
          JCGLException,
          JCBExecutionException,
          Throwable
      {
        KShadingProgramCommon.putMatrixProjectionReuse(p);
        SBRendererSpecific.this.putTextureProjectionMatrixForLight(
          p,
          null,
          mwi);
        KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());
        if (KShadingProgramCommon.existsMatrixNormal(p)) {
          KShadingProgramCommon.putMatrixNormal(p, mwi.getMatrixNormal());
        }

        /**
         * Associate array attributes with program attributes, and draw mesh.
         */

        try {
          final KMeshInstance actual = i.getInstance();
          final KMesh mesh = actual.getMesh();
          final ArrayBuffer array = mesh.getArrayBuffer();
          final IndexBuffer indices = mesh.getIndexBuffer();

          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(p, array);
          SBRendererSpecific.setParameters(
            gc,
            SBRendererSpecific.this.decider,
            null,
            i,
            mwi,
            array,
            p);

          p.programExecute(new JCBProgramProcedure() {
            @Override public void call()
              throws ConstraintError,
                JCGLException,
                Throwable
            {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            }
          });
        } finally {
          gc.arrayBufferUnbind();
        }
      }
    });
  }

  @Override public String rendererGetName()
  {
    return "specific";
  }

  @Override public
    <A, E extends Throwable, V extends KRendererVisitor<A, E>>
    A
    rendererVisitableAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        ConstraintError,
        JCGLCompileException,
        JCGLUnsupportedException,
        IOException,
        KXMLException
  {
    return v.rendererVisitForward(this);
  }
}
