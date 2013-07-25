/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel;

import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.io7m.jaux.CheckedMath;

@ThreadSafe public final class SBUniqueNames
{
  private final @Nonnull HashSet<String> values;

  SBUniqueNames()
  {
    this.values = new HashSet<String>();
  }

  synchronized @Nonnull String get(
    final @Nonnull String name)
  {
    String suffix = null;
    String prefix = null;

    if (this.values.contains(name) == false) {
      this.values.add(name);
      return name;
    }

    final int dot = name.lastIndexOf('.');
    if (dot != -1) {
      suffix = name.substring(dot + 1);
      prefix = name.substring(0, dot);
    } else {
      prefix = name;
    }

    final StringBuilder buffer = new StringBuilder();
    long n = 0;

    /**
     * Loop terminates when a unique name is found, or n overflows.
     */

    for (;;) {
      buffer.setLength(0);
      buffer.append(prefix);
      buffer.append("_");
      buffer.append(n);
      n = CheckedMath.add(n, 1);

      if (suffix != null) {
        buffer.append(".");
        buffer.append(suffix);
      }
      if (this.values.contains(buffer.toString()) == false) {
        this.values.add(buffer.toString());
        return buffer.toString();
      }
    }
  }
}
