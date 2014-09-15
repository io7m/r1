/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.r1.kernel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueType;
import com.io7m.r1.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionIdentity;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionJCGL;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformViewType;

/**
 * The default depth renderer implementation that can produce paraboloid maps.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KDepthParaboloidRenderer implements
  KDepthParaboloidRendererType
{
  private static final String NAME;

  static {
    NAME = "depth-paraboloid";
  }

  private static Map<String, String> makeCodeMap()
  {
    final Map<String, String> m = new HashMap<String, String>();

    {
      final String code = KMaterialDepthAlpha.getMaterialCode();
      m.put(code, String.format("paraboloid_%s", code));
    }
    {
      final String code = KMaterialDepthConstant.getMaterialCode();
      m.put(code, String.format("paraboloid_%s", code));
    }

    return m;
  }

  /**
   * Construct a new depth renderer.
   *
   * @param g
   *          The OpenGL implementation
   * @param shader_cache
   *          The shader cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KDepthParaboloidRendererType newRenderer(
    final JCGLImplementationType g,
    final KShaderCacheDepthType shader_cache,
    final LogUsableType log)
    throws RException
  {
    return new KDepthParaboloidRenderer(g, shader_cache, log);
  }

  private static void renderBatch(
    final KMatricesObserverType mo,
    final JCGLInterfaceCommonType gc,
    final JCBProgramType jp,
    final List<KInstanceOpaqueType> batch,
    final OptionType<KFaceSelection> faces)
    throws RException,
      JCGLException
  {
    final KProjectionType proj = mo.getProjection();
    KShadingProgramCommon.putProjection(jp, proj);

    for (final KInstanceOpaqueType i : batch) {
      assert i != null;

      KShadingProgramCommon.putProjectionReuse(jp);

      mo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mi)
            throws JCGLException,
              RException
          {
            KDepthParaboloidRenderer.renderInstance(mi, gc, jp, i, faces);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderInstance(
    final KMatricesInstanceType mi,
    final JCGLInterfaceCommonType gc,
    final JCBProgramType jp,
    final KInstanceOpaqueType i,
    final OptionType<KFaceSelection> faces)
    throws RException,
      JCGLException
  {
    final KMaterialOpaqueType material =
      i
        .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueType, RException>() {
          @Override public KMaterialOpaqueType regular(
            final KInstanceOpaqueRegular o)
            throws RException
          {
            return o.getMaterial();
          }
        });

    final List<TextureUnitType> units = gc.textureGetUnits();

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(jp, array);

      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixModelViewUnchecked(
        jp,
        mi.getMatrixModelView());

      final KMaterialDepthType depth = material.materialOpaqueGetDepth();
      depth.depthAccept(new KMaterialDepthVisitorType<Unit, JCGLException>() {
        @Override public Unit alpha(
          final KMaterialDepthAlpha mda)
          throws RException,
            JCGLException
        {
          /**
           * Material is alpha-to-depth; must upload albedo texture, matrices,
           * and threshold value.
           */

          return material
            .opaqueAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
              @Override public Unit materialOpaqueRegular(
                final KMaterialOpaqueRegular mor)
                throws RException,
                  JCGLException
              {
                return mor.materialRegularGetAlbedo().albedoAccept(
                  new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
                    @Override public Unit textured(
                      final KMaterialAlbedoTextured mat)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMatrixUVUnchecked(
                        jp,
                        mi.getMatrixUV());
                      KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                        jp,
                        mda.getAlphaThreshold());

                      KShadingProgramCommon
                        .putMaterialAlbedoTextured(jp, mat);

                      final TextureUnitType u = units.get(0);
                      assert u != null;
                      KShadingProgramCommon.bindPutTextureAlbedo(
                        jp,
                        gc,
                        mat,
                        u);

                      KShadingProgramCommon.bindAttributeUVUnchecked(
                        jp,
                        array);
                      return Unit.unit();
                    }

                    @Override public Unit untextured(
                      final KMaterialAlbedoUntextured mau)
                    {
                      /**
                       * Unreachable because material verification does not
                       * allow untextured albedo with alpha-to-depth.
                       */

                      throw new UnreachableCodeException();
                    }
                  });
              }
            });
        }

        @Override public Unit constant(
          final KMaterialDepthConstant m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

      /**
       * If there's an override for face culling specified, use it. Otherwise,
       * use the per-instance face culling settings.
       */

      if (faces.isNone()) {
        KRendererCommon.renderConfigureFaceCulling(
          gc,
          i.instanceGetFaceSelection());
      } else {
        final Some<KFaceSelection> some = (Some<KFaceSelection>) faces;
        KRendererCommon.renderConfigureFaceCulling(gc, some.get());
      }

      jp.programExecute(new JCBProgramProcedureType<JCGLException>() {
        @Override public void call()
          throws JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private final Map<String, String>    code_map;
  private final JCGLImplementationType g;
  private final LogUsableType          log;
  private final KMutableMatrices       matrices;
  private final KShaderCacheDepthType  shader_cache;

  private KDepthParaboloidRenderer(
    final JCGLImplementationType gl,
    final KShaderCacheDepthType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "log").with("depth-paraboloid-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.matrices = KMutableMatrices.newMatrices();
    this.code_map = KDepthParaboloidRenderer.makeCodeMap();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  @Override public void rendererEvaluateDepthParaboloid(
    final RMatrixI4x4F<RTransformViewType> view,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final KFramebufferDepthUsableType framebuffer,
    final OptionType<KFaceSelection> faces,
    final float z_near,
    final float z_far)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(batches, "Batches");
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(faces, "Faces");

    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final FramebufferUsableType fb =
      framebuffer.kFramebufferGetDepthPassFramebuffer();

    try {
      gc.framebufferDrawBind(fb);
      try {
        this.rendererEvaluateDepthParaboloidWithBoundFramebuffer(
          view,
          batches,
          framebuffer.kFramebufferGetArea(),
          faces,
          z_near,
          z_far);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateDepthParaboloidWithBoundFramebuffer(
    final RMatrixI4x4F<RTransformViewType> view,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces,
    final float z_near,
    final float z_far)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(batches, "Batches");
    NullCheck.notNull(framebuffer_area, "Framebuffer area");
    NullCheck.notNull(faces, "Faces");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      gc.blendingDisable();
      gc.cullingDisable();
      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.scissorDisable();
      gc.viewportSet(framebuffer_area);

      final KProjectionType projection =
        KProjectionIdentity.newProjection(z_near, z_far);

      this.matrices.withObserver(
        view,
        projection,
        new KMatricesObserverFunctionType<Unit, JCacheException>() {
          @Override public Unit run(
            final KMatricesObserverType mo)
            throws JCacheException,
              JCGLException,
              RException
          {
            KDepthParaboloidRenderer.this.renderBatches(
              mo,
              batches,
              faces,
              gc);
            return Unit.unit();
          }
        });

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw RExceptionCache.fromJCacheException(e);
    }
  }

  private void renderBatches(
    final KMatricesObserverType mo,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final OptionType<KFaceSelection> faces,
    final JCGLInterfaceCommonType gc)
    throws RException,
      JCacheException,
      JCGLException
  {
    for (final String depth_code : batches.keySet()) {
      assert depth_code != null;
      final List<KInstanceOpaqueType> batch = batches.get(depth_code);
      assert batch != null;

      final String shader_code = this.code_map.get(depth_code);
      assert shader_code != null;

      final KProgramType program = this.shader_cache.cacheGetLU(shader_code);
      final JCBExecutorType exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType jp)
          throws JCGLException,
            RException
        {
          KDepthParaboloidRenderer.renderBatch(mo, gc, jp, batch, faces);
        }
      });
    }
  }

  @Override public String rendererGetName()
  {
    return KDepthParaboloidRenderer.NAME;
  }
}
