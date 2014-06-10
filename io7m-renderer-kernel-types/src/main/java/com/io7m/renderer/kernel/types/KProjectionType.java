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

import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;

/**
 * The type of projections.
 */

public interface KProjectionType
{
  /**
   * @return The resulting projection matrix.
   */

  RMatrixI4x4F<RTransformProjectionType> projectionGetMatrix();

  /**
   * @return The leftmost edge of the frustum's near plane.
   */

  float projectionGetXMinimum();

  /**
   * @return The rightmost edge of the frustum's near plane.
   */

  float projectionGetXMaximum();

  /**
   * @return The topmost edge of the frustum's near plane.
   */

  float projectionGetYMaximum();

  /**
   * @return The bottommost edge of the frustum's near plane.
   */

  float projectionGetYMinimum();

  /**
   * @return The value of the frustum's near plane.
   */

  float projectionGetZNear();

  /**
   * @return The value of the frustum's near plane.
   */

  float projectionGetZFar();
}
