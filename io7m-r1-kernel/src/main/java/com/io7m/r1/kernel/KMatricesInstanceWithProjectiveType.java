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

import com.io7m.jtensors.parameterized.PMatrixDirectReadable4x4FType;
import com.io7m.r1.spaces.RSpaceLightEyeType;
import com.io7m.r1.spaces.RSpaceObjectType;

/**
 * The matrices available within the context of a transformed instance,
 * observed by a projective observer.
 */

public interface KMatricesInstanceWithProjectiveType extends
  KMatricesInstanceValuesType,
  KMatricesProjectiveLightValuesType
{
  /**
   * @return The current modelview matrix for a projective observer
   */

    PMatrixDirectReadable4x4FType<RSpaceObjectType, RSpaceLightEyeType>
    getMatrixProjectiveModelView();
}
