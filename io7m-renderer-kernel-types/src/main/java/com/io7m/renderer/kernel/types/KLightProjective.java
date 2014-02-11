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
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceRGB;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3F;

/**
 * A projective light "projects" a texture into a scene from a given position,
 * according to the given projection matrix. The texture projected is
 * multiplied by the given colour and attenuated according to the given range
 * and falloff values. The light may optionally cast shadows using a variety
 * of shadow mapping techniques, specified by the given shadow value.
 */

@Immutable public final class KLightProjective implements KLight
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
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static @Nonnull KLightProjective newProjective(
    final @Nonnull Integer id,
    final @Nonnull Texture2DStaticUsable texture,
    final @Nonnull RVectorReadable3F<RSpaceWorld> position,
    final @Nonnull QuaternionI4F orientation,
    final @Nonnull RVectorReadable3F<RSpaceRGB> colour,
    final float intensity,
    final float range,
    final float falloff,
    final @Nonnull RMatrixI4x4F<RTransformProjection> projection,
    final @Nonnull Option<KShadow> shadow)
    throws ConstraintError
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

  private final @Nonnull RVectorI3F<RSpaceRGB>              colour;
  private final float                                       falloff;
  private final @Nonnull Integer                            id;
  private final float                                       intensity;
  private final @Nonnull QuaternionI4F                      orientation;
  private final @Nonnull RVectorReadable3F<RSpaceWorld>     position;
  private final @Nonnull RMatrixI4x4F<RTransformProjection> projection;
  private final float                                       range;
  private final @Nonnull Option<KShadow>                    shadow;
  private final @Nonnull Texture2DStaticUsable              texture;

  private KLightProjective(
    final @Nonnull Integer in_id,
    final @Nonnull Texture2DStaticUsable in_texture,
    final @Nonnull RVectorReadable3F<RSpaceWorld> in_position,
    final @Nonnull QuaternionI4F in_orientation,
    final @Nonnull RVectorReadable3F<RSpaceRGB> in_colour,
    final float in_intensity,
    final float in_range,
    final float in_falloff,
    final @Nonnull RMatrixI4x4F<RTransformProjection> in_projection,
    final @Nonnull Option<KShadow> in_shadow)
    throws ConstraintError
  {
    this.intensity = in_intensity;
    this.id = Constraints.constrainNotNull(in_id, "Identifier");
    this.colour =
      new RVectorI3F<RSpaceRGB>(Constraints.constrainNotNull(
        in_colour,
        "Colour"));
    this.position = in_position;
    this.orientation = in_orientation;
    this.range = in_range;
    this.falloff = in_falloff;
    this.projection = in_projection;
    this.texture = in_texture;
    this.shadow = in_shadow;
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
    final KLightProjective other = (KLightProjective) obj;

    return (Float.floatToIntBits(this.falloff) == Float
      .floatToIntBits(other.falloff))
      && this.orientation.equals(other.orientation)
      && this.position.equals(other.position)
      && this.projection.equals(other.projection)
      && (Float.floatToIntBits(this.range) == Float
        .floatToIntBits(other.range))
      && this.shadow.equals(other.shadow)
      && this.texture.equals(other.texture);
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

  public @Nonnull QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  /**
   * @return The position of the light
   */

  public @Nonnull RVectorReadable3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  /**
   * @return The projection matrix for the light
   */

  public @Nonnull RMatrixI4x4F<RTransformProjection> getProjection()
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

  public @Nonnull Texture2DStaticUsable getTexture()
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

  /**
   * @return The colour of the light
   */

  @Override public @Nonnull RVectorI3F<RSpaceRGB> lightGetColour()
  {
    return this.colour;
  }

  /**
   * @return The identifier of the light
   */

  @Override public @Nonnull Integer lightGetID()
  {
    return this.id;
  }

  /**
   * @return The intensity of the light
   */

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public @Nonnull Option<KShadow> lightGetShadow()
  {
    return this.shadow;
  }

  @Override public boolean lightHasShadow()
  {
    return this.shadow.isSome();
  }

  @Override public
    <A, E extends Throwable, V extends KLightVisitor<A, E>>
    A
    lightVisitableAccept(
      final V v)
      throws E,
        ConstraintError,
        RException
  {
    return v.lightVisitProjective(this);
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
    return builder.toString();
  }
}
