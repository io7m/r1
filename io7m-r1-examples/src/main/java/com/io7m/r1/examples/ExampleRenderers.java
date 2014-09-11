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

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The available example renderers.
 */

public final class ExampleRenderers
{
  /**
   * Attempt to retrieve the renderer with the given unqualified name.
   *
   * @param name
   *          The name of the renderer.
   * @return The renderer class.
   * @throws ClassNotFoundException
   *           If the renderer does not exist.
   * @throws IllegalAccessException
   *           If the renderer is not accessible.
   * @throws InstantiationException
   *           If the renderer cannot be instantiated.
   */

  public static ExampleRendererConstructorType getRenderer(
    final String name)
    throws ClassNotFoundException,
      InstantiationException,
      IllegalAccessException
  {
    final StringBuilder b = new StringBuilder();
    b.append(ExampleRendererConstructorType.class.getPackage().getName());
    b.append(".");
    b.append(name);
    final ExampleRendererName c_name = new ExampleRendererName(b.toString());
    assert c_name != null;

    final SortedMap<ExampleRendererName, ExampleRendererConstructorType> rc =
      ExampleRenderers.getRenderers();
    if (rc.containsKey(c_name)) {
      return rc.get(c_name);
    }

    throw new ClassNotFoundException(c_name.toString());
  }

  /**
   * @return The available renderers
   */

  public static
    SortedMap<ExampleRendererName, ExampleRendererConstructorType>
    getRenderers()
  {
    final SortedMap<ExampleRendererName, ExampleRendererConstructorType> r =
      new TreeMap<ExampleRendererName, ExampleRendererConstructorType>();

    r.put(
      ExampleRendererDeferredDefault.getName(),
      ExampleRendererDeferredDefault.get());
    r.put(
      ExampleRendererDeferredWithEmission.getName(),
      ExampleRendererDeferredWithEmission.get());
    r.put(
      ExampleRendererDeferredWithEmissionGlow.getName(),
      ExampleRendererDeferredWithEmissionGlow.get());

    final SortedMap<ExampleRendererName, ExampleRendererConstructorType> ur =
      Collections.unmodifiableSortedMap(r);

    assert ur != null;
    return ur;
  }

  private ExampleRenderers()
  {

  }
}
