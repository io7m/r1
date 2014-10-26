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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A description of the RGBA part of a framebuffer.
 */

@SuppressWarnings("synthetic-access") @EqualityStructural public final class KFramebufferDeferredDescription implements
  KFramebufferDescriptionType
{
  @EqualityReference private static final class Builder implements
    KFramebufferDeferredDescriptionBuilderType
  {
    private AreaInclusive              area;
    private TextureFilterMagnification mag;
    private TextureFilterMinification  min;
    private KNormalPrecision           normal_prec;
    private KRGBAPrecision             rgba_prec;

    Builder(
      final AreaInclusive in_area)
    {
      this.area = NullCheck.notNull(in_area, "Area");
      this.mag = TextureFilterMagnification.TEXTURE_FILTER_LINEAR;
      this.min = TextureFilterMinification.TEXTURE_FILTER_LINEAR;
      this.rgba_prec = KRGBAPrecision.RGBA_PRECISION_8;
      this.normal_prec = KNormalPrecision.NORMAL_PRECISION_16F;
    }

    @Override public KFramebufferDeferredDescription build()
    {
      final KFramebufferRGBADescription new_rgba_desc =
        KFramebufferRGBADescription.newDescription(
          this.area,
          this.mag,
          this.min,
          this.rgba_prec);

      final KGeometryBufferDescriptionBuilderType gbb =
        KGeometryBufferDescription.newBuilder(this.area);
      gbb.setNormalPrecision(this.normal_prec);
      final KGeometryBufferDescription new_gbuffer_desc = gbb.build();

      return new KFramebufferDeferredDescription(
        new_rgba_desc,
        new_gbuffer_desc);
    }

    @Override public void setArea(
      final AreaInclusive a)
    {
      this.area = NullCheck.notNull(a, "Area");
    }

    @Override public void setGBufferNormalPrecision(
      final KNormalPrecision p)
    {
      this.normal_prec = NullCheck.notNull(p, "Precision");
    }

    @Override public void setRGBAMagnificationFilter(
      final TextureFilterMagnification f)
    {
      this.mag = NullCheck.notNull(f, "Filter");
    }

    @Override public void setRGBAMinificationFilter(
      final TextureFilterMinification f)
    {
      this.min = NullCheck.notNull(f, "Filter");
    }

    @Override public void setRGBAPrecision(
      final KRGBAPrecision p)
    {
      this.rgba_prec = NullCheck.notNull(p, "RGBA precision");
    }
  }

  /**
   * @return A new mutable builder for producing framebuffer descriptions
   * @param in_area
   *          The inclusive area of the framebuffer
   */

  public static KFramebufferDeferredDescriptionBuilderType newBuilder(
    final AreaInclusive in_area)
  {
    return new Builder(in_area);
  }

  private final KGeometryBufferDescription  gbuffer_desc;
  private final KFramebufferRGBADescription rgba_desc;

  private KFramebufferDeferredDescription(
    final KFramebufferRGBADescription in_rgba_desc,
    final KGeometryBufferDescription in_gbuffer_desc)
  {
    this.rgba_desc = NullCheck.notNull(in_rgba_desc, "RGBA description");
    this.gbuffer_desc =
      NullCheck.notNull(in_gbuffer_desc, "GBuffer description");
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
    final KFramebufferDeferredDescription other =
      (KFramebufferDeferredDescription) obj;
    return this.gbuffer_desc.equals(other.gbuffer_desc)
      && this.rgba_desc.equals(other.rgba_desc);
  }

  /**
   * @return The inclusive area of the framebuffer
   */

  public AreaInclusive getArea()
  {
    return this.gbuffer_desc.getArea();
  }

  /**
   * @return The geometry buffer description
   */

  public KGeometryBufferDescription getGeometryBufferDescription()
  {
    return this.gbuffer_desc;
  }

  /**
   * @return The description of the renderable RGBA part of the framebuffer
   */

  public KFramebufferRGBADescription getRGBADescription()
  {
    return this.rgba_desc;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.gbuffer_desc.hashCode();
    result = (prime * result) + this.rgba_desc.hashCode();
    return result;
  }
}
