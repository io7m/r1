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

package com.io7m.renderer.tests.types;

import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM2F;
import com.io7m.renderer.types.RSpaceEyeType;
import com.io7m.renderer.types.RSphericalTransform;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorM3F;

@SuppressWarnings("static-method") public final class RSphericalTransformTest
{
  public void testIdentity()
  {
    final VectorM2F encoded = new VectorM2F();
    final RVectorM3F<RSpaceEyeType> decoded = new RVectorM3F<RSpaceEyeType>();

    for (float x = 0.0f; x <= 1.0f; x += 0.01f) {
      for (float y = 0.0f; y <= 1.0f; y += 0.01f) {
        for (float z = 0.0f; z <= 1.0f; z += 0.01f) {
          final VectorI3F vn = VectorI3F.normalize(new VectorI3F(x, y, z));
          final RVectorI3F<RSpaceEyeType> v =
            new RVectorI3F<RSpaceEyeType>(vn.getXF(), vn.getYF(), vn.getZF());

          RSphericalTransform.encode(v, encoded);
          RSphericalTransform.decode(encoded, decoded);

          System.out.println("Normalized  : " + v);
          System.out.println("Encoded     : " + encoded);
          System.out.println("Decoded     : " + decoded);
          System.out.println("--");
        }
      }
    }
  }
}
