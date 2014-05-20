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

import com.io7m.jcache.JCacheException;
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
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KInstanceVisitorType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A debug renderer that displays the calculate depth-variance values for all
 * meshes.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererDebugDepthVariance implements
  KRendererDebugType
{
  private static final String NAME = "debug-depth-variance";

  private static void putMaterialOpaque(
    final JCBProgramType jp,
    final KMaterialOpaqueType material)
    throws JCGLException
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
   */

  public static KRendererDebugDepthVariance rendererNew(
    final JCGLImplementationType g,
    final KMaterialDepthLabelCacheType depth_labels,
    final KShaderCacheType shader_cache,
    final LogUsableType log)
  {
    return new KRendererDebugDepthVariance(g, depth_labels, shader_cache, log);
  }

  private final KMaterialDepthLabelCacheType depth_labels;
  private final JCGLImplementationType       gl;
  private final LogUsableType                log;
  private final KMutableMatrices             matrices;
  private final KShaderCacheType             shader_cache;
  private final KTransformContext            transform_context;

  private KRendererDebugDepthVariance(
    final JCGLImplementationType in_gl,
    final KMaterialDepthLabelCacheType in_depth_labels,
    final KShaderCacheType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(KRendererDebugDepthVariance.NAME);
    this.gl = NullCheck.notNull(in_gl, "GL");
    this.depth_labels = NullCheck.notNull(in_depth_labels, "Depth labels");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");

    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = KTransformContext.newContext();
    this.log.debug("initialized");
  }

  @Override public void rendererDebugEvaluate(
    final KFramebufferRGBAUsableType framebuffer,
    final KScene scene)
    throws RException
  {
    final JCGLInterfaceCommonType gc = this.gl.getGLCommon();
    final KCamera camera = scene.getCamera();

    try {
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix(),
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesObserverType mo)
            throws JCGLException,
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
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugDepthVariance.NAME;
  }

  private static void renderInstance(
    final JCGLInterfaceCommonType gc,
    final JCBProgramType p,
    final MatricesInstanceType mi,
    final KInstanceOpaqueType instance,
    final KMaterialDepthLabel label)
    throws JCGLException
  {
    final KMaterialOpaqueType material =
      instance.instanceGetMeshWithMaterial().meshGetMaterial();
    final List<TextureUnitType> units = gc.textureGetUnits();

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
        final TextureUnitType u = units.get(0);
        assert u != null;
        KShadingProgramCommon.bindPutTextureAlbedo(
          p,
          gc,
          material.materialGetAlbedo(),
          u);
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = instance.instanceGetMeshWithMaterial().instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

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

      p.programExecute(new JCBProgramProcedureType<JCGLException>() {
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

  private void renderInstancePre(
    final JCGLInterfaceCommonType gc,
    final MatricesObserverType mo,
    final KInstanceOpaqueType instance,
    final MatricesInstanceType mi)
    throws JCGLException,
      RException
  {
    try {
      final KMeshWithMaterialOpaqueType ii = instance.instanceGetMeshWithMaterial();

      final KMaterialDepthLabel label =
        KRendererDebugDepthVariance.this.depth_labels.getDepthLabel(ii);
      final String program_name =
        String.format("depth_variance_%s", label.labelGetCode());
      assert program_name != null;
      final KProgram kp =
        KRendererDebugDepthVariance.this.shader_cache
          .cacheGetLU(program_name);

      final JCBExecutorType e = kp.getExecutable();
      e.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType p)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            p,
            mo.getMatrixProjection());

          KRendererDebugDepthVariance.renderInstance(
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
    final KFramebufferRGBAUsableType framebuffer,
    final KScene scene,
    final JCGLInterfaceCommonType gc,
    final MatricesObserverType mo)
    throws JCGLException,
      RException
  {
    final FramebufferUsableType output_buffer =
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

      final Set<KInstanceType> instances =
        scene.getVisibleInstances();

      for (final KInstanceType instance : instances) {
        instance
          .transformedAccept(new KInstanceVisitorType<Unit, JCGLException>() {
            @Override public Unit transformedOpaqueAlphaDepth(
              final KInstanceOpaqueAlphaDepth i)
              throws JCGLException,
                RException
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final MatricesInstanceType mi)
                    throws JCGLException,
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

            @Override public Unit transformedOpaqueRegular(
              final KInstanceOpaqueRegular i)
              throws JCGLException,
                RException
            {
              return mo.withInstance(
                instance,
                new MatricesInstanceFunctionType<Unit, JCGLException>() {
                  @Override public Unit run(
                    final MatricesInstanceType mi)
                    throws JCGLException,
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

            @Override public Unit transformedTranslucentRefractive(
              final KInstanceTranslucentRefractive i)
              throws JCGLException
            {
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRegular(
              final KInstanceTranslucentRegular i)
              throws JCGLException
            {
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentSpecularOnly(
              final KInstanceTranslucentSpecularOnly i)
              throws RException,
                JCGLException
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
