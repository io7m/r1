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

/**
 * Parameters for blur effects.
 */

@Immutable public final class KBlurParameters
{
  /**
   * A mutable builder interface for constructing parameters.
   */

  public interface Builder
  {
    /**
     * @return A new set of blur parameters initialized to all of the values
     *         given to the builder so far.
     */

    @Nonnull KBlurParameters build();

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
     * Set the number of passes. A greater number of passes will strengthen
     * the effect of the blur at the cost of processing time.
     * </p>
     * <p>
     * The default is <code>1</code>.
     * </p>
     * 
     * @param passes
     *          The number of passes
     * @throws ConstraintError
     *           Iff <code>1 &lt;= passes == false</code>
     */

    void setPasses(
      int passes)
      throws ConstraintError;

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
     *          The coefficient by which to multiply the dimensions of the
     *          image
     * @throws ConstraintError
     *           Iff <code>0.0 &lt; scale &lt;= 1.0 == false</code>
     */

    void setScale(
      final float scale)
      throws ConstraintError;
  }

  /**
   * @return A new parameter builder
   */

  public static @Nonnull Builder newBuilder()
  {
    return new Builder() {
      private float blur_size = 1.0f;
      private int   passes    = 1;
      private float scale     = 1.0f;

      @SuppressWarnings("synthetic-access") @Override public
        KBlurParameters
        build()
      {
        return new KBlurParameters(this.blur_size, this.scale, this.passes);
      }

      @Override public void setBlurSize(
        final float size)
      {
        this.blur_size = size;
      }

      @Override public void setPasses(
        final int in_passes)
        throws ConstraintError
      {
        this.passes =
          Constraints.constrainRange(
            in_passes,
            1,
            Integer.MAX_VALUE,
            "Passes");
      }

      @Override public void setScale(
        final float in_scale)
        throws ConstraintError
      {
        this.scale =
          Constraints.constrainRange(in_scale, 0.0f, 1.0f, "Scale");
      }
    };
  }

  private final float blur_size;
  private final int   passes;
  private final float scale;

  private KBlurParameters(
    final float in_blur_size,
    final float in_scale,
    final int in_passes)
  {
    this.blur_size = in_blur_size;
    this.scale = in_scale;
    this.passes = in_passes;
  }

  @Override public boolean equals(
    final Object obj)
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
    final KBlurParameters other = (KBlurParameters) obj;
    if (Float.floatToIntBits(this.blur_size) != Float
      .floatToIntBits(other.blur_size)) {
      return false;
    }
    if (this.passes != other.passes) {
      return false;
    }
    if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
      return false;
    }
    return true;
  }

  /**
   * @return The blur size
   */

  public float getBlurSize()
  {
    return this.blur_size;
  }

  /**
   * @return The number of blur passes
   */

  public int getPasses()
  {
    return this.passes;
  }

  /**
   * @return The downsampling coefficient
   */

  public float getScale()
  {
    return this.scale;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.blur_size);
    result = (prime * result) + this.passes;
    result = (prime * result) + Float.floatToIntBits(this.scale);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder s = new StringBuilder();
    s.append("[KBlurParameters blur_size=");
    s.append(this.blur_size);
    s.append(" scale=");
    s.append(this.scale);
    s.append(" passes=");
    s.append(this.passes);
    s.append("]");
    return s.toString();
  }
}
