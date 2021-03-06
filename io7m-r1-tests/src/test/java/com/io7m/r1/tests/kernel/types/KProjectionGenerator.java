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

import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceClipType;
import com.io7m.r1.spaces.RSpaceEyeType;

public final class KProjectionGenerator implements Generator<KProjectionType>
{
  @Override public KProjectionType next()
  {
    return KProjectionFrustum.newProjection(
      new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
      (float) Math.random(),
      (float) Math.random(),
      (float) Math.random(),
      (float) Math.random(),
      1.0f,
      (float) (1.0f + (Math.random() * 100.0f)));
  }
}
