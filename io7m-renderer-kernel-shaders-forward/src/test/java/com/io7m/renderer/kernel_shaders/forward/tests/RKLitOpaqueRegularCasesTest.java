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

package com.io7m.renderer.kernel_shaders.forward.tests;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel_shaders.forward.RKLightCases;
import com.io7m.renderer.kernel_shaders.forward.RKLitCase;
import com.io7m.renderer.kernel_shaders.forward.RKLitOpaqueRegularCases;
import com.io7m.renderer.kernel_shaders.forward.RKMaterialCases;

@SuppressWarnings("static-method") public final class RKLitOpaqueRegularCasesTest
{
  @Test public void testCasesLitOpaqueRegular()
  {
    final RKMaterialCases mc = new RKMaterialCases();
    final RKLightCases lc = new RKLightCases();
    final RKLitOpaqueRegularCases oc = new RKLitOpaqueRegularCases();

    /**
     * All of the material cases are valid for all of the lights.
     */

    final int actual = oc.getCases().size();
    assert actual > 1;

    final int expected =
      mc.getCasesLitOpaqueRegular().size() * lc.getCases().size();
    assert expected > 1;

    System.out.println("Cases: " + actual);
    Assert.assertEquals(actual, expected);
  }

  @Test public void testCasesLitOpaqueRegularShow()
  {
    final RKLitOpaqueRegularCases oc = new RKLitOpaqueRegularCases();

    for (final RKLitCase<KMaterialOpaqueRegular> c : oc.getCases()) {
      final String lc = c.getLight().lightGetCode();
      final String mc = c.getMaterial().materialLitGetCode();
      System.out.printf("%s_%s\n", lc, mc);
    }
  }
}
