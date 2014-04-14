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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

/**
 * A description of a map suitable for basic shadow mapping.
 */

@Immutable public final class KShadowMapBasicDescription implements
  KShadowMapDescriptionType
{
  /**
   * Create a new description of a shadow map.
   * 
   * @param in_light_id
   *          The light associated with the shadow map
   * @param in_description
   *          The description of the depth framebuffer
   * @param in_size_exponent
   *          The size exponent of the shadow map
   * @return A new shadow map description
   * @throws ConstraintError
   *           If any parameter is <code>null</code> or
   *           <code>in_size_exponent &lt; 1</code>.
   */

  public static @Nonnull KShadowMapBasicDescription newDescription(
    final @Nonnull Integer in_light_id,
    final @Nonnull KFramebufferDepthDescription in_description,
    final int in_size_exponent)
    throws ConstraintError
  {
    return new KShadowMapBasicDescription(
      in_light_id,
      in_description,
      in_size_exponent);
  }

  private final @Nonnull KFramebufferDepthDescription description;
  private final @Nonnull Integer                      light_id;
  private final int                                   size_exponent;

  private KShadowMapBasicDescription(
    final @Nonnull Integer in_light_id,
    final @Nonnull KFramebufferDepthDescription in_description,
    final int in_size_exponent)
    throws ConstraintError
  {
    this.light_id = Constraints.constrainNotNull(in_light_id, "Light ID");
    this.size_exponent =
      (int) Constraints
        .constrainRange(in_size_exponent, 1, Integer.MAX_VALUE);
    this.description =
      Constraints.constrainNotNull(in_description, "Description");
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KShadowMapBasicDescription other = (KShadowMapBasicDescription) obj;
    if (!this.description.equals(other.description)) {
      return false;
    }
    if (!this.light_id.equals(other.light_id)) {
      return false;
    }
    if (this.size_exponent != other.size_exponent) {
      return false;
    }
    return true;
  }

  /**
   * @return A description of the depth buffer
   */

  public @Nonnull KFramebufferDepthDescription getDescription()
  {
    return this.description;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.description.hashCode();
    result = (prime * result) + this.light_id.hashCode();
    result = (prime * result) + this.size_exponent;
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KShadowMapDescriptionVisitorType<A, E>>
    A
    mapDescriptionAccept(
      final @Nonnull V v)
      throws RException,
        ConstraintError,
        E
  {
    return v.shadowMapDescriptionBasic(this);
  }

  @Override public @Nonnull Integer mapGetLightID()
  {
    return this.light_id;
  }

  @Override public int mapGetSizeExponent()
  {
    return this.size_exponent;
  }
}
