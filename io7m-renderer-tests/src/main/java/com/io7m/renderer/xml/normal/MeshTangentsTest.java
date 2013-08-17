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
import java.util.List;

import javax.annotation.Nonnull;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.AlmostEqualFloat;
import com.io7m.jaux.AlmostEqualFloat.ContextRelative;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;
import com.io7m.renderer.xml.collada.ColladaDocument;
import com.io7m.renderer.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.xml.collada.ColladaGeometry;
import com.io7m.renderer.xml.collada.ColladaGeometryID;

public class MeshTangentsTest
{
  private static @Nonnull String floatString(
    final float x)
  {
    return String.format("%.8f", x);
  }

  private static @Nonnull MeshTangents makeAndCheckTangents(
    final @Nonnull Log log,
    final @Nonnull String file,
    final @Nonnull ColladaGeometryID geo)
    throws RXMLException,
      ConstraintError
  {
    final Document doc = ColladaDocumentTest.getDocument(file);
    final ColladaDocument cd = ColladaDocument.newDocument(doc, log);
    final ColladaGeometry geom = cd.getGeometry(geo);

    final MeshBasicColladaImporter importer =
      new MeshBasicColladaImporter(log);
    final MeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final MeshTangents mt = MeshTangents.makeWithTangents(m);

    final List<RVectorI3F<RSpaceObject>> normals = mt.normalsGet();
    final List<RVectorI3F<RSpaceObject>> tangents = mt.tangentsGet();
    final List<RVectorI3F<RSpaceObject>> bitangents = mt.bitangentsGet();

    final ContextRelative context = new ContextRelative();
    context.setMaxAbsoluteDifference(0.000001f);

    for (int index = 0; index < normals.size(); ++index) {
      final RVectorI3F<RSpaceObject> n = normals.get(index);
      final RVectorI3F<RSpaceObject> t = tangents.get(index);
      final RVectorI3F<RSpaceObject> b = bitangents.get(index);

      /**
       * Normals, tangents, and bitangents are all unit length.
       */

      final float nm = VectorI3F.magnitude(n);
      final float tm = VectorI3F.magnitude(t);
      final float bm = VectorI3F.magnitude(b);

      /**
       * Normals, tangents, and bitangents are all perpendicular.
       */

      final float ntd = VectorI3F.dotProduct(n, t);
      final float tbd = VectorI3F.dotProduct(t, b);
      final float nbd = VectorI3F.dotProduct(n, b);

      /**
       * The handedness of the vectors is consistently right handed.
       */

      final float dot_crossNT_B =
        VectorI3F.dotProduct(VectorI3F.crossProduct(n, t), b);

      System.out.println("--");
      System.out.println("N                 : " + n);
      System.out.println("T                 : " + t);
      System.out.println("B                 : " + b);
      System.out.println("N magnitude       : "
        + MeshTangentsTest.floatString(nm));
      System.out.println("T magnitude       : "
        + MeshTangentsTest.floatString(tm));
      System.out.println("B magnitude       : "
        + MeshTangentsTest.floatString(bm));
      System.out.println("N dot T           : "
        + MeshTangentsTest.floatString(ntd));
      System.out.println("T dot B           : "
        + MeshTangentsTest.floatString(tbd));
      System.out.println("N dot B           : "
        + MeshTangentsTest.floatString(nbd));
      System.out.println("dot (cross N T) B : " + dot_crossNT_B);

      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, nm, 1.0f));
      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, tm, 1.0f));
      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, bm, 1.0f));

      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, ntd, 0.0f));
      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, tbd, 0.0f));
      Assert.assertTrue(AlmostEqualFloat.almostEqual(context, nbd, 0.0f));

      Assert.assertTrue(AlmostEqualFloat.almostEqual(
        context,
        dot_crossNT_B,
        1.0f));
    }

    return mt;
  }

  @SuppressWarnings("static-method") @Test public void testMeshTriTextured()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final MeshTangentsRMXExporter exporter = new MeshTangentsRMXExporter(log);
    final MeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "tri.dae",
        new ColladaGeometryID("tri_textured-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshCube()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final MeshTangentsRMXExporter exporter = new MeshTangentsRMXExporter(log);
    final MeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cube.dae",
        new ColladaGeometryID("cube_textured_mesh-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshCubeOneFace()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final MeshTangentsRMXExporter exporter = new MeshTangentsRMXExporter(log);
    final MeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cube_oneface.dae",
        new ColladaGeometryID("Cube_001-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @SuppressWarnings("static-method") @Test public void testMeshCylinder()
    throws RXMLException,
      ConstraintError,
      IOException
  {
    final Log log = ColladaDocumentTest.getLog();
    final MeshTangentsRMXExporter exporter = new MeshTangentsRMXExporter(log);
    final MeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cylinder.dae",
        new ColladaGeometryID("cylinder_textured-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
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
