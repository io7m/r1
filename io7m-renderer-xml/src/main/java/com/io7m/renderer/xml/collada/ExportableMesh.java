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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorM3F;
import com.io7m.renderer.RVectorReadable2F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.xml.RXMLConstants;

final class ExportableMesh
{
  @Immutable static class Triangle
  {
    final int         v0;
    private final int v1;
    private final int v2;

    Triangle(
      final int v0,
      final int v1,
      final int v2)
    {
      this.v0 = v0;
      this.v1 = v1;
      this.v2 = v2;
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
      final Triangle other = (Triangle) obj;
      if (this.v0 != other.v0) {
        return false;
      }
      if (this.v1 != other.v1) {
        return false;
      }
      if (this.v2 != other.v2) {
        return false;
      }
      return true;
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

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[Triangle ");
      builder.append(this.v0);
      builder.append(" ");
      builder.append(this.v1);
      builder.append(" ");
      builder.append(this.v2);
      builder.append("]");
      return builder.toString();
    }
  }

  static class Vertex
  {
    final @Nonnull RVectorReadable3F<RSpaceObject>        position;
    final @CheckForNull RVectorReadable3F<RSpaceObject>   normal;
    private @CheckForNull RVectorReadable3F<RSpaceObject> tangent;
    final @CheckForNull RVectorReadable2F<RSpaceTexture>  uv;

    Vertex(
      final @Nonnull RVectorReadable3F<RSpaceObject> position,
      final @CheckForNull RVectorReadable3F<RSpaceObject> normal,
      final @CheckForNull RVectorReadable3F<RSpaceObject> tangent,
      final @CheckForNull RVectorReadable2F<RSpaceTexture> uv)
    {
      this.position = position;
      this.normal = normal;
      this.tangent = tangent;
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
      if (this.normal == null) {
        if (other.normal != null) {
          return false;
        }
      } else if (!this.normal.equals(other.normal)) {
        return false;
      }
      if (this.position == null) {
        if (other.position != null) {
          return false;
        }
      } else if (!this.position.equals(other.position)) {
        return false;
      }
      if (this.tangent == null) {
        if (other.tangent != null) {
          return false;
        }
      } else if (!this.tangent.equals(other.tangent)) {
        return false;
      }
      if (this.uv == null) {
        if (other.uv != null) {
          return false;
        }
      } else if (!this.uv.equals(other.uv)) {
        return false;
      }
      return true;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result =
        (prime * result)
          + ((this.normal == null) ? 0 : this.normal.hashCode());
      result =
        (prime * result)
          + ((this.position == null) ? 0 : this.position.hashCode());
      result =
        (prime * result)
          + ((this.tangent == null) ? 0 : this.tangent.hashCode());
      result =
        (prime * result) + ((this.uv == null) ? 0 : this.uv.hashCode());
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
      builder.append("] [uv ");
      builder.append(this.uv);
      builder.append("]]");
      return builder.toString();
    }

    void setTangent(
      final @Nonnull RVectorReadable3F<RSpaceObject> t)
    {
      this.tangent = t;
    }
  }

  private static void makeTangents(
    final @Nonnull Vertex v1,
    final @Nonnull Vertex v2,
    final @Nonnull Vertex v3,
    final @Nonnull Log log)
  {
    assert v1.uv != null;
    assert v2.uv != null;
    assert v3.uv != null;

    final float x1 = v2.position.getXF() - v1.position.getXF();
    final float x2 = v3.position.getXF() - v1.position.getXF();

    final float y1 = v2.position.getYF() - v1.position.getYF();
    final float y2 = v3.position.getYF() - v1.position.getYF();

    final float z1 = v2.position.getZF() - v1.position.getZF();
    final float z2 = v3.position.getZF() - v1.position.getZF();

    final float s1 = v2.uv.getXF() - v1.uv.getXF();
    final float s2 = v3.uv.getXF() - v1.uv.getXF();
    final float t1 = v2.uv.getYF() - v1.uv.getYF();
    final float t2 = v3.uv.getYF() - v1.uv.getYF();

    final float r = 1.0F / ((s1 * t2) - (s2 * t1));

    final float t_x = ((t2 * x1) - (t1 * x2)) * r;
    final float t_y = ((t2 * y1) - (t1 * y2)) * r;
    final float t_z = ((t2 * z1) - (t1 * z2)) * r;
    final RVectorM3F<RSpaceObject> tv =
      new RVectorM3F<RSpaceObject>(t_x, t_y, t_z);
    VectorM3F.normalizeInPlace(tv);

    final float b_x = ((s1 * x2) - (s2 * x1)) * r;
    final float b_y = ((s1 * y2) - (s2 * y1)) * r;
    final float b_z = ((s1 * z2) - (s2 * z1)) * r;
    final RVectorM3F<RSpaceObject> bv =
      new RVectorM3F<RSpaceObject>(b_x, b_y, b_z);
    VectorM3F.normalizeInPlace(bv);

    v1.setTangent(tv);
    v2.setTangent(tv);
    v3.setTangent(tv);

    log.debug("v1 : " + v1.normal + " " + tv + " " + bv);
    log.debug("v2 : " + v1.normal + " " + tv + " " + bv);
    log.debug("v3 : " + v1.normal + " " + tv + " " + bv);
    log.debug("--");
  }

  @SuppressWarnings("boxing") private static @Nonnull String outFloat(
    final float x)
  {
    return String.format("%.6f", x);
  }

  private static @Nonnull Element toXMLVertexNormal(
    final RVectorReadable3F<RSpaceObject> normal)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element e = new Element("m:n", uri);
    e.addAttribute(new Attribute("m:x", uri, ExportableMesh.outFloat(normal
      .getXF())));
    e.addAttribute(new Attribute("m:y", uri, ExportableMesh.outFloat(normal
      .getYF())));
    e.addAttribute(new Attribute("m:z", uri, ExportableMesh.outFloat(normal
      .getZF())));
    return e;
  }

  private static @Nonnull Element toXMLVertexPosition(
    final RVectorReadable3F<RSpaceObject> position)
  {
    final String u = RXMLConstants.MESHES_URI.toString();
    final Element e = new Element("m:p", u);
    e.addAttribute(new Attribute("m:x", u, ExportableMesh.outFloat(position
      .getXF())));
    e.addAttribute(new Attribute("m:y", u, ExportableMesh.outFloat(position
      .getYF())));
    e.addAttribute(new Attribute("m:z", u, ExportableMesh.outFloat(position
      .getZF())));
    return e;
  }

  private static @Nonnull Element toXMLVertexTangent(
    final RVectorReadable3F<RSpaceObject> tangent)
  {
    final String u = RXMLConstants.MESHES_URI.toString();
    final Element e = new Element("m:t", u);
    e.addAttribute(new Attribute("m:x", u, ExportableMesh.outFloat(tangent
      .getXF())));
    e.addAttribute(new Attribute("m:y", u, ExportableMesh.outFloat(tangent
      .getYF())));
    e.addAttribute(new Attribute("m:z", u, ExportableMesh.outFloat(tangent
      .getZF())));
    return e;
  }

  private static @Nonnull Element toXMLVertexUV(
    final RVectorReadable2F<RSpaceTexture> uv)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element e = new Element("m:u", uri);
    e.addAttribute(new Attribute("m:x", uri, ExportableMesh.outFloat(uv
      .getXF())));
    e.addAttribute(new Attribute("m:y", uri, ExportableMesh.outFloat(uv
      .getYF())));
    return e;
  }

  private final @Nonnull MatrixM4x4F          matrix;
  private final @Nonnull MatrixM4x4F.Context  matrix_context;
  private final @Nonnull Map<Vertex, Integer> vertices_map;
  private final @Nonnull ArrayList<Vertex>    vertices;
  private final @Nonnull ArrayList<Triangle>  polygons;
  private final @Nonnull Log                  log;
  private final @Nonnull String               name;
  private final @Nonnull ColladaVertexType    type;

  ExportableMesh(
    final @Nonnull ColladaMesh mesh,
    final @Nonnull Log log)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Collada mesh");
    this.log = new Log(Constraints.constrainNotNull(log, "Log"), "export");

    this.vertices = new ArrayList<Vertex>();
    this.vertices_map = new HashMap<Vertex, Integer>();
    this.polygons = new ArrayList<Triangle>();
    this.matrix = new MatrixM4x4F();
    this.matrix_context = new MatrixM4x4F.Context();
    this.name = mesh.getName();
    this.type = mesh.getType();

    final ColladaAxis axis = mesh.getAxis();
    this.log.debug("Rotating vertices and normals from "
      + axis
      + " to "
      + ColladaAxis.COLLADA_AXIS_Y_UP);

    for (int index = 0; index < mesh.polygonCount(); ++index) {
      final ColladaPoly poly = mesh.polygonGet(index);

      RVectorReadable3F<RSpaceObject> p0 =
        mesh.positionGet(poly.getPosition0());
      RVectorReadable3F<RSpaceObject> p1 =
        mesh.positionGet(poly.getPosition1());
      RVectorReadable3F<RSpaceObject> p2 =
        mesh.positionGet(poly.getPosition2());

      p0 =
        ColladaAxis.convertAxes(
          this.matrix_context,
          this.matrix,
          axis,
          p0,
          ColladaAxis.COLLADA_AXIS_Y_UP);
      p1 =
        ColladaAxis.convertAxes(
          this.matrix_context,
          this.matrix,
          axis,
          p1,
          ColladaAxis.COLLADA_AXIS_Y_UP);
      p2 =
        ColladaAxis.convertAxes(
          this.matrix_context,
          this.matrix,
          axis,
          p2,
          ColladaAxis.COLLADA_AXIS_Y_UP);

      RVectorReadable3F<RSpaceObject> n0 = null;
      RVectorReadable3F<RSpaceObject> n1 = null;
      RVectorReadable3F<RSpaceObject> n2 = null;

      if (this.type.hasNormal()) {
        n0 = mesh.normalGet(poly.getNormal0());
        n1 = mesh.normalGet(poly.getNormal1());
        n2 = mesh.normalGet(poly.getNormal2());

        n0 =
          ColladaAxis.convertAxes(
            this.matrix_context,
            this.matrix,
            axis,
            n0,
            ColladaAxis.COLLADA_AXIS_Y_UP);
        n1 =
          ColladaAxis.convertAxes(
            this.matrix_context,
            this.matrix,
            axis,
            n1,
            ColladaAxis.COLLADA_AXIS_Y_UP);
        n2 =
          ColladaAxis.convertAxes(
            this.matrix_context,
            this.matrix,
            axis,
            n2,
            ColladaAxis.COLLADA_AXIS_Y_UP);
      }

      RVectorReadable2F<RSpaceTexture> u0 = null;
      RVectorReadable2F<RSpaceTexture> u1 = null;
      RVectorReadable2F<RSpaceTexture> u2 = null;

      if (this.type.hasUV()) {
        u0 = mesh.uvGet(poly.getUV0());
        u1 = mesh.uvGet(poly.getUV1());
        u2 = mesh.uvGet(poly.getUV2());
      }

      final Vertex v0 = new Vertex(p0, n0, null, u0);
      final Vertex v1 = new Vertex(p1, n1, null, u1);
      final Vertex v2 = new Vertex(p2, n2, null, u2);

      if (this.type.hasUV()) {
        ExportableMesh.makeTangents(v0, v1, v2, log);
      }

      this.makeTriangle(v0, v1, v2);
    }

    this.log
      .debug("Resulting mesh has " + this.vertices.size() + " vertices");
    this.log
      .debug("Resulting mesh has " + this.polygons.size() + " polygons");
  }

  private boolean calculatedTangents()
  {
    return this.type.hasUV() && this.type.hasNormal();
  }

  /**
   * Create a new triangle using vertices <tt>(v0, v1, v2)</tt>. If there are
   * existing vertices equal to <tt>v0</tt>, <tt>v1</tt>, or <tt>v2</tt>, use
   * those instead.
   */

  private void makeTriangle(
    final @Nonnull Vertex v0,
    final @Nonnull Vertex v1,
    final @Nonnull Vertex v2)
  {
    int v0i = -1;
    int v1i = -1;
    int v2i = -1;

    if (this.vertices_map.containsKey(v0)) {
      v0i = this.vertices_map.get(v0).intValue();
    } else {
      this.vertices.add(v0);
      v0i = this.vertices.size() - 1;
      this.vertices_map.put(v0, Integer.valueOf(v0i));
    }

    if (this.vertices_map.containsKey(v1)) {
      v1i = this.vertices_map.get(v1).intValue();
    } else {
      this.vertices.add(v1);
      v1i = this.vertices.size() - 1;
      this.vertices_map.put(v1, Integer.valueOf(v1i));
    }

    if (this.vertices_map.containsKey(v2)) {
      v2i = this.vertices_map.get(v2).intValue();
    } else {
      this.vertices.add(v2);
      v2i = this.vertices.size() - 1;
      this.vertices_map.put(v2, Integer.valueOf(v2i));
    }

    assert v0i != -1;
    assert v1i != -1;
    assert v2i != -1;

    this.polygons.add(new Triangle(v0i, v1i, v2i));
  }

  @Nonnull Element toXML()
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element e = new Element("m:mesh", uri);

    e.addAttribute(new Attribute("m:name", uri, this.name));
    e.addAttribute(new Attribute("m:version", uri, Integer
      .toString(RXMLConstants.MESHES_VERSION)));

    final Element et = this.toXMLType();
    final Element ev = this.toXMLVertices();
    final Element etr = this.toXMLTriangles();

    e.appendChild(et);
    e.appendChild(ev);
    e.appendChild(etr);

    return e;
  }

  @SuppressWarnings("synthetic-access") private @Nonnull
    Element
    toXMLTriangles()
  {
    final String u = RXMLConstants.MESHES_URI.toString();
    final Element ets = new Element("m:triangles", u);

    ets.addAttribute(new Attribute("m:count", u, Integer
      .toString(this.polygons.size())));

    for (final Triangle tri : this.polygons) {
      final Element et = new Element("m:tri", u);
      et.addAttribute(new Attribute("m:v0", u, Integer.toString(tri.v0)));
      et.addAttribute(new Attribute("m:v1", u, Integer.toString(tri.v1)));
      et.addAttribute(new Attribute("m:v2", u, Integer.toString(tri.v2)));
      ets.appendChild(et);
    }

    return ets;
  }

  private @Nonnull Element toXMLType()
  {
    final String uri = RXMLConstants.MESHES_URI.toString();

    final Element et = new Element("m:type", uri);
    final Element etp = new Element("m:attribute-position-3f", uri);
    et.appendChild(etp);

    if (this.type.hasNormal()) {
      final Element etn = new Element("m:attribute-normal-3f", uri);
      et.appendChild(etn);
    }

    if (this.type.hasUV()) {
      final Element etu = new Element("m:attribute-uv-2f", uri);
      et.appendChild(etu);
    }

    if (this.calculatedTangents()) {
      final Element ett = new Element("m:attribute-tangent-3f", uri);
      et.appendChild(ett);
    }
    return et;
  }

  @SuppressWarnings("synthetic-access") private @Nonnull
    Element
    toXMLVertices()
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element evs = new Element("m:vertices", uri);

    evs.addAttribute(new Attribute("m:count", uri, Integer
      .toString(this.vertices.size())));

    for (final Vertex vertex : this.vertices) {
      final Element ev = new Element("m:v", uri);
      ev.appendChild(ExportableMesh.toXMLVertexPosition(vertex.position));
      if (vertex.normal != null) {
        ev.appendChild(ExportableMesh.toXMLVertexNormal(vertex.normal));
      }
      if (vertex.uv != null) {
        ev.appendChild(ExportableMesh.toXMLVertexUV(vertex.uv));
      }
      if (vertex.tangent != null) {
        ev.appendChild(ExportableMesh.toXMLVertexTangent(vertex.tangent));
      }
      evs.appendChild(ev);
    }

    return evs;
  }
}
