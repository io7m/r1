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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.kernel.KTriangleClipping;
import com.io7m.r1.kernel.RTriangle4F;
import com.io7m.r1.types.RSpaceClipType;

@SuppressWarnings("static-method") public final class KTriangleClippingTest
{
  @Test public void testClippingNegativeX_In()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_NEGATIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(1, r.size());

    final RTriangle4F<RSpaceClipType> rt = r.get(0);
    Assert.assertEquals(t, rt);
  }

  @Test public void testClippingNegativeX_P0Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_NEGATIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(-2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingNegativeX_P1Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_NEGATIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(-2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingNegativeX_P2Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_NEGATIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(-2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingNegativeXDiscarded()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_NEGATIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(-2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(-2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(-2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(0, r.size());
  }

  @Test public void testClippingPositiveX_In()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(1, r.size());

    final RTriangle4F<RSpaceClipType> rt = r.get(0);
    Assert.assertEquals(t, rt);
  }

  @Test public void testClippingPositiveX_P0Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveX_P0P1Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(1, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveX_P0P2Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(1, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveX_P1Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(0.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveX_P1P2Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(1, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveX_P2Out()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(0.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(0.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(2, r.size());

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(0);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }

    {
      final RTriangle4F<RSpaceClipType> rt = r.get(1);
      System.out.println(rt);
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP0(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
      Assert.assertTrue(KTriangleClipping.pointIsInside(rt.getP1(), plane));
    }
  }

  @Test public void testClippingPositiveXDiscarded()
  {
    final VectorI4F plane = KTriangleClipping.PLANE_POSITIVE_X;

    final PVectorI4F<RSpaceClipType> p0 =
      new PVectorI4F<RSpaceClipType>(2.0f, 1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p1 =
      new PVectorI4F<RSpaceClipType>(2.0f, -1.0f, 0.0f, 1.0f);
    final PVectorI4F<RSpaceClipType> p2 =
      new PVectorI4F<RSpaceClipType>(2.0f, 0.0f, 0.0f, 1.0f);

    final RTriangle4F<RSpaceClipType> t = RTriangle4F.newTriangle(p0, p1, p2);

    final List<RTriangle4F<RSpaceClipType>> r =
      KTriangleClipping.clipTrianglePlane(t, plane);

    Assert.assertEquals(0, r.size());
  }
}
