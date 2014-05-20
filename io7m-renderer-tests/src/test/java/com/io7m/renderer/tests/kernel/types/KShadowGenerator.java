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

package com.io7m.renderer.tests.kernel.types;

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KBlurParametersBuilderType;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KDepthVariancePrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.tests.EnumGenerator;

public final class KShadowGenerator implements Generator<KShadowType>
{
  private static final double                             SHADOW_TYPE_COUNT =
                                                                              2;

  private final EnumGenerator<TextureFilterMagnification> filter_mag_gen;
  private final EnumGenerator<TextureFilterMinification>  filter_min_gen;
  private final EnumGenerator<KDepthPrecision>            depth_prec_gen;
  private final EnumGenerator<KDepthVariancePrecision>    depth_var_prec_gen;

  public KShadowGenerator()
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

  @Override public KShadowType next()
  {
    final int t = (int) (Math.random() * KShadowGenerator.SHADOW_TYPE_COUNT);
    final float factor_min = (float) Math.random();
    final int in_size_exponent = (int) (1 + (Math.random() * 9));

    switch (t) {
      case 0:
      {
        final TextureFilterMagnification in_filter_mag =
          this.filter_mag_gen.next();
        final TextureFilterMinification in_filter_min =
          this.filter_min_gen.next();
        final KDepthPrecision in_precision_depth = this.depth_prec_gen.next();

        final KFramebufferDepthDescription in_description =
          KFramebufferDepthDescription.newDescription(
            new AreaInclusive(
              new RangeInclusiveL(0, 99),
              new RangeInclusiveL(0, 99)),
            in_filter_mag,
            in_filter_min,
            in_precision_depth);
        final float depth_bias = (float) Math.random();

        final KShadowMapBasicDescription description =
          KShadowMapBasicDescription.newDescription(
            in_description,
            in_size_exponent);

        return KShadowMappedBasic.newMappedBasic(
          depth_bias,
          factor_min,
          description);
      }
      case 1:
      {
        final float minimum_variance = (float) Math.random();
        final float light_bleed_reduction = (float) Math.random();
        final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
        bb.setBlurSize((float) Math.random());
        bb.setPasses((int) (Math.random() * 10));
        bb.setScale((float) Math.random());
        final KBlurParameters in_blur = bb.build();

        final TextureFilterMagnification in_filter_mag =
          this.filter_mag_gen.next();
        final TextureFilterMinification in_filter_min =
          this.filter_min_gen.next();
        final KDepthPrecision in_precision_depth = this.depth_prec_gen.next();

        final KDepthVariancePrecision in_precision_variance =
          this.depth_var_prec_gen.next();
        final KFramebufferDepthVarianceDescription in_description =
          KFramebufferDepthVarianceDescription.newDescription(
            new AreaInclusive(
              new RangeInclusiveL(0, 99),
              new RangeInclusiveL(0, 99)),
            in_filter_mag,
            in_filter_min,
            in_precision_depth,
            in_precision_variance);

        final KShadowMapVarianceDescription description =
          KShadowMapVarianceDescription.newDescription(
            in_description,
            in_size_exponent);

        return KShadowMappedVariance.newMappedVariance(
          factor_min,
          minimum_variance,
          light_bleed_reduction,
          in_blur,
          description);
      }
      default:
      {
        throw new UnreachableCodeException();
      }
    }
  }
}
