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
 * Labels for forward-rendering lit regular translucent objects.
 */

@Immutable public final class KMaterialForwardTranslucentRegularLitLabel implements
  KTexturesRequired,
  KMaterialLabelRegular,
  KMaterialLabelLit
{
  /**
   * @return The set of all possible lit translucent regular labels.
   */

  public static @Nonnull
    Set<KMaterialForwardTranslucentRegularLitLabel>
    allLabels()
  {
    try {
      final Set<KMaterialForwardTranslucentRegularLitLabel> s =
        new HashSet<KMaterialForwardTranslucentRegularLitLabel>();
      final Set<KMaterialForwardRegularLabel> o =
        KMaterialForwardRegularLabel.allLabels();

      for (final KMaterialForwardRegularLabel r : o) {
        if (r.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE) {
          for (final KLightLabel l : KLightLabel.values()) {
            s.add(new KMaterialForwardTranslucentRegularLitLabel(l, r));
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
    final @Nonnull KMaterialForwardRegularLabel in_opaque)
  {
    final StringBuilder code = new StringBuilder();
    code.append(in_light.labelGetCode());
    code.append("_T_");
    code.append(in_opaque.labelGetCode());
    return code.toString();
  }

  private final @Nonnull String                       code;
  private final @Nonnull KLightLabel                  light;
  private final @Nonnull KMaterialForwardRegularLabel regular;
  private final int                                   textures;

  private KMaterialForwardTranslucentRegularLitLabel(
    final @Nonnull KLightLabel in_light,
    final @Nonnull KMaterialForwardRegularLabel in_regular)
    throws ConstraintError
  {
    this.regular = Constraints.constrainNotNull(in_regular, "Regular");

    Constraints.constrainArbitrary(
      in_regular.labelGetNormal() != KMaterialNormalLabel.NORMAL_NONE,
      "Normal vectors available");

    this.light = Constraints.constrainNotNull(in_light, "Light");
    this.code =
      KMaterialForwardTranslucentRegularLitLabel.makeLabelCode(
        in_light,
        in_regular);

    this.textures =
      this.light.texturesGetRequired() + this.regular.texturesGetRequired();
  }

  /**
   * @return The regular label for this material.
   */

  public @Nonnull KMaterialLabelRegularUnlit getRegular()
  {
    return this.regular;
  }

  @Override public KMaterialAlbedoLabel labelGetAlbedo()
  {
    return this.regular.labelGetAlbedo();
  }

  @Override public @Nonnull String labelGetCode()
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

  @Override public @Nonnull KLightLabel labelGetLight()
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
