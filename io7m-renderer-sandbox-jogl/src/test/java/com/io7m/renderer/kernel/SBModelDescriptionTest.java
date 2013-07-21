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

import java.io.File;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

public class SBModelDescriptionTest
{
  private static class ModelGenerator implements
    Generator<SBModelDescription>
  {
    private final @Nonnull SBNonEmptyStringGenerator string_gen;

    public ModelGenerator()
    {
      this.string_gen = new SBNonEmptyStringGenerator();
    }

    @Override public SBModelDescription next()
    {
      return new SBModelDescription(
        new File(this.string_gen.next()),
        this.string_gen.next());
    }
  }

  @SuppressWarnings("static-method") @Test public
    void
    testSerializationRoundTrip()
  {
    QuickCheck.forAllVerbose(
      new ModelGenerator(),
      new AbstractCharacteristic<SBModelDescription>() {
        @Override protected void doSpecify(
          final @Nonnull SBModelDescription desc)
          throws Throwable
        {
          Assert.assertEquals(desc, SBModelDescription.fromXML(desc.toXML()));
        }
      });
  }
}
