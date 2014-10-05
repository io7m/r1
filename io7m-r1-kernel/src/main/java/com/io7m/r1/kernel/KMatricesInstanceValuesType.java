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

import com.io7m.r1.types.RMatrixM3x3F;
import com.io7m.r1.types.RMatrixReadable3x3FType;
import com.io7m.r1.types.RMatrixReadable4x4FType;
import com.io7m.r1.types.RTransformModelType;
import com.io7m.r1.types.RTransformModelViewType;
import com.io7m.r1.types.RTransformNormalType;
import com.io7m.r1.types.RTransformTextureType;

/**
 * The type of instance values.
 */

public interface KMatricesInstanceValuesType extends
  KMatricesObserverValuesType
{
  /**
   * @return The current model matrix for the instance
   */

  RMatrixReadable4x4FType<RTransformModelType> getMatrixModel();

  /**
   * @return The current model-view matrix for the instance
   */

  RMatrixReadable4x4FType<RTransformModelViewType> getMatrixModelView();

  /**
   * @return The current normal matrix for the instance
   */

  RMatrixReadable3x3FType<RTransformNormalType> getMatrixNormal();

  /**
   * @return The current UV matrix for the instance
   */

  RMatrixM3x3F<RTransformTextureType> getMatrixUV();

}
