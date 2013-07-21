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

import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.Texture2DStatic;

@Immutable public final class SBTexture
{
  private final @Nonnull SBTextureDescription description;
  private final @Nonnull Texture2DStatic      texture;
  private final @Nonnull BufferedImage        image;

  public SBTexture(
    final @Nonnull Texture2DStatic texture,
    final @Nonnull BufferedImage image,
    final @Nonnull SBTextureDescription description)
  {
    this.texture = texture;
    this.image = image;
    this.description = description;
  }

  @Nonnull String getName()
  {
    return this.texture.getName();
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBTexture ");
    builder.append(this.texture);
    builder.append(" ");
    builder.append(this.description.getFile());
    builder.append("]");
    return builder.toString();
  }
}
