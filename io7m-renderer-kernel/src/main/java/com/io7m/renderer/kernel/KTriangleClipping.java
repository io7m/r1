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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI4F;
import com.io7m.jtensors.VectorReadable4FType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RTriangle4F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable4FType;

/**
 * Functions for performing clipping on triangles.
 */

@EqualityReference public final class KTriangleClipping
{
  /**
   * The <code>-X</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_NEGATIVE_X;

  /**
   * The <code>-Y</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_NEGATIVE_Y;

  /**
   * The <code>-Z</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_NEGATIVE_Z;

  /**
   * The <code>+X</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_POSITIVE_X;

  /**
   * The <code>+Y</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_POSITIVE_Y;

  /**
   * The <code>+Z</code> homogeneous clipping plane.
   */

  public static final VectorI4F       PLANE_POSITIVE_Z;

  /**
   * A read-only list of the six homogeneous clipping plane.
   */

  public static final List<VectorI4F> PLANES;

  static {
    PLANE_POSITIVE_X = new VectorI4F(1.0f, 0.0f, 0.0f, -1.0f);
    PLANE_NEGATIVE_X = new VectorI4F(-1.0f, 0.0f, 0.0f, -1.0f);

    PLANE_NEGATIVE_Y = new VectorI4F(0.0f, 1.0f, 0.0f, -1.0f);
    PLANE_POSITIVE_Y = new VectorI4F(0.0f, -1.0f, 0.0f, -1.0f);

    PLANE_NEGATIVE_Z = new VectorI4F(0.0f, 0.0f, 1.0f, -1.0f);
    PLANE_POSITIVE_Z = new VectorI4F(0.0f, 0.0f, -1.0f, -1.0f);

    final List<VectorI4F> p = new LinkedList<VectorI4F>();
    p.add(KTriangleClipping.PLANE_NEGATIVE_X);
    p.add(KTriangleClipping.PLANE_NEGATIVE_Y);
    p.add(KTriangleClipping.PLANE_NEGATIVE_Z);
    p.add(KTriangleClipping.PLANE_POSITIVE_X);
    p.add(KTriangleClipping.PLANE_POSITIVE_Y);
    p.add(KTriangleClipping.PLANE_POSITIVE_Z);

    final List<VectorI4F> r = Collections.unmodifiableList(p);
    assert r != null;
    PLANES = r;
  }

  /**
   * Clip the given triangle against the given plane, returning the resulting
   * triangles (if any).
   * 
   * @param triangle
   *          The triangle
   * @param plane
   *          The plane
   * @param <S>
   *          The type of coordinate space
   * @return A list of the resulting clipped triangles
   */

  public static
    <S extends RSpaceType>
    List<RTriangle4F<S>>
    clipTrianglePlane(
      final RTriangle4F<S> triangle,
      final VectorI4F plane)
  {
    NullCheck.notNull(triangle, "Triangle");
    NullCheck.notNull(plane, "Plane");
    return KTriangleClipping.clipTrianglePlaneInner(triangle, plane);
  }

  private static
    <S extends RSpaceType>
    List<RTriangle4F<S>>
    clipTrianglePlaneInner(
      final RTriangle4F<S> triangle,
      final VectorI4F plane)
  {
    final RVectorI4F<S> p0 = triangle.getP0();
    final RVectorI4F<S> p1 = triangle.getP1();
    final RVectorI4F<S> p2 = triangle.getP2();

    final float p0_dot = VectorI4F.dotProduct(p0, plane);
    final float p1_dot = VectorI4F.dotProduct(p1, plane);
    final float p2_dot = VectorI4F.dotProduct(p2, plane);

    final boolean p0_inside = KTriangleClipping.isInside(p0_dot);
    final boolean p1_inside = KTriangleClipping.isInside(p1_dot);
    final boolean p2_inside = KTriangleClipping.isInside(p2_dot);

    final List<RTriangle4F<S>> results = new ArrayList<RTriangle4F<S>>();

    /**
     * Is the triangle completely inside the clipping plane? If so, keep it.
     */

    if (p0_inside && p1_inside && p2_inside) {
      results.add(triangle);
      return results;
    }

    /**
     * The triangle was completely outside of the clipping plane, discard it.
     */

    if ((!p0_inside) && (!p1_inside) && (!p2_inside)) {
      return results;
    }

    /**
     * Some clipping may be required.
     */

    if (p0_inside) {
      if (p1_inside) {
        assert p0_inside;
        assert p1_inside;
        assert !p2_inside;

        final RVectorI4F<S> ip1p2 =
          KTriangleClipping.intersectLinePlane(p1, p2, plane);
        final RVectorI4F<S> ip0p2 =
          KTriangleClipping.intersectLinePlane(p0, p2, plane);

        results.add(RTriangle4F.newTriangle(p0, p1, ip1p2));
        results.add(RTriangle4F.newTriangle(p0, ip1p2, ip0p2));
        return results;
      }

      if (p2_inside) {
        assert p0_inside;
        assert !p1_inside;
        assert p2_inside;

        final RVectorI4F<S> ip0p1 =
          KTriangleClipping.intersectLinePlane(p0, p1, plane);
        final RVectorI4F<S> ip2p1 =
          KTriangleClipping.intersectLinePlane(p2, p1, plane);

        results.add(RTriangle4F.newTriangle(p0, p2, ip2p1));
        results.add(RTriangle4F.newTriangle(p0, ip2p1, ip0p1));
        return results;
      }

      assert p0_inside;
      assert !p1_inside;
      assert !p2_inside;

      final RVectorI4F<S> ip0p1 =
        KTriangleClipping.intersectLinePlane(p0, p1, plane);
      final RVectorI4F<S> ip0p2 =
        KTriangleClipping.intersectLinePlane(p0, p2, plane);

      results.add(RTriangle4F.newTriangle(p0, ip0p2, ip0p1));
      return results;
    }

    if (p1_inside) {
      if (p2_inside) {
        assert !p0_inside;
        assert p1_inside;
        assert p2_inside;

        final RVectorI4F<S> ip1p0 =
          KTriangleClipping.intersectLinePlane(p1, p0, plane);
        final RVectorI4F<S> ip2p0 =
          KTriangleClipping.intersectLinePlane(p2, p0, plane);

        final RTriangle4F<S> t0 = RTriangle4F.newTriangle(p1, p2, ip2p0);
        final RTriangle4F<S> t1 = RTriangle4F.newTriangle(p1, ip2p0, ip1p0);
        results.add(t0);
        results.add(t1);
        return results;
      }

      assert !p0_inside;
      assert p1_inside;
      assert !p2_inside;

      final RVectorI4F<S> ip0p1 =
        KTriangleClipping.intersectLinePlane(p1, p0, plane);
      final RVectorI4F<S> ip1p2 =
        KTriangleClipping.intersectLinePlane(p1, p2, plane);

      results.add(RTriangle4F.newTriangle(p1, ip1p2, ip0p1));
      return results;
    }

    if (p2_inside) {
      assert !p0_inside;
      assert !p1_inside;
      assert p2_inside;

      final RVectorI4F<S> ip0p2 =
        KTriangleClipping.intersectLinePlane(p0, p2, plane);
      final RVectorI4F<S> ip1p2 =
        KTriangleClipping.intersectLinePlane(p1, p2, plane);

      results.add(RTriangle4F.newTriangle(p2, ip0p2, ip1p2));
      return results;
    }

    throw new UnreachableCodeException();
  }

  /**
   * Clip the given triangle against the given planes, writing the resulting
   * triangles to <code>results</code>.
   * 
   * @param triangle
   *          The triangle
   * @param planes
   *          The list of planes
   * @param <S>
   *          The type of coordinate space
   * @return A list of the resulting clipped triangles
   */

  public static
    <S extends RSpaceType>
    List<RTriangle4F<S>>
    clipTrianglePlanes(
      final RTriangle4F<S> triangle,
      final List<VectorI4F> planes)
  {
    NullCheck.notNull(triangle, "Triangle");
    NullCheck.notNull(planes, "Planes");

    final List<VectorI4F> in_planes = new LinkedList<VectorI4F>();
    in_planes.addAll(planes);

    final List<RTriangle4F<S>> in_triangles =
      new LinkedList<RTriangle4F<S>>();
    in_triangles.add(triangle);

    return KTriangleClipping
      .clipTrianglesPlanesInner(in_triangles, in_planes);
  }

  private static
    <S extends RSpaceType>
    List<RTriangle4F<S>>
    clipTrianglesPlanesInner(
      final List<RTriangle4F<S>> triangles,
      final List<VectorI4F> planes)
  {
    if (planes.size() == 0) {
      return triangles;
    }

    final VectorI4F plane = planes.remove(0);
    assert plane != null;

    final List<RTriangle4F<S>> results = new LinkedList<RTriangle4F<S>>();

    for (final RTriangle4F<S> t : triangles) {
      assert t != null;
      results.addAll(KTriangleClipping.clipTrianglePlane(t, plane));
    }

    return KTriangleClipping.clipTrianglesPlanesInner(results, planes);
  }

  private static <S extends RSpaceType> RVectorI4F<S> intersectLinePlane(
    final RVectorReadable4FType<S> p0,
    final RVectorReadable4FType<S> p1,
    final VectorReadable4FType plane)
  {
    final VectorI4F slope = VectorI4F.subtract(p1, p0);
    final float p0_dot = VectorI4F.dotProduct(p0, plane);
    final float t = -p0_dot / VectorI4F.dotProduct(slope, plane);
    final VectorI4F s = VectorI4F.addScaled(p0, slope, t);
    return new RVectorI4F<S>(s.getXF(), s.getYF(), s.getZF(), s.getWF());
  }

  private static boolean isInside(
    final float dot)
  {
    return dot <= 0;
  }

  /**
   * Return <code>true</code> iff the given point is inside or on the given
   * plane.
   * 
   * @param <S>
   *          The type of the coordinate space
   * @param point
   *          The point
   * @param plane
   *          The plane
   * @return <code>true</code> iff the given point is inside or on the given
   *         plane.
   */

  public static <S extends RSpaceType> boolean pointIsInside(
    final RVectorI4F<S> point,
    final VectorI4F plane)
  {
    return KTriangleClipping.isInside(VectorI4F.dotProduct(point, plane));
  }

  private KTriangleClipping()
  {
    throw new UnreachableCodeException();
  }
}
