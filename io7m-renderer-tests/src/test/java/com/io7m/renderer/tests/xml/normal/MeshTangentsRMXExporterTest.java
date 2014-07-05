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

package com.io7m.renderer.tests.xml.normal;

import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Test;

import com.io7m.jlog.LogType;
import com.io7m.jnull.NonNull;
import com.io7m.renderer.meshes.RMeshBasic;
import com.io7m.renderer.meshes.RMeshTangents;
import com.io7m.renderer.tests.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMeshMissingUVs;
import com.io7m.renderer.xml.collada.RColladaDocument;
import com.io7m.renderer.xml.collada.RColladaGeometry;
import com.io7m.renderer.xml.collada.RColladaGeometryID;
import com.io7m.renderer.xml.collada.tools.MeshBasicColladaImporter;
import com.io7m.renderer.xml.rmx.RXMLExporter;

@SuppressWarnings("static-method") public class MeshTangentsRMXExporterTest
{
  private static void dumpXML(
    final Element x)
    throws IOException
  {
    final Document doc = new Document(x);
    final Serializer s = new Serializer(System.out);
    s.setIndent(2);
    s.setLineSeparator("\n");
    s.setMaxLength(80);
    s.write(doc);
  }

  private static @NonNull Element makeMesh(
    final @NonNull String file,
    final @NonNull RColladaGeometryID geo)
    throws RException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument(file);
    final RColladaDocument cd = RColladaDocument.newDocument(doc, log);
    final RColladaGeometry geom = cd.getGeometry(geo);
    assert geom != null;

    final RXMLExporter exporter = new RXMLExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final RMeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final RMeshTangents mt = RMeshTangents.makeWithTangents(m);
    final Element x = exporter.toXML(mt);
    return x;
  }

  @Test public void testMeshCube()
    throws RException,
      IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh("cube.dae", new RColladaGeometryID(
        "cube_textured_mesh-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshCubeOneFace()
    throws RException,
      IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh(
        "cube_oneface.dae",
        new RColladaGeometryID("Cube_001-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshCylinder()
    throws RException,
      IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh(
        "cylinder.dae",
        new RColladaGeometryID("cylinder_textured-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public void testMeshHex()
    throws RException,
      IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh("hex.dae", new RColladaGeometryID(
        "Cylinder-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshTriTextured()
    throws RException,
      IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh("tri.dae", new RColladaGeometryID(
        "tri_textured-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public
    void
    testMeshTriUntextured()
      throws RException,
        IOException
  {
    final Element x =
      MeshTangentsRMXExporterTest.makeMesh(
        "tri_untextured.dae",
        new RColladaGeometryID("tri_untextured-mesh"));
    MeshTangentsRMXExporterTest.dumpXML(x);
  }
}
