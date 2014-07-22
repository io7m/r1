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

package com.io7m.renderer.rmb;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions for determining alignment.
 */

@EqualityReference public final class RBAlign
{
  /**
   * <p>
   * Calculate the required size in bytes of an object of <code>size</code>
   * bytes aligned to <code>align</code> bytes.
   * </p>
   * <p>
   * That is, if an object is going to be serialized at an <code>align</code>
   * -byte boundary, the function returns the number of bytes that will be
   * written including any padding required to bring the object up to a size
   * that is a multiple of <code>align</code>.
   * </p>
   *
   * @param size
   *          The size of the object.
   * @param align
   *          The required alignment.
   * @return The resulting total size including padding.
   */

  public static long alignedSize(
    final int size,
    final int align)
  {
    return ((size + align) / align) * align;
  }

  private RBAlign()
  {
    throw new UnreachableCodeException();
  }
}
