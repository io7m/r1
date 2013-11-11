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

package com.io7m.renderer.kernel_shaders;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

final class ShadowLabels
{
  static enum Albedo
  {
    ALBEDO_COLOURED("BC"),
    ALBEDO_TEXTURED("BT");

    protected final @Nonnull String code;

    private Albedo(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  static enum Alpha
  {
    ALPHA_OPAQUE("AO"),
    ALPHA_TRANSLUCENT("AT");

    protected final @Nonnull String code;

    private Alpha(
      final @Nonnull String code)
    {
      this.code = code;
    }
  }

  protected static boolean makeImpliesUV(
    @SuppressWarnings("unused") final @Nonnull Alpha alpha,
    final @Nonnull Albedo albedo)
  {
    switch (albedo) {
      case ALBEDO_COLOURED:
        break;
      case ALBEDO_TEXTURED:
        return true;
    }

    return false;
  }

  static final class ShadowLabel
  {
    private final boolean         implies_uv;
    private final @Nonnull Albedo albedo;
    private final @Nonnull Alpha  alpha;
    private final @Nonnull String code;

    ShadowLabel(
      final @Nonnull Albedo albedo,
      final @Nonnull Alpha alpha)
      throws ConstraintError
    {
      this.albedo = Constraints.constrainNotNull(albedo, "Albedo");
      this.alpha = Constraints.constrainNotNull(alpha, "Alpha");
      this.implies_uv = ShadowLabels.makeImpliesUV(alpha, albedo);

      final StringBuilder b = new StringBuilder();
      b.append(albedo.code);
      b.append("_");
      b.append(alpha.code);
      this.code = b.toString();
    }

    public @Nonnull Albedo getAlbedo()
    {
      return this.albedo;
    }

    public @Nonnull Alpha getAlpha()
    {
      return this.alpha;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }

    public boolean impliesUV()
    {
      return this.implies_uv;
    }
  }

  static @Nonnull List<ShadowLabel> allLabels()
    throws ConstraintError
  {
    final ArrayList<ShadowLabel> results = new ArrayList<ShadowLabel>();

    for (final Alpha a : Alpha.values()) {
      for (final Albedo b : Albedo.values()) {
        results.add(new ShadowLabel(b, a));
      }
    }

    return results;
  }
}
