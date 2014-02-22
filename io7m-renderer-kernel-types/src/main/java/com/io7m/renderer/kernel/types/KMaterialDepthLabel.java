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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Labels for depth-rendering properties.
 */

public enum KMaterialDepthLabel
  implements
  KLabel
{
  /**
   * Constant-depth materials.
   */

  DEPTH_CONSTANT("C"),

  /**
   * Mapped-depth materials.
   */

  DEPTH_MAPPED("M"),

  /**
   * Uniform-depth materials.
   */

  DEPTH_UNIFORM("U");

  /**
   * Derive a depth label for the given opaque instance.
   * 
   * @param albedo
   *          The material's albedo label
   * @param instance
   *          The opaque instance
   * @return A depth label
   * @throws ConstraintError
   *           Iff any parameter is <code>null</code>
   */

  public static KMaterialDepthLabel fromInstanceOpaque(
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KInstanceOpaque instance)
    throws ConstraintError
  {
    try {
      Constraints.constrainNotNull(albedo, "Albedo");
      Constraints.constrainNotNull(instance, "Instance");

      return instance.instanceGetMaterial().materialOpaqueVisitableAccept(
        new KMaterialOpaqueVisitor<KMaterialDepthLabel, ConstraintError>() {
          @Override public KMaterialDepthLabel materialVisitOpaqueAlphaDepth(
            final @Nonnull KMaterialOpaqueAlphaDepth m)
            throws ConstraintError
          {
            switch (albedo) {
              case ALBEDO_COLOURED:
              {
                return KMaterialDepthLabel.DEPTH_UNIFORM;
              }
              case ALBEDO_TEXTURED:
              {
                return KMaterialDepthLabel.DEPTH_MAPPED;
              }
            }

            throw new UnreachableCodeException();
          }

          @Override public KMaterialDepthLabel materialVisitOpaqueRegular(
            final @Nonnull KMaterialOpaqueRegular m)
            throws ConstraintError
          {
            return KMaterialDepthLabel.DEPTH_CONSTANT;
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final @Nonnull String code;

  private KMaterialDepthLabel(
    final @Nonnull String in_code)
  {
    this.code = in_code;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }
}
