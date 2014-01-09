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
  SHADOW_BASIC_DEPTH_CONSTANT("C"),
  SHADOW_BASIC_DEPTH_UNIFORM("U"),
  SHADOW_BASIC_DEPTH_MAPPED("M"),

  SHADOW_BASIC_DEPTH_CONSTANT_PACKED4444("CP4"),
  SHADOW_BASIC_DEPTH_UNIFORM_PACKED4444("UP4"),
  SHADOW_BASIC_DEPTH_MAPPED_PACKED4444("MP4"),

  ;

  public static @Nonnull KMaterialShadowLabel fromShadow(
    final @Nonnull KMaterialDepthLabel caster,
    final @Nonnull KShadow shadow)
    throws ConstraintError
  {
    Constraints.constrainNotNull(caster, "Caster");
    Constraints.constrainNotNull(shadow, "Shadow");

    switch (shadow.getType()) {
      case SHADOW_MAPPED_BASIC:
      {
        return KMaterialShadowLabel.fromShadowBasic(caster);
      }
      case SHADOW_MAPPED_SOFT:
      {
        throw new UnimplementedCodeException();
      }
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull KMaterialShadowLabel fromShadowBasic(
    final @Nonnull KMaterialDepthLabel caster)
  {
    switch (caster) {
      case DEPTH_CONSTANT:
        return SHADOW_BASIC_DEPTH_CONSTANT;
      case DEPTH_CONSTANT_PACKED4444:
        return SHADOW_BASIC_DEPTH_CONSTANT_PACKED4444;
      case DEPTH_MAPPED:
        return SHADOW_BASIC_DEPTH_MAPPED;
      case DEPTH_MAPPED_PACKED4444:
        return SHADOW_BASIC_DEPTH_MAPPED_PACKED4444;
      case DEPTH_UNIFORM:
        return SHADOW_BASIC_DEPTH_UNIFORM;
      case DEPTH_UNIFORM_PACKED4444:
        return SHADOW_BASIC_DEPTH_UNIFORM_PACKED4444;
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
      case SHADOW_BASIC_DEPTH_CONSTANT:
      case SHADOW_BASIC_DEPTH_CONSTANT_PACKED4444:
      case SHADOW_BASIC_DEPTH_UNIFORM:
      case SHADOW_BASIC_DEPTH_UNIFORM_PACKED4444:
        return false;
      case SHADOW_BASIC_DEPTH_MAPPED:
      case SHADOW_BASIC_DEPTH_MAPPED_PACKED4444:
        return true;
    }

    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }
}
