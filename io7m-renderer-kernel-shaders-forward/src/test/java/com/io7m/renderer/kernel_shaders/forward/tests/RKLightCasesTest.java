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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import com.io7m.jfunctional.Pair;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel_shaders.forward.FakeImmutableCapabilities;
import com.io7m.renderer.kernel_shaders.forward.RKLightCases;

@SuppressWarnings("static-method") public final class RKLightCasesTest
{
  private static Reflections getReflections()
  {
    return new Reflections("com.io7m.renderer.kernel.types");
  }

  @Test public void testLightCasesShow()
  {
    final RKLightCases c = new RKLightCases();
    for (final Pair<KLightType, FakeImmutableCapabilities> p : c.getCases()) {
      System.out.println(p.getLeft().lightGetCode());
    }
  }

  @Test public void testLightCases()
  {
    final Reflections r = RKLightCasesTest.getReflections();
    final Set<Class<? extends KLightType>> lt =
      r.getSubTypesOf(KLightType.class);
    final Set<Class<? extends KShadowType>> st =
      r.getSubTypesOf(KShadowType.class);

    /**
     * One case per light...
     */

    int types = lt.size();

    /**
     * ... plus one case per shadow, with an extra case for
     * basic-shadow-mapping-packed-4444.
     */

    types = types + (st.size() + 1);

    final RKLightCases c = new RKLightCases();
    Assert.assertEquals(types, c.getCases().size());
  }
}
