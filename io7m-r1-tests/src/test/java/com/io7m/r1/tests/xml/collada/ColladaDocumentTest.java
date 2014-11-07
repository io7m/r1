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

package com.io7m.r1.tests.xml.collada;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.TreeSet;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogType;
import com.io7m.jnull.NonNull;
import com.io7m.jproperties.JPropertyException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RXMLException;
import com.io7m.r1.xml.collada.RColladaDocument;
import com.io7m.r1.xml.collada.RColladaGeometry;
import com.io7m.r1.xml.collada.RColladaGeometryID;

@SuppressWarnings("static-method") public class ColladaDocumentTest
{
  public static @NonNull Document getDocument(
    final @NonNull String name)
  {
    try {
      final String file = "/com/io7m/r1/tests/xml/collada/" + name;
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

  public static @NonNull LogType getLog()
  {
    try {
      final Properties p = new Properties();
      p.setProperty("com.io7m.r1.xml.collada.tests.level", "LOG_DEBUG");
      p.setProperty("com.io7m.r1.xml.collada.tests.logs", "true");
      final LogPolicyType policy =
        LogPolicyProperties.newPolicy(p, "com.io7m.r1.xml.collada");
      return Log.newLog(policy, "tests");
    } catch (final JPropertyException x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Test public void testGetGeometry()
    throws RXMLException
  {
    final Document d = ColladaDocumentTest.getDocument("monkeys.dae");
    final RColladaDocument p =
      RColladaDocument.newDocument(d, ColladaDocumentTest.getLog());

    for (final RColladaGeometryID id : p.getGeometryIDs()) {
      final RColladaGeometry m = p.getGeometry(id);
      assert m != null;
      Assert.assertEquals(id, m.getID());
    }
  }

  @Test public void testGetGeometryNamesEmpty()
    throws RXMLException
  {
    final Document d = ColladaDocumentTest.getDocument("empty.dae");
    final RColladaDocument p =
      RColladaDocument.newDocument(d, ColladaDocumentTest.getLog());
    Assert.assertEquals(new TreeSet<RColladaGeometryID>(), p.getGeometryIDs());
  }

  @Test public void testGetGeometryNamesMonkeys()
    throws RXMLException
  {
    final Document d = ColladaDocumentTest.getDocument("monkeys.dae");
    final RColladaDocument p =
      RColladaDocument.newDocument(d, ColladaDocumentTest.getLog());

    final TreeSet<RColladaGeometryID> names = new TreeSet<RColladaGeometryID>();
    names.add(new RColladaGeometryID("monkey_textured_mesh-mesh"));
    names.add(new RColladaGeometryID("monkey_no_material_mesh-mesh"));
    names.add(new RColladaGeometryID("monkey_untextured_mesh-mesh"));

    Assert.assertEquals(names, p.getGeometryIDs());
  }
}
