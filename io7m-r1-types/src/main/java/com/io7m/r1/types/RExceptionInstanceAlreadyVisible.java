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

/**
 * An exception representing an attempt to add an invisible shadow-casting
 * instance to a scene when the instance has already been added to the scene
 * as a visible instance for the given light.
 */

@EqualityReference public final class RExceptionInstanceAlreadyVisible extends
  RExceptionUserError
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -5399210626322903568L;
  }

  /**
   * Construct an exception with the given message.
   * 
   * @param message
   *          The message.
   */

  public RExceptionInstanceAlreadyVisible(
    final String message)
  {
    super(message);
  }

  @Override <T, E extends Throwable> T exceptionAccept(
    final RExceptionVisitorType<T, E> v)
    throws E
  {
    return v.exceptionVisitUserErrorException(this);
  }
}
