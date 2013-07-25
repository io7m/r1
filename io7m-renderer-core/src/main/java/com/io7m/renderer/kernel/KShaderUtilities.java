package com.io7m.renderer.kernel;

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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.GLCompileException;
import com.io7m.jcanephora.GLInterfaceCommon;
import com.io7m.jcanephora.GLUnsupportedException;
import com.io7m.jcanephora.Program;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;

final class KShaderUtilities
{
  static @Nonnull Program makeProgram(
    final @Nonnull GLInterfaceCommon gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
    final @Nonnull String vertex_shader,
    final @Nonnull String fragment_shader,
    final @Nonnull Log log)
    throws ConstraintError,
      GLCompileException,
      GLUnsupportedException
  {
    final boolean is_es = gl.metaIsES();
    final int version_major = gl.metaGetVersionMajor();
    final int version_minor = gl.metaGetVersionMinor();

    final Program program = new Program(name, log);
    program.addVertexShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      vertex_shader));
    program.addFragmentShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      fragment_shader));
    program.compile(fs, gl);
    return program;
  }
}
