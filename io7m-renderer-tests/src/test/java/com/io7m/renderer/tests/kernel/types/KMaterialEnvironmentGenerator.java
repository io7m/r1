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

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;

public final class KMaterialEnvironmentGenerator implements
  Generator<KMaterialEnvironment>
{
  private final Generator<TextureCubeStaticUsableType> tex_gen;

  public KMaterialEnvironmentGenerator(
    final Generator<TextureCubeStaticUsableType> tex_gen1)
  {
    this.tex_gen = tex_gen1;
  }

  @Override public KMaterialEnvironment next()
  {
    if (Math.random() > 0.5) {
      final TextureCubeStaticUsableType tn = this.tex_gen.next();
      assert tn != null;
      return KMaterialEnvironment.newEnvironmentMapped(
        (float) Math.random(),
        tn,
        Math.random() > 0.5 ? true : false);
    }
    return KMaterialEnvironment.newEnvironmentUnmapped();
  }
}
