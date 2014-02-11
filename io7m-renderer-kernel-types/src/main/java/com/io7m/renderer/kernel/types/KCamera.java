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
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RTransformView;

/**
 * An orientable "camera" with a specific projection.
 */

@Immutable public final class KCamera
{
  /**
   * Construct a new camera with the given view and projection.
   * 
   * @param view
   *          The world-to-eye matrix.
   * @param projection
   *          The eye-to-clip matrix.
   * @return A new camera
   * @throws ConstraintError
   *           If any parameters are <code>null</code>
   */

  public static @Nonnull KCamera newCamera(
    final @Nonnull RMatrixI4x4F<RTransformView> view,
    final @Nonnull RMatrixI4x4F<RTransformProjection> projection)
    throws ConstraintError
  {
    return new KCamera(
      Constraints.constrainNotNull(view, "View matrix"),
      Constraints.constrainNotNull(projection, "Projection matrix"));
  }

  private final @Nonnull RMatrixI4x4F<RTransformProjection> projection;
  private final @Nonnull RMatrixI4x4F<RTransformView>       view;

  private KCamera(
    final @Nonnull RMatrixI4x4F<RTransformView> in_view,
    final @Nonnull RMatrixI4x4F<RTransformProjection> in_projection)
  {
    this.view = in_view;
    this.projection = in_projection;
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
    final KCamera other = (KCamera) obj;
    if (!this.projection.equals(other.projection)) {
      return false;
    }
    if (!this.view.equals(other.view)) {
      return false;
    }
    return true;
  }

  /**
   * @return The eye-to-clip projection matrix
   */

  public @Nonnull RMatrixI4x4F<RTransformProjection> getProjectionMatrix()
  {
    return this.projection;
  }

  /**
   * @return The world-to-eye view matrix
   */

  public @Nonnull RMatrixI4x4F<RTransformView> getViewMatrix()
  {
    return this.view;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.projection.hashCode();
    result = (prime * result) + this.view.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KCamera [view ");
    builder.append(this.view);
    builder.append("] [projection ");
    builder.append(this.projection);
    builder.append("]]");
    return builder.toString();
  }
}
