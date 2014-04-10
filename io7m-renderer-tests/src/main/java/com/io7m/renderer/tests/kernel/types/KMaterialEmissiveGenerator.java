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

package com.io7m.renderer.tests.kernel.types;

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.renderer.kernel.types.KMaterialEmissive;

public final class KMaterialEmissiveGenerator implements
  Generator<KMaterialEmissive>
{
  private final @Nonnull Generator<Texture2DStaticUsable> tex_gen;

  public KMaterialEmissiveGenerator(
    final @Nonnull Generator<Texture2DStaticUsable> tex_gen1)
  {
    this.tex_gen = tex_gen1;
  }

  @Override public KMaterialEmissive next()
  {
    try {
      if (Math.random() > 0.5) {
        return KMaterialEmissive.newEmissiveMapped(
          (float) Math.random(),
          this.tex_gen.next());
      }
      if (Math.random() < 0.05) {
        return KMaterialEmissive.newEmissiveNone();
      }
      return KMaterialEmissive.newEmissiveUnmapped((float) Math.random());
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }
}