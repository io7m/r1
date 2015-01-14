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

package com.io7m.r1.kernel;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * Fog parameters.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KFogYParameters
{
  @EqualityReference private static final class Builder implements
    KFogYParametersBuilderType
  {
    private PVectorI3F<RSpaceRGBType>                    color;
    private float                                        lower_y;
    private KFogProgression                              progression;
    private KProjectionType                              projection;
    private float                                        upper_y;
    private PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view;

    Builder(
      final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> in_view,
      final KProjectionType in_projection)
    {
      this.color = PVectorI3F.zero();
      this.lower_y = 0.0f;
      this.upper_y = 8.0f;
      this.view = NullCheck.notNull(in_view, "View");
      this.projection = NullCheck.notNull(in_projection, "Projection");
      this.progression = KFogProgression.FOG_LINEAR;
    }

    @Override public KFogYParameters build()
    {
      return new KFogYParameters(
        this.color,
        this.lower_y,
        this.upper_y,
        this.view,
        this.projection,
        this.progression);
    }

    @Override public void setColor(
      final PVectorI3F<RSpaceRGBType> c)
    {
      this.color = NullCheck.notNull(c, "Color");
    }

    @Override public void setLowerY(
      final float y)
    {
      this.lower_y = y;
    }

    @Override public void setProgression(
      final KFogProgression p)
    {
      this.progression = NullCheck.notNull(p, "Progression");
    }

    @Override public void setProjection(
      final KProjectionType p)
    {
      this.projection = NullCheck.notNull(p, "Projection");
    }

    @Override public void setViewMatrix(
      final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> v)
    {
      this.view = NullCheck.notNull(v, "View matrix");
    }

    @Override public void setUpperY(
      final float y)
    {
      this.upper_y = y;
    }
  }

  /**
   * @param in_view
   *          The view matrix used to render the scene.
   * @param in_projection
   *          The projection used to render the scene.
   * @return A new parameter builder
   */

  public static KFogYParametersBuilderType newBuilder(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> in_view,
    final KProjectionType in_projection)
  {
    return new Builder(in_view, in_projection);
  }

  private final PVectorI3F<RSpaceRGBType>                    color;
  private final float                                        lower_y;
  private final KFogProgression                              progression;
  private final KProjectionType                              projection;
  private final float                                        upper_y;
  private final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view;

  private KFogYParameters(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_lower_y,
    final float in_upper_y,
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> in_view,
    final KProjectionType in_projection,
    final KFogProgression in_progression)
  {
    this.color = NullCheck.notNull(in_color);
    this.lower_y = in_lower_y;
    this.upper_y = in_upper_y;
    this.view = NullCheck.notNull(in_view, "View");
    this.projection = NullCheck.notNull(in_projection, "Projection");
    this.progression = NullCheck.notNull(in_progression, "Progression");
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
    final KFogYParameters other = (KFogYParameters) obj;
    return this.color.equals(other.color)
      && (Float.floatToIntBits(this.upper_y) == Float
        .floatToIntBits(other.upper_y))
      && (Float.floatToIntBits(this.lower_y) == Float
        .floatToIntBits(other.lower_y))
      && this.projection.equals(other.projection)
      && this.progression.equals(other.progression);
  }

  /**
   * @return The fog color.
   */

  public PVectorI3F<RSpaceRGBType> getColor()
  {
    return this.color;
  }

  /**
   * @return The distance at which fog begins (in <i>eye-space</i>).
   */

  public float getLowerY()
  {
    return this.lower_y;
  }

  /**
   * @return The fog progression.
   */

  public KFogProgression getProgression()
  {
    return this.progression;
  }

  /**
   * @return The view matrix used to render the scene.
   */

  public PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> getView()
  {
    return this.view;
  }

  /**
   * @return The projection matrix used to render the scene.
   */

  public KProjectionType getProjection()
  {
    return this.projection;
  }

  /**
   * @return The distance at which objects are completely occluded by fog (in
   *         <i>eye-space</i>).
   */

  public float getUpperY()
  {
    return this.upper_y;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.color.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.upper_y);
    result = (prime * result) + Float.floatToIntBits(this.lower_y);
    result = (prime * result) + this.projection.hashCode();
    result = (prime * result) + this.progression.hashCode();
    return result;
  }
}
