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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jtensors.VectorI3F;
import com.io7m.r1.kernel.KBilinear;

@SuppressWarnings("static-method") public final class KBilinearTest
{
  @Test public void testBilinear0()
  {
    final VectorI3F x0y0 = new VectorI3F(0.0f, 0.0f, 0.0f);
    final VectorI3F x1y0 = new VectorI3F(1.0f, 0.0f, 0.0f);
    final VectorI3F x0y1 = new VectorI3F(0.0f, 1.0f, 0.0f);
    final VectorI3F x1y1 = new VectorI3F(1.0f, 1.0f, 0.0f);
    final float px = 0.0f;
    final float py = 0.0f;

    final VectorI3F r = KBilinear.bilinear3F(x0y0, x1y0, x0y1, x1y1, px, py);
    Assert.assertEquals(0.0f, r.getXF(), 0.0f);
    Assert.assertEquals(0.0f, r.getYF(), 0.0f);
    Assert.assertEquals(0.0f, r.getZF(), 0.0f);
  }

  @Test public void testBilinear1()
  {
    final VectorI3F x0y0 = new VectorI3F(0.0f, 0.0f, 0.0f);
    final VectorI3F x1y0 = new VectorI3F(1.0f, 0.0f, 0.0f);
    final VectorI3F x0y1 = new VectorI3F(0.0f, 1.0f, 0.0f);
    final VectorI3F x1y1 = new VectorI3F(1.0f, 1.0f, 0.0f);
    final float px = 0.5f;
    final float py = 0.5f;

    final VectorI3F r = KBilinear.bilinear3F(x0y0, x1y0, x0y1, x1y1, px, py);
    Assert.assertEquals(0.5f, r.getXF(), 0.0f);
    Assert.assertEquals(0.5f, r.getYF(), 0.0f);
    Assert.assertEquals(0.0f, r.getZF(), 0.0f);
  }

  @Test public void testBilinear2()
  {
    final VectorI3F x0y0 = new VectorI3F(0.0f, 0.0f, 0.0f);
    final VectorI3F x1y0 = new VectorI3F(1.0f, 0.0f, 0.0f);
    final VectorI3F x0y1 = new VectorI3F(0.0f, 1.0f, 0.0f);
    final VectorI3F x1y1 = new VectorI3F(1.0f, 1.0f, 0.0f);
    final float px = 1.0f;
    final float py = 0.0f;

    final VectorI3F r = KBilinear.bilinear3F(x0y0, x1y0, x0y1, x1y1, px, py);
    Assert.assertEquals(1.0f, r.getXF(), 0.0f);
    Assert.assertEquals(0.0f, r.getYF(), 0.0f);
    Assert.assertEquals(0.0f, r.getZF(), 0.0f);
  }
}
