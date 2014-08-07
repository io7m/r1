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

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jvvfs.PathVirtual;

public final class SBTexture2D<K extends SBTexture2DKind>
{
  private final SBTexture2DDescription description;
  private final Texture2DStaticType    texture;
  private final BufferedImage          image;

  public SBTexture2D(
    final SBTexture2DDescription in_description,
    final Texture2DStaticType in_texture,
    final BufferedImage in_image)
  {
    this.description = in_description;
    this.texture = in_texture;
    this.image = in_image;
  }

  public SBTexture2DDescription getDescription()
  {
    return this.description;
  }

  public BufferedImage getImage()
  {
    return this.image;
  }

  public PathVirtual getPath()
  {
    return this.description.getPath();
  }

  public Texture2DStaticType getTexture()
  {
    return this.texture;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBTexture2D ");
    builder.append(this.texture);
    builder.append("]");
    final String s = builder.toString();
    assert s != null;
    return s;
  }
}
