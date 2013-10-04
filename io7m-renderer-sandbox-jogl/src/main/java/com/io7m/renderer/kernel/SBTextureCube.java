package com.io7m.renderer.kernel;

import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import com.io7m.jcanephora.TextureCubeStatic;

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

  public @Nonnull SBTextureCubeDescription getDescription()
  {
    return this.description;
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

  private SBTextureCube(
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
    builder.append(this.description);
    builder.append(" ");
    builder.append(this.texture);
    builder.append(" ");
    builder.append(this.positive_z);
    builder.append(" ");
    builder.append(this.negative_z);
    builder.append(" ");
    builder.append(this.positive_y);
    builder.append(" ");
    builder.append(this.negative_y);
    builder.append(" ");
    builder.append(this.positive_x);
    builder.append(" ");
    builder.append(this.negative_x);
    builder.append("]");
    return builder.toString();
  }
}
