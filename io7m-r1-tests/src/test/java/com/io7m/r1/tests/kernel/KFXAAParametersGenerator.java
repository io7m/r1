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

import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFXAAParameters.Quality;
import com.io7m.r1.kernel.KFXAAParametersBuilderType;

public final class KFXAAParametersGenerator implements
  Generator<KFXAAParameters>
{
  public KFXAAParametersGenerator()
  {
    // Nothing
  }

  @Override public KFXAAParameters next()
  {
    final KFXAAParametersBuilderType b = KFXAAParameters.newBuilder();
    b.setEdgeThreshold((float) Math.random());
    b.setEdgeThresholdMinimum((float) Math.random());
    b.setSubpixelAliasingRemoval((float) Math.random());
    final Quality[] v = KFXAAParameters.Quality.values();
    final Quality q = v[(int) (Math.random() * v.length)];
    assert q != null;
    b.setQuality(q);
    return b.build();
  }
}
