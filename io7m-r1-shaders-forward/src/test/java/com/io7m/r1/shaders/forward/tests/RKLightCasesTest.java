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
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import com.io7m.r1.kernel.types.KLightTranslucentType;
import com.io7m.r1.kernel.types.KLightType;
import com.io7m.r1.shaders.forward.RKFLightCases;

@SuppressWarnings("static-method") public final class RKLightCasesTest
{
  private static Reflections getReflections()
  {
    return new Reflections("com.io7m.r1.kernel.types");
  }

  @Test public void testLightCasesShow()
  {
    final RKFLightCases c = new RKFLightCases();
    final Set<String> set = new HashSet<String>();

    for (final KLightType p : c.getCases()) {
      final String code = p.lightGetCode();
      System.out.println(code);

      if (set.contains(code)) {
        Assert.fail(code + " already exists");
      }
      set.add(code);
    }
  }

  @Test public void testLightCases()
  {
    final Reflections r = RKLightCasesTest.getReflections();
    final Set<Class<? extends KLightTranslucentType>> lt0 =
      r.getSubTypesOf(KLightTranslucentType.class);

    final Set<Class<?>> lt = new HashSet<Class<?>>();
    lt.addAll(lt0);

    /**
     * Remove any non-concrete types.
     */

    final Iterator<Class<?>> iter = lt.iterator();
    while (iter.hasNext()) {
      final Class<?> c = iter.next();
      if (c.isInterface()) {
        iter.remove();
      }
    }

    /**
     * One case per light...
     */

    final int types = lt.size();

    final RKFLightCases c = new RKFLightCases();
    Assert.assertEquals(types, c.getCases().size());
  }
}
