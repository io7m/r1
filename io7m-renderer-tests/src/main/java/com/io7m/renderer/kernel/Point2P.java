package com.io7m.renderer.kernel;

/**
 * Pixel-space coordinates.
 */

class Point2P
{
  static Point2P fromPointH(
    final Point2H p,
    final int x,
    final int y,
    final int w,
    final int h)
  {
    final Point2H q = new Point2H(p.x / 2, p.y / 2, p.z);
    final float px = q.x / q.z;
    final float py = q.y / q.z;
    final int w2 = w / 2;
    final int h2 = h / 2;
    final float ppx = x + (px * w2) + w2;
    final float ppy = y + (py * h2) + h2;
    return new Point2P((int) ppx, (int) ppy);
  }

  final float x;
  final float y;

  public Point2P(
    final float x,
    final float y)
  {
    this.x = x;
    this.y = y;
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
    final Point2P other = (Point2P) obj;
    if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
      return false;
    }
    if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
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
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[PointP x=");
    builder.append(this.x);
    builder.append(" y=");
    builder.append(this.y);
    builder.append("]");
    return builder.toString();
  }
}