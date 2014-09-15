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

/**
 * The type of mutable builders for omnidirectional (dual paraboloid) basic
 * mapped shadows.
 */

public interface KShadowOmnidirectionalDualParaboloidMappedBasicBuilderType extends
  KShadowMappedBasicBuilderType
{
  /**
   * @return A shadow based on the most recent parameters given.
   */

  KShadowOmnidirectionalDualParaboloidMappedBasic build();

  /**
   * <p>
   * Set the shadow map description. This controls the physical aspects of the
   * shadow map such as the size, precision, and filter settings.
   * </p>
   *
   * @param m
   *          The description.
   */

  void setMapDescription(
    final KShadowMapDescriptionOmnidirectionalDualParaboloidBasic m);
}
