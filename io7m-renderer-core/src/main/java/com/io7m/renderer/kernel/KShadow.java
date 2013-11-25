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
    private final int size_exponent;

    @SuppressWarnings("synthetic-access") private KShadowMappedBasic(
      final @Nonnull Integer light_id,
      final int size_exponent)
      throws ConstraintError
    {
      super(Type.SHADOW_MAPPED_BASIC, light_id);
      this.size_exponent =
        Constraints.constrainRange(
          size_exponent,
          1,
          Integer.MAX_VALUE,
          "Shadow size exponent");
    }

    @Override public boolean equals(
      final Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (!super.equals(obj)) {
        return false;
      }
      if (this.getClass() != obj.getClass()) {
        return false;
      }
      final KShadowMappedBasic other = (KShadowMappedBasic) obj;
      if (this.size_exponent != other.size_exponent) {
        return false;
      }
      return true;
    }

    public int getSizeExponent()
    {
      return this.size_exponent;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + this.size_exponent;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[KShadowMappedBasic size_exponent=");
      builder.append(this.size_exponent);
      builder.append("]");
      return builder.toString();
    }
  }

  static enum Type
  {
    SHADOW_MAPPED_BASIC("Mapped basic");

    private final @Nonnull String name;

    private Type(
      final @Nonnull String name)
    {
      this.name = name;
    }

    @Override public @Nonnull String toString()
    {
      return this.name;
    }
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KShadowMappedBasic
    newMappedBasic(
      final @Nonnull Integer light_id,
      final int size_exponent)
      throws ConstraintError
  {
    return new KShadowMappedBasic(light_id, size_exponent);
  }

  private final @Nonnull Integer light_id;
  private final @Nonnull Type    type;

  private KShadow(
    final @Nonnull Type type,
    final @Nonnull Integer light_id)
    throws ConstraintError
  {
    this.light_id = Constraints.constrainNotNull(light_id, "Light ID");
    this.type = Constraints.constrainNotNull(type, "Type");
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
    final KShadow other = (KShadow) obj;
    if (!this.light_id.equals(other.light_id)) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  public @Nonnull Integer getLightID()
  {
    return this.light_id;
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.light_id.hashCode();
    result = (prime * result) + this.type.hashCode();
    return result;
  }
}
