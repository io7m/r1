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

import java.io.IOException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;

public class MeshBasicRMXExporterTest
{
  @SuppressWarnings("static-method") @Test public
    void
    testMeshTriUntextured()
      throws RXMLException,
        ConstraintError,
        IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc =
      ColladaDocumentTest.getDocument("tri_untextured.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("tri_untextured-mesh"));

    final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final Element x = exporter.toXML(m);
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshTriTextured()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument("tri.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("tri_textured-mesh"));

    final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final Element x = exporter.toXML(m);
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshCube()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument("cube.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("cube_textured_mesh-mesh"));

    final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final Element x = exporter.toXML(m);
    MeshBasicRMXExporterTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshCubeOneFace()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument("cube_oneface.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("Cube_001-mesh"));

    final MeshBasicRMXExporter exporter = new MeshBasicRMXExporter(log);
    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final Element x = exporter.toXML(m);
    MeshBasicRMXExporterTest.dumpXML(x);
  }

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
}
