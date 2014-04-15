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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

/**
 * Labels for forward-rendering lit specular-only translucent objects.
 */

@Immutable public final class KMaterialForwardTranslucentSpecularOnlyLitLabel implements
  KTexturesRequiredType,
  KMaterialLabelSpecularOnlyType,
  KMaterialLabelLitType,
  KMaterialLabelTranslucentType
{
  /**
   * @return The set of all possible lit translucent specular-only labels.
   */

  public static @Nonnull
    Set<KMaterialForwardTranslucentSpecularOnlyLitLabel>
    allLabels()
  {
    try {
      final Set<KMaterialForwardTranslucentSpecularOnlyLitLabel> s =
        new HashSet<KMaterialForwardTranslucentSpecularOnlyLitLabel>();
      final Set<KMaterialForwardTranslucentSpecularOnlyLabel> o =
        KMaterialForwardTranslucentSpecularOnlyLabel.allLabels();

      for (final KMaterialForwardTranslucentSpecularOnlyLabel r : o) {
        if (r.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE) {
          for (final KLightLabel l : KLightLabel.values()) {
            for (final KMaterialAlphaOpacityType a : KMaterialAlphaOpacityType
              .values()) {
              s.add(new KMaterialForwardTranslucentSpecularOnlyLitLabel(
                l,
                r,
                a));
            }
          }
        }
      }

      return s;
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull KLightLabel in_light,
    final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel in_specular,
    final @Nonnull KMaterialAlphaOpacityType in_alpha)
  {
    return String.format(
      "fwd_TSO_%s_%s_%s",
      in_light.labelGetCode(),
      in_specular.labelGetCode(),
      in_alpha.labelGetCode());
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull
    KMaterialForwardTranslucentSpecularOnlyLitLabel
    newLabel(
      final @Nonnull KLightLabel in_light,
      final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel in_specular_only,
      final @Nonnull KMaterialAlphaOpacityType in_alpha)
      throws ConstraintError
  {
    return new KMaterialForwardTranslucentSpecularOnlyLitLabel(
      in_light,
      in_specular_only,
      in_alpha);
  }

  private final @Nonnull String                            code;
  private final @Nonnull KLightLabel                       light;
  private final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel specular;
  private final int                                        textures;
  private final @Nonnull KMaterialAlphaOpacityType         alpha;

  private KMaterialForwardTranslucentSpecularOnlyLitLabel(
    final @Nonnull KLightLabel in_light,
    final @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel in_specular,
    final @Nonnull KMaterialAlphaOpacityType in_alpha)
    throws ConstraintError
  {
    this.alpha = Constraints.constrainNotNull(in_alpha, "Alpha");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");

    Constraints.constrainArbitrary(
      in_specular.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE,
      "Normal vectors available");

    this.light = Constraints.constrainNotNull(in_light, "Light");
    this.code =
      KMaterialForwardTranslucentSpecularOnlyLitLabel.makeLabelCode(
        in_light,
        in_specular,
        in_alpha);

    this.textures =
      this.light.texturesGetRequired() + this.specular.texturesGetRequired();
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public @Nonnull KLightLabel labelGetLight()
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

  @Override public KMaterialAlphaOpacityType labelGetAlphaType()
  {
    return this.alpha;
  }
}
