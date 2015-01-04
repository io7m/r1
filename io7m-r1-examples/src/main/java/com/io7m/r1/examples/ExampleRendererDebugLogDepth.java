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
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.KFramebufferRGBAUsableType;
import com.io7m.r1.kernel.KRendererDebugLogDepth;
import com.io7m.r1.kernel.KShaderCacheSetType;
import com.io7m.r1.kernel.types.KVisibleSet;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.types.RException;

/**
 * Example logarithmic depth renderer.
 */

public final class ExampleRendererDebugLogDepth implements
  ExampleRendererDebugType
{
  @Override public <T> T rendererAccept(
    final ExampleRendererVisitorType<T> v)
    throws RException
  {
    return v.visitDebug(this);
  }

  @Override public ExampleRendererName exampleRendererGetName()
  {
    final String name =
      ExampleRendererDeferredWithEmissionGlow.class.getSimpleName();
    assert name != null;
    return new ExampleRendererName(name);
  }

  private final KRendererDebugLogDepth actual;
  private final R1Type                 r1;

  private ExampleRendererDebugLogDepth(
    final R1Type in_r1,
    final KRendererDebugLogDepth rd)
  {
    this.r1 = in_r1;
    this.actual = rd;
  }

  @Override public R1Type getR1()
  {
    return this.r1;
  }

  @Override public void rendererDebugEvaluate(
    final KFramebufferRGBAUsableType framebuffer,
    final KVisibleSet visible)
    throws RException
  {
    this.actual.rendererDebugEvaluate(framebuffer, visible);
  }

  @Override public String rendererGetName()
  {
    return this.exampleRendererGetName().toString();
  }

  /**
   * @return The renderer name
   */

  public static ExampleRendererName getName()
  {
    final String name = ExampleRendererDebugLogDepth.class.getSimpleName();
    return new ExampleRendererName(NullCheck.notNull(name));
  }

  /**
   * @return A new renderer constructor
   */

  public static ExampleRendererConstructorType get()
  {
    return new ExampleRendererConstructorType() {
      @Override public ExampleRendererType newRenderer(
        final LogUsableType log,
        final KShaderCacheSetType caches,
        final JCGLImplementationType gi)
        throws JCGLException,
          RException
      {
        return ExampleRendererDebugLogDepth.make(log, caches, gi);
      }
    };
  }

  private static ExampleRendererType make(
    final LogUsableType log,
    final KShaderCacheSetType caches,
    final JCGLImplementationType gi)
    throws RException
  {
    NullCheck.notNull(gi, "GL");
    final R1BuilderType b = R1.newBuilder(gi, log);
    b.setShaderCacheSet(caches);
    final R1Type r = b.build();
    final KRendererDebugLogDepth dld =
      new KRendererDebugLogDepth(gi, caches.getShaderDebugCache());
    return new ExampleRendererDebugLogDepth(r, dld);
  }
}
