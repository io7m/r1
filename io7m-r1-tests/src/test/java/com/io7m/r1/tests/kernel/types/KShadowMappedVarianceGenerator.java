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
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KDepthVariancePrecision;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVariance;
import com.io7m.r1.kernel.types.KShadowMapDescriptionVarianceBuilderType;
import com.io7m.r1.kernel.types.KShadowMappedVariance;
import com.io7m.r1.kernel.types.KShadowMappedVarianceBuilderType;
import com.io7m.r1.tests.EnumGenerator;

public final class KShadowMappedVarianceGenerator implements
  Generator<KShadowMappedVariance>
{
  private final EnumGenerator<TextureFilterMagnification> filter_mag_gen;
  private final EnumGenerator<TextureFilterMinification>  filter_min_gen;
  private final EnumGenerator<KDepthPrecision>            depth_prec_gen;
  private final EnumGenerator<KDepthVariancePrecision>    depth_var_prec_gen;

  public KShadowMappedVarianceGenerator()
  {
    this.filter_mag_gen =
      new EnumGenerator<TextureFilterMagnification>(
        TextureFilterMagnification.class);
    this.filter_min_gen =
      new EnumGenerator<TextureFilterMinification>(
        TextureFilterMinification.class);
    this.depth_prec_gen =
      new EnumGenerator<KDepthPrecision>(KDepthPrecision.class);
    this.depth_var_prec_gen =
      new EnumGenerator<KDepthVariancePrecision>(
        KDepthVariancePrecision.class);
  }

  @Override public KShadowMappedVariance next()
  {
    final TextureFilterMagnification in_filter_mag =
      this.filter_mag_gen.next();
    final TextureFilterMinification in_filter_min =
      this.filter_min_gen.next();
    final KDepthPrecision in_precision_depth = this.depth_prec_gen.next();
    final KDepthVariancePrecision in_var_prec =
      this.depth_var_prec_gen.next();

    final int exponent = (int) ((Math.random() * 32) + 1);

    final KShadowMapDescriptionVarianceBuilderType smb_map_b =
      KShadowMapDescriptionVariance.newBuilder();
    smb_map_b.setDepthVariancePrecision(in_var_prec);
    smb_map_b.setDepthPrecision(in_precision_depth);
    smb_map_b.setMagnificationFilter(in_filter_mag);
    smb_map_b.setMinificationFilter(in_filter_min);
    smb_map_b.setSizeExponent(exponent);

    final KBlurParametersBuilderType bp_b = KBlurParameters.newBuilder();
    bp_b.setBlurSize((float) Math.random());
    bp_b.setPasses((int) (Math.random() * 8));
    bp_b.setScale((float) Math.random());
    final KBlurParameters bp = bp_b.build();

    final KShadowMappedVarianceBuilderType bb =
      KShadowMappedVariance.newBuilder();
    bb.setLightBleedReduction((float) Math.random());
    bb.setMinimumVariance((float) Math.random());
    bb.setMinimumFactor((float) Math.random());
    bb.setBlurParameters(bp);
    return bb.build();
  }
}
