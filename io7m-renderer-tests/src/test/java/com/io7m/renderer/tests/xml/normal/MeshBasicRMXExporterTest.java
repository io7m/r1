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
import com.io7m.renderer.tests.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.types.RXMLException;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;
import com.io7m.renderer.xml.normal.MeshBasic;
import com.io7m.renderer.xml.normal.MeshBasicColladaImporter;
import com.io7m.renderer.xml.normal.MeshBasicRMXExporter;

@SuppressWarnings("static-method") public class MeshBasicRMXExporterTest
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
    final @NonNull ColladaGeometryID geo)
    throws RXMLException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument(file);
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom = cd.getGeometry(geo);
    assert geom != null;

    final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final Element x = exporter.toXML(m);
    return x;
  }

  @Test public void testMeshCube()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh("cube.dae", new ColladaGeometryID(
        "cube_textured_mesh-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshCubeOneFace()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh(
        "cube_oneface.dae",
        new ColladaGeometryID("Cube_001-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshCylinder()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh(
        "cylinder.dae",
        new ColladaGeometryID("cylinder_textured-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshHex()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh("hex.dae", new ColladaGeometryID(
        "Cylinder-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshTriTextured()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh("tri.dae", new ColladaGeometryID(
        "tri_textured-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @Test public void testMeshTriUntextured()
    throws RXMLException,
      IOException
  {
    final Element x =
      MeshBasicRMXExporterTest.makeMesh(
        "tri_untextured.dae",
        new ColladaGeometryID("tri_untextured-mesh"));
    MeshBasicRMXExporterTest.dumpXML(x);
  }
}
