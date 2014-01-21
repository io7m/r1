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

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.IntegerGenerator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

public final class SBTexture2DGenerator implements
  Generator<SBTexture2DDescription>
{
  private final @Nonnull PathVirtualGenerator path_gen;
  private final @Nonnull IntegerGenerator     wrap_s_index;
  private final @Nonnull IntegerGenerator     wrap_t_index;
  private final @Nonnull IntegerGenerator     texture_min_index;
  private final @Nonnull IntegerGenerator     texture_mag_index;

  public SBTexture2DGenerator()
  {
    this.path_gen = new PathVirtualGenerator();
    this.wrap_s_index =
      new IntegerGenerator(0, TextureWrapS.values().length - 1);
    this.wrap_t_index =
      new IntegerGenerator(0, TextureWrapT.values().length - 1);
    this.texture_min_index =
      new IntegerGenerator(0, TextureFilterMinification.values().length - 1);
    this.texture_mag_index =
      new IntegerGenerator(0, TextureFilterMagnification.values().length - 1);
  }

  @Override public SBTexture2DDescription next()
  {
    try {
      final TextureWrapS wrap_mode_s =
        TextureWrapS.values()[this.wrap_s_index.nextInt()];
      final TextureWrapT wrap_mode_t =
        TextureWrapT.values()[this.wrap_t_index.nextInt()];
      final TextureFilterMinification texture_min =
        TextureFilterMinification.values()[this.texture_min_index.nextInt()];
      final TextureFilterMagnification texture_mag =
        TextureFilterMagnification.values()[this.texture_mag_index.nextInt()];

      return new SBTexture2DDescription(
        this.path_gen.next(),
        wrap_mode_s,
        wrap_mode_t,
        texture_min,
        texture_mag);
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }
}
