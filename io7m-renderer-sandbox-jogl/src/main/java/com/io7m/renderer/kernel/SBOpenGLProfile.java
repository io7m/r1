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

public enum SBOpenGLProfile
{
  OPENGL_PROFILE_DEFAULT("default"),
  OPENGL_PROFILE_GL2("GL2"),
  OPENGL_PROFILE_GL3("GL3"),
  OPENGL_PROFILE_GL4("GL4"),
  OPENGL_PROFILE_GLES2("GLES2"),
  OPENGL_PROFILE_GLES3("GLES3");

  public static @Nonnull SBOpenGLProfile fromString(
    final @Nonnull String name)
    throws ConstraintError
  {
    for (final SBOpenGLProfile v : SBOpenGLProfile.values()) {
      if (v.name.equals(name)) {
        return v;
      }
    }

    throw new ConstraintError("Unknown OpenGL profile '" + name + "'");
  }

  private final @Nonnull String name;

  private SBOpenGLProfile(
    final @Nonnull String in_name)
  {
    this.name = in_name;
  }

  @Override public @Nonnull String toString()
  {
    return this.name;
  }
}
