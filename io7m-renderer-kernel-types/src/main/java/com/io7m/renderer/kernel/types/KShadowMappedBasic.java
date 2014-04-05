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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * A description of a basic mapped shadow.
 */

@Immutable public final class KShadowMappedBasic implements KShadowType
{
  /**
   * Construct a new basic mapped shadow.
   * 
   * @param depth_bias
   *          The bias to apply to the depth values of geometry to alleviate
   *          shadow acne
   * @param factor_min
   *          The minimum light level of shadowed points (0.0 produces very
   *          dark shadows)
   * @param description
   *          The description of the shadow map
   * @return A new shadow
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KShadowMappedBasic newMappedBasic(
    final float depth_bias,
    final float factor_min,
    final @Nonnull KShadowMapBasicDescription description)
    throws ConstraintError
  {
    return new KShadowMappedBasic(depth_bias, factor_min, description);
  }

  private final float                               depth_bias;
  private final @Nonnull KShadowMapBasicDescription description;
  private final float                               factor_min;

  KShadowMappedBasic(
    final float in_depth_bias,
    final float in_factor_min,
    final @Nonnull KShadowMapBasicDescription in_description)
    throws ConstraintError
  {
    this.description =
      Constraints.constrainNotNull(in_description, "Description");
    this.depth_bias = in_depth_bias;
    this.factor_min = in_factor_min;
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

    final KShadowMappedBasic other = (KShadowMappedBasic) obj;
    return (Float.floatToIntBits(this.depth_bias) == Float
      .floatToIntBits(other.depth_bias))
      && this.description.equals(other.description)
      && (Float.floatToIntBits(this.factor_min) == Float
        .floatToIntBits(other.factor_min));
  }

  /**
   * @return The depth bias value for the shadow
   */

  public float getDepthBias()
  {
    return this.depth_bias;
  }

  /**
   * @return The description of the shadow map
   */

  public @Nonnull KShadowMapBasicDescription getDescription()
  {
    return this.description;
  }

  /**
   * @return The minimum light level
   */

  public float getFactorMinimum()
  {
    return this.factor_min;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.depth_bias);
    result = (prime * result) + this.description.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.factor_min);
    return result;
  }

  @Override public
    <T, E extends Throwable, V extends KShadowVisitorType<T, E>>
    T
    shadowAccept(
      final @Nonnull V v)
      throws E,
        JCGLException,
        RException,
        ConstraintError
  {
    return v.shadowVisitMappedBasic(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KShadowMappedBasic description=");
    builder.append(this.description);
    builder.append(" depth_bias=");
    builder.append(this.depth_bias);
    builder.append(" factor_min=");
    builder.append(this.factor_min);
    builder.append("]");
    return builder.toString();
  }
}
