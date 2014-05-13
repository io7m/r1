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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;

/**
 * A description of a basic mapped shadow.
 */

@EqualityStructural public final class KShadowMappedBasic implements
  KShadowType
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
   */

  public static KShadowMappedBasic newMappedBasic(
    final float depth_bias,
    final float factor_min,
    final KShadowMapBasicDescription description)
  {
    return new KShadowMappedBasic(depth_bias, factor_min, description);
  }

  private final float                      depth_bias;
  private final KShadowMapBasicDescription description;
  private final float                      factor_min;

  KShadowMappedBasic(
    final float in_depth_bias,
    final float in_factor_min,
    final KShadowMapBasicDescription in_description)
  {
    this.description = NullCheck.notNull(in_description, "Description");
    this.depth_bias = in_depth_bias;
    this.factor_min = in_factor_min;
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

  public KShadowMapBasicDescription getDescription()
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
      final V v)
      throws E,
        JCGLException,
        RException
  {
    return v.shadowMappedBasic(this);
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
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
