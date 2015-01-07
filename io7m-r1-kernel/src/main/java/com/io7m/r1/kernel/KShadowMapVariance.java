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
import com.io7m.r1.kernel.types.KShadowMapDescriptionVariance;
import com.io7m.r1.types.RException;

/**
 * The type of directional variance shadow maps.
 */

@EqualityReference public final class KShadowMapVariance implements
  KShadowMapType
{
  private final KShadowMapDescriptionVariance description;
  private final KFramebufferDepthVarianceType framebuffer;

  private KShadowMapVariance(
    final KShadowMapDescriptionVariance in_description,
    final KFramebufferDepthVarianceType in_framebuffer)
  {
    this.description = NullCheck.notNull(in_description, "Description");
    this.framebuffer = NullCheck.notNull(in_framebuffer, "Framebuffer");
  }

  /**
   * @return The shadow map description
   */

  public KShadowMapDescriptionVariance getDescription()
  {
    return this.description;
  }

  /**
   * @return The framebuffer
   */

  public KFramebufferDepthVarianceType getFramebuffer()
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
    this.framebuffer.deleteFramebuffer(g);
  }

  @Override public <T, E extends Throwable> T shadowMapAccept(
    final KShadowMapVisitorType<T, E> v)
    throws E,
      RException
  {
    return v.variance(this);
  }

  @Override public long shadowMapGetSizeBytes()
  {
    return this.framebuffer.getSizeBytes();
  }

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

  public static KShadowMapVariance newMap(
    final JCGLImplementationType g,
    final KShadowMapDescriptionVariance description)
    throws RException
  {
    NullCheck.notNull(g, "OpenGL implementation");
    NullCheck.notNull(description, "Description");

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(
        g,
        description.getFramebufferDescription());
    return new KShadowMapVariance(description, f);
  }
}
