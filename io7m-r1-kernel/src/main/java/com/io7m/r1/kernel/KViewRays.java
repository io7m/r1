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

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorM4F;
import com.io7m.r1.types.RMatrixReadable4x4FType;
import com.io7m.r1.types.RTransformProjectionInverseType;

/**
 * View rays used to reconstruct eye-space positions during deferred
 * rendering.
 */

public final class KViewRays
{
  private static final VectorI4F FAR_X0Y0;
  private static final VectorI4F FAR_X0Y1;
  private static final VectorI4F FAR_X1Y0;
  private static final VectorI4F FAR_X1Y1;
  private static final VectorI4F NEAR_X0Y0;
  private static final VectorI4F NEAR_X0Y1;
  private static final VectorI4F NEAR_X1Y0;
  private static final VectorI4F NEAR_X1Y1;

  static {
    NEAR_X0Y0 = new VectorI4F(-1.0f, -1.0f, -1.0f, 1.0f);
    NEAR_X1Y0 = new VectorI4F(1.0f, -1.0f, -1.0f, 1.0f);
    NEAR_X0Y1 = new VectorI4F(-1.0f, 1.0f, -1.0f, 1.0f);
    NEAR_X1Y1 = new VectorI4F(1.0f, 1.0f, -1.0f, 1.0f);

    FAR_X0Y0 = new VectorI4F(-1.0f, -1.0f, 1.0f, 1.0f);
    FAR_X1Y0 = new VectorI4F(1.0f, -1.0f, 1.0f, 1.0f);
    FAR_X0Y1 = new VectorI4F(-1.0f, 1.0f, 1.0f, 1.0f);
    FAR_X1Y1 = new VectorI4F(1.0f, 1.0f, 1.0f, 1.0f);
  }

  private static void calculateRayAndOrigin(
    final Context c,
    final RMatrixReadable4x4FType<RTransformProjectionInverseType> m,
    final VectorI4F near,
    final VectorI4F far,
    final VectorM4F temp_near,
    final VectorM4F temp_far,
    final VectorM4F out_ray,
    final VectorM4F out_origin)
  {
    MatrixM4x4F.multiplyVector4FWithContext(c, m, near, temp_near);
    MatrixM4x4F.multiplyVector4FWithContext(c, m, far, temp_far);
    VectorM4F.scaleInPlace(temp_near, 1.0 / temp_near.getWF());
    VectorM4F.scaleInPlace(temp_far, 1.0 / temp_far.getWF());
    VectorM4F.subtract(temp_far, temp_near, out_ray);
    VectorM4F.scaleInPlace(out_ray, 1.0 / out_ray.getZF());
    VectorM4F.subtract(
      temp_near,
      VectorI4F.scale(out_ray, temp_near.getZF()),
      out_origin);
  }

  /**
   * Construct view-space vectors for the given projection matrix.
   *
   * @param c
   *          Pre-allocated storage for matrix multiplications.
   * @param m
   *          A projection matrix.
   * @return View rays.
   */

  public static KViewRays newRays(
    final Context c,
    final RMatrixReadable4x4FType<RTransformProjectionInverseType> m)
  {
    final VectorM4F temp_near = new VectorM4F();
    final VectorM4F temp_far = new VectorM4F();
    final VectorM4F out_ray = new VectorM4F();
    final VectorM4F out_origin = new VectorM4F();

    KViewRays.calculateRayAndOrigin(
      c,
      m,
      KViewRays.NEAR_X0Y0,
      KViewRays.FAR_X0Y0,
      temp_near,
      temp_far,
      out_ray,
      out_origin);
    final VectorI4F r_x0y0_origin = new VectorI4F(out_origin);
    final VectorI4F r_x0y0_ray = new VectorI4F(out_ray);

    KViewRays.calculateRayAndOrigin(
      c,
      m,
      KViewRays.NEAR_X1Y0,
      KViewRays.FAR_X1Y0,
      temp_near,
      temp_far,
      out_ray,
      out_origin);
    final VectorI4F r_x1y0_origin = new VectorI4F(out_origin);
    final VectorI4F r_x1y0_ray = new VectorI4F(out_ray);

    KViewRays.calculateRayAndOrigin(
      c,
      m,
      KViewRays.NEAR_X0Y1,
      KViewRays.FAR_X0Y1,
      temp_near,
      temp_far,
      out_ray,
      out_origin);
    final VectorI4F r_x0y1_origin = new VectorI4F(out_origin);
    final VectorI4F r_x0y1_ray = new VectorI4F(out_ray);

    KViewRays.calculateRayAndOrigin(
      c,
      m,
      KViewRays.NEAR_X1Y1,
      KViewRays.FAR_X1Y1,
      temp_near,
      temp_far,
      out_ray,
      out_origin);
    final VectorI4F r_x1y1_origin = new VectorI4F(out_origin);
    final VectorI4F r_x1y1_ray = new VectorI4F(out_ray);

    return new KViewRays(
      r_x0y0_origin,
      r_x1y0_origin,
      r_x0y1_origin,
      r_x1y1_origin,
      r_x0y0_ray,
      r_x1y0_ray,
      r_x0y1_ray,
      r_x1y1_ray);
  }

  private final VectorI4F origin_x0y0;
  private final VectorI4F origin_x0y1;
  private final VectorI4F origin_x1y0;
  private final VectorI4F origin_x1y1;
  private final VectorI4F ray_x0y0;
  private final VectorI4F ray_x0y1;
  private final VectorI4F ray_x1y0;
  private final VectorI4F ray_x1y1;

  private KViewRays(
    final VectorI4F in_origin_x0y0,
    final VectorI4F in_origin_x1y0,
    final VectorI4F in_origin_x0y1,
    final VectorI4F in_origin_x1y1,
    final VectorI4F in_ray_x0y0,
    final VectorI4F in_ray_x1y0,
    final VectorI4F in_ray_x0y1,
    final VectorI4F in_ray_x1y1)
  {
    this.origin_x0y0 = NullCheck.notNull(in_origin_x0y0, "Origin");
    this.origin_x1y0 = NullCheck.notNull(in_origin_x1y0, "Origin");
    this.origin_x0y1 = NullCheck.notNull(in_origin_x0y1, "Origin");
    this.origin_x1y1 = NullCheck.notNull(in_origin_x1y1, "Origin");
    this.ray_x0y0 = NullCheck.notNull(in_ray_x0y0, "Ray");
    this.ray_x1y0 = NullCheck.notNull(in_ray_x1y0, "Ray");
    this.ray_x0y1 = NullCheck.notNull(in_ray_x0y1, "Ray");
    this.ray_x1y1 = NullCheck.notNull(in_ray_x1y1, "Ray");
  }

  /**
   * @return The x0y0 origin
   */

  public VectorI4F getOriginX0Y0()
  {
    return this.origin_x0y0;
  }

  /**
   * @return The x0y1 origin
   */

  public VectorI4F getOriginX0Y1()
  {
    return this.origin_x0y1;
  }

  /**
   * @return The x1y0 origin
   */

  public VectorI4F getOriginX1Y0()
  {
    return this.origin_x1y0;
  }

  /**
   * @return The x1y1 origin
   */

  public VectorI4F getOriginX1Y1()
  {
    return this.origin_x1y1;
  }

  /**
   * @return The x0y0 view ray
   */

  public VectorI4F getRayX0Y0()
  {
    return this.ray_x0y0;
  }

  /**
   * @return The x0y1 view ray
   */

  public VectorI4F getRayX0Y1()
  {
    return this.ray_x0y1;
  }

  /**
   * @return The x1y0 view ray
   */

  public VectorI4F getRayX1Y0()
  {
    return this.ray_x1y0;
  }

  /**
   * @return The x1y1 view ray
   */

  public VectorI4F getRayX1Y1()
  {
    return this.ray_x1y1;
  }
}
