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
  DEPTH_CONSTANT("C", false),
  DEPTH_CONSTANT_PACKED4444("CP4", true),
  DEPTH_MAPPED("M", false),

  DEPTH_MAPPED_PACKED4444("MP4", true),
  DEPTH_UNIFORM("U", false),
  DEPTH_UNIFORM_PACKED4444("UP4", true)

  ;

  public static @Nonnull KMaterialDepthLabel fromMaterial(
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull KMaterialAlbedoLabel albedo,
    final @Nonnull KMaterial material)
  {
    final KMaterialAlpha a = material.getAlpha();

    switch (a.getOpacityType()) {
      case ALPHA_OPAQUE:
      {
        if (caps.getSupportsDepthTextures()) {
          return DEPTH_CONSTANT;
        }

        return DEPTH_CONSTANT_PACKED4444;
      }
      case ALPHA_OPAQUE_ALBEDO_ALPHA_TO_DEPTH:
      {
        if (caps.getSupportsDepthTextures()) {
          switch (albedo) {
            case ALBEDO_COLOURED:
              return DEPTH_UNIFORM;
            case ALBEDO_TEXTURED:
              return DEPTH_MAPPED;
          }
        } else {
          switch (albedo) {
            case ALBEDO_COLOURED:
              return DEPTH_UNIFORM_PACKED4444;
            case ALBEDO_TEXTURED:
              return DEPTH_MAPPED_PACKED4444;
          }
        }

        throw new UnreachableCodeException();
      }
      case ALPHA_TRANSLUCENT:
      {
        if (caps.getSupportsDepthTextures()) {
          switch (albedo) {
            case ALBEDO_COLOURED:
              return DEPTH_UNIFORM;
            case ALBEDO_TEXTURED:
              return DEPTH_MAPPED;
          }
        } else {
          switch (albedo) {
            case ALBEDO_COLOURED:
              return DEPTH_UNIFORM_PACKED4444;
            case ALBEDO_TEXTURED:
              return DEPTH_MAPPED_PACKED4444;
          }
        }
      }
    }

    throw new UnreachableCodeException();
  }

  public static @Nonnull KMaterialDepthLabel fromShadowLabel(
    final @Nonnull KMaterialShadowLabel label)
  {
    switch (label) {
      case SHADOW_BASIC_DEPTH_CONSTANT:
        return DEPTH_CONSTANT;
      case SHADOW_BASIC_DEPTH_CONSTANT_PACKED4444:
        return DEPTH_CONSTANT_PACKED4444;
      case SHADOW_BASIC_DEPTH_MAPPED:
        return DEPTH_MAPPED;
      case SHADOW_BASIC_DEPTH_MAPPED_PACKED4444:
        return DEPTH_MAPPED_PACKED4444;
      case SHADOW_BASIC_DEPTH_UNIFORM:
        return DEPTH_UNIFORM;
      case SHADOW_BASIC_DEPTH_UNIFORM_PACKED4444:
        return DEPTH_UNIFORM_PACKED4444;
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull String code;
  private final @Nonnull String name;
  private final boolean         packed;

  private KMaterialDepthLabel(
    final @Nonnull String code,
    final boolean packed)
  {
    this.code = code;
    this.name = "depth_" + code;
    this.packed = packed;
  }

  public @Nonnull String getCode()
  {
    return this.code;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public boolean isPacked()
  {
    return this.packed;
  }
}
