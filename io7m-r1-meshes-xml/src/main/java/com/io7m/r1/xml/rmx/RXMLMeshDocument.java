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

package com.io7m.r1.xml.rmx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.xml.RXMLException;

/**
 * Functions to validate and load an XML mesh document.
 */

@EqualityReference public final class RXMLMeshDocument
{
  /**
   * Load an XML document representing a mesh from the input stream
   * <code>s</code>.
   *
   * @param s
   *          The input stream.
   * @return A loaded document.
   *
   * @throws IOException
   *           Iff an I/O error occurs whilst reading from the input stream.
   * @throws RXMLException
   *           Iff the XML document cannot be parsed, or is not valid
   *           according to the schema.
   */

  public static Document parseFromStreamValidating(
    final InputStream s)
    throws IOException,
      RXMLException
  {
    NullCheck.notNull(s, "Stream");

    try {
      final URL schema_url =
        RXMLMeshParser.class.getResource("/com/io7m/r1/xml/meshes.xsd");

      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      spf.setNamespaceAware(true);

      final SchemaFactory sf =
        SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      spf.setSchema(sf.newSchema(schema_url));

      final List<SAXException> exceptions = new ArrayList<SAXException>();

      final SAXParser sp = spf.newSAXParser();
      final XMLReader xr = sp.getXMLReader();
      xr.setErrorHandler(new ErrorHandler() {
        @Override public void error(
          final @Nullable SAXParseException exception)
          throws SAXException
        {
          exceptions.add(exception);
        }

        @Override public void fatalError(
          final @Nullable SAXParseException exception)
          throws SAXException
        {
          exceptions.add(exception);
        }

        @Override public void warning(
          final @Nullable SAXParseException exception)
          throws SAXException
        {
          exceptions.add(exception);
        }
      });

      final Builder builder = new Builder(xr);
      final Document d = builder.build(s);

      if (exceptions.size() > 0) {
        throw RXMLException.saxExceptions(exceptions);
      }

      assert d != null;
      return d;
    } catch (final SAXException e) {
      final List<SAXException> es = new ArrayList<SAXException>();
      es.add(e);
      throw RXMLException.saxExceptions(es);
    } catch (final ValidityException e) {
      throw RXMLException.validityException(e);
    } catch (final ParsingException e) {
      throw RXMLException.parsingException(e);
    } catch (final ParserConfigurationException e) {
      throw RXMLException.parserConfigurationException(e);
    }
  }

  private RXMLMeshDocument()
  {
    throw new UnreachableCodeException();
  }
}
