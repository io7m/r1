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

import javax.annotation.Nonnull;

import net.java.quickcheck.Generator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureType;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;

public final class Texture2DStaticFake implements Texture2DStaticUsable
{
  public static @Nonnull Texture2DStaticUsable getDefault()
  {
    try {
      final AreaInclusive in_area =
        new AreaInclusive(
          new RangeInclusive(0, 31),
          new RangeInclusive(0, 31));
      return new Texture2DStaticFake(
        1,
        false,
        in_area,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_NEAREST,
        "default",
        TextureType.TEXTURE_TYPE_RGBA_16_8BPP,
        TextureWrapS.TEXTURE_WRAP_REPEAT,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE);
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static Texture2DStaticUsable getDefaultWithName(
    final String name)
  {
    try {
      final AreaInclusive in_area =
        new AreaInclusive(
          new RangeInclusive(0, 31),
          new RangeInclusive(0, 31));
      return new Texture2DStaticFake(
        1,
        false,
        in_area,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_NEAREST,
        name,
        TextureType.TEXTURE_TYPE_RGBA_16_8BPP,
        TextureWrapS.TEXTURE_WRAP_REPEAT,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE);
    } catch (final ConstraintError x) {
      throw new UnreachableCodeException(x);
    }
  }

  private final @Nonnull AreaInclusive              area;

  private final boolean                             deleted;
  private final @Nonnull TextureFilterMagnification filter_mag;
  private final @Nonnull TextureFilterMinification  filter_min;
  private final int                                 gl_name;
  private final @Nonnull String                     name;
  private final long                                size;
  private final @Nonnull TextureType                type;
  private final @Nonnull TextureWrapS               wrap_s;
  private final @Nonnull TextureWrapT               wrap_t;

  private Texture2DStaticFake(
    final int gl_name1,
    final boolean deleted1,
    final @Nonnull AreaInclusive area1,
    final @Nonnull TextureFilterMagnification filter_mag1,
    final @Nonnull TextureFilterMinification filter_min1,
    final @Nonnull String name1,
    final @Nonnull TextureType type1,
    final @Nonnull TextureWrapS wrap_s1,
    final @Nonnull TextureWrapT wrap_t1)
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
    final Object obj)
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
    final Texture2DStaticFake other = (Texture2DStaticFake) obj;
    if (this.area == null) {
      if (other.area != null) {
        return false;
      }
    } else if (!this.area.equals(other.area)) {
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
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
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

  @Override public AreaInclusive getArea()
  {
    return this.area;
  }

  @Override public int getGLName()
  {
    return this.gl_name;
  }

  @Override public int getHeight()
  {
    return (int) this.area.getRangeY().getInterval();
  }

  @Override public TextureFilterMagnification getMagnificationFilter()
  {
    return this.filter_mag;
  }

  @Override public TextureFilterMinification getMinificationFilter()
  {
    return this.filter_min;
  }

  @Override public String getName()
  {
    return this.name;
  }

  @Override public RangeInclusive getRangeX()
  {
    return this.area.getRangeX();
  }

  @Override public RangeInclusive getRangeY()
  {
    return this.area.getRangeY();
  }

  @Override public TextureType getType()
  {
    return this.type;
  }

  @Override public int getWidth()
  {
    return (int) this.area.getRangeX().getInterval();
  }

  @Override public TextureWrapS getWrapS()
  {
    return this.wrap_s;
  }

  @Override public TextureWrapT getWrapT()
  {
    return this.wrap_t;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result) + ((this.area == null) ? 0 : this.area.hashCode());
    result = (prime * result) + (this.deleted ? 1231 : 1237);
    result =
      (prime * result)
        + ((this.filter_mag == null) ? 0 : this.filter_mag.hashCode());
    result =
      (prime * result)
        + ((this.filter_min == null) ? 0 : this.filter_min.hashCode());
    result = (prime * result) + this.gl_name;
    result =
      (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
    result = (prime * result) + (int) (this.size ^ (this.size >>> 32));
    result =
      (prime * result) + ((this.type == null) ? 0 : this.type.hashCode());
    result =
      (prime * result) + ((this.wrap_s == null) ? 0 : this.wrap_s.hashCode());
    result =
      (prime * result) + ((this.wrap_t == null) ? 0 : this.wrap_t.hashCode());
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

  public static Generator<Texture2DStaticUsable> generator(
    final @Nonnull Generator<String> name_gen)
  {
    return new Generator<Texture2DStaticUsable>() {
      @Override public Texture2DStaticUsable next()
      {
        return Texture2DStaticFake.getDefaultWithName(name_gen.next());
      }
    };
  }
}
