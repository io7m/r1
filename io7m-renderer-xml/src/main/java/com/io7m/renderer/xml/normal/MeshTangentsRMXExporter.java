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

import java.util.List;

import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.renderer.types.RSpaceObject;
import com.io7m.renderer.types.RSpaceTexture;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.xml.normal.MeshTangents.Triangle;
import com.io7m.renderer.xml.normal.MeshTangents.Vertex;
import com.io7m.renderer.xml.rmx.RXMLConstants;

public class MeshTangentsRMXExporter
{
  private static @Nonnull Attribute floatAttribute(
    final @Nonnull String name,
    final float x)
  {
    return new Attribute(
      name,
      RXMLConstants.MESHES_URI.toString(),
      MeshTangentsRMXExporter.floatString(x));
  }

  @SuppressWarnings("boxing") private static @Nonnull String floatString(
    final float x)
  {
    return String.format("%.8f", x);
  }

  private static @Nonnull Attribute intAttribute(
    final @Nonnull String name,
    final int x)
  {
    return new Attribute(
      name,
      RXMLConstants.MESHES_URI.toString(),
      Integer.toString(x));
  }

  private static @Nonnull Attribute stringAttribute(
    final @Nonnull String name,
    final @Nonnull String x)
  {
    return new Attribute(name, RXMLConstants.MESHES_URI.toString(), x);
  }

  private static @Nonnull Element toXMLRoot(
    final @Nonnull MeshTangents m)
  {
    final Element e =
      new Element("m:mesh", RXMLConstants.MESHES_URI.toString());
    e.addAttribute(MeshTangentsRMXExporter.intAttribute(
      "m:version",
      RXMLConstants.MESHES_VERSION));
    e.addAttribute(MeshTangentsRMXExporter.stringAttribute(
      "m:name",
      m.getName()));
    return e;
  }

  private static @Nonnull Element toXMLTriangles(
    final @Nonnull MeshTangents m)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element ets = new Element("m:triangles", uri);
    final List<Triangle> triangles = m.trianglesGet();
    ets.addAttribute(MeshTangentsRMXExporter.intAttribute(
      "m:count",
      triangles.size()));

    for (final Triangle t : triangles) {
      final Element et = new Element("m:tri", uri);
      et
        .addAttribute(MeshTangentsRMXExporter.intAttribute("m:v0", t.getV0()));
      et
        .addAttribute(MeshTangentsRMXExporter.intAttribute("m:v1", t.getV1()));
      et
        .addAttribute(MeshTangentsRMXExporter.intAttribute("m:v2", t.getV2()));
      ets.appendChild(et);
    }

    return ets;
  }

  private static @Nonnull Element toXMLType(
    final boolean write_bitangents)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element et = new Element("m:type", uri);
    et.appendChild(new Element("m:attribute-position-3f", uri));
    et.appendChild(new Element("m:attribute-normal-3f", uri));
    et.appendChild(new Element("m:attribute-uv-2f", uri));

    if (write_bitangents) {
      et.appendChild(new Element("m:attribute-tangent-3f", uri));
      et.appendChild(new Element("m:attribute-bitangent-3f", uri));
    } else {
      et.appendChild(new Element("m:attribute-tangent-4f", uri));
    }

    return et;
  }

  private static @Nonnull Element toXMLVertices(
    final @Nonnull MeshTangents m,
    final boolean write_bitangents)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final List<Vertex> vertices = m.verticesGet();
    final List<RVectorI3F<RSpaceObject>> positions = m.positionsGet();
    final List<RVectorI3F<RSpaceObject>> normals = m.normalsGet();
    final List<RVectorI2F<RSpaceTexture>> uvs = m.uvsGet();
    final List<RVectorI4F<RSpaceObject>> tangents = m.tangentsGet();
    final List<RVectorI3F<RSpaceObject>> bitangents = m.bitangentsGet();

    final Element evs = new Element("m:vertices", uri);
    evs.addAttribute(MeshTangentsRMXExporter.intAttribute(
      "m:count",
      vertices.size()));

    for (final Vertex v : vertices) {
      final Element ev = new Element("m:v", uri);

      final Element ep = new Element("m:p", uri);
      final RVectorI3F<RSpaceObject> p = positions.get(v.getPosition());
      ep.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", p.x));
      ep.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", p.y));
      ep.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", p.z));
      ev.appendChild(ep);

      final Element en = new Element("m:n", uri);
      final RVectorI3F<RSpaceObject> n = normals.get(v.getNormal());
      en.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", n.x));
      en.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", n.y));
      en.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", n.z));
      ev.appendChild(en);

      if (write_bitangents) {
        final Element et = new Element("m:t3", uri);
        final RVectorI4F<RSpaceObject> t = tangents.get(v.getTangent());
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", t.x));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", t.y));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", t.z));
        ev.appendChild(et);

        final Element eb = new Element("m:b", uri);
        final RVectorI3F<RSpaceObject> b = bitangents.get(v.getTangent());
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", b.x));
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", b.y));
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", b.z));
        ev.appendChild(eb);
      } else {
        final Element et = new Element("m:t4", uri);
        final RVectorI4F<RSpaceObject> t = tangents.get(v.getTangent());
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", t.x));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", t.y));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", t.z));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:w", t.w));
        ev.appendChild(et);
      }

      final Element eu = new Element("m:u", uri);
      final RVectorI2F<RSpaceTexture> u = uvs.get(v.getUV());
      eu.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", u.x));
      eu.addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", u.y));
      ev.appendChild(eu);

      evs.appendChild(ev);
    }

    return evs;
  }

  private final @Nonnull Log log;

  public MeshTangentsRMXExporter(
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log interface"),
        "mesh-tangents-rmx-exporter");
  }

  @SuppressWarnings("static-method") public @Nonnull Element toXML(
    final @Nonnull MeshTangents m,
    final boolean bitangents)
  {
    final Element e = MeshTangentsRMXExporter.toXMLRoot(m);
    final Element et = MeshTangentsRMXExporter.toXMLType(bitangents);
    final Element ev = MeshTangentsRMXExporter.toXMLVertices(m, bitangents);
    final Element etr = MeshTangentsRMXExporter.toXMLTriangles(m);
    e.appendChild(et);
    e.appendChild(ev);
    e.appendChild(etr);
    return e;
  }
}
