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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nonnull;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Log;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;

/**
 * <p>
 * A program to extract information from the COLLADA data exported by Blender.
 * </p>
 * <p>
 * This code may work on other COLLADA exporters, but the COLLADA
 * specification gives so much freedom that there's really no way to be sure.
 * </p>
 */

public final class BlenderImporter
{
  private static final @Nonnull URI COLLADA_URI;

  static {
    try {
      COLLADA_URI = new URI("http://www.collada.org/2005/11/COLLADASchema");
    } catch (final URISyntaxException e) {
      throw new UnreachableCodeException();
    }
  }

  private static @Nonnull Nodes getGeometries(
    final @Nonnull XPathContext xpc,
    final @Nonnull Document document)
  {
    return document.query("/c:COLLADA/c:library_geometries/c:geometry", xpc);
  }

  private static @Nonnull Element getMeshFromGeometry(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element geometry)
  {
    assert geometry.getLocalName().equals("geometry");

    final Nodes mesh = geometry.query("c:mesh", xpc);
    assert mesh.size() == 1;
    return (Element) mesh.get(0);
  }

  private static @Nonnull Element getNormalArrayElementFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final String vertex_positions_source_name =
      BlenderImporter.getNormalSourceName(xpc, mesh);

    final StringBuilder source_query = new StringBuilder();
    source_query.append("c:source[@id='");
    source_query.append(vertex_positions_source_name);
    source_query.append("']");

    final Nodes vertex_position_source_nodes =
      mesh.query(source_query.toString(), xpc);
    assert vertex_position_source_nodes.size() == 1;
    final Element vertex_position_source =
      (Element) vertex_position_source_nodes.get(0);

    final Elements float_array_nodes =
      vertex_position_source.getChildElements(
        "float_array",
        BlenderImporter.COLLADA_URI.toString());
    assert float_array_nodes.size() == 1;
    return float_array_nodes.get(0);
  }

  private static @Nonnull String[] getNormalArrayFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Element array =
      BlenderImporter.getNormalArrayElementFromMesh(xpc, mesh);
    return array.getValue().split("\\s+");
  }

  private static String getNormalSourceName(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Nodes normal_input_nodes =
      mesh.query("c:polylist/c:input[@semantic='NORMAL']", xpc);
    assert normal_input_nodes.size() == 1;
    final Element input_elem = (Element) normal_input_nodes.get(0);
    assert input_elem != null;
    final Attribute source_attr = input_elem.getAttribute("source");
    assert source_attr != null;
    final String source_name = source_attr.getValue();
    assert source_name.startsWith("#");
    return source_name.substring(1);
  }

  private static @Nonnull Element getPolylistFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Nodes polylist = mesh.query("c:polylist", xpc);
    assert polylist.size() == 1;
    return (Element) polylist.get(0);
  }

  private static @Nonnull String getPolylistMaterialAddress(
    final @Nonnull Element polylist)
  {
    assert polylist.getLocalName().equals("polylist");
    return polylist.getAttribute("material").getValue();
  }

  private static @Nonnull String[] getPolylistPolyArray(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element polylist)
  {
    assert polylist.getLocalName().equals("polylist");
    final String s =
      BlenderImporter.getPolylistPolyArrayElement(xpc, polylist).getValue();
    return s.split("\\s+");
  }

  private static @Nonnull Element getPolylistPolyArrayElement(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element polylist)
  {
    assert polylist.getLocalName().equals("polylist");

    final Nodes poly_array_nodes = polylist.query("c:p", xpc);
    assert poly_array_nodes.size() == 1;
    final Element poly_array = (Element) poly_array_nodes.get(0);
    assert poly_array != null;
    return poly_array;
  }

  private static @Nonnull Element getTexCoordArrayElementFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final String vertex_positions_source_name =
      BlenderImporter.getTexCoordSourceName(xpc, mesh);

    final StringBuilder source_query = new StringBuilder();
    source_query.append("c:source[@id='");
    source_query.append(vertex_positions_source_name);
    source_query.append("']");

    final Nodes vertex_position_source_nodes =
      mesh.query(source_query.toString(), xpc);
    assert vertex_position_source_nodes.size() == 1;
    final Element vertex_position_source =
      (Element) vertex_position_source_nodes.get(0);

    final Elements float_array_nodes =
      vertex_position_source.getChildElements(
        "float_array",
        BlenderImporter.COLLADA_URI.toString());
    assert float_array_nodes.size() == 1;
    return float_array_nodes.get(0);
  }

  private static @Nonnull String[] getTexCoordArrayFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Element array =
      BlenderImporter.getTexCoordArrayElementFromMesh(xpc, mesh);
    return array.getValue().split("\\s+");
  }

  private static String getTexCoordSourceName(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Nodes normal_input_nodes =
      mesh.query("c:polylist/c:input[@semantic='TEXCOORD']", xpc);
    assert normal_input_nodes.size() == 1;
    final Element input_elem = (Element) normal_input_nodes.get(0);
    assert input_elem != null;
    final Attribute source_attr = input_elem.getAttribute("source");
    assert source_attr != null;
    final String source_name = source_attr.getValue();
    assert source_name.startsWith("#");
    return source_name.substring(1);
  }

  private static @Nonnull Element getVertexPositionArrayElementFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final String vertex_positions_source_name =
      BlenderImporter.getVertexPositionSourceName(xpc, mesh);

    final StringBuilder source_query = new StringBuilder();
    source_query.append("c:source[@id='");
    source_query.append(vertex_positions_source_name);
    source_query.append("']");

    final Nodes vertex_position_source_nodes =
      mesh.query(source_query.toString(), xpc);
    assert vertex_position_source_nodes.size() == 1;
    final Element vertex_position_source =
      (Element) vertex_position_source_nodes.get(0);

    final Elements float_array_nodes =
      vertex_position_source.getChildElements(
        "float_array",
        BlenderImporter.COLLADA_URI.toString());
    assert float_array_nodes.size() == 1;
    return float_array_nodes.get(0);
  }

  private static @Nonnull String[] getVertexPositionArrayFromMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Element array =
      BlenderImporter.getVertexPositionArrayElementFromMesh(xpc, mesh);
    return array.getValue().split("\\s+");
  }

  private static @Nonnull String getVertexPositionSourceName(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element mesh)
  {
    assert mesh.getLocalName().equals("mesh");

    final Nodes position_input_nodes =
      mesh.query("c:vertices/c:input[@semantic='POSITION']", xpc);
    assert position_input_nodes.size() == 1;
    final Element input_elem = (Element) position_input_nodes.get(0);
    assert input_elem != null;
    final Attribute source_attr = input_elem.getAttribute("source");
    assert source_attr != null;
    final String source_name = source_attr.getValue();
    assert source_name.startsWith("#");
    return source_name.substring(1);
  }

  private static @Nonnull ColladaVertexType getVertexType(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element polylist)
  {
    final Nodes input_p = polylist.query("c:input[@semantic='VERTEX']", xpc);
    final Nodes input_n = polylist.query("c:input[@semantic='NORMAL']", xpc);
    final Nodes input_uv =
      polylist.query("c:input[@semantic='TEXCOORD']", xpc);

    assert input_p.size() == 1;
    assert (input_n.size() >= 0) && (input_n.size() <= 1);
    assert (input_uv.size() >= 0) && (input_uv.size() <= 1);

    final String offset_ps =
      ((Element) input_p.get(0)).getAttribute("offset").getValue();
    final int offset_p = Integer.valueOf(offset_ps).intValue();

    int offset_n = -1;
    if (input_n.size() > 0) {
      final String offset_ns =
        ((Element) input_n.get(0)).getAttribute("offset").getValue();
      offset_n = Integer.valueOf(offset_ns).intValue();
    }

    int offset_uv = -1;
    if (input_uv.size() > 0) {
      final String offset_uvs =
        ((Element) input_uv.get(0)).getAttribute("offset").getValue();
      offset_uv = Integer.valueOf(offset_uvs).intValue();
    }

    return new ColladaVertexType(offset_p, offset_n, offset_uv);
  }

  static @Nonnull Map<String, ColladaMesh> load(
    final @Nonnull Log log,
    final @Nonnull Document document)
    throws ConstraintError
  {
    Constraints.constrainNotNull(document, "Document");
    Constraints.constrainArbitrary(
      document
        .getRootElement()
        .getNamespaceURI()
        .equals(BlenderImporter.COLLADA_URI.toString()),
      "Document is a COLLADA document");

    final XPathContext xpc =
      new XPathContext("c", BlenderImporter.COLLADA_URI.toString());

    final ColladaAxis axis = BlenderImporter.getAxis(xpc, document, log);

    final Map<String, ColladaMesh> meshes =
      new HashMap<String, ColladaMesh>();

    final Nodes geometries = BlenderImporter.getGeometries(xpc, document);
    for (int index = 0; index < geometries.size(); ++index) {
      final Element geom = (Element) geometries.get(index);
      final ColladaMesh mesh =
        BlenderImporter.processGeometry(xpc, geom, axis, log);
      assert meshes.containsKey(mesh.getName()) == false;
      meshes.put(mesh.getName(), mesh);
    }

    return meshes;
  }

  private static @Nonnull ColladaAxis getAxis(
    final @Nonnull XPathContext xpc,
    final @Nonnull Document document,
    final @Nonnull Log log)
  {
    final Nodes nodes = document.query("/c:COLLADA/c:asset/c:up_axis", xpc);
    assert nodes.size() == 1;
    final Element n = (Element) nodes.get(0);

    final String text = n.getValue();
    log.debug("axis: " + text);

    if (text.equals("X_UP")) {
      return ColladaAxis.COLLADA_AXIS_X_UP;
    }
    if (text.equals("Y_UP")) {
      return ColladaAxis.COLLADA_AXIS_Y_UP;
    }
    return ColladaAxis.COLLADA_AXIS_Z_UP;
  }

  public static void main(
    final String args[])
  {
    if (args.length < 3) {
      System.err.println("usage: collada.conf file.xml output-directory");
      System.exit(1);
    }

    FileInputStream props_in = null;

    try {
      final String path_props = args[0];
      final String path_input = args[1];
      final String path_output = args[2];

      final Builder parser = new Builder();
      final Properties props = new Properties();
      props_in = new FileInputStream(path_props);
      props.load(props_in);
      final Log log = new Log(props, "com.io7m.renderer.xml", "collada");
      final Document document = parser.build(new File(path_input));
      final Map<String, ColladaMesh> meshes =
        BlenderImporter.load(log, document);

      for (final Entry<String, ColladaMesh> e : meshes.entrySet()) {
        final ColladaMesh mesh = e.getValue();
        FileOutputStream out = null;

        try {
          final File out_file =
            new File(new File(path_output), mesh.getName() + ".rmx");
          log.debug("Writing mesh " + mesh.getName() + " to " + out_file);

          out = new FileOutputStream(out_file);
          final ExportableMesh emesh = new ExportableMesh(mesh, log);
          final Serializer serial = new Serializer(out);
          serial.setIndent(2);
          serial.setMaxLength(80);
          serial.write(new Document(emesh.toXML()));
        } finally {
          if (out != null) {
            out.flush();
            out.close();
          }
        }

      }

    } catch (final ValidityException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (final ParsingException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (final IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (final ConstraintError e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (props_in != null) {
        try {
          props_in.close();
        } catch (final IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
  }

  private static void populateNormalArray(
    final @Nonnull XPathContext xpc,
    final @Nonnull ColladaMesh model,
    final @Nonnull Element mesh,
    final @Nonnull Log log)
  {
    assert mesh.getLocalName().equals("mesh");

    final String[] vpa = BlenderImporter.getNormalArrayFromMesh(xpc, mesh);

    assert vpa.length > 0;
    assert (vpa.length % 3) == 0;

    for (int index = 0; index < vpa.length; index += 3) {
      final float x = Float.valueOf(vpa[index + 0]).floatValue();
      final float y = Float.valueOf(vpa[index + 1]).floatValue();
      final float z = Float.valueOf(vpa[index + 2]).floatValue();

      final RVectorI3F<RSpaceObject> n =
        new RVectorI3F<RSpaceObject>(x, y, z);

      model.normalAdd(n);
    }

    log.debug("loaded " + model.normalCount() + " polygon normals");
  }

  private static void populatePolyArray(
    final @Nonnull XPathContext xpc,
    final @Nonnull ColladaMesh mesh,
    final @Nonnull Log log,
    final @Nonnull Element polylist)
  {
    assert polylist.getLocalName().equals("polylist");

    final Attribute count_attr = polylist.getAttribute("count");
    final Integer count = Integer.valueOf(count_attr.getValue());
    log.debug("expecting " + count.toString() + " polygons");

    final String[] indices =
      BlenderImporter.getPolylistPolyArray(xpc, polylist);
    assert indices.length > 0;
    assert (indices.length % 3) == 0;

    final ColladaVertexType type = mesh.getType();
    final int tri_indices = type.getIndicesPerTriangle();
    final int vert_indices = type.getIndicesPerVertex();

    log.debug("vertex position index offset : " + type.getOffsetPosition());
    log.debug("vertex normal index offset   : " + type.getOffsetNormal());
    log.debug("vertex uv index offset       : " + type.getOffsetUV());
    log.debug("triangle indices             : " + tri_indices);
    log.debug("vertex indices               : " + vert_indices);

    for (int offset = 0; offset < indices.length; offset += tri_indices) {
      final int tri0_off = offset;
      final int tri1_off = offset + vert_indices;
      final int tri2_off = offset + vert_indices + vert_indices;

      final int p_offset = type.getOffsetPosition();
      final int p_count = mesh.positionCount();

      final int p0 = Integer.parseInt(indices[tri0_off + p_offset]);
      assert (p0 >= 0) && (p0 < p_count);
      final int p1 = Integer.parseInt(indices[tri1_off + p_offset]);
      assert (p1 >= 0) && (p1 < p_count);
      final int p2 = Integer.parseInt(indices[tri2_off + p_offset]);
      assert (p2 >= 0) && (p2 < p_count);

      int n0 = -1;
      int n1 = -1;
      int n2 = -1;

      final int n_offset = type.getOffsetNormal();
      if (n_offset != -1) {
        final int n_count = mesh.normalCount();
        n0 = Integer.parseInt(indices[tri0_off + n_offset]);
        assert (n0 >= 0) && (n0 < n_count);
        n1 = Integer.parseInt(indices[tri1_off + n_offset]);
        assert (n1 >= 0) && (n1 < n_count);
        n2 = Integer.parseInt(indices[tri2_off + n_offset]);
        assert (n2 >= 0) && (n2 < n_count);
      }

      int u0 = -1;
      int u1 = -1;
      int u2 = -1;

      final int u_offset = type.getOffsetUV();
      if (u_offset != -1) {
        final int u_count = mesh.uvCount();
        u0 = Integer.parseInt(indices[tri0_off + u_offset]);
        assert (u0 >= 0) && (u0 < u_count);
        u1 = Integer.parseInt(indices[tri1_off + u_offset]);
        assert (u1 >= 0) && (u1 < u_count);
        u2 = Integer.parseInt(indices[tri2_off + u_offset]);
        assert (u2 >= 0) && (u2 < u_count);
      }

      final ColladaPoly p =
        new ColladaPoly(p0, p1, p2, n0, n1, n2, u0, u1, u2);
      mesh.polygonAdd(p);
    }

    log.debug("loaded " + mesh.polygonCount() + " polygons");
    assert mesh.polygonCount() == count.intValue();
  }

  private static void populateTexCoordArray(
    final @Nonnull XPathContext xpc,
    final @Nonnull ColladaMesh model,
    final @Nonnull Element mesh,
    final @Nonnull Log log)
  {
    assert mesh.getLocalName().equals("mesh");

    final String[] vpa = BlenderImporter.getTexCoordArrayFromMesh(xpc, mesh);

    assert vpa.length > 0;
    assert (vpa.length % 2) == 0;

    for (int index = 0; index < vpa.length; index += 2) {
      final float s = Float.valueOf(vpa[index + 0]).floatValue();
      final float t = Float.valueOf(vpa[index + 1]).floatValue();
      final RVectorI2F<RSpaceTexture> uv =
        new RVectorI2F<RSpaceTexture>(s, t);
      model.uvAdd(uv);
    }

    log.debug("loaded " + model.uvCount() + " vertex texture coordinates");
  }

  private static void populateVertexArray(
    final @Nonnull XPathContext xpc,
    final @Nonnull ColladaMesh model,
    final @Nonnull Element mesh,
    final @Nonnull Log log)
  {
    assert mesh.getLocalName().equals("mesh");

    final String[] vpa =
      BlenderImporter.getVertexPositionArrayFromMesh(xpc, mesh);

    assert vpa.length > 0;
    assert (vpa.length % 3) == 0;

    for (int index = 0; index < vpa.length; index += 3) {
      final float x = Float.valueOf(vpa[index + 0]).floatValue();
      final float y = Float.valueOf(vpa[index + 1]).floatValue();
      final float z = Float.valueOf(vpa[index + 2]).floatValue();

      final RVectorI3F<RSpaceObject> v =
        new RVectorI3F<RSpaceObject>(x, y, z);

      model.positionAdd(v);
    }

    log.debug("loaded " + model.positionCount() + " vertex positions");
  }

  private static @Nonnull ColladaMesh processGeometry(
    final @Nonnull XPathContext xpc,
    final @Nonnull Element geometry,
    final @Nonnull ColladaAxis axis,
    final @Nonnull Log log)
  {
    assert geometry.getLocalName().equals("geometry");

    final Attribute name_attr = geometry.getAttribute("name");
    assert name_attr != null;
    final String name = name_attr.getValue();
    log.debug("loaded model named '" + name + "'");

    final Element mesh = BlenderImporter.getMeshFromGeometry(xpc, geometry);
    return BlenderImporter.processMesh(xpc, name, mesh, axis, log);
  }

  private static @Nonnull ColladaMesh processMesh(
    final @Nonnull XPathContext xpc,
    final @Nonnull String name,
    final @Nonnull Element e,
    final @Nonnull ColladaAxis axis,
    final @Nonnull Log log)
  {
    assert e.getLocalName().equals("mesh");

    final Element polylist = BlenderImporter.getPolylistFromMesh(xpc, e);

    final ColladaVertexType vertex_type =
      BlenderImporter.getVertexType(xpc, polylist);

    final @Nonnull ColladaMesh mesh =
      new ColladaMesh(name, vertex_type, axis);

    BlenderImporter.populateVertexArray(xpc, mesh, e, log);
    if (vertex_type.hasNormal()) {
      BlenderImporter.populateNormalArray(xpc, mesh, e, log);
    }
    if (vertex_type.hasUV()) {
      BlenderImporter.populateTexCoordArray(xpc, mesh, e, log);
    }

    BlenderImporter.populatePolyArray(xpc, mesh, log, polylist);
    return mesh;
  }

  private BlenderImporter()
  {
    throw new UnreachableCodeException();
  }
}
