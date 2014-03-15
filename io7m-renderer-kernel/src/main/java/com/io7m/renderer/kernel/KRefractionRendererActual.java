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
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.BLUCache;
import com.io7m.jcache.BLUCacheReceipt;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.FramebufferBlitBuffer;
import com.io7m.jcanephora.FramebufferBlitFilter;
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
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.types.KFramebufferRGBADescription;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
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
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorM3F;
import com.io7m.renderer.types.RVectorM4F;
import com.io7m.renderer.types.RVectorReadable3F;

public final class KRefractionRendererActual implements KRefractionRenderer
{
  private static final @Nonnull Set<FramebufferBlitBuffer> COLOR_BUFFER;
  private static final int                                 WINDOW_BOUNDS_PADDING;

  static {
    COLOR_BUFFER =
      EnumSet.of(FramebufferBlitBuffer.FRAMEBUFFER_BLIT_BUFFER_COLOR);

    WINDOW_BOUNDS_PADDING = 2;
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

  public static @Nonnull
    KRefractionRendererActual
    newRenderer(
      final @Nonnull JCGLImplementation gl,
      final @Nonnull LUCache<String, KProgram, RException> shader_cache,
      final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
      final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException> bounds_cache,
      final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache,
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KRefractionRendererActual(
      gl,
      shader_cache,
      rgba_cache,
      bounds_cache,
      bounds_tri_cache,
      caps,
      log);
  }

  private final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException>                              bounds_cache;
  private final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache;
  private final @Nonnull RVectorM4F<RSpaceClip>                                                             clip_tmp;
  private final @Nonnull JCGLImplementation                                                                 g;
  private final @Nonnull Log                                                                                log;
  private final @Nonnull MatrixM4x4F.Context                                                                matrix_context;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_bounds_upper;
  private final @Nonnull RVectorM3F<RSpaceNDC>                                                              ndc_tmp;
  private final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException>                rgba_cache;
  private final @Nonnull VectorM2I                                                                          scissor_pos;
  private final @Nonnull VectorM2I                                                                          scissor_size;
  private final @Nonnull LUCache<String, KProgram, RException>                                              shader_cache;
  private final @Nonnull RVectorM3F<RSpaceWindow>                                                           window_bounds_lower;
  private final @Nonnull RVectorM3F<RSpaceWindow>                                                           window_bounds_upper;

  private KRefractionRendererActual(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull BLUCache<KFramebufferRGBADescription, KFramebufferRGBA, RException> rgba_cache,
    final @Nonnull LUCache<KMesh, KMeshBounds<RSpaceObject>, RException> bounds_cache,
    final @Nonnull LUCache<KMeshBounds<RSpaceObject>, KMeshBoundsTriangles<RSpaceObject>, RException> bounds_tri_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "Log"), "shadow-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.rgba_cache =
      Constraints.constrainNotNull(rgba_cache, "RGBA framebuffer cache");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.bounds_cache =
      Constraints.constrainNotNull(bounds_cache, "Bounds cache");
    this.bounds_tri_cache =
      Constraints.constrainNotNull(bounds_tri_cache, "Bounds triangle cache");

    this.clip_tmp = new RVectorM4F<RSpaceClip>();
    this.ndc_tmp = new RVectorM3F<RSpaceNDC>();
    this.window_bounds_lower = new RVectorM3F<RSpaceWindow>();
    this.window_bounds_upper = new RVectorM3F<RSpaceWindow>();
    this.ndc_bounds_lower = new RVectorM3F<RSpaceNDC>();
    this.ndc_bounds_upper = new RVectorM3F<RSpaceNDC>();
    this.scissor_size = new VectorM2I();
    this.scissor_pos = new VectorM2I();
    this.matrix_context = new MatrixM4x4F.Context();
  }

  private @Nonnull RVectorI4F<RSpaceClip> calculateClipCoordinates(
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> mmv,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> mp,
    final @Nonnull RVectorI3F<RSpaceObject> v_obj,
    final @Nonnull RVectorM4F<RSpaceClip> v_clip)
  {
    v_clip.x = v_obj.x;
    v_clip.y = v_obj.y;
    v_clip.z = v_obj.z;
    v_clip.w = 1.0f;

    MatrixM4x4F.multiplyVector4FWithContext(
      this.matrix_context,
      mmv,
      v_clip,
      v_clip);
    MatrixM4x4F.multiplyVector4FWithContext(
      this.matrix_context,
      mp,
      v_clip,
      v_clip);

    return new RVectorI4F<RSpaceClip>(v_clip);
  }

  private
    <G extends JCGLFramebuffersGL3 & JCGLColorBuffer & JCGLScissor>
    void
    copyFramebufferRegionGL3(
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull G gc)
      throws JCGLRuntimeException,
        ConstraintError
  {
    final RangeInclusive range_x =
      new RangeInclusive(
        (long) this.window_bounds_lower.x,
        (long) this.window_bounds_upper.x);
    final RangeInclusive range_y =
      new RangeInclusive(
        (long) this.window_bounds_lower.y,
        (long) this.window_bounds_upper.y);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);

    this.scissor_pos.x = (int) this.window_bounds_lower.x;
    this.scissor_pos.y = (int) this.window_bounds_lower.y;
    this.scissor_size.x =
      (int) (this.window_bounds_upper.x - this.window_bounds_lower.x);
    this.scissor_size.y =
      (int) (this.window_bounds_upper.y - this.window_bounds_lower.y);

    try {
      gc.scissorEnable(this.scissor_pos, this.scissor_size);
      gc.colorBufferClear4f(1.0f, 0.0f, 1.0f, 1.0f);

      gc.framebufferReadBind(source.kFramebufferGetColorFramebuffer());
      try {
        gc.framebufferDrawBind(target.kFramebufferGetColorFramebuffer());
        try {
          gc.framebufferBlit(
            area,
            area,
            KRefractionRendererActual.COLOR_BUFFER,
            FramebufferBlitFilter.FRAMEBUFFER_BLIT_FILTER_NEAREST);
        } finally {
          gc.framebufferDrawUnbind();
        }
      } finally {
        gc.framebufferReadUnbind();
        gc.framebufferDrawBind(source.kFramebufferGetColorFramebuffer());
      }
    } finally {
      gc.scissorDisable();
    }
  }

  @Override public void rendererRefractionEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KMutableMatrices.MatricesObserver observer,
    final @Nonnull KInstanceTransformedTranslucentRefractive r)
    throws ConstraintError,
      RException
  {
    try {
      Constraints.constrainArbitrary(
        this.g.getGLCommon().framebufferDrawIsBound(
          framebuffer.kFramebufferGetColorFramebuffer()),
        "Framebuffer is bound");

      final KMesh mesh = r.instanceGetMesh();

      final BLUCacheReceipt<KFramebufferRGBADescription, KFramebufferRGBA> target =
        this.rgba_cache.bluCacheGet(framebuffer
          .kFramebufferGetRGBADescription());

      try {
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
                  .rendererRefractionEvaluateForInstance(
                    framebuffer,
                    target.getValue(),
                    r,
                    mi,
                    mesh);
              } catch (final JCacheException e) {
                throw RException.fromJCacheException(e);
              }
              return Unit.unit();
            }

          });
      } finally {
        target.returnToCache();
      }

      Constraints.constrainArbitrary(
        this.g.getGLCommon().framebufferDrawIsBound(
          framebuffer.kFramebufferGetColorFramebuffer()),
        "Framebuffer is still bound");

    } catch (final JCacheException x) {
      throw RException.fromJCacheException(x);
    } catch (final JCGLException x) {
      throw RException.fromJCGLException(x);
    }
  }

  @SuppressWarnings("synthetic-access") private
    void
    rendererRefractionEvaluateForInstance(
      final @Nonnull KFramebufferRGBAUsable source,
      final @Nonnull KFramebufferRGBAUsable target,
      final @Nonnull KInstanceTransformedTranslucentRefractive r,
      final @Nonnull KMutableMatrices.MatricesInstance mi,
      final @Nonnull KMesh mesh)
      throws ConstraintError,
        JCGLException,
        RException,
        JCacheException
  {
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
      RCoordinates.ndcToWindow(
        this.ndc_bounds_lower,
        this.window_bounds_lower,
        source.kFramebufferGetArea(),
        0.0f,
        1.0f);
      RCoordinates.ndcToWindow(
        this.ndc_bounds_upper,
        this.window_bounds_upper,
        source.kFramebufferGetArea(),
        0.0f,
        1.0f);

      /**
       * Due to numerical inaccuracy, the calculate bounds can sometimes be a
       * pixel or so too small onscreen. The refraction effect doesn't require
       * accurate bounds, so it's best for the bounds to be slightly too large
       * than too small.
       */

      this.window_bounds_lower.x -=
        KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
      this.window_bounds_lower.y -=
        KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
      this.window_bounds_upper.x +=
        KRefractionRendererActual.WINDOW_BOUNDS_PADDING;
      this.window_bounds_upper.y +=
        KRefractionRendererActual.WINDOW_BOUNDS_PADDING;

      final JCGLInterfaceCommon gc = this.g.getGLCommon();
      this.g
        .implementationAccept(new JCGLImplementationVisitor<Unit, RException>() {
          @Override public Unit implementationIsGL2(
            final @Nonnull JCGLInterfaceGL2 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRefractionRendererActual.this.copyFramebufferRegionGL3(
              source,
              target,
              gl);
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final @Nonnull JCGLInterfaceGL3 gl)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRefractionRendererActual.this.copyFramebufferRegionGL3(
              source,
              target,
              gl);
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
            KRefractionRendererActual.this.copyFramebufferRegionGL3(
              source,
              target,
              gl);
            return Unit.unit();
          }
        });
    }
  }
}
