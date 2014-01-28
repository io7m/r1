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
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthVarianceDescription;

abstract class KShadowMapDescription implements
  KShadowMapDescriptionVisitable
{
  static final class KShadowMapBasicDescription extends KShadowMapDescription
  {
    private final @Nonnull KFramebufferDepthDescription description;

    KShadowMapBasicDescription(
      final @Nonnull Integer light_id,
      final @Nonnull KFramebufferDepthDescription description,
      final int size_exponent)
      throws ConstraintError
    {
      super(light_id, size_exponent);
      this.description =
        Constraints.constrainNotNull(description, "Description");
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMapBasicDescription other =
        (KShadowMapBasicDescription) obj;
      return this.description.equals(other.description);
    }

    public @Nonnull KFramebufferDepthDescription getDescription()
    {
      return this.description;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + this.description.hashCode();
      return result;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapDescriptionVisitor<A, E>>
      A
      kShadowMapDescriptionAccept(
        final @Nonnull V v)
        throws RException,
          ConstraintError,
          E
    {
      return v.shadowMapDescriptionVisitBasic(this);
    }
  }

  static final class KShadowMapVarianceDescription extends
    KShadowMapDescription
  {
    private final @Nonnull KFramebufferDepthVarianceDescription description;

    KShadowMapVarianceDescription(
      final @Nonnull Integer light_id,
      final @Nonnull KFramebufferDepthVarianceDescription description,
      final int size_exponent)
      throws ConstraintError
    {
      super(light_id, size_exponent);
      this.description =
        Constraints.constrainNotNull(description, "Description");
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMapVarianceDescription other =
        (KShadowMapVarianceDescription) obj;
      return this.description.equals(other.description);
    }

    public @Nonnull KFramebufferDepthVarianceDescription getDescription()
    {
      return this.description;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + this.description.hashCode();
      return result;
    }

    @Override public
      <A, E extends Throwable, V extends KShadowMapDescriptionVisitor<A, E>>
      A
      kShadowMapDescriptionAccept(
        final @Nonnull V v)
        throws E,
          RException,
          ConstraintError
    {
      return v.shadowMapDescriptionVisitVariance(this);
    }
  }

  private final @Nonnull Integer light_id;
  private final int              size_exponent;

  KShadowMapDescription(
    final @Nonnull Integer light_id,
    final int size_exponent)
    throws ConstraintError
  {
    this.light_id = Constraints.constrainNotNull(light_id, "Light ID");
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
    if (this.size_exponent != other.size_exponent) {
      return false;
    }
    return true;
  }

  @Nonnull Integer getLightID()
  {
    return this.light_id;
  }

  int getSizeExponent()
  {
    return this.size_exponent;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.light_id.hashCode();
    result = (prime * result) + this.size_exponent;
    return result;
  }
}
