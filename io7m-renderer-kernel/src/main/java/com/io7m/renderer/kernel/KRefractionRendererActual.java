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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.BLUCache;
import com.io7m.jcache.BLUCacheReceipt;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLColorBuffer;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLFramebuffersGL3;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationVisitor;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.JCGLInterfaceGLES2;
import com.io7m.jcanephora.JCGLInterfaceGLES3;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.JCGLScissor;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.types.KFramebufferForwardDescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialForwardTranslucentRefractiveLabel;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshBounds;
import com.io7m.renderer.kernel.types.KMeshBoundsTriangles;
import com.io7m.renderer.types.RCoordinates;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixReadable4x4F;
import com.io7m.renderer.types.RSpaceClip;
import com.io7m.renderer.types.RSpaceNDC;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceWindow;
import com.io7m.renderer.types.RTransformModelView;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTriangle4F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorM3F;
import com.io7m.renderer.types.RVectorM4F;
import com.io7m.renderer.types.RVectorReadable3F;

public final class KRefractionRendererActual implements KRefractionRenderer
{
  private static final @Nonnull Set<FramebufferBlitBuffer> BLIT_BUFFERS;
  private static final int                                 WINDOW_BOUNDS_PADDING;
  protected static final @Nonnull VectorReadable4F         WHITE;

  static {
    BLIT_BUFFERS =
      EnumSet.of(
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR,
        FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_DEPTH);

    WINDOW_BOUNDS_PADDING = 2;

    WHITE = new VectorI4F(1.0f, 1.0f, 1.0f, 1.0f);
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
      final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException> bounds_cache,
      final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_triangle_cache,
      final @Nonnull MatrixM4x4F.Context matrix_context,
      final @Nonnull KMutableMatrices.MatricesInstance mi,
      final @Nonnull KMesh mesh,
      final @Nonnull RVectorM3F<RSpaceNDC> ndc_bounds_lower,
      final @Nonnull RVectorM3F<RSpaceNDC> ndc_bounds_upper)
      throws ConstraintError,
        RException,
        JCacheException
  {
    final RMatrixReadable4x4F<RTransformModelView> mmv =
      mi.getMatrixModelView();
    final RMatrixReadable4x4F<RTransformProjection> mp =
      mi.getMatrixProjection();

    final KMeshBounds<RSpaceObject> bounds = bounds_cache.cacheGetLU(mesh);
    final KMeshBoundsTriangles<RSpaceObject> obj_triangles =
      bounds_triangle_cache.cacheGetLU(bounds);

    /**
     * Transform the triangles of the bounding box that contains the mesh from
     * object space to clip space.
     */

    final RVectorM4F<RSpaceClip> clip_temp = new RVectorM4F<RSpaceClip>();
    final KMeshBoundsTriangles<RSpaceClip> clip_triangles =
      obj_triangles
        .transform(new PartialFunction<RTriangle4F<RSpaceObject>, RTriangle4F<RSpaceClip>, Constraints.ConstraintError>() {
          @Override public RTriangle4F<RSpaceClip> call(
            final RTriangle4F<RSpaceObject> ot)
            throws ConstraintError
          {
            return ot
              .transform(new PartialFunction<RVectorI4F<RSpaceObject>, RVectorI4F<RSpaceClip>, Constraints.ConstraintError>() {
                @Override public RVectorI4F<RSpaceClip> call(
                  final RVectorI4F<RSpaceObject> op)
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
                  return new RVectorI4F<RSpaceClip>(clip_temp);
                }
              });
          }
        });

    /**
     * Now, clip all of the clip-space triangles produced against the six
     * homogeneous clipping planes.
     */

    final ArrayList<RTriangle4F<RSpaceClip>> triangles =
      new ArrayList<RTriangle4F<RSpaceClip>>();
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

    final RVectorM3F<RSpaceNDC> ndc_temp = new RVectorM3F<RSpaceNDC>();

    boolean visible = false;
    for (int index = 0; index < triangles.size(); ++index) {
      final RTriangle4F<RSpaceClip> t = triangles.get(index);
      final List<RTriangle4F<RSpaceClip>> r =
        KTriangleClipping.clipTrianglePlanes(t, KTriangleClipping.PLANES);

      if (r.size() > 0) {
        visible = true;

        /**
         * For each clipped triangle, accumulate the lower and upper bounds.
         */

        for (int ki = 0; ki < r.size(); ++ki) {
          final RTriangle4F<RSpaceClip> rt = r.get(ki);
          final RVectorI4F<RSpaceClip> p0 = rt.getP0();
          final RVectorI4F<RSpaceClip> p1 = rt.getP1();
          final RVectorI4F<RSpaceClip> p2 = rt.getP2();

          RCoordinates.clipToNDC(p0, ndc_temp);
          KRefractionRendererActual.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);

          RCoordinates.clipToNDC(p1, ndc_temp);
          KRefractionRendererActual.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);

          RCoordinates.clipToNDC(p2, ndc_temp);
          KRefractionRendererActual.calculateNDCBoundsAccumulate(
            ndc_bounds_lower,
            ndc_bounds_upper,
            ndc_temp);
        }
      }
    }

    return visible;
  }

  private static void calculateNDCBoundsAccumulate(
    final @Nonnull RVectorM3F<RSpaceNDC> ndc_bounds_lower,
    final @Nonnull RVectorM3F<RSpaceNDC> ndc_bounds_upper,
    final @Nonnull RVectorReadable3F<RSpaceNDC> ndc)
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
    final @Nonnull RVectorReadable3F<RSpaceNDC> ndc_bounds_lower,
    final @Nonnull RVectorReadable3F<RSpaceNDC> ndc_bounds_upper,
    final @Nonnull RVectorM3F<RSpaceWindow> window_bounds_lower,
    final @Nonnull RVectorM3F<RSpaceWindow> window_bounds_upper)
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

    window_bounds_lower.x -= KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
    window_bounds_lower.y -= KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
    window_bounds_upper.x += KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
    window_bounds_upper.y += KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
  }

  private static
    <G extends JCGLFramebuffersGL3 & JCGLColorBuffer & JCGLScissor>
    void
    copyFramebufferRegionGL3(
      final @Nonnull G gc,
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_lower,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_upper)
      throws JCGLRuntimeException,
        ConstraintError
  {
    final float lo_x = window_bounds_lower.getXF();
    final float hi_x = window_bounds_upper.getXF();
    final float lo_y = window_bounds_lower.getYF();
    final float hi_y = window_bounds_upper.getYF();

    final RangeInclusive range_x =
      new RangeInclusive((long) lo_x, (long) hi_x);
    final RangeInclusive range_y =
      new RangeInclusive((long) lo_y, (long) hi_y);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);

    gc.framebufferReadBind(source.kFramebufferGetColorFramebuffer());
    try {
      gc.framebufferDrawBind(target.kFramebufferGetColorFramebuffer());
      try {
        gc.framebufferBlit(
          area,
          area,
          KRefractionRendererActual.BLIT_BUFFERS,
          FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      gc.framebufferReadUnbind();
    }
  }

  private static void makeScissorRegion(
    final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_lower,
    final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_upper,
    final @Nonnull VectorM2I position,
    final @Nonnull VectorM2I size)
  {
    final float lo_x = window_bounds_lower.getXF();
    final float hi_x = window_bounds_upper.getXF();
    final float lo_y = window_bounds_lower.getYF();
    final float hi_y = window_bounds_upper.getYF();

    position.x = (int) lo_x;
    position.y = (int) lo_y;
    size.x = (int) (hi_x - lo_x);
    size.y = (int) (hi_y - lo_y);
  }

  public static @Nonnull
    KRefractionRendererActual
    newRenderer(
      final @Nonnull JCGLImplementation gl,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull BLUCache<KFramebufferForwardDescription, KFramebufferForwardType, RException> forward_cache,
      final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException> bounds_cache,
      final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabelCache label_cache,
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull Log log)
      throws ConstraintError,
        RException
  {
    return new KRefractionRendererActual(
      gl,
      shader_cache,
      forward_cache,
      bounds_cache,
      bounds_tri_cache,
      label_cache,
      caps,
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
    KShadingProgramCommon.bindAttributePosition(program, array);

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
      KShadingProgramCommon.bindAttributeUV(program, array);
    }
  }

  private static void putInstanceMatrices(
    final @Nonnull JCBProgram program,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMatrixModelView(
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
    <F extends KFramebufferRGBAUsable & KFramebufferDepthUsable>
    void
    putTextures(
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull KMaterialTranslucentRefractive material,
      final @Nonnull KFramebufferRGBAUsable scene,
      final @Nonnull JCBProgram program,
      final @Nonnull KTextureUnitContext context)
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

  @SuppressWarnings("synthetic-access") private static
    void
    rendererRefractionEvaluateCopyFramebufferRegion(
      final @Nonnull JCGLImplementation g,
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_lower,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_upper)
      throws JCGLException,
        ConstraintError,
        RException
  {
    g.implementationAccept(new JCGLImplementationVisitor<Unit, RException>() {
      @Override public Unit implementationIsGL2(
        final @Nonnull JCGLInterfaceGL2 gl)
        throws JCGLException,
          ConstraintError,
          RException
      {
        KRefractionRendererActual.copyFramebufferRegionGL3(
          gl,
          source,
          target,
          window_bounds_lower,
          window_bounds_upper);
        return Unit.unit();
      }

      @Override public Unit implementationIsGL3(
        final @Nonnull JCGLInterfaceGL3 gl)
        throws JCGLException,
          ConstraintError,
          RException
      {
        KRefractionRendererActual.copyFramebufferRegionGL3(
          gl,
          source,
          target,
          window_bounds_lower,
          window_bounds_upper);
        return Unit.unit();
      }

      @Override public Unit implementationIsGLES2(
        final @Nonnull JCGLInterfaceGLES2 gl)
        throws JCGLException,
          ConstraintError,
          RException
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public Unit implementationIsGLES3(
        final @Nonnull JCGLInterfaceGLES3 gl)
        throws JCGLException,
          ConstraintError,
          RException
      {
        KRefractionRendererActual.copyFramebufferRegionGL3(
          gl,
          source,
          target,
          window_bounds_lower,
          window_bounds_upper);
        return Unit.unit();
      }
    });
  }

  @SuppressWarnings("synthetic-access") private static
    <F extends KFramebufferRGBAUsable & KFramebufferDepthUsable>
    void
    rendererRefractionEvaluateRenderMasked(
      final @Nonnull JCGLImplementation g,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull KTextureUnitAllocator unit_allocator,
      final @Nonnull KFramebufferRGBAUsable scene,
      final @Nonnull KFramebufferRGBAUsable scene_mask,
      final @Nonnull KFramebufferRGBAUsable destination,
      final @Nonnull KInstanceTransformedTranslucentRefractive r,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull MatricesInstance mi,
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

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWith() {
            @Override public void run(
              final @Nonnull KTextureUnitContext context)
              throws ConstraintError,
                JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mi.getMatrixProjection());

              KRefractionRendererActual.putInstanceMatrices(
                program,
                mi,
                label);

              KRefractionRendererActual.putTextures(
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
              KRefractionRendererActual.putInstanceAttributes(
                label,
                array,
                program);

              program.programExecute(new JCBProgramProcedure() {
                @Override public void call()
                  throws ConstraintError,
                    JCGLException,
                    Throwable
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

  private static void rendererRefractionEvaluateRenderMask(
    final @Nonnull JCGLImplementation g,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KFramebufferRGBAUsable scene_mask,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull MatricesInstance mi,
    final @Nonnull KMesh mesh)
    throws ConstraintError,
      JCGLRuntimeException,
      RException,
      JCacheException,
      JCBExecutionException
  {
    final KInstanceTranslucentRefractive instance = r.getInstance();
    final KProgram kprogram = shader_cache.cacheGetLU("debug_ccolour");

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
          gc.framebufferDrawBind(scene_mask.kFramebufferGetColorFramebuffer());

          program.programUniformPutVector4f(
            "f_ccolour",
            KRefractionRendererActual.WHITE);

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          gc.colorBufferMask(true, true, true, true);
          gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 1.0f);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          KShadingProgramCommon.putMatrixProjection(
            program,
            mi.getMatrixProjection());

          KShadingProgramCommon.putMatrixModelView(
            program,
            mi.getMatrixModelView());

          gc.arrayBufferBind(array);
          KShadingProgramCommon.bindAttributePosition(program, array);

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
          gc.framebufferDrawUnbind();
        }
      }
    });
  }

  private final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException>                              bounds_cache;
  private final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache;
  private final @Nonnull RVectorM4F<RSpaceClip>                                                             clip_tmp;
  private final @Nonnull BLUCache<KFramebufferForwardDescription, KFramebufferForwardType, RException>      forward_cache;
  private final @Nonnull JCGLImplementation                                                                 g;
  private final @Nonnull KMaterialForwardTranslucentRefractiveLabelCache                                    label_cache;
  private final @Nonnull Log                                                                                log;
  private final @Nonnull MatrixM4x4F.Context                                                                matrix_context;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_bounds_upper;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_tmp;
  private final @Nonnull LUCache<String, KProgram, RException>                                              shader_cache;
  private final @Nonnull KTextureUnitAllocator                                                              texture_units;
  private final @Nonnull RVectorM3F<RSpaceWindow>                                                           window_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceWindow>                                                           window_bounds_upper;

  private KRefractionRendererActual(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull BLUCache<KFramebufferForwardDescription, KFramebufferForwardType, RException> forward_cache,
    final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException> bounds_cache,
    final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabelCache label_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError,
      RException
  {
    try {
      this.log =
        new Log(Constraints.constrainNotNull(log, "Log"), "shadow-renderer");
      this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
      this.forward_cache =
        Constraints.constrainNotNull(
          forward_cache,
          "Forward framebuffer cache");
      this.shader_cache =
        Constraints.constrainNotNull(shader_cache, "Shader cache");
      this.bounds_cache =
        Constraints.constrainNotNull(bounds_cache, "Bounds cache");
      this.bounds_tri_cache =
        Constraints.constrainNotNull(
          bounds_tri_cache,
          "Bounds triangle cache");
      this.label_cache =
        Constraints.constrainNotNull(label_cache, "Label cache");

      this.texture_units = KTextureUnitAllocator.newAllocator(this.g);
      this.clip_tmp = new RVectorM4F<RSpaceClip>();
      this.ndc_tmp = new RVectorM3F<RSpaceNDC>();
      this.window_bounds_lower = new RVectorM3F<RSpaceWindow>();
      this.window_bounds_upper = new RVectorM3F<RSpaceWindow>();
      this.ndc_bounds_lower = new RVectorM3F<RSpaceNDC>();
      this.ndc_bounds_upper = new RVectorM3F<RSpaceNDC>();
      this.matrix_context = new MatrixM4x4F.Context();

    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererRefractionEvaluate(
    final @Nonnull KFramebufferForwardUsable scene,
    final @Nonnull KMutableMatrices.MatricesObserver observer,
    final @Nonnull KInstanceTransformedTranslucentRefractive r)
    throws ConstraintError,
      RException
  {
    try {
      final JCGLInterfaceCommon gc = this.g.getGLCommon();

      Constraints.constrainArbitrary(
        gc.framebufferDrawIsBound(scene.kFramebufferGetColorFramebuffer()),
        "Framebuffer is bound");

      observer.withInstance(
        r,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @SuppressWarnings("synthetic-access") @Override public Unit run(
            final @Nonnull MatricesInstance mi)
            throws ConstraintError,
              RException,
              JCGLException
          {
            try {
              KRefractionRendererActual.this
                .rendererRefractionEvaluateForInstance(scene, r, mi);
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
    final @Nonnull KFramebufferForwardUsable scene,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMutableMatrices.MatricesInstance mi)
    throws ConstraintError,
      JCGLException,
      RException,
      JCacheException
  {
    final KMesh mesh = r.instanceGetMesh();

    final boolean visible =
      KRefractionRendererActual.calculateNDCBounds(
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

      KRefractionRendererActual.calculateWindowBounds(
        scene.kFramebufferGetArea(),
        this.ndc_bounds_lower,
        this.ndc_bounds_upper,
        this.window_bounds_lower,
        this.window_bounds_upper);

      final BLUCacheReceipt<KFramebufferForwardDescription, KFramebufferForwardType> temporary =
        this.forward_cache.bluCacheGet(scene
          .kFramebufferGetForwardDescription());

      try {
        KRefractionRendererActual
          .rendererRefractionEvaluateCopyFramebufferRegion(
            this.g,
            scene,
            temporary.getValue(),
            this.window_bounds_lower,
            this.window_bounds_upper);

        final KMaterialForwardTranslucentRefractiveLabel label =
          this.label_cache.getForwardLabelTranslucentRefractive(r
            .getInstance());

        switch (label.getRefractive()) {
          case REFRACTIVE_MASKED:
          {
            KRefractionRendererActual
              .rendererRefractionEvaluateForInstanceMasked(
                this.g,
                this.forward_cache,
                this.shader_cache,
                this.texture_units,
                scene,
                temporary.getValue(),
                r,
                label,
                mi,
                this.window_bounds_lower,
                this.window_bounds_lower);

            break;
          }
          case REFRACTIVE_UNMASKED:
          {
            KRefractionRendererActual
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

        KRefractionRendererActual
          .rendererRefractionEvaluateCopyFramebufferRegion(
            this.g,
            temporary.getValue(),
            scene,
            this.window_bounds_lower,
            this.window_bounds_upper);

      } finally {
        temporary.returnToCache();
      }

      gc.framebufferDrawBind(scene.kFramebufferGetColorFramebuffer());
    }
  }

  private static void rendererRefractionEvaluateForInstanceUnmasked(
    final @Nonnull JCGLImplementation g,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull KFramebufferForwardUsable scene,
    final @Nonnull KFramebufferForwardUsable temporary,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
    final @Nonnull KMutableMatrices.MatricesInstance mi)
    throws JCGLRuntimeException,
      JCBExecutionException,
      RException,
      ConstraintError,
      JCacheException
  {
    KRefractionRendererActual.rendererRefractionEvaluateRenderUnmasked(
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

  private static void rendererRefractionEvaluateRenderUnmasked(
    final @Nonnull JCGLImplementation g,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KTextureUnitAllocator unit_allocator,
    final @Nonnull KFramebufferForwardUsable scene,
    final @Nonnull KFramebufferForwardUsable temporary,
    final @Nonnull KInstanceTransformedTranslucentRefractive r,
    final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
    final @Nonnull MatricesInstance mi,
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

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaces());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWith() {
            @Override public void run(
              final @Nonnull KTextureUnitContext context)
              throws ConstraintError,
                JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjection(
                program,
                mi.getMatrixProjection());

              KRefractionRendererActual.putInstanceMatrices(
                program,
                mi,
                label);

              KRefractionRendererActual.putTextures(
                label,
                material,
                scene,
                program,
                context);

              KShadingProgramCommon.putMaterialRefractive(
                program,
                material.getRefractive());

              gc.arrayBufferBind(array);
              KRefractionRendererActual.putInstanceAttributes(
                label,
                array,
                program);

              program.programExecute(new JCBProgramProcedure() {
                @Override public void call()
                  throws ConstraintError,
                    JCGLException,
                    Throwable
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

  private static
    void
    rendererRefractionEvaluateForInstanceMasked(
      final @Nonnull JCGLImplementation g,
      final @Nonnull BLUCache<KFramebufferForwardDescription, KFramebufferForwardType, RException> forward_cache,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull KTextureUnitAllocator unit_allocator,
      final @Nonnull KFramebufferForwardUsable scene,
      final @Nonnull KFramebufferForwardUsable temporary,
      final @Nonnull KInstanceTransformedTranslucentRefractive r,
      final @Nonnull KMaterialForwardTranslucentRefractiveLabel label,
      final @Nonnull KMutableMatrices.MatricesInstance mi,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_lower,
      final @Nonnull RVectorReadable3F<RSpaceWindow> window_bounds_upper)
      throws RException,
        ConstraintError,
        JCacheException,
        JCGLException
  {
    final KMesh mesh = r.instanceGetMesh();

    final BLUCacheReceipt<KFramebufferForwardDescription, KFramebufferForwardType> scene_mask =
      forward_cache.bluCacheGet(scene.kFramebufferGetForwardDescription());

    try {
      KRefractionRendererActual
        .rendererRefractionEvaluateCopyFramebufferRegion(
          g,
          scene,
          scene_mask.getValue(),
          window_bounds_lower,
          window_bounds_upper);

      KRefractionRendererActual.rendererRefractionEvaluateRenderMask(
        g,
        shader_cache,
        scene_mask.getValue(),
        r,
        mi,
        mesh);

      KRefractionRendererActual.rendererRefractionEvaluateRenderMasked(
        g,
        shader_cache,
        unit_allocator,
        scene,
        scene_mask.getValue(),
        temporary,
        r,
        label,
        mi,
        mesh);

    } finally {
      scene_mask.returnToCache();
    }
  }
}
