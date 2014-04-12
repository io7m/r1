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

package com.io7m.renderer.kernel.examples;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reflections.Reflections;

/**
 * The list of available examples.
 */

public final class ExampleList
{
  private static Map<String, Class<? extends ExampleSceneType>> EXAMPLES;

  /**
   * @return A list of all of the available examples.
   */

  public static @Nonnull
    Map<String, Class<? extends ExampleSceneType>>
    getExamples()
  {
    if (ExampleList.EXAMPLES == null) {
      ExampleList.EXAMPLES = ExampleList.makeExamples();
    }
    return ExampleList.EXAMPLES;
  }

  private static @Nonnull
    Map<String, Class<? extends ExampleSceneType>>
    makeExamples()
  {
    final Reflections ref =
      new Reflections("com.io7m.renderer.kernel.examples");
    final Set<Class<? extends ExampleSceneType>> s =
      ref.getSubTypesOf(ExampleSceneType.class);

    final Map<String, Class<? extends ExampleSceneType>> examples =
      new HashMap<String, Class<? extends ExampleSceneType>>();

    final Iterator<Class<? extends ExampleSceneType>> i = s.iterator();
    while (i.hasNext()) {
      final Class<? extends ExampleSceneType> c = i.next();
      final boolean ok =
        (c.isInterface() == false)
          && (Modifier.isAbstract(c.getModifiers()) == false)
          && Modifier.isPublic(c.getModifiers());

      if (ok == false) {
        i.remove();
      }

      final String name = c.getCanonicalName();
      assert examples.containsKey(name) == false;
      examples.put(c.getCanonicalName(), c);
    }

    return Collections.unmodifiableMap(examples);
  }

  private ExampleList()
  {

  }
}
