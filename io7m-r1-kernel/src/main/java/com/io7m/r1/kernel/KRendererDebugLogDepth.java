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

package com.io7m.r1.kernel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.FramebufferColorAttachmentPointType;
import com.io7m.jcanephora.FramebufferDrawBufferType;
import com.io7m.jcanephora.FramebufferType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLResourceUsableType;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.RenderableDepthKind;
import com.io7m.jcanephora.RenderbufferType;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLFramebufferBuilderGL3ES3Type;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.kernel.types.KVisibleSetLightGroup;
import com.io7m.r1.kernel.types.KVisibleSetOpaques;
import com.io7m.r1.types.RException;

/**
 * Debugging depth renderer.
 */

@EqualityReference public final class KRendererDebugLogDepth implements
  KRendererDebugType
{
  @EqualityReference private static final class DebugGBuffer implements
    JCGLResourceUsableType
  {
    public static DebugGBuffer newGBuffer(
      final JCGLImplementationType in_gi,
      final AreaInclusive in_area)
    {
      NullCheck.notNull(in_gi);
      NullCheck.notNull(in_area);

      return in_gi
        .implementationAccept(new JCGLImplementationVisitorType<DebugGBuffer, JCGLException>() {
          @Override public DebugGBuffer implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
          {
            // TODO Auto-generated method stub
            throw new UnimplementedCodeException();
          }

          @Override public DebugGBuffer implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
          {
            final int width = (int) in_area.getRangeX().getInterval();
            final int height = (int) in_area.getRangeY().getInterval();

            final Texture2DStaticType in_depth_log =
              gl.texture2DStaticAllocateR32f(
                "depth-log",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

            final Texture2DStaticType in_depth_linear =
              gl.texture2DStaticAllocateR32f(
                "depth-linear",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

            final Texture2DStaticType in_depth_recon =
              gl.texture2DStaticAllocateR32f(
                "depth-recon",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

            final Texture2DStaticType in_depth_diff =
              gl.texture2DStaticAllocateR32f(
                "depth-diff",
                width,
                height,
                TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
                TextureFilterMinification.TEXTURE_FILTER_NEAREST,
                TextureFilterMagnification.TEXTURE_FILTER_NEAREST);

            final RenderbufferType<RenderableDepthKind> in_r =
              gl.renderbufferAllocateDepth24(width, height);

            final List<FramebufferColorAttachmentPointType> points =
              gl.framebufferGetColorAttachmentPoints();
            final FramebufferColorAttachmentPointType attach_0 =
              points.get(0);
            final FramebufferColorAttachmentPointType attach_1 =
              points.get(1);
            final FramebufferColorAttachmentPointType attach_2 =
              points.get(2);
            final FramebufferColorAttachmentPointType attach_3 =
              points.get(3);
            assert attach_0 != null;
            assert attach_1 != null;
            assert attach_2 != null;
            assert attach_3 != null;

            final List<FramebufferDrawBufferType> buffers =
              gl.framebufferGetDrawBuffers();

            final Map<FramebufferDrawBufferType, FramebufferColorAttachmentPointType> mappings =
              new HashMap<FramebufferDrawBufferType, FramebufferColorAttachmentPointType>();
            mappings.put(buffers.get(0), attach_0);
            mappings.put(buffers.get(1), attach_1);
            mappings.put(buffers.get(2), attach_2);
            mappings.put(buffers.get(3), attach_3);

            final JCGLFramebufferBuilderGL3ES3Type fbb =
              gl.framebufferNewBuilderGL3ES3();
            fbb.attachColorTexture2DAt(attach_0, in_depth_log);
            fbb.attachColorTexture2DAt(attach_1, in_depth_linear);
            fbb.attachColorTexture2DAt(attach_2, in_depth_recon);
            fbb.attachColorTexture2DAt(attach_3, in_depth_diff);
            fbb.attachDepthRenderbuffer(in_r);
            fbb.setDrawBuffers(mappings);

            final FramebufferType in_f = gl.framebufferAllocate(fbb);

            return new DebugGBuffer(
              in_depth_log,
              in_depth_linear,
              in_depth_recon,
              in_depth_diff,
              in_r,
              in_f);
          }

          @Override public DebugGBuffer implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
          {
            // TODO Auto-generated method stub
            throw new UnimplementedCodeException();
          }

          @Override public DebugGBuffer implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
          {
            // TODO Auto-generated method stub
            throw new UnimplementedCodeException();
          }
        });
    }

    private final Texture2DStaticType                   depth_log;
    private final Texture2DStaticType                   depth_linear;
    private final Texture2DStaticType                   depth_recon;
    private final Texture2DStaticType                   depth_diff;
    private boolean                                     deleted;
    private final FramebufferType                       f;
    private final RenderbufferType<RenderableDepthKind> r;

    public DebugGBuffer(
      final Texture2DStaticType in_depth_log,
      final Texture2DStaticType in_depth_linear,
      final Texture2DStaticType in_depth_recon,
      final Texture2DStaticType in_depth_diff,
      final RenderbufferType<RenderableDepthKind> in_r,
      final FramebufferType in_f)
    {
      this.depth_log = in_depth_log;
      this.depth_linear = in_depth_linear;
      this.depth_recon = in_depth_recon;
      this.depth_diff = in_depth_diff;
      this.r = in_r;
      this.f = in_f;
    }

    public void delete(
      final JCGLImplementationType gi)
    {
      if (this.deleted == false) {
        final JCGLInterfaceCommonType gc = gi.getGLCommon();
        gc.texture2DStaticDelete(this.depth_log);
        gc.texture2DStaticDelete(this.depth_linear);
        gc.texture2DStaticDelete(this.depth_recon);
        gc.texture2DStaticDelete(this.depth_diff);
        gc.renderbufferDelete(this.r);
        gc.framebufferDelete(this.f);
        this.deleted = true;
      }
    }

    public AreaInclusive getArea()
    {
      return this.depth_linear.textureGetArea();
    }

    @Override public boolean resourceIsDeleted()
    {
      return this.deleted;
    }

    public void bindTextures(
      final JCGLInterfaceCommonType gc)
    {
      final List<TextureUnitType> units = gc.textureGetUnits();
      gc.texture2DStaticBind(units.get(0), this.depth_log);
      gc.texture2DStaticBind(units.get(1), this.depth_linear);
      gc.texture2DStaticBind(units.get(2), this.depth_recon);
    }

    public void dumpTextures(
      final JCGLImplementationType in_gi)
    {
      in_gi
        .implementationAccept(new JCGLImplementationVisitorType<Unit, JCGLException>() {
          @Override public Unit implementationIsGL2(
            final JCGLInterfaceGL2Type gl)
          {
            return Unit.unit();
          }

          @Override public Unit implementationIsGL3(
            final JCGLInterfaceGL3Type gl)
          {
            try {
              final ByteBuffer r_depth_log =
                gl
                  .texture2DStaticGetImageUntyped(DebugGBuffer.this.depth_log);
              DebugGBuffer.dumpFloatTexture("log", r_depth_log);
              final ByteBuffer r_depth_linear =
                gl
                  .texture2DStaticGetImageUntyped(DebugGBuffer.this.depth_linear);
              DebugGBuffer.dumpFloatTexture("linear", r_depth_linear);
              final ByteBuffer r_depth_recon =
                gl
                  .texture2DStaticGetImageUntyped(DebugGBuffer.this.depth_recon);
              DebugGBuffer.dumpFloatTexture("recon", r_depth_recon);
            } catch (final JCGLException e) {
              e.printStackTrace();
            } catch (final IOException e) {
              e.printStackTrace();
            }
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES2(
            final JCGLInterfaceGLES2Type gl)
          {
            return Unit.unit();
          }

          @Override public Unit implementationIsGLES3(
            final JCGLInterfaceGLES3Type gl)
          {
            return Unit.unit();
          }
        });
    }

    protected static void dumpFloatTexture(
      final String name,
      final ByteBuffer r)
      throws IOException
    {
      FileOutputStream os = null;
      try {
        os = new FileOutputStream(new File("/tmp/" + name));
        final FileChannel fc = os.getChannel();
        fc.write(r);
      } finally {
        if (os != null) {
          os.close();
        }
      }
    }
  }

  private static DebugGBuffer makeFramebuffer(
    final JCGLImplementationType gi,
    final @Nullable DebugGBuffer previous,
    final KFramebufferRGBAUsableType framebuffer)
  {
    final AreaInclusive area = framebuffer.kFramebufferGetArea();
    if (previous == null) {
      return DebugGBuffer.newGBuffer(gi, area);
    }

    if (previous.getArea().equals(area) == false) {
      final DebugGBuffer new_gbuffer = DebugGBuffer.newGBuffer(gi, area);
      previous.delete(gi);
      return new_gbuffer;
    }

    return previous;
  }

  private @Nullable DebugGBuffer       buffer;
  private final JCGLImplementationType gi;
  private final KShaderCacheDebugType  shaders_debug;
  private final KMutableMatrices       matrices;
  private boolean                      first;

  /**
   * Construct a new renderer.
   *
   * @param in_gi
   *          An OpenGL implementation
   * @param in_shaders_debug
   *          The shader caches
   */

  public KRendererDebugLogDepth(
    final JCGLImplementationType in_gi,
    final KShaderCacheDebugType in_shaders_debug)
  {
    this.gi = NullCheck.notNull(in_gi, "OpenGL");
    this.shaders_debug = NullCheck.notNull(in_shaders_debug, "Debug shaders");
    this.matrices = KMutableMatrices.newMatrices();
    this.first = true;
  }

  @Override public void rendererDebugEvaluate(
    final KFramebufferRGBAUsableType framebuffer,
    final KVisibleSet visible)
    throws RException
  {
    final JCGLImplementationType in_gi = this.gi;
    final JCGLInterfaceCommonType gc = in_gi.getGLCommon();

    this.buffer =
      KRendererDebugLogDepth.makeFramebuffer(in_gi, this.buffer, framebuffer);
    assert this.buffer != null;

    gc.framebufferDrawBind(this.buffer.f);
    try {
      this.render(visible, in_gi);

      if (this.first) {
        assert this.buffer != null;
        this.buffer.dumpTextures(in_gi);
      }

    } finally {
      this.first = false;
      gc.framebufferDrawUnbind();
    }
  }

  private void render(
    final KVisibleSet visible,
    final JCGLImplementationType in_gi)
    throws RException
  {
    final JCGLInterfaceCommonType gc = in_gi.getGLCommon();
    gc.blendingDisable();
    gc.cullingEnable(
      FaceSelection.FACE_BACK,
      FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);
    gc.colorBufferMask(true, true, true, true);
    gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
    gc.depthBufferWriteEnable();
    gc.depthBufferClear(1.0f);

    final KProgramType program =
      this.shaders_debug.cacheGetLU("show_log_depths");

    final KCamera camera = visible.getCamera();
    this.matrices.withObserver(
      camera.getViewMatrix(),
      camera.getProjection(),
      new KMatricesObserverFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesObserverType mwo)
          throws RException
        {
          final JCBExecutorType exec = program.getExecutable();
          exec.execRun(new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType p)
              throws RException
            {
              KShadingProgramCommon.putMatrixProjection(
                p,
                mwo.getMatrixProjection());
              KShadingProgramCommon.putDepthCoefficient(
                p,
                KRendererCommon.depthCoefficient(mwo.getProjection()));

              final KVisibleSetOpaques opaques = visible.getOpaques();
              final Set<String> group_names = opaques.getGroupNames();
              for (final String name : group_names) {
                final KVisibleSetLightGroup group = opaques.getGroup(name);
                KRendererDebugLogDepth.renderGroup(mwo, in_gi, p, group);
              }
            }
          });

          final DebugGBuffer b = KRendererDebugLogDepth.this.buffer;
          assert b != null;

          b.bindTextures(gc);

          return Unit.unit();
        }
      });
  }

  private static void renderGroup(
    final KMatricesObserverType mwo,
    final JCGLImplementationType gi,
    final JCBProgramType p,
    final KVisibleSetLightGroup group)
    throws RException
  {
    final JCGLInterfaceCommonType gc = gi.getGLCommon();

    final Set<String> codes = group.getMaterialCodes();
    for (final String code : codes) {
      final List<KInstanceOpaqueType> instances = group.getInstances(code);
      for (int index = 0; index < instances.size(); ++index) {
        final KInstanceOpaqueType o = instances.get(index);
        mwo.withInstance(
          o,
          new KMatricesInstanceFunctionType<Unit, RException>() {
            @Override public Unit run(
              final KMatricesInstanceType mwi)
            {
              final KMeshReadableType m = o.instanceGetMesh();
              final ArrayBufferUsableType a = m.meshGetArrayBuffer();
              final IndexBufferUsableType i = m.meshGetIndexBuffer();

              gc.arrayBufferBind(a);
              try {
                KShadingProgramCommon.bindAttributePositionUnchecked(p, a);
                KShadingProgramCommon.putMatrixProjectionReuse(p);
                KShadingProgramCommon.putMatrixModelViewUnchecked(
                  p,
                  mwi.getMatrixModelView());
                KShadingProgramCommon.putDepthCoefficientReuse(p);

                p.programExecute(new JCBProgramProcedureType<RException>() {
                  @Override public void call()
                  {
                    gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, i);
                  }
                });
              } finally {
                gc.arrayBufferUnbind();
              }

              return Unit.unit();
            }
          });
      }
    }
  }

  @Override public String rendererGetName()
  {
    return "debug-log-depth";
  }
}
