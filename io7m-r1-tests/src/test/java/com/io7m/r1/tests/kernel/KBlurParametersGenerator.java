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

import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;

public final class KBlurParametersGenerator implements
  Generator<KBlurParameters>
{
  @Override public KBlurParameters next()
  {
    final KBlurParametersBuilderType b = KBlurParameters.newBuilder();
    b.setBlurSize((float) Math.random());
    b.setPasses((int) (Math.random() * 20));
    b.setScale((float) Math.random());
    return b.build();
  }
}
