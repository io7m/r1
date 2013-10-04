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

package com.io7m.renderer.kernel.programs;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.JCGLCompileException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLSLVersion;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.ProgramReference;
import com.io7m.jcanephora.checkedexec.JCCEExecutionCallable;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixM3x3F;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformModelView;
import com.io7m.renderer.RTransformNormal;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.KMesh;
import com.io7m.renderer.kernel.KMeshInstance;
import com.io7m.renderer.kernel.KRenderingCapabilities;
import com.io7m.renderer.kernel.KRenderingCapabilities.NormalCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.SpecularCapability;
import com.io7m.renderer.kernel.KRenderingCapabilities.TextureCapability;
import com.io7m.renderer.kernel.KShaderUtilities;
import com.io7m.renderer.kernel.KShadingProgram;
import com.io7m.renderer.kernel.KShadingProgramCommon;

public final class KSPF_U_T implements KShadingProgram
{
  public static @Nonnull KSPF_U_T make(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    return new KSPF_U_T(gc, fs, log);
  }

  private final @Nonnull ProgramReference       program;
  private final @Nonnull JCCEExecutionCallable  exec;
  private final @Nonnull Log                    log;
  private final @Nonnull KRenderingCapabilities required;

  private KSPF_U_T(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull FSCapabilityRead fs,
    final @Nonnull Log log)
    throws JCGLException,
      JCGLCompileException,
      JCGLUnsupportedException,
      FilesystemError,
      IOException,
      ConstraintError
  {
    this.log =
      new Log(
        log,
        KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT_TEXTURED.getName());

    final JCGLSLVersion version = gc.metaGetSLVersion();
    this.program =
      KShaderUtilities.makeProgram(
        gc,
        version.getNumber(),
        version.getAPI(),
        fs,
        KSPForwardUnlitPrograms.KSP_FORWARD_UNLIT_TEXTURED.getName(),
        this.log);
    this.exec = new JCCEExecutionCallable(this.program);

    this.required =
      new KRenderingCapabilities(
        TextureCapability.TEXTURE_CAP_DIFFUSE,
        NormalCapability.NORMAL_CAP_NONE,
        SpecularCapability.SPECULAR_CAP_NONE);
  }

  @Override public void ksPreparePass(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    this.exec.execPrepare(gc);
    KShadingProgramCommon.projectionMatrixPut(this.exec, gc, m);
    this.exec.execCancel();
  }

  @Override public void ksRenderWithMeshInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformModelView> mv,
    final @Nonnull RMatrixM3x3F<RTransformNormal> mn,
    final @Nonnull KMeshInstance m)
    throws ConstraintError,
      Exception
  {
    this.exec.execPrepare(gc);
    KShadingProgramCommon.projectionMatrixReuse(this.exec);
    KShadingProgramCommon.modelViewMatrixPut(this.exec, gc, mv);

    final KMesh mesh = m.getMesh();
    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    gc.arrayBufferBind(array);
    try {
      KShadingProgramCommon.attributePositionBind(gc, this.exec, array);
      KShadingProgramCommon.materialTexturesAndAttributesBind(
        gc,
        this.exec,
        this.required,
        m.getMaterial(),
        array);
      KShadingProgramCommon.renderWithIndices(gc, this.exec, indices);
    } finally {
      gc.arrayBufferUnbind();
    }
  }
}
