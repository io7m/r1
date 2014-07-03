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

import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * A renderer constructor.
 */

public interface ExampleRendererConstructorType
{
  /**
   * Accept a visitor.
   * 
   * @param v
   *          The visitor.
   * @param <A>
   *          The type of values.
   * @param <E>
   *          The type of raised exceptions.
   * 
   * @return The value returned by the visitor.
   * @throws E
   *           If the visitor throws <code>E</code>.
   * @throws JCGLException
   *           If the visitor raises this type of exception.
   * @throws RException
   *           If the visitor raises this type of exception.
   */

  <A, E extends Exception> A matchConstructor(
    final ExampleRendererConstructorVisitorType<A, E> v)
    throws E,
      RException,
      JCGLException;
}
