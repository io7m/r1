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

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutionException;
import com.io7m.jcanephora.JCBExecutorProcedure;
import com.io7m.jcanephora.JCBProgram;
import com.io7m.jcanephora.JCBProgramProcedure;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI2I;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorM4F;
import com.io7m.jtensors.VectorReadable4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.kernel.KAbstractRenderer.KAbstractRendererForward;

final class KRendererBlurDemo extends KAbstractRendererForward
{
  private static final @Nonnull String NAME = "blur-demo";

  public static KRendererBlurDemo rendererNew(
    final @Nonnull JCGLImplementation g,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException,
      ConstraintError,
      KXMLException
  {
    return new KRendererBlurDemo(g, fs, log);
  }

  private final @Nonnull VectorM4F               background;
  private final @Nonnull KFramebufferForwardType blur_0;
  private final @Nonnull KFramebufferForwardType blur_1;
  private final @Nonnull JCGLImplementation      gl;
  private final @Nonnull Log                     log;
  private final @Nonnull KMutableMatrices        matrices;
  private final @Nonnull KProgram                program_blur_h;
  private final @Nonnull KProgram                program_blur_v;
  private final @Nonnull KProgram                program_id;
  private final @Nonnull KProgram                program_normals;
  private final @Nonnull KUnitQuad               quad;
  private final @Nonnull KTransform.Context      transform_context;
  private final @Nonnull VectorM2I               viewport_size;
  private final float                            blur_size;
  private final int                              blur_passes;
  private final int                              blur_image_size;

  private KRendererBlurDemo(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLCompileException,
      ConstraintError,
      JCGLUnsupportedException,
      JCGLException,
      FilesystemError,
      IOException,
      KXMLException
  {
    super(KRendererBlurDemo.NAME);

    this.log = new Log(log, KRendererBlurDemo.NAME);
    this.gl = gl;

    final JCGLSLVersion version = gl.getGLCommon().metaGetSLVersion();

    this.background = new VectorM4F(0.0f, 0.0f, 0.0f, 0.0f);
    this.matrices = KMutableMatrices.newMatrices();
    this.transform_context = new KTransform.Context();
    this.viewport_size = new VectorM2I();

    this.program_normals =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "debug_normals_map_local",
        log);

    this.program_id =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "postprocessing_identity",
        log);

    this.program_blur_h =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "postprocessing_gaussian_blur_horizontal",
        log);

    this.program_blur_v =
      KProgram.newProgramFromFilesystem(
        gl.getGLCommon(),
        version.getNumber(),
        version.getAPI(),
        fs,
        "postprocessing_gaussian_blur_vertical",
        log);

    this.blur_passes = 1;
    this.blur_image_size = 512;
    this.blur_size = 1.0f;

    {
      final RangeInclusive range_x =
        new RangeInclusive(0, this.blur_image_size - 1);
      final RangeInclusive range_y =
        new RangeInclusive(0, this.blur_image_size - 1);
      final AreaInclusive size = new AreaInclusive(range_x, range_y);
      this.blur_0 =
        KFramebufferForward.newFramebuffer(
          gl,
          size,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
    }

    {
      final RangeInclusive range_x =
        new RangeInclusive(0, this.blur_image_size - 1);
      final RangeInclusive range_y =
        new RangeInclusive(0, this.blur_image_size - 1);
      final AreaInclusive size = new AreaInclusive(range_x, range_y);
      this.blur_1 =
        KFramebufferForward.newFramebuffer(
          gl,
          size,
          TextureFilterMinification.TEXTURE_FILTER_LINEAR,
          TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
    }

    this.quad = KUnitQuad.newQuad(gl.getGLCommon(), log);
  }

  @Override public void rendererClose()
    throws JCGLException,
      ConstraintError
  {
    this.blur_0.kFramebufferDelete(this.gl);
    this.blur_1.kFramebufferDelete(this.gl);
  }

  @Override public @CheckForNull KRendererDebugging rendererDebug()
  {
    return null;
  }

  @Override public void rendererForwardEvaluate(
    final @Nonnull KFramebufferForwardUsable framebuffer,
    final @Nonnull KScene scene)
    throws JCGLException,
      ConstraintError,
      JCGLCompileException,
      JCGLUnsupportedException,
      IOException,
      KXMLException
  {
    this.renderEvaluateScene(scene, this.blur_0);

    for (int pass = 0; pass < this.blur_passes; ++pass) {
      this.renderEvaluateBlurH(this.blur_0, this.blur_1);
      this.renderEvaluateBlurV(this.blur_1, this.blur_0);
    }

    this.renderEvaluateCopy(this.blur_0, framebuffer);
  }

  @Override public void rendererSetBackgroundRGBA(
    final @Nonnull VectorReadable4F rgba)
    throws ConstraintError
  {
    VectorM4F.copy(rgba, this.background);
  }

  private void renderEvaluateScene(
    final @Nonnull KScene scene,
    final @Nonnull KFramebufferForwardUsable output)
    throws ConstraintError,
      JCGLException
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final KMutableMatrices.WithCamera mwc =
      this.matrices.withCamera(scene.getCamera());

    try {
      final AreaInclusive area = output.kFramebufferGetArea();
      this.viewport_size.x = (int) area.getRangeX().getInterval();
      this.viewport_size.y = (int) area.getRangeY().getInterval();

      try {
        gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
        gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

        gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
        gc.depthBufferClear(1.0f);
        gc.colorBufferClearV4f(this.background);
        gc.blendingDisable();

        final JCBExecutionAPI e = this.program_normals.getExecutable();
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

            for (final KMeshInstanceTransformed mesh : scene
              .getVisibleInstances()) {
              KRendererBlurDemo.this.renderMesh(gc, p, mwc, mesh);
            }
          }
        });
      } catch (final JCBExecutionException x) {
        throw new UnreachableCodeException(x);
      } finally {
        gc.framebufferDrawUnbind();
      }
    } finally {
      mwc.cameraFinish();
    }
  }

  private void renderEvaluateBlurH(
    final @Nonnull KFramebufferForwardUsable input,
    final @Nonnull KFramebufferForwardUsable output)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final AreaInclusive area = output.kFramebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();

    final List<TextureUnit> units = gc.textureGetUnits();

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferWriteDisable();
      gc.depthBufferTestDisable();
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      final JCBExecutionAPI e = this.program_blur_h.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KRendererBlurDemo.this.quad.getArray();
            final IndexBufferUsable indices =
              KRendererBlurDemo.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePosition(p, array);
            KShadingProgramCommon.bindAttributeUV(p, array);

            final long width =
              input.kFramebufferGetArea().getRangeX().getInterval();
            p.programUniformPutFloat("image_width", width
              / KRendererBlurDemo.this.blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input.kFramebufferGetRGBATexture());
            p.programUniformPutTextureUnit("t_image", unit);

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
      });
    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderEvaluateBlurV(
    final @Nonnull KFramebufferForwardUsable input,
    final @Nonnull KFramebufferForwardUsable output)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final AreaInclusive area = output.kFramebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();

    final List<TextureUnit> units = gc.textureGetUnits();

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferWriteDisable();
      gc.depthBufferTestDisable();
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      final JCBExecutionAPI e = this.program_blur_v.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KRendererBlurDemo.this.quad.getArray();
            final IndexBufferUsable indices =
              KRendererBlurDemo.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePosition(p, array);
            KShadingProgramCommon.bindAttributeUV(p, array);

            final long height =
              input.kFramebufferGetArea().getRangeY().getInterval();
            p.programUniformPutFloat("image_height", height
              / KRendererBlurDemo.this.blur_size);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input.kFramebufferGetRGBATexture());
            p.programUniformPutTextureUnit("t_image", unit);

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
      });
    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  private void renderEvaluateCopy(
    final @Nonnull KFramebufferForwardUsable input,
    final @Nonnull KFramebufferForwardUsable output)
    throws JCGLException,
      ConstraintError
  {
    final JCGLInterfaceCommon gc = this.gl.getGLCommon();

    final AreaInclusive area = output.kFramebufferGetArea();
    this.viewport_size.x = (int) area.getRangeX().getInterval();
    this.viewport_size.y = (int) area.getRangeY().getInterval();

    final List<TextureUnit> units = gc.textureGetUnits();

    try {
      gc.framebufferDrawBind(output.kFramebufferGetColorFramebuffer());
      gc.viewportSet(VectorI2I.ZERO, this.viewport_size);

      gc.depthBufferWriteDisable();
      gc.depthBufferTestDisable();
      gc.colorBufferClearV4f(this.background);
      gc.blendingDisable();

      final JCBExecutionAPI e = this.program_id.getExecutable();
      e.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram p)
          throws ConstraintError,
            JCGLException,
            Exception
        {
          try {
            final ArrayBufferUsable array =
              KRendererBlurDemo.this.quad.getArray();
            final IndexBufferUsable indices =
              KRendererBlurDemo.this.quad.getIndices();

            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePosition(p, array);
            KShadingProgramCommon.bindAttributeUV(p, array);

            final TextureUnit unit = units.get(0);
            gc.texture2DStaticBind(unit, input.kFramebufferGetRGBATexture());
            p.programUniformPutTextureUnit("t_image", unit);

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
      });
    } catch (final JCBExecutionException x) {
      throw new UnreachableCodeException(x);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  @SuppressWarnings("static-method") private void renderMesh(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull JCBProgram p,
    final @Nonnull KMutableMatrices.WithCamera mwc,
    final @Nonnull KMeshInstanceTransformed transformed)
    throws ConstraintError,
      JCGLException,
      JCBExecutionException
  {
    final KMutableMatrices.WithInstance mwi = mwc.withInstance(transformed);
    try {

      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixProjectionReuse(p);
      KShadingProgramCommon.putMatrixModelView(p, mwi.getMatrixModelView());

      /**
       * Upload matrices, set textures.
       */

      final List<TextureUnit> texture_units = gc.textureGetUnits();
      final KMeshInstance instance = transformed.getInstance();
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

      KShadingProgramCommon.putTextureNormal(p, texture_units.get(0));

      /**
       * Associate array attributes with program attributes, and draw mesh.
       */

      try {
        final KMesh mesh = instance.getMesh();
        final ArrayBuffer array = mesh.getArrayBuffer();
        final IndexBuffer indices = mesh.getIndexBuffer();

        gc.arrayBufferBind(array);
        KShadingProgramCommon.bindAttributePosition(p, array);
        KShadingProgramCommon.bindAttributeNormal(p, array);
        KShadingProgramCommon.bindAttributeTangent4(p, array);
        KShadingProgramCommon.bindAttributeUV(p, array);

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
