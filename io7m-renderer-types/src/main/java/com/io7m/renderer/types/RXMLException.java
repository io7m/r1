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

package com.io7m.renderer.types;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

/**
 * The root type of exceptions raised by XML parsers and validators.
 */

public abstract class RXMLException extends RException
{
  /**
   * An exception caused by a {@link NumberFormatException}.
   */

  public static final class RXMLExceptionNumberFormatError extends
    RXMLException
  {
    private static final long serialVersionUID = 1690230411468954145L;

    protected RXMLExceptionNumberFormatError(
      final @Nonnull NumberFormatException x)
    {
      super(x);
    }

    /**
     * Construct a new exception with a specific cause and message.
     * 
     * @param x
     *          The cause
     * @param message
     *          The message
     */

    public RXMLExceptionNumberFormatError(
      final @Nonnull NumberFormatException x,
      final @Nonnull String message)
    {
      super(message, x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public @Nonnull NumberFormatException getNumberFormatException()
    {
      return (NumberFormatException) this.getCause();
    }
  }

  /**
   * An exception caused by a {@link ParserConfigurationException}.
   */

  public static final class RXMLParserConfigurationException extends
    RXMLException
  {
    private static final long serialVersionUID = -5385921436341180278L;

    private RXMLParserConfigurationException(
      final @Nonnull ParserConfigurationException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public @Nonnull
      ParserConfigurationException
      getParserConfigurationException()
    {
      return (ParserConfigurationException) this.getCause();
    }
  }

  /**
   * An exception caused by a {@link ParsingException}.
   */

  public static final class RXMLParsingException extends RXMLException
  {
    private static final long serialVersionUID = 3538612428621468503L;

    private RXMLParsingException(
      final @Nonnull ParsingException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public @Nonnull ParsingException getParsingException()
    {
      return (ParsingException) this.getCause();
    }
  }

  /**
   * An exception caused by a {@link SAXException}.
   */

  public static final class RXMLSaxException extends RXMLException
  {
    private static final long serialVersionUID = -94265091031652878L;

    private RXMLSaxException(
      final @Nonnull SAXException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public @Nonnull SAXException getSAXException()
    {
      return (SAXException) this.getCause();
    }
  }

  /**
   * An exception caused by list of {@link SAXException}.
   */

  public static final class RXMLSaxExceptions extends RXMLException
  {
    private static final long                 serialVersionUID =
                                                                 -94265091031652878L;
    private final @Nonnull List<SAXException> exceptions;

    private RXMLSaxExceptions(
      final @Nonnull List<SAXException> x)
    {
      super(x.get(0));
      this.exceptions = x;
    }

    /**
     * @return A list of all exceptions that caused this error. The list is
     *         guaranteed to be non-empty.
     */

    public @Nonnull List<SAXException> getExceptions()
    {
      return this.exceptions;
    }
  }

  /**
   * An exception caused by a {@link ValidityException}.
   */

  public static final class RXMLValidityException extends RXMLException
  {
    private static final long serialVersionUID = -7586780514130613772L;

    private RXMLValidityException(
      final @Nonnull ValidityException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public @Nonnull ValidityException getValidityException()
    {
      return (ValidityException) this.getCause();
    }
  }

  private static final long serialVersionUID = 6801665395546178708L;

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param x
   *          The cause
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RXMLException
    parserConfigurationException(
      final @Nonnull ParserConfigurationException x)
  {
    return new RXMLParserConfigurationException(x);
  }

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param x
   *          The cause
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RXMLException
    parsingException(
      final @Nonnull ParsingException x)
  {
    return new RXMLParsingException(x);
  }

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param x
   *          The cause
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RXMLException
    saxException(
      final @Nonnull SAXException x)
  {
    return new RXMLSaxException(x);
  }

  /**
   * Construct an {@link RXMLException} from the given exceptions.
   * 
   * @param xs
   *          The list of causes
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RXMLException
    saxExceptions(
      final @Nonnull List<SAXException> xs)
  {
    assert xs.isEmpty() == false;
    return new RXMLSaxExceptions(xs);
  }

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param x
   *          The cause
   * @return A new exception
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    RXMLException
    validityException(
      final @Nonnull ValidityException x)
  {
    return new RXMLValidityException(x);
  }

  protected RXMLException(
    final @Nonnull String message,
    final @Nonnull Throwable x)
  {
    super(x, message);
  }

  protected RXMLException(
    final @Nonnull Throwable x)
  {
    super(x);
  }

  @Override public final <T, E extends Throwable> T exceptionAccept(
    final @Nonnull RExceptionVisitorType<T, E> v)
    throws E
  {
    return v.exceptionVisitXMLException(this);
  }
}
