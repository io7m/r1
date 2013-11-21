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

@SuppressWarnings("synthetic-access") @Immutable public abstract class SBLightShadowDescription
{
  @Immutable final static class SBLightShadowMappedBasicDescription extends
    SBLightShadowDescription
  {
    private final int size;

    private SBLightShadowMappedBasicDescription(
      final int size)
      throws ConstraintError
    {
      super(KShadow.Type.SHADOW_MAPPED_BASIC);
      this.size = Constraints.constrainRange(size, 1, 10, "Shadow map size");
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
      final SBLightShadowMappedBasicDescription other =
        (SBLightShadowMappedBasicDescription) obj;
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
      int result = super.hashCode();
      result = (prime * result) + this.size;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SBLightShadowMappedBasicDescription size=");
      builder.append(this.size);
      builder.append("]");
      return builder.toString();
    }
  }

  public static @Nonnull
    SBLightShadowMappedBasicDescription
    newShadowMappedBasic(
      final int size)
      throws ConstraintError
  {
    return new SBLightShadowMappedBasicDescription(size);
  }

  private final @Nonnull KShadow.Type type;

  private SBLightShadowDescription(
    final @Nonnull KShadow.Type type)
    throws ConstraintError
  {
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
    final SBLightShadowDescription other = (SBLightShadowDescription) obj;
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  public @Nonnull KShadow.Type getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.type.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBLightShadowDescription type=");
    builder.append(this.type);
    builder.append("]");
    return builder.toString();
  }
}
