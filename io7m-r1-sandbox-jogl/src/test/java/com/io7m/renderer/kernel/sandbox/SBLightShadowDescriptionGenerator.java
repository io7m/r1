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

package com.io7m.renderer.kernel.sandbox;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.IntegerGenerator;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
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

public final class SBLightShadowDescriptionGenerator implements
  Generator<KShadowType>
{
  private final IntegerGenerator                      index_gen;
  private final IntegerGenerator                      size_gen;
  private final Generator<AreaInclusive>              area_gen;
  private final Generator<TextureFilterMagnification> filter_mag_gen;
  private final Generator<TextureFilterMinification>  filter_min_gen;
  private final Generator<KDepthPrecision>            depth_prec_gen;
  private final Generator<KDepthVariancePrecision>    depth_variance_prec_gen;

  public SBLightShadowDescriptionGenerator()
  {
    this.index_gen =
      new IntegerGenerator(0, SBShadowType.values().length - 1);
    this.size_gen = new IntegerGenerator(1, 10);
    this.filter_mag_gen = new TextureFilterMagGenerator();
    this.filter_min_gen = new TextureFilterMinGenerator();
    this.depth_prec_gen = new KDepthPrecisionGenerator();
    this.depth_variance_prec_gen = new KDepthVariancePrecisionGenerator();
    this.area_gen = new AreaGenerator();
  }

  @Override public KShadowType next()
  {
    switch (SBShadowType.values()[this.index_gen.nextInt()]) {
      case SHADOW_MAPPED_BASIC:
      {
        final KFramebufferDepthDescription description =
          KFramebufferDepthDescription.newDescription(
            this.area_gen.next(),
            this.filter_mag_gen.next(),
            this.filter_min_gen.next(),
            this.depth_prec_gen.next());
        final KShadowMapBasicDescription map_description =
          KShadowMapBasicDescription.newDescription(
            this.index_gen.next(),
            description,
            this.index_gen.next().intValue());
        return KShadowMappedBasic.newMappedBasic(
          (float) Math.random(),
          (float) Math.random(),
          map_description);
      }
      case SHADOW_MAPPED_VARIANCE:
      {
        final KFramebufferDepthVarianceDescription description =
          KFramebufferDepthVarianceDescription.newDescription(
            this.area_gen.next(),
            this.filter_mag_gen.next(),
            this.filter_min_gen.next(),
            this.depth_prec_gen.next(),
            this.depth_variance_prec_gen.next());
        final KShadowMapVarianceDescription map_description =
          KShadowMapVarianceDescription.newDescription(
            this.index_gen.next(),
            description,
            this.index_gen.next().intValue());

        final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
        bb.setBlurSize((float) (Math.random() * 32.0f));
        bb.setScale((float) Math.random());
        bb.setPasses((int) (Math.random() * 32));

        final KBlurParameters blur = bb.build();
        return KShadowMappedVariance.newMappedVariance(
          (float) Math.random(),
          (float) Math.random(),
          (float) Math.random(),
          blur,
          map_description);
      }
    }

    throw new UnreachableCodeException();
  }
}