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
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * A projective light "projects" a texture into a scene from a given position,
 * according to the given projection matrix. The texture projected is
 * multiplied by the given colour and attenuated according to the given range
 * and falloff values. The light may optionally cast shadows using a variety
 * of shadow mapping techniques, specified by the given shadow value.
 */

@EqualityStructural public final class KLightProjective implements KLightType
{
  /**
   * Construct a new projective light.
   * 
   * @param id
   *          The identifier of the new light
   * @param texture
   *          The texture that will be projected into the scene
   * @param position
   *          The position of the light
   * @param orientation
   *          The orientation of the light
   * @param colour
   *          The colour of the light
   * @param intensity
   *          The intensity of the light
   * @param range
   *          The maximum range of the light
   * @param falloff
   *          The falloff value for the light
   * @param projection
   *          The projection matrix for the light
   * @param shadow
   *          A description of the shadows that will be cast, if any
   * @return A new projective light
   */

  public static KLightProjective newProjective(
    final Integer id,
    final Texture2DStaticUsableType texture,
    final RVectorI3F<RSpaceWorldType> position,
    final QuaternionI4F orientation,
    final RVectorI3F<RSpaceRGBType> colour,
    final float intensity,
    final float range,
    final float falloff,
    final RMatrixI4x4F<RTransformProjectionType> projection,
    final OptionType<KShadowType> shadow)
  {
    return new KLightProjective(
      id,
      texture,
      position,
      orientation,
      colour,
      intensity,
      range,
      falloff,
      projection,
      shadow);
  }

  private final RVectorI3F<RSpaceRGBType>              colour;
  private final float                                  falloff;
  private final Integer                                id;
  private final float                                  intensity;
  private final QuaternionI4F                          orientation;
  private final RVectorI3F<RSpaceWorldType>            position;
  private final RMatrixI4x4F<RTransformProjectionType> projection;
  private final float                                  range;
  private final OptionType<KShadowType>                shadow;
  private final Texture2DStaticUsableType              texture;

  private KLightProjective(
    final Integer in_id,
    final Texture2DStaticUsableType in_texture,
    final RVectorI3F<RSpaceWorldType> in_position,
    final QuaternionI4F in_orientation,
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_intensity,
    final float in_range,
    final float in_falloff,
    final RMatrixI4x4F<RTransformProjectionType> in_projection,
    final OptionType<KShadowType> in_shadow)
  {
    this.intensity = in_intensity;
    this.id = NullCheck.notNull(in_id, "Identifier");
    this.colour = NullCheck.notNull(in_colour, "Colour");
    this.position = NullCheck.notNull(in_position, "Position");
    this.orientation = NullCheck.notNull(in_orientation, "Orientation");
    this.range = in_range;
    this.falloff = in_falloff;
    this.projection = NullCheck.notNull(in_projection, "Projection");
    this.texture = NullCheck.notNull(in_texture, "Texture");
    this.shadow = NullCheck.notNull(in_shadow, "Shadow");
  }

  /**
   * @return The falloff exponent for the light
   */

  public float getFalloff()
  {
    return this.falloff;
  }

  /**
   * @return The orientation of the light
   */

  public QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  /**
   * @return The position of the light
   */

  public RVectorI3F<RSpaceWorldType> getPosition()
  {
    return this.position;
  }

  /**
   * @return The projection matrix for the light
   */

  public RMatrixI4x4F<RTransformProjectionType> getProjection()
  {
    return this.projection;
  }

  /**
   * @return The maximum range of the light
   */

  public float getRange()
  {
    return this.range;
  }

  /**
   * @return The texture that will be projected into the scene
   */

  public Texture2DStaticUsableType getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.falloff);
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.position.hashCode();
    result = (prime * result) + this.projection.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.range);
    result = (prime * result) + this.shadow.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public RVectorI3F<RSpaceRGBType> lightGetColour()
  {
    return this.colour;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public OptionType<KShadowType> lightGetShadow()
  {
    return this.shadow;
  }

  @Override public boolean lightHasShadow()
  {
    return this.shadow.isSome();
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitorType<A, E>>
    A
    lightAccept(
      final V v)
      throws E,
        RException
  {
    return v.lightProjective(this);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KProjective ");
    builder.append(super.toString());
    builder.append(" texture=");
    builder.append(this.texture);
    builder.append(" position=");
    builder.append(this.position);
    builder.append(" orientation=");
    builder.append(this.orientation);
    builder.append(" range=");
    builder.append(this.range);
    builder.append(" falloff=");
    builder.append(this.falloff);
    builder.append(" projection=");
    builder.append(this.projection);
    builder.append(" shadow=");
    builder.append(this.shadow);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
