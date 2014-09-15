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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.kernel.types.KFramebufferDepthDescription;
import com.io7m.r1.kernel.types.KShadowMapDescriptionOmnidirectionalDualParaboloidBasic;
import com.io7m.r1.types.RException;

/**
 * The type of omnidirectional (dual paraboloid) basic shadow maps.
 */

@EqualityReference public final class KShadowMapOmnidirectionalDualParaboloidBasic implements
  KShadowMapOmnidirectionalType
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

  public static
    KShadowMapOmnidirectionalDualParaboloidBasic
    newMap(
      final JCGLImplementationType g,
      final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic description)
      throws RException
  {
    NullCheck.notNull(g, "OpenGL implementation");
    NullCheck.notNull(description, "Description");

    final KFramebufferDepthDescription fbd =
      description.getFramebufferDescription();
    final KFramebufferDepth nz =
      KFramebufferDepth.newDepthFramebuffer(g, fbd);
    final KFramebufferDepth pz =
      KFramebufferDepth.newDepthFramebuffer(g, fbd);
    return new KShadowMapOmnidirectionalDualParaboloidBasic(
      nz,
      pz,
      description);
  }

  private final KFramebufferDepth                                       neg_z;
  private final KFramebufferDepth                                       pos_z;
  private final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic description;

  private KShadowMapOmnidirectionalDualParaboloidBasic(
    final KFramebufferDepth in_neg_z,
    final KFramebufferDepth in_pos_z,
    final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic in_description)
  {
    this.neg_z = NullCheck.notNull(in_neg_z, "Negative Z");
    this.pos_z = NullCheck.notNull(in_pos_z, "Positive Z");
    this.description = NullCheck.notNull(in_description, "Description");
  }

  /**
   * @return The shadow map description
   */

  public
    KShadowMapDescriptionOmnidirectionalDualParaboloidBasic
    getDescription()
  {
    return this.description;
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.neg_z.resourceIsDeleted();
  }

  /**
   * @return The framebuffer for the hemisphere that faces negative Z.
   */

  public KFramebufferDepth getFramebufferNegativeZ()
  {
    return this.neg_z;
  }

  /**
   * @return The framebuffer for the hemisphere that faces positive Z.
   */

  public KFramebufferDepth getFramebufferPositiveZ()
  {
    return this.pos_z;
  }

  @Override public <T, E extends Throwable> T shadowMapAccept(
    final KShadowMapVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.omnidirectional(this);
  }

  @Override public void shadowMapDelete(
    final JCGLImplementationType g)
    throws RException
  {
    this.neg_z.kFramebufferDelete(g);
    this.pos_z.kFramebufferDelete(g);
  }

  @Override public long shadowMapGetSizeBytes()
  {
    return this.neg_z.kFramebufferGetSizeBytes()
      + this.pos_z.kFramebufferGetSizeBytes();
  }

  @Override public <T, E extends Throwable> T shadowMapOmnidirectionalAccept(
    final KShadowMapOmnidirectionalVisitorType<T, E> v)
    throws E,
      JCGLException,
      RException
  {
    return v.dualParaboloidBasic(this);
  }
}
