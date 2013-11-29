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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jlucache.LRUCacheTrivial;
import com.io7m.jlucache.LUCache;
import com.io7m.jlucache.LUCacheException;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheFilesystemException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheIOException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLCompileException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheJCGLUnsupportedException;
import com.io7m.renderer.kernel.KShaderCacheException.KShaderCacheXMLException;

final class KRendererDebugDepthShadow implements KRenderer
{
  private static void handleShaderCacheException(
    final @Nonnull KShaderCacheException e)
    throws IOException,
      JCGLCompileException,
      JCGLException,
      JCGLUnsupportedException,
      KXMLException
  {
    switch (e.getCode()) {
      case KSHADER_CACHE_FILESYSTEM_ERROR:
      {
        final KShaderCacheFilesystemException x =
          (KShaderCacheFilesystemException) e;
        throw new IOException(x);
      }
      case KSHADER_CACHE_IO_ERROR:
      {
        final KShaderCacheIOException x = (KShaderCacheIOException) e;
        throw x.getCause();
      }
      case KSHADER_CACHE_JCGL_COMPILE_ERROR:
      {
        final KShaderCacheJCGLCompileException x =
          (KShaderCacheJCGLCompileException) e;
        throw x.getCause();
      }
      case KSHADER_CACHE_JCGL_ERROR:
      {
        final KShaderCacheJCGLException x = (KShaderCacheJCGLException) e;
        throw x.getCause();
      }
      case KSHADER_CACHE_JCGL_UNSUPPORTED_ERROR:
      {
        final KShaderCacheJCGLUnsupportedException x =
          (KShaderCacheJCGLUnsupportedException) e;
        throw x.getCause();
      }
      case KSHADER_CACHE_XML_ERROR:
      {
        final KShaderCacheXMLException x = (KShaderCacheXMLException) e;
        throw x.getCause();
      }
    }
  }

  public static
    KRendererDebugDepthShadow
    rendererNew(
      final @Nonnull JCGLImplementation g,
      final @Nonnull LRUCacheTrivial<String, KProgram, KShaderCacheException> shader_cache,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new KRendererDebugDepthShadow(g, shader_cache, log);
  }

  private final @Nonnull VectorM4F                                        background;
  private final @Nonnull JCGLImplementation                               gl;
  private final @Nonnull Log                                              log;
  private final @Nonnull KMutableMatrices                                 matrices;
  private final @Nonnull KTransform.Context                               transform_context;
  private final @Nonnull VectorM2I                                        viewport_size;
  private final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache;

  private KRendererDebugDepthShadow(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull LUCache<String, KProgram, KShaderCacheException> shader_cache,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.log = new Log(log, "krenderer-debug-depth-shadow");
    this.gl = gl;

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();
    this.shader_cache =
      Constraints.constrainNotNull(shader_cache, "Shader cache");
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

  @Override public void rendererEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KVisibleScene scene)
    throws JCGLException,
      ConstraintError,
      IOException,
      JCGLCompileException,
      JCGLUnsupportedException,
      KXMLException
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KMutableMatrices.WithCamera mwc =
      this.matrices.withCamera(scene.getCamera());

    try {
      final FramebufferReferenceUsable output_buffer =
        framebuffer.kframebufferGetFramebuffer();

      final AreaInclusive area = framebuffer.kframebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();

      try {
        gc.framebufferDrawBind(output_buffer);
        gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
        gc.depthBufferClear(1.0f);
        gc.colorBufferClearV4f(this.background);
        gc.blendingDisable();

        for (final KMeshInstance mesh : scene.getInstances()) {
          this.renderMesh(gc, mwc, mesh);
        }
      } catch (final KShaderCacheException x) {
        KRendererDebugDepthShadow.handleShaderCacheException(x);
      } catch (final LUCacheException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      mwc.cameraFinish();
    }
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
  {
    VectorM4F.copy(rgba, this.background);
  }

  private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMutableMatrices.WithCamera mwc,
    final @Nonnull KMeshInstance instance)
    throws ConstraintError,
      JCGLException,
      KShaderCacheException,
      LUCacheException
  {
    final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);

    try {
      final KMeshInstanceShadowMaterialLabel label =
        instance.getShadowMaterialLabel();

      final KProgram p =
        this.shader_cache.luCacheGet("shadow_" + label.getCode());
      final JCCEExecutionCallable e = p.getExecutable();

      /**
       * Upload matrices.
       */

      e.execPrepare(gc);
      KShadingProgramCommon.putMatrixProjection(
        e,
        gc,
        mwi.getMatrixProjection());
      KShadingProgramCommon.putMatrixModelView(
        e,
        gc,
        mwi.getMatrixModelView());

      /**
       * Associate array attributes with program attributes, and draw mesh.
       */

      final KMesh mesh = instance.getMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);

      try {
        KShadingProgramCommon.bindAttributePosition(gc, e, array);

        switch (label) {
          case SHADOW_OPAQUE:
          {
            break;
          }
          case SHADOW_TRANSLUCENT:
          case SHADOW_TRANSLUCENT_TEXTURED:
          {
            KShadingProgramCommon.putMaterial(e, gc, instance.getMaterial());
            break;
          }
        }

        switch (label) {
          case SHADOW_OPAQUE:
          case SHADOW_TRANSLUCENT:
          {
            break;
          }
          case SHADOW_TRANSLUCENT_TEXTURED:
          {
            final List<TextureUnit> units = gc.textureGetUnits();
            KShadingProgramCommon.bindPutTextureAlbedo(
              gc,
              e,
              instance.getMaterial(),
              units.get(0));
            KShadingProgramCommon.putMatrixUV(gc, e, mwi.getMatrixUV());
            KShadingProgramCommon.bindAttributeUV(gc, e, array);
            break;
          }
        }

        e.execSetCallable(new Callable<Void>() {
          @Override public Void call()
            throws Exception
          {
            try {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            } catch (final ConstraintError x) {
              throw new UnreachableCodeException(x);
            }
            return null;
          }
        });

        try {
          e.execRun(gc);
        } catch (final Exception x) {
          throw new UnreachableCodeException(x);
        }

      } finally {
        gc.arrayBufferUnbind();
      }
    } finally {
      mwi.instanceFinish();
    }
  }
}
