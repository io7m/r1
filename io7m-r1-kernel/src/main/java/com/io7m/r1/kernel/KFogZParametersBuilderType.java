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

import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceRGBType;

/**
 * Mutable builder for {@link KFogZParameters}.
 */

public interface KFogZParametersBuilderType
{
  /**
   * @return Parameters based on all of the values given so far.
   */

  KFogZParameters build();

  /**
   * Set the fog color.
   *
   * @param c
   *          The color
   */

  void setColor(
    PVectorI3F<RSpaceRGBType> c);

  /**
   * Set the far Z distance (in <i>eye-space</i>) at which objects are
   * completely occluded by fog.
   *
   * @param z
   *          The Z distance.
   */

  void setFarZ(
    float z);

  /**
   * Set the near Z distance (in <i>eye-space</i>) at which objects begin to
   * be affected by fog.
   *
   * @param z
   *          The Z distance.
   */

  void setNearZ(
    float z);

  /**
   * Set the fog progression.
   *
   * @param p
   *          The fog progression.
   */

  void setProgression(
    KFogProgression p);

  /**
   * Set the projection matrix that was used to render the scene being
   * filtered.
   *
   * @param p
   *          The projection matrix.
   */

  void setProjectionMatrix(
    PMatrixI4x4F<RSpaceEyeType, RSpaceClipType> p);
}
