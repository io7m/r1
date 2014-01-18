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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

final class KShadowMapDescription
{
  private final @Nonnull Integer          light_id;
  private final @Nonnull KShadowFilter    shadow_filter;
  private final @Nonnull KShadowPrecision shadow_precision;
  private final int                       size_exponent;
  private final @Nonnull KShadowType      type;

  KShadowMapDescription(
    final @Nonnull Integer light_id,
    final @Nonnull KShadowFilter shadow_filter,
    final @Nonnull KShadowPrecision shadow_precision,
    final @Nonnull KShadowType type,
    final int size_exponent)
    throws ConstraintError
  {
    this.light_id = Constraints.constrainNotNull(light_id, "Light ID");
    this.type = Constraints.constrainNotNull(type, "Type");
    this.shadow_precision =
      Constraints.constrainNotNull(shadow_precision, "Shadow precision");
    this.shadow_filter =
      Constraints.constrainNotNull(shadow_filter, "Shadow filter");
    this.size_exponent =
      (int) Constraints.constrainRange(size_exponent, 1, Integer.MAX_VALUE);
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
    final KShadowMapDescription other = (KShadowMapDescription) obj;
    if (!this.light_id.equals(other.light_id)) {
      return false;
    }
    if (this.shadow_filter != other.shadow_filter) {
      return false;
    }
    if (this.shadow_precision != other.shadow_precision) {
      return false;
    }
    if (this.size_exponent != other.size_exponent) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  @Nonnull Integer getLightID()
  {
    return this.light_id;
  }

  @Nonnull KShadowFilter getShadowFilter()
  {
    return this.shadow_filter;
  }

  @Nonnull KShadowPrecision getShadowPrecision()
  {
    return this.shadow_precision;
  }

  int getSizeExponent()
  {
    return this.size_exponent;
  }

  @Nonnull KShadowType getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.light_id.hashCode();
    result = (prime * result) + this.shadow_filter.hashCode();
    result = (prime * result) + this.shadow_precision.hashCode();
    result = (prime * result) + this.size_exponent;
    result = (prime * result) + this.type.hashCode();
    return result;
  }
}
