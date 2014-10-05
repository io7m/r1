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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * The type of renderer names.
 */

public final class ExampleRendererName implements
  Comparable<ExampleRendererName>
{
  private final String actual;

  /**
   * Construct a new renderer name.
   *
   * @param in_actual
   *          The actual name.
   */

  public ExampleRendererName(
    final String in_actual)
  {
    this.actual = NullCheck.notNull(in_actual, "Actual");
  }

  @Override public int compareTo(
    final @Nullable ExampleRendererName o)
  {
    return this.actual.compareTo(NullCheck.notNull(o, "Other").actual);
  }

  @Override public boolean equals(
    @Nullable final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ExampleRendererName other = (ExampleRendererName) obj;
    return this.actual.equals(other.actual);
  }

  @Override public int hashCode()
  {
    return this.actual.hashCode();
  }

  @Override public String toString()
  {
    return this.actual;
  }
}
