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

package com.io7m.renderer.xml.collada;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.renderer.types.RXMLException;

public class ColladaDocumentTest
{
  public static @Nonnull Document getDocument(
    final @Nonnull String name)
  {
    try {
      final String file = "/com/io7m/renderer/xml/collada/" + name;
      final InputStream stream =
        ColladaDocumentTest.class.getResourceAsStream(file);
      if (stream == null) {
        throw new AssertionError("No such file: " + file);
      }
      final URL url = ColladaDocumentTest.class.getResource(file);
      final Builder b = new Builder();
      final Document doc = b.build(stream);
      doc.setBaseURI(url.toString());
      stream.close();
      return doc;
    } catch (final ValidityException e) {
      throw new AssertionError(e);
    } catch (final ParsingException e) {
      throw new AssertionError(e);
    } catch (final IOException e) {
      throw new AssertionError(e);
    }
  }

  public static @Nonnull Log getLog()
  {
    final Properties p = new Properties();
    p.setProperty("com.io7m.renderer.xml.collada.tests.level", "LOG_DEBUG");
    p.setProperty("com.io7m.renderer.xml.collada.tests.logs", "true");
    return new Log(p, "com.io7m.renderer.xml.collada", "tests");
  }

  @SuppressWarnings("static-method") @Test public
    void
    testGetGeometryNamesEmpty()
      throws RXMLException,
        ConstraintError
  {
    final Document d = ColladaDocumentTest.getDocument("empty.dae");
    final ColladaDocument p =
      ColladaDocument.newDocument(d, ColladaDocumentTest.getLog());
    Assert.assertEquals(new TreeSet<ColladaGeometryID>(), p.getGeometryIDs());
  }

  @SuppressWarnings("static-method") @Test public
    void
    testGetGeometryNamesMonkeys()
      throws RXMLException,
        ConstraintError
  {
    final Document d = ColladaDocumentTest.getDocument("monkeys.dae");
    final ColladaDocument p =
      ColladaDocument.newDocument(d, ColladaDocumentTest.getLog());

    final TreeSet<ColladaGeometryID> names = new TreeSet<ColladaGeometryID>();
    names.add(new ColladaGeometryID("monkey_textured_mesh-mesh"));
    names.add(new ColladaGeometryID("monkey_no_material_mesh-mesh"));
    names.add(new ColladaGeometryID("monkey_untextured_mesh-mesh"));

    Assert.assertEquals(names, p.getGeometryIDs());
  }

  @SuppressWarnings("static-method") @Test public void testGetGeometry()
    throws RXMLException,
      ConstraintError
  {
    final Document d = ColladaDocumentTest.getDocument("monkeys.dae");
    final ColladaDocument p =
      ColladaDocument.newDocument(d, ColladaDocumentTest.getLog());

    for (final ColladaGeometryID id : p.getGeometryIDs()) {
      final ColladaGeometry m = p.getGeometry(id);
      Assert.assertEquals(id, m.getID());
    }
  }
}
