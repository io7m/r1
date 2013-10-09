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

import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jvvfs.PathVirtual;

public final class SBTextureCube
{
  private final @Nonnull PathVirtual       path;
  private final @Nonnull TextureCubeStatic texture;
  private final @Nonnull BufferedImage     positive_z;
  private final @Nonnull BufferedImage     negative_z;
  private final @Nonnull BufferedImage     positive_y;
  private final @Nonnull BufferedImage     negative_y;
  private final @Nonnull BufferedImage     positive_x;
  private final @Nonnull BufferedImage     negative_x;

  public @Nonnull PathVirtual getPath()
  {
    return this.path;
  }

  public @Nonnull BufferedImage getPositiveZ()
  {
    return this.positive_z;
  }

  public @Nonnull BufferedImage getNegativeZ()
  {
    return this.negative_z;
  }

  public @Nonnull BufferedImage getPositiveY()
  {
    return this.positive_y;
  }

  public @Nonnull BufferedImage getNegativeY()
  {
    return this.negative_y;
  }

  public @Nonnull BufferedImage getPositiveX()
  {
    return this.positive_x;
  }

  public @Nonnull BufferedImage getNegativeX()
  {
    return this.negative_x;
  }

  SBTextureCube(
    final @Nonnull PathVirtual path,
    final @Nonnull TextureCubeStatic texture,
    final @Nonnull BufferedImage positive_z,
    final @Nonnull BufferedImage negative_z,
    final @Nonnull BufferedImage positive_y,
    final @Nonnull BufferedImage negative_y,
    final @Nonnull BufferedImage positive_x,
    final @Nonnull BufferedImage negative_x)
  {
    this.path = path;
    this.texture = texture;
    this.positive_z = positive_z;
    this.negative_z = negative_z;
    this.positive_y = positive_y;
    this.negative_y = negative_y;
    this.positive_x = positive_x;
    this.negative_x = negative_x;
  }

  @Nonnull String getName()
  {
    return this.texture.getName();
  }

  public @Nonnull TextureCubeStatic getTexture()
  {
    return this.texture;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBTextureCube ");
    builder.append(this.path);
    builder.append(" ");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }
}
