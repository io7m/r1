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
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
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
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RSpaceEye;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorM4F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KSphere;

final class KRendererForwardDiffuseSpecular implements KRenderer
{
  private final @Nonnull MatrixM4x4F           matrix_modelview;
  private final @Nonnull MatrixM4x4F           matrix_model;
  private final @Nonnull MatrixM4x4F           matrix_view;
  private final @Nonnull MatrixM3x3F           matrix_normal;
  private final @Nonnull MatrixM4x4F           matrix_projection;
  private final @Nonnull MatrixM4x4F.Context   matrix_context;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull ProgramReference      program_spherical;
  private final @Nonnull ProgramReference      program_directional;
  private final @Nonnull ProgramReference      program_spherical_map;
  private final @Nonnull ProgramReference      program_directional_map;
  private final @Nonnull ProgramReference      program_depth;
  private final @Nonnull Log                   log;
  private final @Nonnull VectorM4F             background;
  private final @Nonnull JCCEExecutionCallable exec_directional;
  private final @Nonnull JCCEExecutionCallable exec_spherical;
  private final @Nonnull JCCEExecutionCallable exec_directional_map;
  private final @Nonnull JCCEExecutionCallable exec_spherical_map;
  private final @Nonnull JCCEExecutionCallable exec_depth;
  private final @Nonnull VectorM2I             viewport_size;
  private @Nonnull KFramebuffer                framebuffer;

  KRendererForwardDiffuseSpecular(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull AreaInclusive size,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      JCGLException,
      FilesystemError,
      IOException
  {
    this.log = new Log(log, "krenderer-forward-diffuse-specular");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.viewport_size = new VectorM2I();
    this.matrix_modelview = new MatrixM4x4F();
    this.matrix_projection = new MatrixM4x4F();
    this.matrix_model = new MatrixM4x4F();
    this.matrix_view = new MatrixM4x4F();
    this.matrix_normal = new MatrixM3x3F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.transform_context = new KTransform.Context();

    this.program_directional =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_ds_directional",
        log);
    this.exec_directional =
      new JCCEExecutionCallable(this.program_directional);

    this.program_spherical =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_ds_spherical",
        log);
    this.exec_spherical = new JCCEExecutionCallable(this.program_spherical);

    this.program_directional_map =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsm_directional",
        log);
    this.exec_directional_map =
      new JCCEExecutionCallable(this.program_directional_map);

    this.program_spherical_map =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "forward_dsm_spherical",
        log);
    this.exec_spherical_map =
      new JCCEExecutionCallable(this.program_spherical_map);

    this.program_depth =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "depth",
        log);
    this.exec_depth = new JCCEExecutionCallable(this.program_depth);

    this.rendererFramebufferResize(size);
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

    for (final KMeshInstance mesh : scene.getInstances()) {
      this.renderDepthPassMesh(gc, e, mesh);
    }
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();
    gc.programDelete(this.program_directional);
    gc.programDelete(this.program_directional_map);
    gc.programDelete(this.program_spherical);
    gc.programDelete(this.program_spherical_map);
    gc.programDelete(this.program_depth);
  }

  @Override public void rendererEvaluate(
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KCamera camera = scene.getCamera();
    camera.getProjectionMatrix().makeMatrixM4x4F(this.matrix_projection);
    camera.getViewMatrix().makeMatrixM4x4F(this.matrix_view);

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
          case LIGHT_PROJECTIVE:
          {
            throw new UnimplementedCodeException();
          }
        }
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  @Override public @Nonnull KFramebufferUsable rendererFramebufferGet()
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
    VectorM4F.copy(rgba, this.background);
  }

  private void renderInitCommonDirectionalUniforms(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KDirectional light,
    final @Nonnull VectorM4F light_cs,
    final @Nonnull JCCEExecutionCallable e)
    throws ConstraintError,
      JCGLException
  {
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
    e.execUniformPutVector3F(gc, "light.direction", light_cs);
    e.execUniformPutVector3F(gc, "light.color", light.getColour());
    e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
    e.execCancel();
  }

  private void renderInitCommonSphericalUniforms(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KSphere light,
    final @Nonnull RVectorM4F<RSpaceEye> light_eye,
    final @Nonnull JCCEExecutionCallable e)
    throws ConstraintError,
      JCGLException
  {
    e.execPrepare(gc);
    e.execUniformPutMatrix4x4F(gc, "m_projection", this.matrix_projection);
    e.execUniformPutVector3F(gc, "light.position", light_eye);
    e.execUniformPutVector3F(gc, "light.color", light.getColour());
    e.execUniformPutFloat(gc, "light.intensity", light.getIntensity());
    e.execUniformPutFloat(gc, "light.radius", light.getRadius());
    e.execUniformPutFloat(gc, "light.falloff", light.getFalloff());
    e.execCancel();
  }

  private void renderLightPassMeshDirectional(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KMaterial material = instance.getMaterial();
    if (material.getSpecular().getTexture().isSome()) {
      this.renderLightPassMeshDirectionalWithSpecularMap(gc, instance);
    } else {
      this.renderLightPassMeshDirectionalWithoutSpecularMap(gc, instance);
    }
  }

  private void renderLightPassMeshDirectionalWithoutSpecularMap(
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
        material.getAlbedo().getTexture();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    final JCCEExecutionCallable e = this.exec_directional;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.direction");
    e.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
    e.execUniformPutFloat(gc, "material.specular_exponent", material
      .getSpecular()
      .getExponent());
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

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
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
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

  private void renderLightPassMeshDirectionalWithSpecularMap(
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
        material.getAlbedo().getTexture();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    {
      final Option<Texture2DStatic> specular_opt =
        material.getSpecular().getTexture();
      gc.texture2DStaticBind(
        texture_units[3],
        ((Option.Some<Texture2DStatic>) specular_opt).value);
    }

    final JCCEExecutionCallable e = this.exec_directional_map;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.direction");
    e.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
    e.execUniformPutFloat(gc, "material.specular_exponent", material
      .getSpecular()
      .getExponent());
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_specular", texture_units[3]);

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
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
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
      final JCCEExecutionCallable e = this.exec_directional;
      this.renderInitCommonDirectionalUniforms(gc, light, light_cs, e);
    }

    {
      final JCCEExecutionCallable e = this.exec_directional_map;
      this.renderInitCommonDirectionalUniforms(gc, light, light_cs, e);
    }

    for (final KMeshInstance mesh : scene.getInstances()) {
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
      final JCCEExecutionCallable e = this.exec_spherical;
      this.renderInitCommonSphericalUniforms(gc, light, light_eye, e);
    }

    {
      final JCCEExecutionCallable e = this.exec_spherical_map;
      this.renderInitCommonSphericalUniforms(gc, light, light_eye, e);
    }

    for (final KMeshInstance mesh : scene.getInstances()) {
      this.renderLightPassMeshSpherical(gc, mesh);
    }
  }

  private void renderLightPassMeshSpherical(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException
  {
    final KMaterial material = instance.getMaterial();
    if (material.getSpecular().getTexture().isSome()) {
      this.renderLightPassMeshSphericalWithSpecularMap(gc, instance);
    } else {
      this.renderLightPassMeshSphericalWithoutSpecularMap(gc, instance);
    }
  }

  private void renderLightPassMeshSphericalWithoutSpecularMap(
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
        material.getAlbedo().getTexture();
      if (diffuse_0_opt.isSome()) {
        gc.texture2DStaticBind(
          texture_units[0],
          ((Option.Some<Texture2DStatic>) diffuse_0_opt).value);
      } else {
        gc.texture2DStaticUnbind(texture_units[0]);
      }
    }

    final JCCEExecutionCallable e = this.exec_spherical;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.position");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.radius");
    e.execUniformUseExisting("light.falloff");
    e.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
    e.execUniformPutFloat(gc, "material.specular_exponent", material
      .getSpecular()
      .getExponent());
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);

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
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
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

  private void renderLightPassMeshSphericalWithSpecularMap(
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
        material.getAlbedo().getTexture();
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

    {
      final Option<Texture2DStatic> specular_opt =
        material.getSpecular().getTexture();
      gc.texture2DStaticBind(
        texture_units[3],
        ((Option.Some<Texture2DStatic>) specular_opt).value);
    }

    final JCCEExecutionCallable e = this.exec_spherical_map;
    e.execPrepare(gc);
    e.execUniformUseExisting("m_projection");
    e.execUniformUseExisting("light.position");
    e.execUniformUseExisting("light.color");
    e.execUniformUseExisting("light.intensity");
    e.execUniformUseExisting("light.radius");
    e.execUniformUseExisting("light.falloff");
    e.execUniformPutFloat(gc, "material.specular_intensity", 1.0f);
    e.execUniformPutFloat(gc, "material.specular_exponent", material
      .getSpecular()
      .getExponent());
    e.execUniformPutMatrix4x4F(gc, "m_modelview", this.matrix_modelview);
    e.execUniformPutMatrix3x3F(gc, "m_normal", this.matrix_normal);
    e.execUniformPutTextureUnit(gc, "t_diffuse_0", texture_units[0]);
    e.execUniformPutTextureUnit(gc, "t_specular", texture_units[3]);

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
        final ArrayBufferAttribute a_nor =
          array.getAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
        e.execAttributeBind(gc, "v_normal", a_nor);
      } else {
        e.execAttributePutVector3F(gc, "v_normal", VectorI3F.ZERO);
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
}
