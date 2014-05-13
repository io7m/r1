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

package com.io7m.renderer.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.parsers.ParserConfigurationException;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.xml.sax.SAXException;

import com.io7m.jcanephora.FragmentShaderType;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeMissing;
import com.io7m.jcanephora.JCGLExceptionDeleted;
import com.io7m.jcanephora.JCGLExceptionProgramCompileError;
import com.io7m.jcanephora.JCGLExceptionProgramUniformMissing;
import com.io7m.jcanephora.JCGLExceptionTypeError;
import com.io7m.jcanephora.JCGLExceptionUnsupported;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLType;
import com.io7m.jcanephora.ProgramType;
import com.io7m.jcanephora.VertexShaderType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLShadersCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutor;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jcanephora.utilities.ShaderUtilities;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.xml.API;
import com.io7m.jparasol.xml.CompactedShaders;
import com.io7m.jparasol.xml.FragmentParameter;
import com.io7m.jparasol.xml.PGLSLMetaXML;
import com.io7m.jparasol.xml.Version;
import com.io7m.jparasol.xml.VertexInput;
import com.io7m.jparasol.xml.VertexParameter;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RXMLException;

/**
 * A kernel program.
 */

@EqualityStructural public final class KProgram
{
  private static final PathVirtual BASE = KProgram.makeBasePath();

  private static void checkSupport(
    final Version v,
    final PGLSLMetaXML m)
    throws JCGLExceptionUnsupported
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

  private static PathVirtual makeBasePath()
  {
    try {
      return PathVirtual.ofString("/com/io7m/renderer/kernel");
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static API getAPI(
    final JCGLApi api)
  {
    switch (api) {
      case JCGL_ES:
        return API.API_GLSL_ES;
      case JCGL_FULL:
        return API.API_GLSL;
    }

    throw new UnreachableCodeException();
  }

  private static LogUsableType getLog(
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final String name,
    final LogUsableType log)
  {
    final LogUsableType logp = log.with("kprogram");
    if (logp.wouldLog(LogLevel.LOG_DEBUG)) {
      final StringBuilder message = new StringBuilder();
      message.append("Loading ");
      message.append(name);
      message.append(" for ");
      message.append(version);
      message.append(" ");
      message.append(api);
      final String r = message.toString();
      assert r != null;
      logp.debug(r);
    }
    return logp;
  }

  private static PGLSLMetaXML getMeta(
    final FSCapabilityReadType fs,
    final String name,
    final LogUsableType log)
    throws IOException,
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
    final File directory,
    final LogUsableType log)
    throws ParsingException,
      IOException,
      SAXException,
      ParserConfigurationException
  {
    final File meta = new File(directory, "meta.xml");
    final InputStream mf = new FileInputStream(meta);
    try {
      return PGLSLMetaXML.fromStream(mf, log);
    } finally {
      mf.close();
    }
  }

  private static PathVirtual getShaderPath(
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final String name)
    throws JCGLExceptionUnsupported,
      FilesystemError
  {
    return KProgram.getShaderPathDirectory(name).appendName(
      KProgram.getShadingLanguageName(version, api));
  }

  /**
   * Retrieve the path of the directory that contains code and metadata for
   * the shader <code>name</code>.
   * 
   * @param name
   *          The name of the shader
   * @return The shader code path
   */

  public static PathVirtual getShaderPathDirectory(
    final String name)
  {
    try {
      return KProgram.BASE.appendName(name);
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Retrieve the path to the metadata file for the shader.
   * 
   * @param name
   *          The name of the shader
   * @return The path to the metadata file for the shader
   */

  public static PathVirtual getShaderPathMeta(
    final String name)
  {
    try {
      return KProgram.getShaderPathDirectory(name).appendName("meta.xml");
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static String getShadingLanguageName(
    final JCGLSLVersionNumber version,
    final JCGLApi api)
    throws JCGLExceptionUnsupported
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
        throw new JCGLExceptionUnsupported("Unsupported GLSL ES version: "
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
            throw new JCGLExceptionUnsupported("Unsupported GLSL version: "
              + version);
          }
          case 3:
          {
            switch (version.getVersionMinor()) {
              case 30:
                return "glsl-330";
            }
            throw new JCGLExceptionUnsupported("Unsupported GLSL version: "
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

            throw new JCGLExceptionUnsupported("Unsupported GLSL version: "
              + version);
          }
          default:
            throw new JCGLExceptionUnsupported("Unsupported GLSL version: "
              + version);
        }
      }
    }

    throw new UnreachableCodeException();
  }

  private static int getVersion(
    final JCGLSLVersionNumber version)
  {
    return (version.getVersionMajor() * 100) + version.getVersionMinor();
  }

  private static KProgram loadCompacted(
    final JCGLShadersCommonType gl,
    final FSCapabilityReadType fs,
    final Version v,
    final String name,
    final PGLSLMetaXML m,
    final LogUsableType log)
    throws FilesystemError,
      IOException,
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
        final ProgramType p =
          KProgram.newProgramFromStreams(gl, name, v_stream, f_stream, v);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  private static KProgram loadCompactedFromDirectory(
    final JCGLShadersCommonType gl,
    final File directory,
    final Version v,
    final PGLSLMetaXML m,
    final LogUsableType log)
    throws IOException,
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
        final ProgramType p =
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

  private static KProgram loadUncompacted(
    final JCGLShadersCommonType gl,
    final FSCapabilityReadType fs,
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final String name,
    final PGLSLMetaXML m,
    final LogUsableType log)
    throws FilesystemError,
      IOException,
      JCGLException
  {
    final PathVirtual path = KProgram.getShaderPath(version, api, name);
    final PathVirtual path_v = PathVirtual.ofString(path.toString() + ".v");
    final PathVirtual path_f = PathVirtual.ofString(path.toString() + ".f");

    final InputStream v_stream = fs.openFile(path_v);
    try {
      final InputStream f_stream = fs.openFile(path_f);
      try {
        final ProgramType p =
          KProgram.newProgramFromStreams(gl, name, v_stream, f_stream, null);
        return new KProgram(gl, m, p, log);
      } finally {
        f_stream.close();
      }
    } finally {
      v_stream.close();
    }
  }

  private static KProgram loadUncompactedFromDirectory(
    final JCGLShadersCommonType gl,
    final File directory,
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final PGLSLMetaXML m,
    final LogUsableType log)
    throws JCGLExceptionUnsupported,
      IOException,
      JCGLException
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
        final ProgramType p =
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

  private static KProgram newProgramFromDirectory(
    final JCGLShadersCommonType gl,
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final File directory,
    final LogUsableType log)
    throws IOException,
      JCGLException,
      RXMLException
  {
    try {
      NullCheck.notNull(gl, "GL");
      NullCheck.notNull(version, "Version");
      NullCheck.notNull(api, "API");
      NullCheck.notNull(directory, "Directory");
      NullCheck.notNull(log, "Log");

      final String ds = directory.toString();
      assert ds != null;

      final LogUsableType logp = KProgram.getLog(version, api, ds, log);
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
      throw RXMLException.validityException(x);
    } catch (final ParsingException x) {
      throw RXMLException.parsingException(x);
    } catch (final SAXException x) {
      throw RXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw RXMLException.parserConfigurationException(x);
    }
  }

  /**
   * Load the shader named <code>name</code> from the given directory, for the
   * given shading language API and version.
   * 
   * @param gl
   *          The OpenGL interface
   * @param version
   *          The OpenGL version
   * @param api
   *          The OpenGL API
   * @param directory
   *          The directory
   * @param meta
   *          The shader's metadata
   * @param log
   *          A log handle
   * @return A new program
   * @throws JCGLExceptionUnsupported
   *           If the shading program is not supported on the given API and
   *           version
   * @throws IOException
   *           If an I/O error occurs during loading
   * @throws JCGLExceptionProgramCompileError
   *           If the program is invalid
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static KProgram newProgramFromDirectoryMeta(
    final JCGLInterfaceCommonType gl,
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final File directory,
    final PGLSLMetaXML meta,
    final LogUsableType log)
    throws IOException,
      JCGLException,
      JCGLExceptionUnsupported,
      JCGLExceptionProgramCompileError
  {
    NullCheck.notNull(gl, "GL");
    NullCheck.notNull(version, "Version");
    NullCheck.notNull(api, "API");
    NullCheck.notNull(directory, "Filesystem");
    NullCheck.notNull(log, "Log");

    final LogUsableType logp =
      KProgram.getLog(version, api, meta.getName(), log);

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

  /**
   * Load the shader named <code>name</code> from the given filesystem, for
   * the given shading language API and version.
   * 
   * @param gl
   *          The OpenGL interface
   * @param version
   *          The OpenGL version
   * @param api
   *          The OpenGL API
   * @param fs
   *          The filesystem
   * @param name
   *          The name of the shader
   * @param log
   *          A log handle
   * @return A new program
   * 
   * @throws RException
   *           If an error occurs, such as an OpenGL error, or the program not
   *           being supported on the current version and API
   */

  public static KProgram newProgramFromFilesystem(
    final JCGLShadersCommonType gl,
    final JCGLSLVersionNumber version,
    final JCGLApi api,
    final FSCapabilityReadType fs,
    final String name,
    final LogUsableType log)
    throws RException
  {
    try {
      NullCheck.notNull(gl, "GL");
      NullCheck.notNull(version, "Version");
      NullCheck.notNull(api, "API");
      NullCheck.notNull(fs, "Filesystem");
      NullCheck.notNull(name, "Name");
      NullCheck.notNull(log, "Log");

      final LogUsableType logp = KProgram.getLog(version, api, name, log);

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
      throw RXMLException.validityException(x);
    } catch (final ParsingException x) {
      throw RXMLException.parsingException(x);
    } catch (final SAXException x) {
      throw RXMLException.saxException(x);
    } catch (final ParserConfigurationException x) {
      throw RXMLException.parserConfigurationException(x);
    } catch (final IOException e) {
      throw RException.fromIOException(e);
    } catch (final FilesystemError e) {
      throw RException.fromFilesystemException(e);
    } catch (final JCGLException e) {
      throw RException.fromJCGLException(e);
    }
  }

  private static ProgramType newProgramFromStreams(
    final JCGLShadersCommonType gl,
    final String name,
    final InputStream v_stream,
    final InputStream f_stream,
    final @Nullable Version prepend)
    throws IOException,
      JCGLException
  {
    NullCheck.notNull(gl, "GL");
    NullCheck.notNull(name, "Name");
    NullCheck.notNull(v_stream, "Vertex shader stream");
    NullCheck.notNull(f_stream, "Fragment shader stream");

    VertexShaderType v = null;
    FragmentShaderType f = null;

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

    final ProgramType p = gl.programCreateCommon(name, v, f);
    gl.vertexShaderDelete(v);
    gl.fragmentShaderDelete(f);
    return p;
  }

  private static void notSupported(
    final Version v,
    final PGLSLMetaXML m)
    throws JCGLExceptionUnsupported
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
    final String r = message.toString();
    assert r != null;
    throw new JCGLExceptionUnsupported(r);
  }

  private static void prependVersion(
    final List<String> lines,
    final Version prepend)
  {
    final StringBuilder directive = new StringBuilder();
    directive.append("#version ");
    directive.append(prepend.getVersion());
    directive.append("\n");
    lines.add(0, directive.toString());
  }

  private final Map<String, JCGLType> declared_attributes;
  private final Map<String, JCGLType> declared_uniforms;
  private final JCBExecutorType       exec;
  private final PGLSLMetaXML          meta;
  private final ProgramType           program;

  private KProgram(
    final JCGLShadersCommonType gl,
    final PGLSLMetaXML in_meta,
    final ProgramType in_program,
    final LogUsableType log)
    throws JCGLExceptionDeleted,
      JCGLExceptionProgramUniformMissing,
      JCGLExceptionTypeError,
      JCGLExceptionAttributeMissing
  {
    this.meta = NullCheck.notNull(in_meta, "Meta");
    this.program = NullCheck.notNull(in_program, "Program");
    this.declared_uniforms = new HashMap<String, JCGLType>();
    this.declared_attributes = new HashMap<String, JCGLType>();

    for (final VertexParameter p : in_meta.getDeclaredVertexParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }
    for (final FragmentParameter p : in_meta.getDeclaredFragmentParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }
    for (final VertexInput p : in_meta.getDeclaredVertexInputs()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_attributes.put(p.getName(), t);
    }

    this.exec =
      JCBExecutor.newExecutorWithDeclarations(
        gl,
        in_program,
        this.declared_uniforms,
        this.declared_attributes,
        log);
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KProgram other = (KProgram) obj;
    return (this.declared_attributes.equals(other.declared_attributes))
      && (this.declared_uniforms.equals(other.declared_uniforms))
      && (this.exec.equals(other.exec))
      && (this.meta.equals(other.meta))
      && (this.program.equals(other.program));
  }

  /**
   * @return A reference to the program's executable interface
   */

  public JCBExecutorType getExecutable()
  {
    return this.exec;
  }

  /**
   * @return Information about the compiled program
   */

  public PGLSLMetaXML getMeta()
  {
    return this.meta;
  }

  /**
   * @return The compiled program
   */

  public ProgramType getProgram()
  {
    return this.program;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.declared_attributes.hashCode();
    result = (prime * result) + this.declared_uniforms.hashCode();
    result = (prime * result) + this.exec.hashCode();
    result = (prime * result) + this.meta.hashCode();
    result = (prime * result) + this.program.hashCode();
    return result;
  }
}
