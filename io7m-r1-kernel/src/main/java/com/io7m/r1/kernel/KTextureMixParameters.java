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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * Parameters for {@link KImageSourceRGBAMix}.
 */

@EqualityStructural public final class KTextureMixParameters
{
  /**
   * Construct new parameters for mixing textures.
   *
   * @param left
   *          The left texture
   * @param left_matrix
   *          The matrix for the left texture
   * @param mix
   *          The mix value
   * @param right
   *          The right texture
   * @param right_matrix
   *          The matrix for the right texture
   * @return New parameters
   */

  public static KTextureMixParameters newParameters(
    final Texture2DStaticUsableType left,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> left_matrix,
    final float mix,
    final Texture2DStaticUsableType right,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> right_matrix)
  {
    return new KTextureMixParameters(
      left,
      left_matrix,
      mix,
      right,
      right_matrix);
  }

  private final Texture2DStaticUsableType                          left;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> left_matrix;
  private final float                                              mix;
  private final Texture2DStaticUsableType                          right;
  private final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> right_matrix;

  private KTextureMixParameters(
    final Texture2DStaticUsableType in_left,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_left_matrix,
    final float in_mix,
    final Texture2DStaticUsableType in_right,
    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> in_right_matrix)
  {
    this.left = NullCheck.notNull(in_left, "Left");
    this.left_matrix = NullCheck.notNull(in_left_matrix, "Left matrix");
    this.right = NullCheck.notNull(in_right, "Right");
    this.right_matrix = NullCheck.notNull(in_right_matrix, "Right matrix");
    this.mix = in_mix;
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
    final KTextureMixParameters other = (KTextureMixParameters) obj;
    return this.left.equals(other.left)
      && this.left_matrix.equals(other.left_matrix)
      && (Float.floatToIntBits(this.mix) == Float.floatToIntBits(other.mix))
      && this.right.equals(other.right)
      && this.right_matrix.equals(other.right_matrix);
  }

  /**
   * @return The matrix for the left texture coordinates.
   */

  public PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> getLeftMatrix()
  {
    return this.left_matrix;
  }

  /**
   * @return The left texture.
   */

  public Texture2DStaticUsableType getLeftTexture()
  {
    return this.left;
  }

  /**
   * @return The mix value.
   */

  public float getMix()
  {
    return this.mix;
  }

  /**
   * @return The matrix for the right texture coordinates.
   */

  public PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> getRightMatrix()
  {
    return this.right_matrix;
  }

  /**
   * @return The right texture.
   */

  public Texture2DStaticUsableType getRightTexture()
  {
    return this.right;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.left.hashCode();
    result = (prime * result) + this.left_matrix.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + this.right.hashCode();
    result = (prime * result) + this.right_matrix.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KTextureMixParameters left=");
    b.append(this.left);
    b.append(" left_matrix=");
    b.append(this.left_matrix);
    b.append(" mix=");
    b.append(this.mix);
    b.append(" right=");
    b.append(this.right);
    b.append(" right_matrix=");
    b.append(this.right_matrix);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
