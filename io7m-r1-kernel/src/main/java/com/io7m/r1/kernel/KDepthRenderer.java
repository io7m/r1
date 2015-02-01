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
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KDepthInstancesType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * The default depth renderer implementation.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KDepthRenderer implements
  KDepthRendererType
{
  private static final String NAME;

  static {
    NAME = "depth";
  }

  private static Map<String, String> makeCodeMap()
  {
    final Map<String, String> m = new HashMap<String, String>();

    {
      final String code = KMaterialDepthAlpha.getMaterialCode();
      m.put(code, code);
    }
    {
      final String code = KMaterialDepthConstant.getMaterialCode();
      m.put(code, code);
    }

    return m;
  }

  /**
   * Construct a new depth renderer.
   *
   * @param g
   *          The OpenGL implementation
   * @param bindings
   *          A texture binding controller
   * @param shader_cache
   *          The shader cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KDepthRendererType newRenderer(
    final JCGLImplementationType g,
    final KTextureBindingsControllerType bindings,
    final KShaderCacheDepthType shader_cache,
    final LogUsableType log)
    throws RException
  {
    return new KDepthRenderer(g, shader_cache, bindings, log);
  }

  private static void renderDepthPassBatch(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsControllerType bindings,
    final KMatricesObserverType mwo,
    final JCBProgramType jp,
    final List<KInstanceOpaqueType> batch,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    for (final KInstanceOpaqueType i : batch) {
      assert i != null;

      mwo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mwi)
            throws RException
          {
            bindings
              .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
                @Override public void call(
                  final KTextureBindingsContextType c)
                  throws RException
                {
                  KDepthRenderer.renderDepthPassInstance(
                    gc,
                    c,
                    mwi,
                    jp,
                    i,
                    faces);
                }
              });
            return Unit.unit();
          }
        });
    }
  }

  private static void renderDepthPassInstance(
    final JCGLInterfaceCommonType gc,
    final KTextureBindingsContextType units,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType jp,
    final KInstanceOpaqueType i,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(jp, array);
      KShadingProgramCommon.putAttributeNormalUnchecked(jp, VectorI3F.ZERO);
      KShadingProgramCommon.bindAttributeUVUnchecked(jp, array);

      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixProjectionReuse(jp);
      KShadingProgramCommon.putMatrixModelViewUnchecked(
        jp,
        mwi.getMatrixModelView());
      KShadingProgramCommon.putDepthCoefficientReuse(jp);
      KShadingProgramCommon.putMatrixNormal(jp, mwi.getMatrixNormal());
      KShadingProgramCommon.putMatrixUVUnchecked(jp, mwi.getMatrixUV());

      i.opaqueAccept(new KInstanceOpaqueVisitorType<Unit, RException>() {
        @Override public Unit regular(
          final KInstanceOpaqueRegular o)
          throws RException
        {
          final KMaterialOpaqueRegular material = o.getMaterial();
          final KMaterialDepthType depth = material.getDepth();
          depth
            .depthAccept(new KMaterialDepthVisitorType<Unit, JCGLException>() {
              @Override public Unit alpha(
                final KMaterialDepthAlpha mda)
                throws RException
              {
                /**
                 * Material is alpha-to-depth; must upload albedo texture,
                 * matrices, and threshold value.
                 */

                KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                  jp,
                  mda.getAlphaThreshold());
                KShadingProgramCommon.putMaterialAlbedoTypeWithTextures(
                  jp,
                  units,
                  material);
                return Unit.unit();
              }

              @Override public Unit constant(
                final KMaterialDepthConstant m)
                throws RException
              {
                KShadingProgramCommon.putAttributeUVUnchecked(
                  jp,
                  VectorI2F.ZERO);
                return Unit.unit();
              }
            });

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

  private final KTextureBindingsControllerType bindings;
  private final Map<String, String>            code_map;
  private final JCGLImplementationType         g;
  private final LogUsableType                  log;
  private final KMutableMatrices               matrices;
  private final KShaderCacheDepthType          shader_cache;

  private KDepthRenderer(
    final JCGLImplementationType gl,
    final KShaderCacheDepthType in_shader_cache,
    final KTextureBindingsControllerType in_bindings,
    final LogUsableType in_log)
  {
    this.log = NullCheck.notNull(in_log, "log").with("depth-renderer");
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.bindings = NullCheck.notNull(in_bindings, "Texture bindings");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.matrices = KMutableMatrices.newMatrices();
    this.code_map = KDepthRenderer.makeCodeMap();

    if (this.log.wouldLog(LogLevel.LOG_DEBUG)) {
      this.log.debug("initialized");
    }
  }

  private void renderDepthPassBatches(
    final KDepthInstancesType instances,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException,
      JCacheException
  {
    for (final String depth_code : instances.getMaterialCodes()) {
      assert depth_code != null;
      final List<KInstanceOpaqueType> batch =
        instances.getInstancesForMaterial(depth_code);
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
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            jp,
            mwo.getMatrixProjection());
          KShadingProgramCommon.putDepthCoefficient(
            jp,
            KRendererCommon.depthCoefficient(mwo.getProjection()));
          KDepthRenderer.renderDepthPassBatch(
            gc,
            KDepthRenderer.this.bindings,
            mwo,
            jp,
            batch,
            faces);
        }
      });
    }
  }

  @Override public void rendererEvaluateDepth(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final KFramebufferDepthUsableType framebuffer,
    final OptionType<KFaceSelection> faces)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(projection, "Projection matrix");
    NullCheck.notNull(instances, "Instances");
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(faces, "Faces");

    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    final FramebufferUsableType fb = framebuffer.getDepthPassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      this.rendererEvaluateDepthWithBoundFramebuffer(
        view,
        projection,
        instances,
        framebuffer.getArea(),
        faces);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  @Override public void rendererEvaluateDepthWithBoundFramebuffer(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(projection, "Projection matrix");
    NullCheck.notNull(instances, "Instances");
    NullCheck.notNull(framebuffer_area, "Framebuffer area");
    NullCheck.notNull(faces, "Faces");

    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    this.matrices.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesObserverType mwo)
          throws RException,
            JCGLException
        {
          try {
            KDepthRenderer.this.renderScene(
              gc,
              instances,
              framebuffer_area,
              mwo,
              faces);
            return Unit.unit();
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
  }

  @Override public String rendererGetName()
  {
    return KDepthRenderer.NAME;
  }

  private void renderScene(
    final JCGLInterfaceCommonType gc,
    final KDepthInstancesType instances,
    final AreaInclusive area,
    final KMatricesObserverType mwo,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException,
      JCacheException
  {
    gc.blendingDisable();
    gc.colorBufferMask(false, false, false, false);
    gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
    gc.cullingDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
    gc.depthBufferWriteEnable();
    gc.depthBufferClear(1.0f);
    gc.scissorDisable();
    gc.viewportSet(area);
    this.renderDepthPassBatches(instances, gc, mwo, faces);
  }
}
