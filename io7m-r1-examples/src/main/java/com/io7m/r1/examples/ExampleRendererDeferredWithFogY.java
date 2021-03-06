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
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KFogProgression;
import com.io7m.r1.kernel.KFogYParameters;
import com.io7m.r1.kernel.KFogYParametersBuilderType;
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KImageFilterDeferredType;
import com.io7m.r1.kernel.KRendererDeferredControlType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredWithFogY implements
  ExampleRendererDeferredType
{
  private static final PMatrixM4x4F<RSpaceEyeType, RSpaceClipType> TEMPORARY;

  static {
    TEMPORARY = new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>();
  }

  /**
   * @return A renderer constructor.
   */

  public static ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorType() {
      @SuppressWarnings("synthetic-access") @Override public
        ExampleRendererType
        newRenderer(
          final LogUsableType log,
          final KShaderCacheSetType caches,
          final JCGLImplementationType gi)
          throws JCGLException,
            RException
      {
        return ExampleRendererDeferredWithFogY.make(log, caches, gi);
      }
    };
  }

  /**
   * @return The renderer name.
   */

  public static ExampleRendererName getName()
  {
    final String name = ExampleRendererDeferredWithFogY.class.getSimpleName();
    return new ExampleRendererName(NullCheck.notNull(name));
  }

  private static ExampleRendererDeferredType make(
    final LogUsableType log,
    final KShaderCacheSetType in_cache,
    final JCGLImplementationType gi)
    throws JCGLException,
      RException
  {
    final R1BuilderType b = R1.newBuilder(gi, log);
    b.setShaderCacheSet(in_cache);
    final R1Type r = b.build();
    final KRendererDeferredType dr = r.getRendererDeferred();
    final KImageFilterDeferredType<KFogYParameters> p = r.getFilterFogY();

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> in_view =
      PMatrixI4x4F.identity();

    final KProjectionFrustum projection =
      KProjectionFrustum.newProjection(
        ExampleRendererDeferredWithFogY.TEMPORARY,
        0.0f,
        1.0f,
        0.0f,
        1.0f,
        1.0f,
        100.0f);

    final PVectorI3F<RSpaceRGBType> grey =
      new PVectorI3F<RSpaceRGBType>(0.2f, 0.2f, 0.2f);
    final KFogYParametersBuilderType fog_b =
      KFogYParameters.newBuilder(in_view, projection);

    final float lower = 2.0f;
    final float upper = 0.0f;
    fog_b.setProgression(KFogProgression.FOG_LOGARITHMIC);

    final KRendererDeferredType r_with_e = new KRendererDeferredType() {
      @Override public
        void
        rendererDeferredEvaluate(
          final KFramebufferDeferredUsableType framebuffer,
          final KVisibleSet scene,
          final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
          throws RException
      {
        dr.rendererDeferredEvaluate(framebuffer, scene, procedure);

        final KCamera camera = scene.getCamera();
        fog_b.setProjection(camera.getProjection());
        fog_b.setLowerY(lower);
        fog_b.setUpperY(upper);
        fog_b.setColor(grey);
        fog_b.setViewMatrix(camera.getViewMatrix());
        fog_b.setProjection(camera.getProjection());
        final KFogYParameters config = fog_b.build();
        p.filterEvaluateDeferred(config, framebuffer, framebuffer);
      }

      @Override public void rendererDeferredEvaluateFull(
        final KFramebufferDeferredUsableType framebuffer,
        final KVisibleSet scene)
        throws RException
      {
        dr.rendererDeferredEvaluateFull(framebuffer, scene);

        final KCamera camera = scene.getCamera();
        fog_b.setProjection(camera.getProjection());
        fog_b.setLowerY(lower);
        fog_b.setUpperY(upper);
        fog_b.setColor(grey);
        fog_b.setViewMatrix(camera.getViewMatrix());
        fog_b.setProjection(camera.getProjection());
        final KFogYParameters config = fog_b.build();

        p.filterEvaluateDeferred(config, framebuffer, framebuffer);
      }

      @Override public String rendererGetName()
      {
        final String name =
          ExampleRendererDeferredWithFogY.class.getCanonicalName();
        return NullCheck.notNull(name);
      }
    };

    return new ExampleRendererDeferredWithFogY(r, r_with_e);
  }

  private final KRendererDeferredType actual;
  private final R1Type                r1;

  private ExampleRendererDeferredWithFogY(
    final R1Type in_r1,
    final KRendererDeferredType rd)
  {
    this.r1 = in_r1;
    this.actual = rd;
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    final String name = ExampleRendererDeferredWithFogY.class.getSimpleName();
    assert name != null;
    return new ExampleRendererName(name);
  }

  @Override public R1Type getR1()
  {
    return this.r1;
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

  @Override public String rendererGetName()
  {
    return this.exampleRendererGetName().toString();
  }
}
