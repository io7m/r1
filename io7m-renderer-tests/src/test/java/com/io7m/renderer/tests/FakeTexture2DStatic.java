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

package com.io7m.renderer.tests;

import net.java.quickcheck.Generator;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;

public final class FakeTexture2DStatic implements Texture2DStaticType
{
  public static Texture2DStaticType getDefault()
  {
    final AreaInclusive in_area =
      new AreaInclusive(
        new RangeInclusiveL(0, 31),
        new RangeInclusiveL(0, 31));
    return new FakeTexture2DStatic(
      1,
      false,
      in_area,
      TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
      TextureFilterMinification.TEXTURE_FILTER_NEAREST,
      "default",
      TextureFormat.TEXTURE_FORMAT_RGBA_16_8BPP,
      TextureWrapS.TEXTURE_WRAP_REPEAT,
      TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE);
  }

  public static Texture2DStaticType getDefaultWithName(
    final String name)
  {
    final AreaInclusive in_area =
      new AreaInclusive(
        new RangeInclusiveL(0, 31),
        new RangeInclusiveL(0, 31));
    return new FakeTexture2DStatic(
      1,
      false,
      in_area,
      TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
      TextureFilterMinification.TEXTURE_FILTER_NEAREST,
      name,
      TextureFormat.TEXTURE_FORMAT_RGBA_16_8BPP,
      TextureWrapS.TEXTURE_WRAP_REPEAT,
      TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE);
  }

  private final AreaInclusive              area;
  private final boolean                    deleted;
  private final TextureFilterMagnification filter_mag;
  private final TextureFilterMinification  filter_min;
  private final int                        gl_name;
  private final String                     name;
  private final long                       size;
  private final TextureFormat              type;
  private final TextureWrapS               wrap_s;
  private final TextureWrapT               wrap_t;

  private FakeTexture2DStatic(
    final int gl_name1,
    final boolean deleted1,
    final AreaInclusive area1,
    final TextureFilterMagnification filter_mag1,
    final TextureFilterMinification filter_min1,
    final String name1,
    final TextureFormat type1,
    final TextureWrapS wrap_s1,
    final TextureWrapT wrap_t1)
  {
    this.gl_name = gl_name1;
    this.deleted = deleted1;
    this.area = area1;
    this.filter_mag = filter_mag1;
    this.filter_min = filter_min1;
    this.name = name1;
    this.type = type1;
    this.wrap_s = wrap_s1;
    this.wrap_t = wrap_t1;
    this.size =
      (area1.getRangeX().getInterval() * type1.getBytesPerPixel())
        * this.area.getRangeY().getInterval();
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final FakeTexture2DStatic other = (FakeTexture2DStatic) obj;
    if (!this.area.equals(other.area)) {
      return false;
    }
    if (this.deleted != other.deleted) {
      return false;
    }
    if (this.filter_mag != other.filter_mag) {
      return false;
    }
    if (this.filter_min != other.filter_min) {
      return false;
    }
    if (this.gl_name != other.gl_name) {
      return false;
    }
    if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.size != other.size) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    if (this.wrap_s != other.wrap_s) {
      return false;
    }
    if (this.wrap_t != other.wrap_t) {
      return false;
    }
    return true;
  }

  @Override public AreaInclusive textureGetArea()
  {
    return this.area;
  }

  @Override public int getGLName()
  {
    return this.gl_name;
  }

  @Override public int textureGetHeight()
  {
    return (int) this.area.getRangeY().getInterval();
  }

  @Override public TextureFilterMagnification textureGetMagnificationFilter()
  {
    return this.filter_mag;
  }

  @Override public TextureFilterMinification textureGetMinificationFilter()
  {
    return this.filter_min;
  }

  @Override public String textureGetName()
  {
    return this.name;
  }

  @Override public RangeInclusiveL textureGetRangeX()
  {
    return this.area.getRangeX();
  }

  @Override public RangeInclusiveL textureGetRangeY()
  {
    return this.area.getRangeY();
  }

  @Override public TextureFormat textureGetFormat()
  {
    return this.type;
  }

  @Override public int textureGetWidth()
  {
    return (int) this.area.getRangeX().getInterval();
  }

  @Override public TextureWrapS textureGetWrapS()
  {
    return this.wrap_s;
  }

  @Override public TextureWrapT textureGetWrapT()
  {
    return this.wrap_t;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.area.hashCode();
    result = (prime * result) + (this.deleted ? 1231 : 1237);
    result = (prime * result) + this.filter_mag.hashCode();
    result = (prime * result) + this.filter_min.hashCode();
    result = (prime * result) + this.gl_name;
    result = (prime * result) + this.name.hashCode();
    result = (prime * result) + (int) (this.size ^ (this.size >>> 32));
    result = (prime * result) + this.type.hashCode();
    result = (prime * result) + this.wrap_s.hashCode();
    result = (prime * result) + this.wrap_t.hashCode();
    return result;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.size;
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }

  public static Generator<Texture2DStaticUsableType> generator(
    final Generator<String> name_gen)
  {
    return new Generator<Texture2DStaticUsableType>() {
      @Override public Texture2DStaticUsableType next()
      {
        return FakeTexture2DStatic.getDefaultWithName(name_gen.next());
      }
    };
  }
}
