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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.FragmentShader;
import com.io7m.jcanephora.JCBExecutionAPI;
import com.io7m.jcanephora.JCBExecutor;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLShadersCommon;
import com.io7m.jcanephora.JCGLType;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.ShaderUtilities;
import com.io7m.jcanephora.VertexShader;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jparasol.xml.API;
import com.io7m.jparasol.xml.CompactedShaders;
import com.io7m.jparasol.xml.FragmentParameter;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.jparasol.xml.Version;
import com.io7m.jparasol.xml.VertexInput;
import com.io7m.jparasol.xml.VertexParameter;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;

@Immutable public final class KProgram
{
  private static @Nonnull PathVirtual BASE;

  static {
    try {
      KProgram.BASE = PathVirtual.ofString("/com/io7m/renderer/kernel");
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException();
    }
  }

  private static void checkSupport(
    final @Nonnull Version v,
    final @Nonnull PGLSLMetaXML m)
    throws JCGLUnsupportedException
  {
    final Integer vn = Integer.valueOf(v.getVersion());

    switch (v.getAPI()) {
      case API_GLSL:
      {
        if (m.getSupportsFull().contains(vn) == false) {
          KProgram.notSupported(v, m);
        }
        break;
      }
      case API_GLSL_ES:
      {
        if (m.getSupportsES().contains(vn) == false) {
          KProgram.notSupported(v, m);
        }
        break;
      }
    }
  }

  private static @Nonnull API getAPI(
    final @Nonnull JCGLApi api)
  {
    switch (api) {
      case JCGL_ES:
        return API.API_GLSL_ES;
      case JCGL_FULL:
        return API.API_GLSL;
    }

    throw new UnreachableCodeException();
  }

  private static Log getLog(
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final String name,
    final Log log)
  {
    final Log logp = new Log(log, "kprogram");
    if (logp.enabled(Level.LOG_DEBUG)) {
      final StringBuilder message = new StringBuilder();
      message.append("Loading ");
      message.append(name);
      message.append(" for ");
      message.append(version);
      message.append(" ");
      message.append(api);
      logp.debug(message.toString());
    }
    return logp;
  }

  private static @Nonnull PGLSLMetaXML getMeta(
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
    final @Nonnull Log log)
    throws IOException,
      ConstraintError,
      FilesystemError,
      ValidityException,
      ParsingException,
      SAXException,
      ParserConfigurationException
  {
    final PathVirtual path = KProgram.getShaderPathMeta(name);
    final InputStream mf = fs.openFile(path);
    try {
      return PGLSLMetaXML.fromStream(mf, log);
    } finally {
      mf.close();
    }
  }

  private static PGLSLMetaXML getMetaFromDirectory(
    final @Nonnull File directory,
    final @Nonnull Log log)
    throws ParsingException,
      IOException,
      SAXException,
      ParserConfigurationException,
      ConstraintError
  {
    final File meta = new File(directory, "meta.xml");
    final InputStream mf = new FileInputStream(meta);
    try {
      return PGLSLMetaXML.fromStream(mf, log);
    } finally {
      mf.close();
    }
  }

  public static @Nonnull PathVirtual getShaderPath(
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull String name)
    throws ConstraintError,
      JCGLUnsupportedException
  {
    return KProgram.getShaderPathDirectory(name).appendName(
      KProgram.getShadingLanguageName(version, api));
  }

  public static @Nonnull PathVirtual getShaderPathDirectory(
    final @Nonnull String name)
    throws ConstraintError
  {
    return KProgram.BASE.appendName(name);
  }

  public static @Nonnull PathVirtual getShaderPathMeta(
    final @Nonnull String name)
    throws ConstraintError
  {
    return KProgram.getShaderPathDirectory(name).appendName("meta.xml");
  }

  private static @Nonnull String getShadingLanguageName(
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api)
    throws JCGLUnsupportedException
  {
    switch (api) {
      case JCGL_ES:
      {
        if (version.getVersionMajor() == 1) {
          return "glsl-es-100";
        }
        if (version.getVersionMajor() == 3) {
          return "glsl-es-300";
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
                return "glsl-110";
              case 20:
                return "glsl-120";
              case 30:
                return "glsl-130";
              case 40:
                return "glsl-140";
              case 50:
                return "glsl-150";
            }
            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
          }
          case 3:
          {
            switch (version.getVersionMinor()) {
              case 30:
                return "glsl-330";
            }
            throw new JCGLUnsupportedException("Unsupported GLSL version: "
              + version);
          }
          case 4:
          {
            switch (version.getVersionMinor()) {
              case 0:
                return "glsl-400";
              case 10:
                return "glsl-410";
              case 20:
                return "glsl-420";
              case 30:
                return "glsl-430";
              case 40:
                return "glsl-440";
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

  private static int getVersion(
    final @Nonnull JCGLSLVersionNumber version)
  {
    return (version.getVersionMajor() * 100) + version.getVersionMinor();
  }

  private static @Nonnull KProgram loadCompacted(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Version v,
    final @Nonnull String name,
    final @Nonnull PGLSLMetaXML m,
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError,
      IOException,
      JCGLCompileException,
      JCGLException
  {
    final SortedMap<Version, CompactedShaders> mappings =
      m.getCompactMappings();
    assert mappings.containsKey(v);
    final CompactedShaders cs = mappings.get(v);

    final PathVirtual path = KProgram.getShaderPathDirectory(name);
    final PathVirtual path_v = path.appendName(cs.getVertexShader() + ".g");
    final PathVirtual path_f = path.appendName(cs.getFragmentShader() + ".g");

    final InputStream v_stream = fs.openFile(path_v);
    try {
      final InputStream f_stream = fs.openFile(path_f);
      try {
        final ProgramReference p =
          KProgram.newProgramFromStreams(gl, name, v_stream, f_stream, v);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  private static @Nonnull KProgram loadCompactedFromDirectory(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull File directory,
    final @Nonnull Version v,
    final @Nonnull PGLSLMetaXML m,
    final @Nonnull Log log)
    throws ConstraintError,
      IOException,
      JCGLCompileException,
      JCGLException
  {
    final SortedMap<Version, CompactedShaders> mappings =
      m.getCompactMappings();
    assert mappings.containsKey(v);
    final CompactedShaders cs = mappings.get(v);

    final File path_v = new File(directory, cs.getVertexShader() + ".g");
    final File path_f = new File(directory, cs.getFragmentShader() + ".g");

    final InputStream v_stream = new FileInputStream(path_v);
    try {
      final InputStream f_stream = new FileInputStream(path_f);
      try {
        final ProgramReference p =
          KProgram.newProgramFromStreams(
            gl,
            m.getName(),
            v_stream,
            f_stream,
            v);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  private static @Nonnull KProgram loadUncompacted(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull String name,
    final @Nonnull PGLSLMetaXML m,
    final @Nonnull Log log)
    throws JCGLUnsupportedException,
      ConstraintError,
      FilesystemError,
      IOException,
      JCGLCompileException,
      JCGLException
  {
    final PathVirtual path = KProgram.getShaderPath(version, api, name);
    final PathVirtual path_v = PathVirtual.ofString(path.toString() + ".v");
    final PathVirtual path_f = PathVirtual.ofString(path.toString() + ".f");

    final InputStream v_stream = fs.openFile(path_v);
    try {
      final InputStream f_stream = fs.openFile(path_f);
      try {
        final ProgramReference p =
          KProgram.newProgramFromStreams(gl, name, v_stream, f_stream, null);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  private static @Nonnull KProgram loadUncompactedFromDirectory(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull File directory,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull PGLSLMetaXML m,
    final @Nonnull Log log)
    throws JCGLUnsupportedException,
      IOException,
      JCGLCompileException,
      JCGLException,
      ConstraintError
  {
    final String name_v =
      KProgram.getShadingLanguageName(version, api) + ".v";
    final String name_f =
      KProgram.getShadingLanguageName(version, api) + ".f";

    final File path_v = new File(directory, name_v);
    final File path_f = new File(directory, name_f);

    final InputStream v_stream = new FileInputStream(path_v);
    try {
      final InputStream f_stream = new FileInputStream(path_f);
      try {
        final ProgramReference p =
          KProgram.newProgramFromStreams(
            gl,
            m.getName(),
            v_stream,
            f_stream,
            null);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  public static @Nonnull KProgram newProgramFromDirectory(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull File directory,
    final @Nonnull Log log)
    throws ConstraintError,
      IOException,
      JCGLUnsupportedException,
      JCGLCompileException,
      JCGLException,
      KXMLException
  {
    try {
      Constraints.constrainNotNull(gl, "GL");
      Constraints.constrainNotNull(version, "Version");
      Constraints.constrainNotNull(api, "API");
      Constraints.constrainNotNull(directory, "Filesystem");
      Constraints.constrainNotNull(log, "Log");

      final Log logp =
        KProgram.getLog(version, api, directory.toString(), log);
      final Version v =
        Version
          .newVersion(KProgram.getVersion(version), KProgram.getAPI(api));
      final PGLSLMetaXML m = KProgram.getMetaFromDirectory(directory, logp);
      KProgram.checkSupport(v, m);

      if (m.isCompacted()) {
        return KProgram.loadCompactedFromDirectory(gl, directory, v, m, logp);
      }
      return KProgram.loadUncompactedFromDirectory(
        gl,
        directory,
        version,
        api,
        m,
        logp);
    } catch (final ValidityException x) {
      throw KXMLException.validityException(x);
    } catch (final ParsingException x) {
      throw KXMLException.parsingException(x);
    } catch (final SAXException x) {
      throw KXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw KXMLException.parserConfigurationException(x);
    }
  }

  public static @Nonnull KProgram newProgramFromDirectoryMeta(
    final @Nonnull JCGLInterfaceCommon gl,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull File directory,
    final @Nonnull PGLSLMetaXML meta,
    final @Nonnull Log log)
    throws ConstraintError,
      JCGLUnsupportedException,
      IOException,
      JCGLCompileException,
      JCGLException
  {
    Constraints.constrainNotNull(gl, "GL");
    Constraints.constrainNotNull(version, "Version");
    Constraints.constrainNotNull(api, "API");
    Constraints.constrainNotNull(directory, "Filesystem");
    Constraints.constrainNotNull(log, "Log");

    final Log logp = KProgram.getLog(version, api, meta.getName(), log);

    final Version v =
      Version.newVersion(KProgram.getVersion(version), KProgram.getAPI(api));
    KProgram.checkSupport(v, meta);

    if (meta.isCompacted()) {
      return KProgram
        .loadCompactedFromDirectory(gl, directory, v, meta, logp);
    }
    return KProgram.loadUncompactedFromDirectory(
      gl,
      directory,
      version,
      api,
      meta,
      logp);
  }

  public static @Nonnull KProgram newProgramFromFilesystem(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull JCGLSLVersionNumber version,
    final @Nonnull JCGLApi api,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull String name,
    final @Nonnull Log log)
    throws ConstraintError,
      FilesystemError,
      IOException,
      JCGLUnsupportedException,
      JCGLCompileException,
      JCGLException,
      KXMLException
  {
    try {
      Constraints.constrainNotNull(gl, "GL");
      Constraints.constrainNotNull(version, "Version");
      Constraints.constrainNotNull(api, "API");
      Constraints.constrainNotNull(fs, "Filesystem");
      Constraints.constrainNotNull(name, "Name");
      Constraints.constrainNotNull(log, "Log");

      final Log logp = KProgram.getLog(version, api, name, log);

      final Version v =
        Version
          .newVersion(KProgram.getVersion(version), KProgram.getAPI(api));

      final PGLSLMetaXML m = KProgram.getMeta(fs, name, logp);
      KProgram.checkSupport(v, m);

      if (m.isCompacted()) {
        return KProgram.loadCompacted(gl, fs, v, name, m, logp);
      }
      return KProgram.loadUncompacted(gl, fs, version, api, name, m, logp);

    } catch (final ValidityException x) {
      throw KXMLException.validityException(x);
    } catch (final ParsingException x) {
      throw KXMLException.parsingException(x);
    } catch (final SAXException x) {
      throw KXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw KXMLException.parserConfigurationException(x);
    }
  }

  public static @Nonnull ProgramReference newProgramFromStreams(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull String name,
    final @Nonnull InputStream v_stream,
    final @Nonnull InputStream f_stream,
    final @CheckForNull Version prepend)
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
    if (prepend != null) {
      KProgram.prependVersion(v_lines, prepend);
    }
    v = gl.vertexShaderCompile(name, v_lines);

    final List<String> f_lines = ShaderUtilities.readLines(f_stream);
    if (prepend != null) {
      KProgram.prependVersion(f_lines, prepend);
    }
    f = gl.fragmentShaderCompile(name, f_lines);

    assert v != null;
    assert f != null;

    final ProgramReference p = gl.programCreateCommon(name, v, f);
    gl.vertexShaderDelete(v);
    gl.fragmentShaderDelete(f);
    return p;
  }

  private static void notSupported(
    final @Nonnull Version v,
    final @Nonnull PGLSLMetaXML m)
    throws JCGLUnsupportedException
  {
    final StringBuilder message = new StringBuilder();
    message.append("GLSL version ");
    message.append(v.getVersion());
    message.append(" is not supported by program ");
    message.append(m.getName());
    message.append(".\n");
    message.append("Supported versions are: ");

    for (final Integer ver : m.getSupportsES()) {
      message.append("GLSL ES ");
      message.append(ver);
      message.append("\n");
    }
    for (final Integer ver : m.getSupportsFull()) {
      message.append("GLSL ");
      message.append(ver);
      message.append("\n");
    }
    throw new JCGLUnsupportedException(message.toString());
  }

  private static void prependVersion(
    final @Nonnull List<String> lines,
    final @Nonnull Version prepend)
  {
    final StringBuilder directive = new StringBuilder();
    directive.append("#version ");
    directive.append(prepend.getVersion());
    directive.append("\n");
    lines.add(0, directive.toString());
  }

  private final @Nonnull HashMap<String, JCGLType> declared_attributes;
  private final @Nonnull HashMap<String, JCGLType> declared_uniforms;
  private final @Nonnull JCBExecutionAPI           exec;
  private final @Nonnull PGLSLMetaXML              meta;
  private final @Nonnull ProgramReference          program;

  private KProgram(
    final @Nonnull JCGLShadersCommon gl,
    final @Nonnull PGLSLMetaXML meta,
    final @Nonnull ProgramReference program,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.meta = Constraints.constrainNotNull(meta, "Meta");
    this.program = Constraints.constrainNotNull(program, "Program");
    this.declared_uniforms = new HashMap<String, JCGLType>();
    this.declared_attributes = new HashMap<String, JCGLType>();

    for (final VertexParameter p : meta.getDeclaredVertexParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }
    for (final FragmentParameter p : meta.getDeclaredFragmentParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }
    for (final VertexInput p : meta.getDeclaredVertexInputs()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_attributes.put(p.getName(), t);
    }

    this.exec =
      JCBExecutor.newExecutorWithDeclarations(
        gl,
        program,
        this.declared_uniforms,
        this.declared_attributes,
        log);
  }

  public @Nonnull JCBExecutionAPI getExecutable()
  {
    return this.exec;
  }

  public @Nonnull PGLSLMetaXML getMeta()
  {
    return this.meta;
  }

  public @Nonnull ProgramReference getProgram()
  {
    return this.program;
  }
}
