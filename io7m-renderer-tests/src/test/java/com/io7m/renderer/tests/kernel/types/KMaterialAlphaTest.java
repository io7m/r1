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

package com.io7m.renderer.tests.kernel.types;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;
import com.io7m.renderer.tests.utilities.TestUtilities;

@SuppressWarnings("static-method") public final class KMaterialAlphaTest
{
  @Test public void testAttributes()
  {
    final KMaterialAlpha a =
      KMaterialAlpha.newAlpha(
        KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
        1.0f);
    Assert.assertEquals(a.getOpacity(), 1.0f, 0.0f);
    Assert.assertEquals(
      a.getType(),
      KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT);
  }

  @Test public void testEqualsHashCode()
  {
    final KMaterialAlpha m0 =
      KMaterialAlpha.newAlpha(
        KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
        1.0f);
    final KMaterialAlpha m1 =
      KMaterialAlpha.newAlpha(
        KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
        1.0f);
    final KMaterialAlpha m2 =
      KMaterialAlpha.newAlpha(
        KMaterialAlphaOpacityType.ALPHA_OPACITY_ONE_MINUS_DOT,
        1.0f);
    final KMaterialAlpha m3 =
      KMaterialAlpha.newAlpha(
        KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
        0.5f);

    Assert.assertEquals(m0, m0);
    Assert.assertEquals(m0, m1);
    Assert.assertNotEquals(m0, null);
    Assert.assertNotEquals(m0, Integer.valueOf(23));
    Assert.assertNotEquals(m0, m2);
    Assert.assertNotEquals(m0, m3);

    Assert.assertEquals(m0.hashCode(), m1.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m2.hashCode());
    Assert.assertNotEquals(m0.hashCode(), m3.hashCode());

    Assert.assertEquals(m0.toString(), m1.toString());
    Assert.assertNotEquals(m0.toString(), m2.toString());
    Assert.assertNotEquals(m0.toString(), m3.toString());
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    KMaterialAlpha.newAlpha(
      (KMaterialAlphaOpacityType) TestUtilities.actuallyNull(),
      1.0f);
  }
}
