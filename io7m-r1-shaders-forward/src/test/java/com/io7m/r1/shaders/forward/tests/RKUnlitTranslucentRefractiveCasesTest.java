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

import com.io7m.r1.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.r1.shaders.forward.RKFMaterialCases;
import com.io7m.r1.shaders.forward.RKFUnlitTranslucentRefractiveCases;

@SuppressWarnings("static-method") public final class RKUnlitTranslucentRefractiveCasesTest
{
  @Test public void testCasesUnlitTranslucentRefractive()
  {
    final RKFMaterialCases mc = new RKFMaterialCases();
    final RKFUnlitTranslucentRefractiveCases oc =
      new RKFUnlitTranslucentRefractiveCases();

    final int materials = oc.getCases().size();
    assert materials > 1;

    final int unlit_cases = mc.getCasesUnlitTranslucentRefractive().size();
    assert unlit_cases > 1;

    System.out.println("Material cases                     : " + materials);
    System.out.println("Unlit translucent refractive cases : " + unlit_cases);
    Assert.assertEquals(unlit_cases, materials);
  }

  @Test public void testCasesUnlitTranslucentRefractiveShow()
  {
    final RKFUnlitTranslucentRefractiveCases oc =
      new RKFUnlitTranslucentRefractiveCases();
    final Set<String> set = new HashSet<String>();

    for (final KMaterialTranslucentRefractive c : oc.getCases()) {
      final String code = c.getCode();
      System.out.println(code);

      if (set.contains(code)) {
        Assert.fail(code + " already exists");
      }
      set.add(code);
    }
  }
}
