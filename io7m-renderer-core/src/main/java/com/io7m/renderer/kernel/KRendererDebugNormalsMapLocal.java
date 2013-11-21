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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FramebufferReferenceUsable;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;

final class KRendererDebugNormalsMapLocal implements KRenderer
{
  public static KRendererDebugNormalsMapLocal rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError
  {
    return new KRendererDebugNormalsMapLocal(g, fs, log);
  }

  private final @Nonnull VectorM4F             background;
  private final @Nonnull JCCEExecutionCallable exec;
  private final @Nonnull JCGLImplementation    gl;
  private final @Nonnull Log                   log;
  private final @Nonnull KMutableMatrices      matrices;
  private final @Nonnull ProgramReference      program;
  private final @Nonnull KTransform.Context    transform_context;
  private final @Nonnull VectorM2I             viewport_size;

  private KRendererDebugNormalsMapLocal(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException
  {
    this.log = new Log(log, "krenderer-debug-normals-map-local");
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.program =
      KShaderUtilities.makeProgram(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "debug_normals_computed_bitangent_map_local",
        log);

    this.exec = new JCCEExecutionCallable(this.program);
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();
    gc.programDelete(this.program);
  }

  @Override public void rendererEvaluate(
    final @Nonnull KFramebufferRGBAUsable framebuffer,
    final @Nonnull KVisibleScene scene)
    throws JCGLException,
      ConstraintError
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

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferClear(1.0f);
        gc.colorBufferClearV4f(this.background);
        gc.blendingDisable();

        this.exec.execPrepare(gc);
        KShadingProgramCommon.putMatrixProjection(
          this.exec,
          gc,
          mwc.getMatrixProjection());
        this.exec.execCancel();

        for (final KMeshInstance mesh : scene.getInstances()) {
          this.renderMesh(gc, mwc, mesh);
        }
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
      JCGLException
  {
    final KMutableMatrices.WithInstance mwi = mwc.withInstance(instance);
    try {

      /**
       * Upload matrices.
       */

      this.exec.execPrepare(gc);
      KShadingProgramCommon.putMatrixProjectionReuse(this.exec);
      KShadingProgramCommon.putMatrixModelView(
        this.exec,
        gc,
        mwi.getMatrixModelView());

      /**
       * Upload matrices, set textures.
       */

      final List<TextureUnit> texture_units = gc.textureGetUnits();
      final KMaterial material = instance.getMaterial();

      {
        final Option<Texture2DStatic> normal_opt =
          material.getNormal().getTexture();
        if (normal_opt.isSome()) {
          gc.texture2DStaticBind(
            texture_units.get(0),
            ((Option.Some<Texture2DStatic>) normal_opt).value);
        } else {
          gc.texture2DStaticUnbind(texture_units.get(0));
        }
      }

      this.exec.execUniformPutTextureUnit(
        gc,
        "t_normal",
        texture_units.get(0));

      /**
       * Associate array attributes with program attributes, and draw mesh.
       */

      try {
        final KMesh mesh = instance.getMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();

        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePosition(gc, this.exec, array);
        KShadingProgramCommon.bindAttributeNormal(gc, this.exec, array);
        KShadingProgramCommon.bindAttributeTangent4(gc, this.exec, array);
        KShadingProgramCommon.bindAttributeUV(gc, this.exec, array);

        this.exec.execSetCallable(new Callable<Void>() {
          @Override public Void call()
            throws Exception
          {
            try {
              gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
            } catch (final ConstraintError e) {
              throw new UnreachableCodeException();
            }
            return null;
          }
        });

        try {
          this.exec.execRun(gc);
        } catch (final Exception e) {
          throw new UnreachableCodeException();
        }

      } finally {
        gc.arrayBufferUnbind();
      }
    } finally {
      mwi.instanceFinish();
    }
  }
}
