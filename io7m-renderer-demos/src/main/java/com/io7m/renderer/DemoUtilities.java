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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable2f;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.FragmentShader;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLMeta;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLScalarType;
import com.io7m.jcanephora.JCGLShadersCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.ShaderUtilities;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.VertexShader;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.KShaderPaths;

public final class DemoUtilities
{
  static @Nonnull ProgramReference makeProgramSingleOutput(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
    final @Nonnull String vertex_shader,
    final @Nonnull String fragment_shader,
    final @Nonnull Log log)
    throws ConstraintError,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      JCGLException
  {
    InputStream stream = null;
    VertexShader v = null;
    FragmentShader f = null;

    final PathVirtual pv =
      KShaderPaths.getShaderPath(version, api, vertex_shader);
    final PathVirtual pf =
      KShaderPaths.getShaderPath(version, api, fragment_shader);

    log.debug("Compiling vertex shader: " + pv);
    log.debug("Compiling fragment shader: " + pf);

    try {
      stream = fs.openFile(pv);
      final List<String> lines = ShaderUtilities.readLines(stream);
      v = gl.vertexShaderCompile(vertex_shader, lines);
    } finally {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    try {
      stream = fs.openFile(pf);
      final List<String> lines = ShaderUtilities.readLines(stream);
      f = gl.fragmentShaderCompile(fragment_shader, lines);
    } finally {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    assert v != null;
    assert f != null;

    final ProgramReference p = gl.programCreateCommon(name, v, f);
    gl.vertexShaderDelete(v);
    gl.fragmentShaderDelete(f);
    return p;
  }

  public static @Nonnull
    <G extends JCGLShadersCommon & JCGLMeta>
    ProgramReference
    makeFlatUVProgram(
      final @Nonnull JCGLInterfaceCommon gl,
      final @Nonnull FSCapabilityRead fs,
      final @Nonnull JCGLSLVersionNumber version,
      final @Nonnull JCGLApi api,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLCompileException,
        JCGLUnsupportedException,
        FilesystemError,
        IOException,
        JCGLException
  {
    return DemoUtilities.makeProgramSingleOutput(
      gl,
      version,
      api,
      fs,
      "flat-uv",
      "standard.v",
      "flat_uv.f",
      log);
  }

  /**
   * Create a textured quad with edges of length <code>size</code> at
   * <code>z = 0</code>.
   */

  public static @Nonnull
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    Pair<ArrayBuffer, IndexBuffer>
    texturedSquare(
      final G gl,
      final int size)
      throws ConstraintError,
        JCGLException
  {
    final ArrayBufferAttributeDescriptor[] ab =
      new ArrayBufferAttributeDescriptor[3];
    ab[0] =
      new ArrayBufferAttributeDescriptor(
        "position",
        JCGLScalarType.TYPE_FLOAT,
        3);
    ab[1] =
      new ArrayBufferAttributeDescriptor(
        "normal",
        JCGLScalarType.TYPE_FLOAT,
        3);
    ab[2] =
      new ArrayBufferAttributeDescriptor("uv", JCGLScalarType.TYPE_FLOAT, 2);
    final ArrayBufferTypeDescriptor array_type =
      new ArrayBufferTypeDescriptor(ab);
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
    gl.arrayBufferUpdate(array_data);

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

    gl.indexBufferUpdate(indices_data);
    return new Pair<ArrayBuffer, IndexBuffer>(array, indices);
  }
}
