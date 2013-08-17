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

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

public abstract class RXMLException extends Exception
{
  public static final class RXMLExceptionNumberFormatError extends
    RXMLException
  {
    private static final long                    serialVersionUID;
    private final @Nonnull NumberFormatException e;

    static {
      serialVersionUID = -5639465367483354396L;
    }

    public @SuppressWarnings("synthetic-access") RXMLExceptionNumberFormatError(
      final @Nonnull NumberFormatException e,
      final @Nonnull String message)
      throws ConstraintError
    {
      super(e, Type.XML_NUMBER_FORMAT_ERROR, message);
      this.e = Constraints.constrainNotNull(e, "Exception");
    }

    public @Nonnull NumberFormatException getException()
    {
      return this.e;
    }
  }

  public static final class RXMLExceptionParseError extends RXMLException
  {
    private static final long               serialVersionUID;
    private final @Nonnull ParsingException e;

    static {
      serialVersionUID = -5639465367483354396L;
    }

    public @SuppressWarnings("synthetic-access") RXMLExceptionParseError(
      final @Nonnull ParsingException e)
      throws ConstraintError
    {
      super(e, Type.XML_PARSE_ERROR);
      this.e = Constraints.constrainNotNull(e, "Exception");
    }

    public @Nonnull ParsingException getException()
    {
      return this.e;
    }
  }

  public static final class RXMLExceptionParserConfigurationError extends
    RXMLException
  {
    private static final long                           serialVersionUID;
    private final @Nonnull ParserConfigurationException e;

    static {
      serialVersionUID = -5639465367483354396L;
    }

    public @SuppressWarnings("synthetic-access") RXMLExceptionParserConfigurationError(
      final @Nonnull ParserConfigurationException e)
      throws ConstraintError
    {
      super(e, Type.XML_PARSE_ERROR);
      this.e = Constraints.constrainNotNull(e, "Exception");
    }

    public @Nonnull ParserConfigurationException getException()
    {
      return this.e;
    }
  }

  public static final class RXMLExceptionValidityError extends RXMLException
  {
    private static final long                serialVersionUID;
    private final @Nonnull ValidityException e;

    static {
      serialVersionUID = -6695888203449564788L;
    }

    public @SuppressWarnings("synthetic-access") RXMLExceptionValidityError(
      final @Nonnull ValidityException e)
      throws ConstraintError
    {
      super(e, Type.XML_VALIDITY_ERROR);
      this.e = Constraints.constrainNotNull(e, "Exception");
    }

    public @Nonnull ValidityException getException()
    {
      return this.e;
    }
  }

  public static final class RXMLExceptionValiditySAXErrors extends
    RXMLException
  {
    private static final long                 serialVersionUID;
    private final @Nonnull List<SAXException> es;

    static {
      serialVersionUID = 8405145698494143173L;
    }

    public @SuppressWarnings("synthetic-access") RXMLExceptionValiditySAXErrors(
      final @Nonnull List<SAXException> es)
      throws ConstraintError
    {
      super(es.get(0), Type.XML_VALIDITY_SAX_ERROR);
      this.es = Constraints.constrainNotNull(es, "Exceptions");
    }

    public @Nonnull List<SAXException> getExceptions()
    {
      return this.es;
    }
  }

  public static enum Type
  {
    XML_PARSE_ERROR,
    XML_VALIDITY_SAX_ERROR,
    XML_VALIDITY_ERROR,
    XML_NUMBER_FORMAT_ERROR,
  }

  private static final long   serialVersionUID = 5005826249892823232L;
  private final @Nonnull Type type;

  private RXMLException(
    final @Nonnull Exception exception,
    final @Nonnull Type type)
    throws ConstraintError
  {
    super(exception);
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  private RXMLException(
    final @Nonnull Exception exception,
    final @Nonnull Type type,
    final @Nonnull String message)
    throws ConstraintError
  {
    super(message, exception);
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }
}
