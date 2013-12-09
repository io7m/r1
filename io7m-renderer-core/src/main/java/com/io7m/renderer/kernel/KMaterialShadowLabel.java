/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;

enum KMaterialShadowLabel
{
  SHADOW_BASIC_OPAQUE("BO"),
  SHADOW_BASIC_TRANSLUCENT("BT"),
  SHADOW_BASIC_TRANSLUCENT_TEXTURED("BX"),

  SHADOW_BASIC_OPAQUE_PACKED4444("BOP4"),
  SHADOW_BASIC_TRANSLUCENT_PACKED4444("BTP4"),
  SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444("BXP4"),

  SHADOW_VARIANCE_OPAQUE("VO"),
  SHADOW_VARIANCE_TRANSLUCENT("VT"),
  SHADOW_VARIANCE_TRANSLUCENT_TEXTURED("VX"),

  ;

  public static @Nonnull KMaterialShadowLabel fromShadow(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull KMaterialShadowCastLabel caster,
    final @Nonnull KShadow shadow)
    throws ConstraintError
  {
    Constraints.constrainNotNull(caster, "Caster");
    Constraints.constrainNotNull(shadow, "Shadow");

    switch (shadow.getType()) {
      case SHADOW_MAPPED_BASIC:
      {
        return KMaterialShadowLabel.fromShadowBasic(capabilities, caster);
      }
      case SHADOW_MAPPED_VARIANCE:
      {
        return KMaterialShadowLabel.fromShadowVariance(caster);
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull KMaterialShadowLabel fromShadowVariance(
    final @Nonnull KMaterialShadowCastLabel caster)
  {
    switch (caster) {
      case SHADOW_CAST_OPAQUE:
        return SHADOW_VARIANCE_OPAQUE;
      case SHADOW_CAST_TRANSLUCENT:
        return SHADOW_VARIANCE_TRANSLUCENT;
      case SHADOW_CAST_TRANSLUCENT_TEXTURED:
        return SHADOW_VARIANCE_TRANSLUCENT_TEXTURED;
    }

    throw new UnimplementedCodeException();
  }

  private static @Nonnull KMaterialShadowLabel fromShadowBasic(
    final @Nonnull KGraphicsCapabilities capabilities,
    final @Nonnull KMaterialShadowCastLabel caster)
  {
    if (capabilities.getSupportsDepthTextures()) {
      switch (caster) {
        case SHADOW_CAST_OPAQUE:
          return SHADOW_BASIC_OPAQUE;
        case SHADOW_CAST_TRANSLUCENT:
          return SHADOW_BASIC_TRANSLUCENT;
        case SHADOW_CAST_TRANSLUCENT_TEXTURED:
          return SHADOW_BASIC_TRANSLUCENT_TEXTURED;
      }
    } else {
      switch (caster) {
        case SHADOW_CAST_OPAQUE:
          return SHADOW_BASIC_OPAQUE_PACKED4444;
        case SHADOW_CAST_TRANSLUCENT:
          return SHADOW_BASIC_TRANSLUCENT_PACKED4444;
        case SHADOW_CAST_TRANSLUCENT_TEXTURED:
          return SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444;
      }
    }

    throw new UnreachableCodeException();
  }

  private final String name;

  private KMaterialShadowLabel(
    final @Nonnull String name)
  {
    this.name = name;
  }

  public @Nonnull String getCode()
  {
    return this.name;
  }

  public boolean impliesUV()
  {
    switch (this) {
      case SHADOW_BASIC_OPAQUE_PACKED4444:
      case SHADOW_BASIC_OPAQUE:
      case SHADOW_BASIC_TRANSLUCENT:
      case SHADOW_BASIC_TRANSLUCENT_PACKED4444:
      case SHADOW_VARIANCE_OPAQUE:
      case SHADOW_VARIANCE_TRANSLUCENT:
        return false;
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED_PACKED4444:
      case SHADOW_BASIC_TRANSLUCENT_TEXTURED:
      case SHADOW_VARIANCE_TRANSLUCENT_TEXTURED:
        return true;
    }

    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }
}
