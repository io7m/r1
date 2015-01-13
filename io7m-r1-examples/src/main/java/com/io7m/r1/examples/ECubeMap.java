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

package com.io7m.r1.examples;

import java.util.Properties;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;

/**
 * Descriptions of cube maps.
 */

@EqualityStructural public final class ECubeMap
{
  /**
   * Load a cube map description from the given properties.
   *
   * @param p
   *          The properties.
   * @return A cube map description.
   * @throws JPropertyException
   *           If a cube map description cannot be parsed
   */

  public static ECubeMap fromProperties(
    final Properties p)
    throws JPropertyException
  {
    NullCheck.notNull(p, "Properties");
    final String an = JProperties.getString(p, "name");
    final String pz = JProperties.getString(p, "positive-z");
    final String nz = JProperties.getString(p, "negative-z");
    final String py = JProperties.getString(p, "positive-y");
    final String ny = JProperties.getString(p, "negative-y");
    final String px = JProperties.getString(p, "positive-x");
    final String nx = JProperties.getString(p, "negative-x");
    return new ECubeMap(an, pz, nz, py, ny, px, nx);
  }

  private final String name;
  private final String negative_x;
  private final String negative_y;
  private final String negative_z;
  private final String positive_x;
  private final String positive_y;
  private final String positive_z;

  /**
   * Construct a cube map description.
   *
   * @param in_name
   *          The name of the mesh.
   * @param pz
   *          The positive Z image.
   * @param nz
   *          The negative Z image.
   * @param py
   *          The positive Y image.
   * @param ny
   *          The negative Y image.
   * @param px
   *          The positive X image.
   * @param nx
   *          The negative X image.
   */

  public ECubeMap(
    final String in_name,
    final String pz,
    final String nz,
    final String py,
    final String ny,
    final String px,
    final String nx)
  {
    NullCheck.notNull(in_name, "Name");
    NullCheck.notNull(pz, "Positive Z");
    NullCheck.notNull(py, "Positive Y");
    NullCheck.notNull(px, "Positive X");
    NullCheck.notNull(nz, "Negative Z");
    NullCheck.notNull(ny, "Negative Y");
    NullCheck.notNull(nx, "Negative X");

    this.name = in_name;
    this.positive_z = pz;
    this.negative_z = nz;
    this.positive_y = py;
    this.negative_y = ny;
    this.positive_x = px;
    this.negative_x = nx;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    final ECubeMap other = (ECubeMap) obj;

    return this.name.equals(other.name)
      && this.negative_x.equals(other.negative_x)
      && this.negative_y.equals(other.negative_y)
      && this.negative_z.equals(other.negative_z)
      && this.positive_x.equals(other.positive_x)
      && this.positive_y.equals(other.positive_y)
      && this.positive_z.equals(other.positive_z);
  }

  /**
   * @return The name of the mesh.
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * @return The negative X image.
   */

  public String getNegativeX()
  {
    return this.negative_x;
  }

  /**
   * @return The negative Y image.
   */

  public String getNegativeY()
  {
    return this.negative_y;
  }

  /**
   * @return The negative Z image.
   */

  public String getNegativeZ()
  {
    return this.negative_z;
  }

  /**
   * @return The positive X image.
   */

  public String getPositiveX()
  {
    return this.positive_x;
  }

  /**
   * @return The positive Y image.
   */

  public String getPositiveY()
  {
    return this.positive_y;
  }

  /**
   * @return The positive Z image.
   */

  public String getPositiveZ()
  {
    return this.positive_z;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.name.hashCode();
    result = (prime * result) + this.negative_x.hashCode();
    result = (prime * result) + this.negative_y.hashCode();
    result = (prime * result) + this.negative_z.hashCode();
    result = (prime * result) + this.positive_x.hashCode();
    result = (prime * result) + this.positive_y.hashCode();
    result = (prime * result) + this.positive_z.hashCode();
    return result;
  }
}
