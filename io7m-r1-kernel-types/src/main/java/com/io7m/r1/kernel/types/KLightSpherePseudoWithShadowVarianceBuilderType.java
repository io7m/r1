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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.r1.types.RException;

/**
 * The type of mutable builders for spherical lights with variance shadows.
 */

public interface KLightSpherePseudoWithShadowVarianceBuilderType extends
  KLightSphereBuilderType
{
  /**
   * <p>
   * Because projected textures typically have one-pixel black borders with
   * their wrapping modes set to "clamp to edge", it's often necessary to set
   * a bias value to slightly enlarge the field of view of the six sub-lights
   * in order to seamlessly blend the edges of the light contributions
   * together.
   * </p>
   * <p>
   * The default bias value is <code>0.065</code>, which was simply obtained
   * through experimentation.
   * </p>
   *
   * @param r
   *          The number of radians by which to increase the field of view
   */

  void setFOVCompensationBias(
    final float r);

  /**
   * Construct a light.
   *
   * @param context
   *          The transform context.
   * @param texture
   *          The projected texture.
   * @return A new light
   * @throws RException
   *           On errors
   */

  KLightSpherePseudoWithShadowVariance build(
    final KTransformContext context,
    final Texture2DStaticUsableType texture)
    throws RException;

  /**
   * Enable/disable the negative X sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledNegativeX(
    boolean enabled);

  /**
   * Enable/disable the negative Y sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledNegativeY(
    boolean enabled);

  /**
   * Enable/disable the negative Z sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledNegativeZ(
    boolean enabled);

  /**
   * Enable/disable the positive X sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledPositiveX(
    boolean enabled);

  /**
   * Enable/disable the positive Y sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledPositiveY(
    boolean enabled);

  /**
   * Enable/disable the positive Z sub-light.
   *
   * @param enabled
   *          <code>true</code> if the sub-light should be enabled.
   */

  void setEnabledPositiveZ(
    boolean enabled);

  /**
   * Set the shadow.
   *
   * @param s
   *          The shadow.
   */

  void setShadow(
    KShadowMappedVariance s);
}
