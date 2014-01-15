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
import com.io7m.jcanephora.AreaInclusive;
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
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererDebug;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstance;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunction;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserver;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverFunction;

final class KRendererDebugDepthVariance extends KAbstractRendererDebug
{
  private static final @Nonnull String NAME = "debug-depth";

  public static KRendererDebugDepthVariance rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
  {
    return new KRendererDebugDepthVariance(g, depth_labels, shader_cache, log);
  }

  private final @Nonnull VectorM4F                             background;
  private final @Nonnull KMaterialDepthLabelCache              depth_labels;
  private final @Nonnull JCGLImplementation                    gl;
  private final @Nonnull Log                                   log;
  private final @Nonnull KMutableMatrices                      matrices;
  private final @Nonnull LUCache<String, KProgram, RException> shader_cache;
  private final @Nonnull KTransform.Context                    transform_context;
  private final @Nonnull VectorM2I                             viewport_size;

  private KRendererDebugDepthVariance(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull LUCache<String, KProgram, RException> shader_cache,
    final @Nonnull Log log)
  {
    super(KRendererDebugDepthVariance.NAME);

    this.log = new Log(log, KRendererDebugDepthVariance.NAME);
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();
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

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @SuppressWarnings("static-method") private void renderInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull MatricesInstance mi,
    final @Nonnull KMeshInstanceTransformed instance,
    final @Nonnull KMaterialDepthLabel label)
    throws ConstraintError,
      JCGLException
  {
    final KMaterial material = instance.getInstance().getMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(p);
    KShadingProgramCommon.putMatrixModelView(p, mi.getMatrixModelView());

    switch (label) {
      case DEPTH_VARIANCE_CONSTANT:
      case DEPTH_CONSTANT_PACKED4444:
      case DEPTH_CONSTANT:
      {
        break;
      }
      case DEPTH_VARIANCE_UNIFORM:
      case DEPTH_UNIFORM_PACKED4444:
      case DEPTH_UNIFORM:
      {
        KShadingProgramCommon.putMaterial(p, material);
        break;
      }
      case DEPTH_VARIANCE_MAPPED:
      case DEPTH_MAPPED_PACKED4444:
      case DEPTH_MAPPED:
      {
        KShadingProgramCommon.putMaterial(p, material);
        KShadingProgramCommon.putMatrixUV(p, mi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          p,
          gc,
          material,
          units.get(0));
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMesh mesh = instance.getInstance().getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePosition(p, array);

      switch (label) {
        case DEPTH_CONSTANT_PACKED4444:
        case DEPTH_CONSTANT:
        case DEPTH_UNIFORM:
        case DEPTH_UNIFORM_PACKED4444:
        case DEPTH_VARIANCE_CONSTANT:
        case DEPTH_VARIANCE_UNIFORM:
        {
          break;
        }
        case DEPTH_VARIANCE_MAPPED:
        case DEPTH_MAPPED_PACKED4444:
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

  private static @Nonnull KMaterialDepthLabel forceVariance(
    final @Nonnull KMaterialDepthLabel label)
  {
    switch (label) {
      case DEPTH_CONSTANT:
      case DEPTH_CONSTANT_PACKED4444:
      {
        return KMaterialDepthLabel.DEPTH_VARIANCE_CONSTANT;
      }
      case DEPTH_MAPPED:
      case DEPTH_MAPPED_PACKED4444:
      {
        return KMaterialDepthLabel.DEPTH_VARIANCE_MAPPED;
      }
      case DEPTH_UNIFORM:
      case DEPTH_UNIFORM_PACKED4444:
      {
        return KMaterialDepthLabel.DEPTH_VARIANCE_UNIFORM;
      }
      case DEPTH_VARIANCE_CONSTANT:
      case DEPTH_VARIANCE_MAPPED:
      case DEPTH_VARIANCE_UNIFORM:
      {
        return label;
      }
    }

    throw new UnreachableCodeException();
  }

  private void renderInstancePre(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserver mo,
    final @Nonnull KMeshInstanceTransformed instance,
    final @Nonnull MatricesInstance mi)
    throws ConstraintError,
      JCGLException,
      RException
  {
    try {
      final KMeshInstance ii = instance.getInstance();
      final KMaterial m = ii.getMaterial();
      if (m.getAlpha().isTranslucent()) {
        return;
      }

      final KMaterialDepthLabel label =
        KRendererDebugDepthVariance
          .forceVariance(KRendererDebugDepthVariance.this.depth_labels
            .getDepthLabel(ii));

      final KProgram kp =
        KRendererDebugDepthVariance.this.shader_cache.luCacheGet(label
          .getName());

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

          KRendererDebugDepthVariance.this.renderInstance(
            gc,
            p,
            mi,
            instance,
            label);
        }
      });

    } catch (final LUCacheException x) {
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

    final AreaInclusive area = framebuffer.kFramebufferGetArea();
    KRendererDebugDepthVariance.this.viewport_size.x =
      (int) area.getRangeX().getInterval();
    KRendererDebugDepthVariance.this.viewport_size.y =
      (int) area.getRangeY().getInterval();

    gc.framebufferDrawBind(output_buffer);
    try {
      gc.viewportSet(
        VectorI2I.ZERO,
        KRendererDebugDepthVariance.this.viewport_size);

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferClear(1.0f);
      gc.colorBufferClearV4f(KRendererDebugDepthVariance.this.background);
      gc.blendingDisable();

      final Set<KMeshInstanceTransformed> instances =
        scene.getVisibleInstances();

      for (final KMeshInstanceTransformed instance : instances) {
        mo.withInstance(
          instance,
          new MatricesInstanceFunction<Unit, JCGLException>() {
            @SuppressWarnings("synthetic-access") @Override public Unit run(
              final @Nonnull MatricesInstance mi)
              throws JCGLException,
                ConstraintError,
                RException
            {
              KRendererDebugDepthVariance.this.renderInstancePre(
                gc,
                mo,
                instance,
                mi);
              return Unit.unit();
            }
          });
      }
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
