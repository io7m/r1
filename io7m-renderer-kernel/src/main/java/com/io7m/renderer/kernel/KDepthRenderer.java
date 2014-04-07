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
import java.util.Map;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Option.Some;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcache.JCacheException;
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
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverFunctionType;
import com.io7m.renderer.kernel.KMutableMatricesType.MatricesObserverType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialOpaqueAlphaDepth;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialOpaqueType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The default depth renderer implementation.
 */

@SuppressWarnings("synthetic-access") public final class KDepthRenderer implements
  KDepthRendererType
{
  private static enum InternalDepthLabel
  {
    DEPTH_INTERNAL_CONSTANT("depth_C"),
    DEPTH_INTERNAL_CONSTANT_PACKED4444("depth_CP4"),
    DEPTH_INTERNAL_MAPPED("depth_M"),
    DEPTH_INTERNAL_MAPPED_PACKED4444("depth_MP4"),
    DEPTH_INTERNAL_UNIFORM("depth_U"),
    DEPTH_INTERNAL_UNIFORM_PACKED4444("depth_UP4");

    static @Nonnull InternalDepthLabel fromDepthLabel(
      final @Nonnull KGraphicsCapabilities caps,
      final @Nonnull KMaterialDepthLabel label)
    {
      switch (label) {
        case DEPTH_CONSTANT:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_CONSTANT;
          }
          return DEPTH_INTERNAL_CONSTANT_PACKED4444;
        }
        case DEPTH_MAPPED:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_MAPPED;
          }
          return DEPTH_INTERNAL_MAPPED_PACKED4444;
        }
        case DEPTH_UNIFORM:
        {
          if (caps.getSupportsDepthTextures()) {
            return DEPTH_INTERNAL_UNIFORM;
          }
          return DEPTH_INTERNAL_UNIFORM_PACKED4444;
        }
      }

      throw new UnreachableCodeException();
    }

    private final @Nonnull String name;

    private InternalDepthLabel(
      final @Nonnull String in_name)
    {
      this.name = in_name;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }
  }

  private static final @Nonnull String NAME;

  static {
    NAME = "depth";
  }

  /**
   * Construct a new depth renderer.
   * 
   * @param g
   *          The OpenGL implementation
   * @param shader_cache
   *          The shader cache
   * @param log
   *          A log handle
   * @return A new depth renderer
   * @throws RException
   *           If an error occurs during initialization
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KDepthRendererType newRenderer(
    final @Nonnull JCGLImplementation g,
    final @Nonnull KShaderCacheType shader_cache,
    final @Nonnull Log log)
    throws RException,
      ConstraintError
  {
    return new KDepthRenderer(g, shader_cache, log);
  }

  private static void putMaterialAlphaDepth(
    final @Nonnull JCBProgram jp,
    final @Nonnull KMaterialOpaqueType material)
    throws RException,
      ConstraintError,
      JCGLException
  {
    material
      .materialOpaqueVisitableAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
        @Override public Unit materialVisitOpaqueAlphaDepth(
          final @Nonnull KMaterialOpaqueAlphaDepth m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(
            jp,
            m.getAlphaThreshold());
          return Unit.unit();
        }

        @Override public Unit materialVisitOpaqueRegular(
          final @Nonnull KMaterialOpaqueRegular m)
          throws ConstraintError,
            RException,
            JCGLException
        {
          KShadingProgramCommon.putMaterialAlphaDepthThreshold(jp, 0.0f);
          return Unit.unit();
        }
      });
  }

  private static void renderDepthPassBatch(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesObserverType mwo,
    final @Nonnull JCBProgram jp,
    final @Nonnull InternalDepthLabel label,
    final @Nonnull List<KInstanceTransformedOpaqueType> batch,
    final @Nonnull Option<KFaceSelection> faces)
    throws ConstraintError,
      JCGLException,
      RException
  {
    for (final KInstanceTransformedOpaqueType i : batch) {
      mwo.withInstance(
        i,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesInstanceType mwi)
            throws JCGLException,
              ConstraintError,
              RException
          {
            KDepthRenderer.renderDepthPassInstance(
              gc,
              mwi,
              jp,
              label,
              i,
              faces);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderDepthPassInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull MatricesInstanceType mwi,
    final @Nonnull JCBProgram jp,
    final @Nonnull InternalDepthLabel label,
    final @Nonnull KInstanceTransformedOpaqueType i,
    final @Nonnull Option<KFaceSelection> faces)
    throws JCGLException,
      ConstraintError,
      RException
  {
    final KMaterialOpaqueType material =
      i.instanceGet().instanceGetMaterial();
    final List<TextureUnit> units = gc.textureGetUnits();

    /**
     * Upload matrices.
     */

    KShadingProgramCommon.putMatrixProjectionReuse(jp);
    KShadingProgramCommon.putMatrixModelViewUnchecked(jp, mwi.getMatrixModelView());

    switch (label) {
      case DEPTH_INTERNAL_CONSTANT:
      case DEPTH_INTERNAL_CONSTANT_PACKED4444:
      {
        break;
      }
      case DEPTH_INTERNAL_MAPPED:
      case DEPTH_INTERNAL_MAPPED_PACKED4444:
      {
        KShadingProgramCommon.putMaterialAlbedo(
          jp,
          material.materialGetAlbedo());
        KDepthRenderer.putMaterialAlphaDepth(jp, material);
        KShadingProgramCommon.putMatrixUV(jp, mwi.getMatrixUV());
        KShadingProgramCommon.bindPutTextureAlbedo(
          jp,
          gc,
          material.materialGetAlbedo(),
          units.get(0));
        break;
      }
      case DEPTH_INTERNAL_UNIFORM:
      case DEPTH_INTERNAL_UNIFORM_PACKED4444:
      {
        KShadingProgramCommon.putMaterialAlbedo(
          jp,
          material.materialGetAlbedo());
        KDepthRenderer.putMaterialAlphaDepth(jp, material);
        break;
      }
    }

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KInstanceOpaqueType actual = i.instanceGet();
      final KMesh mesh = actual.instanceGetMesh();
      final ArrayBuffer array = mesh.getArrayBuffer();
      final IndexBuffer indices = mesh.getIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(jp, array);

      switch (label) {
        case DEPTH_INTERNAL_CONSTANT:
        case DEPTH_INTERNAL_CONSTANT_PACKED4444:
        case DEPTH_INTERNAL_UNIFORM:
        case DEPTH_INTERNAL_UNIFORM_PACKED4444:
        {
          break;
        }
        case DEPTH_INTERNAL_MAPPED:
        case DEPTH_INTERNAL_MAPPED_PACKED4444:
        {
          KShadingProgramCommon.bindAttributeUVUnchecked(jp, array);
          break;
        }
      }

      /**
       * If there's an override for face culling specified, use it. Otherwise,
       * use the per-instance face culling settings.
       */

      if (faces.isNone()) {
        KRendererCommon.renderConfigureFaceCulling(
          gc,
          actual.instanceGetFaces());
      } else {
        final Some<KFaceSelection> some = (Option.Some<KFaceSelection>) faces;
        KRendererCommon.renderConfigureFaceCulling(gc, some.value);
      }

      jp.programExecute(new JCBProgramProcedure() {
        @Override public void call()
          throws ConstraintError,
            JCGLException
        {
          gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
        }
      });

    } finally {
      gc.arrayBufferUnbind();
    }
  }

  private final @Nonnull KGraphicsCapabilities caps;
  private boolean                              closed;
  private final @Nonnull JCGLImplementation    g;
  private final @Nonnull Log                   log;
  private final @Nonnull KMutableMatricesType  matrices;

  private final @Nonnull KShaderCacheType      shader_cache;

  private KDepthRenderer(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull KShaderCacheType in_shader_cache,
    final @Nonnull Log in_log)
    throws ConstraintError,
      RException
  {
    try {
      this.log =
        new Log(Constraints.constrainNotNull(in_log, "log"), "depth-renderer");
      this.g = Constraints.constrainNotNull(gl, "OpenGL implementation");
      this.shader_cache =
        Constraints.constrainNotNull(in_shader_cache, "Shader cache");

      this.caps = KGraphicsCapabilities.getCapabilities(gl);
      this.matrices = KMutableMatricesType.newMatrices();

      if (this.log.enabled(Level.LOG_DEBUG)) {
        this.log.debug("initialized");
      }
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private void renderConfigureDepthColorMasks(
    final @Nonnull JCGLInterfaceCommon gc)
    throws ConstraintError,
      JCGLException
  {
    if (this.caps.getSupportsDepthTextures()) {
      gc.colorBufferMask(false, false, false, false);
    } else {
      gc.colorBufferMask(true, true, true, true);
    }
  }

  private
    void
    renderDepthPassBatches(
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull JCGLInterfaceCommon gc,
      final @Nonnull MatricesObserverType mwo,
      final @Nonnull Option<KFaceSelection> faces)
      throws ConstraintError,
        JCacheException,
        JCGLException,
        RException
  {
    for (final KMaterialDepthLabel depth : batches.keySet()) {
      final InternalDepthLabel label =
        InternalDepthLabel.fromDepthLabel(this.caps, depth);

      final List<KInstanceTransformedOpaqueType> batch = batches.get(depth);
      final KProgram program = this.shader_cache.cacheGetLU(label.getName());
      final JCBExecutionAPI exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedure() {
        @Override public void call(
          final @Nonnull JCBProgram jp)
          throws ConstraintError,
            JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            jp,
            mwo.getMatrixProjection());
          KDepthRenderer.renderDepthPassBatch(
            gc,
            mwo,
            jp,
            label,
            batch,
            faces);
        }
      });
    }
  }

  @Override public void rendererClose()
    throws RException,
      ConstraintError
  {
    Constraints.constrainArbitrary(
      this.closed == false,
      "Renderer is not closed");
    this.closed = true;

    if (this.log.enabled(Level.LOG_DEBUG)) {
      this.log.debug("closed");
    }
  }

  @Override public
    void
    rendererEvaluateDepth(
      final @Nonnull RMatrixI4x4F<RTransformViewType> view,
      final @Nonnull RMatrixI4x4F<RTransformProjectionType> projection,
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull KFramebufferDepthUsableType framebuffer,
      final @Nonnull Option<KFaceSelection> faces)
      throws ConstraintError,
        RException
  {
    Constraints.constrainNotNull(view, "View matrix");
    Constraints.constrainNotNull(projection, "Projection matrix");
    Constraints.constrainNotNull(batches, "Batches");
    Constraints.constrainNotNull(framebuffer, "Framebuffer");
    Constraints.constrainNotNull(faces, "Faces");
    Constraints.constrainArbitrary(
      this.rendererIsClosed() == false,
      "Renderer not closed");

    try {
      this.matrices.withObserver(
        view,
        projection,
        new MatricesObserverFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final @Nonnull MatricesObserverType mwo)
            throws ConstraintError,
              RException,
              JCGLException
          {
            KDepthRenderer.this.renderScene(batches, framebuffer, mwo, faces);
            return Unit.unit();
          }
        });
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KDepthRenderer.NAME;
  }

  @Override public boolean rendererIsClosed()
  {
    return this.closed;
  }

  private
    void
    renderScene(
      final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> batches,
      final @Nonnull KFramebufferDepthUsableType framebuffer,
      final @Nonnull MatricesObserverType mwo,
      final @Nonnull Option<KFaceSelection> faces)
      throws JCGLException,
        ConstraintError,
        RException
  {
    final JCGLInterfaceCommon gc = this.g.getGLCommon();

    final FramebufferReferenceUsable fb =
      framebuffer.kFramebufferGetDepthPassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      final AreaInclusive area = framebuffer.kFramebufferGetArea();

      gc.blendingDisable();

      gc.colorBufferMask(true, true, true, true);
      gc.colorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);

      gc.cullingDisable();

      gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
      gc.depthBufferWriteEnable();
      gc.depthBufferClear(1.0f);

      gc.viewportSet(area);

      this.renderConfigureDepthColorMasks(gc);
      this.renderDepthPassBatches(batches, gc, mwo, faces);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }
}
