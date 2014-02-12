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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
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
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstance;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaque;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitor;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformView;

final class KDepthRenderer
{
  private static enum InternalDepthLabel
  {
    DEPTH_INTERNAL_CONSTANT("depth_C"),
    DEPTH_INTERNAL_CONSTANT_PACKED4444("depth_CP4"),
    DEPTH_INTERNAL_MAPPED("depth_M"),
    DEPTH_INTERNAL_MAPPED_PACKED4444("depth_MP4"),
    DEPTH_INTERNAL_UNIFORM("depth_U"),
    DEPTH_INTERNAL_UNIFORM_PACKED4444("depth_UP4")

    ;

    static @Nonnull InternalDepthLabel fromDepthLabel(
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull KMaterialDepthLabel label)
    {
      switch (label) {
        case DEPTH_CONSTANT:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_CONSTANT;
          }
          return DEPTH_INTERNAL_CONSTANT_PACKED4444;
        }
        case DEPTH_MAPPED:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_MAPPED;
          }
          return DEPTH_INTERNAL_MAPPED_PACKED4444;
        }
        case DEPTH_UNIFORM:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_UNIFORM;
          }
          return DEPTH_INTERNAL_UNIFORM_PACKED4444;
        }
      }

      throw new UnreachableCodeException();
    }

    private final @Nonnull String name;

    private InternalDepthLabel(
      final @Nonnull String name)
    {
      this.name = name;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }
  }

  public static @Nonnull KDepthRenderer newDepthRenderer(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KDepthRenderer(gi, shader_cache, caps, log);
  }

  protected static void renderDepthPassBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mwo,
    final @Nonnull JCBProgram jp,
    final @Nonnull InternalDepthLabel label,
    final @Nonnull List<KInstanceTransformedOpaque> batch)
    throws ConstraintError,
      JCGLException,
      RException
  {
    for (final KInstanceTransformedOpaque i : batch) {
      mwo.withInstance(
        i,
        new MatricesInstanceFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstance mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KDepthRenderer.renderDepthPassInstance(gc, mwi, jp, label, i);
            return Unit.unit();
          }
        });
    }
  }

  protected static void renderDepthPassInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesInstance mwi,
    final @Nonnull JCBProgram jp,
    final @Nonnull InternalDepthLabel label,
    final @Nonnull KInstanceTransformedOpaque i)
    throws JCGLException,
      ConstraintError,
      RException
  {
    final KMaterialOpaque material = i.getInstance().instanceGetMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelView(jp, mwi.getMatrixModelView());

    switch (label) {
      case DEPTH_INTERNAL_CONSTANT:
      case DEPTH_INTERNAL_CONSTANT_PACKED4444:
      {
        break;
      }
      case DEPTH_INTERNAL_MAPPED:
      case DEPTH_INTERNAL_MAPPED_PACKED4444:
      {
        KShadingProgramCommon.putMaterialAlbedo(
          jp,
          material.materialGetAlbedo());
        KDepthRenderer.putMaterialAlphaDepth(jp, material);
        KShadingProgramCommon.putMatrixUV(jp, mwi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          jp,
          gc,
          material.materialGetAlbedo(),
          units.get(0));
        break;
      }
      case DEPTH_INTERNAL_UNIFORM:
      case DEPTH_INTERNAL_UNIFORM_PACKED4444:
      {
        KShadingProgramCommon.putMaterialAlbedo(
          jp,
          material.materialGetAlbedo());
        KDepthRenderer.putMaterialAlphaDepth(jp, material);
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KInstance actual = i.getInstance();
      final KMesh mesh = actual.instanceGetMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(jp, array);

      switch (label) {
        case DEPTH_INTERNAL_CONSTANT:
        case DEPTH_INTERNAL_CONSTANT_PACKED4444:
        case DEPTH_INTERNAL_UNIFORM:
        case DEPTH_INTERNAL_UNIFORM_PACKED4444:
        {
          break;
        }
        case DEPTH_INTERNAL_MAPPED:
        case DEPTH_INTERNAL_MAPPED_PACKED4444:
        {
          KShadingProgramCommon.bindAttributeUV(jp, array);
          break;
        }
      }

      jp.programExecute(new JCBProgramProcedure() {
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

  private static void putMaterialAlphaDepth(
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialOpaque material)
    throws RException,
      ConstraintError,
      JCGLException
  {
    material
      .materialOpaqueVisitableAccept(new KMaterialOpaqueVisitor<Unit, JCGLException>() {
        @Override public Unit materialVisitOpaqueAlphaDepth(
          final @Nonnull KMaterialOpaqueAlphaDepth m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(
            jp,
            m.getAlphaThreshold());
          return Unit.unit();
        }

        @Override public Unit materialVisitOpaqueRegular(
          final @Nonnull KMaterialOpaqueRegular m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(jp, 0.0f);
          return Unit.unit();
        }
      });
  }

  private final @Nonnull KGraphicsCapabilities                 caps;
  private final @Nonnull JCGLImplementation                    g;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull VectorM2I                             viewport_size;

  private KDepthRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log =
      new Log(Constraints.constrainNotNull(log, "log"), "depth-renderer");
    this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
    this.caps = Constraints.constrainNotNull(caps, "Capabilities");
    this.matrices = KMutableMatrices.newMatrices();
    this.viewport_size = new VectorM2I();
  }

  public
    void
    depthRendererEvaluate(
      final @Nonnull RMatrixI4x4F<RTransformView> view,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> batches,
      final @Nonnull KFramebufferDepthUsable framebuffer,
      final @Nonnull FaceSelection faces,
      final @Nonnull FaceWindingOrder order)
      throws ConstraintError,
        RException
  {
    try {
      this.matrices.withObserver(
        view,
        projection,
        new MatricesObserverFunction<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserver mwo)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KDepthRenderer.this.renderScene(
              batches,
              framebuffer,
              mwo,
              faces,
              order);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private void renderConfigureDepthColorMasks(
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    if (this.caps.getSupportsDepthTextures()) {
      gc.colorBufferMask(false, false, false, false);
    } else {
      gc.colorBufferMask(true, true, true, true);
    }
  }

  private
    void
    renderDepthPassBatches(
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> batches,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull MatricesObserver mwo)
      throws ConstraintError,
        JCacheException,
        JCGLException,
        RException
  {
    for (final KMaterialDepthLabel depth : batches.keySet()) {
      final InternalDepthLabel label =
        InternalDepthLabel.fromDepthLabel(this.caps, depth);

      final List<KInstanceTransformedOpaque> batch = batches.get(depth);
      final KProgram program = this.shader_cache.cacheGetLU(label.getName());
      final JCBExecutionAPI exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram jp)
          throws ConstraintError,
            JCGLRuntimeException,
            JCBExecutionException,
            Throwable
        {
          KShadingProgramCommon.putMatrixProjection(
            jp,
            mwo.getMatrixProjection());
          KDepthRenderer.renderDepthPassBatch(gc, mwo, jp, label, batch);
        }
      });
    }
  }

  protected
    void
    renderScene(
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> batches,
      final @Nonnull KFramebufferDepthUsable framebuffer,
      final @Nonnull MatricesObserver mwo,
      final @Nonnull FaceSelection faces,
      final @Nonnull FaceWindingOrder order)
      throws JCGLException,
        ConstraintError,
        RException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final FramebufferReferenceUsable fb =
      framebuffer.kFramebufferGetDepthPassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      KDepthRenderer.this.viewport_size.x =
        (int) area.getRangeX().getInterval();
      KDepthRenderer.this.viewport_size.y =
        (int) area.getRangeY().getInterval();
      gc.viewportSet(VectorI2I.ZERO, KDepthRenderer.this.viewport_size);

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);
      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
      gc.blendingDisable();
      this.renderConfigureDepthColorMasks(gc);

      gc.cullingEnable(faces, order);
      this.renderDepthPassBatches(batches, gc, mwo);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
