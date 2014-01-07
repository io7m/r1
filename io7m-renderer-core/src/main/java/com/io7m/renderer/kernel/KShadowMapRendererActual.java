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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jlucache.PCache;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KSceneBatchedShadow.BatchOpaqueShadow;
import com.io7m.renderer.kernel.KSceneBatchedShadow.BatchTranslucentShadow;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KTransform.Context;

final class KShadowMapRendererActual implements KShadowMapRenderer
{
  private static enum State
  {
    SHADOW_RENDERER_EVALUATED,
    SHADOW_RENDERER_INITIAL,
    SHADOW_RENDERER_STARTED
  }

  private static void handleJCBException(
    final @Nonnull JCBExecutionException e)
    throws JCGLException,
      JCGLUnsupportedException,
      KShadowCacheException
  {
    Throwable x = e;
    for (;;) {
      if (x == null) {
        throw new UnreachableCodeException();
      }
      if (x instanceof JCGLException) {
        throw (JCGLException) x;
      }
      if (x instanceof JCGLUnsupportedException) {
        throw (JCGLUnsupportedException) x;
      }
      if (x instanceof KShadowCacheException) {
        throw (KShadowCacheException) x;
      }
      x = x.getCause();
    }
  }

  private static void makeShadowLabel(
    final @Nonnull StringBuilder cache,
    final @Nonnull KMaterialShadowLabel label)
  {
    cache.setLength(0);
    cache.append("depth_");
    cache.append(label.getCode());
  }

  public static @Nonnull
    KShadowMapRendererActual
    newRenderer(
      final @Nonnull JCGLImplementation gl,
      final @Nonnull KMaterialShadowLabelCache label_decider,
      final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache)
      throws ConstraintError
  {
    return new KShadowMapRendererActual(
      gl,
      label_decider,
      shader_cache,
      shadow_cache);
  }

  private static void renderShadowMapsConfigureDepthColorMasks(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMaterialShadowLabel label)
    throws ConstraintError,
      JCGLException
  {
    switch (label) {
      case SHADOW_BASIC_OPAQUE:
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
        gc.colorBufferMask(false, false, false, false);
        break;
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
        gc.colorBufferMask(true, true, true, true);
        break;
    }
  }

  private final @Nonnull JCGLImplementation                                 g;
  private final @Nonnull StringBuilder                                      label_cache;
  private final @Nonnull KMaterialShadowLabelCache                          label_decider;
  private final @Nonnull RMatrixM4x4F<RTransformView>                       m4_view;
  private final @Nonnull KMutableMatrices                                   matrices;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException>   shader_cache;
  private final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache;
  private @Nonnull State                                                    state;
  private final @Nonnull Context                                            transform_context;
  private final @Nonnull VectorM2I                                          viewport_size;

  private KShadowMapRendererActual(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KMaterialShadowLabelCache label_decider,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull PCache<KShadow, KShadowMap, KShadowCacheException> shadow_cache)
    throws ConstraintError
  {
    this.state = State.SHADOW_RENDERER_INITIAL;

    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.shadow_cache =
      Constraints.constrainNotNull(shadow_cache, "Shadow cache");
    this.label_decider =
      Constraints.constrainNotNull(label_decider, "Label decider");

    this.viewport_size = new VectorM2I();
    this.label_cache = new StringBuilder();
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.m4_view = new RMatrixM4x4F<RTransformView>();
  }

  private void renderShadowMapOpaqueBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KProjective lp,
    final @Nonnull BatchOpaqueShadow batch)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final RMatrixI4x4F<RTransformProjection> projection = lp.getProjection();

    KMatrices.makeViewMatrix(
      this.transform_context,
      lp.getPosition(),
      lp.getOrientation(),
      this.m4_view);
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(this.m4_view);

    final KMutableMatrices.WithCamera mwc =
      this.matrices.withObserver(view, projection);

    try {
      KShadowMapRendererActual.makeShadowLabel(
        this.label_cache,
        batch.getLabel());
      KShadowMapRendererActual.renderShadowMapsConfigureDepthColorMasks(
        gc,
        batch.getLabel());

      final KProgram p =
        this.shader_cache.luCacheGet(this.label_cache.toString());
      final JCBExecutionAPI e = p.getExecutable();

      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException,
            Throwable
        {
          KShadingProgramCommon.putMatrixProjection(
            program,
            mwc.getMatrixProjection());

          final List<KMeshInstanceTransformed> instances =
            batch.getInstances();

          for (int index = 0; index < instances.size(); ++index) {
            final KMeshInstanceTransformed instance = instances.get(index);
            final KMutableMatrices.WithInstance mwi =
              mwc.withInstance(instance);
            try {
              KShadowMapRendererActual.this.renderShadowMapOpaqueMesh(
                gc,
                program,
                mwi,
                instance);
            } finally {
              mwi.instanceFinish();
            }
          }
        }
      });

    } finally {
      mwc.cameraFinish();
    }
  }

  @SuppressWarnings("static-method") private void renderShadowMapOpaqueMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KMutableMatrices.WithInstance mwi,
    final @Nonnull KMeshInstanceTransformed i)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final KMeshInstance actual = i.getInstance();
    final KMesh mesh = actual.getMesh();
    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    try {
      gc.arrayBufferBind(array);

      KShadingProgramCommon.bindAttributePosition(program, array);
      KShadingProgramCommon.putMatrixProjectionReuse(program);
      KShadingProgramCommon.putMatrixModelView(
        program,
        mwi.getMatrixModelView());

      program.programExecute(new JCBProgramProcedure() {
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

  private void renderShadowMapsOpaqueBatches(
    final @Nonnull KSceneBatchedShadow batched)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      KShadowCacheException,
      JCBExecutionException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    /**
     * Render only back faces to reduce self-shadowing artifacts.
     */

    gc.cullingEnable(
      FaceSelection.FACE_FRONT,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

    /**
     * Opaque objects will contribute to and be tested against the contents of
     * the depth buffer. On platforms supporting depth textures (which is most
     * of them), the depth buffer is the only thing produced by the renderer.
     */

    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
    gc.depthBufferWriteEnable();

    /**
     * On platforms that don't support depth textures, the depth values are
     * packed into a colour texture (including the alpha channel), so it's
     * critical that blending is disabled.
     */

    gc.blendingDisable();

    final Map<KLight, BatchOpaqueShadow> opaque_batches =
      batched.getShadowCastersOpaque();

    for (final KLight light : opaque_batches.keySet()) {
      final BatchOpaqueShadow batch = opaque_batches.get(light);

      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        {
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          final KProjective projective = (KProjective) light;
          final Option<KShadow> os = projective.getShadow();
          assert os.isSome();
          final KShadow s = ((Option.Some<KShadow>) os).value;

          switch (s.getType()) {
            case SHADOW_MAPPED_SOFT:
            {
              // TODO:
              throw new UnimplementedCodeException();
            }
            case SHADOW_MAPPED_BASIC:
            {
              final KShadowMapBasic smb =
                (KShadowMapBasic) this.shadow_cache.pcCacheGet(s);
              final FramebufferReferenceUsable fb =
                smb.mapGetDepthFramebuffer();

              gc.framebufferDrawBind(fb);
              try {
                final AreaInclusive area = smb.mapGetDepthFramebufferArea();
                this.viewport_size.x = (int) area.getRangeX().getInterval();
                this.viewport_size.y = (int) area.getRangeY().getInterval();
                gc.viewportSet(VectorI2I.ZERO, this.viewport_size);
                this.renderShadowMapOpaqueBatch(gc, projective, batch);
              } finally {
                gc.framebufferDrawUnbind();
              }
              break;
            }
          }
          break;
        }
        case LIGHT_SPHERE:
        {
          break;
        }
      }
    }
  }

  private void renderShadowMapsPrecacheMaps(
    final @Nonnull KSceneBatchedShadow batched)
    throws KShadowCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    for (final KLight l : batched.getShadowLights()) {
      switch (l.getType()) {
        case LIGHT_DIRECTIONAL:
        case LIGHT_SPHERE:
          break;
        case LIGHT_PROJECTIVE:
          final KProjective kp = (KProjective) l;
          final KShadow s = ((Option.Some<KShadow>) kp.getShadow()).value;
          this.renderShadowMapsPrecacheShadow(gc, s);
      }
    }
  }

  private void renderShadowMapsPrecacheShadow(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadow s)
    throws KShadowCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException
  {
    switch (s.getType()) {
      case SHADOW_MAPPED_SOFT:
      {
        // TODO
        throw new UnimplementedCodeException();
      }
      case SHADOW_MAPPED_BASIC:
      {
        final KShadowMapBasic fb =
          (KShadowMapBasic) this.shadow_cache.pcCacheGet(s);

        gc.framebufferDrawBind(fb.mapGetDepthFramebuffer());
        try {
          gc.colorBufferMask(true, true, true, true);
          gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
          gc.depthBufferWriteEnable();
          gc.depthBufferClear(1.0f);
        } finally {
          gc.framebufferDrawUnbind();
        }
        break;
      }
    }
  }

  private void renderShadowMapsTranslucentBatches(
    final KSceneBatchedShadow batched)
    throws KShadowCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      KShaderCacheException,
      JCBExecutionException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    /**
     * Translucent objects will contribute to the depth buffer but are not
     * subject to being tested against the existing contents of the depth
     * buffer, due to being drawn in a carefully defined order.
     * 
     * On platforms supporting depth textures (which is most of them), the
     * depth buffer is the only thing produced by the renderer.
     */

    gc.cullingDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
    gc.depthBufferWriteEnable();

    /**
     * On platforms that don't support depth textures, the depth values are
     * packed into a colour texture (including the alpha channel), so it's
     * critical that blending is disabled.
     */

    gc.blendingDisable();

    final Map<KLight, BatchTranslucentShadow> translucent_batches =
      batched.getShadowCastersTranslucent();

    for (final KLight light : translucent_batches.keySet()) {
      final BatchTranslucentShadow batch = translucent_batches.get(light);

      switch (light.getType()) {
        case LIGHT_DIRECTIONAL:
        {
          break;
        }
        case LIGHT_PROJECTIVE:
        {
          final KProjective projective = (KProjective) light;
          final Option<KShadow> os = projective.getShadow();
          assert os.isSome();
          final KShadow s = ((Option.Some<KShadow>) os).value;

          switch (s.getType()) {
            case SHADOW_MAPPED_SOFT:
            {
              // TODO:
              throw new UnimplementedCodeException();
            }
            case SHADOW_MAPPED_BASIC:
            {
              final KShadowMapBasic map =
                (KShadowMapBasic) this.shadow_cache.pcCacheGet(s);
              gc.framebufferDrawBind(map.mapGetDepthFramebuffer());
              try {
                final AreaInclusive area = map.mapGetDepthFramebufferArea();
                this.viewport_size.x = (int) area.getRangeX().getInterval();
                this.viewport_size.y = (int) area.getRangeY().getInterval();
                gc.viewportSet(VectorI2I.ZERO, this.viewport_size);
                this.renderShadowMapTranslucentBatch(gc, projective, batch);
              } finally {
                gc.framebufferDrawUnbind();
              }
              break;
            }
          }
          break;
        }
        case LIGHT_SPHERE:
        {
          break;
        }
      }
    }
  }

  private void renderShadowMapTranslucentBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KProjective projective,
    final @Nonnull BatchTranslucentShadow batch)
    throws ConstraintError,
      KShaderCacheException,
      JCGLException,
      LUCacheException,
      JCBExecutionException
  {
    final RMatrixI4x4F<RTransformProjection> projection =
      projective.getProjection();

    KMatrices.makeViewMatrix(
      this.transform_context,
      projective.getPosition(),
      projective.getOrientation(),
      this.m4_view);
    final RMatrixI4x4F<RTransformView> view =
      new RMatrixI4x4F<RTransformView>(this.m4_view);
    final KMutableMatrices.WithCamera mwc =
      this.matrices.withObserver(view, projection);

    final KShadow shadow =
      ((Option.Some<KShadow>) projective.getShadow()).value;

    try {
      final List<KMeshInstanceTransformed> instances = batch.getInstances();
      for (int index = 0; index < instances.size(); ++index) {
        final KMeshInstanceTransformed instance = instances.get(index);
        final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);
        try {
          this.renderShadowMapTranslucentMesh(gc, shadow, mwi, instance);
        } finally {
          mwi.instanceFinish();
        }
      }
    } finally {
      mwc.cameraFinish();
    }
  }

  private void renderShadowMapTranslucentMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KShadow shadow,
    final @Nonnull KMutableMatrices.WithInstance mwi,
    final @Nonnull KMeshInstanceTransformed i)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final KMeshInstance actual = i.getInstance();

    final KMaterialShadowLabel label =
      this.label_decider.getShadowLabel(actual, shadow);

    KShadowMapRendererActual.makeShadowLabel(this.label_cache, label);
    KShadowMapRendererActual.renderShadowMapsConfigureDepthColorMasks(
      gc,
      label);

    final KProgram p =
      this.shader_cache.luCacheGet(this.label_cache.toString());
    final JCBExecutionAPI e = p.getExecutable();

    e.execRun(new JCBExecutorProcedure() {
      @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          JCBExecutionException
      {
        final KMesh mesh = actual.getMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();

        try {
          gc.arrayBufferBind(array);

          KShadingProgramCommon.putMatrixProjection(
            program,
            mwi.getMatrixProjection());
          KShadingProgramCommon.bindAttributePosition(program, array);
          KShadingProgramCommon.putMatrixModelView(
            program,
            mwi.getMatrixModelView());
          KShadingProgramCommon.putMaterial(program, actual.getMaterial());

          if (label.impliesUV()) {
            KShadingProgramCommon.bindAttributeUV(program, array);
            KShadingProgramCommon.putMatrixUV(program, mwi.getMatrixUV());
            final List<TextureUnit> units = gc.textureGetUnits();
            KShadingProgramCommon.bindPutTextureAlbedo(
              program,
              gc,
              actual.getMaterial(),
              units.get(0));
          }

          program.programExecute(new JCBProgramProcedure() {
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
    });
  }

  @Override public void shadowRendererEvaluate(
    final @Nonnull KSceneBatchedShadow batched)
    throws ConstraintError,
      KShadowCacheException,
      JCGLException,
      KShaderCacheException,
      JCGLUnsupportedException
  {
    Constraints.constrainArbitrary(
      (this.state == State.SHADOW_RENDERER_STARTED)
        || (this.state == State.SHADOW_RENDERER_EVALUATED),
      "Renderer in started or evaluated state");
    Constraints.constrainNotNull(batched, "Shadow batches");

    this.state = State.SHADOW_RENDERER_STARTED;
    try {
      this.renderShadowMapsPrecacheMaps(batched);
      this.renderShadowMapsOpaqueBatches(batched);
      this.renderShadowMapsTranslucentBatches(batched);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    } catch (final JCBExecutionException x) {
      KShadowMapRendererActual.handleJCBException(x);
    }
    this.state = State.SHADOW_RENDERER_EVALUATED;
  }

  @Override public void shadowRendererFinish()
    throws ConstraintError
  {
    this.state = State.SHADOW_RENDERER_INITIAL;
    this.shadow_cache.pcPeriodEnd();
  }

  @Override public @Nonnull KShadowMap shadowRendererGetRenderedMap(
    final @Nonnull KShadow shadow)
    throws ConstraintError,
      KShadowCacheException
  {
    Constraints.constrainArbitrary(
      this.state == State.SHADOW_RENDERER_EVALUATED,
      "Shadow maps evaluated");
    Constraints.constrainNotNull(shadow, "Shadow");
    try {
      return this.shadow_cache.pcCacheGet(shadow);
    } catch (final LUCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public void shadowRendererStart()
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.state == State.SHADOW_RENDERER_INITIAL,
      "Renderer in initial state");

    this.state = State.SHADOW_RENDERER_STARTED;
    this.shadow_cache.pcPeriodStart();
  }

}
