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

package com.io7m.r1.rmb;

import com.io7m.r1.types.RException;

/**
 * Exceptions raised upon errors whilst parsing RMB meshes.
 */

public abstract class RBException extends RException
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -1189812950538543213L;
  }

  /**
   * Construct an exception.
   *
   * @param message
   *          The message
   */

  public RBException(
    final String message)
  {
    super(message);
  }

  /**
   * Construct an exception.
   *
   * @param e
   *          The cause
   */

  public RBException(
    final Throwable e)
  {
    super(e);
  }

  /**
   * Construct an exception.
   *
   * @param x
   *          The cause
   * @param message
   *          The message
   */

  public RBException(
    final Throwable x,
    final String message)
  {
    super(x, message);
  }
}
