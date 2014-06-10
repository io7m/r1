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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionUserError;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * The type of mutable builders for projective lights.
 */

public interface KLightProjectiveBuilderType
{
  /**
   * <p>
   * Construct a light.
   * </p>
   * 
   * @param caps
   *          The current graphics capabilities.
   * @return A new light based on all of the parameters given so far.
   * @throws RExceptionUserError
   *           If no texture was specified.
   * @throws RException
   *           If any other error occurs.
   */

  KLightProjective build(
    final KGraphicsCapabilitiesType caps)
    throws RExceptionUserError,
      RException;

  /**
   * <p>
   * Set the color of the light.
   * </p>
   * <p>
   * The default color is full-intensity white.
   * </p>
   * 
   * @param color
   *          The color.
   */

  void setColor(
    final RVectorI3F<RSpaceRGBType> color);

  /**
   * <p>
   * Set the falloff exponent.
   * </p>
   * <p>
   * The default exponent is <code>1.0</code> (linear falloff).
   * </p>
   * 
   * @param exponent
   *          The exponent.
   */

  void setFalloff(
    @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float exponent);

  /**
   * <p>
   * Set the intensity of the light.
   * </p>
   * <p>
   * The default intensity is <code>1.0</code>.
   * </p>
   * 
   * @param intensity
   *          The intensity.
   */

  void setIntensity(
    @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity);

  /**
   * <p>
   * Unset the shadow for the light, if any.
   * </p>
   * <p>
   * The default is for the light not to have a shadow.
   * </p>
   * 
   * @see #setShadow(KShadowType)
   */

  void setNoShadow();

  /**
   * <p>
   * Set the orientation of the light.
   * </p>
   * <p>
   * The default orientation is <code>(0.0, 0.0, 0.0, 1.0)</code>.
   * </p>
   * 
   * @param orientation
   *          The orientation.
   */

  void setOrientation(
    final QuaternionI4F orientation);

  /**
   * <p>
   * Set the position in world-space of the light.
   * </p>
   * <p>
   * The default position is <code>(0.0, 0.0, 0.0)</code>.
   * </p>
   * 
   * @param position
   *          The position.
   */

  void setPosition(
    final RVectorI3F<RSpaceWorldType> position);

  /**
   * <p>
   * Set the projection of the light.
   * </p>
   * 
   * @param projection
   *          The projection.
   */

  void setProjection(
    final KProjectionType projection);

  /**
   * <p>
   * Set the range of the light.
   * </p>
   * <p>
   * The default range is <code>8.0</code>.
   * </p>
   * 
   * @param range
   *          The range.
   */

    void
    setRange(
      final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float range);

  /**
   * <p>
   * Set the shadow for the light.
   * </p>
   * <p>
   * The default is for the light not to have a shadow.
   * </p>
   * 
   * @see #setNoShadow()
   * @param s
   *          The shadow.
   */

  void setShadow(
    final KShadowType s);

  /**
   * <p>
   * Set the shadow for the light. Equivalent to {@link #setNoShadow()} for
   * {@link com.io7m.jfunctional.None} and {@link #setShadow(KShadowType)} for
   * {@link com.io7m.jfunctional.Some}.
   * </p>
   * <p>
   * The default is for the light not to have a shadow.
   * </p>
   * 
   * @see #setShadow(KShadowType)
   * @see #setNoShadow()
   * @param s
   *          The shadow.
   */

  void setShadowOption(
    OptionType<KShadowType> s);

  /**
   * <p>
   * Set the texture for the light.
   * </p>
   * <p>
   * There is no default texture; this parameter is required to be set.
   * </p>
   * 
   * @param texture
   *          The texture.
   */

  void setTexture(
    final Texture2DStaticUsableType texture);
}
