/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.AlmostEqualDouble;
import com.io7m.jaux.AlmostEqualDouble.ContextRelative;
import com.io7m.jtensors.VectorM3F;

public class SBFirstPersonCameraTest
{
  @SuppressWarnings("static-method") @Test public void testYawVector()
  {
    double yaw;
    final VectorM3F v = new VectorM3F();
    final ContextRelative context = new AlmostEqualDouble.ContextRelative();
    context.setMaxAbsoluteDifference(0.000000000000001);

    yaw = 0.0;
    SBFirstPersonCamera.makeYawVector(yaw, v);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, -1.0));

    yaw = Math.PI;
    SBFirstPersonCamera.makeYawVector(yaw, v);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 1.0));

    yaw = Math.PI / 2.0;
    SBFirstPersonCamera.makeYawVector(yaw, v);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));

    yaw = -(Math.PI / 2.0);
    SBFirstPersonCamera.makeYawVector(yaw, v);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, -1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));
  }

  @SuppressWarnings("static-method") @Test public void testPitchYawVector()
  {
    double pitch;
    double yaw;
    final VectorM3F v = new VectorM3F();
    final ContextRelative context = new AlmostEqualDouble.ContextRelative();
    context.setMaxAbsoluteDifference(0.000000000000001);

    pitch = 0.0;
    yaw = 0.0;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, -1.0));

    pitch = 0.0;
    yaw = Math.PI;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 1.0));

    pitch = 0.0;
    yaw = Math.PI / 2.0;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));

    pitch = 0.0;
    yaw = -(Math.PI / 2.0);
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, -1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));

    pitch = Math.PI;
    yaw = 0.0;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 1.0));

    pitch = Math.PI / 2.0;
    yaw = 0.0;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, 1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));

    pitch = -(Math.PI / 2.0);
    yaw = 0.0;
    SBFirstPersonCamera.makePitchYawVector(pitch, yaw, v);
    System.out.println("Pitch  : " + pitch);
    System.out.println("Yaw    : " + yaw);
    System.out.println("Result : " + v);
    System.out.println();
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.x, 0.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.y, -1.0));
    Assert.assertTrue(AlmostEqualDouble.almostEqual(context, v.z, 0.0));
  }
}
