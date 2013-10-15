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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
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
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RSpaceEye;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.RVectorM4F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;

final class KRendererForwardDiffuse implements KRenderer
{
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull ProgramReference      program_directional;
  private final @Nonnull ProgramReference      program_spherical;
  private final @Nonnull ProgramReference      program_depth;
  private final @Nonnull JCCEExecutionCallable exec_directional;
  private final @Nonnull JCCEExecutionCallable exec_spherical;
  private final @Nonnull JCCEExecutionCallable exec_depth;
  private final @Nonnull KMatrices             matrices;

  KRendererForwardDiffuse(
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

    this.program_directional =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_d_directional",
        log);
    this.exec_directional =
      new JCCEExecutionCallable(this.program_directional);

    this.program_spherical =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_d_spherical",
        log);
    this.exec_spherical = new JCCEExecutionCallable(this.program_spherical);
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
       * Pass 0: render all meshes into the depth buffer, without touching the
       * color buffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(false, false, false, false);
      gc.blendingDisable();
      this.renderDepthPassMeshes(scene, gc);

      /**
       * Pass 1 .. n: render all lit meshes, blending additively, into the
       * framebuffer.
       */

      gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
      gc.depthBufferWriteDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);

      for (final KLight light : scene.getLights()) {
        switch (light.getType()) {
          case LIGHT_CONE:
          {
            throw new UnimplementedCodeException();
          }
          case LIGHT_SPHERE:
          {
            final KSphere slight = (KLight.KSphere) light;
            this.renderLightPassMeshesSpherical(scene, gc, slight);
            break;
          }
          case LIGHT_DIRECTIONAL:
          {
            final KDirectional dlight = (KLight.KDirectional) light;
            this.renderLightPassMeshesDirectional(scene, gc, dlight);
            break;
          }
        }
      }
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
    KShadingProgramCommon.putMatrixNormal(
      e,
      gc,
      this.matrices.getMatrixNormal());

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

    for (final KMeshInstance mesh : scene.getInstances()) {
      this.renderDepthPassMesh(gc, this.exec_depth, mesh);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    this.matrices.matricesMakeFromTransform(instance.getTransform());

    /**
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getDiffuse().getTexture();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    exec.execPrepare(gc);
    exec.execUniformUseExisting("m_projection");
    exec.execUniformUseExisting("light.intensity");
    exec.execUniformUseExisting("light.color");
    exec.execUniformUseExisting("light.direction");
    exec.execUniformPutMatrix4x4F(
      gc,
      "m_modelview",
      this.matrices.getMatrixModelView());
    exec.execUniformPutMatrix3x3F(
      gc,
      "m_normal",
      this.matrices.getMatrixNormal());
    exec.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

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
      exec.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        exec.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        exec.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a_uv =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        exec.execAttributeBind(gc, "v_uv", a_uv);
      } else {
        exec.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

      exec.execSetCallable(new Callable<Void>() {
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
        exec.execRun(gc);
      } catch (final Exception e) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  /**
   * Render all meshes with a directional light shader, using
   * <code>light</code> as input.
   */

  private void renderLightPassMeshesDirectional(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KDirectional light)
    throws ConstraintError,
      JCGLException
  {
    final VectorM4F light_cs = new VectorM4F();
    light_cs.x = light.getDirection().getXF();
    light_cs.y = light.getDirection().getYF();
    light_cs.z = light.getDirection().getZF();
    light_cs.w = 0.0f;

    final Context mc = this.matrices.getMatrixContext();
    final RMatrixReadable4x4F<RTransformView> mv =
      this.matrices.getMatrixView();

    MatrixM4x4F.multiplyVector4FWithContext(mc, mv, light_cs, light_cs);

    final JCCEExecutionCallable e = this.exec_directional;
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(
      gc,
      "m_projection",
      this.matrices.getMatrixProjection());
    e.execUniformPutVector3F(gc, "light.direction", light_cs);
    e.execUniformPutVector3F(gc, "light.color", light.getColour());
    e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
    e.execCancel();

    for (final KMeshInstance mesh : scene.getInstances()) {
      this.renderLightPassMeshDirectional(gc, e, mesh);
    }
  }

  /**
   * Render all meshes with a spherical light shader, using <code>light</code>
   * as input.
   */

  private void renderLightPassMeshesSpherical(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSphere light)
    throws ConstraintError,
      JCGLException
  {
    final RVectorM4F<RSpaceWorld> light_world = new RVectorM4F<RSpaceWorld>();
    final RVectorM4F<RSpaceEye> light_eye = new RVectorM4F<RSpaceEye>();

    light_world.x = light.getPosition().getXF();
    light_world.y = light.getPosition().getYF();
    light_world.z = light.getPosition().getZF();
    light_world.w = 1.0f;

    final Context mc = this.matrices.getMatrixContext();
    final RMatrixReadable4x4F<RTransformView> mv =
      this.matrices.getMatrixView();

    MatrixM4x4F.multiplyVector4FWithContext(mc, mv, light_world, light_eye);

    final JCCEExecutionCallable e = this.exec_spherical;
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(
      gc,
      "m_projection",
      this.matrices.getMatrixProjection());
    e.execUniformPutVector3F(gc, "light.position", light_eye);
    e.execUniformPutVector3F(gc, "light.color", light.getColour());
    e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
    e.execUniformPutFloat(gc, "light.radius", light.getRadius());
    e.execUniformPutFloat(gc, "light.falloff", light.getExponent());
    e.execCancel();

    for (final KMeshInstance mesh : scene.getInstances()) {
      this.renderLightPassMeshSpherical(gc, e, mesh);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable exec,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    this.matrices.matricesMakeFromTransform(instance.getTransform());

    /**
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getDiffuse().getTexture();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    {
      final Option<Texture2DStatic> normal_opt =
        material.getNormal().getTexture();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[2],
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[2]);
      }
    }

    exec.execPrepare(gc);
    exec.execUniformUseExisting("m_projection");
    exec.execUniformUseExisting("light.position");
    exec.execUniformUseExisting("light.color");
    exec.execUniformUseExisting("light.intensity");
    exec.execUniformUseExisting("light.radius");
    exec.execUniformUseExisting("light.falloff");
    exec.execUniformPutMatrix4x4F(
      gc,
      "m_modelview",
      this.matrices.getMatrixModelView());
    exec.execUniformPutMatrix3x3F(
      gc,
      "m_normal",
      this.matrices.getMatrixNormal());
    exec.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

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
      exec.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        exec.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        exec.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a_uv =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        exec.execAttributeBind(gc, "v_uv", a_uv);
      } else {
        exec.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

      exec.execSetCallable(new Callable<Void>() {
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
        exec.execRun(gc);
      } catch (final Exception e) {
        throw new UnreachableCodeException();
      }

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @Override public void close()
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();
    gc.programDelete(this.program_directional);
    gc.programDelete(this.program_spherical);
    // XXX: Depth not deleted
  }
}
