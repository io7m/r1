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

package com.io7m.r1.tests.kernel.types;

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KShadowDirectionalMappedBasic;
import com.io7m.r1.kernel.types.KShadowDirectionalMappedBasicBuilderType;
import com.io7m.r1.kernel.types.KShadowMapDescriptionBuilderType;
import com.io7m.r1.kernel.types.KShadowMapDescriptionDirectionalBasic;
import com.io7m.r1.tests.EnumGenerator;

public final class KShadowDirectionalMappedBasicGenerator implements
  Generator<KShadowDirectionalMappedBasic>
{
  private final EnumGenerator<TextureFilterMagnification> filter_mag_gen;
  private final EnumGenerator<TextureFilterMinification>  filter_min_gen;
  private final EnumGenerator<KDepthPrecision>            depth_prec_gen;

  public KShadowDirectionalMappedBasicGenerator()
  {
    this.filter_mag_gen =
      new EnumGenerator<TextureFilterMagnification>(
        TextureFilterMagnification.class);
    this.filter_min_gen =
      new EnumGenerator<TextureFilterMinification>(
        TextureFilterMinification.class);
    this.depth_prec_gen =
      new EnumGenerator<KDepthPrecision>(KDepthPrecision.class);
  }

  @Override public KShadowDirectionalMappedBasic next()
  {
    final TextureFilterMagnification in_filter_mag =
      this.filter_mag_gen.next();
    final TextureFilterMinification in_filter_min =
      this.filter_min_gen.next();
    final KDepthPrecision in_precision_depth = this.depth_prec_gen.next();

    final int exponent = (int) ((Math.random() * 32) + 1);
    final float depth_bias = (float) Math.random();

    final KShadowMapDescriptionBuilderType smb_map_b =
      KShadowMapDescriptionDirectionalBasic.newBuilder();
    smb_map_b.setDepthPrecision(in_precision_depth);
    smb_map_b.setMagnificationFilter(in_filter_mag);
    smb_map_b.setMinificationFilter(in_filter_min);
    smb_map_b.setSizeExponent(exponent);

    final KShadowDirectionalMappedBasicBuilderType bb =
      KShadowDirectionalMappedBasic.newBuilder();
    bb.setDepthBias(depth_bias);
    bb.setMinimumFactor((float) Math.random());
    return bb.build();
  }
}
