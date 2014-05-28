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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A description of a framebuffer suitable for forward rendering and sampling
 * of the depth buffer.
 */

@EqualityStructural public final class KFramebufferForwardDescription implements
  KFramebufferDescriptionType
{
  /**
   * Create a new description of a forward-rendering framebuffer.
   * 
   * @param in_rgba_description
   *          The description of the color buffer
   * @param in_depth_description
   *          The description of the depth buffer
   * @return A new description of a forward-rendering framebuffer
   */

  public static KFramebufferForwardDescription newDescription(
    final KFramebufferRGBADescription in_rgba_description,
    final KFramebufferDepthDescription in_depth_description)
  {
    return new KFramebufferForwardDescription(
      in_rgba_description,
      in_depth_description);
  }

  private final KFramebufferDepthDescription depth_description;
  private final KFramebufferRGBADescription  rgba_description;

  private KFramebufferForwardDescription(
    final KFramebufferRGBADescription in_rgba_description,
    final KFramebufferDepthDescription in_depth_description)
  {
    this.rgba_description =
      NullCheck.notNull(in_rgba_description, "RGBA description");
    this.depth_description =
      NullCheck.notNull(in_depth_description, "Depth description");
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

  public KFramebufferDepthDescription getDepthDescription()
  {
    return this.depth_description;
  }

  /**
   * @return The description of the color buffer
   */

  public KFramebufferRGBADescription getRGBADescription()
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
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
