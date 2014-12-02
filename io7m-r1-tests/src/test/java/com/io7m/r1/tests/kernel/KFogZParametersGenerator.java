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

package com.io7m.r1.tests.kernel;

import net.java.quickcheck.Generator;

import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.KFogProgression;
import com.io7m.r1.kernel.KFogZParameters;
import com.io7m.r1.kernel.KFogZParametersBuilderType;
import com.io7m.r1.tests.types.PMatrixI4x4FGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceRGBType;

@SuppressWarnings("null") public final class KFogZParametersGenerator implements
  Generator<KFogZParameters>
{
  private final Generator<PVectorI3F<RSpaceRGBType>>                   color_gen;
  private final Generator<PMatrixI4x4F<RSpaceEyeType, RSpaceClipType>> proj_gen;

  public KFogZParametersGenerator()
  {
    this.proj_gen =
      new PMatrixI4x4FGenerator<RSpaceEyeType, RSpaceClipType>();
    this.color_gen = new PVectorI3FGenerator<RSpaceRGBType>();
  }

  @Override public KFogZParameters next()
  {
    final KFogZParametersBuilderType b =
      KFogZParameters.newBuilder(this.proj_gen.next());
    b.setColor(this.color_gen.next());
    b.setFarZ((float) Math.random());
    b.setNearZ((float) Math.random());
    final KFogProgression[] progressions = KFogProgression.values();
    b
      .setProgression(progressions[(int) (Math.random() * progressions.length)]);
    b.setProjectionMatrix(this.proj_gen.next());
    return b.build();
  }
}
