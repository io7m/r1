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

package com.io7m.r1.examples;

import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.ViewMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.types.KCamera;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceWorldType;

/**
 * Look at a target from a source.
 */

@Immutable public final class ExampleViewLookAt implements
  ExampleViewLookAtType
{
  private static final VectorReadable3FType Y_AXIS;

  static {
    Y_AXIS = new VectorI3F(0.0f, 1.0f, 0.0f);
  }

  /**
   * Look at a target from a source.
   *
   * @param in_source
   *          The source
   * @param in_target
   *          The target
   * @return The resulting view
   */

  public static ExampleViewLookAtType lookAt(
    final PVectorI3F<RSpaceWorldType> in_source,
    final PVectorI3F<RSpaceWorldType> in_target)
  {
    return new ExampleViewLookAt(in_source, in_target);
  }

  private final KCamera                     camera;
  private final PVectorI3F<RSpaceWorldType> source;
  private final PVectorI3F<RSpaceWorldType> target;

  private ExampleViewLookAt(
    final PVectorI3F<RSpaceWorldType> in_source,
    final PVectorI3F<RSpaceWorldType> in_target)
  {
    this.source = in_source;
    this.target = in_target;

    final MatrixM4x4F temp = new MatrixM4x4F();
    ViewMatrix.lookAt(
      temp,
      this.source,
      this.target,
      ExampleViewLookAt.Y_AXIS);

    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view =
      PMatrixI4x4F.newFromReadableUntyped(temp);

    final KProjectionType projection =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90),
        640.0f / 480.0f,
        0.01f,
        100.0f);

    this.camera = KCamera.newCamera(view, projection);
  }

  @Override public KCamera getCamera()
  {
    return this.camera;
  }

  @Override public PVectorI3F<RSpaceWorldType> viewGetSource()
  {
    return this.source;
  }

  @Override public PVectorI3F<RSpaceWorldType> viewGetTarget()
  {
    return this.target;
  }
}
