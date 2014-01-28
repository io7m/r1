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

public abstract class KXMLException extends Exception
{
  public static final class RXMLParserConfigurationException extends
    KXMLException
  {
    private static final long serialVersionUID = -5385921436341180278L;

    @SuppressWarnings("synthetic-access") private RXMLParserConfigurationException(
      final @Nonnull ParserConfigurationException x)
    {
      super(x);
    }
  }

  public static final class RXMLParsingException extends KXMLException
  {
    private static final long serialVersionUID = 3538612428621468503L;

    @SuppressWarnings("synthetic-access") private RXMLParsingException(
      final @Nonnull ParsingException x)
    {
      super(x);
    }
  }

  public static final class RXMLSaxException extends KXMLException
  {
    private static final long serialVersionUID = -94265091031652878L;

    @SuppressWarnings("synthetic-access") private RXMLSaxException(
      final @Nonnull SAXException x)
    {
      super(x);
    }
  }

  public static final class RXMLValidityException extends KXMLException
  {
    private static final long serialVersionUID = -7586780514130613772L;

    @SuppressWarnings("synthetic-access") private RXMLValidityException(
      final @Nonnull ValidityException x)
    {
      super(x);
    }
  }

  private static final long serialVersionUID = -4760622215644296545L;

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    parserConfigurationException(
      final @Nonnull ParserConfigurationException x)
  {
    return new RXMLParserConfigurationException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    parsingException(
      final @Nonnull ParsingException x)
  {
    return new RXMLParsingException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    saxException(
      final @Nonnull SAXException x)
  {
    return new RXMLSaxException(x);
  }

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KXMLException
    validityException(
      final @Nonnull ValidityException x)
  {
    return new RXMLValidityException(x);
  }

  private KXMLException(
    final @Nonnull Exception x)
  {
    super(x);
  }
}
