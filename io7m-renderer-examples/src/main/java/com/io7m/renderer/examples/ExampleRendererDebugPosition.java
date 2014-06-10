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

package com.io7m.renderer.examples;

import java.math.BigInteger;

import com.io7m.jcache.PCacheConfig;
import com.io7m.jcache.PCacheConfig.BuilderType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.KDepthRenderer;
import com.io7m.renderer.kernel.KDepthRendererType;
import com.io7m.renderer.kernel.KRendererDebugPosition;
import com.io7m.renderer.kernel.KRendererDebugType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.types.KGraphicsCapabilities;
import com.io7m.renderer.types.RException;

/**
 * A debug renderer for testing position reconstruction.
 */

public final class ExampleRendererDebugPosition implements
  ExampleRendererDebugType
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
          final KShaderCacheType shader_cache,
          final JCGLImplementationType gi)
          throws JCGLException,
            RException
      {
        return ExampleRendererDebugPosition.make(log, shader_cache, gi);
      }
    };
  }

  @SuppressWarnings("null") private static ExampleRendererDebugType make(
    final LogUsableType log,
    final KShaderCacheType shader_cache,
    final JCGLImplementationType gi)
    throws JCGLException,
      RException
  {
    NullCheck.notNull(gi, "GL");

    final KGraphicsCapabilities caps =
      KGraphicsCapabilities.getCapabilities(gi);

    final KDepthRendererType depth_renderer =
      KDepthRenderer.newRenderer(gi, caps, shader_cache, log);

    final BuilderType shadow_cache_config_builder = PCacheConfig.newBuilder();
    shadow_cache_config_builder.setMaximumAge(BigInteger.valueOf(60));
    shadow_cache_config_builder.setNoMaximumSize();

    final KRendererDebugType r =
      KRendererDebugPosition.newRenderer(
        gi,
        depth_renderer,
        shader_cache,
        log);

    return new ExampleRendererDebugPosition(r);
  }

  private final KRendererDebugType actual;

  private ExampleRendererDebugPosition(
    final KRendererDebugType r)
  {
    this.actual = NullCheck.notNull(r, "Renderer");
  }

  @Override public <T> T rendererAccept(
    final ExampleRendererVisitorType<T> v)
    throws RException
  {
    return v.visitDebug(this);
  }

  @Override public KRendererDebugType rendererGetDebug()
  {
    return this.actual;
  }

  @Override public String rendererGetName()
  {
    final String r = this.actual.getClass().getCanonicalName();
    assert r != null;
    return r;
  }
}
