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

package com.io7m.r1.examples;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KPostprocessorEmission;
import com.io7m.r1.kernel.KRendererDeferredControlType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KShaderCacheDebugType;
import com.io7m.r1.kernel.KShaderCacheDeferredGeometryType;
import com.io7m.r1.kernel.KShaderCacheDeferredLightType;
import com.io7m.r1.kernel.KShaderCacheDepthType;
import com.io7m.r1.kernel.KShaderCacheDepthVarianceType;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.r1.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.r1.kernel.KShaderCachePostprocessingType;
import com.io7m.r1.kernel.types.KUnitQuadCache;
import com.io7m.r1.kernel.types.KUnitQuadCacheType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.types.RException;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredWithEmission extends
  AbstractExampleDeferredRenderer
{
  /**
   * @return A renderer constructor.
   */

  public static ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorDeferredType() {
      @Override public <A, E extends Exception> A matchConstructor(
        final ExampleRendererConstructorVisitorType<A, E> v)
        throws E,
          RException,
          JCGLException
      {
        return v.deferred(this);
      }

      @Override public
        ExampleRendererDeferredType
        newRenderer(
          final LogUsableType log,
          final KShaderCacheDebugType in_shader_debug_cache,
          final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
          final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
          final KShaderCacheDepthType in_shader_depth_cache,
          final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
          final KShaderCachePostprocessingType in_shader_postprocessing_cache,
          final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
          final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
          final JCGLImplementationType gi)
          throws JCGLException,
            RException
      {
        return ExampleRendererDeferredWithEmission.make(
          log,
          in_shader_debug_cache,
          in_shader_translucent_lit_cache,
          in_shader_translucent_unlit_cache,
          in_shader_depth_cache,
          in_shader_depth_variance_cache,
          in_shader_postprocessing_cache,
          in_shader_deferred_geo_cache,
          in_shader_deferred_light_cache,
          gi);
      }
    };
  }

  /**
   * @return The renderer name.
   */

  public static ExampleRendererName getName()
  {
    final String name =
      ExampleRendererDeferredWithEmission.class.getCanonicalName();
    return new ExampleRendererName(NullCheck.notNull(name));
  }

  protected static
    ExampleRendererDeferredType
    make(
      final LogUsableType log,
      final KShaderCacheDebugType in_shader_debug_cache,
      final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
      final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
      final KShaderCacheDepthType in_shader_depth_cache,
      final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
      final KShaderCachePostprocessingType in_shader_postprocessing_cache,
      final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
      final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
      final JCGLImplementationType gi)
      throws JCGLException,
        RException
  {
    final ExampleRendererConstructorType rc =
      ExampleRendererDeferredDefault.get();
    final ExampleRendererConstructorDeferredType rdc =
      (ExampleRendererConstructorDeferredType) rc;
    final ExampleRendererDeferredType r =
      rdc.newRenderer(
        log,
        in_shader_debug_cache,
        in_shader_translucent_lit_cache,
        in_shader_translucent_unlit_cache,
        in_shader_depth_cache,
        in_shader_depth_variance_cache,
        in_shader_postprocessing_cache,
        in_shader_deferred_geo_cache,
        in_shader_deferred_light_cache,
        gi);

    final KUnitQuadCacheType quad_cache =
      KUnitQuadCache.newCache(gi.getGLCommon(), log);
    final KPostprocessorEmission p =
      KPostprocessorEmission.postprocessorNew(
        gi,
        quad_cache,
        in_shader_postprocessing_cache);

    final KRendererDeferredType r_with_e = new KRendererDeferredType() {
      @Override public
        void
        rendererDeferredEvaluate(
          final KFramebufferDeferredUsableType framebuffer,
          final KVisibleSet scene,
          final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
          throws RException
      {
        r.rendererDeferredEvaluate(framebuffer, scene, procedure);
        p
          .postprocessorEvaluateDeferred(
            Unit.unit(),
            framebuffer,
            framebuffer);
      }

      @Override public void rendererDeferredEvaluateFull(
        final KFramebufferDeferredUsableType framebuffer,
        final KVisibleSet scene)
        throws RException
      {
        r.rendererDeferredEvaluateFull(framebuffer, scene);
        p
          .postprocessorEvaluateDeferred(
            Unit.unit(),
            framebuffer,
            framebuffer);
      }

      @Override public String rendererGetName()
      {
        final String name =
          ExampleRendererDeferredWithEmission.class.getCanonicalName();
        return NullCheck.notNull(name);
      }
    };

    return new ExampleRendererDeferredWithEmission(r_with_e);
  }

  private final KRendererDeferredType actual;

  private ExampleRendererDeferredWithEmission(
    final KRendererDeferredType r)
  {
    super(r);
    this.actual = r;
  }

  @Override public <T> T rendererAccept(
    final ExampleRendererVisitorType<T> v)
    throws RException
  {
    return v.visitDeferred(this);
  }

  @Override public
    void
    rendererDeferredEvaluate(
      final KFramebufferDeferredUsableType framebuffer,
      final KVisibleSet scene,
      final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
      throws RException
  {
    this.actual.rendererDeferredEvaluate(framebuffer, scene, procedure);
  }

  @Override public void rendererDeferredEvaluateFull(
    final KFramebufferDeferredUsableType framebuffer,
    final KVisibleSet scene)
    throws RException
  {
    this.actual.rendererDeferredEvaluateFull(framebuffer, scene);
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    return new ExampleRendererName(
      ExampleRendererDeferredWithEmission.class.getCanonicalName());
  }

  @Override public String rendererGetName()
  {
    return this.exampleRendererGetName().toString();
  }
}
