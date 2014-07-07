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

package com.io7m.renderer.meshes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jranges.RangeCheck;
import com.io7m.renderer.types.RExceptionMeshNameInvalid;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;

/**
 * <p>
 * The type of <i>basic</i> mutable meshes.
 * </p>
 * <p>
 * Basic meshes have vertices (of type {@link RMeshBasicVertex} that have
 * positions, normals, and UV coordinates.
 * </p>
 */

@EqualityReference public final class RMeshBasic
{
  /**
   * Construct a new empty mesh with the given name.
   *
   * @param name
   *          The name.
   * @return A new mesh.
   * @throws RExceptionMeshNameInvalid
   *           If the mesh name is not valid.
   * @see RMeshNames
   */

  public static RMeshBasic newMesh(
    final String name)
    throws RExceptionMeshNameInvalid
  {
    return new RMeshBasic(name);
  }

  private final String                              name;
  private final List<RVectorI3F<RSpaceObjectType>>  normals;
  private final List<RVectorI3F<RSpaceObjectType>>  positions;
  private final List<RMeshTriangle>                 triangles;
  private final List<RVectorI2F<RSpaceTextureType>> uvs;
  private final Map<RMeshBasicVertex, Integer>      vertex_map;
  private final List<RMeshBasicVertex>              vertices;

  private RMeshBasic(
    final String in_name)
    throws RExceptionMeshNameInvalid
  {
    this.name = RMeshNames.checkMeshName(in_name);
    this.normals = new ArrayList<RVectorI3F<RSpaceObjectType>>();
    this.positions = new ArrayList<RVectorI3F<RSpaceObjectType>>();
    this.uvs = new ArrayList<RVectorI2F<RSpaceTextureType>>();
    this.vertices = new ArrayList<RMeshBasicVertex>();
    this.vertex_map = new HashMap<RMeshBasicVertex, Integer>();
    this.triangles = new ArrayList<RMeshTriangle>();
  }

  private RMeshBasicVertex createVertex(
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
    RangeCheck.checkLessEqual(uv, "UV", this.uvs.size() - 1, "Maximum UV");

    final RMeshBasicVertex v = new RMeshBasicVertex(position, normal, uv);
    return v;
  }

  /**
   * @return The name of the mesh.
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * Add a new normal vector.
   *
   * @param normal
   *          The normal vector.
   * @return The index of the newly added vector.
   */

  public int normalAdd(
    final RVectorI3F<RSpaceObjectType> normal)
  {
    this.normals.add(NullCheck.notNull(normal, "KMaterialNormalLabel"));
    return this.normals.size() - 1;
  }

  /**
   * @return A read-only view of the current list of normal vectors.
   */

  public List<RVectorI3F<RSpaceObjectType>> normalsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.normals);
    assert r != null;
    return r;
  }

  /**
   * Add a new position vector.
   *
   * @param position
   *          The position vector.
   * @return The index of the newly added vector.
   */

  public int positionAdd(
    final RVectorI3F<RSpaceObjectType> position)
  {
    this.positions.add(NullCheck.notNull(position, "Position"));
    return this.positions.size() - 1;
  }

  /**
   * @return A read-only view of the current list of positions.
   */

  public List<RVectorI3F<RSpaceObjectType>> positionsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.positions);
    assert r != null;
    return r;
  }

  /**
   * Add a new triangle consisting of the given vertices.
   *
   * @param v0
   *          The index of the first vertex.
   * @param v1
   *          The index of the second vertex.
   * @param v2
   *          The index of the third vertex.
   * @return The index of the new triangle.
   */

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

    this.triangles.add(new RMeshTriangle(v0, v1, v2));
    return this.triangles.size() - 1;
  }

  /**
   * @return A read-only view of the current list of triangles.
   */

  public List<RMeshTriangle> trianglesGet()
  {
    final List<RMeshTriangle> r =
      Collections.unmodifiableList(this.triangles);
    assert r != null;
    return r;
  }

  /**
   * Add a new UV coordinate vector.
   *
   * @param uv
   *          The UV coordinate vector.
   * @return The index of the newly added vector.
   */

  public int uvAdd(
    final RVectorI2F<RSpaceTextureType> uv)
  {
    this.uvs.add(NullCheck.notNull(uv, "UV"));
    return this.uvs.size() - 1;
  }

  /**
   * @return A read-only view of the current list of UV coordinates.
   */

  public List<RVectorI2F<RSpaceTextureType>> uvsGet()
  {
    final List<RVectorI2F<RSpaceTextureType>> r =
      Collections.unmodifiableList(this.uvs);
    assert r != null;
    return r;
  }

  /**
   * Add a new vertex consisting of the given indices.
   *
   * @param position
   *          The index of the position vector.
   * @param normal
   *          The index of the normal vector.
   * @param uv
   *          The index of the UV coordinate vector.
   * @return The index of the new vertex.
   */

  public int vertexAdd(
    final int position,
    final int normal,
    final int uv)
  {
    final RMeshBasicVertex v = this.createVertex(position, normal, uv);

    if (this.vertex_map.containsKey(v)) {
      return this.vertex_map.get(v).intValue();
    }

    this.vertices.add(v);
    final int index = this.vertices.size() - 1;
    this.vertex_map.put(v, Integer.valueOf(index));
    return index;
  }

  /**
   * @return A read-only view of the current list of vertices.
   */

  public List<RMeshBasicVertex> verticesGet()
  {
    final List<RMeshBasicVertex> r =
      Collections.unmodifiableList(this.vertices);
    assert r != null;
    return r;
  }
}
