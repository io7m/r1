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
import com.io7m.r1.kernel.KFogYParameters;
import com.io7m.r1.kernel.KFogYParametersBuilderType;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceRGBType;
import com.io7m.r1.spaces.RSpaceWorldType;
import com.io7m.r1.tests.kernel.types.KProjectionGenerator;
import com.io7m.r1.tests.types.PMatrixI4x4FGenerator;
import com.io7m.r1.tests.types.PVectorI3FGenerator;

@SuppressWarnings("null") public final class KFogYParametersGenerator implements
  Generator<KFogYParameters>
{
  private final Generator<PVectorI3F<RSpaceRGBType>>                    color_gen;
  private final Generator<KProjectionType>                              proj_gen;
  private final Generator<PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType>> view_gen;

  public KFogYParametersGenerator()
  {
    this.proj_gen = new KProjectionGenerator();
    this.color_gen = new PVectorI3FGenerator<RSpaceRGBType>();
    this.view_gen =
      new PMatrixI4x4FGenerator<RSpaceWorldType, RSpaceEyeType>();
  }

  @Override public KFogYParameters next()
  {
    final KFogYParametersBuilderType b =
      KFogYParameters.newBuilder(this.view_gen.next(), this.proj_gen.next());
    b.setColor(this.color_gen.next());
    b.setUpperY((float) Math.random());
    b.setLowerY((float) Math.random());
    final KFogProgression[] progressions = KFogProgression.values();
    b
      .setProgression(progressions[(int) (Math.random() * progressions.length)]);
    b.setProjection(this.proj_gen.next());
    b.setViewMatrix(this.view_gen.next());
    return b.build();
  }
}
