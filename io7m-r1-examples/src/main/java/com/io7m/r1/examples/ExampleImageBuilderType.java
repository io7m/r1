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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.main.R1Type;

/**
 * The type of example image builders.
 */

public interface ExampleImageBuilderType
{
  /**
   * Load the texture with the given name.
   *
   * @param name
   *          The name
   * @return An allocated texture
   * @throws RException
   *           If an error occurs
   */

  Texture2DStaticUsableType texture(
    final String name)
    throws RException;

  /**
   * @return The R1 renderer
   */

  R1Type getR1();
}
