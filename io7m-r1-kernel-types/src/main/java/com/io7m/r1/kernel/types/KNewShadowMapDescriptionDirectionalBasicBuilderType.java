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

import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;

/**
 * The type of mutable builders for directional basic shadow maps.
 */

public interface KNewShadowMapDescriptionDirectionalBasicBuilderType
{
  /**
   * @return A description based on the most recently given parameters.
   */

  KNewShadowMapDescriptionDirectionalBasic build();

  /**
   * Set the depth precision for the map
   *
   * @param p
   *          The precision
   */

  void setDepthPrecision(
    final KDepthPrecision p);

  /**
   * Set the magnification filter for the map.
   *
   * @param f
   *          The filter
   */

  void setMagnificationFilter(
    final TextureFilterMagnification f);

  /**
   * Set the minification filter for the map.
   *
   * @param f
   *          The filter
   */

  void setMinificationFilter(
    final TextureFilterMinification f);

  /**
   * Set the size exponent for the map. The resulting map will be (2ⁿ * 2ⁿ)
   * texels.
   *
   * @param n
   *          The exponent
   */

  void setSizeExponent(
    int n);
}
