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
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;

enum KMaterialDepthVarianceLabel
{
  DEPTH_VARIANCE_CONSTANT("depth_variance_C"),
  DEPTH_VARIANCE_MAPPED("depth_variance_M"),
  DEPTH_VARIANCE_UNIFORM("depth_variance_U");

  public static @Nonnull KMaterialDepthVarianceLabel fromDepthLabel(
    final @Nonnull KMaterialDepthLabel k)
  {
    switch (k) {
      case DEPTH_CONSTANT:
        return DEPTH_VARIANCE_CONSTANT;
      case DEPTH_MAPPED:
        return DEPTH_VARIANCE_MAPPED;
      case DEPTH_UNIFORM:
        return DEPTH_VARIANCE_UNIFORM;
    }

    throw new UnreachableCodeException();
  }

  private final @Nonnull String name;

  private KMaterialDepthVarianceLabel(
    final @Nonnull String in_name)
  {
    this.name = in_name;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }
}
