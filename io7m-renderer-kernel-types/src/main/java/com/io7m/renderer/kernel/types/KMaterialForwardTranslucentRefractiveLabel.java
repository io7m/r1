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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;

/**
 * Labels for forward-rendering refractive translucent objects.
 */

public final class KMaterialForwardTranslucentRefractiveLabel implements
  KTexturesRequiredType,
  KMaterialLabelImpliesUVType,
  KLabelType
{
  /**
   * @return The set of all possible translucent refractive labels.
   */

  public static @Nonnull
    Set<KMaterialForwardTranslucentRefractiveLabel>
    allLabels()
  {
    try {
      final Set<KMaterialForwardTranslucentRefractiveLabel> s =
        new HashSet<KMaterialForwardTranslucentRefractiveLabel>();

      for (final KMaterialRefractiveLabel r : KMaterialRefractiveLabel
        .values()) {
        for (final KMaterialNormalLabel n : KMaterialNormalLabel.values()) {
          switch (n) {
            case NORMAL_NONE:
              break;
            case NORMAL_MAPPED:
            case NORMAL_VERTEX:
              s
                .add(KMaterialForwardTranslucentRefractiveLabel
                  .newLabel(r, n));
              break;
          }
        }
      }

      return s;
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static @Nonnull String makeLabelCode(
    final @Nonnull KMaterialRefractiveLabel refractive,
    final @Nonnull KMaterialNormalLabel normal)
  {
    switch (normal) {
      case NORMAL_NONE:
      {
        throw new UnreachableCodeException();
      }
      case NORMAL_MAPPED:
      case NORMAL_VERTEX:
      {
        return String.format(
          "fwd_U_%s_%s",
          refractive.labelGetCode(),
          normal.labelGetCode());
      }
    }

    throw new UnreachableCodeException();
  }

  /**
   * Create a new refractive forward-rendering label.
   * 
   * @param in_refractive
   *          The refractive properties
   * @param in_normal
   *          The normal label
   * @return A new label
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialForwardTranslucentRefractiveLabel newLabel(
    final @Nonnull KMaterialRefractiveLabel in_refractive,
    final @Nonnull KMaterialNormalLabel in_normal)
    throws ConstraintError
  {
    return new KMaterialForwardTranslucentRefractiveLabel(
      in_refractive,
      in_normal);
  }

  private final @Nonnull String                   code;
  private final @Nonnull KMaterialNormalLabel     normal;
  private final @Nonnull KMaterialRefractiveLabel refractive;

  private KMaterialForwardTranslucentRefractiveLabel(
    final @Nonnull KMaterialRefractiveLabel in_refractive,
    final @Nonnull KMaterialNormalLabel in_normal)
    throws ConstraintError
  {
    this.refractive =
      Constraints.constrainNotNull(in_refractive, "Refractive");
    this.normal = Constraints.constrainNotNull(in_normal, "Normal");

    Constraints.constrainArbitrary(
      this.normal != KMaterialNormalLabel.NORMAL_NONE,
      "Normal vectors provided");

    this.code =
      KMaterialForwardTranslucentRefractiveLabel.makeLabelCode(
        in_refractive,
        in_normal);
  }

  /**
   * @return The refractive label
   */

  public @Nonnull KMaterialRefractiveLabel getRefractive()
  {
    return this.refractive;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  /**
   * @return The normal-mapping label
   */

  public @Nonnull KMaterialNormalLabel labelGetNormal()
  {
    return this.normal;
  }

  @Override public boolean labelImpliesUV()
  {
    return this.normal == KMaterialNormalLabel.NORMAL_MAPPED;
  }

  @Override public int texturesGetRequired()
  {
    /**
     * At most one texture for the normal map, and one for the scene that will
     * be refracted.
     */
    return this.normal.texturesGetRequired() + 1;
  }
}
