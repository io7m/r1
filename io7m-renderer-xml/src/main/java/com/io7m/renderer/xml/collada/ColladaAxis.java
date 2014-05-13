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

package com.io7m.renderer.xml.collada;

import com.io7m.jtensors.MatrixM3x3F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * <p>
 * A description of the axes used in the current model.
 * </p>
 * <p>
 * According to the COLLADA 1.4 specification:
 * </p>
 * <ul>
 * <li>All coordinates are right-handed by definition.</li>
 * <li>{@link #COLLADA_AXIS_X_UP} means that positive X faces up, negative Y
 * faces right, and positive Z faces towards the viewer.</li>
 * <li>{@link #COLLADA_AXIS_Y_UP} means that positive X faces right, positive
 * Y faces up, and positive Z faces towards the viewer.</li>
 * <li>{@link #COLLADA_AXIS_Z_UP} means that positive X faces right, negative
 * Y faces towards the viewer, and positive Z faces up.</li>
 * </ul>
 */

public enum ColladaAxis
{
  COLLADA_AXIS_X_UP,
  COLLADA_AXIS_Y_UP,
  COLLADA_AXIS_Z_UP;

  private static final VectorI3F AXIS_X;
  private static final VectorI3F AXIS_Y;
  private static final VectorI3F AXIS_Z;

  static {
    AXIS_X = new VectorI3F(1, 0, 0);
    AXIS_Y = new VectorI3F(0, 1, 0);
    AXIS_Z = new VectorI3F(0, 0, 1);
  }

  /**
   * <p>
   * Convert <tt>source</tt>, which is assumed to have its axes oriented
   * according to <tt>source_axes</tt>, to the coordinate system with the axes
   * oriented according to <tt>dest_axes</tt>.
   * </p>
   */

  public static <R extends RSpaceType> RVectorI3F<R> convertAxes(
    final MatrixM3x3F matrix,
    final ColladaAxis source_axes,
    final RVectorReadable3FType<R> source,
    final ColladaAxis dest_axes)
  {
    switch (source_axes) {
      case COLLADA_AXIS_X_UP:
      {
        switch (dest_axes) {
          case COLLADA_AXIS_X_UP:
          {
            return new RVectorI3F<R>(source);
          }
          case COLLADA_AXIS_Y_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(90),
              ColladaAxis.AXIS_Z,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
          case COLLADA_AXIS_Z_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(-90),
              ColladaAxis.AXIS_Y,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
        }
        throw new UnreachableCodeException();
      }
      case COLLADA_AXIS_Y_UP:
      {
        switch (dest_axes) {
          case COLLADA_AXIS_X_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(-90),
              ColladaAxis.AXIS_Z,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
          case COLLADA_AXIS_Y_UP:
          {
            return new RVectorI3F<R>(source);
          }
          case COLLADA_AXIS_Z_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(90),
              ColladaAxis.AXIS_X,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
        }
        throw new UnreachableCodeException();
      }
      case COLLADA_AXIS_Z_UP:
      {
        switch (dest_axes) {
          case COLLADA_AXIS_X_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(90),
              ColladaAxis.AXIS_Y,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
          case COLLADA_AXIS_Y_UP:
          {
            final VectorM3F out = new VectorM3F();

            MatrixM3x3F.setIdentity(matrix);
            MatrixM3x3F.makeRotation(
              Math.toRadians(-90),
              ColladaAxis.AXIS_X,
              matrix);

            MatrixM3x3F.multiplyVector3F(matrix, source, out);
            return new RVectorI3F<R>(out.getXF(), out.getYF(), out.getZF());
          }
          case COLLADA_AXIS_Z_UP:
          {
            return new RVectorI3F<R>(source);
          }
        }

        throw new UnreachableCodeException();
      }
    }

    throw new UnreachableCodeException();
  }
}
