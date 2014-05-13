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

package com.io7m.renderer.tests.kernel;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Check properties of the public API.
 */

public final class APITest
{
  private static void implementsHashCodeEquals(
    final Class<? extends Object> c)
  {
    if (c.isEnum()) {
      System.out.println(c.getCanonicalName() + " is an enum");
      return;
    }

    boolean equals = false;
    boolean hashcode = false;

    final Method[] ms = c.getMethods();
    for (final Method m : ms) {
      if (m.getName().equals("equals")) {
        if (m.getDeclaringClass().equals(c) == false) {
          System.out.println(c + " failed to override equals");
          Assert.assertTrue(m.getDeclaringClass().equals(c));
        }

        assert equals == false;
        equals = true;
      }

      if (m.getName().equals("hashCode")) {
        if (m.getDeclaringClass().equals(c) == false) {
          System.out.println(c + " failed to override hashCode");
          Assert.assertTrue(m.getDeclaringClass().equals(c));
        }

        assert hashcode == false;
        hashcode = true;
      }
    }

    Assert.assertTrue(equals);
    Assert.assertTrue(hashcode);

    System.out.println(c.getCanonicalName() + " implements equals/hashcode");
  }

  private static Reflections setupReflections()
  {
    final List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    classLoadersList.add(ClasspathHelper.staticClassLoader());

    return new Reflections(new ConfigurationBuilder()
      .setScanners(
        new SubTypesScanner(false /* don't exclude Object.class */),
        new ResourcesScanner())
      .setUrls(
        ClasspathHelper.forClassLoader(classLoadersList
          .toArray(new ClassLoader[0])))
      .filterInputsBy(
        new FilterBuilder().include(FilterBuilder
          .prefix("com.io7m.renderer.kernel"))));
  }

  /**
   * Check that all types implement equals and hashcode.
   */

  @SuppressWarnings("static-method") @Test public void testEqualsHashcode()
  {
    final Reflections ref = APITest.setupReflections();
    final Set<Class<? extends Object>> types =
      ref.getSubTypesOf(Object.class);

    Assert.assertTrue(types.size() > 1);

    for (final Class<? extends Object> c : types) {
      final int m = c.getModifiers();
      if (Modifier.isFinal(m)) {
        APITest.implementsHashCodeEquals(c);
      }
    }
  }

  /**
   * Check that all public types are final.
   */

  @SuppressWarnings("static-method") @Test public void testPublicFinal()
  {
    final Reflections ref = APITest.setupReflections();
    final Set<Class<? extends Object>> types =
      ref.getSubTypesOf(Object.class);

    Assert.assertTrue(types.size() > 1);

    for (final Class<? extends Object> c : types) {
      final int m = c.getModifiers();
      if (Modifier.isPublic(m)) {
        if (c.isInterface() == false) {
          Assert.assertTrue(Modifier.isFinal(m));
          System.out.println(c.getCanonicalName() + " is public and final");
        }
      }
    }
  }
}
