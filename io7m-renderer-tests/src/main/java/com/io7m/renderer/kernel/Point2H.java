package com.io7m.renderer.kernel;


/**
 * 3D homogeneous coordinates.
 */

class Point2H
{
  static Point2H fromPointP(
    final Point2P p,
    final int x,
    final int y,
    final int w,
    final int h)
  {
    final float w2 = w / 2;
    final float h2 = h / 2;
    final float px = (p.x - x - w2) / w2;
    final float py = (p.y - y - h2) / h2;
    return new Point2H(px * 2.0f, py * 2.0f, 1.0f);
  }

  final float x;
  final float y;
  final float z;

  public Point2H plus(
    final Point2H p)
  {
    return new Point2H(p.x + this.x, p.y + this.y, 1.0f);
  }

  public Point2H scale(
    final float s)
  {
    return new Point2H(this.x * s, this.y * s, 1.0f);
  }

  public Point2H(
    final float x,
    final float y,
    final float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final Point2H other = (Point2H) obj;
    if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
      return false;
    }
    if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
      return false;
    }
    if (Float.floatToIntBits(this.z) != Float.floatToIntBits(other.z)) {
      return false;
    }
    return true;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.x);
    result = (prime * result) + Float.floatToIntBits(this.y);
    result = (prime * result) + Float.floatToIntBits(this.z);
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[PointH x=");
    builder.append(this.x);
    builder.append(" y=");
    builder.append(this.y);
    builder.append(" z=");
    builder.append(this.z);
    builder.append("]");
    return builder.toString();
  }
}