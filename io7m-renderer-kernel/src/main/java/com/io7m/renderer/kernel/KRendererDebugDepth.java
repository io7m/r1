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
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTransformedTranslucentSpecularOnly;
import com.io7m.renderer.kernel.types.KInstanceTransformedType;
import com.io7m.renderer.kernel.types.KInstanceTransformedVisitorType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KTransformContext;
import com.io7m.renderer.types.RException;

/**
 * A debug renderer that displays the calculated depth for all meshes.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRendererDebugDepth implements
  KRendererDebugType
{
  private static final String NAME = "debug-depth";

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

  public static KRendererDebugDepth rendererNew(
    final JCGLImplementationType g,
    final KMaterialDepthLabelCacheType depth_labels,
    final KShaderCacheType shader_cache,
    final LogUsableType log)
  {
    return new KRendererDebugDepth(g, depth_labels, shader_cache, log);
  }

  private final KMaterialDepthLabelCacheType depth_labels;
  private final JCGLImplementationType       gl;
  private final LogUsableType                log;
  private final KMutableMatrices             matrices;
  private final KShaderCacheType             shader_cache;
  private final KTransformContext            transform_context;

  private KRendererDebugDepth(
    final JCGLImplementationType in_gl,
    final KMaterialDepthLabelCacheType in_depth_labels,
    final KShaderCacheType in_shader_cache,
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log").with(KRendererDebugDepth.NAME);
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
            KRendererDebugDepth.this.renderScene(framebuffer, scene, gc, mo);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRendererDebugDepth.NAME;
  }

  @SuppressWarnings({ "static-method" }) private void renderInstance(
    final JCGLInterfaceCommonType gc,
    final JCBProgramType p,
    final MatricesInstanceType mi,
    final KInstanceTransformedOpaqueType instance,
    final KMaterialDepthLabel label)
    throws JCGLException,
      RException
  {
    final KMaterialOpaqueType material =
      instance.instanceGet().instanceGetMaterial();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final TextureUnitType u = units.get(0);
    assert u != null;

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      p,
      mi.getMatrixModelView());

    material
      .materialOpaqueAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
        @Override public Unit materialOpaqueAlphaDepth(
          final KMaterialOpaqueAlphaDepth m)
          throws RException,
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
              KShadingProgramCommon.putMatrixUVUnchecked(p, mi.getMatrixUV());

              KShadingProgramCommon.bindPutTextureAlbedo(
                p,
                gc,
                material.materialGetAlbedo(),
                u);
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

        @Override public Unit materialOpaqueRegular(
          final KMaterialOpaqueRegular m)
          throws RException,
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
              KShadingProgramCommon.putMatrixUVUnchecked(p, mi.getMatrixUV());
              KShadingProgramCommon.bindPutTextureAlbedo(
                p,
                gc,
                material.materialGetAlbedo(),
                u);
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
      final KMeshReadableType mesh = instance.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.getArrayBuffer();
      final IndexBufferUsableType indices = mesh.getIndexBuffer();

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
    final KInstanceTransformedOpaqueType instance,
    final MatricesInstanceType mi)
    throws JCGLException,
      RException
  {
    try {
      final KInstanceOpaqueType ii = instance.instanceGet();

      final KMaterialDepthLabel label =
        KRendererDebugDepth.this.depth_labels.getDepthLabel(ii);

      final String sh = String.format("depth_%s", label.labelGetCode());
      assert sh != null;

      final KProgram kp =
        KRendererDebugDepth.this.shader_cache.cacheGetLU(sh);

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

          KRendererDebugDepth.this.renderInstance(gc, p, mi, instance, label);
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
      gc.viewportSet(framebuffer.kFramebufferGetArea());

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
          .transformedAccept(new KInstanceTransformedVisitorType<Unit, JCGLException>() {
            @Override public Unit transformedOpaqueAlphaDepth(
              final KInstanceTransformedOpaqueAlphaDepth i)
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
                    KRendererDebugDepth.this.renderInstancePre(gc, mo, i, mi);
                    return Unit.unit();
                  }
                });
            }

            @Override public Unit transformedOpaqueRegular(
              final KInstanceTransformedOpaqueRegular i)
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
                    KRendererDebugDepth.this.renderInstancePre(gc, mo, i, mi);
                    return Unit.unit();
                  }
                });
            }

            @Override public Unit transformedTranslucentRefractive(
              final KInstanceTransformedTranslucentRefractive i)
              throws JCGLException
            {
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentRegular(
              final KInstanceTransformedTranslucentRegular i)
              throws JCGLException
            {
              return Unit.unit();
            }

            @Override public Unit transformedTranslucentSpecularOnly(
              final KInstanceTransformedTranslucentSpecularOnly i)
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
