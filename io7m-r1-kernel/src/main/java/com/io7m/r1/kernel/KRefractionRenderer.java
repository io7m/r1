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

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.kernel.types.KFramebufferRGBADescription;
import com.io7m.r1.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.r1.kernel.types.KMaterialNormalMapped;
import com.io7m.r1.kernel.types.KMaterialNormalVertex;
import com.io7m.r1.kernel.types.KMaterialNormalVisitorType;
import com.io7m.r1.kernel.types.KMaterialRefractiveMasked;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmasked;
import com.io7m.r1.kernel.types.KMaterialRefractiveVisitorType;
import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.kernel.types.KMeshReadableType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionCache;
import com.io7m.r1.types.RExceptionFramebufferNotBound;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * The default implementation of the refraction renderer interface.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KRefractionRenderer implements
  KRefractionRendererType
{
  private static final String               NAME;
  private static final VectorReadable4FType WHITE;

  static {
    NAME = "refraction";
    WHITE = new VectorI4F(1.0f, 1.0f, 1.0f, 1.0f);
  }

  /**
   * Construct a new refraction renderer.
   *
   * @param gl
   *          The OpenGL implementation
   * @param copier
   *          A region copier
   * @param shader_cache
   *          A shader cache
   * @param rgba_cache
   *          A framebuffer cache
   * @return A new renderer
   *
   * @throws RException
   *           If an error occurs during initialization
   */

  public static KRefractionRendererType newRenderer(
    final JCGLImplementationType gl,
    final KRegionCopierType copier,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KFramebufferRGBAWithDepthCacheType rgba_cache)
    throws RException
  {
    return new KRefractionRenderer(gl, copier, shader_cache, rgba_cache);
  }

  private static void putInstanceAttributes(
    final KMaterialTranslucentRefractive material,
    final ArrayBufferUsableType array,
    final JCBProgramType program)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.bindAttributePositionUnchecked(program, array);

    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.bindAttributeTangent4(program, array);
          KShadingProgramCommon.bindAttributeNormal(program, array);
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.bindAttributeNormal(program, array);
          return Unit.unit();
        }
      });

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
    }
  }

  private static void putInstanceMatrices(
    final JCBProgramType program,
    final KMatricesInstanceValuesType mwi,
    final KMaterialTranslucentRefractive material)
    throws JCGLException
  {
    KShadingProgramCommon.putMatrixModelViewUnchecked(
      program,
      mwi.getMatrixModelView());

    if (material.materialRequiresUVCoordinates()) {
      KShadingProgramCommon.putMatrixUVUnchecked(program, mwi.getMatrixUV());
    }

    KShadingProgramCommon.putMatrixNormal(program, mwi.getMatrixNormal());
  }

  private static
    <F extends KFramebufferRGBAWithDepthUsableType & KFramebufferDepthUsableType>
    void
    putTextures(
      final KMaterialTranslucentRefractive material,
      final KFramebufferRGBAWithDepthUsableType scene,
      final JCBProgramType program,
      final KTextureUnitContextType context)
      throws JCGLException,
        RException
  {
    material.materialGetNormal().normalAccept(
      new KMaterialNormalVisitorType<Unit, JCGLException>() {
        @Override public Unit mapped(
          final KMaterialNormalMapped m)
          throws RException,
            JCGLException
        {
          KShadingProgramCommon.putTextureNormal(
            program,
            context.withTexture2D(m.getTexture()));
          return Unit.unit();
        }

        @Override public Unit vertex(
          final KMaterialNormalVertex m)
          throws RException,
            JCGLException
        {
          return Unit.unit();
        }
      });

    KShadingProgramCommon.putRefractionTextureScene(
      program,
      context.withTexture2D(scene.rgbaGetTexture()));
  }

  private static void rendererRefractionEvaluateForInstanceMasked(
    final JCGLImplementationType g,
    final KFramebufferRGBAWithDepthCacheType rgba_cache,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KRegionCopierType copier,
    final KFramebufferRGBAWithDepthUsableType scene,
    final KFramebufferRGBAWithDepthUsableType scene_copy,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi)
    throws RException,
      JCacheException,
      JCGLException
  {
    final KMeshReadableType mesh = r.instanceGetMesh();

    final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAWithDepthUsableType> scene_mask =
      rgba_cache.bluCacheGet(scene.rgbaGetDescription());

    try {
      final KFramebufferRGBAWithDepthUsableType mask = scene_mask.getValue();

      copier.copierCopyDepthOnly(
        scene,
        scene.kFramebufferGetArea(),
        mask,
        scene.kFramebufferGetArea());

      KRefractionRenderer.rendererRefractionEvaluateRenderMask(
        g,
        shader_cache,
        mask,
        r,
        mi,
        mesh);

      KRefractionRenderer.rendererRefractionEvaluateRenderMasked(
        g,
        shader_cache,
        unit_allocator,
        scene,
        mask,
        scene_copy,
        r,
        mi,
        mesh);

    } finally {
      scene_mask.returnToCache();
    }
  }

  private static void rendererRefractionEvaluateForInstanceUnmasked(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KFramebufferRGBAWithDepthUsableType scene,
    final KFramebufferRGBAWithDepthUsableType temporary,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi)
    throws JCGLException,
      RException,
      JCacheException
  {
    KRefractionRenderer.rendererRefractionEvaluateRenderUnmasked(
      g,
      shader_cache,
      unit_allocator,
      scene,
      temporary,
      r,
      mi,
      r.instanceGetMesh());
  }

  private static void rendererRefractionEvaluateRenderMask(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KFramebufferRGBAWithDepthUsableType scene_mask,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final KMeshReadableType mesh)
    throws RException,
      JCGLException,
      JCacheException
  {
    final KProgramType kprogram = shader_cache.cacheGetLU("refraction_mask");

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          try {
            gc.framebufferDrawBind(scene_mask.rgbaGetColorFramebuffer());

            program.programUniformPutVector4f(
              "f_ccolor",
              KRefractionRenderer.WHITE);

            gc.blendingDisable();

            KRendererCommon.renderConfigureFaceCulling(
              gc,
              r.instanceGetFaceSelection());

            gc.colorBufferMask(true, true, true, true);
            gc.colorBufferClear4f(0.0f, 0.0f, 0.0f, 1.0f);
            gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
            gc.depthBufferWriteDisable();

            KShadingProgramCommon.putMatrixProjectionUnchecked(
              program,
              mi.getMatrixProjection());

            KShadingProgramCommon.putMatrixModelViewUnchecked(
              program,
              mi.getMatrixModelView());

            final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
            final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
            gc.arrayBufferBind(array);
            KShadingProgramCommon.bindAttributePositionUnchecked(
              program,
              array);

            program
              .programExecute(new JCBProgramProcedureType<JCGLException>() {
                @Override public void call()
                  throws JCGLException
                {
                  gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                }
              });

          } finally {
            gc.framebufferDrawUnbind();
          }
        }
      });
  }

  private static void rendererRefractionEvaluateRenderMasked(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KFramebufferRGBAWithDepthUsableType scene,
    final KFramebufferRGBAWithDepthUsableType scene_mask,
    final KFramebufferRGBAWithDepthUsableType scene_copy,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final KMeshReadableType mesh)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KMaterialTranslucentRefractive material = r.getMaterial();
    final String shader_code = material.materialGetCode();
    final KProgramType kprogram = shader_cache.cacheGetLU(shader_code);

    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          gc.framebufferDrawBind(scene.rgbaGetColorFramebuffer());

          gc.blendingDisable();

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            r.instanceGetFaceSelection());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final KTextureUnitContextType context)
              throws JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjectionUnchecked(
                program,
                mi.getMatrixProjection());

              KRefractionRenderer.putInstanceMatrices(program, mi, material);

              KRefractionRenderer.putTextures(
                material,
                scene_copy,
                program,
                context);

              KShadingProgramCommon.putRefractionTextureSceneMask(
                program,
                context.withTexture2D(scene_mask.rgbaGetTexture()));

              material.getRefractive().refractiveAccept(
                new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
                  @Override public Unit masked(
                    final KMaterialRefractiveMasked m)
                    throws RException,
                      JCGLException
                  {
                    KShadingProgramCommon.putMaterialRefractiveMasked(
                      program,
                      m);
                    return Unit.unit();
                  }

                  @Override public Unit unmasked(
                    final KMaterialRefractiveUnmasked m)
                    throws RException,
                      JCGLException
                  {
                    KShadingProgramCommon.putMaterialRefractiveUnmasked(
                      program,
                      m);
                    return Unit.unit();
                  }
                });

              gc.arrayBufferBind(array);
              KRefractionRenderer.putInstanceAttributes(
                material,
                array,
                program);

              program
                .programExecute(new JCBProgramProcedureType<JCGLException>() {
                  @Override public void call()
                    throws JCGLException
                  {
                    gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                  }
                });
            }
          });
        }
      });
  }

  private static void rendererRefractionEvaluateRenderUnmasked(
    final JCGLImplementationType g,
    final KShaderCacheForwardTranslucentUnlitType shader_cache,
    final KTextureUnitAllocator unit_allocator,
    final KFramebufferRGBAWithDepthUsableType scene,
    final KFramebufferRGBAWithDepthUsableType scene_copy,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi,
    final KMeshReadableType mesh)
    throws JCGLException,
      RException,
      JCacheException
  {
    final KMaterialTranslucentRefractive material = r.getMaterial();
    final String shader_code = material.materialGetCode();

    final KProgramType kprogram = shader_cache.cacheGetLU(shader_code);
    final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
    final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();

    final JCGLInterfaceCommonType gc = g.getGLCommon();
    kprogram.getExecutable().execRun(
      new JCBExecutorProcedureType<RException>() {
        @Override public void call(
          final JCBProgramType program)
          throws JCGLException,
            RException
        {
          gc.framebufferDrawBind(scene.rgbaGetColorFramebuffer());

          gc.blendingDisable();

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            r.instanceGetFaceSelection());

          gc.colorBufferMask(true, true, true, true);
          gc.depthBufferTestEnable(DepthFunction.DEPTH_LESS_THAN_OR_EQUAL);
          gc.depthBufferWriteDisable();

          unit_allocator.withContext(new KTextureUnitWithType() {
            @Override public void run(
              final KTextureUnitContextType context)
              throws JCGLException,
                RException
            {
              KShadingProgramCommon.putMatrixProjectionUnchecked(
                program,
                mi.getMatrixProjection());

              KRefractionRenderer.putInstanceMatrices(program, mi, material);

              KRefractionRenderer.putTextures(
                material,
                scene_copy,
                program,
                context);

              material.getRefractive().refractiveAccept(
                new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
                  @Override public Unit masked(
                    final KMaterialRefractiveMasked m)
                    throws RException,
                      JCGLException
                  {
                    KShadingProgramCommon.putMaterialRefractiveMasked(
                      program,
                      m);
                    return Unit.unit();
                  }

                  @Override public Unit unmasked(
                    final KMaterialRefractiveUnmasked m)
                    throws RException,
                      JCGLException
                  {
                    KShadingProgramCommon.putMaterialRefractiveUnmasked(
                      program,
                      m);
                    return Unit.unit();
                  }
                });

              gc.arrayBufferBind(array);
              KRefractionRenderer.putInstanceAttributes(
                material,
                array,
                program);

              program
                .programExecute(new JCBProgramProcedureType<JCGLException>() {
                  @Override public void call()
                    throws JCGLException
                  {
                    gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
                  }
                });
            }

          });
        }
      });
  }

  private final KRegionCopierType                       copier;
  private final KFramebufferRGBAWithDepthCacheType      rgba_cache;
  private final JCGLImplementationType                  g;
  private final KShaderCacheForwardTranslucentUnlitType shader_cache;
  private final KTextureUnitAllocator                   texture_units;

  private KRefractionRenderer(
    final JCGLImplementationType gl,
    final KRegionCopierType in_copier,
    final KShaderCacheForwardTranslucentUnlitType in_shader_cache,
    final KFramebufferRGBAWithDepthCacheType in_forward_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(gl, "OpenGL implementation");
      this.copier = NullCheck.notNull(in_copier, "Copier");
      this.rgba_cache =
        NullCheck.notNull(in_forward_cache, "Forward framebuffer cache");
      this.shader_cache = NullCheck.notNull(in_shader_cache, "Shader cache");

      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public String rendererGetName()
  {
    return KRefractionRenderer.NAME;
  }

  @Override public void rendererRefractionEvaluate(
    final KFramebufferRGBAWithDepthUsableType scene,
    final KMatricesObserverType observer,
    final KInstanceTranslucentRefractive r)
    throws RException
  {
    NullCheck.notNull(scene, "Scene");
    NullCheck.notNull(observer, "Observer");
    NullCheck.notNull(r, "Refractive instance");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      if (gc.framebufferDrawIsBound(scene.rgbaGetColorFramebuffer()) == false) {
        throw new RExceptionFramebufferNotBound("Framebuffer is not bound");
      }

      observer.withInstance(
        r,
        new KMatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final KMatricesInstanceType mi)
            throws RException,
              JCGLException
          {
            try {
              KRefractionRenderer.this.rendererRefractionEvaluateForInstance(
                scene,
                r,
                mi);
            } catch (final JCacheException e) {
              throw RExceptionCache.fromJCacheException(e);
            }
            return Unit.unit();
          }
        });

      if (gc.framebufferDrawIsBound(scene.rgbaGetColorFramebuffer()) == false) {
        throw new RExceptionFramebufferNotBound("Framebuffer is not bound");
      }

    } catch (final JCGLException x) {
      throw RExceptionJCGL.fromJCGLException(x);
    }
  }

  private void rendererRefractionEvaluateForInstance(
    final KFramebufferRGBAWithDepthUsableType scene,
    final KInstanceTranslucentRefractive r,
    final KMatricesInstanceValuesType mi)
    throws JCGLException,
      RException,
      JCacheException
  {
    final JCGLInterfaceCommonType gc = this.g.getGLCommon();
    gc.blendingDisable();

    final BLUCacheReceiptType<KFramebufferRGBADescription, KFramebufferRGBAWithDepthUsableType> temporary =
      this.rgba_cache.bluCacheGet(scene.rgbaGetDescription());

    try {
      final KFramebufferRGBAWithDepthUsableType scene_copy =
        temporary.getValue();

      this.copier.copierCopyRGBAOnly(
        scene,
        scene.kFramebufferGetArea(),
        scene_copy,
        scene.kFramebufferGetArea());

      final KMaterialTranslucentRefractive material = r.getMaterial();

      material.getRefractive().refractiveAccept(
        new KMaterialRefractiveVisitorType<Unit, JCGLException>() {
          @Override public Unit masked(
            final KMaterialRefractiveMasked m)
            throws RException,
              JCGLException
          {
            try {
              KRefractionRenderer
                .rendererRefractionEvaluateForInstanceMasked(
                  KRefractionRenderer.this.g,
                  KRefractionRenderer.this.rgba_cache,
                  KRefractionRenderer.this.shader_cache,
                  KRefractionRenderer.this.texture_units,
                  KRefractionRenderer.this.copier,
                  scene,
                  scene_copy,
                  r,
                  mi);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }

          @Override public Unit unmasked(
            final KMaterialRefractiveUnmasked m)
            throws RException,
              JCGLException
          {
            try {
              KRefractionRenderer
                .rendererRefractionEvaluateForInstanceUnmasked(
                  KRefractionRenderer.this.g,
                  KRefractionRenderer.this.shader_cache,
                  KRefractionRenderer.this.texture_units,
                  scene,
                  scene_copy,
                  r,
                  mi);
              return Unit.unit();
            } catch (final JCacheException e) {
              throw new UnreachableCodeException(e);
            }
          }
        });

    } finally {
      temporary.returnToCache();
    }
  }
}
