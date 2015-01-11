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

package com.io7m.r1.examples.images;

import com.io7m.jcache.BLUCacheReceiptType;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.ClearSpecification;
import com.io7m.jcanephora.ClearSpecificationBuilderType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.ExampleImageBuilderType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceUsableType;
import com.io7m.r1.kernel.KImageSourceDepthVarianceType;
import com.io7m.r1.kernel.KTextureMixParameters;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KDepthVariancePrecision;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceTextureType;

final class DepthVarianceExampleUtilities
{
  static
    BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType>
    makeBlankFramebuffer(
      final R1Type r1)
      throws RException
  {
    final JCGLImplementationType gi = r1.getJCGLImplementation();
    final JCGLInterfaceCommonType gc = gi.getGLCommon();
    final KFramebufferDepthVarianceCacheType dvc = r1.getDepthVarianceCache();

    final AreaInclusive in_area =
      new AreaInclusive(new RangeInclusiveL(0, 639), new RangeInclusiveL(
        0,
        479));
    final KFramebufferDepthVarianceDescription desc =
      KFramebufferDepthVarianceDescription.newDescription(
        in_area,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_16,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);
    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> r =
      dvc.bluCacheGet(desc);

    final KFramebufferDepthVarianceUsableType dvfb = r.getValue();
    gc
      .framebufferDrawBind(dvfb.getDepthVariancePassFramebuffer());
    {
      final ClearSpecificationBuilderType bb =
        ClearSpecification.newBuilder();
      bb.setStrictChecking(true);
      bb.enableColorBufferClear4f(1.0f, 1.0f, 0.0f, 0.0f);
      bb.enableDepthBufferClear(1.0f);
      bb.disableStencilBufferClear();
      final ClearSpecification c = bb.build();
      gc.clear(c);
    }
    gc.framebufferDrawUnbind();
    return r;
  }

  static
    BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType>
    makeFoodTexture(
      final ExampleImageBuilderType image,
      final R1Type r1)
      throws RException
  {
    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> r =
      DepthVarianceExampleUtilities.makeBlankFramebuffer(r1);

    {
      final KImageSourceDepthVarianceType<KTextureMixParameters> sdv =
        r1.getSourceDepthVarianceTextureMix();
      final Texture2DStaticUsableType t = image.texture("food_640x480.jpg");
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m =
        PMatrixI3x3F.identity();
      final KTextureMixParameters dv_config =
        KTextureMixParameters.newParameters(t, m, 0.0f, t, m);
      sdv.sourceEvaluateDepthVariance(dv_config, r.getValue());
    }
    return r;
  }

  private DepthVarianceExampleUtilities()
  {
    throw new UnreachableCodeException();
  }

}
