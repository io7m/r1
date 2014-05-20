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

package com.io7m.renderer.types;

import com.io7m.jequality.annotations.EqualityReference;

/**
 * An exception raised when a feature is used that is not supported on the
 * current OpenGL implementation.
 */

@EqualityReference public final class RExceptionNotSupported extends
  RException
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 1140529563547632005L;
  }

  RExceptionNotSupported(
    final String message)
  {
    super(message);
  }

  @Override public <T, E extends Throwable> T exceptionAccept(
    final RExceptionVisitorType<T, E> v)
    throws E
  {
    return v.exceptionVisitNotSupportedException(this);
  }
}
