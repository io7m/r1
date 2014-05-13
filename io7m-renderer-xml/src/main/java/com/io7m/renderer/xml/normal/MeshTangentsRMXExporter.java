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

package com.io7m.renderer.xml.normal;

import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.xml.normal.MeshTangents.Triangle;
import com.io7m.renderer.xml.normal.MeshTangents.Vertex;
import com.io7m.renderer.xml.rmx.RXMLConstants;

public class MeshTangentsRMXExporter
{
  private static Attribute floatAttribute(
    final String name,
    final float x)
  {
    return new Attribute(
      name,
      RXMLConstants.MESHES_URI.toString(),
      MeshTangentsRMXExporter.floatString(x));
  }

  @SuppressWarnings("boxing") private static String floatString(
    final float x)
  {
    return String.format("%.8f", x);
  }

  private static Attribute intAttribute(
    final String name,
    final int x)
  {
    return new Attribute(
      name,
      RXMLConstants.MESHES_URI.toString(),
      Integer.toString(x));
  }

  private static Attribute stringAttribute(
    final String name,
    final String x)
  {
    return new Attribute(name, RXMLConstants.MESHES_URI.toString(), x);
  }

  private static Element toXMLRoot(
    final MeshTangents m)
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

  private static Element toXMLTriangles(
    final MeshTangents m)
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

  private static Element toXMLType(
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

  private static Element toXMLVertices(
    final MeshTangents m,
    final boolean write_bitangents)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final List<Vertex> vertices = m.verticesGet();
    final List<RVectorI3F<RSpaceObjectType>> positions = m.positionsGet();
    final List<RVectorI3F<RSpaceObjectType>> normals = m.normalsGet();
    final List<RVectorI2F<RSpaceTextureType>> uvs = m.uvsGet();
    final List<RVectorI4F<RSpaceObjectType>> tangents = m.tangentsGet();
    final List<RVectorI3F<RSpaceObjectType>> bitangents = m.bitangentsGet();

    final Element evs = new Element("m:vertices", uri);
    evs.addAttribute(MeshTangentsRMXExporter.intAttribute(
      "m:count",
      vertices.size()));

    for (final Vertex v : vertices) {
      final Element ev = new Element("m:v", uri);

      final Element ep = new Element("m:p", uri);
      final RVectorI3F<RSpaceObjectType> p = positions.get(v.getPosition());
      ep
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", p.getXF()));
      ep
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", p.getYF()));
      ep
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", p.getZF()));
      ev.appendChild(ep);

      final Element en = new Element("m:n", uri);
      final RVectorI3F<RSpaceObjectType> n = normals.get(v.getNormal());
      en
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", n.getXF()));
      en
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", n.getYF()));
      en
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:z", n.getZF()));
      ev.appendChild(en);

      if (write_bitangents) {
        final Element et = new Element("m:t3", uri);
        final RVectorI4F<RSpaceObjectType> t = tangents.get(v.getTangent());
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:x",
          t.getXF()));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:y",
          t.getYF()));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:z",
          t.getZF()));
        ev.appendChild(et);

        final Element eb = new Element("m:b", uri);
        final RVectorI3F<RSpaceObjectType> b = bitangents.get(v.getTangent());
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:x",
          b.getXF()));
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:y",
          b.getYF()));
        eb.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:z",
          b.getZF()));
        ev.appendChild(eb);
      } else {
        final Element et = new Element("m:t4", uri);
        final RVectorI4F<RSpaceObjectType> t = tangents.get(v.getTangent());
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:x",
          t.getXF()));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:y",
          t.getYF()));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:z",
          t.getZF()));
        et.addAttribute(MeshTangentsRMXExporter.floatAttribute(
          "m:w",
          t.getWF()));
        ev.appendChild(et);
      }

      final Element eu = new Element("m:u", uri);
      final RVectorI2F<RSpaceTextureType> u = uvs.get(v.getUV());
      eu
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:x", u.getXF()));
      eu
        .addAttribute(MeshTangentsRMXExporter.floatAttribute("m:y", u.getYF()));
      ev.appendChild(eu);

      evs.appendChild(ev);
    }

    return evs;
  }

  private final LogUsableType log;

  public MeshTangentsRMXExporter(
    final LogUsableType in_log)
  {
    this.log =
      NullCheck.notNull(in_log, "Log interface").with(
        "mesh-tangents-rmx-exporter");
  }

  @SuppressWarnings("static-method") public Element toXML(
    final MeshTangents m,
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
