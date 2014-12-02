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
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceRGBType;

/**
 * Fog parameters.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KFogZParameters
{
  @EqualityReference private static final class Builder implements
    KFogZParametersBuilderType
  {
    private PVectorI3F<RSpaceRGBType>                   color;
    private float                                       far_z;
    private float                                       near_z;
    private KFogProgression                             progression;
    private PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> projection;

    Builder(
      final PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> in_projection)
    {
      this.color = PVectorI3F.zero();
      this.near_z = 0.0f;
      this.far_z = 8.0f;
      this.projection = NullCheck.notNull(in_projection, "Projection");
      this.progression = KFogProgression.FOG_LINEAR;
    }

    @Override public KFogZParameters build()
    {
      return new KFogZParameters(
        this.color,
        this.near_z,
        this.far_z,
        this.projection,
        this.progression);
    }

    @Override public void setColor(
      final PVectorI3F<RSpaceRGBType> c)
    {
      this.color = NullCheck.notNull(c, "Color");
    }

    @Override public void setFarZ(
      final float z)
    {
      this.far_z = z;
    }

    @Override public void setNearZ(
      final float z)
    {
      this.near_z = z;
    }

    @Override public void setProgression(
      final KFogProgression p)
    {
      this.progression = NullCheck.notNull(p, "Progression");
    }

    @Override public void setProjectionMatrix(
      final PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> p)
    {
      this.projection = NullCheck.notNull(p, "Projection");
    }
  }

  /**
   * @param in_projection
   *          The projection matrix used to render the scene.
   * @return A new parameter builder
   */

  public static KFogZParametersBuilderType newBuilder(
    final PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> in_projection)
  {
    return new Builder(in_projection);
  }

  private final PVectorI3F<RSpaceRGBType>                   color;
  private final float                                       far_z;
  private final float                                       near_z;
  private final KFogProgression                             progression;
  private final PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> projection;

  private KFogZParameters(
    final PVectorI3F<RSpaceRGBType> in_color,
    final float in_near_z,
    final float in_far_z,
    final PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> in_projection,
    final KFogProgression in_progression)
  {
    this.color = NullCheck.notNull(in_color);
    this.near_z = in_near_z;
    this.far_z = in_far_z;
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
    final KFogZParameters other = (KFogZParameters) obj;
    return this.color.equals(other.color)
      && (Float.floatToIntBits(this.far_z) == Float
        .floatToIntBits(other.far_z))
      && (Float.floatToIntBits(this.near_z) == Float
        .floatToIntBits(other.near_z))
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
   * @return The distance at which objects are completely occluded by fog (in
   *         <i>eye-space</i>).
   */

  public float getFarZ()
  {
    return this.far_z;
  }

  /**
   * @return The distance at which fog begins (in <i>eye-space</i>).
   */

  public float getNearZ()
  {
    return this.near_z;
  }

  /**
   * @return The fog progression.
   */

  public KFogProgression getProgression()
  {
    return this.progression;
  }

  /**
   * @return The projection matrix used to render the scene.
   */

  public PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> getProjection()
  {
    return this.projection;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.color.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.far_z);
    result = (prime * result) + Float.floatToIntBits(this.near_z);
    result = (prime * result) + this.projection.hashCode();
    result = (prime * result) + this.progression.hashCode();
    return result;
  }
}
