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

package com.io7m.r1.examples;

import java.lang.reflect.Modifier;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("static-method") public final class ExampleListTest
{
  @Test public void testExampleListScenesWorks()
  {
    final Map<String, Class<? extends ExampleSceneType>> l =
      ExampleList.getExamplesScenes();

    Assert.assertTrue(l.size() > 0);

    for (final String name : l.keySet()) {
      final Class<? extends ExampleSceneType> c = l.get(name);
      System.out.println(c.getCanonicalName());
      Assert.assertEquals(name, c.getCanonicalName());
      Assert.assertFalse(c.isInterface());
      Assert.assertFalse(Modifier.isAbstract(c.getModifiers()));
      Assert.assertTrue(Modifier.isPublic(c.getModifiers()));
    }
  }

  // @Test public void testExampleListImagesWorks()
  // {
  // final Map<String, Class<? extends ExampleImageType>> l =
  // ExampleList.getExamplesImages();
  //
  // Assert.assertTrue(l.size() > 0);
  //
  // for (final String name : l.keySet()) {
  // final Class<? extends ExampleImageType> c = l.get(name);
  // System.out.println(c.getCanonicalName());
  // Assert.assertEquals(name, c.getCanonicalName());
  // Assert.assertFalse(c.isInterface());
  // Assert.assertFalse(Modifier.isAbstract(c.getModifiers()));
  // Assert.assertTrue(Modifier.isPublic(c.getModifiers()));
  // }
  // }

  @Test public void testExampleListWorks()
  {
    final Map<String, Class<? extends ExampleType>> l =
      ExampleList.getExamples();

    Assert.assertTrue(l.size() > 0);

    for (final String name : l.keySet()) {
      final Class<? extends ExampleType> c = l.get(name);
      System.out.println(c.getCanonicalName());
      Assert.assertEquals(name, c.getCanonicalName());
      Assert.assertFalse(c.isInterface());
      Assert.assertFalse(Modifier.isAbstract(c.getModifiers()));
      Assert.assertTrue(Modifier.isPublic(c.getModifiers()));
    }
  }
}
