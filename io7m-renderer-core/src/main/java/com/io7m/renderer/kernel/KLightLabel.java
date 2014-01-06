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

import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.KLight.KProjective;

public enum KLightLabel
{
  LIGHT_LABEL_DIRECTIONAL("LD"),

  LIGHT_LABEL_SPHERICAL("LS"),

  LIGHT_LABEL_PROJECTIVE("LP"),
  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC("LPSMB"),
  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444("LPSMBP4"),
  LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE("LPSMV")

  ;

  private final @Nonnull String code;

  private KLightLabel(
    final @Nonnull String code)
  {
    this.code = code;
  }

  @Override public @Nonnull String toString()
  {
    return this.code;
  }

  public static @Nonnull KLightLabel fromLight(
    final @Nonnull KGraphicsCapabilities caps,
    final @Nonnull KLight light)
  {
    switch (light.getType()) {
      case LIGHT_DIRECTIONAL:
        return LIGHT_LABEL_DIRECTIONAL;
      case LIGHT_PROJECTIVE:
      {
        final KProjective projective = (KProjective) light;
        final Option<KShadow> shadow_opt = projective.getShadow();
        if (shadow_opt.isNone()) {
          return LIGHT_LABEL_PROJECTIVE;
        }
        final KShadow shadow = ((Option.Some<KShadow>) shadow_opt).value;
        switch (shadow.getType()) {
          case SHADOW_MAPPED_BASIC:
            if (caps.getSupportsDepthTextures()) {
              return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC;
            }
            return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_BASIC_PACKED4444;
          case SHADOW_MAPPED_SOFT:
            return LIGHT_LABEL_PROJECTIVE_SHADOW_MAPPED_VARIANCE;
        }
        throw new UnreachableCodeException();
      }
      case LIGHT_SPHERE:
        return LIGHT_LABEL_SPHERICAL;
    }

    throw new UnreachableCodeException();
  }
}
