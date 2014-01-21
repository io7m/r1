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

package com.io7m.renderer.kernel;

import net.java.quickcheck.Generator;

import com.io7m.jtensors.QuaternionI4F;

public final class QuaternionI4FGenerator implements Generator<QuaternionI4F>
{
  @Override public QuaternionI4F next()
  {
    final double x =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    final double y =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    final double z =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    final double w =
      Float.MIN_VALUE + (Math.random() * (Float.MAX_VALUE - Float.MIN_VALUE));
    return new QuaternionI4F((float) x, (float) y, (float) z, (float) w);
  }
}
