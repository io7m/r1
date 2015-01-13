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
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFXAAParameters.Quality;
import com.io7m.r1.kernel.KFXAAParametersBuilderType;
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KImageFilterRGBAType;
import com.io7m.r1.kernel.KRendererDeferredControlType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredWithFXAA implements
  ExampleRendererDeferredType
{
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
        return ExampleRendererDeferredWithFXAA.make(log, caches, gi);
      }
    };
  }

  /**
   * @return The renderer name.
   */

  public static ExampleRendererName getName()
  {
    final String name = ExampleRendererDeferredWithFXAA.class.getSimpleName();
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
    final KImageFilterRGBAType<KFXAAParameters> p = r.getFilterFXAA();

    final KFXAAParametersBuilderType fxb = KFXAAParameters.newBuilder();
    fxb.setQuality(Quality.QUALITY_39);
    final KFXAAParameters fx = fxb.build();

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
        p.filterEvaluateRGBA(fx, framebuffer, framebuffer);
      }

      @Override public void rendererDeferredEvaluateFull(
        final KFramebufferDeferredUsableType framebuffer,
        final KVisibleSet scene)
        throws RException
      {
        dr.rendererDeferredEvaluateFull(framebuffer, scene);
        p.filterEvaluateRGBA(fx, framebuffer, framebuffer);
      }

      @Override public String rendererGetName()
      {
        final String name =
          ExampleRendererDeferredWithFXAA.class.getCanonicalName();
        return NullCheck.notNull(name);
      }
    };

    return new ExampleRendererDeferredWithFXAA(r, r_with_e);
  }

  private final KRendererDeferredType actual;
  private final R1Type                r1;

  private ExampleRendererDeferredWithFXAA(
    final R1Type in_r1,
    final KRendererDeferredType r)
  {
    this.r1 = in_r1;
    this.actual = r;
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    final String name =
      ExampleRendererDeferredWithFXAA.class.getCanonicalName();
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
