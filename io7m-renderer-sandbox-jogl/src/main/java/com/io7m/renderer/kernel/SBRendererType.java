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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

abstract class SBRendererType
{
  public final static class SBRendererTypeKernel extends SBRendererType
  {
    private final @Nonnull SBKRendererType renderer;

    public @Nonnull SBKRendererType getRenderer()
    {
      return this.renderer;
    }

    @SuppressWarnings("synthetic-access") public SBRendererTypeKernel(
      final @Nonnull SBKRendererType renderer)
      throws ConstraintError
    {
      super(Type.TYPE_KERNEL);
      this.renderer = Constraints.constrainNotNull(renderer, "Renderer");
    }
  }

  public final static class SBRendererTypeSpecific extends SBRendererType
  {
    @SuppressWarnings("synthetic-access") public SBRendererTypeSpecific()
      throws ConstraintError
    {
      super(Type.TYPE_SPECIFIC);
    }
  }

  public static enum Type
  {
    TYPE_KERNEL,
    TYPE_SPECIFIC
  }

  private final @Nonnull Type type;

  public @Nonnull Type getType()
  {
    return this.type;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * this.type.hashCode();
    return result;
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
    final SBRendererType other = (SBRendererType) obj;
    if (this.type != other.type) {
      return false;
    }
    return true;
  }

  private SBRendererType(
    final @Nonnull Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }
}
