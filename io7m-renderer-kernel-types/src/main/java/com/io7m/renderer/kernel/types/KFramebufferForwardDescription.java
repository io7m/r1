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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * A description of a framebuffer suitable for forward rendering and sampling
 * of the depth buffer.
 */

@Immutable public final class KFramebufferForwardDescription implements
  KFramebufferDescriptionType
{
  /**
   * Create a new description of a forward-rendering framebuffer.
   * 
   * @param in_rgba_description
   *          The description of the colour buffer
   * @param in_depth_description
   *          The description of the depth buffer
   * @return A new description of a forward-rendering framebuffer
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KFramebufferForwardDescription newDescription(
    final @Nonnull KFramebufferRGBADescription in_rgba_description,
    final @Nonnull KFramebufferDepthDescription in_depth_description)
    throws ConstraintError
  {
    return new KFramebufferForwardDescription(
      in_rgba_description,
      in_depth_description);
  }

  private final @Nonnull KFramebufferDepthDescription depth_description;
  private final @Nonnull KFramebufferRGBADescription  rgba_description;

  private KFramebufferForwardDescription(
    final @Nonnull KFramebufferRGBADescription in_rgba_description,
    final @Nonnull KFramebufferDepthDescription in_depth_description)
    throws ConstraintError
  {
    this.rgba_description =
      Constraints.constrainNotNull(in_rgba_description, "RGBA description");
    this.depth_description =
      Constraints.constrainNotNull(in_depth_description, "Depth description");
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
    final KFramebufferForwardDescription other =
      (KFramebufferForwardDescription) obj;
    if (!this.depth_description.equals(other.depth_description)) {
      return false;
    }
    if (!this.rgba_description.equals(other.rgba_description)) {
      return false;
    }
    return true;
  }

  /**
   * @return The description of the depth buffer
   */

  public @Nonnull KFramebufferDepthDescription getDepthDescription()
  {
    return this.depth_description;
  }

  /**
   * @return The description of the colour buffer
   */

  public @Nonnull KFramebufferRGBADescription getRGBADescription()
  {
    return this.rgba_description;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.depth_description.hashCode();
    result = (prime * result) + this.rgba_description.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KFramebufferForwardDescription depth_description=");
    builder.append(this.depth_description);
    builder.append(" rgba_description=");
    builder.append(this.rgba_description);
    builder.append("]");
    return builder.toString();
  }
}
