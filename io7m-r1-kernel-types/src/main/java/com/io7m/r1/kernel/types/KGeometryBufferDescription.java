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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A description of a geometry buffer.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KGeometryBufferDescription implements
  KFramebufferDescriptionType
{
  @EqualityReference private static final class Builder implements
    KGeometryBufferDescriptionBuilderType
  {
    private AreaInclusive    area;
    private KNormalPrecision normal_prec;

    Builder(
      final AreaInclusive in_area)
    {
      this.area = NullCheck.notNull(in_area, "Area");
      this.normal_prec = KNormalPrecision.NORMAL_PRECISION_16F;
    }

    @Override public KGeometryBufferDescription build()
    {
      return new KGeometryBufferDescription(this.area, this.normal_prec);
    }

    @Override public void setArea(
      final AreaInclusive a)
    {
      this.area = NullCheck.notNull(a, "Area");
    }

    @Override public void setNormalPrecision(
      final KNormalPrecision p)
    {
      this.normal_prec = NullCheck.notNull(p, "Precision");
    }
  }

  /**
   * @return A new mutable builder for producing framebuffer descriptions
   * @param in_area
   *          The inclusive area of the framebuffer
   */

  public static KGeometryBufferDescriptionBuilderType newBuilder(
    final AreaInclusive in_area)
  {
    return new Builder(in_area);
  }

  private final AreaInclusive    area;
  private final KNormalPrecision precision_normal;

  private KGeometryBufferDescription(
    final AreaInclusive in_area,
    final KNormalPrecision in_precision_normal)
  {
    this.area = NullCheck.notNull(in_area, "Area");
    this.precision_normal =
      NullCheck.notNull(in_precision_normal, "RGBA precision");
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
    final KGeometryBufferDescription other = (KGeometryBufferDescription) obj;
    if (!this.area.equals(other.area)) {
      return false;
    }
    if (this.precision_normal != other.precision_normal) {
      return false;
    }
    return true;
  }

  /**
   * @return The desired framebuffer area
   */

  public AreaInclusive getArea()
  {
    return this.area;
  }

  /**
   * @return The desired precision of normal vectors
   */

  public KNormalPrecision getPrecisionNormal()
  {
    return this.precision_normal;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.area.hashCode();
    result = (prime * result) + this.precision_normal.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KGeometryBufferDescription area=");
    b.append(this.area);
    b.append(", precision_normal=");
    b.append(this.precision_normal);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
