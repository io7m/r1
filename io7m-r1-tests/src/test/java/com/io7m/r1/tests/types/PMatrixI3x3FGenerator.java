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

package com.io7m.r1.tests.types;

import net.java.quickcheck.Generator;

import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.jtensors.parameterized.PMatrixI3x3F;
import com.io7m.r1.spaces.RSpaceType;

public final class PMatrixI3x3FGenerator<S0 extends RSpaceType, S1 extends RSpaceType> implements
  Generator<PMatrixI3x3F<S0, S1>>
{
  @Override public PMatrixI3x3F<S0, S1> next()
  {
    final VectorReadable3FType column_0 =
      new VectorI3F(
        (float) Math.random(),
        (float) Math.random(),
        (float) Math.random());
    final VectorReadable3FType column_1 =
      new VectorI3F(
        (float) Math.random(),
        (float) Math.random(),
        (float) Math.random());
    final VectorReadable3FType column_2 =
      new VectorI3F(
        (float) Math.random(),
        (float) Math.random(),
        (float) Math.random());
    return PMatrixI3x3F.newFromColumns(column_0, column_1, column_2);
  }
}
