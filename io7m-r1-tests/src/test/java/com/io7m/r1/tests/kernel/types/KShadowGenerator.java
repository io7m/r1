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

import com.io7m.r1.kernel.types.KShadowType;

public final class KShadowGenerator implements Generator<KShadowType>
{
  private final KShadowMappedBasicGenerator    gen_basic;
  private final KShadowMappedVarianceGenerator gen_var;

  public KShadowGenerator()
  {
    this.gen_basic = new KShadowMappedBasicGenerator();
    this.gen_var = new KShadowMappedVarianceGenerator();
  }

  @Override public KShadowType next()
  {
    if (Math.random() > 0.5) {
      return this.gen_basic.next();
    }
    return this.gen_var.next();
  }
}
