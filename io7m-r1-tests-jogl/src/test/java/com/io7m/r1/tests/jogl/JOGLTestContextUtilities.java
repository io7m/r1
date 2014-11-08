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

package com.io7m.r1.tests.jogl;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.tests.TestContext;

public final class JOGLTestContextUtilities
{
  public static JCGLInterfaceGL2Type getGL2(
    final TestContext context)
  {
    try {
      return context
        .getGLImplementation()
        .implementationAccept(
          new JCGLImplementationVisitorType<JCGLInterfaceGL2Type, UnreachableCodeException>() {
            @Override public JCGLInterfaceGL2Type implementationIsGL2(
              final JCGLInterfaceGL2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              return gl;
            }

            @Override public JCGLInterfaceGL2Type implementationIsGL3(
              final JCGLInterfaceGL3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGL2Type implementationIsGLES2(
              final JCGLInterfaceGLES2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGL2Type implementationIsGLES3(
              final JCGLInterfaceGLES3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }
          });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static JCGLInterfaceGL3Type getGL3(
    final TestContext context)
  {
    try {
      return context
        .getGLImplementation()
        .implementationAccept(
          new JCGLImplementationVisitorType<JCGLInterfaceGL3Type, UnreachableCodeException>() {
            @Override public JCGLInterfaceGL3Type implementationIsGL2(
              final JCGLInterfaceGL2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGL3Type implementationIsGL3(
              final JCGLInterfaceGL3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              return gl;
            }

            @Override public JCGLInterfaceGL3Type implementationIsGLES2(
              final JCGLInterfaceGLES2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGL3Type implementationIsGLES3(
              final JCGLInterfaceGLES3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }
          });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static JCGLInterfaceGLES2Type getGLES2(
    final TestContext context)
  {
    try {
      return context
        .getGLImplementation()
        .implementationAccept(
          new JCGLImplementationVisitorType<JCGLInterfaceGLES2Type, UnreachableCodeException>() {
            @Override public JCGLInterfaceGLES2Type implementationIsGL2(
              final JCGLInterfaceGL2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGLES2Type implementationIsGL3(
              final JCGLInterfaceGL3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGLES2Type implementationIsGLES2(
              final JCGLInterfaceGLES2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              return gl;
            }

            @Override public JCGLInterfaceGLES2Type implementationIsGLES3(
              final JCGLInterfaceGLES3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }
          });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static JCGLInterfaceGLES3Type getGLES3(
    final TestContext context)
  {
    try {
      return context
        .getGLImplementation()
        .implementationAccept(
          new JCGLImplementationVisitorType<JCGLInterfaceGLES3Type, UnreachableCodeException>() {
            @Override public JCGLInterfaceGLES3Type implementationIsGL2(
              final JCGLInterfaceGL2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGLES3Type implementationIsGL3(
              final JCGLInterfaceGL3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGLES3Type implementationIsGLES2(
              final JCGLInterfaceGLES2Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              throw new UnreachableCodeException();
            }

            @Override public JCGLInterfaceGLES3Type implementationIsGLES3(
              final JCGLInterfaceGLES3Type gl)
              throws JCGLException,
                UnreachableCodeException
            {
              return gl;
            }
          });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
