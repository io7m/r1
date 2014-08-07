/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.sandbox;

import net.java.quickcheck.Generator;

import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RVectorI3F;

public final class SBVectorI3FGenerator<T extends RSpaceType> implements
  Generator<RVectorI3F<T>>
{
  @Override public RVectorI3F<T> next()
  {
    final double x =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    final double y =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    final double z =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    return new RVectorI3F<T>((float) x, (float) y, (float) z);
  }
}
