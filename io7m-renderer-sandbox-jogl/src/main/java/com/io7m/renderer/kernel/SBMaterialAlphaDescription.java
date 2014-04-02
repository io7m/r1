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

import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;

final class SBMaterialAlphaDescription
{
  private final float                              opacity;
  private final @Nonnull KMaterialAlphaOpacityType type;

  public SBMaterialAlphaDescription(
    final @Nonnull KMaterialAlphaOpacityType in_type,
    final float in_opacity)
  {
    this.type = in_type;
    this.opacity = in_opacity;
  }

  public float getOpacity()
  {
    return this.opacity;
  }

  public @Nonnull KMaterialAlphaOpacityType getType()
  {
    return this.type;
  }
}
