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

package com.io7m.r1.tests.xml.collada;

import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jequality.AlmostEqualFloat;
import com.io7m.jequality.AlmostEqualFloat.ContextRelative;
import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceType;
import com.io7m.r1.xml.collada.RColladaAxis;

@SuppressWarnings({ "null", "static-method" }) public final class ColladaAxisTest
{
  static class PVectorI3FGenerator<R extends RSpaceType> implements
    Generator<PVectorI3F<R>>
  {
    public PVectorI3FGenerator()
    {

    }

    @Override public PVectorI3F<R> next()
    {
      final float x = (float) Math.random();
      final float y = (float) Math.random();
      final float z = (float) Math.random();
      return new PVectorI3F<R>(x, y, z);
    }
  }

  private static ContextRelative makeRelativeContext()
  {
    final ContextRelative cr = new AlmostEqualFloat.ContextRelative();
    cr.setMaxAbsoluteDifference(0.0000000000000001f);
    return cr;
  }

  @Test public void testXUpToXUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();

    QuickCheck.forAllVerbose(
      new PVectorI3FGenerator<RSpaceObjectType>(),
      new AbstractCharacteristic<PVectorI3F<RSpaceObjectType>>() {
        @Override protected void doSpecify(
          final PVectorI3F<RSpaceObjectType> v)
          throws Throwable
        {
          Assert.assertEquals(v, RColladaAxis.convertAxes(
            matrix,
            RColladaAxis.COLLADA_AXIS_X_UP,
            v,
            RColladaAxis.COLLADA_AXIS_X_UP));
        }
      });
  }

  @Test public void testXUpToYUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(1, 0, 0);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(0, 1, 0);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_X_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_Y_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testXUpToZUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(1, 0, 0);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(0, 0, 1);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_X_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_Z_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testYUpToXUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(0, 1, 0);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(1, 0, 0);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_Y_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_X_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testYUpToYUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();

    QuickCheck.forAllVerbose(
      new PVectorI3FGenerator<RSpaceObjectType>(),
      new AbstractCharacteristic<PVectorI3F<RSpaceObjectType>>() {
        @Override protected void doSpecify(
          final PVectorI3F<RSpaceObjectType> v)
          throws Throwable
        {
          Assert.assertEquals(v, RColladaAxis.convertAxes(
            matrix,
            RColladaAxis.COLLADA_AXIS_Y_UP,
            v,
            RColladaAxis.COLLADA_AXIS_Y_UP));
        }
      });
  }

  @Test public void testYUpToZUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(0, 1, 0);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(0, 0, 1);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_Y_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_Z_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testZUpToXUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(0, 0, 1);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(1, 0, 0);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_Z_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_X_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testZUpToYUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();
    final ContextRelative cr = ColladaAxisTest.makeRelativeContext();

    final PVectorI3F<RSpaceObjectType> v_in =
      new PVectorI3F<RSpaceObjectType>(0, 0, 1);
    final PVectorI3F<RSpaceObjectType> v_exp =
      new PVectorI3F<RSpaceObjectType>(0, 1, 0);
    final PVectorI3F<RSpaceObjectType> v_out =
      RColladaAxis.convertAxes(
        matrix,
        RColladaAxis.COLLADA_AXIS_Z_UP,
        v_in,
        RColladaAxis.COLLADA_AXIS_Y_UP);

    System.out.println("v_in  : " + v_in);
    System.out.println("v_exp : " + v_exp);
    System.out.println("v_out : " + v_out);

    Assert.assertTrue(VectorI3F.almostEqual(cr, v_exp, v_out));
  }

  @Test public void testZUpToZUp()
  {
    final MatrixM3x3F matrix = new MatrixM3x3F();

    QuickCheck.forAllVerbose(
      new PVectorI3FGenerator<RSpaceObjectType>(),
      new AbstractCharacteristic<PVectorI3F<RSpaceObjectType>>() {
        @Override protected void doSpecify(
          final PVectorI3F<RSpaceObjectType> v)
          throws Throwable
        {
          Assert.assertEquals(v, RColladaAxis.convertAxes(
            matrix,
            RColladaAxis.COLLADA_AXIS_Z_UP,
            v,
            RColladaAxis.COLLADA_AXIS_Z_UP));
        }
      });
  }
}
