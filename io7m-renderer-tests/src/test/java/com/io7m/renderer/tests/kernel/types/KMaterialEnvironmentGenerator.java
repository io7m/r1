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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentNone;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflection;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentReflectionMapped;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentType;

public final class KMaterialEnvironmentGenerator implements
  Generator<KMaterialEnvironmentType>
{
  private final Generator<TextureCubeStaticUsableType> tex_gen;

  public KMaterialEnvironmentGenerator(
    final Generator<TextureCubeStaticUsableType> in_tex_gen)
  {
    this.tex_gen = in_tex_gen;
  }

  @SuppressWarnings("null") @Override public KMaterialEnvironmentType next()
  {
    final int r = (int) (Math.random() * 3);
    switch (r) {
      case 0:
      {
        return KMaterialEnvironmentNone.none();
      }
      case 1:
      {
        return KMaterialEnvironmentReflection.reflection(
          (float) Math.random(),
          this.tex_gen.next());
      }
      case 2:
      {
        return KMaterialEnvironmentReflectionMapped.reflectionMapped(
          (float) Math.random(),
          this.tex_gen.next());
      }
    }
    throw new UnreachableCodeException();
  }
}
