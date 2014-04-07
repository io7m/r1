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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCacheType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshBoundsTriangles;
import com.io7m.renderer.types.RCoordinates;
import com.io7m.renderer.types.RException;
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

public final class KRefractionRenderer implements KRefractionRendererType
{
  private static final @Nonnull String           NAME;
  private static final @Nonnull VectorReadable4F WHITE;

  private static final int                       WINDOW_BOUNDS_PADDING;

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
      final @Nonnull KMeshBoundsCacheType<RSpaceObjectType> bounds_cache,
      final @Nonnull LUCacheType<KMeshBounds<RSpaceObjectType>, KMeshBoundsTriangles<RSpaceObjectType>, RException> bounds_triangle_cache,
      final @Nonnull MatrixM4x4F.Context matrix_context,
      final @Nonnull KMutableMatricesType.MatricesInstanceType mi,
      final @Nonnull KMesh mesh,
      final @Nonnull RVectorM3F<RSpaceNDCType> ndc_bounds_lower,
      final @Nonnull RVectorM3F<RSpaceNDCType> ndc_bounds_upper)
      throws ConstraintError,
        RException,
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
        .transform(new PartialFunction<RTriangle4F<RSpaceObjectType>, RTriangle4F<RSpaceClipType>, Constraints.ConstraintError>() {
          @Override public RTriangle4F<RSpaceClipType> call(
            final RTriangle4F<RSpaceObjectType> ot)
            throws ConstraintError
          {
            return ot
              .transform(new PartialFunction<RVectorI4F<RSpaceObjectType>, RVectorI4F<RSpaceClipType>, Constraints.ConstraintError>() {
                @Override public RVectorI4F<RSpaceClipType> call(
                  final RVectorI4F<RSpaceObjectType> op)
                  throws ConstraintError
                {
                  clip_temp.x = op.x;
                  clip_temp.y = op.y;
                  clip_temp.z = op.z;
                  clip_temp.w = op.w;

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

    ndc_bounds_lower.x = Float.MAX_VALUE;
    ndc_bounds_lower.y = Float.MAX_VALUE;
    ndc_bounds_lower.z = Float.MAX_VALUE;
    ndc_bounds_upper.x = -Float.MAX_VALUE;
    ndc_bounds_upper.y = -Float.MAX_VALUE;
    ndc_bounds_upper.z = -Float.MAX_VALUE;

    final RVectorM3F<RSpaceNDCType> ndc_temp =
      new RVectorM3F<RSpaceNDCType>();

    boolean visible = false;
    for (int index = 0; index < triangles.size(); ++index) {
      final RTriangle4F<RSpaceClipType> t = triangles.get(index);
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
    final @Nonnull RVectorM3F<RSpaceNDCType> ndc_bounds_lower,
    final @Nonnull RVectorM3F<RSpaceNDCType> ndc_bounds_upper,
    final @Nonnull RVectorReadable3FType<RSpaceNDCType> ndc)
  {
    final float x = ndc.getXF();
    final float y = ndc.getYF();
    final float z = ndc.getZF();

    ndc_bounds_lower.x = Math.min(ndc_bounds_lower.x, x);
    ndc_bounds_lower.y = Math.min(ndc_bounds_lower.y, y);
    ndc_bounds_lower.z = Math.min(ndc_bounds_lower.z, z);

    ndc_bounds_upper.x = Math.max(ndc_bounds_upper.x, x);
    ndc_bounds_upper.y = Math.max(ndc_bounds_upper.y, y);
    ndc_bounds_upper.z = Math.max(ndc_bounds_upper.z, z);
  }

  private static void calculateWindowBounds(
    final @Nonnull AreaInclusive area,
    final @Nonnull RVectorReadable3FType<RSpaceNDCType> ndc_bounds_lower,
    final @Nonnull RVectorReadable3FType<RSpaceNDCType> ndc_bounds_upper,
    final @Nonnull RVectorM3F<RSpaceWindowType> window_bounds_lower,
    final @Nonnull RVectorM3F<RSpaceWindowType> window_bounds_upper)
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

    window_bounds_lower.x -= KRefractionRenderer.WINDOW_BOUNDS_PADDING;
    window_bounds_lower.y -= KRefractionRenderer.WINDOW_BOUNDS_PADDING;
    window_bounds_upper.x += KRefractionRenderer.WINDOW_BOUNDS_PADDING;
    window_bounds_upper.y += KRefractionRenderer.WINDOW_BOUNDS_PADDING;
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
   * @param label_cache
   *          A label cache
   * @param log
   *          A log handle
   * @return A new renderer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   * @throws RException
   *           If an error occurs during initialization
   */

  public static @Nonnull
    KRefractionRendererType
    newRenderer(
      final @Nonnull JCGLImplementation gl,
      final @Nonnull KRegionCopierType copier,
      final @Nonnull KShaderCacheType shader_cache,
      final @Nonnull KFramebufferForwardCacheType forward_cache,
      final @Nonnull KMeshBoundsCacheType<RSpaceObjectType> bounds_cache,
      final @Nonnull KMeshBoundsTrianglesCacheType<RSpaceObjectType> bounds_tri_cache,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabelCacheType label_cache,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KRefractionRenderer(
      gl,
      copier,
      shader_cache,
      forward_cache,
      bounds_cache,
      bounds_tri_cache,
      label_cache,
      log);
  }

  private static void putInstanceAttributes(
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
    final @Nonnull ArrayBuffer array,
    final @Nonnull JCBProgram program)
    throws ConstraintError,
      JCGLException,
      JCGLRuntimeException
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

    switch (label.labelGetNormal()) {
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

    if (label.labelImpliesUV()) {
      KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
    }
  }

  private static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());

    if (label.labelImpliesUV()) {
      KShadingProgramCommon.putMatrixUV(program, mwi.getMatrixUV());
    }

    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
    }
  }

  private static
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    putTextures(
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull KMaterialTranslucentRefractive material,
      final @Nonnull KFramebufferRGBAUsableType scene,
      final @Nonnull JCBProgram program,
      final @Nonnull KTextureUnitContextType context)
      throws JCGLRuntimeException,
        JCGLException,
        ConstraintError
  {
    switch (label.labelGetNormal()) {
      case NORMAL_MAPPED:
      {
        final Some<Texture2DStatic> some =
          (Some<Texture2DStatic>) material.materialGetNormal().getTexture();
        KShadingProgramCommon.putTextureNormal(
          program,
          context.withTexture2D(some.value));
        break;
      }
      case NORMAL_VERTEX:
      {
        break;
      }
      case NORMAL_NONE:
      {
        break;
      }
    }

    KShadingProgramCommon.putRefractionTextureScene(
      program,
      context.withTexture2D(scene.kFramebufferGetRGBATexture()));
  }

  private static void rendererRefractionEvaluateForInstanceMasked(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KFramebufferForwardCacheType forward_cache,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull KRegionCopierType copier,
    final @Nonnull KFramebufferForwardUsableType scene,
    final @Nonnull KFramebufferForwardUsableType temporary,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
    final @Nonnull KMutableMatricesType.MatricesInstanceType mi,
    final @Nonnull AreaInclusive window_bounds_area)
    throws RException,
      ConstraintError,
      JCacheException,
      JCGLException
  {
    final KMesh mesh = r.instanceGetMesh();

    final BLUCacheReceiptType<KFramebufferForwardDescription, KFramebufferForwardType> scene_mask =
      forward_cache.bluCacheGet(scene.kFramebufferGetForwardDescription());

    try {
      final KFramebufferForwardType mask = scene_mask.getValue();

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
        label,
        mi,
        mesh);

    } finally {
      scene_mask.returnToCache();
    }
  }

  private static void rendererRefractionEvaluateForInstanceUnmasked(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull KFramebufferForwardUsableType scene,
    final @Nonnull KFramebufferForwardUsableType temporary,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
    final @Nonnull KMutableMatricesType.MatricesInstanceType mi)
    throws JCGLRuntimeException,
      JCBExecutionException,
      RException,
      ConstraintError,
      JCacheException
  {
    KRefractionRenderer.rendererRefractionEvaluateRenderUnmasked(
      g,
      shader_cache,
      unit_allocator,
      scene,
      temporary,
      r,
      label,
      mi,
      r.instanceGetMesh());
  }

  private static void rendererRefractionEvaluateRenderMask(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull KFramebufferRGBAUsableType scene_mask,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull MatricesInstanceType mi,
    final @Nonnull KMesh mesh)
    throws ConstraintError,
      JCGLRuntimeException,
      RException,
      JCacheException,
      JCBExecutionException
  {
    final KProgram kprogram = shader_cache.cacheGetLU("debug_ccolour");

    final JCGLInterfaceCommon gc = g.getGLCommon();
    kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
      @SuppressWarnings("synthetic-access") @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          RException
      {
        try {
          gc.framebufferDrawBind(scene_mask.kFramebufferGetColorFramebuffer());

          program.programUniformPutVector4f(
            "f_ccolour",
            KRefractionRenderer.WHITE);

          gc.blendingDisable();

          final KInstanceTranslucentRefractive instance = r.getInstance();
          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          for (int index = 0; index < 10; ++index) {
            KRendererCommon.renderConfigureFaceCulling(
              gc,
              instance.instanceGetFaces());
          }

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

          final ArrayBuffer array = mesh.getArrayBuffer();
          final IndexBuffer indices = mesh.getIndexBuffer();
          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

          program.programExecute(new JCBProgramProcedure() {
            @Override public void call()
              throws ConstraintError,
                JCGLException
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

  @SuppressWarnings("synthetic-access") private static
    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    rendererRefractionEvaluateRenderMasked(
      final @Nonnull JCGLImplementation g,
      final @Nonnull KShaderCacheType shader_cache,
      final @Nonnull KTextureUnitAllocator unit_allocator,
      final @Nonnull KFramebufferRGBAUsableType scene,
      final @Nonnull KFramebufferRGBAUsableType scene_mask,
      final @Nonnull KFramebufferRGBAUsableType destination,
      final @Nonnull KInstanceTransformedTranslucentRefractive r,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull MatricesInstanceType mi,
      final @Nonnull KMesh mesh)
      throws ConstraintError,
        JCGLRuntimeException,
        RException,
        JCacheException,
        JCBExecutionException
  {
    final KInstanceTranslucentRefractive instance = r.getInstance();
    final KMaterialTranslucentRefractive material =
      instance.instanceGetMaterial();
    final KProgram kprogram = shader_cache.cacheGetLU(label.labelGetCode());

    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    final JCGLInterfaceCommon gc = g.getGLCommon();
    kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
      @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          RException
      {
        try {
          gc.framebufferDrawBind(destination
            .kFramebufferGetColorFramebuffer());

          gc.blendingDisable();

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final @Nonnull KTextureUnitContextType context)
              throws ConstraintError,
                JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjectionUnchecked(
                program,
                mi.getMatrixProjection());

              KRefractionRenderer.putInstanceMatrices(program, mi, label);

              KRefractionRenderer.putTextures(
                label,
                material,
                scene,
                program,
                context);

              KShadingProgramCommon
                .putRefractionTextureSceneMask(program, context
                  .withTexture2D(scene_mask.kFramebufferGetRGBATexture()));

              KShadingProgramCommon.putMaterialRefractive(
                program,
                material.getRefractive());

              gc.arrayBufferBind(array);
              KRefractionRenderer
                .putInstanceAttributes(label, array, program);

              program.programExecute(new JCBProgramProcedure() {
                @Override public void call()
                  throws ConstraintError,
                    JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
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

  @SuppressWarnings("synthetic-access") private static
    void
    rendererRefractionEvaluateRenderUnmasked(
      final @Nonnull JCGLImplementation g,
      final @Nonnull KShaderCacheType shader_cache,
      final @Nonnull KTextureUnitAllocator unit_allocator,
      final @Nonnull KFramebufferForwardUsableType scene,
      final @Nonnull KFramebufferForwardUsableType temporary,
      final @Nonnull KInstanceTransformedTranslucentRefractive r,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull MatricesInstanceType mi,
      final @Nonnull KMesh mesh)
      throws JCGLRuntimeException,
        JCBExecutionException,
        ConstraintError,
        RException,
        JCacheException
  {
    final KInstanceTranslucentRefractive instance = r.getInstance();
    final KMaterialTranslucentRefractive material =
      instance.instanceGetMaterial();
    final KProgram kprogram = shader_cache.cacheGetLU(label.labelGetCode());

    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    final JCGLInterfaceCommon gc = g.getGLCommon();
    kprogram.getExecutable().execRun(new JCBExecutorProcedure() {
      @Override public void call(
        final @Nonnull JCBProgram program)
        throws ConstraintError,
          JCGLException,
          RException
      {
        try {
          gc.framebufferDrawBind(temporary.kFramebufferGetColorFramebuffer());

          gc.blendingDisable();

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final @Nonnull KTextureUnitContextType context)
              throws ConstraintError,
                JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjectionUnchecked(
                program,
                mi.getMatrixProjection());

              KRefractionRenderer.putInstanceMatrices(program, mi, label);

              KRefractionRenderer.putTextures(
                label,
                material,
                scene,
                program,
                context);

              KShadingProgramCommon.putMaterialRefractive(
                program,
                material.getRefractive());

              gc.arrayBufferBind(array);
              KRefractionRenderer
                .putInstanceAttributes(label, array, program);

              program.programExecute(new JCBProgramProcedure() {
                @Override public void call()
                  throws ConstraintError,
                    JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
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

  private final @Nonnull KMeshBoundsCacheType<RSpaceObjectType>              bounds_cache;
  private final @Nonnull KMeshBoundsTrianglesCacheType<RSpaceObjectType>     bounds_tri_cache;
  private final @Nonnull RVectorM4F<RSpaceClipType>                          clip_tmp;
  private boolean                                                            closed;
  private final @Nonnull KRegionCopierType                                   copier;
  private final @Nonnull KFramebufferForwardCacheType                        forward_cache;
  private final @Nonnull JCGLImplementation                                  g;
  private final @Nonnull KMaterialForwardTranslucentRefractiveLabelCacheType label_cache;
  private final @Nonnull Log                                                 log;
  private final @Nonnull MatrixM4x4F.Context                                 matrix_context;
  private final @Nonnull RVectorM3F<RSpaceNDCType>                           ndc_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceNDCType>                           ndc_bounds_upper;
  private final @Nonnull RVectorM3F<RSpaceNDCType>                           ndc_tmp;
  private final @Nonnull KShaderCacheType                                    shader_cache;
  private final @Nonnull KTextureUnitAllocator                               texture_units;
  private final @Nonnull RVectorM3F<RSpaceWindowType>                        window_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceWindowType>                        window_bounds_upper;

  private KRefractionRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KRegionCopierType in_copier,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull KFramebufferForwardCacheType in_forward_cache,
    final @Nonnull KMeshBoundsCacheType<RSpaceObjectType> in_bounds_cache,
    final @Nonnull KMeshBoundsTrianglesCacheType<RSpaceObjectType> in_bounds_tri_cache,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabelCacheType in_label_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    try {
      this.log =
        new Log(
          Constraints.constrainNotNull(in_log, "Log"),
          "shadow-renderer");
      this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
      this.copier = Constraints.constrainNotNull(in_copier, "Copier");
      this.forward_cache =
        Constraints.constrainNotNull(
          in_forward_cache,
          "Forward framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");
      this.bounds_cache =
        Constraints.constrainNotNull(in_bounds_cache, "Bounds cache");
      this.bounds_tri_cache =
        Constraints.constrainNotNull(
          in_bounds_tri_cache,
          "Bounds triangle cache");
      this.label_cache =
        Constraints.constrainNotNull(in_label_cache, "Label cache");

      this.texture_units = KTextureUnitAllocator.newAllocator(this.g);
      this.clip_tmp = new RVectorM4F<RSpaceClipType>();
      this.ndc_tmp = new RVectorM3F<RSpaceNDCType>();
      this.window_bounds_lower = new RVectorM3F<RSpaceWindowType>();
      this.window_bounds_upper = new RVectorM3F<RSpaceWindowType>();
      this.ndc_bounds_lower = new RVectorM3F<RSpaceNDCType>();
      this.ndc_bounds_upper = new RVectorM3F<RSpaceNDCType>();
      this.matrix_context = new MatrixM4x4F.Context();

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererClose()
    throws RException,
      ConstraintError
  {
    Constraints.constrainArbitrary(
      this.closed == false,
      "Renderer is not closed");
    this.closed = true;

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public String rendererGetName()
  {
    return KRefractionRenderer.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  @Override public void rendererRefractionEvaluate(
    final @Nonnull KFramebufferForwardUsableType scene,
    final @Nonnull KMutableMatricesType.MatricesObserverType observer,
    final @Nonnull KInstanceTransformedTranslucentRefractive r)
    throws ConstraintError,
      RException
  {
    Constraints.constrainNotNull(scene, "Scene");
    Constraints.constrainNotNull(observer, "Observer");
    Constraints.constrainNotNull(r, "Refractive instance");
    Constraints.constrainArbitrary(
      this.rendererIsClosed() == false,
      "Renderer is not closed");

    try {
      final JCGLInterfaceCommon gc = this.g.getGLCommon();

      Constraints.constrainArbitrary(
        gc.framebufferDrawIsBound(scene.kFramebufferGetColorFramebuffer()),
        "Framebuffer is bound");

      observer.withInstance(
        r,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @SuppressWarnings("synthetic-access") @Override public Unit run(
            final @Nonnull MatricesInstanceType mi)
            throws ConstraintError,
              RException,
              JCGLException
          {
            try {
              KRefractionRenderer.this.rendererRefractionEvaluateForInstance(
                scene,
                r,
                mi);
            } catch (final JCacheException e) {
              throw RException.fromJCacheException(e);
            }
            return Unit.unit();
          }
        });

      Constraints.constrainArbitrary(
        gc.framebufferDrawIsBound(scene.kFramebufferGetColorFramebuffer()),
        "Framebuffer is still bound");

    } catch (final JCGLException x) {
      throw RException.fromJCGLException(x);
    }
  }

  private void rendererRefractionEvaluateForInstance(
    final @Nonnull KFramebufferForwardUsableType scene,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMutableMatricesType.MatricesInstanceType mi)
    throws ConstraintError,
      JCGLException,
      RException,
      JCacheException
  {
    final KMesh mesh = r.instanceGetMesh();

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
      final JCGLInterfaceCommon gc = this.g.getGLCommon();
      gc.blendingDisable();

      KRefractionRenderer.calculateWindowBounds(
        scene.kFramebufferGetArea(),
        this.ndc_bounds_lower,
        this.ndc_bounds_upper,
        this.window_bounds_lower,
        this.window_bounds_upper);

      final BLUCacheReceiptType<KFramebufferForwardDescription, KFramebufferForwardType> temporary =
        this.forward_cache.bluCacheGet(scene
          .kFramebufferGetForwardDescription());

      try {
        final float lo_x = this.window_bounds_lower.getXF();
        final float hi_x = this.window_bounds_upper.getXF();
        final float lo_y = this.window_bounds_lower.getYF();
        final float hi_y = this.window_bounds_upper.getYF();

        final RangeInclusive range_x =
          new RangeInclusive((long) lo_x, (long) hi_x);
        final RangeInclusive range_y =
          new RangeInclusive((long) lo_y, (long) hi_y);
        final AreaInclusive window_bounds_area =
          new AreaInclusive(range_x, range_y);

        this.copier.copierCopyRGBAWithDepth(
          scene,
          window_bounds_area,
          temporary.getValue(),
          window_bounds_area);

        final KMaterialForwardTranslucentRefractiveLabel label =
          this.label_cache.getForwardLabelTranslucentRefractive(r
            .getInstance());

        switch (label.getRefractive()) {
          case REFRACTIVE_MASKED:
          {
            KRefractionRenderer.rendererRefractionEvaluateForInstanceMasked(
              this.g,
              this.forward_cache,
              this.shader_cache,
              this.texture_units,
              this.copier,
              scene,
              temporary.getValue(),
              r,
              label,
              mi,
              window_bounds_area);

            break;
          }
          case REFRACTIVE_UNMASKED:
          {
            KRefractionRenderer
              .rendererRefractionEvaluateForInstanceUnmasked(
                this.g,
                this.shader_cache,
                this.texture_units,
                scene,
                temporary.getValue(),
                r,
                label,
                mi);
            break;
          }
        }

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
