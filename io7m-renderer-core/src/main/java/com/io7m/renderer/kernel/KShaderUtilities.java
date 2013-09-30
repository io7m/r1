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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.FragmentShader;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLShadersCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.ShaderUtilities;
import com.io7m.jcanephora.VertexShader;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

final class KShaderUtilities
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

    log.debug("Shading language " + version + " " + api);

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

  static @Nonnull ProgramReference makeParasolProgramSingleOutput(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
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

    log.debug("Shading language " + version + " " + api);

    final PathVirtual pv =
      KShaderPaths.getShaderPathParasol(version, api, name);
    final PathVirtual pf =
      KShaderPaths.getShaderPathParasol(version, api, name);

    final PathVirtual pvn = PathVirtual.ofString(pv.toString() + ".v");
    final PathVirtual pfn = PathVirtual.ofString(pf.toString() + ".f");

    log.debug("Compiling vertex shader: " + pvn);
    log.debug("Compiling fragment shader: " + pfn);

    try {
      stream = fs.openFile(pvn);
      final List<String> lines = ShaderUtilities.readLines(stream);
      v = gl.vertexShaderCompile(name, lines);
    } finally {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    try {
      stream = fs.openFile(pfn);
      final List<String> lines = ShaderUtilities.readLines(stream);
      f = gl.fragmentShaderCompile(name, lines);
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
}
