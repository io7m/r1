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

package com.io7m.renderer.kernel.sandbox;

import java.awt.image.BufferedImage;

import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jvvfs.PathVirtual;

public final class SBTextureCube
{
  private final SBTextureCubeDescription description;
  private final TextureCubeStaticType    texture;
  private final BufferedImage            positive_z;
  private final BufferedImage            negative_z;
  private final BufferedImage            positive_y;
  private final BufferedImage            negative_y;
  private final BufferedImage            positive_x;
  private final BufferedImage            negative_x;

  SBTextureCube(
    final SBTextureCubeDescription in_description,
    final TextureCubeStaticType in_texture,
    final BufferedImage in_positive_z,
    final BufferedImage in_negative_z,
    final BufferedImage in_positive_y,
    final BufferedImage in_negative_y,
    final BufferedImage in_positive_x,
    final BufferedImage in_negative_x)
  {
    this.description = in_description;
    this.texture = in_texture;
    this.positive_z = in_positive_z;
    this.negative_z = in_negative_z;
    this.positive_y = in_positive_y;
    this.negative_y = in_negative_y;
    this.positive_x = in_positive_x;
    this.negative_x = in_negative_x;
  }

  public SBTextureCubeDescription getDescription()
  {
    return this.description;
  }

  String getName()
  {
    return this.texture.textureGetName();
  }

  public BufferedImage getNegativeX()
  {
    return this.negative_x;
  }

  public BufferedImage getNegativeY()
  {
    return this.negative_y;
  }

  public BufferedImage getNegativeZ()
  {
    return this.negative_z;
  }

  public PathVirtual getPath()
  {
    return this.description.getPath();
  }

  public BufferedImage getPositiveX()
  {
    return this.positive_x;
  }

  public BufferedImage getPositiveY()
  {
    return this.positive_y;
  }

  public BufferedImage getPositiveZ()
  {
    return this.positive_z;
  }

  public TextureCubeStaticType getTexture()
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
    final String s = builder.toString();
    assert s != null;
    return s;
  }
}
