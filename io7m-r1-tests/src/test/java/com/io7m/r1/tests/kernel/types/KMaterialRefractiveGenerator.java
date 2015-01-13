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

import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.kernel.types.KMaterialRefractiveMaskedNormals;
import com.io7m.r1.kernel.types.KMaterialRefractiveType;
import com.io7m.r1.kernel.types.KMaterialRefractiveUnmaskedNormals;
import com.io7m.r1.spaces.RSpaceRGBAType;
import com.io7m.r1.tests.types.PVectorI4FGenerator;

public final class KMaterialRefractiveGenerator implements
  Generator<KMaterialRefractiveType>
{
  private final PVectorI4FGenerator<RSpaceRGBAType> gen;

  public KMaterialRefractiveGenerator()
  {
    this.gen = new PVectorI4FGenerator<RSpaceRGBAType>();
  }

  @Override public KMaterialRefractiveType next()
  {
    final PVectorI4F<RSpaceRGBAType> c = this.gen.next();
    assert c != null;

    if (Math.random() > 0.5) {
      return KMaterialRefractiveUnmaskedNormals.create(
        (float) Math.random(),
        c);
    }

    return KMaterialRefractiveMaskedNormals.create((float) Math.random(), c);
  }
}
