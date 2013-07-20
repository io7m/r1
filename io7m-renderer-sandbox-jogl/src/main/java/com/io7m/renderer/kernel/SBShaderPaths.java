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
import com.io7m.jvvfs.PathVirtual;

final class SBShaderPaths
{
  private static @Nonnull String directoryName(
    final int version_major,
    final int version_minor,
    final boolean es)
  {
    if (es == false) {
      switch (version_major) {
        case 2:
        {
          return "gl21";
        }
        case 3:
        {
          switch (version_minor) {
            case 0:
              return "gl30";
            default:
              return "gl31";
          }
        }
        default:
          return "gl31";
      }
    }

    switch (version_major) {
      case 2:
        return "gles2";
      default:
        return "gles3";
    }
  }

  public static @Nonnull PathVirtual getShader(
    final int version_major,
    final int version_minor,
    final boolean es,
    final @Nonnull String name)
    throws ConstraintError
  {
    return PathVirtual
      .ofString("/com/io7m/renderer/sandbox/")
      .appendName(
        SBShaderPaths.directoryName(version_major, version_minor, es))
      .appendName(name);
  }
}
