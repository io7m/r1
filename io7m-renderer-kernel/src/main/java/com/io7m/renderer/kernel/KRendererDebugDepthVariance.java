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
import java.util.Set;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitorType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

/**
 * A debug renderer that displays the calculate depth-variance values for all
 * meshes.
 */

public final class KRendererDebugDepthVariance implements KRendererDebugType
{
  private static final @Nonnull String NAME = "debug-depth-variance";

  private static void putMaterialOpaque(
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialOpaqueType material)
    throws JCGLException,
      ConstraintError
  {
    KShadingProgramCommon.putMaterialAlbedo(jp, material.materialGetAlbedo());
  }

  /**
   * Construct a new renderer.
   * 
   * @param g
   *          The OpenGL implementation
   * @param depth_labels
   *          The depth label decider
   * @param shader_cache
   *          A shader cache
   * @param log
   *          A log handle
   * @return A new renderer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static KRendererDebugDepthVariance rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KMaterialDepthLabelCacheType depth_labels,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    return new KRendererDebugDepthVariance(g, depth_labels, shader_cache, log);
  }

  private boolean                                     closed;
  private final @Nonnull KMaterialDepthLabelCacheType depth_labels;
  private final @Nonnull JCGLImplementation           gl;
  private final @Nonnull Log                          log;
  private final @Nonnull KMutableMatricesType         matrices;
  private final @Nonnull KShaderCacheType             shader_cache;
  private final @Nonnull KTransformContext            transform_context;

  private KRendererDebugDepthVariance(
    final @Nonnull JCGLImplementation in_gl,
    final @Nonnull KMaterialDepthLabelCacheType in_depth_labels,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log"),
        KRendererDebugDepthVariance.NAME);
    this.gl = Constraints.constrainNotNull(in_gl, "GL");
    this.depth_labels =
      Constraints.constrainNotNull(in_depth_labels, "Depth labels");
    this.shader_cache =
      Constraints.constrainNotNull(in_shader_cache, "Shader cache");

    this.matrices = KMutableMatricesType.newMatrices();
    this.transform_context = KTransformContext.newContext();
    this.log.debug("initialized");
  }

  @Override public void rendererClose()
    throws ConstraintError,
      RException
  {
    Constraints.constrainArbitrary(
      this.closed == false,
      "Renderer is not closed");
    this.closed = true;

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public void rendererDebugEvaluate(
    final @Nonnull KFramebufferRGBAUsableType framebuffer,
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
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @SuppressWarnings("synthetic-access") @Override public Unit run(
            final MatricesObserverType mo)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KRendererDebugDepthVariance.this.renderScene(
              framebuffer,
              scene,
              gc,
              mo);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugDepthVariance.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  @SuppressWarnings("static-method") private void renderInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull MatricesInstanceType mi,
    final @Nonnull KInstanceTransformedOpaqueType instance,
    final @Nonnull KMaterialDepthLabel label)
    throws ConstraintError,
      JCGLException
  {
    final KMaterialOpaqueType material =
      instance.instanceGet().instanceGetMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      p,
      mi.getMatrixModelView());

    switch (label) {
      case DEPTH_CONSTANT:
      {
        break;
      }
      case DEPTH_UNIFORM:
      {
        KRendererDebugDepthVariance.putMaterialOpaque(p, material);
        break;
      }
      case DEPTH_MAPPED:
      {
        KRendererDebugDepthVariance.putMaterialOpaque(p, material);
        KShadingProgramCommon.putMatrixUVUnchecked(p, mi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          p,
          gc,
          material.materialGetAlbedo(),
          units.get(0));
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = instance.instanceGet().instanceGetMesh();
      final ArrayBufferUsable array = mesh.getArrayBuffer();
      final IndexBufferUsable indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(p, array);

      switch (label) {
        case DEPTH_CONSTANT:
        case DEPTH_UNIFORM:
        {
          break;
        }
        case DEPTH_MAPPED:
        {
          KShadingProgramCommon.bindAttributeUVUnchecked(p, array);
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

  private void renderInstancePre(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserverType mo,
    final @Nonnull KInstanceTransformedOpaqueType instance,
    final @Nonnull MatricesInstanceType mi)
    throws ConstraintError,
      JCGLException,
      RException
  {
    try {
      final KInstanceOpaqueType ii = instance.instanceGet();

      final KMaterialDepthLabel label =
        KRendererDebugDepthVariance.this.depth_labels.getDepthLabel(ii);
      final String program_name =
        String.format("depth_variance_%s", label.labelGetCode());
      final KProgram kp =
        KRendererDebugDepthVariance.this.shader_cache
          .cacheGetLU(program_name);

      final JCBExecutionAPI e = kp.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @SuppressWarnings("synthetic-access") @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            p,
            mo.getMatrixProjection());

          KRendererDebugDepthVariance.this.renderInstance(
            gc,
            p,
            mi,
            instance,
            label);
        }
      });

    } catch (final JCacheException x) {
      throw new UnreachableCodeException(x);
    }
  }

  private void renderScene(
    final @Nonnull KFramebufferRGBAUsableType framebuffer,
    final @Nonnull KScene scene,
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserverType mo)
    throws ConstraintError,
      JCGLException,
      RException
  {
    final FramebufferReferenceUsable output_buffer =
      framebuffer.kFramebufferGetColorFramebuffer();

    gc.framebufferDrawBind(output_buffer);
    try {
      gc.blendingDisable();

      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 0.0f);

      gc.cullingDisable();

      gc.depthBufferWriteEnable();
      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);

      gc.viewportSet(framebuffer.kFramebufferGetArea());

      final Set<KInstanceTransformedType> instances =
        scene.getVisibleInstances();

      for (final KInstanceTransformedType instance : instances) {
        instance
          .transformedVisitableAccept(new KInstanceTransformedVisitorType<Unit, JCGLException>() {
            @Override public Unit transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth i)
              throws JCGLException,
                RException,
                ConstraintError
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunctionType<Unit, JCGLException>() {
                  @SuppressWarnings("synthetic-access") @Override public
                    Unit
                    run(
                      final @Nonnull MatricesInstanceType mi)
                      throws JCGLException,
                        ConstraintError,
                        RException
                  {
                    KRendererDebugDepthVariance.this.renderInstancePre(
                      gc,
                      mo,
                      i,
                      mi);
                    return Unit.unit();
                  }
                });
            }

            @Override public Unit transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular i)
              throws JCGLException,
                RException,
                ConstraintError
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunctionType<Unit, JCGLException>() {
                  @SuppressWarnings("synthetic-access") @Override public
                    Unit
                    run(
                      final @Nonnull MatricesInstanceType mi)
                      throws JCGLException,
                        ConstraintError,
                        RException
                  {
                    KRendererDebugDepthVariance.this.renderInstancePre(
                      gc,
                      mo,
                      i,
                      mi);
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
