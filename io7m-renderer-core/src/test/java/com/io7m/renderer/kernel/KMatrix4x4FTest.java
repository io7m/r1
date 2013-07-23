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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jtensors.MatrixM4x4F;

public class KMatrix4x4FTest
{
  @SuppressWarnings("static-method") @Test public void testToString()
  {
    final MatrixM4x4F m0 = new MatrixM4x4F();
    final MatrixM4x4F m1 = new MatrixM4x4F();

    int index = 0;
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final KMatrix4x4F<KMatrixKind> im = new KMatrix4x4F<KMatrixKind>(m0);
    Assert.assertEquals(m0.toString(), im.toString());
    im.makeMatrixM4x4F(m1);
    Assert.assertEquals(m1.toString(), m0.toString());
  }

  @SuppressWarnings("static-method") @Test public void testMakeMatrix4x4F()
  {
    final MatrixM4x4F m0 = new MatrixM4x4F();
    final MatrixM4x4F m1 = new MatrixM4x4F();

    int index = 0;
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final KMatrix4x4F<KMatrixKind> im = new KMatrix4x4F<KMatrixKind>(m0);
    im.makeMatrixM4x4F(m1);
    Assert.assertEquals(m0, m1);
  }

  @SuppressWarnings("static-method") @Test public void testEquals()
  {
    final MatrixM4x4F m0 = new MatrixM4x4F();

    int index = 0;
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final KMatrix4x4F<KMatrixKind> im0 = new KMatrix4x4F<KMatrixKind>(m0);
    final KMatrix4x4F<KMatrixKind> im1 = new KMatrix4x4F<KMatrixKind>(m0);

    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
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
    for (int row = 0; row < 4; ++row) {
      for (int col = 0; col < 4; ++col) {
        m0.set(row, col, index);
        ++index;
      }
    }

    final KMatrix4x4F<KMatrixKind> im2 = new KMatrix4x4F<KMatrixKind>(m0);
    Assert.assertFalse(im0.equals(im2));
  }
}
