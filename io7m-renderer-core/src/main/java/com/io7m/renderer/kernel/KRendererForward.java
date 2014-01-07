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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
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
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LRUCacheTrivial;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RMatrixM4x4F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformView;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KMutableMatrices.WithInstance;
import com.io7m.renderer.kernel.KSceneBatchedForward.BatchTranslucent;
import com.io7m.renderer.kernel.KSceneBatchedForward.BatchTranslucentLit;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheFilesystemException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheIOException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLCompileException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLUnsupportedException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheXMLException;
import com.io7m.renderer.kernel.KShadowCacheException.KShadowCacheJCGLException;
import com.io7m.renderer.kernel.KShadowCacheException.KShadowCacheJCGLUnsupportedException;
import com.io7m.renderer.kernel.KShadowMap.KShadowMapBasic;
import com.io7m.renderer.kernel.KTransform.Context;

public final class KRendererForward implements KRenderer
{
  private class Debugging implements KRendererDebugging
  {
    private final @Nonnull AtomicReference<KRendererDebugging.DebugShadowMapReceiver> dump_shadow_maps;

    public Debugging()
    {
      this.dump_shadow_maps =
        new AtomicReference<KRendererDebugging.DebugShadowMapReceiver>();
    }

    @Override public void debugForEachShadowMap(
      final @Nonnull DebugShadowMapReceiver receiver)
      throws ConstraintError
    {
      this.dump_shadow_maps.set(receiver);
    }

    @SuppressWarnings("synthetic-access") public
      void
      debugPerformDumpShadowMaps(
        final @Nonnull Collection<KLight> lights)
        throws ConstraintError,
          KShadowCacheException
    {
      final DebugShadowMapReceiver dump =
        this.dump_shadow_maps.getAndSet(null);

      if (dump != null) {
        KRendererForward.this.log.debug("Dumping shadow maps");

        for (final KLight l : lights) {
          switch (l.getType()) {
            case LIGHT_DIRECTIONAL:
            {
              break;
            }
            case LIGHT_PROJECTIVE:
            {
              final KProjective kp = (KProjective) l;
              final Option<KShadow> os = kp.getShadow();

              switch (os.type) {
                case OPTION_NONE:
                {
                  break;
                }
                case OPTION_SOME:
                {
                  final KShadow ks = ((Option.Some<KShadow>) os).value;
                  final KShadowMap fb =
                    KRendererForward.this.shadow_map_renderer
                      .shadowRendererGetRenderedMap(ks);
                  dump.receive(ks, fb);
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
    }
  }

  private static void handleJCBException(
    final @Nonnull JCBExecutionException e)
    throws JCGLException,
      JCGLUnsupportedException
  {
    Throwable x = e;
    for (;;) {
      if (x == null) {
        throw new UnreachableCodeException();
      }
      if (x instanceof KShadowCacheException) {
        KRendererForward
          .handleShadowCacheException((KShadowCacheException) x);
      }
      x = x.getCause();
    }
  }

  private static void handleShaderCacheException(
    final @Nonnull KShaderCacheException e)
    throws IOException,
      JCGLCompileException,
      JCGLException,
      JCGLUnsupportedException,
      KXMLException
  {
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
      case KSHADER_CACHE_XML_ERROR:
      {
        final KShaderCacheXMLException x = (KShaderCacheXMLException) e;
        throw x.getCause();
      }
    }
  }

  private static void handleShadowCacheException(
    final @Nonnull KShadowCacheException e)
    throws JCGLException,
      JCGLUnsupportedException
  {
    switch (e.getCode()) {
      case KSHADER_CACHE_JCGL_ERROR:
      {
        final KShadowCacheJCGLException x =
          (KShadowCacheException.KShadowCacheJCGLException) e;
        throw x.getCause();
      }
      case KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR:
      {
        final KShadowCacheJCGLUnsupportedException x =
          (KShadowCacheException.KShadowCacheJCGLUnsupportedException) e;
        throw x.getCause();
      }
    }
  }

  private static void makeLitLabel(
    final @Nonnull StringBuilder buffer,
    final @Nonnull KLightLabelCache labels,
    final @Nonnull KLight light,
    final @Nonnull KMaterialForwardLabel label)
    throws ConstraintError
  {
    buffer.setLength(0);
    buffer.append("fwd_");
    buffer.append(labels.getLightLabel(light));
    buffer.append("_");
    buffer.append(label.getCode());
  }

  private static void makeUnlitLabel(
    final @Nonnull StringBuilder buffer,
    final @Nonnull KMaterialForwardLabel label)
  {
    buffer.setLength(0);
    buffer.append("fwd_U_");
    buffer.append(label.getCode());
  }

  private static void putLight(
    final @Nonnull KLight light,
    final @Nonnull MatrixM4x4F.Context context,
    final @Nonnull RMatrixReadable4x4F<RTransformView> view,
    final @Nonnull JCBProgram program)
    throws JCGLException,
      ConstraintError
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        KShadingProgramCommon.putLightDirectional(
          program,
          context,
          view,
          (KDirectional) light);
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        KShadingProgramCommon.putLightProjectiveWithoutTextureProjection(
          program,
          context,
          view,
          (KProjective) light);
        break;
      }
      case LIGHT_SPHERE:
      {
        KShadingProgramCommon.putLightSpherical(
          program,
          context,
          view,
          (KSphere) light);
        break;
      }
    }
  }

  public static
    KRendererForward
    rendererNew(
      final @Nonnull JCGLImplementation g,
      final @Nonnull LRUCacheTrivial<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull KShadowMapRenderer shadow_map_renderer,
      final @Nonnull KLabelDecider decider,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KRendererForward(
      g,
      shader_cache,
      shadow_map_renderer,
      decider,
      log);
  }

  private final @Nonnull VectorM4F                                        background;
  private @CheckForNull Debugging                                         debug;
  private final @Nonnull KLabelDecider                                    decider;
  private final @Nonnull JCGLImplementation                               g;
  private final @Nonnull StringBuilder                                    label_cache;
  private final @Nonnull Log                                              log;
  private final @Nonnull RMatrixM4x4F<RTransformView>                     m4_view;
  private final @Nonnull KMutableMatrices                                 matrices;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache;
  private final @Nonnull KShadowMapRenderer                               shadow_map_renderer;
  private final @Nonnull Context                                          transform_context;
  private final @Nonnull VectorM2I                                        viewport_size;

  private KRendererForward(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull KShadowMapRenderer shadow_map_renderer,
    final @Nonnull KLabelDecider decider,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "krenderer-forward");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.shadow_map_renderer =
      Constraints
        .constrainNotNull(shadow_map_renderer, "Shadow map renderer");
    this.decider = Constraints.constrainNotNull(decider, "Label decider");

    this.label_cache = new StringBuilder();
    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.viewport_size = new VectorM2I();
    this.transform_context = new KTransform.Context();
    this.m4_view = new RMatrixM4x4F<RTransformView>();
  }

  @SuppressWarnings("static-method") private
    void
    putLightProjectiveMatricesIfNecessary(
      final @Nonnull JCBProgram program,
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
          KShadingProgramCommon.putMatrixProjectiveProjection(
            program,
            mwp.getMatrixProjectiveProjection());
          KShadingProgramCommon.putMatrixProjectiveModelView(
            program,
            mwp.getMatrixProjectiveModelView());
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

  @SuppressWarnings("static-method") private void putLightReuse(
    final @Nonnull JCBProgram program,
    final @Nonnull KLight light)
    throws JCGLException,
      ConstraintError
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        KShadingProgramCommon.putLightDirectionalReuse(program);
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        KShadingProgramCommon
          .putLightProjectiveWithoutTextureProjectionReuse(
            program,
            (KProjective) light);
        break;
      }
      case LIGHT_SPHERE:
      {
        KShadingProgramCommon.putLightSphericalReuse(program);
        break;
      }
    }
  }

  @SuppressWarnings("static-method") private void putMeshInstanceMatrices(
    final @Nonnull JCBProgram p,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KMutableMatrices.WithInstanceMatrices mwi)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

    if (label.impliesUV()) {
      KShadingProgramCommon.putMatrixUV(p, mwi.getMatrixUV());
    }

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        KShadingProgramCommon.putMatrixNormal(p, mwi.getMatrixNormal());
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
          p,
          mwi.getMatrixViewInverse());
        break;
      }
    }
  }

  private void putTextures(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMaterialForwardLabel label,
    final @CheckForNull KLight light)
    throws JCGLException,
      ConstraintError,
      KShadowCacheException
  {
    final KMaterial material = i.getInstance().getMaterial();
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
          program,
          gc,
          material,
          units.get(current_unit));
        ++current_unit;
        break;
      }
    }

    switch (label.getEmissive()) {
      case EMISSIVE_MAPPED:
      {
        KShadingProgramCommon.bindPutTextureEmissive(
          program,
          gc,
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

    switch (label.getNormal()) {
      case NORMAL_MAPPED:
      {
        KShadingProgramCommon.bindPutTextureNormal(
          program,
          gc,
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
        program,
        gc,
        material,
        units.get(current_unit));
      ++current_unit;
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
          program,
          gc,
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
          final KProjective kp = (KProjective) light;

          KShadingProgramCommon.bindPutTextureProjection(
            program,
            gc,
            kp,
            units.get(current_unit));
          ++current_unit;

          switch (kp.getShadow().type) {
            case OPTION_NONE:
            {
              break;
            }
            case OPTION_SOME:
            {
              final KShadow ks =
                ((Option.Some<KShadow>) kp.getShadow()).value;

              switch (ks.getType()) {
                case SHADOW_MAPPED_BASIC:
                {
                  final KShadowMapBasic fb =
                    (KShadowMapBasic) this.shadow_map_renderer
                      .shadowRendererGetRenderedMap(ks);

                  KShadingProgramCommon.bindPutTextureShadowMap(
                    program,
                    gc,
                    fb.mapGetDepthTexture(),
                    units.get(current_unit));
                  ++current_unit;
                  break;
                }
                case SHADOW_MAPPED_SOFT:
                {
                  // TODO
                  throw new UnimplementedCodeException();
                }
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

  private void renderDepthPassMeshes(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>> depth_batch =
      batched.getBatchesDepth();

    for (final KMaterialDepthLabel label : depth_batch.keySet()) {
      final List<KMeshInstanceTransformed> batch = depth_batch.get(label);

      this.label_cache.setLength(0);
      this.label_cache.append("depth_");
      this.label_cache.append(label.getCode());

      final KProgram p =
        this.shader_cache.luCacheGet(this.label_cache.toString());
      final JCBExecutionAPI e = p.getExecutable();

      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException,
            Exception
        {
          KShadingProgramCommon.putMatrixProjection(
            program,
            mwc.getMatrixProjection());

          for (final KMeshInstanceTransformed instance : batch) {
            final KMutableMatrices.WithInstance mwi =
              mwc.withInstance(instance);
            try {
              KRendererForward.this.renderDepthPassMeshOpaque(
                gc,
                program,
                instance,
                label,
                mwi);
            } finally {
              mwi.instanceFinish();
            }
          }
        }
      });
    }
  }

  @SuppressWarnings("static-method") private void renderDepthPassMeshOpaque(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMaterialDepthLabel label,
    final @Nonnull KMutableMatrices.WithInstanceMatrices mwi)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final KMaterial material = i.getInstance().getMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

    switch (label) {
      case DEPTH_CONSTANT:
      {
        break;
      }
      case DEPTH_MAPPED:
      {
        KShadingProgramCommon.putMaterial(p, material);
        KShadingProgramCommon.putMatrixUV(p, mwi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          p,
          gc,
          material,
          units.get(0));
        break;
      }
      case DEPTH_UNIFORM:
      {
        KShadingProgramCommon.putMaterial(p, material);
        break;
      }
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

      switch (label) {
        case DEPTH_UNIFORM:
        case DEPTH_CONSTANT:
        {
          break;
        }
        case DEPTH_MAPPED:
        {
          KShadingProgramCommon.bindAttributeUV(p, array);
          break;
        }
      }

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

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    // Nothing
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    this.debug = new Debugging();
    return this.debug;
  }

  @Override public void rendererEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError,
      JCGLCompileException,
      JCGLUnsupportedException,
      IOException,
      KXMLException
  {
    Constraints.constrainNotNull(framebuffer, "Framebuffer");
    Constraints.constrainNotNull(scene, "Scene");

    final KSceneBatchedForward batched =
      KSceneBatchedForward.newBatchedScene(
        this.decider,
        this.decider,
        this.decider,
        scene);

    try {
      this.shadow_map_renderer.shadowRendererStart();
      this.shadow_map_renderer.shadowRendererEvaluate(batched
        .getBatchedShadow());

      if (this.debug != null) {
        this.debug.debugPerformDumpShadowMaps(batched
          .getBatchedShadow()
          .getShadowCasters()
          .keySet());
      }

      final JCGLInterfaceCommon gc = this.g.getGLCommon();

      final KMutableMatrices.WithCamera mwc =
        this.matrices.withCamera(scene.getCamera());

      try {
        final FramebufferReferenceUsable output_buffer =
          framebuffer.kframebufferGetFramebuffer();

        final AreaInclusive area = framebuffer.kframebufferGetArea();
        this.viewport_size.x = (int) area.getRangeX().getInterval();
        this.viewport_size.y = (int) area.getRangeY().getInterval();

        try {
          gc.framebufferDrawBind(output_buffer);
          gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

          /**
           * Render all opaque meshes into the depth buffer, without touching
           * the color buffer.
           */

          gc.cullingEnable(
            FaceSelection.FACE_BACK,
            FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
          gc.depthBufferWriteEnable();
          gc.depthBufferClear(1.0f);
          gc.colorBufferMask(false, false, false, false);
          gc.blendingDisable();
          this.renderDepthPassMeshes(batched, gc, mwc);

          /**
           * Render all opaque meshes, blending additively, into the
           * framebuffer.
           */

          gc.colorBufferMask(true, true, true, true);
          gc.colorBufferClearV4f(this.background);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_EQUAL);
          gc.depthBufferWriteDisable();
          gc.colorBufferMask(true, true, true, true);
          gc.blendingEnable(BlendFunction.BLEND_ONE, BlendFunction.BLEND_ONE);
          this.renderOpaqueMeshes(batched, gc, mwc);

          /**
           * Render all translucent meshes into the framebuffer.
           */

          gc.cullingDisable();
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();
          gc.colorBufferMask(true, true, true, true);
          this.renderTranslucentMeshes(batched, gc, mwc);

        } catch (final KShaderCacheException e) {
          KRendererForward.handleShaderCacheException(e);
        } catch (final LUCacheException e) {
          throw new UnreachableCodeException(e);
        } catch (final JCBExecutionException e) {
          KRendererForward.handleJCBException(e);
        } finally {
          gc.framebufferDrawUnbind();
        }
      } finally {
        mwc.cameraFinish();
      }
    } catch (final KShadowCacheException x) {
      KRendererForward.handleShadowCacheException(x);
    } catch (final KShaderCacheException x) {
      KRendererForward.handleShaderCacheException(x);
    } finally {
      this.shadow_map_renderer.shadowRendererFinish();
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    Constraints.constrainNotNull(rgba, "RGBA");
    VectorM4F.copy(rgba, this.background);
  }

  private void renderOpaqueMeshes(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    this.renderOpaqueMeshesLit(batched, gc, mwc);
    this.renderOpaqueMeshesUnlit(batched, gc, mwc);
  }

  private void renderOpaqueMeshesLit(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws ConstraintError,
      KShaderCacheException,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> by_light =
      batched.getBatchesOpaqueLit();

    for (final KLight light : by_light.keySet()) {
      final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> by_label =
        by_light.get(light);

      for (final KMaterialForwardLabel label : by_label.keySet()) {
        KRendererForward.makeLitLabel(
          this.label_cache,
          this.decider,
          light,
          label);

        final KProgram p =
          this.shader_cache.luCacheGet(this.label_cache.toString());
        final JCBExecutionAPI e = p.getExecutable();

        e.execRun(new JCBExecutorProcedure() {
          @SuppressWarnings("synthetic-access") @Override public void call(
            final @Nonnull JCBProgram program)
            throws ConstraintError,
              JCGLException,
              JCBExecutionException,
              Exception,
              LUCacheException
          {
            KShadingProgramCommon.putMatrixProjection(
              program,
              mwc.getMatrixProjection());

            KRendererForward.putLight(
              light,
              mwc.getMatrixContext(),
              mwc.getMatrixView(),
              program);

            final List<KMeshInstanceTransformed> instances =
              by_label.get(label);

            for (int index = 0; index < instances.size(); ++index) {
              final KMeshInstanceTransformed instance = instances.get(index);
              final WithInstance mwi = mwc.withInstance(instance);
              try {
                KRendererForward.this.renderOpaqueMeshLit(
                  gc,
                  program,
                  light,
                  label,
                  instance,
                  mwi);
              } finally {
                mwi.instanceFinish();
              }
            }
          }
        });
      }
    }
  }

  private void renderOpaqueMeshesUnlit(
    final @Nonnull KSceneBatchedForward batched,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> batches =
      batched.getBatchesOpaqueUnlit();

    for (final KMaterialForwardLabel label : batches.keySet()) {
      final List<KMeshInstanceTransformed> batch = batches.get(label);

      KRendererForward.makeUnlitLabel(this.label_cache, label);

      final KProgram p =
        this.shader_cache.luCacheGet(this.label_cache.toString());
      final JCBExecutionAPI e = p.getExecutable();

      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram program)
          throws ConstraintError,
            JCGLException,
            JCBExecutionException,
            Throwable
        {
          KShadingProgramCommon.putMatrixProjection(
            program,
            mwc.getMatrixProjection());

          for (int index = 0; index < batch.size(); ++index) {
            final KMeshInstanceTransformed instance = batch.get(index);
            final WithInstance mwi = mwc.withInstance(instance);
            try {
              KRendererForward.this.renderOpaqueMeshUnlit(
                gc,
                program,
                label,
                instance,
                mwi);
            } finally {
              mwi.instanceFinish();
            }
          }
        }
      });
    }
  }

  private void renderOpaqueMeshLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KLight light,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException,
      KShadowCacheException,
      JCBExecutionException
  {
    final KMeshInstance actual = i.getInstance();
    final KMaterial material = actual.getMaterial();

    this.putMeshInstanceMatrices(program, label, mwi);
    this.putLightReuse(program, light);
    this.putTextures(gc, program, i, label, light);
    KShadingProgramCommon.putMaterial(program, material);
    this.putLightProjectiveMatricesIfNecessary(program, light, mwi);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(program, array);

      switch (label.getNormal()) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(program, array);
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_NONE:
        {
          break;
        }
      }

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(program, array);
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

  private void renderOpaqueMeshUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KMeshInstanceTransformed instance,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws JCGLException,
      ConstraintError,
      JCBExecutionException,
      KShadowCacheException
  {
    final KMeshInstance actual = instance.getInstance();
    final KMaterial material = actual.getMaterial();

    this.putMeshInstanceMatrices(program, label, mwi);
    this.putTextures(gc, program, instance, label, null);
    KShadingProgramCommon.putMaterial(program, material);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(program, array);

      switch (label.getNormal()) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(program, array);
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_NONE:
        {
          break;
        }
      }

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(program, array);
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

  private void renderTranslucentMeshes(
    final @Nonnull KSceneBatchedForward scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc)
    throws KShaderCacheException,
      ConstraintError,
      LUCacheException,
      JCGLException,
      JCBExecutionException
  {
    final List<BatchTranslucent> batches = scene.getBatchesTranslucent();

    for (int index = 0; index < batches.size(); ++index) {
      final BatchTranslucent batch = batches.get(index);
      final KMeshInstanceTransformed instance = batch.getInstance();
      final KMaterialForwardLabel label = batch.getLabel();

      final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);
      try {

        if (batch instanceof BatchTranslucentLit) {
          final List<KLight> lights =
            ((BatchTranslucentLit) batch).getLights();

          final AtomicBoolean first_light = new AtomicBoolean(true);
          for (int light_index = 0; light_index < lights.size(); ++light_index) {
            final KLight light = lights.get(light_index);

            KRendererForward.makeLitLabel(
              this.label_cache,
              this.decider,
              light,
              label);

            final KProgram p =
              this.shader_cache.luCacheGet(this.label_cache.toString());

            final JCBExecutionAPI e = p.getExecutable();
            e.execRun(new JCBExecutorProcedure() {
              @SuppressWarnings("synthetic-access") @Override public
                void
                call(
                  final @Nonnull JCBProgram program)
                  throws ConstraintError,
                    JCGLException,
                    JCBExecutionException,
                    KShadowCacheException,
                    LUCacheException
              {
                KShadingProgramCommon.putMatrixProjection(
                  program,
                  mwc.getMatrixProjection());
                KRendererForward.putLight(
                  light,
                  mwc.getMatrixContext(),
                  mwc.getMatrixView(),
                  program);

                if (first_light.get()) {
                  gc.blendingEnable(
                    BlendFunction.BLEND_ONE,
                    BlendFunction.BLEND_ONE_MINUS_SOURCE_ALPHA);
                } else {
                  gc.blendingEnable(
                    BlendFunction.BLEND_ONE,
                    BlendFunction.BLEND_ONE);
                }

                KRendererForward.this.renderTranslucentMeshLit(
                  gc,
                  program,
                  light,
                  instance,
                  label,
                  mwi);
              }
            });

            first_light.set(false);
          }
        } else {

          KRendererForward.makeUnlitLabel(this.label_cache, label);

          final KProgram p =
            this.shader_cache.luCacheGet(this.label_cache.toString());

          final JCBExecutionAPI e = p.getExecutable();
          e.execRun(new JCBExecutorProcedure() {
            @SuppressWarnings("synthetic-access") @Override public void call(
              final @Nonnull JCBProgram program)
              throws ConstraintError,
                JCGLException,
                JCBExecutionException,
                KShadowCacheException,
                LUCacheException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mwc.getMatrixProjection());

              gc.blendingEnable(
                BlendFunction.BLEND_ONE,
                BlendFunction.BLEND_ONE);

              KRendererForward.this.renderTranslucentMeshUnlit(
                gc,
                program,
                instance,
                label,
                mwi);
            }
          });
        }

      } finally {
        mwi.instanceFinish();
      }
    }
  }

  private void renderTranslucentMeshLit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed i,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws ConstraintError,
      JCGLException,
      KShadowCacheException,
      JCBExecutionException
  {
    final KMeshInstance actual = i.getInstance();
    final KMaterial material = actual.getMaterial();

    this.putMeshInstanceMatrices(program, label, mwi);
    this.putLightReuse(program, light);
    this.putTextures(gc, program, i, label, light);
    KShadingProgramCommon.putMaterial(program, material);
    this.putLightProjectiveMatricesIfNecessary(program, light, mwi);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(program, array);

      switch (label.getNormal()) {
        case NORMAL_MAPPED:
        {
          KShadingProgramCommon.bindAttributeTangent4(program, array);
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_VERTEX:
        {
          KShadingProgramCommon.bindAttributeNormal(program, array);
          break;
        }
        case NORMAL_NONE:
        {
          break;
        }
      }

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(program, array);
      }

      program.programExecute(new JCBProgramProcedure() {
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

  private void renderTranslucentMeshUnlit(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram program,
    final @Nonnull KMeshInstanceTransformed instance,
    final @Nonnull KMaterialForwardLabel label,
    final @Nonnull KMutableMatrices.WithInstance mwi)
    throws JCGLException,
      ConstraintError,
      KShadowCacheException,
      JCBExecutionException
  {
    final KMeshInstance actual = instance.getInstance();
    final KMaterial material = actual.getMaterial();

    this.putMeshInstanceMatrices(program, label, mwi);
    this.putTextures(gc, program, instance, label, null);
    KShadingProgramCommon.putMaterial(program, material);

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = actual.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(program, array);

      if (label.impliesUV()) {
        KShadingProgramCommon.bindAttributeUV(program, array);
      }

      program.programExecute(new JCBProgramProcedure() {
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
}
