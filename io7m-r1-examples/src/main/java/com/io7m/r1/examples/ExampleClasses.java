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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.images.Images;
import com.io7m.r1.examples.scenes.Scenes;

/**
 * Functions for retrieving example classes.
 */

public final class ExampleClasses
{
  /**
   * Attempt to retrieve the example with the given unqualified name.
   *
   * @param type
   *          The type of example.
   * @param name
   *          The name of the example.
   * @return The example class.
   * @throws ClassNotFoundException
   *           If the example does not exist.
   * @throws IllegalAccessException
   *           If the example is not accessible.
   * @throws InstantiationException
   *           If the example cannot be instantiated.
   */

  public static ExampleType getExample(
    final ExampleTypeEnum type,
    final String name)
    throws ClassNotFoundException,
      InstantiationException,
      IllegalAccessException
  {
    final StringBuilder b = new StringBuilder();

    switch (type) {
      case EXAMPLE_IMAGE:
      {
        b.append(Images.class.getPackage().getName());
        break;
      }
      case EXAMPLE_SCENE:
      {
        b.append(Scenes.class.getPackage().getName());
        break;
      }
    }

    b.append(".");
    b.append(name);
    final String c_name = b.toString();
    assert c_name != null;

    final Class<? extends ExampleType> c =
      (Class<? extends ExampleType>) Class.forName(c_name);
    return c.newInstance();
  }

  private ExampleClasses()
  {
    throw new UnreachableCodeException();
  }
}
