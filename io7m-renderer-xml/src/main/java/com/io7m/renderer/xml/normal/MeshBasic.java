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
import java.util.HashMap;
import java.util.List;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;

public final class MeshBasic
{
  @EqualityStructural public static final class Triangle
  {
    private final int v0;
    private final int v1;
    private final int v2;

    public Triangle(
      final int in_v0,
      final int in_v1,
      final int in_v2)
    {
      this.v0 = in_v0;
      this.v1 = in_v1;
      this.v2 = in_v2;
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
      final Triangle other = (Triangle) obj;
      return (this.v0 == other.v0)
        && (this.v1 == other.v1)
        && (this.v2 == other.v2);
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

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.v0;
      result = (prime * result) + this.v1;
      result = (prime * result) + this.v2;
      return result;
    }
  }

  @EqualityStructural public static final class Vertex
  {
    private final int normal;
    private final int position;
    private final int uv;

    public Vertex(
      final int in_position,
      final int in_normal,
      final int in_uv)
    {
      this.position = in_position;
      this.normal = in_normal;
      this.uv = in_uv;
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
      final Vertex other = (Vertex) obj;
      if (this.normal != other.normal) {
        return false;
      }
      if (this.position != other.position) {
        return false;
      }
      if (this.uv != other.uv) {
        return false;
      }
      return true;
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

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.normal;
      result = (prime * result) + this.position;
      result = (prime * result) + this.uv;
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[Vertex [position ");
      builder.append(this.position);
      builder.append("] [normal ");
      builder.append(this.normal);
      builder.append("] [uv ");
      builder.append(this.uv);
      builder.append("]]");
      final String r = builder.toString();
      assert r != null;
      return r;
    }
  }

  public static MeshBasic newMesh(
    final String name)
  {
    return new MeshBasic(name);
  }

  private boolean                                        has_uv;
  private final String                                   name;
  private final ArrayList<RVectorI3F<RSpaceObjectType>>  normals;
  private final ArrayList<RVectorI3F<RSpaceObjectType>>  positions;
  private final ArrayList<Triangle>                      triangles;
  private final ArrayList<RVectorI2F<RSpaceTextureType>> uvs;
  private final HashMap<Vertex, Integer>                 vertex_map;
  private final ArrayList<Vertex>                        vertices;

  private MeshBasic(
    final String in_name)
  {
    this.name = NullCheck.notNull(in_name, "Mesh name");
    this.normals = new ArrayList<RVectorI3F<RSpaceObjectType>>();
    this.positions = new ArrayList<RVectorI3F<RSpaceObjectType>>();
    this.uvs = new ArrayList<RVectorI2F<RSpaceTextureType>>();
    this.vertices = new ArrayList<Vertex>();
    this.vertex_map = new HashMap<MeshBasic.Vertex, Integer>();
    this.triangles = new ArrayList<Triangle>();
    this.has_uv = false;
  }

  private Vertex createVertex(
    final int position,
    final int normal,
    final int uv)
  {
    RangeCheck.checkLessEqual(
      position,
      "Position",
      this.positions.size() - 1,
      "Maximum position");
    RangeCheck.checkLessEqual(
      normal,
      "Normal",
      this.normals.size() - 1,
      "Maximum normal");

    if (this.hasUV()) {
      RangeCheck.checkLessEqual(uv, "UV", this.uvs.size() - 1, "Maximum UV");
    } else {
      if (uv != -1) {
        throw new IllegalStateException("Mesh must have UV attributes");
      }
    }

    final Vertex v = new Vertex(position, normal, uv);
    return v;
  }

  public String getName()
  {
    return this.name;
  }

  public boolean hasUV()
  {
    return this.has_uv;
  }

  public int normalAdd(
    final RVectorI3F<RSpaceObjectType> normal)
  {
    this.normals.add(NullCheck.notNull(normal, "KMaterialNormalLabel"));
    return this.normals.size() - 1;
  }

  public List<RVectorI3F<RSpaceObjectType>> normalsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.normals);
    assert r != null;
    return r;
  }

  public int positionAdd(
    final RVectorI3F<RSpaceObjectType> position)
  {
    this.positions.add(NullCheck.notNull(position, "Position"));
    return this.positions.size() - 1;
  }

  public List<RVectorI3F<RSpaceObjectType>> positionsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.positions);
    assert r != null;
    return r;
  }

  public void setHasUV(
    final boolean in_has_uv)
  {
    this.has_uv = in_has_uv;
  }

  public int triangleAdd(
    final int v0,
    final int v1,
    final int v2)
  {
    RangeCheck.checkLessEqual(
      v0,
      "Vertex 0",
      this.vertices.size() - 1,
      "Maximum vertex index");
    RangeCheck.checkLessEqual(
      v1,
      "Vertex 1",
      this.vertices.size() - 1,
      "Maximum vertex index");
    RangeCheck.checkLessEqual(
      v2,
      "Vertex 2",
      this.vertices.size() - 1,
      "Maximum vertex index");

    this.triangles.add(new Triangle(v0, v1, v2));
    return this.triangles.size() - 1;
  }

  public List<Triangle> trianglesGet()
  {
    final List<Triangle> r = Collections.unmodifiableList(this.triangles);
    assert r != null;
    return r;
  }

  public int uvAdd(
    final RVectorI2F<RSpaceTextureType> uv)
  {
    if (this.hasUV() == false) {
      throw new IllegalArgumentException("Mesh does not have UV attributes");
    }

    this.uvs.add(NullCheck.notNull(uv, "UV"));
    return this.uvs.size() - 1;
  }

  public List<RVectorI2F<RSpaceTextureType>> uvsGet()
  {
    final List<RVectorI2F<RSpaceTextureType>> r =
      Collections.unmodifiableList(this.uvs);
    assert r != null;
    return r;
  }

  public int vertexAdd(
    final int position,
    final int normal,
    final int uv)
  {
    final Vertex v = this.createVertex(position, normal, uv);

    if (this.vertex_map.containsKey(v)) {
      return this.vertex_map.get(v).intValue();
    }

    this.vertices.add(v);
    final int index = this.vertices.size() - 1;
    this.vertex_map.put(v, Integer.valueOf(index));
    return index;
  }

  public List<Vertex> verticesGet()
  {
    final List<Vertex> r = Collections.unmodifiableList(this.vertices);
    assert r != null;
    return r;
  }
}
