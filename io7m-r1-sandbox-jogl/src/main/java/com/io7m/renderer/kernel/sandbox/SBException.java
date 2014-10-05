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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;

abstract class SBException extends Exception
{
  final static class SBExceptionImageLoading extends SBException
  {
    private static final long   serialVersionUID = 7521983144069448194L;
    private final  File file;

    @SuppressWarnings("synthetic-access") SBExceptionImageLoading(
      final  File in_file,
      final  String message)
    {
      super(Type.SB_EXCEPTION_IMAGE_LOADING, in_file + ": " + message);
      this.file = in_file;
    }
  }

  final static class SBExceptionInputError extends SBException
  {
    private static final long serialVersionUID = 4254147123710014337L;

    @SuppressWarnings("synthetic-access") SBExceptionInputError(
      final  String message)
    {
      super(Type.SB_EXCEPTION_INPUT_ERROR, message);
    }
  }

  static enum Type
  {
    SB_EXCEPTION_IMAGE_LOADING,
    SB_EXCEPTION_INPUT_ERROR,
  }

  private static final long   serialVersionUID = -746503205823207820L;

  private final  Type type;

  private SBException(
    final  Type in_type,
    final  String message)
  {
    super(message);
    this.type = in_type;
  }
}
