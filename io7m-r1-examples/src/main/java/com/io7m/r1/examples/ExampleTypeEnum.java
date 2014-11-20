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

/**
 * Example types as an enum.
 */

public enum ExampleTypeEnum
{
  /**
   * An "image" type example.
   */

  EXAMPLE_IMAGE("images"),

  /**
   * A "scene" type example.
   */

  EXAMPLE_SCENE("scenes");

  private String directory_name;

  private ExampleTypeEnum(
    final String in_directory_name)
  {
    this.directory_name = in_directory_name;
  }

  /**
   * @return The name of the directory containing results.
   */

  public String getDirectoryName()
  {
    return this.directory_name;
  }

  @Override public String toString()
  {
    switch (this) {
      case EXAMPLE_IMAGE:
        return "Image";
      case EXAMPLE_SCENE:
        return "Scene";
    }

    throw new UnreachableCodeException();
  }
}
