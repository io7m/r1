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

package com.io7m.r1.meshes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.OrthonormalizedI3F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.jtensors.parameterized.PVectorM3F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RExceptionMeshNameInvalid;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceTextureType;

/**
 * <p>
 * The type of mutable meshes that have generated tangent and bitangent
 * vectors.
 * </p>
 */

@EqualityReference public final class RMeshTangents
{
  /**
   * Construct a new builder that will directly construct an
   * {@link RMeshTangents}.
   *
   * @param in_name
   *          The name of the mesh.
   * @return A new mesh with tangents.
   * @throws RExceptionMeshNameInvalid
   *           If the mesh name is invalid.
   */

  public static RMeshTangentsBuilderType newBuilder(
    final String in_name)
    throws RExceptionMeshNameInvalid
  {
    final String mesh_name = RMeshNames.checkMeshName(in_name);

    final List<PVectorI3F<RSpaceObjectType>> in_normals =
      new ArrayList<PVectorI3F<RSpaceObjectType>>();
    final List<PVectorI3F<RSpaceObjectType>> in_positions =
      new ArrayList<PVectorI3F<RSpaceObjectType>>();
    final List<PVectorI2F<RSpaceTextureType>> in_uvs =
      new ArrayList<PVectorI2F<RSpaceTextureType>>();
    final List<PVectorI4F<RSpaceObjectType>> in_tangents =
      new ArrayList<PVectorI4F<RSpaceObjectType>>();
    final List<PVectorI3F<RSpaceObjectType>> in_bitangents =
      new ArrayList<PVectorI3F<RSpaceObjectType>>();
    final List<RMeshTriangle> in_triangles = new ArrayList<RMeshTriangle>();
    final List<RMeshTangentsVertex> in_vertices =
      new ArrayList<RMeshTangentsVertex>();

    return new RMeshTangentsBuilderType() {
      @SuppressWarnings("synthetic-access") @Override public
        RMeshTangents
        build()
      {
        return new RMeshTangents(
          in_normals,
          in_tangents,
          in_bitangents,
          in_positions,
          in_uvs,
          in_vertices,
          in_triangles,
          mesh_name);
      }

      @Override public void addVertex(
        final PVectorI3F<RSpaceObjectType> position,
        final PVectorI3F<RSpaceObjectType> normal,
        final PVectorI4F<RSpaceObjectType> tangent,
        final PVectorI3F<RSpaceObjectType> bitangent,
        final PVectorI2F<RSpaceTextureType> uv)
      {
        NullCheck.notNull(position, "Position");
        NullCheck.notNull(normal, "Normal");
        NullCheck.notNull(tangent, "Tangent");
        NullCheck.notNull(bitangent, "Bitangent");
        NullCheck.notNull(uv, "UV");

        final int index = in_positions.size();
        in_positions.add(position);
        in_normals.add(normal);
        in_tangents.add(tangent);
        in_bitangents.add(bitangent);
        in_uvs.add(uv);
        in_vertices.add(new RMeshTangentsVertex(
          index,
          index,
          index,
          index,
          index));
      }

      @Override public void addTriangle(
        final long v0,
        final long v1,
        final long v2)
      {
        final RMeshTriangle tt = new RMeshTriangle(v0, v1, v2);
        in_triangles.add(tt);
      }
    };
  }

  private static RMeshTangents copyMesh(
    final RMeshBasic m)
  {
    try {
      final RMeshTangents mt =
        new RMeshTangents(
          m.normalsGet(),
          m.positionsGet(),
          m.uvsGet(),
          m.getName());

      for (final RMeshBasicVertex vb : m.verticesGet()) {
        final RMeshTangentsVertex vt =
          new RMeshTangentsVertex(
            vb.getPosition(),
            vb.getNormal(),
            vb.getNormal(),
            vb.getNormal(),
            vb.getUV());
        mt.vertices.add(vt);
      }

      for (final RMeshTriangle bt : m.trianglesGet()) {
        final RMeshTriangle tt =
          new RMeshTriangle(bt.getV0(), bt.getV1(), bt.getV2());
        mt.triangles.add(tt);
      }

      assert mt.positions.size() == m.positionsGet().size();
      assert mt.normals.size() == m.normalsGet().size();
      assert mt.tangents.size() == m.normalsGet().size();
      assert mt.bitangents.size() == m.normalsGet().size();
      assert mt.triangles.size() == m.trianglesGet().size();
      assert mt.uvs.size() == m.uvsGet().size();
      assert mt.name.equals(m.getName());

      return mt;
    } catch (final RExceptionMeshNameInvalid e) {
      // If the name of the basic mesh was valid, then so is a copy of it.
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Generate tangent and bitangent vectors from the given {@link RMeshBasic}.
   *
   * @param m
   *          The basic mesh.
   * @return The same mesh with generated tangent and bitangent vectors.
   */

  public static RMeshTangents makeWithTangents(
    final RMeshBasic m)
  {
    NullCheck.notNull(m, "Mesh");

    final RMeshTangents mt = RMeshTangents.copyMesh(m);
    final PVectorM3F<RSpaceObjectType> tangent =
      new PVectorM3F<RSpaceObjectType>();
    final PVectorM3F<RSpaceObjectType> bitangent =
      new PVectorM3F<RSpaceObjectType>();

    /**
     * Generate tangents and bitangents, accumulating the resulting vectors.
     */

    for (final RMeshTriangle triangle : mt.triangles) {
      final RMeshTangentsVertex v0 = mt.vertices.get((int) triangle.getV0());
      final RMeshTangentsVertex v1 = mt.vertices.get((int) triangle.getV1());
      final RMeshTangentsVertex v2 = mt.vertices.get((int) triangle.getV2());

      final PVectorI3F<RSpaceObjectType> v0p =
        mt.positions.get(v0.getPosition());
      final PVectorI3F<RSpaceObjectType> v1p =
        mt.positions.get(v1.getPosition());
      final PVectorI3F<RSpaceObjectType> v2p =
        mt.positions.get(v2.getPosition());

      final PVectorI4F<RSpaceObjectType> v0t =
        mt.tangents.get(v0.getTangent());
      final PVectorI4F<RSpaceObjectType> v1t =
        mt.tangents.get(v1.getTangent());
      final PVectorI4F<RSpaceObjectType> v2t =
        mt.tangents.get(v2.getTangent());

      final PVectorI3F<RSpaceObjectType> v0b =
        mt.bitangents.get(v0.getBitangent());
      final PVectorI3F<RSpaceObjectType> v1b =
        mt.bitangents.get(v1.getBitangent());
      final PVectorI3F<RSpaceObjectType> v2b =
        mt.bitangents.get(v2.getBitangent());

      final PVectorI2F<RSpaceTextureType> v0u = mt.uvs.get(v0.getUV());
      final PVectorI2F<RSpaceTextureType> v1u = mt.uvs.get(v1.getUV());
      final PVectorI2F<RSpaceTextureType> v2u = mt.uvs.get(v2.getUV());

      final double x1 = v1p.getXF() - v0p.getXF();
      final double x2 = v2p.getXF() - v0p.getXF();

      final double y1 = v1p.getYF() - v0p.getYF();
      final double y2 = v2p.getYF() - v0p.getYF();

      final double z1 = v1p.getZF() - v0p.getZF();
      final double z2 = v2p.getZF() - v0p.getZF();

      final double s1 = v1u.getXF() - v0u.getXF();
      final double s2 = v2u.getXF() - v0u.getXF();

      final double t1 = v1u.getYF() - v0u.getYF();
      final double t2 = v2u.getYF() - v0u.getYF();

      final double r = 1.0 / ((s1 * t2) - (s2 * t1));

      final double tx = ((t2 * x1) - (t1 * x2)) * r;
      final double ty = ((t2 * y1) - (t1 * y2)) * r;
      final double tz = ((t2 * z1) - (t1 * z2)) * r;

      final double bx = ((s1 * x2) - (s2 * x1)) * r;
      final double by = ((s1 * y2) - (s2 * y1)) * r;
      final double bz = ((s1 * z2) - (s2 * z1)) * r;

      tangent.set3F((float) tx, (float) ty, (float) tz);
      bitangent.set3F((float) bx, (float) by, (float) bz);

      final PVectorI4F<RSpaceObjectType> v0t_acc =
        new PVectorI4F<RSpaceObjectType>(
          v0t.getXF() + tangent.getXF(),
          v0t.getYF() + tangent.getYF(),
          v0t.getZF() + tangent.getZF(),
          1.0f);
      final PVectorI4F<RSpaceObjectType> v1t_acc =
        new PVectorI4F<RSpaceObjectType>(
          v1t.getXF() + tangent.getXF(),
          v1t.getYF() + tangent.getYF(),
          v1t.getZF() + tangent.getZF(),
          1.0f);
      final PVectorI4F<RSpaceObjectType> v2t_acc =
        new PVectorI4F<RSpaceObjectType>(
          v2t.getXF() + tangent.getXF(),
          v2t.getYF() + tangent.getYF(),
          v2t.getZF() + tangent.getZF(),
          1.0f);

      mt.tangents.set(v0.getTangent(), v0t_acc);
      mt.tangents.set(v1.getTangent(), v1t_acc);
      mt.tangents.set(v2.getTangent(), v2t_acc);

      final PVectorI3F<RSpaceObjectType> v0b_acc =
        new PVectorI3F<RSpaceObjectType>(
          v0b.getXF() + bitangent.getXF(),
          v0b.getYF() + bitangent.getYF(),
          v0b.getZF() + bitangent.getZF());
      final PVectorI3F<RSpaceObjectType> v1b_acc =
        new PVectorI3F<RSpaceObjectType>(
          v1b.getXF() + bitangent.getXF(),
          v1b.getYF() + bitangent.getYF(),
          v1b.getZF() + bitangent.getZF());
      final PVectorI3F<RSpaceObjectType> v2b_acc =
        new PVectorI3F<RSpaceObjectType>(
          v2b.getXF() + bitangent.getXF(),
          v2b.getYF() + bitangent.getYF(),
          v2b.getZF() + bitangent.getZF());

      mt.bitangents.set(v0.getBitangent(), v0b_acc);
      mt.bitangents.set(v1.getBitangent(), v1b_acc);
      mt.bitangents.set(v2.getBitangent(), v2b_acc);
    }

    /**
     * Orthonormalize tangents and bitangents.
     *
     * The normal, tangent, and bitangent vectors must form an orthonormal
     * right-handed basis.
     *
     * Because precomputed bitangents are optional, this code does two things:
     * It calculates bitangents, inverting them if necessary to form a
     * right-handed coordinate space, and it also saves a value in the w
     * component of the tangent vector in order to allow shading language
     * programs to perform this inversion themselves, if they are calculating
     * the bitangents at runtime (with <code>cross (N, T.xyz) * T.w</code>).
     */

    for (int index = 0; index < mt.tangents.size(); ++index) {
      final PVectorI4F<RSpaceObjectType> t = mt.tangents.get(index);
      assert t != null;
      final PVectorI3F<RSpaceObjectType> b = mt.bitangents.get(index);
      assert b != null;
      final PVectorI3F<RSpaceObjectType> n = mt.normals.get(index);
      assert n != null;

      final OrthonormalizedI3F o = new OrthonormalizedI3F(n, t, b);
      final VectorI3F ot = o.getV1();
      final VectorI3F ob = o.getV2();

      /**
       * Invert the bitangent if the resulting coordinate system is not
       * right-handed (and save the fact that the inversion occurred in the w
       * component of the tangent vector).
       */

      PVectorI4F<RSpaceObjectType> rt;
      PVectorI3F<RSpaceObjectType> rb;
      if (VectorI3F.dotProduct(VectorI3F.crossProduct(n, t), b) < 0.0f) {
        rt =
          new PVectorI4F<RSpaceObjectType>(
            ot.getXF(),
            ot.getYF(),
            ot.getZF(),
            -1.0f);
        rb =
          new PVectorI3F<RSpaceObjectType>(
            -ob.getXF(),
            -ob.getYF(),
            -ob.getZF());
      } else {
        rt =
          new PVectorI4F<RSpaceObjectType>(
            ot.getXF(),
            ot.getYF(),
            ot.getZF(),
            1.0f);
        rb =
          new PVectorI3F<RSpaceObjectType>(ob.getXF(), ob.getYF(), ob.getZF());
      }

      mt.tangents.set(index, rt);
      mt.bitangents.set(index, rb);
    }

    return mt;
  }

  private final List<PVectorI3F<RSpaceObjectType>>  normals;
  private final List<PVectorI4F<RSpaceObjectType>>  tangents;
  private final List<PVectorI3F<RSpaceObjectType>>  bitangents;
  private final List<PVectorI3F<RSpaceObjectType>>  positions;
  private final List<PVectorI2F<RSpaceTextureType>> uvs;
  private final List<RMeshTangentsVertex>           vertices;
  private final List<RMeshTriangle>                 triangles;
  private final String                              name;

  private RMeshTangents(
    final List<PVectorI3F<RSpaceObjectType>> in_normals,
    final List<PVectorI3F<RSpaceObjectType>> in_positions,
    final List<PVectorI2F<RSpaceTextureType>> in_uvs,
    final String in_name)
    throws RExceptionMeshNameInvalid
  {
    this.name = RMeshNames.checkMeshName(in_name);

    this.normals =
      new ArrayList<PVectorI3F<RSpaceObjectType>>(NullCheck.notNullAll(
        in_normals,
        "Normals"));
    this.positions =
      new ArrayList<PVectorI3F<RSpaceObjectType>>(NullCheck.notNullAll(
        in_positions,
        "Positions"));
    this.uvs =
      new ArrayList<PVectorI2F<RSpaceTextureType>>(NullCheck.notNullAll(
        in_uvs,
        "UVs"));

    this.tangents =
      new ArrayList<PVectorI4F<RSpaceObjectType>>(this.normals.size());
    this.bitangents =
      new ArrayList<PVectorI3F<RSpaceObjectType>>(this.normals.size());

    for (int index = 0; index < this.normals.size(); ++index) {
      this.tangents.add(new PVectorI4F<RSpaceObjectType>(0, 0, 0, 0));
    }

    for (int index = 0; index < this.normals.size(); ++index) {
      this.bitangents.add(new PVectorI3F<RSpaceObjectType>(0, 0, 0));
    }

    this.vertices = new ArrayList<RMeshTangentsVertex>();
    this.triangles = new ArrayList<RMeshTriangle>();
  }

  private RMeshTangents(
    final List<PVectorI3F<RSpaceObjectType>> in_normals,
    final List<PVectorI4F<RSpaceObjectType>> in_tangents,
    final List<PVectorI3F<RSpaceObjectType>> in_bitangents,
    final List<PVectorI3F<RSpaceObjectType>> in_positions,
    final List<PVectorI2F<RSpaceTextureType>> in_uvs,
    final List<RMeshTangentsVertex> in_vertices,
    final List<RMeshTriangle> in_triangles,
    final String in_name)
  {
    this.normals = NullCheck.notNull(in_normals, "Normals");
    this.tangents = NullCheck.notNull(in_tangents, "Tangents");
    this.bitangents = NullCheck.notNull(in_bitangents, "Bitangents");
    this.positions = NullCheck.notNull(in_positions, "Positions");
    this.uvs = NullCheck.notNull(in_uvs, "UVs");
    this.vertices = NullCheck.notNull(in_vertices, "Vertices");
    this.triangles = NullCheck.notNull(in_triangles, "Triangles");
    this.name = NullCheck.notNull(in_name, "Name");
  }

  /**
   * @return A read-only view of the current list of bitangents.
   */

  public List<PVectorI3F<RSpaceObjectType>> bitangentsGet()
  {
    final List<PVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.bitangents);
    assert r != null;
    return r;
  }

  /**
   * @return The name of the mesh.
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * @return A read-only view of the current list of normals.
   */

  public List<PVectorI3F<RSpaceObjectType>> normalsGet()
  {
    final List<PVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.normals);
    assert r != null;
    return r;
  }

  /**
   * @return A read-only view of the current list of positions.
   */

  public List<PVectorI3F<RSpaceObjectType>> positionsGet()
  {
    final List<PVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.positions);
    assert r != null;
    return r;
  }

  /**
   * @return A read-only view of the current list of tangents.
   */

  public List<PVectorI4F<RSpaceObjectType>> tangentsGet()
  {
    final List<PVectorI4F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.tangents);
    assert r != null;
    return r;
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
   * @return A read-only view of the current list of UV coordinate vectors.
   */

  public List<PVectorI2F<RSpaceTextureType>> uvsGet()
  {
    final List<PVectorI2F<RSpaceTextureType>> r =
      Collections.unmodifiableList(this.uvs);
    assert r != null;
    return r;
  }

  /**
   * @return A read-only view of the current list of vertices.
   */

  public List<RMeshTangentsVertex> verticesGet()
  {
    final List<RMeshTangentsVertex> r =
      Collections.unmodifiableList(this.vertices);
    assert r != null;
    return r;
  }
}
