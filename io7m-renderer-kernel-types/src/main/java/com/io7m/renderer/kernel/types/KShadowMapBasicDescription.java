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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.renderer.types.RException;

/**
 * A description of a map suitable for basic shadow mapping.
 */

@EqualityStructural public final class KShadowMapBasicDescription implements
  KShadowMapDescriptionType
{
  /**
   * Create a new description of a shadow map.
   * 
   * @param in_description
   *          The description of the depth framebuffer
   * @param in_size_exponent
   *          The size exponent of the shadow map
   * @return A new shadow map description
   */

  public static KShadowMapBasicDescription newDescription(
    final KFramebufferDepthDescription in_description,
    final int in_size_exponent)
  {
    return new KShadowMapBasicDescription(in_description, in_size_exponent);
  }

  private final KFramebufferDepthDescription description;
  private final int                          size_exponent;

  private KShadowMapBasicDescription(
    final KFramebufferDepthDescription in_description,
    final int in_size_exponent)
  {
    this.size_exponent =
      (int) RangeCheck.checkIncludedIn(
        in_size_exponent,
        "Exponent",
        RangeCheck.POSITIVE_INTEGER,
        "Valid exponents");
    this.description = NullCheck.notNull(in_description, "Description");
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    if (this.size_exponent != other.size_exponent) {
      return false;
    }
    return true;
  }

  /**
   * @return A description of the depth buffer
   */

  public KFramebufferDepthDescription getDescription()
  {
    return this.description;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.description.hashCode();
    result = (prime * result) + this.size_exponent;
    return result;
  }

  @Override public
    <A, E extends Throwable, V extends KShadowMapDescriptionVisitorType<A, E>>
    A
    mapDescriptionAccept(
      final V v)
      throws RException,
        E
  {
    return v.shadowMapDescriptionBasic(this);
  }

  @Override public int mapGetSizeExponent()
  {
    return this.size_exponent;
  }
}
