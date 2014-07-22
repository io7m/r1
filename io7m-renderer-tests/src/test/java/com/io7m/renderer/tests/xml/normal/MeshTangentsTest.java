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
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jequality.AlmostEqualFloat;
import com.io7m.jequality.AlmostEqualFloat.ContextRelative;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.meshes.RMeshBasic;
import com.io7m.renderer.meshes.RMeshTangents;
import com.io7m.renderer.tests.xml.collada.ColladaDocumentTest;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.xml.collada.RColladaDocument;
import com.io7m.renderer.xml.collada.RColladaGeometry;
import com.io7m.renderer.xml.collada.RColladaGeometryID;
import com.io7m.renderer.xml.collada.tools.RColladaToMeshBasic;
import com.io7m.renderer.xml.rmx.RXMLExporter;

@SuppressWarnings({ "boxing", "static-method" }) public class MeshTangentsTest
{
  private static @NonNull String floatString(
    final float x)
  {
    final String r = String.format("%.8f", x);
    assert r != null;
    return r;
  }

  private static @NonNull RMeshTangents makeAndCheckTangents(
    final @NonNull LogUsableType log,
    final @NonNull String file,
    final @NonNull RColladaGeometryID geo)
    throws RException
  {
    final Document doc = ColladaDocumentTest.getDocument(file);
    final RColladaDocument cd = RColladaDocument.newDocument(doc, log);
    final RColladaGeometry geom = cd.getGeometry(geo);
    assert geom != null;

    final RColladaToMeshBasic importer =
      new RColladaToMeshBasic(log);
    final RMeshBasic m = importer.newMeshFromColladaGeometry(cd, geom);
    final RMeshTangents mt = RMeshTangents.makeWithTangents(m);

    final List<RVectorI3F<RSpaceObjectType>> normals = mt.normalsGet();
    final List<RVectorI4F<RSpaceObjectType>> tangents = mt.tangentsGet();
    final List<RVectorI3F<RSpaceObjectType>> bitangents = mt.bitangentsGet();

    final ContextRelative context = new ContextRelative();
    context.setMaxAbsoluteDifference(0.0009f);

    for (int index = 0; index < normals.size(); ++index) {
      final RVectorI3F<RSpaceObjectType> n = normals.get(index);
      final RVectorI4F<RSpaceObjectType> t = tangents.get(index);
      final RVectorI3F<RSpaceObjectType> b = bitangents.get(index);
      assert n != null;
      assert t != null;
      assert b != null;

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

  @Test public void testMeshTriTextured()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "tri.dae",
        new RColladaGeometryID("tri_textured-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshCube()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cube.dae",
        new RColladaGeometryID("cube_textured_mesh-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshCubeOneFace()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cube_oneface.dae",
        new RColladaGeometryID("Cube_001-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshCylinder()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "cylinder.dae",
        new RColladaGeometryID("cylinder_textured-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshMonkeys()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "monkeys.dae",
        new RColladaGeometryID("monkey_textured_mesh-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshSphere()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "sphere.dae",
        new RColladaGeometryID("sphere_16_8_textured-mesh"));

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

  @Test public void testMeshTriLH()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "tri_LHRH.dae",
        new RColladaGeometryID("tri_lh-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }

  @Test public void testMeshTriRH()
    throws RException,
      IOException
  {
    final LogType log = ColladaDocumentTest.getLog();
    final RXMLExporter exporter = new RXMLExporter(log);
    final RMeshTangents mt =
      MeshTangentsTest.makeAndCheckTangents(
        log,
        "tri_LHRH.dae",
        new RColladaGeometryID("tri_rh-mesh"));

    final Element x = exporter.toXML(mt);
    MeshTangentsTest.dumpXML(x);
  }
}
