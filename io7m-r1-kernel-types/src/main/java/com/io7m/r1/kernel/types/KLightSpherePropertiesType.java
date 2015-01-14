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

import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * Readable properties of spherical lights.
 */

public interface KLightSpherePropertiesType extends KLightPropertiesType
{
  /**
   * @return The falloff exponent for the light
   */

  float lightGetFalloff();

  /**
   * @return The inverse falloff exponent for the light (1.0 /
   *         {@link #lightGetFalloff()}).
   */

  float lightGetFalloffInverse();

  /**
   * @return The world position of the light
   */

  PVectorI3F<RSpaceWorldType> lightGetPosition();

  /**
   * @return The radius of the light
   */

  float lightGetRadius();

  /**
   * @return The inverse radius of the light (1.0 / {@link #lightGetRadius()}
   *         ).
   */

  float lightGetRadiusInverse();

}
