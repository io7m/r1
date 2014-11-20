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
import com.io7m.r1.kernel.KFramebufferDeferredUsableType;
import com.io7m.r1.kernel.KRendererDeferredControlType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.types.RException;

/**
 * An example renderer using the default deferred renderer.
 */

public final class ExampleRendererDeferredDefault implements
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
        return ExampleRendererDeferredDefault.make(log, caches, gi);
      }
    };
  }

  /**
   * @return The renderer name.
   */

  public static ExampleRendererName getName()
  {
    final String name = ExampleRendererDeferredDefault.class.getSimpleName();
    assert name != null;
    return new ExampleRendererName(name);
  }

  private static ExampleRendererDeferredType make(
    final LogUsableType log,
    final KShaderCacheSetType in_cache,
    final JCGLImplementationType gi)
    throws RException
  {
    NullCheck.notNull(gi, "GL");
    final R1BuilderType b = R1.newBuilder(gi, log);
    b.setShaderCacheSet(in_cache);
    final R1Type r = b.build();
    return new ExampleRendererDeferredDefault(r, r.getRendererDeferred());
  }

  private final KRendererDeferredType actual;
  private final R1Type                r;

  private ExampleRendererDeferredDefault(
    final R1Type in_r,
    final KRendererDeferredType rd)
  {
    this.r = in_r;
    this.actual = rd;
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    final String name = ExampleRendererDeferredDefault.class.getSimpleName();
    assert name != null;
    return new ExampleRendererName(name);
  }

  @Override public R1Type getR1()
  {
    return this.r;
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
      final KVisibleSet visible,
      final PartialProcedureType<KRendererDeferredControlType, RException> procedure)
      throws RException
  {
    this.actual.rendererDeferredEvaluate(framebuffer, visible, procedure);
  }

  @Override public void rendererDeferredEvaluateFull(
    final KFramebufferDeferredUsableType framebuffer,
    final KVisibleSet visible)
    throws RException
  {
    this.actual.rendererDeferredEvaluateFull(framebuffer, visible);
  }

  @Override public String rendererGetName()
  {
    return this.exampleRendererGetName().toString();
  }
}
