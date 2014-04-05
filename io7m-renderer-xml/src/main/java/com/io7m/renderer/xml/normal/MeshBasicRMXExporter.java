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
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.xml.normal.MeshBasic.Triangle;
import com.io7m.renderer.xml.normal.MeshBasic.Vertex;
import com.io7m.renderer.xml.rmx.RXMLConstants;

public class MeshBasicRMXExporter
{
  private static @Nonnull Attribute floatAttribute(
    final @Nonnull String name,
    final float x)
  {
    return new Attribute(
      name,
      RXMLConstants.MESHES_URI.toString(),
      MeshBasicRMXExporter.floatString(x));
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
    final @Nonnull MeshBasic m)
  {
    final Element e =
      new Element("m:mesh", RXMLConstants.MESHES_URI.toString());
    e.addAttribute(MeshBasicRMXExporter.intAttribute(
      "m:version",
      RXMLConstants.MESHES_VERSION));
    e
      .addAttribute(MeshBasicRMXExporter.stringAttribute(
        "m:name",
        m.getName()));
    return e;
  }

  private static @Nonnull Element toXMLTriangles(
    final @Nonnull MeshBasic m)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element ets = new Element("m:triangles", uri);
    final List<Triangle> triangles = m.trianglesGet();
    ets.addAttribute(MeshBasicRMXExporter.intAttribute(
      "m:count",
      triangles.size()));

    for (final Triangle t : triangles) {
      final Element et = new Element("m:tri", uri);
      et.addAttribute(MeshBasicRMXExporter.intAttribute("m:v0", t.getV0()));
      et.addAttribute(MeshBasicRMXExporter.intAttribute("m:v1", t.getV1()));
      et.addAttribute(MeshBasicRMXExporter.intAttribute("m:v2", t.getV2()));
      ets.appendChild(et);
    }

    return ets;
  }

  private static @Nonnull Element toXMLType(
    final @Nonnull MeshBasic m)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final Element et = new Element("m:type", uri);
    et.appendChild(new Element("m:attribute-position-3f", uri));
    et.appendChild(new Element("m:attribute-normal-3f", uri));
    if (m.hasUV()) {
      et.appendChild(new Element("m:attribute-uv-2f", uri));
    }
    return et;
  }

  private static @Nonnull Element toXMLVertices(
    final @Nonnull MeshBasic m)
  {
    final String uri = RXMLConstants.MESHES_URI.toString();
    final List<Vertex> vertices = m.verticesGet();
    final List<RVectorI3F<RSpaceObjectType>> positions = m.positionsGet();
    final List<RVectorI3F<RSpaceObjectType>> normals = m.normalsGet();
    final List<RVectorI2F<RSpaceTextureType>> uvs = m.uvsGet();

    final Element evs = new Element("m:vertices", uri);
    evs.addAttribute(MeshBasicRMXExporter.intAttribute(
      "m:count",
      vertices.size()));

    for (final Vertex v : vertices) {
      final Element ev = new Element("m:v", uri);

      final Element ep = new Element("m:p", uri);
      final RVectorI3F<RSpaceObjectType> p = positions.get(v.getPosition());
      ep.addAttribute(MeshBasicRMXExporter.floatAttribute("m:x", p.x));
      ep.addAttribute(MeshBasicRMXExporter.floatAttribute("m:y", p.y));
      ep.addAttribute(MeshBasicRMXExporter.floatAttribute("m:z", p.z));
      ev.appendChild(ep);

      final Element en = new Element("m:n", uri);
      final RVectorI3F<RSpaceObjectType> n = normals.get(v.getNormal());
      en.addAttribute(MeshBasicRMXExporter.floatAttribute("m:x", n.x));
      en.addAttribute(MeshBasicRMXExporter.floatAttribute("m:y", n.y));
      en.addAttribute(MeshBasicRMXExporter.floatAttribute("m:z", n.z));
      ev.appendChild(en);

      if (m.hasUV()) {
        final Element eu = new Element("m:u", uri);
        final RVectorI2F<RSpaceTextureType> u = uvs.get(v.getUV());
        eu.addAttribute(MeshBasicRMXExporter.floatAttribute("m:x", u.x));
        eu.addAttribute(MeshBasicRMXExporter.floatAttribute("m:y", u.y));
        ev.appendChild(eu);
      }

      evs.appendChild(ev);
    }

    return evs;
  }

  private final @Nonnull Log log;

  public MeshBasicRMXExporter(
    final @Nonnull Log in_log)
    throws ConstraintError
  {
    this.log =
      new Log(
        Constraints.constrainNotNull(in_log, "Log interface"),
        "mesh-basic-rmx-exporter");
  }

  @SuppressWarnings("static-method") public @Nonnull Element toXML(
    final @Nonnull MeshBasic m)
  {
    final Element e = MeshBasicRMXExporter.toXMLRoot(m);
    final Element et = MeshBasicRMXExporter.toXMLType(m);
    final Element ev = MeshBasicRMXExporter.toXMLVertices(m);
    final Element etr = MeshBasicRMXExporter.toXMLTriangles(m);
    e.appendChild(et);
    e.appendChild(ev);
    e.appendChild(etr);
    return e;
  }
}
