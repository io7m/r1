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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.OrthonormalizedI3F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorI4F;
import com.io7m.renderer.RVectorM3F;

public final class MeshTangents
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
    private final int tangent;
    private final int bitangent;
    private final int uv;

    public Vertex(
      final int position,
      final int normal,
      final int tangent,
      final int bitangent,
      final int uv)
    {
      this.position = position;
      this.normal = normal;
      this.tangent = tangent;
      this.bitangent = bitangent;
      this.uv = uv;
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
      return builder.toString();
    }
  }

  private static @Nonnull MeshTangents copyMesh(
    final @Nonnull MeshBasic m)
    throws ConstraintError
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

  public static @Nonnull MeshTangents makeWithTangents(
    final @Nonnull MeshBasic m)
    throws ConstraintError
  {
    Constraints.constrainNotNull(m, "Mesh");

    final MeshTangents mt = MeshTangents.copyMesh(m);
    final RVectorM3F<RSpaceObject> tangent = new RVectorM3F<RSpaceObject>();
    final RVectorM3F<RSpaceObject> bitangent = new RVectorM3F<RSpaceObject>();

    /**
     * Generate tangents and bitangents, accumulating the resulting vectors.
     */

    for (final Triangle triangle : mt.triangles) {
      final Vertex v0 = mt.vertices.get(triangle.getV0());
      final Vertex v1 = mt.vertices.get(triangle.getV1());
      final Vertex v2 = mt.vertices.get(triangle.getV2());

      final RVectorI3F<RSpaceObject> v0p = mt.positions.get(v0.getPosition());
      final RVectorI3F<RSpaceObject> v1p = mt.positions.get(v1.getPosition());
      final RVectorI3F<RSpaceObject> v2p = mt.positions.get(v2.getPosition());

      final RVectorI4F<RSpaceObject> v0t = mt.tangents.get(v0.getTangent());
      final RVectorI4F<RSpaceObject> v1t = mt.tangents.get(v1.getTangent());
      final RVectorI4F<RSpaceObject> v2t = mt.tangents.get(v2.getTangent());

      final RVectorI3F<RSpaceObject> v0b =
        mt.bitangents.get(v0.getBitangent());
      final RVectorI3F<RSpaceObject> v1b =
        mt.bitangents.get(v1.getBitangent());
      final RVectorI3F<RSpaceObject> v2b =
        mt.bitangents.get(v2.getBitangent());

      final RVectorI2F<RSpaceTexture> v0u = mt.uvs.get(v0.getUV());
      final RVectorI2F<RSpaceTexture> v1u = mt.uvs.get(v1.getUV());
      final RVectorI2F<RSpaceTexture> v2u = mt.uvs.get(v2.getUV());

      final double x1 = v1p.x - v0p.x;
      final double x2 = v2p.x - v0p.x;
      final double y1 = v1p.y - v0p.y;
      final double y2 = v2p.y - v0p.y;
      final double z1 = v1p.z - v0p.z;
      final double z2 = v2p.z - v0p.z;

      final double s1 = v1u.x - v0u.x;
      final double s2 = v2u.x - v0u.x;
      final double t1 = v1u.y - v0u.y;
      final double t2 = v2u.y - v0u.y;

      final double r = 1.0 / ((s1 * t2) - (s2 * t1));

      final double tx = ((t2 * x1) - (t1 * x2)) * r;
      final double ty = ((t2 * y1) - (t1 * y2)) * r;
      final double tz = ((t2 * z1) - (t1 * z2)) * r;

      final double bx = ((s1 * x2) - (s2 * x1)) * r;
      final double by = ((s1 * y2) - (s2 * y1)) * r;
      final double bz = ((s1 * z2) - (s2 * z1)) * r;

      tangent.x = (float) tx;
      tangent.y = (float) ty;
      tangent.z = (float) tz;

      bitangent.x = (float) bx;
      bitangent.y = (float) by;
      bitangent.z = (float) bz;

      final RVectorI4F<RSpaceObject> v0t_acc =
        new RVectorI4F<RSpaceObject>(
          v0t.x + tangent.x,
          v0t.y + tangent.y,
          v0t.z + tangent.z,
          1.0f);
      final RVectorI4F<RSpaceObject> v1t_acc =
        new RVectorI4F<RSpaceObject>(
          v1t.x + tangent.x,
          v1t.y + tangent.y,
          v1t.z + tangent.z,
          1.0f);
      final RVectorI4F<RSpaceObject> v2t_acc =
        new RVectorI4F<RSpaceObject>(
          v2t.x + tangent.x,
          v2t.y + tangent.y,
          v2t.z + tangent.z,
          1.0f);

      mt.tangents.set(v0.getTangent(), v0t_acc);
      mt.tangents.set(v1.getTangent(), v1t_acc);
      mt.tangents.set(v2.getTangent(), v2t_acc);

      final RVectorI3F<RSpaceObject> v0b_acc =
        new RVectorI3F<RSpaceObject>(
          v0b.x + bitangent.x,
          v0b.y + bitangent.y,
          v0b.z + bitangent.z);
      final RVectorI3F<RSpaceObject> v1b_acc =
        new RVectorI3F<RSpaceObject>(
          v1b.x + bitangent.x,
          v1b.y + bitangent.y,
          v1b.z + bitangent.z);
      final RVectorI3F<RSpaceObject> v2b_acc =
        new RVectorI3F<RSpaceObject>(
          v2b.x + bitangent.x,
          v2b.y + bitangent.y,
          v2b.z + bitangent.z);

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
     * It calculates bintangents, inverting them if necessary to form a
     * right-handed coordinate space, and it also saves a value in the w
     * component of the tangent vector in order to allow shading language
     * programs to perform this inversion themselves, if they are calculating
     * the bitangents at runtime (with <code>cross (N, T.xyz) * T.w</code>).
     */

    for (int index = 0; index < mt.tangents.size(); ++index) {
      final RVectorI4F<RSpaceObject> t = mt.tangents.get(index);
      final RVectorI3F<RSpaceObject> b = mt.bitangents.get(index);
      final RVectorI3F<RSpaceObject> n = mt.normals.get(index);

      final OrthonormalizedI3F o = new OrthonormalizedI3F(n, t, b);
      final VectorI3F ot = o.getV1();
      final VectorI3F ob = o.getV2();

      /**
       * Invert the bitangent if the resulting coordinate system is not
       * right-handed (and save the fact that the inversion occurred in the w
       * component of the tangent vector).
       */

      RVectorI4F<RSpaceObject> rt;
      RVectorI3F<RSpaceObject> rb;
      if (VectorI3F.dotProduct(VectorI3F.crossProduct(n, t), b) < 0.0f) {
        rt = new RVectorI4F<RSpaceObject>(ot.x, ot.y, ot.z, -1.0f);
        rb = new RVectorI3F<RSpaceObject>(-ob.x, -ob.y, -ob.z);
      } else {
        rt = new RVectorI4F<RSpaceObject>(ot.x, ot.y, ot.z, 1.0f);
        rb = new RVectorI3F<RSpaceObject>(ob.x, ob.y, ob.z);
      }

      mt.tangents.set(index, rt);
      mt.bitangents.set(index, rb);
    }

    return mt;
  }

  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  normals;
  private final @Nonnull ArrayList<RVectorI4F<RSpaceObject>>  tangents;
  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  bitangents;
  private final @Nonnull ArrayList<RVectorI3F<RSpaceObject>>  positions;
  private final @Nonnull ArrayList<RVectorI2F<RSpaceTexture>> uvs;
  private final @Nonnull ArrayList<Vertex>                    vertices;
  private final @Nonnull HashMap<Vertex, Integer>             vertex_map;
  private final @Nonnull ArrayList<Triangle>                  triangles;
  private final @Nonnull String                               name;

  private MeshTangents(
    final @Nonnull List<RVectorI3F<RSpaceObject>> normals,
    final @Nonnull List<RVectorI3F<RSpaceObject>> positions,
    final @Nonnull List<RVectorI2F<RSpaceTexture>> uvs,
    final @Nonnull String name)
    throws ConstraintError
  {
    this.name = Constraints.constrainNotNull(name, "Name");

    this.normals =
      new ArrayList<RVectorI3F<RSpaceObject>>(Constraints.constrainNotNull(
        normals,
        "Normals"));
    this.positions =
      new ArrayList<RVectorI3F<RSpaceObject>>(Constraints.constrainNotNull(
        positions,
        "Positions"));
    this.uvs =
      new ArrayList<RVectorI2F<RSpaceTexture>>(Constraints.constrainNotNull(
        uvs,
        "UVs"));

    this.tangents =
      new ArrayList<RVectorI4F<RSpaceObject>>(this.normals.size());
    this.bitangents =
      new ArrayList<RVectorI3F<RSpaceObject>>(this.normals.size());

    for (int index = 0; index < this.normals.size(); ++index) {
      this.tangents.add(new RVectorI4F<RSpaceObject>(0, 0, 0, 0));
    }

    for (int index = 0; index < this.normals.size(); ++index) {
      this.bitangents.add(new RVectorI3F<RSpaceObject>(0, 0, 0));
    }

    this.vertices = new ArrayList<MeshTangents.Vertex>();
    this.vertex_map = new HashMap<MeshTangents.Vertex, Integer>();
    this.triangles = new ArrayList<MeshTangents.Triangle>();
  }

  public @Nonnull List<RVectorI3F<RSpaceObject>> bitangentsGet()
  {
    return Collections.unmodifiableList(this.bitangents);
  }

  public @Nonnull String getName()
  {
    return this.name;
  }

  public @Nonnull List<RVectorI3F<RSpaceObject>> normalsGet()
  {
    return Collections.unmodifiableList(this.normals);
  }

  public @Nonnull List<RVectorI3F<RSpaceObject>> positionsGet()
  {
    return Collections.unmodifiableList(this.positions);
  }

  public @Nonnull List<RVectorI4F<RSpaceObject>> tangentsGet()
  {
    return Collections.unmodifiableList(this.tangents);
  }

  public @Nonnull List<Triangle> trianglesGet()
  {
    return Collections.unmodifiableList(this.triangles);
  }

  public @Nonnull List<RVectorI2F<RSpaceTexture>> uvsGet()
  {
    return Collections.unmodifiableList(this.uvs);
  }

  public @Nonnull List<Vertex> verticesGet()
  {
    return Collections.unmodifiableList(this.vertices);
  }
}
