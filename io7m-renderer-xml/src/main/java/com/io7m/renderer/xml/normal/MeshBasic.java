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

package com.io7m.renderer.xml.normal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;

public final class MeshBasic
{
  @Immutable public static final class Triangle
  {
    private final int v0;
    private final int v1;
    private final int v2;

    public Triangle(
      final int v0,
      final int v1,
      final int v2)
    {
      this.v0 = v0;
      this.v1 = v1;
      this.v2 = v2;
    }

    public int getV0()
    {
      return this.v0;
    }

    public int getV1()
    {
      return this.v1;
    }

    public int getV2()
    {
      return this.v2;
    }
  }

  @Immutable public static final class Vertex
  {
    private final int position;
    private final int normal;
    private final int uv;

    public Vertex(
      final int position,
      final int normal,
      final int uv)
    {
      this.position = position;
      this.normal = normal;
      this.uv = uv;
    }

    public int getNormal()
    {
      return this.normal;
    }

    public int getPosition()
    {
      return this.position;
    }

    public int getUV()
    {
      return this.uv;
    }
  }

  public static @Nonnull MeshBasic newMesh(
    final @Nonnull String name)
    throws ConstraintError
  {
    return new MeshBasic(name);
  }

  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  normals;
  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  positions;
  private final @Nonnull ArrayList<RVectorI2F<RSpaceTexture>> uvs;
  private final @Nonnull ArrayList<Vertex>                    vertices;
  private final @Nonnull ArrayList<Triangle>                  triangles;
  private final @Nonnull String                               name;
  private boolean                                             has_uv;

  private MeshBasic(
    final @Nonnull String name)
    throws ConstraintError
  {
    this.name = Constraints.constrainNotNull(name, "Mesh name");
    this.normals = new ArrayList<RVectorI3F<RSpaceObject>>();
    this.positions = new ArrayList<RVectorI3F<RSpaceObject>>();
    this.uvs = new ArrayList<RVectorI2F<RSpaceTexture>>();
    this.vertices = new ArrayList<Vertex>();
    this.triangles = new ArrayList<Triangle>();
    this.has_uv = false;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public boolean hasUV()
  {
    return this.has_uv;
  }

  public int normalAdd(
    final @Nonnull RVectorI3F<RSpaceObject> normal)
    throws ConstraintError
  {
    this.normals.add(Constraints.constrainNotNull(normal, "Normal"));
    return this.normals.size() - 1;
  }

  public @Nonnull List<RVectorI3F<RSpaceObject>> normalsGet()
  {
    return Collections.unmodifiableList(this.normals);
  }

  public int positionAdd(
    final @Nonnull RVectorI3F<RSpaceObject> position)
    throws ConstraintError
  {
    this.positions.add(Constraints.constrainNotNull(position, "Position"));
    return this.positions.size() - 1;
  }

  public @Nonnull List<RVectorI3F<RSpaceObject>> positionsGet()
  {
    return Collections.unmodifiableList(this.positions);
  }

  public void setHasUV(
    final boolean has_uv)
  {
    this.has_uv = has_uv;
  }

  public int triangleAdd(
    final int v0,
    final int v1,
    final int v2)
    throws ConstraintError
  {
    Constraints.constrainRange(
      v0,
      0,
      this.vertices.size() - 1,
      "Vertex 0 in range");
    Constraints.constrainRange(
      v1,
      0,
      this.vertices.size() - 1,
      "Vertex 1 in range");
    Constraints.constrainRange(
      v2,
      0,
      this.vertices.size() - 1,
      "Vertex 2 in range");
    this.triangles.add(new Triangle(v0, v1, v2));
    return this.triangles.size() - 1;
  }

  public @Nonnull List<Triangle> trianglesGet()
  {
    return Collections.unmodifiableList(this.triangles);
  }

  public int uvAdd(
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      this.hasUV(),
      "Mesh should have UV attributes");
    this.uvs.add(Constraints.constrainNotNull(uv, "UV"));
    return this.uvs.size() - 1;
  }

  public @Nonnull List<RVectorI2F<RSpaceTexture>> uvsGet()
  {
    return Collections.unmodifiableList(this.uvs);
  }

  public int vertexAdd(
    final int position,
    final int normal,
    final int uv)
    throws ConstraintError
  {
    Constraints.constrainRange(
      position,
      0,
      this.positions.size() - 1,
      "Position in range");
    Constraints.constrainRange(
      normal,
      0,
      this.normals.size() - 1,
      "Normal in range");

    if (this.hasUV()) {
      Constraints.constrainRange(uv, 0, this.uvs.size() - 1, "UV in range");
    } else {
      if (uv != -1) {
        Constraints.constrainArbitrary(false, "Mesh has UV attributes");
      }
    }

    this.vertices.add(new Vertex(position, normal, uv));
    return this.vertices.size() - 1;
  }

  public @Nonnull List<Vertex> verticesGet()
  {
    return Collections.unmodifiableList(this.vertices);
  }
}
