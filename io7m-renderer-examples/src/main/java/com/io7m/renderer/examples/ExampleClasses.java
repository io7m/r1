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

package com.io7m.renderer.examples;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.examples.scenes.SLEmpty0;

/**
 * Functions for retrieving example classes.
 */

public final class ExampleClasses
{
  /**
   * Attempt to retrieve the scene with the given unqualified name.
   * 
   * @param name
   *          The name of the scene.
   * @return The scene class.
   * @throws ClassNotFoundException
   *           If the scene does not exist.
   * @throws IllegalAccessException
   *           If the scene is not accessible.
   * @throws InstantiationException
   *           If the scene cannot be instantiated.
   */

  public static ExampleSceneType getScene(
    final String name)
    throws ClassNotFoundException,
      InstantiationException,
      IllegalAccessException
  {
    final StringBuilder b = new StringBuilder();
    b.append(SLEmpty0.class.getPackage().getName());
    b.append(".");
    b.append(name);
    final String c_name = b.toString();
    assert c_name != null;

    final Class<? extends ExampleSceneType> c =
      (Class<? extends ExampleSceneType>) Class.forName(c_name);
    return c.newInstance();
  }

  private ExampleClasses()
  {
    throw new UnreachableCodeException();
  }
}
