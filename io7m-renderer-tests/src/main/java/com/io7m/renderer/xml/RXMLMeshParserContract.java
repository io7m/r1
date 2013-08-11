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
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.TestContext;
import com.io7m.renderer.TestContract;

public abstract class RXMLMeshParserContract<E extends Throwable> extends
  TestContract
{
  abstract RXMLMeshParser<E> parseFromStream(
    final @Nonnull TestContext context,
    final @Nonnull InputStream stream)
    throws JCGLException,
      IOException,
      RXMLException,
      ConstraintError;

  final @Nonnull InputStream getFile(
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
      final TestContext context = this.newTestContext();
      this.parseFromStream(context, this.getFile("malformed.xml"));
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
      final TestContext context = this.newTestContext();
      this.parseFromStream(context, this.getFile("invalid.xml"));
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
    final TestContext context = this.newTestContext();
    this.parseFromStream(context, this.getFile("valid.xml"));
  }

  @Test(expected = RXMLException.class) public
    void
    testXMLStreamWrongTriangleCount()
      throws RXMLException
  {
    try {
      final TestContext context = this.newTestContext();
      this.parseFromStream(context, this.getFile("wrongtrianglecount.xml"));
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
      final TestContext context = this.newTestContext();
      this.parseFromStream(context, this.getFile("wrongversion.xml"));
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
      final TestContext context = this.newTestContext();
      this.parseFromStream(context, this.getFile("wrongvertexcount.xml"));
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
