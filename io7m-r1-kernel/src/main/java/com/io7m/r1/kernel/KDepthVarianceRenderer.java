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

import java.util.List;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ClearSpecification;
import com.io7m.jcanephora.ClearSpecificationBuilderType;
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
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KDepthInstancesType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueRegular;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.r1.kernel.types.KMaterialAlbedoTextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.r1.kernel.types.KMaterialAlbedoVisitorType;
import com.io7m.r1.kernel.types.KMaterialDepthAlpha;
import com.io7m.r1.kernel.types.KMaterialDepthConstant;
import com.io7m.r1.kernel.types.KMaterialDepthType;
import com.io7m.r1.kernel.types.KMaterialDepthVisitorType;
import com.io7m.r1.kernel.types.KMaterialOpaqueRegular;
import com.io7m.r1.kernel.types.KMaterialOpaqueType;
import com.io7m.r1.kernel.types.KMaterialOpaqueVisitorType;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * The default depth-variance renderer implementation.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KDepthVarianceRenderer implements
  KDepthVarianceRendererType
{
  private static final String             NAME;
  private static final ClearSpecification CLEAR_SPECIFICATION;

  static {
    NAME = "depth-variance";

    final ClearSpecificationBuilderType cbd = ClearSpecification.newBuilder();
    cbd.enableColorBufferClear4f(1.0f, 1.0f, 1.0f, 1.0f);
    cbd.enableDepthBufferClear(1.0f);
    cbd.enableStencilBufferClear(1);
    cbd.setStrictChecking(false);
    CLEAR_SPECIFICATION = cbd.build();
  }

  /**
   * Construct a new depth renderer.
   *
   * @param g
   *          The OpenGL implementation
   * @param shader_cache
   *          The shader cache
   *
   * @return A new depth renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KDepthVarianceRendererType newRenderer(
    final JCGLImplementationType g,
    final KShaderCacheDepthVarianceType shader_cache)
    throws RException
  {
    return new KDepthVarianceRenderer(g, shader_cache);
  }

  private static void renderDepthPassBatch(
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final JCBProgramType jp,
    final List<KInstanceOpaqueType> batch,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    for (final KInstanceOpaqueType i : batch) {
      assert i != null;

      mwo.withInstance(
        i,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mwi)
            throws JCGLException,
              RException
          {
            KDepthVarianceRenderer.renderDepthPassInstance(
              gc,
              mwi,
              jp,
              i,
              faces);
            return Unit.unit();
          }
        });
    }
  }

  private static void renderDepthPassInstance(
    final JCGLInterfaceCommonType gc,
    final KMatricesInstanceValuesType mwi,
    final JCBProgramType jp,
    final KInstanceOpaqueType i,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException
  {
    final KMaterialOpaqueType material =
      i
        .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueType, RException>() {
          @Override public KMaterialOpaqueType regular(
            final KInstanceOpaqueRegular o)
            throws RException
          {
            return o.getMaterial();
          }
        });

    final List<TextureUnitType> units = gc.textureGetUnits();

    /**
     * Associate array attributes with program attributes, and draw mesh.
     */

    try {
      final KMeshReadableType mesh = i.instanceGetMesh();
      final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
      final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

      gc.arrayBufferBind(array);
      KShadingProgramCommon.bindAttributePositionUnchecked(jp, array);

      /**
       * Upload matrices.
       */

      KShadingProgramCommon.putMatrixProjectionReuse(jp);
      KShadingProgramCommon.putMatrixModelViewUnchecked(
        jp,
        mwi.getMatrixModelView());

      final KMaterialDepthType depth = material.materialOpaqueGetDepth();
      depth.depthAccept(new KMaterialDepthVisitorType<Unit, JCGLException>() {
        @Override public Unit alpha(
          final KMaterialDepthAlpha mda)
          throws RException,
            JCGLException
        {
          /**
           * Material is alpha-to-depth; must upload albedo texture, matrices,
           * and threshold value.
           */

          return material
            .opaqueAccept(new KMaterialOpaqueVisitorType<Unit, JCGLException>() {
              @Override public Unit materialOpaqueRegular(
                final KMaterialOpaqueRegular mor)
                throws RException,
                  JCGLException
              {
                return mor.materialRegularGetAlbedo().albedoAccept(
                  new KMaterialAlbedoVisitorType<Unit, JCGLException>() {
                    @Override public Unit textured(
                      final KMaterialAlbedoTextured mat)
                      throws RException,
                        JCGLException
                    {
                      KShadingProgramCommon.putMatrixUVUnchecked(
                        jp,
                        mwi.getMatrixUV());
                      KShadingProgramCommon.putMaterialAlphaDepthThreshold(
                        jp,
                        mda.getAlphaThreshold());

                      KShadingProgramCommon
                        .putMaterialAlbedoTextured(jp, mat);

                      final TextureUnitType u = units.get(0);
                      assert u != null;
                      KShadingProgramCommon.bindPutTextureAlbedo(
                        jp,
                        gc,
                        mat,
                        u);

                      KShadingProgramCommon.bindAttributeUVUnchecked(
                        jp,
                        array);
                      return Unit.unit();
                    }

                    @Override public Unit untextured(
                      final KMaterialAlbedoUntextured mau)
                    {
                      /**
                       * Unreachable because material verification does not
                       * allow untextured albedo with alpha-to-depth.
                       */

                      throw new UnreachableCodeException();
                    }
                  });
              }
            });
        }

        @Override public Unit constant(
          final KMaterialDepthConstant m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

      /**
       * If there's an override for face culling specified, use it. Otherwise,
       * use the per-instance face culling settings.
       */

      if (faces.isNone()) {
        KRendererCommon.renderConfigureFaceCulling(
          gc,
          i.instanceGetFaceSelection());
      } else {
        final Some<KFaceSelection> some = (Some<KFaceSelection>) faces;
        KRendererCommon.renderConfigureFaceCulling(gc, some.get());
      }

      jp.programExecute(new JCBProgramProcedureType<JCGLException>() {
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

  private final JCGLImplementationType        g;
  private final KMutableMatrices              matrices;
  private final KShaderCacheDepthVarianceType shader_cache;

  private KDepthVarianceRenderer(
    final JCGLImplementationType gl,
    final KShaderCacheDepthVarianceType in_shader_cache)
  {
    this.g = NullCheck.notNull(gl, "OpenGL implementation");
    this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");
    this.matrices = KMutableMatrices.newMatrices();
  }

  private void renderDepthPassBatches(
    final KDepthInstancesType instances,
    final JCGLInterfaceCommonType gc,
    final KMatricesObserverType mwo,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException,
      JCacheException
  {
    for (final String depth_code : instances.getMaterialCodes()) {
      assert depth_code != null;

      final List<KInstanceOpaqueType> batch =
        instances.getInstancesForMaterial(depth_code);
      assert batch != null;

      final KProgramType program = this.shader_cache.cacheGetLU(depth_code);
      final JCBExecutorType exec = program.getExecutable();

      exec.execRun(new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType jp)
          throws JCGLException,
            RException
        {
          KShadingProgramCommon.putMatrixProjectionUnchecked(
            jp,
            mwo.getMatrixProjection());
          KDepthVarianceRenderer.renderDepthPassBatch(
            gc,
            mwo,
            jp,
            batch,
            faces);
        }
      });
    }
  }

  @Override public void rendererEvaluateDepthVariance(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final KFramebufferDepthVarianceUsableType framebuffer,
    final OptionType<KFaceSelection> faces)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(projection, "Projection matrix");
    NullCheck.notNull(instances, "Instances");
    NullCheck.notNull(framebuffer, "Framebuffer");
    NullCheck.notNull(faces, "Faces");

    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    final FramebufferUsableType fb =
      framebuffer.kFramebufferGetDepthVariancePassFramebuffer();

    gc.framebufferDrawBind(fb);
    try {
      this.rendererEvaluateDepthVarianceWithBoundFramebuffer(
        view,
        projection,
        instances,
        framebuffer.kFramebufferGetArea(),
        faces);
    } finally {
      gc.framebufferDrawUnbind();
    }
  }

  @Override public void rendererEvaluateDepthVarianceWithBoundFramebuffer(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces)
    throws RException
  {
    NullCheck.notNull(view, "View matrix");
    NullCheck.notNull(projection, "Projection matrix");
    NullCheck.notNull(instances, "Instances");
    NullCheck.notNull(framebuffer_area, "Framebuffer area");
    NullCheck.notNull(faces, "Faces");

    this.matrices.withObserver(
      view,
      projection,
      new KMatricesObserverFunctionType<Unit, JCGLException>() {
        @Override public Unit run(
          final KMatricesObserverType mwo)
          throws RException,
            JCGLException
        {
          try {
            KDepthVarianceRenderer.this.renderScene(
              instances,
              framebuffer_area,
              mwo,
              faces);
            return Unit.unit();
          } catch (final JCacheException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
  }

  @Override public String rendererGetName()
  {
    return KDepthVarianceRenderer.NAME;
  }

  private void renderScene(
    final KDepthInstancesType instances,
    final AreaInclusive framebuffer_area,
    final KMatricesObserverType mwo,
    final OptionType<KFaceSelection> faces)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();

    gc.blendingDisable();
    gc.colorBufferMask(true, true, true, true);
    gc.cullingDisable();
    gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN);
    gc.depthBufferWriteEnable();
    gc.scissorDisable();
    gc.viewportSet(framebuffer_area);
    gc.clear(KDepthVarianceRenderer.CLEAR_SPECIFICATION);

    this.renderDepthPassBatches(instances, gc, mwo, faces);
  }
}
