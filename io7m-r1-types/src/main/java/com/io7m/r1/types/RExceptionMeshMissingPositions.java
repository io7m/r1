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

import java.util.Map;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jequality.annotations.EqualityReference;

/**
 * An exception representing an attempt to load a mesh that has missing
 * coordinates.
 */

@EqualityReference public final class RExceptionMeshMissingPositions extends
  RExceptionUserError
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -8323078581219987690L;
  }

  /**
   * Construct an exception with the given message.
   *
   * @param message
   *          The message.
   */

  public RExceptionMeshMissingPositions(
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

  /**
   * Construct an exception explaining that the given array lacks positions.
   *
   * @param array
   *          The array.
   * @return An exception.
   */

  public static RExceptionMeshMissingPositions fromArray(
    final ArrayBufferType array)
  {
    final StringBuilder s = new StringBuilder();
    s.append("The given array ");
    s.append(array);
    s.append(" does not have positions\n");
    s.append("Attributes include:\n");

    final ArrayDescriptor ad = array.arrayGetDescriptor();
    final Map<String, ArrayAttributeDescriptor> as = ad.getAttributes();

    for (final String name : as.keySet()) {
      s.append("  ");
      s.append(as.get(name));
      s.append("\n");
    }

    final String r = s.toString();
    assert r != null;
    return new RExceptionMeshMissingPositions(r);
  }
}
