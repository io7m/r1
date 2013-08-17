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

import javax.annotation.Nonnull;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTangent;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;

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
      final RVectorI3F<RSpaceObject> normal)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexPosition(
      final int index,
      final RVectorI3F<RSpaceObject> position)
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

    @Override public void eventMeshVertexTangent(
      final int index,
      final RVectorI3F<RSpaceTangent> tangent)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVertexUV(
      final int index,
      final RVectorI2F<RSpaceTexture> uv)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventMeshVerticesEnded()
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

  @Test(expected = RXMLException.class) public void testXMLStreamMalformed()
    throws RXMLException
  {
    try {
      final InputStream s = this.getFile("malformed.xml");
      RXMLMeshDocument.parseFromStreamValidating(s);
      s.close();
    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(RXMLException.Type.XML_PARSE_ERROR, e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamNotSchemaValid()
      throws RXMLException
  {
    try {
      final InputStream s = this.getFile("invalid.xml");
      RXMLMeshDocument.parseFromStreamValidating(s);
      s.close();
    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(
        RXMLException.Type.XML_VALIDITY_SAX_ERROR,
        e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  @Test public void testXMLStreamValid()
    throws IOException,
      ConstraintError,
      Throwable
  {
    final InputStream s = this.getFile("valid.xml");
    RXMLMeshDocument.parseFromStreamValidating(s);
    s.close();
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamWrongTriangleCount()
      throws RXMLException
  {
    try {
      final InputStream s = this.getFile("wrongtrianglecount.xml");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, new Show());
      s.close();
    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(RXMLException.Type.XML_VALIDITY_ERROR, e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamWrongVersion()
      throws RXMLException
  {
    try {
      final InputStream s = this.getFile("wrongversion.xml");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, new Show());
      s.close();
    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(RXMLException.Type.XML_VALIDITY_ERROR, e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamWrongVertexCount()
      throws RXMLException
  {
    try {
      final InputStream s = this.getFile("wrongvertexcount.xml");
      final Document d = RXMLMeshDocument.parseFromStreamValidating(s);
      RXMLMeshParser.parseFromDocument(d, new Show());
      s.close();
    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(RXMLException.Type.XML_VALIDITY_ERROR, e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    } catch (final Throwable e) {
      throw new AssertionError(e);
    }
  }

  static class Checked implements RXMLMeshParserEvents<Throwable>
  {
    private boolean                     mesh_ended;
    private boolean                     mesh_started;
    private final @Nonnull String       expected_name;
    private boolean                     triangle_called;
    private boolean                     triangles_ended;
    private boolean                     triangles_started;
    private final int                   expected_triangles;
    private boolean                     mesh_type_called;
    private final @Nonnull RXMLMeshType expected_type;
    private boolean                     mesh_vertex_ended;
    private boolean                     vertex_normal_called;
    private boolean                     vertex_position_called;
    private boolean                     vertex_started_called;
    private boolean                     vertex_tangent_called;
    private boolean                     vertex_uv_called;
    private boolean                     vertices_ended;
    private boolean                     vertices_started;
    private final int                   expected_vertices;
    private int                         triangle = 0;
    private int                         vertices = 0;

    Checked(
      final @Nonnull String expected_name,
      final int expected_triangles,
      final @Nonnull RXMLMeshType expected_type,
      final int vertices_expected)
    {
      this.expected_name = expected_name;
      this.expected_triangles = expected_triangles;
      this.expected_type = expected_type;
      this.expected_vertices = vertices_expected;
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
      final RVectorI3F<RSpaceObject> normal)
      throws Throwable,
        ConstraintError
    {
      this.vertex_normal_called = true;
    }

    @Override public void eventMeshVertexPosition(
      final int index,
      final RVectorI3F<RSpaceObject> position)
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

    @Override public void eventMeshVertexTangent(
      final int index,
      final RVectorI3F<RSpaceTangent> tangent)
      throws Throwable,
        ConstraintError
    {
      this.vertex_tangent_called = true;
    }

    @Override public void eventMeshVertexUV(
      final int index,
      final RVectorI2F<RSpaceTexture> uv)
      throws Throwable,
        ConstraintError
    {
      this.vertex_uv_called = true;
    }

    @Override public void eventMeshVerticesEnded()
      throws Throwable,
        ConstraintError
    {
      this.vertices_ended = true;
      Assert.assertEquals(this.expected_vertices, this.vertices);
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
  }

  @SuppressWarnings("synthetic-access") @Test public
    void
    testXMLStreamAllCalled()
      throws Throwable
  {
    try {
      final String expected_name = "some-mesh";
      final int expected_triangles = 3;
      final RXMLMeshType expected_type = new RXMLMeshType(true, true, true);
      final int expected_vertices = 3;

      final Checked c =
        new Checked(
          expected_name,
          expected_triangles,
          expected_type,
          expected_vertices);

      final InputStream s = this.getFile("valid.xml");
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
      Assert.assertTrue(c.vertex_tangent_called);
      Assert.assertTrue(c.vertex_uv_called);
      Assert.assertTrue(c.vertices_ended);
      Assert.assertTrue(c.vertices_started);

    } catch (final IOException e) {
      throw new AssertionError(e);
    } catch (final RXMLException e) {
      Assert.assertEquals(RXMLException.Type.XML_VALIDITY_ERROR, e.getType());
      throw e;
    } catch (final ConstraintError e) {
      throw new AssertionError(e);
    }
  }
}
