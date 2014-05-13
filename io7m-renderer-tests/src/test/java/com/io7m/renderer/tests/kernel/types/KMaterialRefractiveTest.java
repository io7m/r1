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

import javax.annotation.Nonnull;

import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.renderer.kernel.types.KMaterialRefractive;

public final class KMaterialRefractiveTest
{
  @SuppressWarnings("static-method") @Test public void testAttributes()
  {
    QuickCheck.forAllVerbose(
      new KMaterialRefractiveGenerator(),
      new AbstractCharacteristic<KMaterialRefractive>() {
        @Override protected void doSpecify(
          final @Nonnull KMaterialRefractive m)
          throws Throwable
        {
          Assert.assertTrue(m.withMasked(true).isMasked());
          Assert.assertFalse(m.withMasked(false).isMasked());
          Assert.assertEquals(m.withScale(0.5f).getScale(), 0.5f, 0.0f);
        }
      });
  }
}
