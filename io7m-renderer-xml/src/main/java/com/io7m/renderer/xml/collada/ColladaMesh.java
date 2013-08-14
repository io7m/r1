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

import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;

class ColladaMesh
{
  private final @Nonnull ArrayList<ColladaPoly>               polygons;

  /**
   * Positions are shared between "vertices" in the model. So, in the example
   * of a cube, there'll be 8 vertex positions (one for each corner of the
   * cube).
   */

  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  positions;

  /**
   * Normal vectors are stored per-polygon. So, in the example of a cube,
   * there'll be (6 * 2 = 12) normal vectors (six square faces each
   * represented by two triangles).
   */

  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  normals;

  /**
   * Texture coordinates appear to be completely unshared per "vertex". So, in
   * the case of a cube, there'll be (12 * 3 = 36) texture coordinate vectors
   * (twelve polygons, with three vertices per polygon, and one texture
   * coordinate vector for each vertex).
   */

  private final @Nonnull ArrayList<RVectorI2F<RSpaceTexture>> texcoords;
  private final @Nonnull String                               name;
  private final @Nonnull ColladaVertexType                    type;
  private final @Nonnull ColladaAxis                          axis;

  ColladaMesh(
    final @Nonnull String name,
    final @Nonnull ColladaVertexType type,
    final @Nonnull ColladaAxis axis)
  {
    this.name = name;
    this.polygons = new ArrayList<ColladaPoly>();
    this.positions = new ArrayList<RVectorI3F<RSpaceObject>>();
    this.normals = new ArrayList<RVectorI3F<RSpaceObject>>();
    this.texcoords = new ArrayList<RVectorI2F<RSpaceTexture>>();
    this.type = type;
    this.axis = axis;
  }

  public @Nonnull ColladaAxis getAxis()
  {
    return this.axis;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public @Nonnull ColladaVertexType getType()
  {
    return this.type;
  }

  public void normalAdd(
    final @Nonnull RVectorI3F<RSpaceObject> n)
  {
    this.normals.add(n);
  }

  public int normalCount()
  {
    return this.normals.size();
  }

  public @Nonnull RVectorReadable3F<RSpaceObject> normalGet(
    final int index)
    throws ConstraintError
  {
    Constraints.constrainRange(
      index,
      0,
      this.normalCount() - 1,
      "Normal index");
    return this.normals.get(index);
  }

  public void polygonAdd(
    final @Nonnull ColladaPoly p)
  {
    this.polygons.add(p);
  }

  public int polygonCount()
  {
    return this.polygons.size();
  }

  public @Nonnull ColladaPoly polygonGet(
    final int index)
    throws ConstraintError
  {
    Constraints.constrainRange(
      index,
      0,
      this.polygonCount() - 1,
      "Polygon index");
    return this.polygons.get(index);
  }

  public void positionAdd(
    final @Nonnull RVectorI3F<RSpaceObject> p)
  {
    this.positions.add(p);
  }

  public int positionCount()
  {
    return this.positions.size();
  }

  public @Nonnull RVectorI3F<RSpaceObject> positionGet(
    final int index)
    throws ConstraintError
  {
    Constraints.constrainRange(
      index,
      0,
      this.positionCount() - 1,
      "Position index");
    return this.positions.get(index);
  }

  public void uvAdd(
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
  {
    this.texcoords.add(uv);
  }

  public int uvCount()
  {
    return this.texcoords.size();
  }

  public @Nonnull RVectorI2F<RSpaceTexture> uvGet(
    final int index)
    throws ConstraintError
  {
    Constraints.constrainRange(index, 0, this.uvCount() - 1, "UV index");
    return this.texcoords.get(index);
  }
}
