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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionM4F;

/**
 * Preallocated storage to allow various function to execute without
 * allocating.
 */

@NotThreadSafe final class KTransformContext
{
  final @Nonnull MatrixM3x3F         t_matrix3x3;
  final @Nonnull MatrixM3x3F.Context t_matrix3x3_context;
  final @Nonnull MatrixM4x4F         t_matrix4x4;
  final @Nonnull MatrixM4x4F.Context t_matrix4x4_context;
  final @Nonnull QuaternionM4F       t_rotation;

  KTransformContext()
  {
    this.t_rotation = new QuaternionM4F();
    this.t_matrix4x4 = new MatrixM4x4F();
    this.t_matrix4x4_context = new MatrixM4x4F.Context();
    this.t_matrix3x3 = new MatrixM3x3F();
    this.t_matrix3x3_context = new MatrixM3x3F.Context();
  }
}
