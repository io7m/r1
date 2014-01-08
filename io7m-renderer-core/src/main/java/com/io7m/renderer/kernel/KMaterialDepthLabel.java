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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.UnreachableCodeException;

enum KMaterialDepthLabel
{
  DEPTH_CONSTANT("C"),
  DEPTH_MAPPED("M"),
  DEPTH_UNIFORM("U")

  ;

  public static @Nonnull KMaterialDepthLabel fromMaterial(
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KMaterial material)
  {
    final KMaterialAlpha a = material.getAlpha();

    switch (a.getOpacityType()) {
      case ALPHA_OPAQUE:
      {
        return DEPTH_CONSTANT;
      }
      case ALPHA_OPAQUE_ALBEDO_ALPHA_TO_DEPTH:
      {
        switch (albedo) {
          case ALBEDO_COLOURED:
            return DEPTH_UNIFORM;
          case ALBEDO_TEXTURED:
            return DEPTH_MAPPED;
        }

        throw new UnreachableCodeException();
      }
      case ALPHA_TRANSLUCENT:
      {
        switch (albedo) {
          case ALBEDO_COLOURED:
            return DEPTH_UNIFORM;
          case ALBEDO_TEXTURED:
            return DEPTH_MAPPED;
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull String code;
  private final @Nonnull String name;

  private KMaterialDepthLabel(
    final @Nonnull String code)
  {
    this.code = code;
    this.name = "depth_" + code;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }
}
