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
 * Labels for forward-rendering lit specular-only translucent objects.
 */

@EqualityStructural public final class KMaterialForwardTranslucentSpecularOnlyLitLabel implements
  KTexturesRequiredType,
  KMaterialLabelSpecularOnlyType,
  KMaterialLabelLitType,
  KMaterialLabelTranslucentType
{

  /**
   * @return The set of all possible lit translucent specular-only labels.
   */

  public static
    Set<KMaterialForwardTranslucentSpecularOnlyLitLabel>
    allLabels()
  {
    final Set<KMaterialForwardTranslucentSpecularOnlyLitLabel> s =
      new HashSet<KMaterialForwardTranslucentSpecularOnlyLitLabel>();
    final Set<KMaterialForwardTranslucentSpecularOnlyLabel> o =
      KMaterialForwardTranslucentSpecularOnlyLabel.allLabels();

    for (final KMaterialForwardTranslucentSpecularOnlyLabel r : o) {
      assert r != null;
      if (r.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE) {
        for (final KLightLabel l : KLightLabel.values()) {
          assert l != null;
          for (final KMaterialAlphaOpacityType a : KMaterialAlphaOpacityType
            .values()) {
            assert a != null;
            s
              .add(new KMaterialForwardTranslucentSpecularOnlyLitLabel(
                l,
                r,
                a));
          }
        }
      }
    }

    return s;
  }

  private static String makeLabelCode(
    final KLightLabel in_light,
    final KMaterialForwardTranslucentSpecularOnlyLabel in_specular,
    final KMaterialAlphaOpacityType in_alpha)
  {
    final String r =
      String.format(
        "fwd_TSO_%s_%s_%s",
        in_light.labelGetCode(),
        in_specular.labelGetCode(),
        in_alpha.labelGetCode());
    assert r != null;
    return r;
  }

  /**
   * Construct a new label for the given light and specular-only label.
   * 
   * @param in_light
   *          The light
   * @param in_specular_only
   *          The specular-only label
   * @param in_alpha
   *          The alpha label
   * @return A new label
   */

  public static KMaterialForwardTranslucentSpecularOnlyLitLabel newLabel(
    final KLightLabel in_light,
    final KMaterialForwardTranslucentSpecularOnlyLabel in_specular_only,
    final KMaterialAlphaOpacityType in_alpha)
  {
    return new KMaterialForwardTranslucentSpecularOnlyLitLabel(
      in_light,
      in_specular_only,
      in_alpha);
  }

  private final KMaterialAlphaOpacityType                    alpha;
  private final String                                       code;
  private final KLightLabel                                  light;
  private final KMaterialForwardTranslucentSpecularOnlyLabel specular;
  private final int                                          textures;

  private KMaterialForwardTranslucentSpecularOnlyLitLabel(
    final KLightLabel in_light,
    final KMaterialForwardTranslucentSpecularOnlyLabel in_specular,
    final KMaterialAlphaOpacityType in_alpha)
  {
    this.alpha = NullCheck.notNull(in_alpha, "Alpha");
    this.specular = NullCheck.notNull(in_specular, "Specular");

    if (in_specular.labelGetNormal() == KMaterialNormalLabel.NORMAL_NONE) {
      throw new IllegalArgumentException("No normal vectors available");
    }

    this.light = NullCheck.notNull(in_light, "Light");
    this.code =
      KMaterialForwardTranslucentSpecularOnlyLitLabel.makeLabelCode(
        in_light,
        in_specular,
        in_alpha);

    this.textures =
      this.light.texturesGetRequired() + this.specular.texturesGetRequired();
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
    final KMaterialForwardTranslucentSpecularOnlyLitLabel other =
      (KMaterialForwardTranslucentSpecularOnlyLitLabel) obj;
    return (this.alpha == other.alpha)
      && this.code.equals(other.code)
      && (this.light == other.light)
      && this.specular.equals(other.specular)
      && (this.textures == other.textures);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.alpha.hashCode();
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + this.light.hashCode();
    result = (prime * result) + this.specular.hashCode();
    result = (prime * result) + this.textures;
    return result;
  }

  @Override public KMaterialAlphaOpacityType labelGetAlphaType()
  {
    return this.alpha;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public KLightLabel labelGetLight()
  {
    return this.light;
  }

  @Override public KMaterialNormalLabel labelGetNormal()
  {
    return this.specular.labelGetNormal();
  }

  @Override public KMaterialSpecularLabel labelGetSpecular()
  {
    return this.specular.labelGetSpecular();
  }

  @Override public boolean labelImpliesSpecularMap()
  {
    return this.specular.labelImpliesSpecularMap();
  }

  @Override public boolean labelImpliesUV()
  {
    return this.specular.labelImpliesUV();
  }

  @Override public int texturesGetRequired()
  {
    return this.textures;
  }
}
