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

import java.util.HashSet;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * Labels for forward-rendering of specular-only objects.
 */

@EqualityStructural public final class KMaterialForwardTranslucentSpecularOnlyLabel implements
  KTexturesRequiredType,
  KMaterialLabelSpecularOnlyType
{
  /**
   * @return The set of all possible specular-only labels.
   */

  public static Set<KMaterialForwardTranslucentSpecularOnlyLabel> allLabels()
  {
    final Set<KMaterialForwardTranslucentSpecularOnlyLabel> s =
      new HashSet<KMaterialForwardTranslucentSpecularOnlyLabel>();

    for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
      assert normal != null;
      if (normal == KMaterialNormalLabel.NORMAL_NONE) {
        continue;
      }
      for (final KMaterialSpecularLabel specular : KMaterialSpecularLabel
        .values()) {
        assert specular != null;
        s.add(KMaterialForwardTranslucentSpecularOnlyLabel.newLabel(
          normal,
          specular));
      }
    }

    return s;
  }

  private static boolean makeImpliesSpecularMap(
    final KMaterialSpecularLabel specular)
  {
    return specular == KMaterialSpecularLabel.SPECULAR_MAPPED;
  }

  private static boolean makeImpliesUV(
    final KMaterialNormalLabel normal,
    final KMaterialSpecularLabel specular)
  {
    switch (normal) {
      case NORMAL_MAPPED:
        return true;
      case NORMAL_NONE:
      case NORMAL_VERTEX:
        break;
    }
    switch (specular) {
      case SPECULAR_CONSTANT:
      case SPECULAR_NONE:
        break;
      case SPECULAR_MAPPED:
        return true;
    }

    return false;
  }

  private static String makeLabelCode(
    final KMaterialNormalLabel normal,
    final KMaterialSpecularLabel specular)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(normal.labelGetCode());
    if (specular != KMaterialSpecularLabel.SPECULAR_NONE) {
      buffer.append("_");
      buffer.append(specular.labelGetCode());
    }
    final String r = buffer.toString();
    assert r != null;
    return r;
  }

  /**
   * Create a new forward-rendering label.
   * 
   * @param in_normal
   *          The normal label
   * @param in_specular
   *          The specular label
   * @return A new label
   */

  public static KMaterialForwardTranslucentSpecularOnlyLabel newLabel(
    final KMaterialNormalLabel in_normal,
    final KMaterialSpecularLabel in_specular)
  {
    return new KMaterialForwardTranslucentSpecularOnlyLabel(
      in_normal,
      in_specular);
  }

  private final String                 code;
  private final boolean                implies_specular_map;
  private final boolean                implies_uv;
  private final KMaterialNormalLabel   normal;
  private final KMaterialSpecularLabel specular;
  private final int                    texture_units_required;

  private KMaterialForwardTranslucentSpecularOnlyLabel(
    final KMaterialNormalLabel in_normal,
    final KMaterialSpecularLabel in_specular)
  {
    this.normal = NullCheck.notNull(in_normal, "Normal");
    this.specular = NullCheck.notNull(in_specular, "Specular");

    this.code =
      KMaterialForwardTranslucentSpecularOnlyLabel.makeLabelCode(
        in_normal,
        in_specular);

    this.implies_uv =
      KMaterialForwardTranslucentSpecularOnlyLabel.makeImpliesUV(
        in_normal,
        in_specular);

    this.implies_specular_map =
      KMaterialForwardTranslucentSpecularOnlyLabel
        .makeImpliesSpecularMap(in_specular);

    int req = 0;
    req += in_normal.texturesGetRequired();
    req += in_specular.texturesGetRequired();
    this.texture_units_required = req;
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
    final KMaterialForwardTranslucentSpecularOnlyLabel other =
      (KMaterialForwardTranslucentSpecularOnlyLabel) obj;
    return this.code.equals(other.code)
      && (this.implies_specular_map == other.implies_specular_map)
      && (this.implies_uv == other.implies_uv)
      && (this.normal == other.normal)
      && (this.specular == other.specular)
      && (this.texture_units_required == other.texture_units_required);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + (this.implies_specular_map ? 1231 : 1237);
    result = (prime * result) + (this.implies_uv ? 1231 : 1237);
    result = (prime * result) + this.normal.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.texture_units_required;
    return result;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public KMaterialSpecularLabel labelGetSpecular()
  {
    return this.specular;
  }

  @Override public boolean labelImpliesSpecularMap()
  {
    return this.implies_specular_map;
  }

  @Override public boolean labelImpliesUV()
  {
    return this.implies_uv;
  }

  @Override public int texturesGetRequired()
  {
    return this.texture_units_required;
  }
}
