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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.jtensors.parameterized.PMatrixM3x3F;
import com.io7m.r1.examples.ExampleImageBuilderType;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageSourceRGBAType;
import com.io7m.r1.kernel.KTextureMixParameters;
import com.io7m.r1.kernel.types.KAxes;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * A demonstration using a texture as an image source.
 */

public final class Food1FXAA implements ExampleImageType
{
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> uv;
  private final PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType> temporary;

  /**
   * Construct the example.
   */

  public Food1FXAA()
  {
    this.uv = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
    this.temporary = new PMatrixM3x3F<RSpaceTextureType, RSpaceTextureType>();
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

    final KImageSourceRGBAType<KTextureMixParameters> s =
      r1.getSourceRGBATextureMix();

    final Texture2DStaticUsableType t = image.texture("food_640x480.jpg");

    PMatrixM3x3F.setIdentity(this.uv);
    PMatrixM3x3F.scale(this.uv, 3.0f, this.uv);

    PMatrixM3x3F.makeRotationInto(
      Math.toRadians(45.0),
      KAxes.AXIS_Z,
      this.temporary);
    PMatrixM3x3F.multiply(this.uv, this.temporary, this.uv);

    final PMatrixI3x3F<RSpaceTextureType, RSpaceTextureType> m =
      PMatrixI3x3F.newFromReadable(this.uv);

    final KTextureMixParameters config =
      KTextureMixParameters.newParameters(t, m, 0.0f, t, m);
    s.sourceEvaluateRGBA(config, fb);

    final KFXAAParameters fxaa_config = KFXAAParameters.getDefault();
    r1.getFilterFXAA().filterEvaluateRGBA(fxaa_config, fb, fb);
  }
}
