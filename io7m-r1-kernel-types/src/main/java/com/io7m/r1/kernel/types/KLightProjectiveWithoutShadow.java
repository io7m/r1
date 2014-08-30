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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionLightMissingTexture;
import com.io7m.r1.types.RExceptionUserError;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;
import com.io7m.r1.types.RVectorI3F;

/**
 * <p>
 * A projective light without a shadow.
 * </p>
 */

@EqualityReference public final class KLightProjectiveWithoutShadow implements
  KLightProjectiveType
{
  @SuppressWarnings("synthetic-access") @EqualityReference private static final class Builder implements
    KLightProjectiveWithoutShadowBuilderType
  {
    private RVectorI3F<RSpaceRGBType>           color;
    private float                               falloff;
    private float                               intensity;
    private QuaternionI4F                       orientation;
    private RVectorI3F<RSpaceWorldType>         position;
    private KProjectionType                     projection;
    private float                               range;
    private @Nullable Texture2DStaticUsableType texture;

    Builder(
      final KLightProjectiveWithoutShadow in_original)
    {
      NullCheck.notNull(in_original, "Light");
      this.color = in_original.color;
      this.intensity = in_original.intensity;
      this.falloff = in_original.falloff;
      this.position = in_original.position;
      this.orientation = in_original.orientation;
      this.projection = in_original.projection;
      this.range = in_original.range;
      this.texture = in_original.texture;
    }

    Builder(
      final Texture2DStaticUsableType in_texture,
      final KProjectionType in_projection)
    {
      this.color = RVectorI3F.one();
      this.intensity = 1.0f;
      this.falloff = 1.0f;
      this.position = RVectorI3F.zero();
      this.orientation = QuaternionI4F.IDENTITY;
      this.projection = NullCheck.notNull(in_projection, "Projection");
      this.range = 8.0f;
      this.texture = NullCheck.notNull(in_texture, "Texture");
    }

    @Override public KLightProjectiveWithoutShadow build()
      throws RException,
        RExceptionUserError
    {
      final Texture2DStaticUsableType t = this.texture;
      if (t == null) {
        throw new RExceptionLightMissingTexture(
          "No texture specified for projective light");
      }

      return new KLightProjectiveWithoutShadow(
        t,
        this.position,
        this.orientation,
        this.color,
        this.intensity,
        this.range,
        this.falloff,
        this.projection);
    }

    @Override public void setColor(
      final RVectorI3F<RSpaceRGBType> in_color)
    {
      this.color = NullCheck.notNull(in_color, "Color");
    }

    @Override public void setFalloff(
      final float in_exponent)
    {
      this.falloff = in_exponent;
    }

    @Override public void setIntensity(
      final float in_intensity)
    {
      this.intensity = in_intensity;
    }

    @Override public void setOrientation(
      final QuaternionI4F in_orientation)
    {
      this.orientation = NullCheck.notNull(in_orientation, "Orientation");
    }

    @Override public void setPosition(
      final RVectorI3F<RSpaceWorldType> in_position)
    {
      this.position = NullCheck.notNull(in_position, "Position");
    }

    @Override public void setProjection(
      final KProjectionType in_projection)
    {
      this.projection = NullCheck.notNull(in_projection, "Projection");
    }

    @Override public void setRange(
      final float in_range)
    {
      this.range = in_range;
    }

    @Override public void setTexture(
      final Texture2DStaticUsableType in_texture)
    {
      this.texture = NullCheck.notNull(in_texture, "Texture");
    }
  }

  private static final VectorI3F ONE = new VectorI3F(1.0f, 1.0f, 1.0f);

  /**
   * <p>
   * Create a builder for creating new projective lights.
   * </p>
   *
   * @param in_texture
   *          The texture.
   * @param in_projection
   *          The projection.
   * @return A new light builder.
   */

  public static KLightProjectiveWithoutShadowBuilderType newBuilder(
    final Texture2DStaticUsableType in_texture,
    final KProjectionType in_projection)
  {
    return new Builder(in_texture, in_projection);
  }

  /**
   * <p>
   * Create a builder for creating new spherical lights. The builder will be
   * initialized to values based on the given light.
   * </p>
   *
   * @param p
   *          The initial light.
   * @return A new light builder.
   */

  public static KLightProjectiveWithoutShadowBuilderType newBuilderFrom(
    final KLightProjectiveWithoutShadow p)
  {
    return new Builder(p);
  }

  private final RVectorI3F<RSpaceRGBType>   color;
  private final float                       falloff;
  private final float                       intensity;
  private final QuaternionI4F               orientation;
  private final RVectorI3F<RSpaceWorldType> position;
  private final KProjectionType             projection;
  private final float                       range;
  private final Texture2DStaticUsableType   texture;
  private final int                         textures;
  private final KTransformType              transform;

  private KLightProjectiveWithoutShadow(
    final Texture2DStaticUsableType in_texture,
    final RVectorI3F<RSpaceWorldType> in_position,
    final QuaternionI4F in_orientation,
    final RVectorI3F<RSpaceRGBType> in_color,
    final float in_intensity,
    final float in_range,
    final float in_falloff,
    final KProjectionType in_projection)
  {
    this.intensity = in_intensity;
    this.color = NullCheck.notNull(in_color, "Color");
    this.position = NullCheck.notNull(in_position, "Position");
    this.orientation = NullCheck.notNull(in_orientation, "Orientation");
    this.range = in_range;
    this.falloff = in_falloff;
    this.projection = NullCheck.notNull(in_projection, "Projection");
    this.texture = NullCheck.notNull(in_texture, "Texture");

    this.transform =
      KTransformOST.newTransform(
        this.orientation,
        KLightProjectiveWithoutShadow.ONE,
        this.position);

    /**
     * One texture for the light.
     */

    this.textures = 1;
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

  @Override public String lightGetCode()
  {
    return "LProj";
  }

  @Override public RVectorI3F<RSpaceRGBType> lightGetColor()
  {
    return this.color;
  }

  @Override public float lightGetIntensity()
  {
    return this.intensity;
  }

  @Override public KTransformType lightGetTransform()
  {
    return this.transform;
  }

  @Override public float lightProjectiveGetFalloff()
  {
    return this.falloff;
  }

  @Override public QuaternionI4F lightProjectiveGetOrientation()
  {
    return this.orientation;
  }

  @Override public RVectorI3F<RSpaceWorldType> lightProjectiveGetPosition()
  {
    return this.position;
  }

  @Override public KProjectionType lightProjectiveGetProjection()
  {
    return this.projection;
  }

  @Override public float lightProjectiveGetRange()
  {
    return this.range;
  }

  @Override public Texture2DStaticUsableType lightProjectiveGetTexture()
  {
    return this.texture;
  }

  @Override public
    <A, E extends Throwable, V extends KLightProjectiveVisitorType<A, E>>
    A
    projectiveAccept(
      final V v)
      throws RException,
        E
  {
    return v.projectiveWithoutShadow(this);
  }

  @Override public int texturesGetRequired()
  {
    return this.textures;
  }
}
