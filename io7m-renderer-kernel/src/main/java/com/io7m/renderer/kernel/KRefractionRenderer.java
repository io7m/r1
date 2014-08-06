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

package com.io7m.renderer.kernel;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.NullCheckException;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialRefractiveMasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.renderer.kernel.types.KMaterialRefractiveVisitorType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshBoundsTriangles;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.types.RCoordinates;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionCache;
import com.io7m.renderer.types.RExceptionFramebufferNotBound;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RMatrixReadable4x4FType;
import com.io7m.renderer.types.RSpaceClipType;
import com.io7m.renderer.types.RSpaceNDCType;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceWindowType;
import com.io7m.renderer.types.RTransformModelViewType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTriangle4F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorM3F;
import com.io7m.renderer.types.RVectorM4F;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * The default implementation of the refraction renderer interface.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRefractionRenderer implements
  KRefractionRendererType
{
  private static final String               NAME;
  private static final VectorReadable4FType WHITE;
  private static final int                  WINDOW_BOUNDS_PADDING;

  static {
    WINDOW_BOUNDS_PADDING = 2;
    WHITE = new VectorI4F(1.0f, 1.0f, 1.0f, 1.0f);
  }

  static {
    NAME = "refraction";
  }

  /**
   * Calculate the bounding box for the given mesh and matrices in normalized
   * device coordinates.
   *
   * @return true If the given mesh is actually visible
   */

  private static
    boolean
    calculateNDCBounds(
      final KMeshBoundsCacheType<RSpaceObjectType> bounds_cache,
      final LUCacheType<KMeshBounds<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, RException> bounds_triangle_cache,
      final MatrixM4x4F.Context matrix_context,
      final KMatricesInstanceValuesType mi,
      final KMeshReadableType mesh,
      final RVectorM3F<RSpaceNDCType> ndc_bounds_lower,
      final RVectorM3F<RSpaceNDCType> ndc_bounds_upper)
      throws RException,
        JCacheException
  {
    final RMatrixReadable4x4FType<RTransformModelViewType> mmv =
      mi.getMatrixModelView();
    final RMatrixReadable4x4FType<RTransformProjectionType> mp =
      mi.getMatrixProjection();

    final KMeshBounds<RSpaceObjectType> bounds =
      bounds_cache.cacheGetLU(mesh);
    final KMeshBoundsTriangles<RSpaceObjectType> obj_triangles =
      bounds_triangle_cache.cacheGetLU(bounds);

    /**
     * Transform the triangles of the bounding box that contains the mesh from
     * object space to clip space.
     */

    final RVectorM4F<RSpaceClipType> clip_temp =
      new RVectorM4F<RSpaceClipType>();
    final KMeshBoundsTriangles<RSpaceClipType> clip_triangles =
      obj_triangles
        .transform(new PartialFunctionType<RTriangle4F<RSpaceObjectType>, RTriangle4F<RSpaceClipType>, NullCheckException>() {
          @Override public RTriangle4F<RSpaceClipType> call(
            final RTriangle4F<RSpaceObjectType> ot)
          {
            return ot
              .transform(new PartialFunctionType<RVectorI4F<RSpaceObjectType>, RVectorI4F<RSpaceClipType>, NullCheckException>() {
                @Override public RVectorI4F<RSpaceClipType> call(
                  final RVectorI4F<RSpaceObjectType> op)
                {
                  clip_temp.copyFrom4F(op);

                  MatrixM4x4F.multiplyVector4FWithContext(
                    matrix_context,
                    mmv,
                    clip_temp,
                    clip_temp);
                  MatrixM4x4F.multiplyVector4FWithContext(
                    matrix_context,
                    mp,
                    clip_temp,
                    clip_temp);
                  return new RVectorI4F<RSpaceClipType>(clip_temp);
                }
              });
          }
        });

    /**
     * Now, clip all of the clip-space triangles produced against the six
     * homogeneous clipping planes.
     */

    final List<RTriangle4F<RSpaceClipType>> triangles =
      new ArrayList<RTriangle4F<RSpaceClipType>>();
    triangles.add(clip_triangles.getBack0());
    triangles.add(clip_triangles.getBack1());
    triangles.add(clip_triangles.getBottom0());
    triangles.add(clip_triangles.getBottom1());
    triangles.add(clip_triangles.getFront0());
    triangles.add(clip_triangles.getFront1());
    triangles.add(clip_triangles.getLeft0());
    triangles.add(clip_triangles.getLeft1());
    triangles.add(clip_triangles.getRight0());
    triangles.add(clip_triangles.getRight1());
    triangles.add(clip_triangles.getTop0());
    triangles.add(clip_triangles.getTop1());

    /**
     * If clipping any of the triangles actually produces triangles as a
     * result, then at least one triangle is visible onscreen.
     */

    ndc_bounds_lower.set3F(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    ndc_bounds_upper.set3F(
      -Float.MAX_VALUE,
      -Float.MAX_VALUE,
      -Float.MAX_VALUE);

    final RVectorM3F<RSpaceNDCType> ndc_temp =
      new RVectorM3F<RSpaceNDCType>();

    boolean visible = false;
    for (int index = 0; index < triangles.size(); ++index) {
      final RTriangle4F<RSpaceClipType> t = triangles.get(index);
      assert t != null;

      final List<RTriangle4F<RSpaceClipType>> r =
        KTriangleClipping.clipTrianglePlanes(t, KTriangleClipping.PLANES);

      if (r.size() > 0) {
        visible = true;

        /**
         * For each clipped triangle, accumulate the lower and upper bounds.
         */

        for (int ki = 0; ki < r.size(); ++ki) {
          final RTriangle4F<RSpaceClipType> rt = r.get(ki);
          final RVectorI4F<RSpaceClipType> p0 = rt.getP0();
          final RVectorI4F<RSpaceClipType> p1 = rt.getP1();
          final RVectorI4F<RSpaceClipType> p2 = rt.getP2();

          RCoordinates.clipToNDC(p0, ndc_temp);
          KRefractionRenderer.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);

          RCoordinates.clipToNDC(p1, ndc_temp);
          KRefractionRenderer.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);

          RCoordinates.clipToNDC(p2, ndc_temp);
          KRefractionRenderer.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);
        }
      }
    }

    return visible;
  }

  private static void calculateNDCBoundsAccumulate(
    final RVectorM3F<RSpaceNDCType> ndc_bounds_lower,
    final RVectorM3F<RSpaceNDCType> ndc_bounds_upper,
    final RVectorReadable3FType<RSpaceNDCType> ndc)
  {
    final float x = ndc.getXF();
    final float y = ndc.getYF();
    final float z = ndc.getZF();

    ndc_bounds_lower.set3F(
      Math.min(ndc_bounds_lower.getXF(), x),
      Math.min(ndc_bounds_lower.getYF(), y),
      Math.min(ndc_bounds_lower.getZF(), z));

    ndc_bounds_upper.set3F(
      Math.max(ndc_bounds_upper.getXF(), x),
      Math.max(ndc_bounds_upper.getYF(), y),
      Math.max(ndc_bounds_upper.getZF(), z));
  }

  private static void calculateWindowBounds(
    final AreaInclusive area,
    final RVectorReadable3FType<RSpaceNDCType> ndc_bounds_lower,
    final RVectorReadable3FType<RSpaceNDCType> ndc_bounds_upper,
    final RVectorM3F<RSpaceWindowType> window_bounds_lower,
    final RVectorM3F<RSpaceWindowType> window_bounds_upper)
  {
    RCoordinates.ndcToWindow(
      ndc_bounds_lower,
      window_bounds_lower,
      area,
      0.0f,
      1.0f);

    RCoordinates.ndcToWindow(
      ndc_bounds_upper,
      window_bounds_upper,
      area,
      0.0f,
      1.0f);

    /**
     * Due to numerical inaccuracy, the calculated bounds can sometimes be a
     * pixel or so too small onscreen. The refraction effect doesn't require
     * accurate bounds, so it's best for the bounds to be slightly too large
     * than too small.
     */

    window_bounds_lower
      .set2F(
        window_bounds_lower.getXF()
          - KRefractionRenderer.WINDOW_BOUNDS_PADDING,
        window_bounds_lower.getYF()
          - KRefractionRenderer.WINDOW_BOUNDS_PADDING);
    window_bounds_upper
      .set2F(
        window_bounds_upper.getXF()
          + KRefractionRenderer.WINDOW_BOUNDS_PADDING,
        window_bounds_upper.getYF()
          + KRefractionRenderer.WINDOW_BOUNDS_PADDING);
  }

  /**
   * Construct a new refraction renderer.
   *
   * @param gl
   *          The OpenGL implementation
   * @param copier
   *          A region copier
   * @param shader_cache
   *          A shader cache
   * @param forward_cache
   *          A framebuffer cache
   * @param bounds_cache
   *          A bounds cache
   * @param bounds_tri_cache
   *          A triangle cache
   * @return A new renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRefractionRendererType newRenderer(
    final JCGLImplementationType gl,
    final KRegionCopierType copier,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KFramebufferForwardCacheType forward_cache,
    final KMeshBoundsCacheType<RSpaceObjectType> bounds_cache,
    final KMeshBoundsTrianglesCacheType<RSpaceObjectType> bounds_tri_cache)
    throws RException
  {
    return new KRefractionRenderer(
      gl,
      copier,
      shader_cache,
      forward_cache,
      bounds_cache,
      bounds_tri_cache);
  }

  private static void putInstanceAttributes(
    final KMaterialTranslucentRefractive material,
    final ArrayBufferUsableType array,
    final JCBProgramType program)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.bindAttributeTangent4(program, array);
          KShadingProgramCommon.bindAttributeNormal(program, array);
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.bindAttributeNormal(program, array);
          return Unit.unit();
        }
      });

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
    }
  }

  private static void putInstanceMatrices(
    final JCBProgramType program,
    final KMatricesInstanceValuesType mwi,
    final KMaterialTranslucentRefractive material)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
    }

    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
  }

  private static
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    putTextures(
      final KMaterialTranslucentRefractive material,
      final KFramebufferRGBAUsableType scene,
      final JCBProgramType program,
      final KTextureUnitContextType context)
      throws JCGLException,
        RException
  {
    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureNormal(
            program,
            context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    KShadingProgramCommon.putRefractionTextureScene(
      program,
      context.withTexture2D(scene.kFramebufferGetRGBATexture()));
  }

  private static void rendererRefractionEvaluateForInstanceMasked(
    final JCGLImplementationType g,
    final KFramebufferForwardCacheType forward_cache,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KRegionCopierType copier,
    final KFramebufferForwardUsableType scene,
    final KFramebufferForwardUsableType temporary,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final AreaInclusive window_bounds_area)
    throws RException,
      JCacheException,
      JCGLException
  {
    final KMeshReadableType mesh = r.instanceGetMesh();

    final BLUCacheReceiptType<KFramebufferForwardDescription, KFramebufferForwardUsableType> scene_mask =
      forward_cache.bluCacheGet(scene.kFramebufferGetForwardDescription());

    try {
      final KFramebufferForwardUsableType mask = scene_mask.getValue();

      copier.copierCopyRGBAWithDepth(
        scene,
        window_bounds_area,
        mask,
        window_bounds_area);

      KRefractionRenderer.rendererRefractionEvaluateRenderMask(
        g,
        shader_cache,
        mask,
        r,
        mi,
        mesh);

      KRefractionRenderer.rendererRefractionEvaluateRenderMasked(
        g,
        shader_cache,
        unit_allocator,
        scene,
        mask,
        temporary,
        r,
        mi,
        mesh);

    } finally {
      scene_mask.returnToCache();
    }
  }

  private static void rendererRefractionEvaluateForInstanceUnmasked(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KFramebufferForwardUsableType scene,
    final KFramebufferForwardUsableType temporary,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi)
    throws JCGLException,
      RException,
      JCacheException
  {
    KRefractionRenderer.rendererRefractionEvaluateRenderUnmasked(
      g,
      shader_cache,
      unit_allocator,
      scene,
      temporary,
      r,
      mi,
      r.instanceGetMesh());
  }

  private static void rendererRefractionEvaluateRenderMask(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KFramebufferRGBAUsableType scene_mask,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final KMeshReadableType mesh)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KProgramType kprogram = shader_cache.cacheGetLU("refraction_mask");

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          try {
            gc.framebufferDrawBind(scene_mask
              .kFramebufferGetColorFramebuffer());

            program.programUniformPutVector4f(
              "f_ccolor",
              KRefractionRenderer.WHITE);

            gc.blendingDisable();

            KRendererCommon.renderConfigureFaceCulling(
              gc,
              r.instanceGetFaceSelection());

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 1.0f);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
            gc.depthBufferWriteDisable();

            KShadingProgramCommon.putMatrixProjectionUnchecked(
              program,
              mi.getMatrixProjection());

            KShadingProgramCommon.putMatrixModelViewUnchecked(
              program,
              mi.getMatrixModelView());

            final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
            final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(
              program,
              array);

            program
              .programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                }
              });

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  private static
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    rendererRefractionEvaluateRenderMasked(
      final JCGLImplementationType g,
      final KShaderCacheForwardTranslucentUnlitType shader_cache,
      final KTextureUnitAllocator unit_allocator,
      final KFramebufferRGBAUsableType scene,
      final KFramebufferRGBAUsableType scene_mask,
      final KFramebufferRGBAUsableType destination,
      final KInstanceTranslucentRefractive r,
      final KMatricesInstanceValuesType mi,
      final KMeshReadableType mesh)
      throws JCGLException,
        RException,
        JCacheException
  {
    final KMaterialTranslucentRefractive material = r.getMaterial();
    final String shader_code = material.materialGetCode();
    final KProgramType kprogram = shader_cache.cacheGetLU(shader_code);

    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          try {
            gc.framebufferDrawBind(destination
              .kFramebufferGetColorFramebuffer());

            gc.blendingDisable();

            KRendererCommon.renderConfigureFaceCulling(
              gc,
              r.instanceGetFaceSelection());

            gc.colorBufferMask(true, true, true, true);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
            gc.depthBufferWriteDisable();

            unit_allocator.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws JCGLException,
                  RException
              {
                KShadingProgramCommon.putMatrixProjectionUnchecked(
                  program,
                  mi.getMatrixProjection());

                KRefractionRenderer
                  .putInstanceMatrices(program, mi, material);

                KRefractionRenderer.putTextures(
                  material,
                  scene,
                  program,
                  context);

                KShadingProgramCommon.putRefractionTextureSceneMask(
                  program,
                  context.withTexture2D(scene_mask
                    .kFramebufferGetRGBATexture()));

                material.getRefractive().refractiveAccept(
                  new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
                    @Override public Unit masked(
                      final KMaterialRefractiveMasked m)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMaterialRefractiveMasked(
                        program,
                        m);
                      return Unit.unit();
                    }

                    @Override public Unit unmasked(
                      final KMaterialRefractiveUnmasked m)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMaterialRefractiveUnmasked(
                        program,
                        m);
                      return Unit.unit();
                    }
                  });

                gc.arrayBufferBind(array);
                KRefractionRenderer.putInstanceAttributes(
                  material,
                  array,
                  program);

                program
                  .programExecute(new JCBProgramProcedureType<JCGLException>() {
                    @Override public void call()
                      throws JCGLException
                    {
                      gc
                        .drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                    }
                  });
              }

            });

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  private static void rendererRefractionEvaluateRenderUnmasked(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KFramebufferForwardUsableType scene,
    final KFramebufferForwardUsableType temporary,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final KMeshReadableType mesh)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KMaterialTranslucentRefractive material = r.getMaterial();
    final String shader_code = material.materialGetCode();

    final KProgramType kprogram = shader_cache.cacheGetLU(shader_code);
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          try {
            gc.framebufferDrawBind(temporary
              .kFramebufferGetColorFramebuffer());

            gc.blendingDisable();

            KRendererCommon.renderConfigureFaceCulling(
              gc,
              r.instanceGetFaceSelection());

            gc.colorBufferMask(true, true, true, true);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
            gc.depthBufferWriteDisable();

            unit_allocator.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws JCGLException,
                  RException
              {
                KShadingProgramCommon.putMatrixProjectionUnchecked(
                  program,
                  mi.getMatrixProjection());

                KRefractionRenderer
                  .putInstanceMatrices(program, mi, material);

                KRefractionRenderer.putTextures(
                  material,
                  scene,
                  program,
                  context);

                material.getRefractive().refractiveAccept(
                  new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
                    @Override public Unit masked(
                      final KMaterialRefractiveMasked m)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMaterialRefractiveMasked(
                        program,
                        m);
                      return Unit.unit();
                    }

                    @Override public Unit unmasked(
                      final KMaterialRefractiveUnmasked m)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMaterialRefractiveUnmasked(
                        program,
                        m);
                      return Unit.unit();
                    }
                  });

                gc.arrayBufferBind(array);
                KRefractionRenderer.putInstanceAttributes(
                  material,
                  array,
                  program);

                program
                  .programExecute(new JCBProgramProcedureType<JCGLException>() {
                    @Override public void call()
                      throws JCGLException
                    {
                      gc
                        .drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                    }
                  });
              }

            });

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  private final KMeshBoundsCacheType<RSpaceObjectType>          bounds_cache;
  private final KMeshBoundsTrianglesCacheType<RSpaceObjectType> bounds_tri_cache;
  private final KRegionCopierType                               copier;
  private final KFramebufferForwardCacheType                    forward_cache;
  private final JCGLImplementationType                          g;
  private final MatrixM4x4F.Context                             matrix_context;
  private final RVectorM3F<RSpaceNDCType>                       ndc_bounds_lower;
  private final RVectorM3F<RSpaceNDCType>                       ndc_bounds_upper;
  private final KShaderCacheForwardTranslucentUnlitType         shader_cache;
  private final KTextureUnitAllocator                           texture_units;
  private final RVectorM3F<RSpaceWindowType>                    window_bounds_lower;
  private final RVectorM3F<RSpaceWindowType>                    window_bounds_upper;

  private KRefractionRenderer(
    final JCGLImplementationType gl,
    final KRegionCopierType in_copier,
    final KShaderCacheForwardTranslucentUnlitType in_shader_cache,
    final KFramebufferForwardCacheType in_forward_cache,
    final KMeshBoundsCacheType<RSpaceObjectType> in_bounds_cache,
    final KMeshBoundsTrianglesCacheType<RSpaceObjectType> in_bounds_tri_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(gl, "OpenGL implementation");
      this.copier = NullCheck.notNull(in_copier, "Copier");
      this.forward_cache =
        NullCheck.notNull(in_forward_cache, "Forward framebuffer cache");
      this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
      this.bounds_cache = NullCheck.notNull(in_bounds_cache, "Bounds cache");
      this.bounds_tri_cache =
        NullCheck.notNull(in_bounds_tri_cache, "Bounds triangle cache");

      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());
      this.window_bounds_lower = new RVectorM3F<RSpaceWindowType>();
      this.window_bounds_upper = new RVectorM3F<RSpaceWindowType>();
      this.ndc_bounds_lower = new RVectorM3F<RSpaceNDCType>();
      this.ndc_bounds_upper = new RVectorM3F<RSpaceNDCType>();
      this.matrix_context = new MatrixM4x4F.Context();

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRefractionRenderer.NAME;
  }

  @Override public void rendererRefractionEvaluate(
    final KFramebufferForwardUsableType scene,
    final KMatricesObserverType observer,
    final KInstanceTranslucentRefractive r)
    throws RException
  {
    NullCheck.notNull(scene, "Scene");
    NullCheck.notNull(observer, "Observer");
    NullCheck.notNull(r, "Refractive instance");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      if (gc.framebufferDrawIsBound(scene.kFramebufferGetColorFramebuffer()) == false) {
        throw new RExceptionFramebufferNotBound("Framebuffer is not bound");
      }

      observer.withInstance(
        r,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mi)
            throws RException,
              JCGLException
          {
            try {
              KRefractionRenderer.this.rendererRefractionEvaluateForInstance(
                scene,
                r,
                mi);
            } catch (final JCacheException e) {
              throw RExceptionCache.fromJCacheException(e);
            }
            return Unit.unit();
          }
        });

      if (gc.framebufferDrawIsBound(scene.kFramebufferGetColorFramebuffer()) == false) {
        throw new RExceptionFramebufferNotBound("Framebuffer is not bound");
      }

    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  private void rendererRefractionEvaluateForInstance(
    final KFramebufferForwardUsableType scene,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KMeshReadableType mesh = r.instanceGetMesh();

    final boolean visible =
      KRefractionRenderer.calculateNDCBounds(
        this.bounds_cache,
        this.bounds_tri_cache,
        this.matrix_context,
        mi,
        mesh,
        this.ndc_bounds_lower,
        this.ndc_bounds_upper);

    if (visible == true) {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();
      gc.blendingDisable();

      KRefractionRenderer.calculateWindowBounds(
        scene.kFramebufferGetArea(),
        this.ndc_bounds_lower,
        this.ndc_bounds_upper,
        this.window_bounds_lower,
        this.window_bounds_upper);

      final BLUCacheReceiptType<KFramebufferForwardDescription, KFramebufferForwardUsableType> temporary =
        this.forward_cache.bluCacheGet(scene
          .kFramebufferGetForwardDescription());

      try {
        final float lo_x = this.window_bounds_lower.getXF();
        final float hi_x = this.window_bounds_upper.getXF();
        final float lo_y = this.window_bounds_lower.getYF();
        final float hi_y = this.window_bounds_upper.getYF();

        final RangeInclusiveL range_x =
          new RangeInclusiveL((long) lo_x, (long) hi_x);
        final RangeInclusiveL range_y =
          new RangeInclusiveL((long) lo_y, (long) hi_y);
        final AreaInclusive window_bounds_area =
          new AreaInclusive(range_x, range_y);

        this.copier.copierCopyRGBAWithDepth(
          scene,
          window_bounds_area,
          temporary.getValue(),
          window_bounds_area);

        final KMaterialTranslucentRefractive material = r.getMaterial();

        material.getRefractive().refractiveAccept(
          new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
            @Override public Unit masked(
              final KMaterialRefractiveMasked m)
              throws RException,
                JCGLException
            {
              try {
                KRefractionRenderer
                  .rendererRefractionEvaluateForInstanceMasked(
                    KRefractionRenderer.this.g,
                    KRefractionRenderer.this.forward_cache,
                    KRefractionRenderer.this.shader_cache,
                    KRefractionRenderer.this.texture_units,
                    KRefractionRenderer.this.copier,
                    scene,
                    temporary.getValue(),
                    r,
                    mi,
                    window_bounds_area);
                return Unit.unit();
              } catch (final JCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }

            @Override public Unit unmasked(
              final KMaterialRefractiveUnmasked m)
              throws RException,
                JCGLException
            {
              try {
                KRefractionRenderer
                  .rendererRefractionEvaluateForInstanceUnmasked(
                    KRefractionRenderer.this.g,
                    KRefractionRenderer.this.shader_cache,
                    KRefractionRenderer.this.texture_units,
                    scene,
                    temporary.getValue(),
                    r,
                    mi);
                return Unit.unit();
              } catch (final JCacheException e) {
                throw new UnreachableCodeException(e);
              }
            }
          });

        this.copier.copierCopyRGBAWithDepth(
          temporary.getValue(),
          window_bounds_area,
          scene,
          window_bounds_area);

      } finally {
        temporary.returnToCache();
      }

      gc.framebufferDrawBind(scene.kFramebufferGetColorFramebuffer());
    }
  }
}
