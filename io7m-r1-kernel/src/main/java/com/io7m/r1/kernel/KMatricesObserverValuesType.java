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

package com.io7m.r1.kernel;

import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RMatrixReadable4x4FType;
import com.io7m.r1.types.RTransformProjectionInverseType;
import com.io7m.r1.types.RTransformProjectionType;
import com.io7m.r1.types.RTransformViewInverseType;
import com.io7m.r1.types.RTransformViewType;

/**
 * Access to the matrices for a given observer.
 */

public interface KMatricesObserverValuesType
{
  /**
   * @return The matrix context
   */

  MatrixM4x4F.Context getMatrixContext();

  /**
   * @return The current projection matrix for an observer
   */

  RMatrixReadable4x4FType<RTransformProjectionType> getMatrixProjection();

  /**
   * @return The current inverse projection matrix for an observer
   */

    RMatrixReadable4x4FType<RTransformProjectionInverseType>
    getMatrixProjectionInverse();

  /**
   * @return The current view matrix for an observer
   */

  RMatrixReadable4x4FType<RTransformViewType> getMatrixView();

  /**
   * @return The current inverse view matrix for an observer
   */

  RMatrixReadable4x4FType<RTransformViewInverseType> getMatrixViewInverse();

  /**
   * @return The current projection for the observer
   */

  KProjectionType getProjection();
}
