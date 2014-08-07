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

package com.io7m.r1.shaders.forward.tests;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;
import com.io7m.r1.shaders.forward.RKFLightCases;
import com.io7m.r1.shaders.forward.RKFLitCase;
import com.io7m.r1.shaders.forward.RKFLitTranslucentRegularCases;
import com.io7m.r1.shaders.forward.RKFMaterialCases;

@SuppressWarnings("static-method") public final class RKLitTranslucentRegularCasesTest
{
  @Test public void testCasesLitTranslucentRegular()
  {
    final RKFMaterialCases mc = new RKFMaterialCases();
    final RKFLightCases lc = new RKFLightCases();
    final RKFLitTranslucentRegularCases oc =
      new RKFLitTranslucentRegularCases();

    /**
     * All of the material cases are valid for all of the lights.
     */

    final int actual = oc.getCases().size();
    assert actual > 1;

    final int expected =
      mc.getCasesLitTranslucentRegular().size() * lc.getCases().size();
    assert expected > 1;

    System.out.println("Cases: " + actual);
    Assert.assertEquals(actual, expected);
  }

  @Test public void testCasesLitTranslucentRegularShow()
  {
    final RKFLitTranslucentRegularCases oc =
      new RKFLitTranslucentRegularCases();
    final Set<String> set = new HashSet<String>();

    for (final RKFLitCase<KMaterialTranslucentRegular> c : oc.getCases()) {
      final String lc = c.getLight().lightGetCode();
      final String mc = c.getMaterial().materialGetCode();
      final String code = String.format("%s_%s", lc, mc);
      System.out.println(code);

      if (set.contains(code)) {
        Assert.fail(code + " already exists");
      }
      set.add(code);
    }
  }
}
