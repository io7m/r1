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

import javax.annotation.concurrent.Immutable;

@Immutable final class KMaterialRefractive
{
  private final float mix;
  private final float scale;

  KMaterialRefractive(
    final float in_scale,
    final float in_mix)
  {
    this.scale = in_scale;
    this.mix = in_mix;
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
    final KMaterialRefractive other = (KMaterialRefractive) obj;
    if (Float.floatToIntBits(this.mix) != Float.floatToIntBits(other.mix)) {
      return false;
    }
    if (Float.floatToIntBits(this.scale) != Float.floatToIntBits(other.scale)) {
      return false;
    }
    return true;
  }

  public float getMix()
  {
    return this.mix;
  }

  public float getScale()
  {
    return this.scale;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + Float.floatToIntBits(this.scale);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialRefractive scale=");
    builder.append(this.scale);
    builder.append(" mix=");
    builder.append(this.mix);
    builder.append("]");
    return builder.toString();
  }
}
