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
import com.io7m.jtensors.OrthonormalizedI3F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorM3F;

public final class MeshTangents
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

  @EqualityStructural public static final class Vertex
  {
    private final int position;
    private final int normal;
    private final int tangent;
    private final int bitangent;
    private final int uv;

    public Vertex(
      final int in_position,
      final int in_normal,
      final int in_tangent,
      final int in_bitangent,
      final int in_uv)
    {
      this.position = in_position;
      this.normal = in_normal;
      this.tangent = in_tangent;
      this.bitangent = in_bitangent;
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
      if (this.bitangent != other.bitangent) {
        return false;
      }
      if (this.normal != other.normal) {
        return false;
      }
      if (this.position != other.position) {
        return false;
      }
      if (this.tangent != other.tangent) {
        return false;
      }
      if (this.uv != other.uv) {
        return false;
      }
      return true;
    }

    public int getBitangent()
    {
      return this.bitangent;
    }

    public int getNormal()
    {
      return this.normal;
    }

    public int getPosition()
    {
      return this.position;
    }

    public int getTangent()
    {
      return this.tangent;
    }

    public int getUV()
    {
      return this.uv;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.bitangent;
      result = (prime * result) + this.normal;
      result = (prime * result) + this.position;
      result = (prime * result) + this.tangent;
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
      builder.append("] [tangent ");
      builder.append(this.tangent);
      builder.append("] [bitangent ");
      builder.append(this.bitangent);
      builder.append("] [uv ");
      builder.append(this.uv);
      builder.append("]]");
      final String r = builder.toString();
      assert r != null;
      return r;
    }
  }

  private static MeshTangents copyMesh(
    final MeshBasic m)
  {
    final MeshTangents mt =
      new MeshTangents(
        m.normalsGet(),
        m.positionsGet(),
        m.uvsGet(),
        m.getName());

    for (final MeshBasic.Vertex vb : m.verticesGet()) {
      final MeshTangents.Vertex vt =
        new MeshTangents.Vertex(
          vb.getPosition(),
          vb.getNormal(),
          vb.getNormal(),
          vb.getNormal(),
          vb.getUV());
      mt.vertices.add(vt);
    }

    for (final MeshBasic.Triangle bt : m.trianglesGet()) {
      final Triangle tt =
        new MeshTangents.Triangle(bt.getV0(), bt.getV1(), bt.getV2());
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
  }

  public static MeshTangents makeWithTangents(
    final MeshBasic m)
  {
    NullCheck.notNull(m, "Mesh");

    final MeshTangents mt = MeshTangents.copyMesh(m);
    final RVectorM3F<RSpaceObjectType> tangent =
      new RVectorM3F<RSpaceObjectType>();
    final RVectorM3F<RSpaceObjectType> bitangent =
      new RVectorM3F<RSpaceObjectType>();

    /**
     * Generate tangents and bitangents, accumulating the resulting vectors.
     */

    for (final Triangle triangle : mt.triangles) {
      final Vertex v0 = mt.vertices.get(triangle.getV0());
      final Vertex v1 = mt.vertices.get(triangle.getV1());
      final Vertex v2 = mt.vertices.get(triangle.getV2());

      final RVectorI3F<RSpaceObjectType> v0p =
        mt.positions.get(v0.getPosition());
      final RVectorI3F<RSpaceObjectType> v1p =
        mt.positions.get(v1.getPosition());
      final RVectorI3F<RSpaceObjectType> v2p =
        mt.positions.get(v2.getPosition());

      final RVectorI4F<RSpaceObjectType> v0t =
        mt.tangents.get(v0.getTangent());
      final RVectorI4F<RSpaceObjectType> v1t =
        mt.tangents.get(v1.getTangent());
      final RVectorI4F<RSpaceObjectType> v2t =
        mt.tangents.get(v2.getTangent());

      final RVectorI3F<RSpaceObjectType> v0b =
        mt.bitangents.get(v0.getBitangent());
      final RVectorI3F<RSpaceObjectType> v1b =
        mt.bitangents.get(v1.getBitangent());
      final RVectorI3F<RSpaceObjectType> v2b =
        mt.bitangents.get(v2.getBitangent());

      final RVectorI2F<RSpaceTextureType> v0u = mt.uvs.get(v0.getUV());
      final RVectorI2F<RSpaceTextureType> v1u = mt.uvs.get(v1.getUV());
      final RVectorI2F<RSpaceTextureType> v2u = mt.uvs.get(v2.getUV());

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

      final RVectorI4F<RSpaceObjectType> v0t_acc =
        new RVectorI4F<RSpaceObjectType>(
          v0t.getXF() + tangent.getXF(),
          v0t.getYF() + tangent.getYF(),
          v0t.getZF() + tangent.getZF(),
          1.0f);
      final RVectorI4F<RSpaceObjectType> v1t_acc =
        new RVectorI4F<RSpaceObjectType>(
          v1t.getXF() + tangent.getXF(),
          v1t.getYF() + tangent.getYF(),
          v1t.getZF() + tangent.getZF(),
          1.0f);
      final RVectorI4F<RSpaceObjectType> v2t_acc =
        new RVectorI4F<RSpaceObjectType>(
          v2t.getXF() + tangent.getXF(),
          v2t.getYF() + tangent.getYF(),
          v2t.getZF() + tangent.getZF(),
          1.0f);

      mt.tangents.set(v0.getTangent(), v0t_acc);
      mt.tangents.set(v1.getTangent(), v1t_acc);
      mt.tangents.set(v2.getTangent(), v2t_acc);

      final RVectorI3F<RSpaceObjectType> v0b_acc =
        new RVectorI3F<RSpaceObjectType>(
          v0b.getXF() + bitangent.getXF(),
          v0b.getYF() + bitangent.getYF(),
          v0b.getZF() + bitangent.getZF());
      final RVectorI3F<RSpaceObjectType> v1b_acc =
        new RVectorI3F<RSpaceObjectType>(
          v1b.getXF() + bitangent.getXF(),
          v1b.getYF() + bitangent.getYF(),
          v1b.getZF() + bitangent.getZF());
      final RVectorI3F<RSpaceObjectType> v2b_acc =
        new RVectorI3F<RSpaceObjectType>(
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
      final RVectorI4F<RSpaceObjectType> t = mt.tangents.get(index);
      final RVectorI3F<RSpaceObjectType> b = mt.bitangents.get(index);
      final RVectorI3F<RSpaceObjectType> n = mt.normals.get(index);

      final OrthonormalizedI3F o = new OrthonormalizedI3F(n, t, b);
      final VectorI3F ot = o.getV1();
      final VectorI3F ob = o.getV2();

      /**
       * Invert the bitangent if the resulting coordinate system is not
       * right-handed (and save the fact that the inversion occurred in the w
       * component of the tangent vector).
       */

      RVectorI4F<RSpaceObjectType> rt;
      RVectorI3F<RSpaceObjectType> rb;
      if (VectorI3F.dotProduct(VectorI3F.crossProduct(n, t), b) < 0.0f) {
        rt =
          new RVectorI4F<RSpaceObjectType>(
            ot.getXF(),
            ot.getYF(),
            ot.getZF(),
            -1.0f);
        rb =
          new RVectorI3F<RSpaceObjectType>(
            -ob.getXF(),
            -ob.getYF(),
            -ob.getZF());
      } else {
        rt =
          new RVectorI4F<RSpaceObjectType>(
            ot.getXF(),
            ot.getYF(),
            ot.getZF(),
            1.0f);
        rb =
          new RVectorI3F<RSpaceObjectType>(ob.getXF(), ob.getYF(), ob.getZF());
      }

      mt.tangents.set(index, rt);
      mt.bitangents.set(index, rb);
    }

    return mt;
  }

  private final ArrayList<RVectorI3F<RSpaceObjectType>>  normals;
  private final ArrayList<RVectorI4F<RSpaceObjectType>>  tangents;
  private final ArrayList<RVectorI3F<RSpaceObjectType>>  bitangents;
  private final ArrayList<RVectorI3F<RSpaceObjectType>>  positions;
  private final ArrayList<RVectorI2F<RSpaceTextureType>> uvs;
  private final ArrayList<Vertex>                        vertices;
  private final HashMap<Vertex, Integer>                 vertex_map;
  private final ArrayList<Triangle>                      triangles;
  private final String                                   name;

  private MeshTangents(
    final List<RVectorI3F<RSpaceObjectType>> in_normals,
    final List<RVectorI3F<RSpaceObjectType>> in_positions,
    final List<RVectorI2F<RSpaceTextureType>> in_uvs,
    final String in_name)
  {
    this.name = NullCheck.notNull(in_name, "Name");

    this.normals =
      new ArrayList<RVectorI3F<RSpaceObjectType>>(NullCheck.notNullAll(
        in_normals,
        "Normals"));
    this.positions =
      new ArrayList<RVectorI3F<RSpaceObjectType>>(NullCheck.notNullAll(
        in_positions,
        "Positions"));
    this.uvs =
      new ArrayList<RVectorI2F<RSpaceTextureType>>(NullCheck.notNullAll(
        in_uvs,
        "UVs"));

    this.tangents =
      new ArrayList<RVectorI4F<RSpaceObjectType>>(this.normals.size());
    this.bitangents =
      new ArrayList<RVectorI3F<RSpaceObjectType>>(this.normals.size());

    for (int index = 0; index < this.normals.size(); ++index) {
      this.tangents.add(new RVectorI4F<RSpaceObjectType>(0, 0, 0, 0));
    }

    for (int index = 0; index < this.normals.size(); ++index) {
      this.bitangents.add(new RVectorI3F<RSpaceObjectType>(0, 0, 0));
    }

    this.vertices = new ArrayList<MeshTangents.Vertex>();
    this.vertex_map = new HashMap<MeshTangents.Vertex, Integer>();
    this.triangles = new ArrayList<MeshTangents.Triangle>();
  }

  public List<RVectorI3F<RSpaceObjectType>> bitangentsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.bitangents);
    assert r != null;
    return r;
  }

  public String getName()
  {
    return this.name;
  }

  public List<RVectorI3F<RSpaceObjectType>> normalsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.normals);
    assert r != null;
    return r;
  }

  public List<RVectorI3F<RSpaceObjectType>> positionsGet()
  {
    final List<RVectorI3F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.positions);
    assert r != null;
    return r;
  }

  public List<RVectorI4F<RSpaceObjectType>> tangentsGet()
  {
    final List<RVectorI4F<RSpaceObjectType>> r =
      Collections.unmodifiableList(this.tangents);
    assert r != null;
    return r;
  }

  public List<Triangle> trianglesGet()
  {
    final List<Triangle> r = Collections.unmodifiableList(this.triangles);
    assert r != null;
    return r;
  }

  public List<RVectorI2F<RSpaceTextureType>> uvsGet()
  {
    final List<RVectorI2F<RSpaceTextureType>> r =
      Collections.unmodifiableList(this.uvs);
    assert r != null;
    return r;
  }

  public List<Vertex> verticesGet()
  {
    final List<Vertex> r = Collections.unmodifiableList(this.vertices);
    assert r != null;
    return r;
  }
}
