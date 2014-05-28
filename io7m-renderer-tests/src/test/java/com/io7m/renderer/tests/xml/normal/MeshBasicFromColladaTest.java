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

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jlog.LogType;
import com.io7m.jnull.NonNull;
import com.io7m.renderer.meshes.MeshBasic;
import com.io7m.renderer.tests.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMeshMissingUVs;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;
import com.io7m.renderer.xml.tools.MeshBasicColladaImporter;

@SuppressWarnings("static-method") public class MeshBasicFromColladaTest
{
  private static @NonNull MeshBasic makeMeshBasic(
    final @NonNull String file,
    final @NonNull ColladaGeometryID geo)
    throws RException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final Document doc = ColladaDocumentTest.getDocument(file);
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom = cd.getGeometry(geo);
    assert geom != null;

    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    return m;
  }

  @Test public void testMeshCubeOneFace()
    throws RException
  {
    final MeshBasic m =
      MeshBasicFromColladaTest.makeMeshBasic(
        "cube_oneface.dae",
        new ColladaGeometryID("Cube_001-mesh"));

    Assert.assertEquals(8, m.positionsGet().size());
    Assert.assertEquals(36, m.verticesGet().size());
    Assert.assertEquals(12, m.normalsGet().size());
    Assert.assertEquals(36, m.uvsGet().size());
    Assert.assertEquals(12, m.trianglesGet().size());
  }

  @Test public void testMeshCylinder()
    throws RException
  {
    final MeshBasic m =
      MeshBasicFromColladaTest.makeMeshBasic(
        "cylinder.dae",
        new ColladaGeometryID("cylinder_textured-mesh"));

    Assert.assertEquals(32, m.positionsGet().size());
    Assert.assertEquals(180, m.verticesGet().size());
    Assert.assertEquals(60, m.normalsGet().size());
    Assert.assertEquals(180, m.uvsGet().size());
    Assert.assertEquals(60, m.trianglesGet().size());
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public void testMeshHex()
    throws RException
  {
    final MeshBasic m =
      MeshBasicFromColladaTest.makeMeshBasic(
        "hex.dae",
        new ColladaGeometryID("Cylinder-mesh"));

    Assert.assertEquals(12, m.positionsGet().size());
    Assert.assertEquals(60, m.verticesGet().size());
    Assert.assertEquals(20, m.normalsGet().size());
    Assert.assertEquals(0, m.uvsGet().size());
    Assert.assertEquals(20, m.trianglesGet().size());
  }

  @Test public void testMeshTriTextured()
    throws RException
  {
    final MeshBasic m =
      MeshBasicFromColladaTest.makeMeshBasic(
        "tri.dae",
        new ColladaGeometryID("tri_textured-mesh"));

    Assert.assertEquals(3, m.positionsGet().size());
    Assert.assertEquals(3, m.verticesGet().size());
    Assert.assertEquals(1, m.normalsGet().size());
    Assert.assertEquals(3, m.uvsGet().size());
    Assert.assertEquals(1, m.trianglesGet().size());
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public
    void
    testMeshTriUntextured()
      throws RException
  {
    final MeshBasic m =
      MeshBasicFromColladaTest.makeMeshBasic(
        "tri_untextured.dae",
        new ColladaGeometryID("tri_untextured-mesh"));

    Assert.assertEquals(3, m.positionsGet().size());
    Assert.assertEquals(3, m.verticesGet().size());
    Assert.assertEquals(1, m.normalsGet().size());
    Assert.assertEquals(0, m.uvsGet().size());
    Assert.assertEquals(1, m.trianglesGet().size());
  }
}
