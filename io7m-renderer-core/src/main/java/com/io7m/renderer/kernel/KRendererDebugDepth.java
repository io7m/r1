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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
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
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererDebug;

final class KRendererDebugDepth extends KAbstractRendererDebug
{
  private static final @Nonnull String NAME = "debug-depth";

  public static
    KRendererDebugDepth
    rendererNew(
      final @Nonnull JCGLImplementation g,
      final @Nonnull KMaterialDepthLabelCache depth_labels,
      final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull Log log)
  {
    return new KRendererDebugDepth(g, depth_labels, shader_cache, log);
  }

  private final @Nonnull VectorM4F                                        background;
  private final @Nonnull JCGLImplementation                               gl;
  private final @Nonnull Log                                              log;
  private final @Nonnull KMutableMatrices                                 matrices;
  private final @Nonnull KTransform.Context                               transform_context;
  private final @Nonnull VectorM2I                                        viewport_size;
  private final @Nonnull KMaterialDepthLabelCache                         depth_labels;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache;

  private KRendererDebugDepth(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull Log log)
  {
    super(KRendererDebugDepth.NAME);
    this.log = new Log(log, KRendererDebugDepth.NAME);
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();
    this.depth_labels = depth_labels;
    this.shader_cache = shader_cache;
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
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
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KCamera camera = scene.getCamera();
    final KMutableMatrices.WithObserver mwc =
      this.matrices.withObserver(
        camera.getViewMatrix(),
        camera.getProjectionMatrix());

    try {
      final FramebufferReferenceUsable output_buffer =
        framebuffer.kFramebufferGetColorFramebuffer();

      final AreaInclusive area = framebuffer.kFramebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();

      try {
        gc.framebufferDrawBind(output_buffer);
        gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferClear(1.0f);
        gc.colorBufferClearV4f(this.background);
        gc.blendingDisable();

        for (final KMeshInstanceTransformed instance : scene
          .getVisibleInstances()) {
          final KMeshInstance ii = instance.getInstance();
          final KMaterial m = ii.getMaterial();
          if (m.getAlpha().isTranslucent()) {
            continue;
          }

          final KMaterialDepthLabel label =
            this.depth_labels.getDepthLabel(ii);

          final KProgram kp =
            this.shader_cache.luCacheGet("depth_" + label.getCode());

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
                mwc.getMatrixProjection());
              KRendererDebugDepth.this
                .renderMesh(gc, p, mwc, instance, label);
            }
          });
        }

      } catch (final JCBExecutionException x) {
        throw new UnreachableCodeException(x);
      } catch (final KShaderCacheException x) {
        throw new UnreachableCodeException(x);
      } catch (final LUCacheException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      mwc.observerFinish();
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  @SuppressWarnings("static-method") private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KMutableMatrices.WithObserver mwc,
    final @Nonnull KMeshInstanceTransformed instance,
    final @Nonnull KMaterialDepthLabel label)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);
    final KMaterial material = instance.getInstance().getMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    try {
      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixProjectionReuse(p);
      KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

      switch (label) {
        case DEPTH_CONSTANT_PACKED4444:
        case DEPTH_CONSTANT:
        {
          break;
        }
        case DEPTH_UNIFORM_PACKED4444:
        case DEPTH_UNIFORM:
        {
          KShadingProgramCommon.putMaterial(p, material);
          break;
        }
        case DEPTH_MAPPED_PACKED4444:
        case DEPTH_MAPPED:
        {
          KShadingProgramCommon.putMaterial(p, material);
          KShadingProgramCommon.putMatrixUV(p, mwi.getMatrixUV());
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
          {
            break;
          }
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
    } finally {
      mwi.instanceFinish();
    }
  }
}
