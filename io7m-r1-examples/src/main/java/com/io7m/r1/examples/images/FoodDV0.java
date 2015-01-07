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
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.examples.ExampleImageBuilderType;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KFramebufferDepthVarianceUsableType;
import com.io7m.r1.kernel.KImageSourceRGBAType;
import com.io7m.r1.kernel.KTextureMixParameters;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * A demonstration using a texture as an image source.
 */

public final class FoodDV0 implements ExampleImageType
{
  /**
   * Construct the example.
   */

  public FoodDV0()
  {

  }

  @Override public <A> A exampleAccept(
    final ExampleVisitorType<A> v)
  {
    return v.image(this);
  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getSimpleName());
  }

  @Override public void exampleImage(
    final ExampleImageBuilderType image,
    final KFramebufferDeferredType fb)
    throws RException
  {
    final R1Type r1 = image.getR1();

    final BLUCacheReceiptType<KFramebufferDepthVarianceDescription, KFramebufferDepthVarianceUsableType> r =
      DepthVarianceExampleUtilities.makeFoodTexture(image, r1);

    final KImageSourceRGBAType<KTextureMixParameters> sr =
      r1.getSourceRGBATextureMix();

    {
      final Texture2DStaticUsableType t =
        r.getValue().getDepthVarianceTexture();
      final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m = PMatrixI3x3F.identity();
      final KTextureMixParameters config =
        KTextureMixParameters.newParameters(t, m, 0.0f, t, m);
      sr.sourceEvaluateRGBA(config, fb);
    }

    r.returnToCache();
  }
}
