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

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jcache.JCacheException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.types.RException.RNotSupportedException;
import com.io7m.renderer.types.RException.RResourceException;

/**
 * A generic exception visitor, returning values of type <code>T</code> and
 * raising exceptions of type <code>E</code>.
 * 
 * @param <T>
 *          The return value type of the implementing visitor
 * @param <E>
 *          The type of exceptions raised by the implementing visitor
 */

public interface RExceptionVisitor<T, E extends Throwable>
{
  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitFilesystemException(
    final @Nonnull FilesystemError e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitIOException(
    final @Nonnull IOException e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitJCacheException(
    final @Nonnull JCacheException e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitJCGLException(
    final @Nonnull JCGLException e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitNotSupportedException(
    final @Nonnull RNotSupportedException e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitResourceException(
    final @Nonnull RResourceException e)
    throws E;

  /**
   * Visit the given exception type.
   * 
   * @param e
   *          The exception
   * @return T value of type <code>T</code>
   * @throws E
   *           If required
   */

  T exceptionVisitXMLException(
    final @Nonnull RXMLException e)
    throws E;
}
