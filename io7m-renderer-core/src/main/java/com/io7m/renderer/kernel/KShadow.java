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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable abstract class KShadow
{
  @Immutable final static class KShadowMappedBasic extends KShadow
  {
    public static @Nonnull KShadowMappedBasic make(
      final int size)
      throws ConstraintError
    {
      return new KShadowMappedBasic(size);
    }

    private final int size;

    @SuppressWarnings("synthetic-access") private KShadowMappedBasic(
      final int size)
      throws ConstraintError
    {
      super(Type.SHADOW_MAPPED_BASIC);
      this.size =
        Constraints.constrainRange(
          size,
          2,
          Integer.MAX_VALUE,
          "Shadow map size");
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
      final KShadowMappedBasic other = (KShadowMappedBasic) obj;
      if (this.size != other.size) {
        return false;
      }
      return true;
    }

    public int getSize()
    {
      return this.size;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.size;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic size=");
      builder.append(this.size);
      builder.append("]");
      return builder.toString();
    }
  }

  static enum Type
  {
    SHADOW_MAPPED_BASIC
  }

  private final @Nonnull Type type;

  private KShadow(
    final @Nonnull Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }
}
