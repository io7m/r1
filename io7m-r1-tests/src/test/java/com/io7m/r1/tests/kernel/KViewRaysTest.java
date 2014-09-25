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

package com.io7m.r1.tests.kernel;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jequality.AlmostEqualFloat.ContextRelative;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.MatrixM4x4F.Context;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorI4F;
import com.io7m.r1.kernel.KBilinear;
import com.io7m.r1.kernel.KViewRays;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionOrthographic;
import com.io7m.r1.types.RMatrixM4x4F;
import com.io7m.r1.types.RTransformProjectionInverseType;

@SuppressWarnings("static-method") public final class KViewRaysTest
{
  private static void dumpViewRays(
    final KViewRays vr)
  {
    System.out.println("origin_X0Y0: " + vr.getOriginX0Y0());
    System.out.println("origin_X1Y0: " + vr.getOriginX1Y0());
    System.out.println("origin_X0Y1: " + vr.getOriginX0Y1());
    System.out.println("origin_X1Y1: " + vr.getOriginX1Y1());
    System.out.println("ray_X0Y0: " + vr.getRayX0Y0());
    System.out.println("ray_X1Y0: " + vr.getRayX1Y0());
    System.out.println("ray_X0Y1: " + vr.getRayX0Y1());
    System.out.println("ray_X1Y1: " + vr.getRayX1Y1());
  }

  @Test public void testOrthographic0()
  {
    final Context c = new MatrixM4x4F.Context();
    final MatrixM4x4F t = new MatrixM4x4F();

    final KProjectionOrthographic p =
      KProjectionOrthographic.newProjection(
        t,
        -320.0f,
        320.0f,
        -240.0f,
        240.0f,
        1.0f,
        100.0f);

    final RMatrixM4x4F<RTransformProjectionInverseType> ipm =
      new RMatrixM4x4F<RTransformProjectionInverseType>();
    p.projectionGetMatrix().makeMatrixM4x4F(ipm);
    MatrixM4x4F.invertInPlaceWithContext(c, ipm);

    final KViewRays vr = KViewRays.newRays(c, ipm);
    KViewRaysTest.dumpViewRays(vr);

    final ContextRelative eqc = new ContextRelative();
    eqc.setMaxAbsoluteDifference(0.0001f);

    final VectorI4F expected = new VectorI4F(0.0f, 0.0f, 1.0f, 0.0f);
    Assert.assertTrue(VectorI4F.almostEqual(eqc, expected, vr.getRayX0Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, expected, vr.getRayX1Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, expected, vr.getRayX0Y1()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, expected, vr.getRayX1Y1()));

    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      -320.0f,
      -240.0f,
      0.0f,
      1.0f), vr.getOriginX0Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      320.0f,
      -240.0f,
      0.0f,
      1.0f), vr.getOriginX1Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      -320.0f,
      240.0f,
      0.0f,
      1.0f), vr.getOriginX0Y1()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      320.0f,
      240.0f,
      0.0f,
      1.0f), vr.getOriginX1Y1()));
  }

  @Test public void testPerspective0()
  {
    final Context c = new MatrixM4x4F.Context();
    final MatrixM4x4F t = new MatrixM4x4F();

    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        t,
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        100.0f);

    final RMatrixM4x4F<RTransformProjectionInverseType> ipm =
      new RMatrixM4x4F<RTransformProjectionInverseType>();
    p.projectionGetMatrix().makeMatrixM4x4F(ipm);
    MatrixM4x4F.invertInPlaceWithContext(c, ipm);

    final KViewRays vr = KViewRays.newRays(c, ipm);
    KViewRaysTest.dumpViewRays(vr);

    final ContextRelative eqc = new ContextRelative();
    eqc.setMaxAbsoluteDifference(0.0001f);

    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      1.0f,
      1.0f,
      1.0f,
      0.0f), vr.getRayX0Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      -1.0f,
      1.0f,
      1.0f,
      0.0f), vr.getRayX1Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      1.0f,
      -1.0f,
      1.0f,
      0.0f), vr.getRayX0Y1()));
    Assert.assertTrue(VectorI4F.almostEqual(eqc, new VectorI4F(
      -1.0f,
      -1.0f,
      1.0f,
      0.0f), vr.getRayX1Y1()));

    {
      final VectorI3F q =
        KBilinear.bilinear3F(
          vr.getRayX0Y0(),
          vr.getRayX1Y0(),
          vr.getRayX0Y1(),
          vr.getRayX1Y1(),
          0.5f,
          0.5f);
      System.out.printf("q: %s\n", q);
    }

    final VectorI4F expected_origin = new VectorI4F(0.0f, 0.0f, 0.0f, 1.0f);
    Assert.assertTrue(VectorI4F.almostEqual(
      eqc,
      expected_origin,
      vr.getOriginX0Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(
      eqc,
      expected_origin,
      vr.getOriginX1Y0()));
    Assert.assertTrue(VectorI4F.almostEqual(
      eqc,
      expected_origin,
      vr.getOriginX0Y1()));
    Assert.assertTrue(VectorI4F.almostEqual(
      eqc,
      expected_origin,
      vr.getOriginX1Y1()));
  }
}
