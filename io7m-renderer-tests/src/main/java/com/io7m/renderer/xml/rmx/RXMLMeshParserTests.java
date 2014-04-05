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

package com.io7m.renderer.xml.rmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.AlmostEqualFloat.ContextRelative;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RXMLException;

public final class RXMLMeshParserTests
{
  class Show implements RXMLMeshParserEvents<Throwable>
  {
    @Override public void eventError(
      final RXMLException e)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshEnded()
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshName(
      final String name)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshTriangle(
      final int index,
      final int v0,
      final int v1,
      final int v2)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesEnded()
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesStarted(
      final int count)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshType(
      final RXMLMeshType mt)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexEnded(
      final int index)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexNormal(
      final int index,
      final RVectorI3F<RSpaceObjectType> normal)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexPosition(
      final int index,
      final RVectorI3F<RSpaceObjectType> position)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexStarted(
      final int index)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexTangent3f(
      final int index,
      final RVectorI3F<RSpaceObjectType> tangent)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexTangent4f(
      final int index,
      final RVectorI4F<RSpaceObjectType> tangent)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexBitangent(
      final int index,
      final RVectorI3F<RSpaceObjectType> bitangent)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexUV(
      final int index,
      final RVectorI2F<RSpaceTextureType> uv)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVerticesEnded(
      final @Nonnull RVectorI3F<RSpaceObjectType> lower,
      final @Nonnull RVectorI3F<RSpaceObjectType> upper)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVerticesStarted(
      final int count)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshStarted()
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }
  }

  private @Nonnull InputStream getFile(
    final @Nonnull String name)
  {
    final InputStream s = this.getClass().getResourceAsStream(name);
    if (s == null) {
      throw new AssertionError("No such file!");
    }
    return s;
  }

  @Test(expected = RXMLException.RXMLParsingException.class) public
    void
    testXMLStreamMalformed()
      throws IOException,
        RXMLException,
        ConstraintError
  {
    final InputStream s = this.getFile("malformed.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test(expected = RXMLException.RXMLSaxExceptions.class) public
    void
    testXMLStreamNotSchemaValid()
      throws IOException,
        RXMLException,
        ConstraintError
  {
    final InputStream s = this.getFile("invalid.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test public void testXMLStreamValid()
    throws IOException,
      ConstraintError,
      Throwable
  {
    final InputStream s = this.getFile("valid.rmx");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test public void testXMLBounds()
    throws IOException,
      ConstraintError,
      Throwable
  {
    try {
      final String expected_name = "some-mesh";
      final int expected_triangles = 3;

      final EnumSet<RXMLMeshAttribute> attribs =
        EnumSet.noneOf(RXMLMeshAttribute.class);
      attribs.add(RXMLMeshAttribute.NORMAL_3F);
      attribs.add(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F);
      attribs.add(RXMLMeshAttribute.UV_2F);

      final RXMLMeshType expected_type = new RXMLMeshType(attribs);
      final int expected_vertices = 3;

      final Checked c =
        new Checked(
          expected_name,
          expected_triangles,
          expected_type,
          expected_vertices,
          new RVectorI3F<RSpaceObjectType>(-23.0f, -34.0f, -11.0f),
          new RVectorI3F<RSpaceObjectType>(56.0f, 72.0f, 4.0f));

      final InputStream s = this.getFile("bounds.rmx");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, c);
      s.close();

      Assert.assertTrue(c.mesh_ended);
      Assert.assertTrue(c.mesh_started);
      Assert.assertTrue(c.mesh_type_called);
      Assert.assertTrue(c.mesh_vertex_ended);
      Assert.assertTrue(c.triangle_called);
      Assert.assertTrue(c.triangles_ended);
      Assert.assertTrue(c.triangles_started);
      Assert.assertTrue(c.vertex_normal_called);
      Assert.assertTrue(c.vertex_position_called);
      Assert.assertTrue(c.vertex_started_called);
      Assert.assertTrue(c.vertex_tangent3f_called);
      Assert.assertTrue(c.vertex_uv_called);
      Assert.assertTrue(c.vertices_ended);
      Assert.assertTrue(c.vertices_started);

    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    }
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
    testXMLStreamWrongVersion()
      throws Throwable
  {
    final InputStream s = this.getFile("wrongversion.rmx");
    final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
    RXMLMeshParser.parseFromDocument(d, new Show());
    s.close();
  }

  @Test(expected = RXMLException.RXMLValidityException.class) public
    void
    testXMLStreamWrongVertexCount()
      throws RXMLException,
        ConstraintError,
        Throwable
  {
    final InputStream s = this.getFile("wrongvertexcount.rmx");
    final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
    RXMLMeshParser.parseFromDocument(d, new Show());
    s.close();
  }

  static class Checked implements RXMLMeshParserEvents<Throwable>
  {
    private boolean                        mesh_ended;
    private boolean                        mesh_started;
    private final @Nonnull String          expected_name;
    private boolean                        triangle_called;
    private boolean                        triangles_ended;
    private boolean                        triangles_started;
    private final int                      expected_triangles;
    private boolean                        mesh_type_called;
    private final @Nonnull RXMLMeshType    expected_type;
    private boolean                        mesh_vertex_ended;
    private boolean                        vertex_normal_called;
    private boolean                        vertex_position_called;
    private boolean                        vertex_started_called;
    private boolean                        vertex_tangent3f_called;
    private boolean                        vertex_tangent4f_called;
    private boolean                        vertex_bitangent_called;
    private boolean                        vertex_uv_called;
    private boolean                        vertices_ended;
    private boolean                        vertices_started;
    private final int                      expected_vertices;
    private int                            triangle = 0;
    private int                            vertices = 0;
    private final RVectorI3F<RSpaceObjectType> expected_lower;
    private final RVectorI3F<RSpaceObjectType> expected_upper;

    Checked(
      final @Nonnull String in_expected_name,
      final int in_expected_triangles,
      final @Nonnull RXMLMeshType in_expected_type,
      final int in_vertices_expected,
      final @Nonnull RVectorI3F<RSpaceObjectType> in_expected_lower,
      final @Nonnull RVectorI3F<RSpaceObjectType> in_expected_upper)
    {
      this.expected_name = in_expected_name;
      this.expected_triangles = in_expected_triangles;
      this.expected_type = in_expected_type;
      this.expected_vertices = in_vertices_expected;
      this.expected_lower = in_expected_lower;
      this.expected_upper = in_expected_upper;
    }

    @Override public void eventError(
      final RXMLException e)
      throws Throwable,
        ConstraintError
    {
      throw new AssertionError(e);
    }

    @Override public void eventMeshEnded()
      throws Throwable,
        ConstraintError
    {
      this.mesh_ended = true;
    }

    @Override public void eventMeshName(
      final String name)
      throws Throwable,
        ConstraintError
    {
      Assert.assertEquals(this.expected_name, name);
    }

    @Override public void eventMeshTriangle(
      final int index,
      final int v0,
      final int v1,
      final int v2)
      throws Throwable,
        ConstraintError
    {
      this.triangle_called = true;
      ++this.triangle;
    }

    @Override public void eventMeshTrianglesEnded()
      throws Throwable,
        ConstraintError
    {
      this.triangles_ended = true;
      Assert.assertEquals(this.expected_triangles, this.triangle);
    }

    @Override public void eventMeshTrianglesStarted(
      final int count)
      throws Throwable,
        ConstraintError
    {
      this.triangles_started = true;
      Assert.assertEquals(this.expected_triangles, count);
    }

    @Override public void eventMeshType(
      final RXMLMeshType mt)
      throws Throwable,
        ConstraintError
    {
      this.mesh_type_called = true;
      Assert.assertEquals(this.expected_type, mt);
    }

    @Override public void eventMeshVertexEnded(
      final int index)
      throws Throwable,
        ConstraintError
    {
      this.mesh_vertex_ended = true;
    }

    @Override public void eventMeshVertexNormal(
      final int index,
      final RVectorI3F<RSpaceObjectType> normal)
      throws Throwable,
        ConstraintError
    {
      this.vertex_normal_called = true;
    }

    @Override public void eventMeshVertexPosition(
      final int index,
      final RVectorI3F<RSpaceObjectType> position)
      throws Throwable,
        ConstraintError
    {
      this.vertex_position_called = true;
    }

    @Override public void eventMeshVertexStarted(
      final int index)
      throws Throwable,
        ConstraintError
    {
      this.vertex_started_called = true;
      ++this.vertices;
    }

    @Override public void eventMeshVertexTangent3f(
      final int index,
      final RVectorI3F<RSpaceObjectType> tangent)
      throws Throwable,
        ConstraintError
    {
      this.vertex_tangent3f_called = true;
    }

    @Override public void eventMeshVertexTangent4f(
      final int index,
      final RVectorI4F<RSpaceObjectType> tangent)
      throws Throwable,
        ConstraintError
    {
      this.vertex_tangent4f_called = true;
    }

    @Override public void eventMeshVertexUV(
      final int index,
      final RVectorI2F<RSpaceTextureType> uv)
      throws Throwable,
        ConstraintError
    {
      this.vertex_uv_called = true;
    }

    @Override public void eventMeshVerticesEnded(
      final @Nonnull RVectorI3F<RSpaceObjectType> lower,
      final @Nonnull RVectorI3F<RSpaceObjectType> upper)
      throws Throwable,
        ConstraintError
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
      final int count)
      throws Throwable,
        ConstraintError
    {
      this.vertices_started = true;
      Assert.assertEquals(this.expected_vertices, count);
    }

    @Override public void eventMeshStarted()
      throws Throwable,
        ConstraintError
    {
      this.mesh_started = true;
    }

    @Override public void eventMeshVertexBitangent(
      final int index,
      final RVectorI3F<RSpaceObjectType> bitangent)
      throws Throwable,
        ConstraintError
    {
      this.vertex_bitangent_called = true;
    }
  }

  @SuppressWarnings("synthetic-access") @Test public
    void
    testXMLStreamAllCalled()
      throws Throwable
  {
    try {
      final String expected_name = "some-mesh";
      final int expected_triangles = 3;

      final EnumSet<RXMLMeshAttribute> attribs =
        EnumSet.noneOf(RXMLMeshAttribute.class);
      attribs.add(RXMLMeshAttribute.NORMAL_3F);
      attribs.add(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F);
      attribs.add(RXMLMeshAttribute.UV_2F);

      final RXMLMeshType expected_type = new RXMLMeshType(attribs);
      final int expected_vertices = 3;

      final Checked c =
        new Checked(
          expected_name,
          expected_triangles,
          expected_type,
          expected_vertices,
          new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f),
          new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f));

      final InputStream s = this.getFile("valid.rmx");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, c);
      s.close();

      Assert.assertTrue(c.mesh_ended);
      Assert.assertTrue(c.mesh_started);
      Assert.assertTrue(c.mesh_type_called);
      Assert.assertTrue(c.mesh_vertex_ended);
      Assert.assertTrue(c.triangle_called);
      Assert.assertTrue(c.triangles_ended);
      Assert.assertTrue(c.triangles_started);
      Assert.assertTrue(c.vertex_normal_called);
      Assert.assertTrue(c.vertex_position_called);
      Assert.assertTrue(c.vertex_started_called);
      Assert.assertTrue(c.vertex_tangent3f_called);
      Assert.assertTrue(c.vertex_uv_called);
      Assert.assertTrue(c.vertices_ended);
      Assert.assertTrue(c.vertices_started);

    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    }
  }
}
