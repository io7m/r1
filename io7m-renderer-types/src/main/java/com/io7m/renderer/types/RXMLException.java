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

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jparasol.xml.JPXMLException;

/**
 * The root type of exceptions raised by XML parsers and validators.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public abstract class RXMLException extends
  RException
{
  /**
   * An exception caused by a {@link JPXMLException}.
   */

  @EqualityReference public static final class RJPXMLException extends
    RXMLException
  {
    private static final long serialVersionUID = -7586780514130613772L;

    private RJPXMLException(
      final JPXMLException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public ValidityException getValidityException()
    {
      final ValidityException x = (ValidityException) this.getCause();
      assert x != null;
      return x;
    }
  }

  /**
   * An exception caused by a {@link NumberFormatException}.
   */

  @EqualityReference public static final class RXMLExceptionNumberFormatError extends
    RXMLException
  {
    private static final long serialVersionUID = 1690230411468954145L;

    protected RXMLExceptionNumberFormatError(
      final NumberFormatException x)
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
      final NumberFormatException x,
      final String message)
    {
      super(message, x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public NumberFormatException getNumberFormatException()
    {
      final NumberFormatException x = (NumberFormatException) this.getCause();
      assert x != null;
      return x;
    }
  }

  /**
   * An exception caused by a {@link ParserConfigurationException}.
   */

  @EqualityReference public static final class RXMLParserConfigurationException extends
    RXMLException
  {
    private static final long serialVersionUID = -5385921436341180278L;

    private RXMLParserConfigurationException(
      final ParserConfigurationException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public ParserConfigurationException getParserConfigurationException()
    {
      final ParserConfigurationException x =
        (ParserConfigurationException) this.getCause();
      assert x != null;
      return x;
    }
  }

  /**
   * An exception caused by a {@link ParsingException}.
   */

  @EqualityReference public static final class RXMLParsingException extends
    RXMLException
  {
    private static final long serialVersionUID = 3538612428621468503L;

    private RXMLParsingException(
      final ParsingException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public ParsingException getParsingException()
    {
      final ParsingException x = (ParsingException) this.getCause();
      assert x != null;
      return x;
    }
  }

  /**
   * An exception caused by a {@link SAXException}.
   */

  @EqualityReference public static final class RXMLSaxException extends
    RXMLException
  {
    private static final long serialVersionUID = -94265091031652878L;

    private RXMLSaxException(
      final SAXException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public SAXException getSAXException()
    {
      final SAXException x = (SAXException) this.getCause();
      assert x != null;
      return x;
    }
  }

  /**
   * An exception caused by list of {@link SAXException}.
   */

  @EqualityReference public static final class RXMLSaxExceptions extends
    RXMLException
  {
    private static final long        serialVersionUID = -94265091031652878L;
    private final List<SAXException> exceptions;

    private RXMLSaxExceptions(
      final List<SAXException> x)
    {
      super(NullCheck.notNull(NullCheck.notNullAll(x, "Exceptions").get(0)));
      this.exceptions = x;
    }

    /**
     * @return A list of all exceptions that caused this error. The list is
     *         guaranteed to be non-empty.
     */

    public List<SAXException> getExceptions()
    {
      return this.exceptions;
    }
  }

  /**
   * An exception caused by a {@link ValidityException}.
   */

  @EqualityReference public static final class RXMLValidityException extends
    RXMLException
  {
    private static final long serialVersionUID = -7586780514130613772L;

    private RXMLValidityException(
      final ValidityException x)
    {
      super(x);
    }

    /**
     * @return The cause of this exception as a specific type.
     */

    public ValidityException getValidityException()
    {
      final ValidityException x = (ValidityException) this.getCause();
      assert x != null;
      return x;
    }
  }

  private static final long serialVersionUID = 6801665395546178708L;

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param e
   *          The cause
   * @return A new exception
   */

  public static RXMLException fromJPXMLException(
    final JPXMLException e)
  {
    return new RJPXMLException(e);
  }

  /**
   * Construct an {@link RXMLException} from the given exception.
   * 
   * @param x
   *          The cause
   * @return A new exception
   */

  public static RXMLException parserConfigurationException(
    final ParserConfigurationException x)
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

  public static RXMLException parsingException(
    final ParsingException x)
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

  public static RXMLException saxException(
    final SAXException x)
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

  public static RXMLException saxExceptions(
    final List<SAXException> xs)
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

  public static RXMLException validityException(
    final ValidityException x)
  {
    return new RXMLValidityException(x);
  }

  protected RXMLException(
    final String message,
    final Throwable x)
  {
    super(x, message);
  }

  protected RXMLException(
    final Throwable x)
  {
    super(x);
  }

  @Override public final <T, E extends Throwable> T exceptionAccept(
    final RExceptionVisitorType<T, E> v)
    throws E
  {
    return v.exceptionVisitXMLException(this);
  }
}
