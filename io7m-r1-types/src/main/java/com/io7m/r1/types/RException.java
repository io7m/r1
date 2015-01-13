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

package com.io7m.r1.types;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * The root exception type for the renderer package.
 */

@EqualityReference public abstract class RException extends Exception
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 883316517600668005L;
  }

  protected RException(
    final String message)
  {
    super(message);
  }

  protected RException(
    final Throwable e)
  {
    super(NullCheck.notNull(e, "Exception"));
  }

  protected RException(
    final Throwable x,
    final String message)
  {
    super(message, x);
  }
}
