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
 * The common functions shared by directional and omnidirectional basic mapped
 * shadow builders.
 */

public interface KShadowMappedBasicBuilderType extends KShadowBuilderType
{
  /**
   * <p>
   * Set the depth bias value. This is a small value added to the depth of all
   * shadow casting objects to alleviate "shadow acne" caused by objects
   * shadowing themselves. A value of <tt>0.001f</tt> tends to work well for
   * most scenes, and is the default.
   * </p>
   *
   * @param b
   *          The depth bias.
   */

  void setDepthBias(
    float b);
}
