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

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;

public class MeshBasicFromColladaTest
{
  @SuppressWarnings("static-method") @Test public
    void
    testMeshTriUntextured()
      throws RXMLException,
        ConstraintError
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc =
      ColladaDocumentTest.getDocument("tri_untextured.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("tri_untextured-mesh"));

    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);

    Assert.assertEquals(3, m.verticesGet().size());
    Assert.assertEquals(1, m.normalsGet().size());
    Assert.assertEquals(0, m.uvsGet().size());
    Assert.assertEquals(1, m.trianglesGet().size());
  }

  @SuppressWarnings("static-method") @Test public void testMeshTriTextured()
    throws RXMLException,
      ConstraintError
  {
    final Log log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument("tri.dae");
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom =
      cd.getGeometry(new ColladaGeometryID("tri_textured-mesh"));

    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);

    Assert.assertEquals(3, m.verticesGet().size());
    Assert.assertEquals(1, m.normalsGet().size());
    Assert.assertEquals(3, m.uvsGet().size());
    Assert.assertEquals(1, m.trianglesGet().size());
  }
}
