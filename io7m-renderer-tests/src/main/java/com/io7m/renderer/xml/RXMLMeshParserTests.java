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

package com.io7m.renderer.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTangent;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;

public final class RXMLMeshParserTests
{
  class Show implements RXMLMeshParserEvents<Throwable>
  {
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

    @Override public void eventMeshVertexStarted(
      final int index)
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

    @Override public void eventVertexNormal(
      final int index,
      final RVectorI3F<RSpaceObject> normal)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventVertexPosition(
      final int index,
      final RVectorI3F<RSpaceObject> position)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventVertexTangent(
      final int index,
      final RVectorI3F<RSpaceTangent> tangent)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventVertexUV(
      final int index,
      final RVectorI2F<RSpaceTexture> uv)
      throws Throwable,
        ConstraintError
    {
      // Nothing
    }

    @Override public void eventXMLError(
      final RXMLException e)
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
      RXMLMeshParser.parseFromStreamValidating(s, new Show());
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
      RXMLMeshParser.parseFromStreamValidating(s, new Show());
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
    RXMLMeshParser.parseFromStreamValidating(s, new Show());
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamWrongTriangleCount()
      throws RXMLException
  {
    try {
      final InputStream s = this.getFile("wrongtrianglecount.xml");
      RXMLMeshParser.parseFromStreamValidating(s, new Show());
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
      RXMLMeshParser.parseFromStreamValidating(s, new Show());
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
      RXMLMeshParser.parseFromStreamValidating(s, new Show());
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
}
