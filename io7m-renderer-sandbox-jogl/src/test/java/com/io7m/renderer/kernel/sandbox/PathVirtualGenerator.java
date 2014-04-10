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

package com.io7m.renderer.kernel.sandbox;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.StringGenerator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jvvfs.PathVirtual;

public final class PathVirtualGenerator implements Generator<PathVirtual>
{
  private final @Nonnull StringGenerator gen = new StringGenerator('a', 'z');

  @Override public PathVirtual next()
  {
    final int components = (int) Math.abs(Math.random() * 32);
    PathVirtual path = PathVirtual.ROOT;
    for (int index = 0; index < components; ++index) {
      try {
        final String comp = this.gen.next();
        path = path.appendName(comp.isEmpty() ? "z" : comp);
      } catch (final ConstraintError e) {
        throw new UnreachableCodeException();
      }
    }
    return path;
  }
}