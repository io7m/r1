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
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcache.LUCache;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererDebug;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstanceOpaque;
import com.io7m.renderer.kernel.types.KInstanceTransformed;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitor;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaque;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitor;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

final class KRendererDebugDepth extends KAbstractRendererDebug
{
  private static final @Nonnull String NAME = "debug-depth";

  public static KRendererDebugDepth rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
  {
    return new KRendererDebugDepth(g, depth_labels, shader_cache, log);
  }

  private final @Nonnull VectorM4F                             background;
  private final @Nonnull KMaterialDepthLabelCache              depth_labels;
  private final @Nonnull JCGLImplementation                    gl;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull KTransformContext                     transform_context;

  private KRendererDebugDepth(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
  {
    super(KRendererDebugDepth.NAME);

    this.log = new Log(log, KRendererDebugDepth.NAME);
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = KTransformContext.newContext();
    this.depth_labels = depth_labels;
    this.shader_cache = shader_cache;

    this.log.debug("initialized");
  }

  @Override public void rendererClose()
    throws ConstraintError
  {
    // Nothing
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene)
    throws ConstraintError,
      RException
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();
    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunction<Unit, JCGLException>() {
          @SuppressWarnings("synthetic-access") @Override public Unit run(
            final MatricesObserver mo)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererDebugDepth.this.renderScene(framebuffer, scene, gc, mo);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @SuppressWarnings({ "static-method", "synthetic-access" }) private
    void
    renderInstance(
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull JCBProgram p,
      final @Nonnull MatricesInstance mi,
      final @Nonnull KInstanceTransformedOpaque instance,
      final @Nonnull KMaterialDepthLabel label)
      throws ConstraintError,
        JCGLException,
        RException
  {
    final KMaterialOpaque material =
      instance.instanceGet().instanceGetMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mi.getMatrixModelView());

    material
      .materialOpaqueVisitableAccept(new KMaterialOpaqueVisitor<Unit, JCGLException>() {
        @Override public Unit materialVisitOpaqueAlphaDepth(
          final @Nonnull KMaterialOpaqueAlphaDepth m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          switch (label) {
            case DEPTH_CONSTANT:
            {
              break;
            }
            case DEPTH_MAPPED:
            {
              KRendererDebugDepth.putMaterialOpaque(p, m);
              KShadingProgramCommon.putMatrixUV(p, mi.getMatrixUV());
              KShadingProgramCommon.bindPutTextureAlbedo(
                p,
                gc,
                material.materialGetAlbedo(),
                units.get(0));
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                p,
                m.getAlphaThreshold());
              break;
            }
            case DEPTH_UNIFORM:
            {
              KRendererDebugDepth.putMaterialOpaque(p, m);
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                p,
                m.getAlphaThreshold());
              break;
            }
          }

          return Unit.unit();
        }

        @Override public Unit materialVisitOpaqueRegular(
          final @Nonnull KMaterialOpaqueRegular m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          switch (label) {
            case DEPTH_CONSTANT:
            {
              break;
            }
            case DEPTH_MAPPED:
            {
              KRendererDebugDepth.putMaterialOpaque(p, m);
              KShadingProgramCommon.putMatrixUV(p, mi.getMatrixUV());
              KShadingProgramCommon.bindPutTextureAlbedo(
                p,
                gc,
                material.materialGetAlbedo(),
                units.get(0));
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(p, 0.0f);
              break;
            }
            case DEPTH_UNIFORM:
            {
              KRendererDebugDepth.putMaterialOpaque(p, m);
              KShadingProgramCommon.putMaterialAlphaDepthThreshold(p, 0.0f);
              break;
            }
          }

          return Unit.unit();
        }
      });

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.instanceGetMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(p, array);

      switch (label) {
        case DEPTH_CONSTANT:
        case DEPTH_UNIFORM:
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
            JCGLException,
            Exception
        {
          try {
            gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
          } catch (final ConstraintError x) {
            throw new UnreachableCodeException(x);
          }
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private static void putMaterialOpaque(
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialOpaque material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlbedo(jp, material.materialGetAlbedo());
  }

  private void renderInstancePre(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mo,
    final @Nonnull KInstanceTransformedOpaque instance,
    final @Nonnull MatricesInstance mi)
    throws ConstraintError,
      JCGLException,
      RException
  {
    try {
      final KInstanceOpaque ii = instance.instanceGet();

      final KMaterialDepthLabel label =
        KRendererDebugDepth.this.depth_labels.getDepthLabel(ii);

      final KProgram kp =
        KRendererDebugDepth.this.shader_cache.cacheGetLU(String.format(
          "depth_%s",
          label.labelGetCode()));

      final JCBExecutionAPI e = kp.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          KShadingProgramCommon.putMatrixProjection(
            p,
            mo.getMatrixProjection());

          KRendererDebugDepth.this.renderInstance(gc, p, mi, instance, label);
        }
      });

    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private void renderScene(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mo)
    throws ConstraintError,
      JCGLException,
      RException
  {
    final FramebufferReferenceUsable output_buffer =
      framebuffer.kFramebufferGetColorFramebuffer();

    gc.framebufferDrawBind(output_buffer);
    try {
      gc.viewportSet(framebuffer.kFramebufferGetArea());

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(KRendererDebugDepth.this.background);
      gc.blendingDisable();

      final Set<KInstanceTransformed> instances = scene.getVisibleInstances();

      for (final KInstanceTransformed instance : instances) {
        instance
          .transformedVisitableAccept(new KInstanceTransformedVisitor<Unit, JCGLException>() {
            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular i)
              throws JCGLException,
                RException,
                ConstraintError
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunction<Unit, JCGLException>() {
                  @SuppressWarnings("synthetic-access") @Override public
                    Unit
                    run(
                      final @Nonnull MatricesInstance mi)
                      throws JCGLException,
                        ConstraintError,
                        RException
                  {
                    KRendererDebugDepth.this.renderInstancePre(gc, mo, i, mi);
                    return Unit.unit();
                  }
                });
            }

            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth i)
              throws JCGLException,
                RException,
                ConstraintError
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunction<Unit, JCGLException>() {
                  @SuppressWarnings("synthetic-access") @Override public
                    Unit
                    run(
                      final @Nonnull MatricesInstance mi)
                      throws JCGLException,
                        ConstraintError,
                        RException
                  {
                    KRendererDebugDepth.this.renderInstancePre(gc, mo, i, mi);
                    return Unit.unit();
                  }
                });
            }

            @Override public Unit transformedVisitTranslucentRefractive(
              final @Nonnull KInstanceTransformedTranslucentRefractive i)
              throws JCGLException
            {
              return Unit.unit();
            }

            @Override public Unit transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular i)
              throws JCGLException
            {
              return Unit.unit();
            }
          });
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
