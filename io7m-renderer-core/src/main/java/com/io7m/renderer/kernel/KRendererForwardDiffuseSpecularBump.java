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
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RSpaceEye;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorM4F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;

final class KRendererForwardDiffuseSpecularBump implements KRenderer
{
  private static final int                     SHININESS = 64;

  private final @Nonnull MatrixM4x4F           matrix_modelview;
  private final @Nonnull MatrixM4x4F           matrix_model;
  private final @Nonnull MatrixM4x4F           matrix_view;
  private final @Nonnull MatrixM3x3F           matrix_normal;
  private final @Nonnull MatrixM4x4F           matrix_projection;
  private final @Nonnull MatrixM4x4F.Context   matrix_context;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull ProgramReference      program_depth;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull JCCEExecutionCallable exec_depth;
  private final @Nonnull ProgramReference      program_pb_directional;
  private final @Nonnull JCCEExecutionCallable exec_pb_directional;
  private final @Nonnull ProgramReference      program_pb_spherical;
  private final @Nonnull JCCEExecutionCallable exec_pb_spherical;
  private final @Nonnull ProgramReference      program_cb_directional;
  private final @Nonnull JCCEExecutionCallable exec_cb_directional;
  private final @Nonnull ProgramReference      program_cb_spherical;
  private final @Nonnull JCCEExecutionCallable exec_cb_spherical;

  KRendererForwardDiffuseSpecularBump(
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
    this.log = new Log(log, "krenderer-forward-diffuse-specular-bump");
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

    this.program_pb_directional =
      KShaderUtilities.makeParasolProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsn_pb_directional",
        log);
    this.exec_pb_directional =
      new JCCEExecutionCallable(this.program_pb_directional);

    this.program_pb_spherical =
      KShaderUtilities.makeParasolProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsn_pb_spherical",
        log);
    this.exec_pb_spherical =
      new JCCEExecutionCallable(this.program_pb_spherical);

    this.program_cb_directional =
      KShaderUtilities.makeParasolProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsn_cb_directional",
        log);
    this.exec_cb_directional =
      new JCCEExecutionCallable(this.program_cb_directional);

    this.program_cb_spherical =
      KShaderUtilities.makeParasolProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsn_cb_spherical",
        log);
    this.exec_cb_spherical =
      new JCCEExecutionCallable(this.program_cb_spherical);

    this.program_depth =
      KShaderUtilities.makeParasolProgramSingleOutput(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth",
        log);
    this.exec_depth = new JCCEExecutionCallable(this.program_depth);
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
    final @Nonnull JCCEExecutionCallable exec,
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

    /**
     * Upload matrices, set textures.
     */

    exec.execPrepare(gc);
    exec.execUniformUseExisting("m_projection");
    exec.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();
      final ArrayBufferAttribute a_pos = array.getAttribute("position");

      gc.arrayBufferBind(array);
      exec.execAttributeBind(gc, "v_position", a_pos);
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
   * Render all meshes into the depth buffer, without writing anything to the
   * color buffer.
   */

  private void renderDepthPassMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    final JCCEExecutionCallable e = this.exec_depth;
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
    e.execCancel();

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderDepthPassMesh(gc, e, mesh);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshDirectional(
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
      this.renderLightPassMeshDirectionalWithProvidedBitangents(gc, instance);
    } else {
      Constraints.constrainArbitrary(
        array.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName()),
        "Mesh has tangent4");
      this.renderLightPassMeshDirectionalWithComputedBitangents(gc, instance);
    }
  }

  /**
   * Render the given mesh, lit with the given program, assuming computed
   * bitangents.
   */

  private void renderLightPassMeshDirectionalWithComputedBitangents(
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
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getTextureDiffuse0();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      }
    }

    {
      final Option<Texture2DStatic> diffuse_1_opt =
        material.getTextureDiffuse1();
      if (diffuse_1_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[1],
          ((Option.Some<Texture2DStatic>) diffuse_1_opt).value);
      }
    }

    {
      final Option<Texture2DStatic> normal_opt = material.getTextureNormal();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[2],
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      }
    }

    final JCCEExecutionCallable e = this.exec_cb_directional;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.direction");
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_normal", texture_units[2]);
    e.execUniformPutFloat(
      gc,
      "shininess",
      KRendererForwardDiffuseSpecularBump.SHININESS);

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
      e.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
        e.execAttributeBind(gc, "v_tangent4", a);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        e.execAttributeBind(gc, "v_uv", a);
      } else {
        e.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

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
   * Render the given mesh, lit with the given program, assuming provided
   * bitangents.
   */

  private void renderLightPassMeshDirectionalWithProvidedBitangents(
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
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getTextureDiffuse0();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      }
    }

    {
      final Option<Texture2DStatic> diffuse_1_opt =
        material.getTextureDiffuse1();
      if (diffuse_1_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[1],
          ((Option.Some<Texture2DStatic>) diffuse_1_opt).value);
      }
    }

    {
      final Option<Texture2DStatic> normal_opt = material.getTextureNormal();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[2],
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      }
    }

    final JCCEExecutionCallable e = this.exec_pb_directional;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.direction");
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_normal", texture_units[2]);
    e.execUniformPutFloat(
      gc,
      "shininess",
      KRendererForwardDiffuseSpecularBump.SHININESS);

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
      e.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT3.getName());
        e.execAttributeBind(gc, "v_tangent3", a);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_BITANGENT.getName());
        e.execAttributeBind(gc, "v_bitangent", a);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        e.execAttributeBind(gc, "v_uv", a);
      } else {
        e.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

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

    MatrixM4x4F.multiplyVector4FWithContext(
      this.matrix_context,
      this.matrix_view,
      light_cs,
      light_cs);

    {
      final JCCEExecutionCallable e = this.exec_cb_directional;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.direction", light_cs);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execCancel();
    }

    {
      final JCCEExecutionCallable e = this.exec_pb_directional;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.direction", light_cs);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execCancel();
    }

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderLightPassMeshDirectional(gc, mesh);
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

    MatrixM4x4F.multiplyVector4FWithContext(
      this.matrix_context,
      this.matrix_view,
      light_world,
      light_eye);

    {
      final JCCEExecutionCallable e = this.exec_pb_spherical;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.position", light_eye);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execUniformPutFloat(gc, "light.radius", light.getRadius());
      e.execUniformPutFloat(gc, "light.falloff", light.getExponent());
      e.execCancel();
    }

    {
      final JCCEExecutionCallable e = this.exec_pb_spherical;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.position", light_eye);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execUniformPutFloat(gc, "light.radius", light.getRadius());
      e.execUniformPutFloat(gc, "light.falloff", light.getExponent());
      e.execCancel();
    }

    {
      final JCCEExecutionCallable e = this.exec_cb_spherical;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.position", light_eye);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execUniformPutFloat(gc, "light.radius", light.getRadius());
      e.execUniformPutFloat(gc, "light.falloff", light.getExponent());
      e.execCancel();
    }

    {
      final JCCEExecutionCallable e = this.exec_cb_spherical;
      e.execPrepare(gc);
      e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
      e.execUniformPutVector3F(gc, "light.position", light_eye);
      e.execUniformPutVector3F(gc, "light.color", light.getColour());
      e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
      e.execUniformPutFloat(gc, "light.radius", light.getRadius());
      e.execUniformPutFloat(gc, "light.falloff", light.getExponent());
      e.execCancel();
    }

    for (final KMeshInstance mesh : scene.getMeshes()) {
      this.renderLightPassMeshSpherical(gc, mesh);
    }
  }

  /**
   * Render the given mesh, lit with the given program.
   */

  private void renderLightPassMeshSpherical(
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
      this.renderLightPassMeshSphericalWithProvidedBitangents(gc, instance);
    } else {
      Constraints.constrainArbitrary(
        array.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName()),
        "Mesh has tangent4");
      this.renderLightPassMeshSphericalWithComputedBitangents(gc, instance);
    }
  }

  /**
   * Render the given mesh, lit with the given program, assuming provided
   * bitangents.
   */

  private void renderLightPassMeshSphericalWithComputedBitangents(
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
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getTextureDiffuse0();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    {
      final Option<Texture2DStatic> diffuse_1_opt =
        material.getTextureDiffuse1();
      if (diffuse_1_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[1],
          ((Option.Some<Texture2DStatic>) diffuse_1_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[1]);
      }
    }

    {
      final Option<Texture2DStatic> normal_opt = material.getTextureNormal();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[2],
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[2]);
      }
    }

    final JCCEExecutionCallable e = this.exec_cb_spherical;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.position");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.radius");
    e.execUniformUseExisting("light.falloff");
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_normal", texture_units[2]);
    e.execUniformPutFloat(
      gc,
      "shininess",
      KRendererForwardDiffuseSpecularBump.SHININESS);

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
      e.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
        e.execAttributeBind(gc, "v_tangent4", a);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a_uv =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        e.execAttributeBind(gc, "v_uv", a_uv);
      } else {
        e.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

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
   * Render the given mesh, lit with the given program, assuming provided
   * bitangents.
   */

  private void renderLightPassMeshSphericalWithProvidedBitangents(
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
     * Upload matrices, set textures.
     */

    final KMaterial material = instance.getMaterial();
    final TextureUnit[] texture_units = gc.textureGetUnits();

    {
      final Option<Texture2DStatic> diffuse_0_opt =
        material.getTextureDiffuse0();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    {
      final Option<Texture2DStatic> diffuse_1_opt =
        material.getTextureDiffuse1();
      if (diffuse_1_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[1],
          ((Option.Some<Texture2DStatic>) diffuse_1_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[1]);
      }
    }

    {
      final Option<Texture2DStatic> normal_opt = material.getTextureNormal();
      if (normal_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[2],
          ((Option.Some<Texture2DStatic>) normal_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[2]);
      }
    }

    final JCCEExecutionCallable e = this.exec_pb_spherical;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.position");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.radius");
    e.execUniformUseExisting("light.falloff");
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_normal", texture_units[2]);
    e.execUniformPutFloat(
      gc,
      "shininess",
      KRendererForwardDiffuseSpecularBump.SHININESS);

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
      e.execAttributeBind(gc, "v_position", a_pos);

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_TANGENT3.getName());
        e.execAttributeBind(gc, "v_tangent3", a);
      }

      {
        final ArrayBufferAttribute a =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_BITANGENT.getName());
        e.execAttributeBind(gc, "v_bitangent", a);
      }

      if (array.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
        final ArrayBufferAttribute a_uv =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_UV.getName());
        e.execAttributeBind(gc, "v_uv", a_uv);
      } else {
        e.execAttributePutVector2F(gc, "v_uv", VectorI2F.ZERO);
      }

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

  @Override public void setBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }
}
