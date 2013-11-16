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
import java.util.concurrent.Callable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
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
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMutableMatrices.WithInstance;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheFilesystemException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheIOException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLCompileException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLUnsupportedException;

public final class KRendererForward implements KRenderer
{
  private static void makeLitLabel(
    final @Nonnull StringBuilder buffer,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceForwardMaterialLabel label)
  {
    buffer.setLength(0);
    buffer.append("fwd_");
    buffer.append(light.getType().getCode());
    buffer.append("_");
    buffer.append(label.getCode());
  }

  private static void makeUnlitLabel(
    final @Nonnull StringBuilder buffer,
    final @Nonnull KMeshInstanceForwardMaterialLabel label)
  {
    buffer.setLength(0);
    buffer.append("fwd_");
    buffer.append(label.getCode());
  }

  private final @Nonnull Log                                                      log;
  private final @Nonnull JCGLImplementation                                       g;
  private final @Nonnull VectorM4F                                                background;
  private final @Nonnull KMutableMatrices                                         matrices;
  private final @Nonnull VectorM2I                                                viewport_size;
  private final @Nonnull StringBuilder                                            label_cache;
  private final @Nonnull LUCache<String, ProgramReference, KShaderCacheException> shader_cache;
  private @Nonnull KFramebufferBasic                                              framebuffer;

  public KRendererForward(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, ProgramReference, KShaderCacheException> shader_cache,
    final @Nonnull AreaInclusive size,
    final @Nonnull Log log)
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "krenderer-forward");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.label_cache = new StringBuilder();
    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.make();
    this.viewport_size = new VectorM2I();
    this.rendererFramebufferResize(size);
  }

  @SuppressWarnings("static-method") private void putLight(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KLight light,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull JCCEExecutionCallable e)
    throws JCGLException,
      ConstraintError
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        KShadingProgramCommon.putLightDirectional(
          gc,
          e,
          context,
          view,
          (KDirectional) light);
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
          gc,
          e,
          context,
          view,
          (KProjective) light);
        break;
      }
      case LIGHT_SPHERE:
      {
        KShadingProgramCommon.putLightSpherical(
          gc,
          e,
          context,
          view,
          (KSphere) light);
        break;
      }
    }
  }

  @SuppressWarnings("static-method") private void putLightReuse(
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KLight light)
    throws JCGLException,
      ConstraintError
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        KShadingProgramCommon.putLightDirectionalReuse(e);
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        KShadingProgramCommon
          .putLightProjectiveWithoutTextureProjectionReuse(e);
        break;
      }
      case LIGHT_SPHERE:
      {
        KShadingProgramCommon.putLightSphericalReuse(e);
        break;
      }
    }
  }

  @SuppressWarnings("static-method") private void putMeshInstanceMatrices(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KMeshInstanceForwardMaterialLabel label,
    final @Nonnull KMutableMatrices.WithInstanceMatrices mwi)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixProjectionReuse(e);
    KShadingProgramCommon.putMatrixModelView(e, gc, mwi.getMatrixModelView());

    if (label.impliesUV()) {
      KShadingProgramCommon.putMatrixUV(gc, e, mwi.getMatrixUV());
    }

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        KShadingProgramCommon.putMatrixNormal(e, gc, mwi.getMatrixNormal());
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      {
        KShadingProgramCommon.putMatrixInverseView(
          e,
          gc,
          mwi.getMatrixViewInverse());
        break;
      }
    }
  }

  @SuppressWarnings("static-method") private void putTextures(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KMeshInstance i,
    final @CheckForNull KLight light)
    throws JCGLException,
      ConstraintError
  {
    final KMaterial material = i.getMaterial();
    final KMeshInstanceForwardMaterialLabel label =
      i.getForwardMaterialLabel();
    final List<TextureUnit> units = gc.textureGetUnits();
    int current_unit = 0;

    switch (label.getAlbedo()) {
      case ALBEDO_COLOURED:
      {
        break;
      }
      case ALBEDO_TEXTURED:
      {
        KShadingProgramCommon.bindPutTextureAlbedo(
          gc,
          e,
          material,
          units.get(current_unit));
        ++current_unit;
        break;
      }
    }

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      {
        KShadingProgramCommon.bindPutTextureNormal(
          gc,
          e,
          material,
          units.get(current_unit));
        ++current_unit;
        break;
      }
      case NORMAL_NONE:
      case NORMAL_VERTEX:
      {
        break;
      }
    }

    if (label.impliesSpecularMap()) {
      KShadingProgramCommon.bindPutTextureSpecular(
        gc,
        e,
        material,
        units.get(current_unit));
      ++current_unit;
    }

    switch (label.getEmissive()) {
      case EMISSIVE_MAPPED:
      {
        KShadingProgramCommon.bindPutTextureEmissive(
          gc,
          e,
          material,
          units.get(current_unit));
        ++current_unit;
        break;
      }
      case EMISSIVE_CONSTANT:
      case EMISSIVE_NONE:
      {
        break;
      }
    }

    switch (label.getEnvironment()) {
      case ENVIRONMENT_NONE:
      {
        break;
      }
      case ENVIRONMENT_REFLECTIVE:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE:
      case ENVIRONMENT_REFRACTIVE:
      case ENVIRONMENT_REFLECTIVE_MAPPED:
      case ENVIRONMENT_REFLECTIVE_REFRACTIVE_MAPPED:
      case ENVIRONMENT_REFRACTIVE_MAPPED:
      {
        KShadingProgramCommon.bindPutTextureEnvironment(
          gc,
          e,
          material,
          units.get(current_unit));
        ++current_unit;
        break;
      }
    }

    if (light != null) {
      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        {
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          KShadingProgramCommon.bindPutTextureProjection(
            gc,
            e,
            (KProjective) light,
            units.get(current_unit));
          ++current_unit;
          break;
        }
        case LIGHT_SPHERE:
        {
          break;
        }
      }
    }
  }

  private void renderDepthPassMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    final JCCEExecutionCallable e =
      new JCCEExecutionCallable(this.shader_cache.luCacheGet("depth"));

    e.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(
      e,
      gc,
      mwc.getMatrixProjection());
    e.execCancel();

    final KBatches batches = scene.getBatches();

    for (final KBatchOpaqueLit bl : batches.getBatchesOpaqueLit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        final KMutableMatrices.WithInstance mwi = mwc.withInstance(i);
        try {
          this.renderDepthPassMeshOpaque(gc, e, i, mwi);
        } finally {
          mwi.instanceFinish();
        }
      }
    }

    for (final KBatchOpaqueUnlit bl : batches.getBatchesOpaqueUnlit()) {
      for (final KMeshInstance i : bl.getInstances()) {
        final KMutableMatrices.WithInstance mwi = mwc.withInstance(i);
        try {
          this.renderDepthPassMeshOpaque(gc, e, i, mwi);
        } finally {
          mwi.instanceFinish();
        }
      }
    }
  }

  @SuppressWarnings("static-method") private void renderDepthPassMeshOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KMeshInstance i,
    final @Nonnull KMutableMatrices.WithInstanceMatrices mwi)
    throws ConstraintError,
      JCGLException
  {
    /**
     * Upload matrices.
     */

    e.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(e);
    KShadingProgramCommon.putMatrixModelView(e, gc, mwi.getMatrixModelView());

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.getMesh();
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

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    this.framebuffer.kframebufferDelete(this.g);
  }

  @Override public void rendererEvaluate(
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError,
      JCGLCompileException,
      JCGLUnsupportedException,
      IOException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();
    final KMutableMatrices.WithCamera mwc =
      this.matrices.withCamera(scene.getCamera());

    try {
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
        this.renderOpaqueMeshes(scene, gc, mwc);

        /**
         * Render all translucent meshes into the framebuffer.
         */

        gc.cullingDisable();
        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
        gc.depthBufferWriteDisable();
        gc.colorBufferMask(true, true, true, true);
        this.renderTranslucentMeshes(scene, gc, mwc);

      } catch (final KShaderCacheException e) {
        switch (e.getCode()) {
          case KSHADER_CACHE_FILESYSTEM_ERROR:
          {
            final KShaderCacheFilesystemException x =
              (KShaderCacheFilesystemException) e;
            throw new IOException(x);
          }
          case KSHADER_CACHE_IO_ERROR:
          {
            final KShaderCacheIOException x = (KShaderCacheIOException) e;
            throw x.getCause();
          }
          case KSHADER_CACHE_JCGL_COMPILE_ERROR:
          {
            final KShaderCacheJCGLCompileException x =
              (KShaderCacheJCGLCompileException) e;
            throw x.getCause();
          }
          case KSHADER_CACHE_JCGL_ERROR:
          {
            final KShaderCacheJCGLException x = (KShaderCacheJCGLException) e;
            throw x.getCause();
          }
          case KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR:
          {
            final KShaderCacheJCGLUnsupportedException x =
              (KShaderCacheJCGLUnsupportedException) e;
            throw x.getCause();
          }
        }
      } catch (final LUCacheException e) {
        throw new UnreachableCodeException(e);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      mwc.cameraFinish();
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
    Constraints.constrainNotNull(size, "Size");
    if (this.framebuffer != null) {
      this.framebuffer.kframebufferDelete(this.g);
    }
    this.framebuffer = KFramebufferCommon.allocateBasicRGBA(this.g, size);
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    Constraints.constrainNotNull(rgba, "RGBA");
    VectorM4F.copy(rgba, this.background);
  }

  private void renderOpaqueMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    final KBatches batches = scene.getBatches();

    for (final KBatchOpaqueLit bl : batches.getBatchesOpaqueLit()) {
      final KLight light = bl.getLight();
      final KMeshInstanceForwardMaterialLabel label = bl.getLabel();

      KRendererForward.makeLitLabel(this.label_cache, light, label);

      final JCCEExecutionCallable e =
        new JCCEExecutionCallable(
          this.shader_cache.luCacheGet(this.label_cache.toString()));

      e.execPrepare(gc);
      KShadingProgramCommon.putMatrixProjection(
        e,
        gc,
        mwc.getMatrixProjection());
      this
        .putLight(gc, light, mwc.getMatrixContext(), mwc.getMatrixView(), e);
      e.execCancel();

      for (final KMeshInstance i : bl.getInstances()) {
        final WithInstance mwi = mwc.withInstance(i);
        try {
          this.renderOpaqueMeshLit(gc, e, light, i, mwi);
        } finally {
          mwi.instanceFinish();
        }
      }
    }

    for (final KBatchOpaqueUnlit bl : batches.getBatchesOpaqueUnlit()) {
      KRendererForward.makeUnlitLabel(this.label_cache, bl.getLabel());

      final JCCEExecutionCallable e =
        new JCCEExecutionCallable(
          this.shader_cache.luCacheGet(this.label_cache.toString()));

      e.execPrepare(gc);
      KShadingProgramCommon.putMatrixProjection(
        e,
        gc,
        mwc.getMatrixProjection());
      e.execCancel();

      for (final KMeshInstance i : bl.getInstances()) {
        final WithInstance mwi = mwc.withInstance(i);
        try {
          this.renderOpaqueMeshUnlit(gc, e, i, mwi);
        } finally {
          mwi.instanceFinish();
        }
      }
    }
  }

  private void renderOpaqueMeshLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException
  {
    final KMeshInstanceForwardMaterialLabel label =
      i.getForwardMaterialLabel();
    final KMaterial material = i.getMaterial();

    e.execPrepare(gc);
    this.putMeshInstanceMatrices(gc, e, label, mwi);
    this.putLightReuse(e, light);
    this.putTextures(gc, e, i, light);
    KShadingProgramCommon.putMaterial(e, gc, material);
    this.putLightProjectiveMatrixIfNecessary(gc, e, light, mwi);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(gc, e, array);

      switch (label.getNormal()) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(gc, e, array);
          KShadingProgramCommon.bindAttributeNormal(gc, e, array);
          break;
        }
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(gc, e, array);
          break;
        }
        case NORMAL_NONE:
        {
          break;
        }
      }

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(gc, e, array);
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

  private void putLightProjectiveMatrixIfNecessary(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KLight light,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        final KMutableMatrices.WithProjectiveLight mwp =
          mwi.withProjectiveLight((KProjective) light);

        try {
          KShadingProgramCommon.putMatrixTextureProjection(
            gc,
            e,
            mwp.getTextureProjection());
        } finally {
          mwp.projectiveLightFinish();
        }
        break;
      }
      case LIGHT_SPHERE:
      {
        break;
      }
    }
  }

  @SuppressWarnings("static-method") private void renderOpaqueMeshUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KMeshInstance i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
  {
    throw new UnimplementedCodeException();
  }

  private void renderTranslucentMeshes(
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    final KBatches batches = scene.getBatches();

    for (final KBatchTranslucent bl : batches.getBatchesTranslucent()) {
      final KMeshInstanceForwardMaterialLabel label = bl.getLabel();
      final KMeshInstance i = bl.getInstance();
      final KMutableMatrices.WithInstance mwi = mwc.withInstance(i);

      try {
        boolean first_light = true;
        for (final KLight light : bl.getLights()) {
          KRendererForward.makeLitLabel(this.label_cache, light, label);

          final JCCEExecutionCallable e =
            new JCCEExecutionCallable(
              this.shader_cache.luCacheGet(this.label_cache.toString()));

          e.execPrepare(gc);
          KShadingProgramCommon.putMatrixProjection(
            e,
            gc,
            mwc.getMatrixProjection());
          this.putLight(
            gc,
            light,
            mwc.getMatrixContext(),
            mwc.getMatrixView(),
            e);
          e.execCancel();

          if (first_light) {
            gc.blendingEnable(
              BlendFunction.BLEND_ONE,
              BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
          } else {
            gc.blendingEnable(
              BlendFunction.BLEND_ONE,
              BlendFunction.BLEND_ONE);
          }

          this.renderTranslucentMeshLit(gc, e, light, i, mwi);
          first_light = false;
        }
      } finally {
        mwi.instanceFinish();
      }
    }
  }

  private void renderTranslucentMeshLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCCEExecutionCallable e,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstance i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException
  {
    final KMeshInstanceForwardMaterialLabel label =
      i.getForwardMaterialLabel();
    final KMaterial material = i.getMaterial();

    e.execPrepare(gc);
    this.putMeshInstanceMatrices(gc, e, label, mwi);
    this.putLightReuse(e, light);
    this.putTextures(gc, e, i, light);
    KShadingProgramCommon.putMaterial(e, gc, material);
    this.putLightProjectiveMatrixIfNecessary(gc, e, light, mwi);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = i.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(gc, e, array);

      switch (label.getNormal()) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(gc, e, array);
          KShadingProgramCommon.bindAttributeNormal(gc, e, array);
          break;
        }
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(gc, e, array);
          break;
        }
        case NORMAL_NONE:
        {
          break;
        }
      }

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(gc, e, array);
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
