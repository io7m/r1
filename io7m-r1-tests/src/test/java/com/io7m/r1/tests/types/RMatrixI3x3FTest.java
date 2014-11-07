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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.r1.types.RMatrixI3x3F;
import com.io7m.r1.types.RTransformType;

public class RMatrixI3x3FTest
{
  @SuppressWarnings("static-method") @Test public void testEquals()
  {
    final MatrixM3x3F m0 = new MatrixM3x3F();

    int index = 0;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final RMatrixI3x3F<RTransformType> im0 = RMatrixI3x3F.newFromReadable(m0);
    final RMatrixI3x3F<RTransformType> im1 = RMatrixI3x3F.newFromReadable(m0);

    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        Assert.assertEquals(
          im0.getRowColumnF(row, col),
          m0.get(row, col),
          0.0);
      }
    }

    Assert.assertEquals(im0, im0);
    Assert.assertEquals(im0.hashCode(), im0.hashCode());
    Assert.assertEquals(im0, im1);
    Assert.assertFalse(im0.equals(null));
    Assert.assertFalse(im0.equals(Integer.valueOf(23)));

    index = 100;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final RMatrixI3x3F<RTransformType> im2 = RMatrixI3x3F.newFromReadable(m0);
    Assert.assertFalse(im0.equals(im2));
  }

  @SuppressWarnings("static-method") @Test public void testMakeMatrix3x3F()
  {
    final MatrixM3x3F m0 = new MatrixM3x3F();
    final MatrixM3x3F m1 = new MatrixM3x3F();

    int index = 0;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final RMatrixI3x3F<RTransformType> im = RMatrixI3x3F.newFromReadable(m0);
    im.makeMatrixM3x3F(m1);
    Assert.assertEquals(m0, m1);
  }

  @SuppressWarnings("static-method") @Test public void testToString()
  {
    final MatrixM3x3F m0 = new MatrixM3x3F();
    final MatrixM3x3F m1 = new MatrixM3x3F();

    int index = 0;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 3; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final RMatrixI3x3F<RTransformType> im = RMatrixI3x3F.newFromReadable(m0);
    im.makeMatrixM3x3F(m1);
    Assert.assertEquals(m1.toString(), m0.toString());
  }
}
