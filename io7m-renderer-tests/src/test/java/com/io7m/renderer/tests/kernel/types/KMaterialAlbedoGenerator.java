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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoTextured;
import com.io7m.renderer.kernel.types.KMaterialAlbedoType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RVectorI4F;

@SuppressWarnings("null") public final class KMaterialAlbedoGenerator implements
  Generator<KMaterialAlbedoType>
{
  private final Generator<Texture2DStaticUsableType>  tex_gen;
  private final Generator<RVectorI4F<RSpaceRGBAType>> vec_gen;

  public KMaterialAlbedoGenerator(
    final Generator<RVectorI4F<RSpaceRGBAType>> in_vec_gen,
    final Generator<Texture2DStaticUsableType> in_tex_gen)
  {
    this.vec_gen = in_vec_gen;
    this.tex_gen = in_tex_gen;
  }

  @Override public KMaterialAlbedoType next()
  {
    if (Math.random() > 0.5) {
      return KMaterialAlbedoTextured.textured(
        this.vec_gen.next(),
        (float) Math.random(),
        this.tex_gen.next());
    }
    return KMaterialAlbedoUntextured.untextured(this.vec_gen.next());
  }
}
