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

import java.util.Map;
import java.util.Set;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.DepthFunction;
import com.io7m.jcanephora.FaceSelection;
import com.io7m.jcanephora.FaceWindingOrder;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutorProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceFunctionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

/**
 * A forward renderer for unlit opaque instances.
 */

@EqualityReference public final class KRendererForwardOpaqueUnlit implements
  KRendererForwardOpaqueUnlitType
{
  /**
   * Construct a new opaque renderer.
   *
   * @param in_g
   *          The OpenGL interface.
   * @param in_shader_unlit_cache
   *          The unlit shader cache.
   *
   * @return A new renderer.
   * @throws RException
   *           If an error occurs.
   */

  public static KRendererForwardOpaqueUnlitType newRenderer(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardOpaqueUnlitType in_shader_unlit_cache)
    throws RException
  {
    return new KRendererForwardOpaqueUnlit(in_g, in_shader_unlit_cache);
  }

  static void renderOpaqueUnlitBatch(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitAllocator units,
    final MatricesObserverType mwo,
    final Set<KInstanceOpaqueType> instances,
    final JCBProgramType program)
    throws JCGLException,
      RException
  {
    KShadingProgramCommon.putMatrixProjectionUnchecked(
      program,
      mwo.getMatrixProjection());

    for (final KInstanceOpaqueType instance : instances) {
      assert instance != null;

      mwo.withInstance(
        instance,
        new MatricesInstanceFunctionType<Unit, JCGLException>() {
          @Override public Unit run(
            final MatricesInstanceType mwi)
            throws JCGLException,
              RException
          {
            units.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType context)
                throws JCGLException,
                  RException
              {
                KRendererForwardOpaqueCommon.renderOpaqueInstance(
                  gc,
                  context,
                  mwi,
                  program,
                  instance);
              }
            });
            return Unit.unit();
          }
        });
    }
  }

  private final JCGLImplementationType             g;
  private final KShaderCacheForwardOpaqueUnlitType shader_unlit_cache;
  private final KTextureUnitAllocator              texture_units;

  private KRendererForwardOpaqueUnlit(
    final JCGLImplementationType in_g,
    final KShaderCacheForwardOpaqueUnlitType in_shader_unlit_cache)
    throws RException
  {
    try {
      this.g = NullCheck.notNull(in_g, "GL");
      this.texture_units =
        KTextureUnitAllocator.newAllocator(this.g.getGLCommon());
      this.shader_unlit_cache =
        NullCheck.notNull(in_shader_unlit_cache, "Shader unlit cache");
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    }
  }

  @Override public void rendererEvaluateOpaqueUnlit(
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final boolean depth_write,
    final MatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> batches)
    throws RException
  {
    NullCheck.notNull(shadow_context, "Shadow context");
    NullCheck.notNull(depth_function, "Depth function");
    NullCheck.notNull(mwo, "Matrices");
    NullCheck.notNull(batches, "Batches");

    try {
      final JCGLInterfaceCommonType gc = this.g.getGLCommon();

      gc.blendingDisable();
      gc.colorBufferMask(true, true, true, true);
      gc.cullingEnable(
        FaceSelection.FACE_BACK,
        FaceWindingOrder.FRONT_FACE_COUNTER_CLOCKWISE);

      if (depth_write) {
        gc.depthBufferWriteEnable();
      } else {
        gc.depthBufferWriteDisable();
      }

      depth_function
        .acceptPartial(new OptionPartialVisitorType<DepthFunction, Unit, JCGLException>() {
          @Override public Unit none(
            final None<DepthFunction> n)
            throws JCGLException
          {
            gc.depthBufferTestDisable();
            return Unit.unit();
          }

          @Override public Unit some(
            final Some<DepthFunction> s)
            throws JCGLException
          {
            gc.depthBufferTestEnable(s.get());
            return Unit.unit();
          }
        });

      for (final String material_code : batches.keySet()) {
        assert material_code != null;

        final Set<KInstanceOpaqueType> instances = batches.get(material_code);
        assert instances != null;
        assert instances.isEmpty() == false;

        final KTextureUnitAllocator units = this.texture_units;
        final KProgramType kprogram =
          this.shader_unlit_cache.cacheGetLU(material_code);

        kprogram.getExecutable().execRun(
          new JCBExecutorProcedureType<RException>() {
            @Override public void call(
              final JCBProgramType program)
              throws JCGLException,
                RException
            {
              KRendererForwardOpaqueUnlit.renderOpaqueUnlitBatch(
                gc,
                units,
                mwo,
                instances,
                program);
            }
          });
      }

    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
    } catch (final JCacheException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
