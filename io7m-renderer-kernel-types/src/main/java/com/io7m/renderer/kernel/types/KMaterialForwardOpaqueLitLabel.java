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
 * Labels for forward-rendering lit opaque objects.
 */

@EqualityStructural public final class KMaterialForwardOpaqueLitLabel implements
  KTexturesRequiredType,
  KMaterialLabelLitType,
  KMaterialLabelRegularType
{

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.code.hashCode();
    result = (prime * result) + this.light.hashCode();
    result = (prime * result) + this.regular.hashCode();
    result = (prime * result) + this.textures;
    return result;
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
    final KMaterialForwardOpaqueLitLabel other =
      (KMaterialForwardOpaqueLitLabel) obj;
    return this.code.equals(other.code)
      && (this.light == other.light)
      && (this.regular.equals(other.regular))
      && (this.textures == other.textures);
  }

  /**
   * @return The set of all possible lit opaque labels.
   */

  public static Set<KMaterialForwardOpaqueLitLabel> allLabels()
  {
    final Set<KMaterialForwardOpaqueLitLabel> s =
      new HashSet<KMaterialForwardOpaqueLitLabel>();
    final Set<KMaterialForwardRegularLabel> o =
      KMaterialForwardRegularLabel.allLabels();

    for (final KMaterialForwardRegularLabel r : o) {
      if (r.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE) {
        for (final KLightLabel l : KLightLabel.values()) {
          assert l != null;
          s.add(new KMaterialForwardOpaqueLitLabel(l, r));
        }
      }
    }

    return s;
  }

  private static String makeLabelCode(
    final KLightLabel in_light,
    final KMaterialForwardRegularLabel in_opaque)
  {
    final String s =
      String.format(
        "fwd_%s_O_%s",
        in_light.labelGetCode(),
        in_opaque.labelGetCode());
    assert s != null;
    return s;
  }

  /**
   * Construct a new label for the given light and regular label.
   * 
   * @param in_light
   *          The light
   * @param in_regular
   *          The regular label
   * @return A new label
   */

  public static KMaterialForwardOpaqueLitLabel newLabel(
    final KLightLabel in_light,
    final KMaterialForwardRegularLabel in_regular)
  {
    return new KMaterialForwardOpaqueLitLabel(in_light, in_regular);
  }

  private final String                       code;
  private final KLightLabel                  light;
  private final KMaterialForwardRegularLabel regular;
  private final int                          textures;

  private KMaterialForwardOpaqueLitLabel(
    final KLightLabel in_light,
    final KMaterialForwardRegularLabel in_regular)
  {
    this.regular = NullCheck.notNull(in_regular, "Regular");

    if (in_regular.labelGetNormal() == KMaterialNormalLabel.NORMAL_NONE) {
      throw new IllegalArgumentException("No normal vectors available");
    }

    this.light = NullCheck.notNull(in_light, "Light");
    this.code =
      KMaterialForwardOpaqueLitLabel.makeLabelCode(in_light, in_regular);

    this.textures =
      this.light.texturesGetRequired() + this.regular.texturesGetRequired();
  }

  /**
   * @return The label of the current light for this material.
   */

  public KLightLabel getLight()
  {
    return this.light;
  }

  /**
   * @return The regular label for this material.
   */

  public KMaterialLabelRegularType getRegular()
  {
    return this.regular;
  }

  @Override public KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.regular.labelGetAlbedo();
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public KMaterialEmissiveLabel labelGetEmissive()
  {
    return this.regular.labelGetEmissive();
  }

  @Override public KMaterialEnvironmentLabel labelGetEnvironment()
  {
    return this.regular.labelGetEnvironment();
  }

  @Override public KLightLabel labelGetLight()
  {
    return this.light;
  }

  @Override public KMaterialNormalLabel labelGetNormal()
  {
    return this.regular.labelGetNormal();
  }

  @Override public KMaterialSpecularLabel labelGetSpecular()
  {
    return this.regular.labelGetSpecular();
  }

  @Override public boolean labelImpliesSpecularMap()
  {
    return this.regular.labelImpliesSpecularMap();
  }

  @Override public boolean labelImpliesUV()
  {
    return this.regular.labelImpliesUV();
  }

  @Override public int texturesGetRequired()
  {
    return this.textures;
  }
}
