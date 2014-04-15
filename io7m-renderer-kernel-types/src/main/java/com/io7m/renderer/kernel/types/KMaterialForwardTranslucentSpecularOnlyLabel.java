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
 * Labels for forward-rendering of specular-only objects.
 */

@Immutable public final class KMaterialForwardTranslucentSpecularOnlyLabel implements
  KTexturesRequiredType,
  KMaterialLabelSpecularOnlyType
{
  /**
   * @return The set of all possible specular-only labels.
   */

  public static @Nonnull Set<KMaterialForwardTranslucentSpecularOnlyLabel> allLabels()
  {
    try {
      final Set<KMaterialForwardTranslucentSpecularOnlyLabel> s =
        new HashSet<KMaterialForwardTranslucentSpecularOnlyLabel>();

      for (final KMaterialNormalLabel normal : KMaterialNormalLabel.values()) {
        if (normal == KMaterialNormalLabel.NORMAL_NONE) {
          continue;
        }
        for (final KMaterialSpecularLabel specular : KMaterialSpecularLabel
          .values()) {
          s.add(KMaterialForwardTranslucentSpecularOnlyLabel.newLabel(normal, specular));
        }
      }

      return s;
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static boolean makeImpliesSpecularMap(
    final @Nonnull KMaterialSpecularLabel specular)
  {
    return specular == KMaterialSpecularLabel.SPECULAR_MAPPED;
  }

  private static boolean makeImpliesUV(
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialSpecularLabel specular)
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

  private static @Nonnull String makeLabelCode(
    final @Nonnull KMaterialNormalLabel normal,
    final @Nonnull KMaterialSpecularLabel specular)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(normal.labelGetCode());
    if (specular != KMaterialSpecularLabel.SPECULAR_NONE) {
      buffer.append("_");
      buffer.append(specular.labelGetCode());
    }
    return buffer.toString();
  }

  /**
   * Create a new forward-rendering label.
   * 
   * @param in_normal
   *          The normal label
   * @param in_specular
   *          The specular label
   * @return A new label
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialForwardTranslucentSpecularOnlyLabel newLabel(
    final @Nonnull KMaterialNormalLabel in_normal,
    final @Nonnull KMaterialSpecularLabel in_specular)
    throws ConstraintError
  {
    return new KMaterialForwardTranslucentSpecularOnlyLabel(in_normal, in_specular);
  }

  private final @Nonnull String                 code;
  private final boolean                         implies_specular_map;
  private final boolean                         implies_uv;
  private final @Nonnull KMaterialNormalLabel   normal;
  private final @Nonnull KMaterialSpecularLabel specular;
  private final int                             texture_units_required;

  private KMaterialForwardTranslucentSpecularOnlyLabel(
    final @Nonnull KMaterialNormalLabel in_normal,
    final @Nonnull KMaterialSpecularLabel in_specular)
    throws ConstraintError
  {
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");
    this.specular = Constraints.constrainNotNull(in_specular, "Specular");

    this.code =
      KMaterialForwardTranslucentSpecularOnlyLabel.makeLabelCode(in_normal, in_specular);

    this.implies_uv =
      KMaterialForwardTranslucentSpecularOnlyLabel.makeImpliesUV(in_normal, in_specular);

    this.implies_specular_map =
      KMaterialForwardTranslucentSpecularOnlyLabel.makeImpliesSpecularMap(in_specular);

    int req = 0;
    req += in_normal.texturesGetRequired();
    req += in_specular.texturesGetRequired();
    this.texture_units_required = req;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public @Nonnull KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public @Nonnull KMaterialSpecularLabel labelGetSpecular()
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
