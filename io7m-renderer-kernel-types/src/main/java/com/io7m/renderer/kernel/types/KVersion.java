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

package com.io7m.renderer.kernel.types;

import com.io7m.jnull.Nullable;

/**
 * The type of persistent data structure version numbers.
 */

public final class KVersion implements Comparable<KVersion>
{
  private final long value;

  private KVersion(
    final long i)
  {
    this.value = i;
  }

  /**
   * @return The next version.
   */

  public KVersion next()
  {
    /**
     * Assuming one new version number every millisecond, this condition will
     * become true after the program has been running for approximately 584
     * million years.
     */

    if (this.value == Long.MAX_VALUE) {
      throw new ArithmeticException("Version number overflow");
    }

    return KVersion.valueOf(this.value + 1);
  }

  /**
   * Construct a version number from the given integer.
   * 
   * @param i
   *          The integer
   * @return A version number
   */

  public static KVersion valueOf(
    final long i)
  {
    return new KVersion(i);
  }

  /**
   * @return The first possible version number.
   */

  public static KVersion first()
  {
    return KVersion.valueOf(Long.MIN_VALUE);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (int) (this.value ^ (this.value >>> 32));
    return result;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    final KVersion other = (KVersion) obj;
    return (this.value == other.value);
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KVersion ");
    b.append(this.value);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }

  @Override public int compareTo(
    final KVersion o)
  {
    return Long.compare(this.value, o.value);
  }
}
