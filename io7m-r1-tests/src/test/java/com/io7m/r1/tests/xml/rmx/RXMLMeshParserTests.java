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

package com.io7m.r1.tests.xml.rmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jequality.AlmostEqualFloat.ContextRelative;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.meshes.RMeshParserEventsType;
import com.io7m.r1.spaces.RSpaceObjectType;
import com.io7m.r1.spaces.RSpaceTextureType;
import com.io7m.r1.xml.RXMLException;
import com.io7m.r1.xml.rmx.RXMLMeshAttribute;
import com.io7m.r1.xml.rmx.RXMLMeshDocument;
import com.io7m.r1.xml.rmx.RXMLMeshParser;

@SuppressWarnings("synthetic-access") public final class RXMLMeshParserTests
{
  static class Checked implements RMeshParserEventsType<Throwable>
  {
    private final @NonNull PVectorI3F<RSpaceObjectType> expected_lower;
    private final @NonNull String                       expected_name;
    private final int                                   expected_triangles;
    private final @NonNull PVectorI3F<RSpaceObjectType> expected_upper;
    private final int                                   expected_vertices;
    private boolean                                     mesh_ended;
    private boolean                                     mesh_started;
    private boolean                                     mesh_vertex_ended;
    private int                                         triangle = 0;
    private boolean                                     triangle_called;
    private boolean                                     triangles_ended;
    private boolean                                     triangles_started;
    private boolean                                     vertex_normal_called;
    private boolean                                     vertex_position_called;
    private boolean                                     vertex_started_called;
    private boolean                                     vertex_tangent4f_called;
    private boolean                                     vertex_uv_called;
    private int                                         vertices = 0;
    private boolean                                     vertices_ended;
    private boolean                                     vertices_started;

    Checked(
      final @NonNull String in_expected_name,
      final int in_expected_triangles,
      final int in_vertices_expected,
      final @NonNull PVectorI3F<RSpaceObjectType> in_expected_lower,
      final @NonNull PVectorI3F<RSpaceObjectType> in_expected_upper)
    {
      this.expected_name = in_expected_name;
      this.expected_triangles = in_expected_triangles;
      this.expected_vertices = in_vertices_expected;
      this.expected_lower = in_expected_lower;
      this.expected_upper = in_expected_upper;
    }

    @Override public void eventError(
      final Exception e)
      throws Throwable
    {
      throw new AssertionError(e);
    }

    @Override public void eventMeshEnded()
      throws Throwable
    {
      this.mesh_ended = true;
    }

    @Override public void eventMeshName(
      final String name)
      throws Throwable
    {
      Assert.assertEquals(this.expected_name, name);
    }

    @Override public void eventMeshStarted()
      throws Throwable
    {
      this.mesh_started = true;
    }

    @Override public void eventMeshTriangle(
      final long index,
      final long v0,
      final long v1,
      final long v2)
      throws Throwable
    {
      this.triangle_called = true;
      ++this.triangle;
    }

    @Override public void eventMeshTrianglesEnded()
      throws Throwable
    {
      this.triangles_ended = true;
      Assert.assertEquals(this.expected_triangles, this.triangle);
    }

    @Override public void eventMeshTrianglesStarted(
      final long count)
      throws Throwable
    {
      this.triangles_started = true;
      Assert.assertEquals(this.expected_triangles, count);
    }

    @Override public void eventMeshVertexEnded(
      final long index)
      throws Throwable
    {
      this.mesh_vertex_ended = true;
    }

    @Override public void eventMeshVertexNormal(
      final long index,
      final PVectorI3F<RSpaceObjectType> normal)
      throws Throwable
    {
      this.vertex_normal_called = true;
    }

    @Override public void eventMeshVertexPosition(
      final long index,
      final PVectorI3F<RSpaceObjectType> position)
      throws Throwable
    {
      this.vertex_position_called = true;
    }

    @Override public void eventMeshVertexStarted(
      final long index)
      throws Throwable
    {
      this.vertex_started_called = true;
      ++this.vertices;
    }

    @Override public void eventMeshVertexTangent4f(
      final long index,
      final PVectorI4F<RSpaceObjectType> tangent)
      throws Throwable
    {
      this.vertex_tangent4f_called = true;
    }

    @Override public void eventMeshVertexUV(
      final long index,
      final PVectorI2F<RSpaceTextureType> uv)
      throws Throwable
    {
      this.vertex_uv_called = true;
    }

    @Override public void eventMeshVerticesEnded(
      final PVectorI3F<RSpaceObjectType> lower,
      final PVectorI3F<RSpaceObjectType> upper)
      throws Throwable
    {
      this.vertices_ended = true;
      Assert.assertEquals(this.expected_vertices, this.vertices);

      System.out.println("Expected lower bound : " + this.expected_lower);
      System.out.println("Got lower bound      : " + lower);
      System.out.println("Expected upper bound : " + this.expected_upper);
      System.out.println("Got upper bound      : " + upper);

      final ContextRelative context = new ContextRelative();
      context.setMaxAbsoluteDifference(0.00000001f);

      Assert.assertTrue(VectorI3F.almostEqual(
        context,
        this.expected_lower,
        lower));
      Assert.assertTrue(VectorI3F.almostEqual(
        context,
        this.expected_upper,
        upper));
    }

    @Override public void eventMeshVerticesStarted(
      final long count)
      throws Throwable
    {
      this.vertices_started = true;
      Assert.assertEquals(this.expected_vertices, count);
    }
  }

  class Show implements RMeshParserEventsType<Throwable>
  {
    @Override public void eventError(
      final Exception e)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshEnded()
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshName(
      final String name)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshStarted()
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshTriangle(
      final long index,
      final long v0,
      final long v1,
      final long v2)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesEnded()
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesStarted(
      final long count)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexEnded(
      final long index)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexNormal(
      final long index,
      final PVectorI3F<RSpaceObjectType> normal)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexPosition(
      final long index,
      final PVectorI3F<RSpaceObjectType> position)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexStarted(
      final long index)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexTangent4f(
      final long index,
      final PVectorI4F<RSpaceObjectType> tangent)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVertexUV(
      final long index,
      final PVectorI2F<RSpaceTextureType> uv)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVerticesEnded(
      final PVectorI3F<RSpaceObjectType> lower,
      final PVectorI3F<RSpaceObjectType> upper)
      throws Throwable
    {
      // Nothing
    }

    @Override public void eventMeshVerticesStarted(
      final long count)
      throws Throwable
    {
      // Nothing
    }
  }

  private @NonNull InputStream getFile(
    final String name)
  {
    final InputStream s = this.getClass().getResourceAsStream(name);
    if (s == null) {
      throw new AssertionError("No such file!");
    }
    return s;
  }

  @Test public void testXMLBounds()
    throws IOException,
      Throwable
  {
    try {
      final String expected_name = "some-mesh";
      final int expected_triangles = 3;

      final EnumSet<RXMLMeshAttribute> attribs =
        EnumSet.noneOf(RXMLMeshAttribute.class);
      attribs.add(RXMLMeshAttribute.NORMAL_3F);
      attribs.add(RXMLMeshAttribute.TANGENT_4F);
      attribs.add(RXMLMeshAttribute.UV_2F);

      final int expected_vertices = 3;

      final Checked c =
        new Checked(
          expected_name,
          expected_triangles,
          expected_vertices,
          new PVectorI3F<RSpaceObjectType>(-23.0f, -34.0f, -11.0f),
          new PVectorI3F<RSpaceObjectType>(56.0f, 72.0f, 4.0f));

      final InputStream s = this.getFile("bounds.rmx");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, c);
      s.close();

      Assert.assertTrue(c.mesh_ended);
      Assert.assertTrue(c.mesh_started);
      Assert.assertTrue(c.mesh_vertex_ended);
      Assert.assertTrue(c.triangle_called);
      Assert.assertTrue(c.triangles_ended);
      Assert.assertTrue(c.triangles_started);
      Assert.assertTrue(c.vertex_normal_called);
      Assert.assertTrue(c.vertex_position_called);
      Assert.assertTrue(c.vertex_started_called);
      Assert.assertTrue(c.vertex_tangent4f_called);
      Assert.assertTrue(c.vertex_uv_called);
      Assert.assertTrue(c.vertices_ended);
      Assert.assertTrue(c.vertices_started);

    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Test public void testXMLStreamAllCalled()
    throws Throwable
  {
    try {
      final String expected_name = "some-mesh";
      final int expected_triangles = 3;

      final EnumSet<RXMLMeshAttribute> attribs =
        EnumSet.noneOf(RXMLMeshAttribute.class);
      attribs.add(RXMLMeshAttribute.NORMAL_3F);
      attribs.add(RXMLMeshAttribute.TANGENT_4F);
      attribs.add(RXMLMeshAttribute.UV_2F);

      final int expected_vertices = 3;

      final Checked c =
        new Checked(
          expected_name,
          expected_triangles,
          expected_vertices,
          new PVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f),
          new PVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f));

      final InputStream s = this.getFile("valid.rmx");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, c);
      s.close();

      Assert.assertTrue(c.mesh_ended);
      Assert.assertTrue(c.mesh_started);
      Assert.assertTrue(c.mesh_vertex_ended);
      Assert.assertTrue(c.triangle_called);
      Assert.assertTrue(c.triangles_ended);
      Assert.assertTrue(c.triangles_started);
      Assert.assertTrue(c.vertex_normal_called);
      Assert.assertTrue(c.vertex_position_called);
      Assert.assertTrue(c.vertex_started_called);
      Assert.assertTrue(c.vertex_tangent4f_called);
      Assert.assertTrue(c.vertex_uv_called);
      Assert.assertTrue(c.vertices_ended);
      Assert.assertTrue(c.vertices_started);

    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  @Test(expected = RXMLException.RXMLParsingException.class) public
    void
    testXMLStreamMalformed()
      throws IOException,
        RXMLException
  {
    final InputStream s = this.getFile("malformed.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test(expected = RXMLException.RXMLSaxExceptions.class) public
    void
    testXMLStreamNotSchemaValid()
      throws IOException,
        RXMLException
  {
    final InputStream s = this.getFile("invalid.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test public void testXMLStreamValid()
    throws IOException,
      Throwable
  {
    final InputStream s = this.getFile("valid.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test(expected = RXMLException.RXMLValidityException.class) public
    void
    testXMLStreamWrongTriangleCount()
      throws Throwable
  {
    final InputStream s = this.getFile("wrongtrianglecount.rmx");
    final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
    RXMLMeshParser.parseFromDocument(d, new Show());
    s.close();
  }

  @Test(expected = RXMLException.RXMLValidityException.class) public
    void
    testXMLStreamWrongVertexCount()
      throws RXMLException,
        Throwable
  {
    final InputStream s = this.getFile("wrongvertexcount.rmx");
    final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
    RXMLMeshParser.parseFromDocument(d, new Show());
    s.close();
  }
}
