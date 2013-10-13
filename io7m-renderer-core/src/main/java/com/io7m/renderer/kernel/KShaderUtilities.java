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

import com.io7m.jaux.Constraints;
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

public final class KShaderUtilities
{
  public static @Nonnull ProgramReference makeProgramFromStreams(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull String name,
    final @Nonnull InputStream v_stream,
    final @Nonnull InputStream f_stream)
    throws IOException,
      ConstraintError,
      JCGLCompileException,
      JCGLException
  {
    Constraints.constrainNotNull(gl, "GL");
    Constraints.constrainNotNull(name, "Name");
    Constraints.constrainNotNull(v_stream, "Vertex shader stream");
    Constraints.constrainNotNull(f_stream, "Fragment shader stream");

    VertexShader v = null;
    FragmentShader f = null;

    final List<String> v_lines = ShaderUtilities.readLines(v_stream);
    v = gl.vertexShaderCompile(name, v_lines);
    final List<String> f_lines = ShaderUtilities.readLines(f_stream);
    f = gl.fragmentShaderCompile(name, f_lines);

    assert v != null;
    assert f != null;

    final ProgramReference p = gl.programCreateCommon(name, v, f);
    gl.vertexShaderDelete(v);
    gl.fragmentShaderDelete(f);
    return p;
  }

  public static @Nonnull ProgramReference makeProgram(
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
    Constraints.constrainNotNull(gl, "GL");
    Constraints.constrainNotNull(version, "Version");
    Constraints.constrainNotNull(api, "API");
    Constraints.constrainNotNull(fs, "Filesystem");
    Constraints.constrainNotNull(name, "Name");
    Constraints.constrainNotNull(log, "Log");

    InputStream v_stream = null;
    InputStream f_stream = null;

    log.debug("Shading language " + version + " " + api);

    final PathVirtual pv = KShaderPaths.getShaderPath(version, api, name);
    final PathVirtual pf = KShaderPaths.getShaderPath(version, api, name);

    final PathVirtual pvn = PathVirtual.ofString(pv.toString() + ".v");
    final PathVirtual pfn = PathVirtual.ofString(pf.toString() + ".f");

    log.debug("Compiling vertex shader: " + pvn);
    log.debug("Compiling fragment shader: " + pfn);

    try {
      v_stream = fs.openFile(pvn);
      f_stream = fs.openFile(pfn);

      return KShaderUtilities.makeProgramFromStreams(
        gl,
        name,
        v_stream,
        f_stream);

    } finally {
      if (v_stream != null) {
        v_stream.close();
      }
      if (f_stream != null) {
        f_stream.close();
      }
    }
  }
}
