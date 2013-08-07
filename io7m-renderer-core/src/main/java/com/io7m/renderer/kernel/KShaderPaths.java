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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jvvfs.PathVirtual;

public final class KShaderPaths
{
  private static @Nonnull PathVirtual BASE;

  static {
    try {
      KShaderPaths.BASE = PathVirtual.ofString("/com/io7m/renderer/kernel");
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }

  private static @Nonnull String getShaderDirectoryName(
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api)
    throws JCGLUnsupportedException
  {
    switch (api) {
      case JCGL_ES:
      {
        if (version.getVersionMajor() == 1) {
          return "glsl110";
        }
        if (version.getVersionMajor() == 3) {
          return "glsl300";
        }
        throw new JCGLUnsupportedException("Unsupported GLSL ES version: "
          + version);
      }
      case JCGL_FULL:
      {
        switch (version.getVersionMajor()) {
          case 1:
          {
            switch (version.getVersionMinor()) {
              case 10:
                return "glsl110";
              case 20:
                return "glsl120";
              case 30:
                return "glsl130";
              case 40:
                return "glsl140";
              case 50:
                return "glsl150";
            }
            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
          }
          case 3:
          {
            switch (version.getVersionMinor()) {
              case 30:
                return "glsl330";
            }
            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
          }
          case 4:
          {
            switch (version.getVersionMinor()) {
              case 0:
                return "glsl400";
              case 10:
                return "glsl410";
              case 20:
                return "glsl420";
              case 30:
                return "glsl430";
              case 40:
                return "glsl440";
            }

            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
          }
          default:
            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
        }
      }
    }

    throw new UnreachableCodeException();
  }

  public static @Nonnull PathVirtual getShaderPath(
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull String name)
    throws JCGLUnsupportedException,
      ConstraintError
  {
    return KShaderPaths.BASE.appendName(
      KShaderPaths.getShaderDirectoryName(version, api)).appendName(name);
  }
}
