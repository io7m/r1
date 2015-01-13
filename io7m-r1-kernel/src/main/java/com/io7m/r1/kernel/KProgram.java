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

package com.io7m.r1.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.io7m.jcanephora.FragmentShaderType;
import com.io7m.jcanephora.FramebufferDrawBufferType;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeMissing;
import com.io7m.jcanephora.JCGLExceptionDeleted;
import com.io7m.jcanephora.JCGLExceptionProgramCompileError;
import com.io7m.jcanephora.JCGLExceptionProgramUniformMissing;
import com.io7m.jcanephora.JCGLExceptionTypeError;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLType;
import com.io7m.jcanephora.ProgramType;
import com.io7m.jcanephora.VertexShaderType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLImplementationVisitorType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLInterfaceGL2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGL3Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES2Type;
import com.io7m.jcanephora.api.JCGLInterfaceGLES3Type;
import com.io7m.jcanephora.api.JCGLShadersCommonType;
import com.io7m.jcanephora.batchexec.JCBExecutor;
import com.io7m.jcanephora.batchexec.JCBExecutorType;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jparasol.core.GVersionES;
import com.io7m.jparasol.core.GVersionFull;
import com.io7m.jparasol.core.GVersionType;
import com.io7m.jparasol.core.GVersionVisitorType;
import com.io7m.jparasol.core.JPCompiledShaderMetaType;
import com.io7m.jparasol.core.JPFragmentOutput;
import com.io7m.jparasol.core.JPFragmentParameter;
import com.io7m.jparasol.core.JPFragmentShaderMetaType;
import com.io7m.jparasol.core.JPSourceLines;
import com.io7m.jparasol.core.JPUncompactedProgramShaderMeta;
import com.io7m.jparasol.core.JPVertexInput;
import com.io7m.jparasol.core.JPVertexParameter;
import com.io7m.jparasol.core.JPVertexShaderMetaType;
import com.io7m.jparasol.metaserializer.JPMetaDeserializerType;
import com.io7m.jparasol.metaserializer.JPSerializerException;
import com.io7m.jparasol.metaserializer.protobuf.JPProtobufMetaDeserializer;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FSCapabilityReadType;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionFilesystem;
import com.io7m.r1.exceptions.RExceptionIO;
import com.io7m.r1.exceptions.RExceptionNotSupported;
import com.io7m.r1.exceptions.RExceptionProgramInvalid;
import com.io7m.r1.exceptions.RExceptionShaderFragmentConflictingOutputs;

/**
 * A kernel program.
 */

@SuppressWarnings({ "boxing" }) @EqualityStructural public final class KProgram implements
  KProgramType
{
  private static void checkProgramVersionSupport(
    final GVersionType v,
    final PathVirtual program_base,
    final JPUncompactedProgramShaderMeta program_meta)
    throws RException
  {
    v.versionAccept(new GVersionVisitorType<Unit, RException>() {
      @Override public Unit versionVisitES(
        final GVersionES ve)
        throws RException
      {
        if (program_meta.getSupportsES().contains(ve) == false) {
          throw RExceptionNotSupported.programNotSupported(
            program_base.toString(),
            program_meta.getSupportsES(),
            program_meta.getSupportsFull());
        }
        return Unit.unit();
      }

      @Override public Unit versionVisitFull(
        final GVersionFull vf)
        throws RException
      {
        if (program_meta.getSupportsFull().contains(vf) == false) {
          throw RExceptionNotSupported.programNotSupported(
            program_base.toString(),
            program_meta.getSupportsES(),
            program_meta.getSupportsFull());
        }
        return Unit.unit();
      }
    });
  }

  private static
    JPFragmentShaderMetaType
    getFragmentShaderMetaFromFilesystem(
      final JPMetaDeserializerType deserial,
      final FSCapabilityReadType fs,
      final PathVirtual path,
      final LogUsableType log)
      throws RException
  {
    InputStream stream = null;
    try {
      try {
        stream =
          fs.openFile(path.appendName(deserial.metaGetSuggestedFilename()));
        return deserial.metaDeserializeFragmentShader(stream);
      } catch (final JPSerializerException e) {
        throw new RExceptionProgramInvalid(e);
      } finally {
        if (stream != null) {
          stream.close();
        }
      }
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }

  private static JPUncompactedProgramShaderMeta getProgramMetaFromFilesystem(
    final JPMetaDeserializerType deserial,
    final FSCapabilityReadType fs,
    final PathVirtual name,
    final LogUsableType log)
    throws FilesystemError,
      IOException,
      RExceptionProgramInvalid
  {
    InputStream stream = null;
    try {
      stream = fs.openFile(name.appendName(deserial.metaGetSuggestedFilename()));
      return deserial.metaDeserializeProgramShaderUncompacted(stream);
    } catch (final JPSerializerException e) {
      throw new RExceptionProgramInvalid(e);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  private static JPVertexShaderMetaType getVertexShaderMetaFromFilesystem(
    final JPMetaDeserializerType deserial,
    final FSCapabilityReadType fs,
    final PathVirtual path,
    final LogUsableType log)
    throws RException
  {
    InputStream stream = null;
    try {
      try {
        stream = fs.openFile(path.appendName(deserial.metaGetSuggestedFilename()));
        return deserial.metaDeserializeVertexShader(stream);
      } catch (final JPSerializerException e) {
        throw new RExceptionProgramInvalid(e);
      } finally {
        if (stream != null) {
          stream.close();
        }
      }
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }

  private static Map<String, FramebufferDrawBufferType> makeOutputMappings(
    final JCGLInterfaceCommonType gc,
    final JPFragmentShaderMetaType meta)
    throws JCGLException,
      RExceptionNotSupported,
      RExceptionShaderFragmentConflictingOutputs
  {
    final List<FramebufferDrawBufferType> buffers =
      gc.framebufferGetDrawBuffers();
    final SortedMap<Integer, JPFragmentOutput> outs =
      meta.getDeclaredFragmentOutputs();

    final Map<String, FramebufferDrawBufferType> results =
      new HashMap<String, FramebufferDrawBufferType>();

    for (final Integer index : outs.keySet()) {
      final JPFragmentOutput o = outs.get(index);

      if (index.intValue() >= buffers.size()) {
        throw RExceptionNotSupported.notEnoughDrawBuffers(
          meta.getName(),
          buffers.size(),
          outs.size());
      }

      if (results.containsKey(o.getName())) {
        final String s =
          String.format("Output '%s' already specified", o.getName());
        assert s != null;
        throw new RExceptionShaderFragmentConflictingOutputs(s);
      }

      final FramebufferDrawBufferType buffer = buffers.get(index.intValue());
      assert buffer != null;
      results.put(o.getName(), buffer);
    }

    return results;
  }

  private static ProgramType makeProgram(
    final JCGLImplementationType gl,
    final String name,
    final Map<String, FramebufferDrawBufferType> output_mappings,
    final FragmentShaderType fragment_shader,
    final VertexShaderType vertex_shader)
    throws JCGLException,
      RException
  {
    final ProgramType p =
      gl
        .implementationAccept(new JCGLImplementationVisitorType<ProgramType, RException>() {
          @Override public ProgramType implementationIsGL2(
            final JCGLInterfaceGL2Type gl2)
            throws JCGLException,
              RException
          {
            return gl2.programCreateCommon(
              name,
              vertex_shader,
              fragment_shader);
          }

          @Override public ProgramType implementationIsGL3(
            final JCGLInterfaceGL3Type gl3)
            throws JCGLException,
              RException
          {
            return gl3.programCreateWithOutputs(
              name,
              vertex_shader,
              fragment_shader,
              output_mappings);
          }

          @Override public ProgramType implementationIsGLES2(
            final JCGLInterfaceGLES2Type gles2)
            throws JCGLException,
              RException
          {
            return gles2.programCreateCommon(
              name,
              vertex_shader,
              fragment_shader);
          }

          @Override public ProgramType implementationIsGLES3(
            final JCGLInterfaceGLES3Type gles3)
            throws JCGLException,
              RException
          {
            return gles3.programCreateCommon(
              name,
              vertex_shader,
              fragment_shader);
          }
        });
    return p;
  }

  private static FragmentShaderType newFragmentShaderFromFilesystem(
    final JCGLShadersCommonType gc,
    final GVersionType version,
    final FSCapabilityReadType fs,
    final PathVirtual path,
    final JPFragmentShaderMetaType meta)
    throws FilesystemError,
      IOException,
      JCGLExceptionProgramCompileError,
      JCGLException
  {
    final List<String> source =
      KProgram.newSourceFromFilesystem(version, fs, path, meta);

    return gc.fragmentShaderCompile(path.toString(), source);
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
   *
   * @return A new program
   *
   * @throws RException
   *           If an error occurs, such as an OpenGL error, or the program not
   *           being supported on the current version and API
   */

  public static KProgram newProgramFromFilesystem(
    final JCGLImplementationType gl,
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

      final JCGLInterfaceCommonType gc = gl.getGLCommon();
      final GVersionType v = KProgram.versionNumber(version, api);

      final PathVirtual program_base = PathVirtual.ROOT.appendName(name);
      final JPMetaDeserializerType d =
        JPProtobufMetaDeserializer.newDeserializer();
      final JPUncompactedProgramShaderMeta program_meta =
        KProgram.getProgramMetaFromFilesystem(d, fs, program_base, log);

      KProgram.checkProgramVersionSupport(v, program_base, program_meta);

      final String fragment_shader_name = program_meta.getFragmentShader();
      assert fragment_shader_name != null;
      final PathVirtual fragment_shader_path =
        PathVirtual.ROOT.appendName(fragment_shader_name);
      final JPFragmentShaderMetaType fragment_shader_meta =
        KProgram.getFragmentShaderMetaFromFilesystem(
          d,
          fs,
          fragment_shader_path,
          log);
      final FragmentShaderType fragment_shader =
        KProgram.newFragmentShaderFromFilesystem(
          gc,
          v,
          fs,
          fragment_shader_path,
          fragment_shader_meta);

      final String vertex_shader_name =
        program_meta.getVertexShaders().first();
      assert vertex_shader_name != null;
      final PathVirtual vertex_shader_path =
        PathVirtual.ROOT.appendName(vertex_shader_name);
      final JPVertexShaderMetaType vertex_shader_meta =
        KProgram.getVertexShaderMetaFromFilesystem(
          d,
          fs,
          vertex_shader_path,
          log);
      final VertexShaderType vertex_shader =
        KProgram.newVertexShaderFromFilesystem(
          gc,
          v,
          fs,
          vertex_shader_path,
          vertex_shader_meta);

      final Map<String, FramebufferDrawBufferType> output_mappings =
        KProgram.makeOutputMappings(gc, fragment_shader_meta);
      final ProgramType p =
        KProgram.makeProgram(
          gl,
          name,
          output_mappings,
          fragment_shader,
          vertex_shader);

      return new KProgram(
        gl,
        program_meta,
        p,
        vertex_shader_meta,
        vertex_shader,
        fragment_shader_meta,
        fragment_shader,
        log);

    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } catch (final FilesystemError e) {
      throw RExceptionFilesystem.fromFilesystemException(e);
    }
  }

  private static List<String> newSourceFromFilesystem(
    final GVersionType version,
    final FSCapabilityReadType fs,
    final PathVirtual path,
    final JPCompiledShaderMetaType meta)
    throws FilesystemError,
      IOException
  {
    final Some<String> source_name_some =
      (Some<String>) meta.getSourceCodeFilename(version);

    final String source_name = source_name_some.get();
    final PathVirtual source_path = path.appendName(source_name);
    final InputStream stream = fs.openFile(source_path);
    try {
      final List<String> lines = JPSourceLines.fromStream(stream);
      if (meta.isCompacted()) {
        KProgram.prependVersionDirective(version, lines);
      }

      for (int index = 0; index < lines.size(); ++index) {
        final String line = lines.get(index);
        if (line.isEmpty()) {
          lines.set(index, "\n");
        } else if (line.charAt(line.length() - 1) != '\n') {
          lines.set(index, line + "\n");
        }
      }

      return lines;
    } finally {
      stream.close();
    }
  }

  private static VertexShaderType newVertexShaderFromFilesystem(
    final JCGLShadersCommonType gc,
    final GVersionType version,
    final FSCapabilityReadType fs,
    final PathVirtual path,
    final JPVertexShaderMetaType meta)
    throws FilesystemError,
      IOException,
      JCGLException
  {
    final List<String> source =
      KProgram.newSourceFromFilesystem(version, fs, path, meta);
    return gc.vertexShaderCompile(path.toString(), source);
  }

  private static void prependVersionDirective(
    final GVersionType version,
    final List<String> lines)
  {
    final String directive =
      String.format("#version %d\n", version.versionGetNumber());
    assert directive != null;
    lines.add(0, directive);
  }

  private static GVersionType versionNumber(
    final JCGLSLVersionNumber version,
    final JCGLApi api)
  {
    final int n =
      (version.getVersionMajor() * 100) + version.getVersionMinor();

    switch (api) {
      case JCGL_ES:
      {
        return new GVersionES(n);
      }
      case JCGL_FULL:
      {
        return new GVersionFull(n);
      }
    }

    throw new UnreachableCodeException();
  }

  private final Map<String, JCGLType>          declared_attributes;
  private final Map<String, JCGLType>          declared_uniforms;
  private final JCBExecutorType                exec;
  private final FragmentShaderType             fragment_shader;
  private final JPFragmentShaderMetaType       fragment_shader_meta;
  private final ProgramType                    program;
  private final JPUncompactedProgramShaderMeta program_meta;
  private final VertexShaderType               vertex_shader;
  private final JPVertexShaderMetaType         vertex_shader_meta;

  private KProgram(
    final JCGLImplementationType in_gl,
    final JPUncompactedProgramShaderMeta in_program_meta,
    final ProgramType in_program,
    final JPVertexShaderMetaType in_vertex_shader_meta,
    final VertexShaderType in_vertex_shader,
    final JPFragmentShaderMetaType in_fragment_shader_meta,
    final FragmentShaderType in_fragment_shader,
    final LogUsableType in_log)
    throws JCGLExceptionDeleted,
      JCGLExceptionProgramUniformMissing,
      JCGLExceptionTypeError,
      JCGLExceptionAttributeMissing
  {
    this.program_meta = NullCheck.notNull(in_program_meta, "Program meta");
    this.program = NullCheck.notNull(in_program, "Program");
    this.vertex_shader_meta =
      NullCheck.notNull(in_vertex_shader_meta, "Vertex shader meta");
    this.vertex_shader = NullCheck.notNull(in_vertex_shader, "Vertex shader");
    this.fragment_shader_meta =
      NullCheck.notNull(in_fragment_shader_meta, "Fragment shader meta");
    this.fragment_shader =
      NullCheck.notNull(in_fragment_shader, "Fragment shader");

    this.declared_uniforms = new HashMap<String, JCGLType>();
    this.declared_attributes = new HashMap<String, JCGLType>();

    for (final JPVertexParameter p : in_vertex_shader_meta
      .getDeclaredVertexParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }

    for (final JPFragmentParameter p : in_fragment_shader_meta
      .getDeclaredFragmentParameters()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_uniforms.put(p.getName(), t);
    }

    for (final JPVertexInput p : in_vertex_shader_meta
      .getDeclaredVertexInputs()) {
      final JCGLType t = JCGLType.fromName(p.getType());
      this.declared_attributes.put(p.getName(), t);
    }

    this.exec =
      JCBExecutor.newExecutorWithDeclarations(
        in_gl.getGLCommon(),
        in_program,
        this.declared_uniforms,
        this.declared_attributes,
        in_log);
  }

  @Override public boolean equals(
    @Nullable final Object obj)
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
    return this.declared_attributes.equals(other.declared_attributes)
      && this.declared_uniforms.equals(other.declared_uniforms)
      && this.exec.equals(other.exec)
      && this.fragment_shader.equals(other.fragment_shader)
      && this.fragment_shader_meta.equals(other.fragment_shader_meta)
      && this.program.equals(other.program)
      && this.program_meta.equals(other.program_meta)
      && this.vertex_shader.equals(other.vertex_shader)
      && this.vertex_shader_meta.equals(other.vertex_shader_meta);
  }

  @Override public JCBExecutorType getExecutable()
  {
    return this.exec;
  }

  @Override public JPFragmentShaderMetaType getFragmentShaderMeta()
  {
    return this.fragment_shader_meta;
  }

  @Override public JPUncompactedProgramShaderMeta getMeta()
  {
    return this.program_meta;
  }

  @Override public ProgramType getProgram()
  {
    return this.program;
  }

  @Override public JPVertexShaderMetaType getVertexShaderMeta()
  {
    return this.vertex_shader_meta;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.declared_attributes.hashCode();
    result = (prime * result) + this.declared_uniforms.hashCode();
    result = (prime * result) + this.exec.hashCode();
    result = (prime * result) + this.fragment_shader.hashCode();
    result = (prime * result) + this.fragment_shader_meta.hashCode();
    result = (prime * result) + this.program.hashCode();
    result = (prime * result) + this.program_meta.hashCode();
    result = (prime * result) + this.vertex_shader.hashCode();
    result = (prime * result) + this.vertex_shader_meta.hashCode();
    return result;
  }
}
