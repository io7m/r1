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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

public abstract class KXMLException extends Exception
{
  public static final class KXMLParserConfigurationException extends
    KXMLException
  {
    private static final long serialVersionUID = -5385921436341180278L;

    @SuppressWarnings("synthetic-access") private KXMLParserConfigurationException(
      final @Nonnull ParserConfigurationException x)
      throws ConstraintError
    {
      super(Type.KXML_PARSER_CONFIGURATION_EXCEPTION, x);
    }
  }

  public static final class KXMLParsingException extends KXMLException
  {
    private static final long serialVersionUID = 3538612428621468503L;

    @SuppressWarnings("synthetic-access") private KXMLParsingException(
      final @Nonnull ParsingException x)
      throws ConstraintError
    {
      super(Type.KXML_PARSING_EXCEPTION, x);
    }
  }

  public static final class KXMLSaxException extends KXMLException
  {
    private static final long serialVersionUID = -94265091031652878L;

    @SuppressWarnings("synthetic-access") private KXMLSaxException(
      final @Nonnull SAXException x)
      throws ConstraintError
    {
      super(Type.KXML_SAX_EXCEPTION, x);
    }
  }

  public static final class KXMLValidityException extends KXMLException
  {
    private static final long serialVersionUID = -7586780514130613772L;

    @SuppressWarnings("synthetic-access") private KXMLValidityException(
      final @Nonnull ValidityException x)
      throws ConstraintError
    {
      super(Type.KXML_VALIDITY_EXCEPTION, x);
    }
  }

  public static enum Type
  {
    KXML_PARSER_CONFIGURATION_EXCEPTION,
    KXML_PARSING_EXCEPTION,
    KXML_SAX_EXCEPTION,
    KXML_VALIDITY_EXCEPTION
  }

  private static final long serialVersionUID = -4760622215644296545L;

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    parserConfigurationException(
      final @Nonnull ParserConfigurationException x)
      throws ConstraintError
  {
    return new KXMLParserConfigurationException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    parsingException(
      final @Nonnull ParsingException x)
      throws ConstraintError
  {
    return new KXMLParsingException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    saxException(
      final @Nonnull SAXException x)
      throws ConstraintError
  {
    return new KXMLSaxException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    validityException(
      final @Nonnull ValidityException x)
      throws ConstraintError
  {
    return new KXMLValidityException(x);
  }

  private final @Nonnull Type type;

  private KXMLException(
    final @Nonnull Type type,
    final @Nonnull Exception x)
    throws ConstraintError
  {
    super(x);
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  public final @Nonnull Type getType()
  {
    return this.type;
  }
}
