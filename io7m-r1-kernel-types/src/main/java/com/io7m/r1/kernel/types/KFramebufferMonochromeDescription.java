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
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A description of the monochrome part of a framebuffer.
 */

@EqualityStructural public final class KFramebufferMonochromeDescription implements
  KFramebufferDescriptionType
{
  /**
   * Create a new description of a monochrome framebuffer.
   *
   * @param in_area
   *          The inclusive area of the framebuffer
   * @param in_filter_mag
   *          The magnification filter for the texture that backs the
   *          framebuffer
   * @param in_filter_min
   *          The minification filter for the texture that backs the
   *          framebuffer
   * @param in_precision_mono
   *          The desired precision of the framebuffer
   * @return A new description
   */

  public static KFramebufferMonochromeDescription newDescription(
    final AreaInclusive in_area,
    final TextureFilterMagnification in_filter_mag,
    final TextureFilterMinification in_filter_min,
    final KMonochromePrecision in_precision_mono)
  {
    return new KFramebufferMonochromeDescription(
      in_area,
      in_filter_mag,
      in_filter_min,
      in_precision_mono);
  }

  private final AreaInclusive              area;
  private final TextureFilterMagnification filter_mag;
  private final TextureFilterMinification  filter_min;
  private final KMonochromePrecision       precision_mono;

  private KFramebufferMonochromeDescription(
    final AreaInclusive in_area,
    final TextureFilterMagnification in_filter_mag,
    final TextureFilterMinification in_filter_min,
    final KMonochromePrecision in_precision_mono)
  {
    this.area = NullCheck.notNull(in_area, "Area");
    this.filter_mag =
      NullCheck.notNull(in_filter_mag, "Magnification filter");
    this.filter_min = NullCheck.notNull(in_filter_min, "Minification filter");
    this.precision_mono =
      NullCheck.notNull(in_precision_mono, "Monochrome precision");
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
    final KFramebufferMonochromeDescription other =
      (KFramebufferMonochromeDescription) obj;
    return this.area.equals(other.area)
      && (this.filter_mag == other.filter_mag)
      && (this.filter_min == other.filter_min)
      && (this.precision_mono == other.precision_mono);
  }

  /**
   * @return The inclusive area of the framebuffer
   */

  public AreaInclusive getArea()
  {
    return this.area;
  }

  /**
   * @return The magnification filter for the texture that backs the
   *         framebuffer
   */

  public TextureFilterMagnification getFilterMagnification()
  {
    return this.filter_mag;
  }

  /**
   * @return The minification filter for the texture that backs the
   *         framebuffer
   */

  public TextureFilterMinification getFilterMinification()
  {
    return this.filter_min;
  }

  /**
   * @return The desired precision of the framebuffer
   */

  public KMonochromePrecision getMonochromePrecision()
  {
    return this.precision_mono;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.area.hashCode();
    result = (prime * result) + this.filter_mag.hashCode();
    result = (prime * result) + this.filter_min.hashCode();
    result = (prime * result) + this.precision_mono.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KFramebufferMonochromeDescription area=");
    builder.append(this.area);
    builder.append(" filter_mag=");
    builder.append(this.filter_mag);
    builder.append(" filter_min=");
    builder.append(this.filter_min);
    builder.append(" precision_mono=");
    builder.append(this.precision_mono);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
