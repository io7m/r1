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

package com.io7m.renderer.examples;

import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.ProjectionMatrix;
import com.io7m.jcanephora.ViewMatrix;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3FType;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;

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
    final RVectorI3F<RSpaceWorldType> in_source,
    final RVectorI3F<RSpaceWorldType> in_target)
  {
    return new ExampleViewLookAt(in_source, in_target);
  }

  private final KCamera                     camera;
  private final RVectorI3F<RSpaceWorldType> source;
  private final RVectorI3F<RSpaceWorldType> target;

  private ExampleViewLookAt(
    final RVectorI3F<RSpaceWorldType> in_source,
    final RVectorI3F<RSpaceWorldType> in_target)
  {
    this.source = in_source;
    this.target = in_target;

    final MatrixM4x4F temp = new MatrixM4x4F();

    ViewMatrix.lookAt(
      temp,
      this.source,
      this.target,
      ExampleViewLookAt.Y_AXIS);

    final RMatrixI4x4F<RTransformViewType> view =
      RMatrixI4x4F.newFromReadable(temp);

    ProjectionMatrix.makePerspectiveProjection(
      temp,
      0.1f,
      100.0f,
      640.0 / 480.0,
      Math.toRadians(90.0));

    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.newFromReadable(temp);

    this.camera = KCamera.newCamera(view, projection);
  }

  @Override public KCamera getCamera()
  {
    return this.camera;
  }

  @Override public RVectorI3F<RSpaceWorldType> viewGetSource()
  {
    return this.source;
  }

  @Override public RVectorI3F<RSpaceWorldType> viewGetTarget()
  {
    return this.target;
  }
}
