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

package com.io7m.renderer.shaders.deferred;

import java.util.ArrayList;
import java.util.List;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KDepthVariancePrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KProjection;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.shaders.core.FakeImmutableCapabilities;
import com.io7m.renderer.shaders.core.FakeTexture2DStatic;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI3F;

@EqualityReference public final class RKDLightCases
{
  private static List<KLightType> makeLightCases()
  {
    try {
      final ArrayList<KLightType> cases = new ArrayList<KLightType>();
      final RVectorI3F<RSpaceWorldType> v =
        new RVectorI3F<RSpaceWorldType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceRGBType> c =
        new RVectorI3F<RSpaceRGBType>(1.0f, 1.0f, 1.0f);

      {
        final KLightType l = KLightDirectional.newLight(v, c, 1.0f);
        cases.add(l);
      }

      {
        final KLightType l = KLightSphere.newLight(c, 1.0f, v, 1.0f, 1.0f);
        cases.add(l);
      }

      {
        final KLightProjectiveBuilderType b =
          KLightProjective.newBuilder(
            FakeTexture2DStatic.getDefault(),
            KProjection.fromFrustumWithContext(
              new MatrixM4x4F(),
              -1.0f,
              1.0f,
              -1.0f,
              1.0f,
              1.0f,
              100.0f));

        /**
         * No shadow.
         */

        {
          b.setTexture(FakeTexture2DStatic.getDefault());
          cases.add(b.build(FakeImmutableCapabilities.withDepthTextures()));
        }

        /**
         * Basic shadow mapping.
         */

        {
          final AreaInclusive a =
            new AreaInclusive(
              new RangeInclusiveL(0, 99),
              new RangeInclusiveL(0, 99));
          final KFramebufferDepthDescription fd =
            KFramebufferDepthDescription.newDescription(
              a,
              TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
              TextureFilterMinification.TEXTURE_FILTER_NEAREST,
              KDepthPrecision.DEPTH_PRECISION_16);
          final KShadowMapBasicDescription d =
            KShadowMapBasicDescription.newDescription(fd, 2);

          b.setShadow(KShadowMappedBasic.newMappedBasic(1.0f, 1.0f, d));
          cases.add(b.build(FakeImmutableCapabilities.withDepthTextures()));
        }

        /**
         * Variance shadow mapping.
         */

        {
          final AreaInclusive a =
            new AreaInclusive(
              new RangeInclusiveL(0, 99),
              new RangeInclusiveL(0, 99));
          final KBlurParameters kp = KBlurParameters.newBuilder().build();
          final KFramebufferDepthVarianceDescription fvd =
            KFramebufferDepthVarianceDescription.newDescription(
              a,
              TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
              TextureFilterMinification.TEXTURE_FILTER_NEAREST,
              KDepthPrecision.DEPTH_PRECISION_16,
              KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);
          final KShadowMapVarianceDescription description =
            KShadowMapVarianceDescription.newDescription(fvd, 1);
          b.setShadow(KShadowMappedVariance.newMappedVariance(
            1.0f,
            1.0f,
            1.0f,
            kp,
            description));
          cases.add(b.build(FakeImmutableCapabilities.withDepthTextures()));
        }

      }

      return cases;
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final List<KLightType> light_cases;

  public RKDLightCases()
  {
    this.light_cases = RKDLightCases.makeLightCases();
  }

  public List<KLightType> getCases()
  {
    return this.light_cases;
  }
}
