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

package com.io7m.renderer;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttribute;
import com.io7m.jcanephora.ArrayBufferDescriptor;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable2f;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.GLArrayBuffers;
import com.io7m.jcanephora.GLCompileException;
import com.io7m.jcanephora.GLException;
import com.io7m.jcanephora.GLIndexBuffers;
import com.io7m.jcanephora.GLInterfaceCommon;
import com.io7m.jcanephora.GLMeta;
import com.io7m.jcanephora.GLScalarType;
import com.io7m.jcanephora.GLShaders;
import com.io7m.jcanephora.GLUnsupportedException;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.Program;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.renderer.kernel.KShaderPaths;

public final class DemoUtilities
{
  public static @Nonnull
    <G extends GLShaders & GLMeta>
    Program
    makeFlatUVProgram(
      final @Nonnull GLInterfaceCommon gl,
      final @Nonnull FSCapabilityRead fs,
      final @Nonnull Log log)
      throws ConstraintError,
        GLCompileException,
        GLUnsupportedException
  {
    final boolean is_es = gl.metaIsES();
    final int version_major = gl.metaGetVersionMajor();
    final int version_minor = gl.metaGetVersionMinor();

    final Program program = new Program("flat-uv", log);
    program.addVertexShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      "standard.v"));
    program.addFragmentShader(KShaderPaths.getShader(
      is_es,
      version_major,
      version_minor,
      "flat_uv.f"));

    program.compile(fs, gl);
    return program;
  }

  /**
   * Create a textured quad with edges of length <code>size</code> at
   * <code>z = 0</code>.
   */

  public static @Nonnull
    <G extends GLArrayBuffers & GLIndexBuffers>
    Pair<ArrayBuffer, IndexBuffer>
    texturedSquare(
      final G gl,
      final int size)
      throws ConstraintError,
        GLException
  {
    final ArrayBufferAttribute[] ab = new ArrayBufferAttribute[3];
    ab[0] = new ArrayBufferAttribute("position", GLScalarType.TYPE_FLOAT, 3);
    ab[1] = new ArrayBufferAttribute("normal", GLScalarType.TYPE_FLOAT, 3);
    ab[2] = new ArrayBufferAttribute("uv", GLScalarType.TYPE_FLOAT, 2);
    final ArrayBufferDescriptor array_type = new ArrayBufferDescriptor(ab);
    final ArrayBuffer array =
      gl.arrayBufferAllocate(4, array_type, UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferWritableData array_data =
      new ArrayBufferWritableData(array);

    {
      final CursorWritable3f pos_cursor = array_data.getCursor3f("position");
      final CursorWritable3f norm_cursor = array_data.getCursor3f("normal");
      final CursorWritable2f uv_cursor = array_data.getCursor2f("uv");

      pos_cursor.put3f(-size, size, 0f);
      pos_cursor.put3f(-size, -size, 0f);
      pos_cursor.put3f(size, -size, 0f);
      pos_cursor.put3f(size, size, 0f);

      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);

      uv_cursor.put2f(0.0f, 1.0f);
      uv_cursor.put2f(0.0f, 0.0f);
      uv_cursor.put2f(1.0f, 0.0f);
      uv_cursor.put2f(1.0f, 1.0f);
    }

    gl.arrayBufferBind(array);
    gl.arrayBufferUpdate(array, array_data);

    final IndexBuffer indices = gl.indexBufferAllocate(array, 6);
    final IndexBufferWritableData indices_data =
      new IndexBufferWritableData(indices);

    {
      final CursorWritableIndex ind_cursor = indices_data.getCursor();
      ind_cursor.putIndex(0);
      ind_cursor.putIndex(1);
      ind_cursor.putIndex(2);

      ind_cursor.putIndex(0);
      ind_cursor.putIndex(2);
      ind_cursor.putIndex(3);
    }

    gl.indexBufferUpdate(indices, indices_data);
    return new Pair<ArrayBuffer, IndexBuffer>(array, indices);
  }
}
