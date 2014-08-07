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
 * A mutable builder interface for constructing parameters.
 */

public interface KBlurParametersBuilderType
{
  /**
   * @return A new set of blur parameters initialized to all of the values
   *         given to the builder so far.
   */

  KBlurParameters build();

  /**
   * <p>
   * Set the blur size. A blur size larger than <code>1.0</code> will
   * typically result in visible banding.
   * </p>
   * <p>
   * The default is <code>1.0</code>.
   * </p>
   * 
   * @param size
   *          The size of the blur effect.
   */

  void setBlurSize(
    final float size);

  /**
   * <p>
   * Set the number of passes. A greater number of passes will strengthen the
   * effect of the blur at the cost of processing time. A value of
   * <code>0</code> will result in no blur being applied at all.
   * </p>
   * <p>
   * The default is <code>1</code>.
   * </p>
   * 
   * @param passes
   *          The number of passes
   */

  void setPasses(
    int passes);

  /**
   * <p>
   * The amount of downsampling to be performed during blurring. A value of
   * <code>1.0</code> implies no scaling. A value of <code>0.5</code> will
   * halve the width and height of the original image before blurring. The
   * more downsampling applied, the stronger the effect of the blur (but the
   * greater the loss of image precision).
   * </p>
   * <p>
   * The default is <code>1</code>.
   * </p>
   * 
   * @param scale
   *          The coefficient by which to multiply the dimensions of the image
   */

  void setScale(
    final float scale);
}
