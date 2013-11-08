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
  private final @Nonnull SBTextureCubeDescription description;
  private final @Nonnull TextureCubeStatic        texture;
  private final @Nonnull BufferedImage            positive_z;
  private final @Nonnull BufferedImage            negative_z;
  private final @Nonnull BufferedImage            positive_y;
  private final @Nonnull BufferedImage            negative_y;
  private final @Nonnull BufferedImage            positive_x;
  private final @Nonnull BufferedImage            negative_x;

  SBTextureCube(
    final @Nonnull SBTextureCubeDescription description,
    final @Nonnull TextureCubeStatic texture,
    final @Nonnull BufferedImage positive_z,
    final @Nonnull BufferedImage negative_z,
    final @Nonnull BufferedImage positive_y,
    final @Nonnull BufferedImage negative_y,
    final @Nonnull BufferedImage positive_x,
    final @Nonnull BufferedImage negative_x)
  {
    this.description = description;
    this.texture = texture;
    this.positive_z = positive_z;
    this.negative_z = negative_z;
    this.positive_y = positive_y;
    this.negative_y = negative_y;
    this.positive_x = positive_x;
    this.negative_x = negative_x;
  }

  public @Nonnull SBTextureCubeDescription getDescription()
  {
    return this.description;
  }

  @Nonnull String getName()
  {
    return this.texture.getName();
  }

  public @Nonnull BufferedImage getNegativeX()
  {
    return this.negative_x;
  }

  public @Nonnull BufferedImage getNegativeY()
  {
    return this.negative_y;
  }

  public @Nonnull BufferedImage getNegativeZ()
  {
    return this.negative_z;
  }

  public @Nonnull PathVirtual getPath()
  {
    return this.description.getPath();
  }

  public @Nonnull BufferedImage getPositiveX()
  {
    return this.positive_x;
  }

  public @Nonnull BufferedImage getPositiveY()
  {
    return this.positive_y;
  }

  public @Nonnull BufferedImage getPositiveZ()
  {
    return this.positive_z;
  }

  public @Nonnull TextureCubeStatic getTexture()
  {
    return this.texture;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBTextureCube [description=");
    builder.append(this.description);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append(", positive_z=");
    builder.append(this.positive_z);
    builder.append(", negative_z=");
    builder.append(this.negative_z);
    builder.append(", positive_y=");
    builder.append(this.positive_y);
    builder.append(", negative_y=");
    builder.append(this.negative_y);
    builder.append(", positive_x=");
    builder.append(this.positive_x);
    builder.append(", negative_x=");
    builder.append(this.negative_x);
    builder.append("]");
    return builder.toString();
  }
}
