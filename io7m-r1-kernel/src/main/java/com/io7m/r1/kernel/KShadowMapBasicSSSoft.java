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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBasicSSSoft;

/**
 * The type of directional basic shadow maps.
 */

@EqualityReference public final class KShadowMapBasicSSSoft implements
  KShadowMapType
{
  /**
   * Construct a new shadow map.
   *
   * @param g
   *          The OpenGL implementation
   * @param description
   *          A map description
   * @return A new map
   * @throws RException
   *           If an error occurs
   */

  public static KShadowMapBasicSSSoft newMap(
    final JCGLImplementationType g,
    final KShadowMapDescriptionBasicSSSoft description)
    throws RException
  {
    NullCheck.notNull(g, "OpenGL implementation");
    NullCheck.notNull(description, "Description");

    final KFramebufferDepthType f =
      KFramebufferDepth.newDepthFramebuffer(
        g,
        description.getFramebufferDescription());
    return new KShadowMapBasicSSSoft(description, f);
  }

  private final KShadowMapDescriptionBasicSSSoft description;
  private final KFramebufferDepthType            framebuffer;

  private KShadowMapBasicSSSoft(
    final KShadowMapDescriptionBasicSSSoft in_description,
    final KFramebufferDepthType in_framebuffer)
  {
    this.description = NullCheck.notNull(in_description, "Description");
    this.framebuffer = NullCheck.notNull(in_framebuffer, "Framebuffer");
  }

  /**
   * @return The shadow map description
   */

  public KShadowMapDescriptionBasicSSSoft getDescription()
  {
    return this.description;
  }

  /**
   * @return The framebuffer
   */

  public KFramebufferDepthType getFramebuffer()
  {
    return this.framebuffer;
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.framebuffer.resourceIsDeleted();
  }

  @Override public void shadowMapDelete(
    final JCGLImplementationType g)
    throws RException
  {
    this.framebuffer.delete(g);
  }

  @Override public <T, E extends Throwable> T shadowMapAccept(
    final KShadowMapVisitorType<T, E> v)
    throws E,
      RException
  {
    return v.basicSSSoft(this);
  }

  @Override public long shadowMapGetSizeBytes()
  {
    return this.framebuffer.getSizeInBytes();
  }
}
