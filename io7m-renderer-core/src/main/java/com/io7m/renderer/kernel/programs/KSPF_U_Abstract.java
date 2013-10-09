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
import com.io7m.jtensors.VectorM4F;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.renderer.RMatrixReadable4x4F;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.kernel.KMatrices;
import com.io7m.renderer.kernel.KMesh;
import com.io7m.renderer.kernel.KMeshInstance;
import com.io7m.renderer.kernel.KRenderingCapabilities;
import com.io7m.renderer.kernel.KShaderUtilities;
import com.io7m.renderer.kernel.KShadingProgram;
import com.io7m.renderer.kernel.KShadingProgramCommon;

abstract class KSPF_U_Abstract implements KShadingProgram
{
  private final @Nonnull Log                     log;
  private final @Nonnull ProgramReference        program;
  private final @Nonnull JCCEExecutionCallable   exec;
  private final @Nonnull VectorM4F               diffuse;
  private final @Nonnull KRenderingCapabilities  required;
  private final @Nonnull KSPForwardUnlitPrograms type;

  protected KSPF_U_Abstract(
    final @Nonnull KSPForwardUnlitPrograms type,
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
    this.log = new Log(log, type.name());

    final JCGLSLVersion version = gc.metaGetSLVersion();
    this.program =
      KShaderUtilities.makeProgram(
        gc,
        version.getNumber(),
        version.getAPI(),
        fs,
        type.getName(),
        this.log);

    this.exec = new JCCEExecutionCallable(this.program);
    this.diffuse = new VectorM4F();
    this.required = type.getRequiredCapabilities();
    this.type = type;
  }

  @Override public void ksPreparePass(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull RMatrixReadable4x4F<RTransformProjection> m)
    throws JCGLException,
      ConstraintError
  {
    this.exec.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjection(this.exec, gc, m);
    this.exec.execCancel();
  }

  @Override public void ksRenderWithMeshInstance(
    final @Nonnull JCGLInterfaceCommon gc,
    final @Nonnull KMatrices matrices,
    final @Nonnull KMeshInstance m)
    throws JCGLException,
      ConstraintError,
      Exception
  {
    this.exec.execPrepare(gc);
    KShadingProgramCommon.putMatrixProjectionReuse(this.exec);
    KShadingProgramCommon.putMatrixModelView(
      this.exec,
      gc,
      matrices.getMatrixModelView());
    KShadingProgramCommon.putMaterial(
      this.exec,
      gc,
      this.required,
      m.getMaterial());

    switch (this.required.getEnvironment()) {
      case ENVIRONMENT_MAPPED:
      {
        KShadingProgramCommon.putMatrixNormal(
          this.exec,
          gc,
          matrices.getMatrixNormal());
        KShadingProgramCommon.putMatrixInverseView(
          this.exec,
          gc,
          matrices.getMatrixViewInverse());
        break;
      }
      case ENVIRONMENT_NONE:
      {
        break;
      }
    }

    final KMesh mesh = m.getMesh();
    final ArrayBuffer array = mesh.getArrayBuffer();
    final IndexBuffer indices = mesh.getIndexBuffer();

    gc.arrayBufferBind(array);
    try {
      KShadingProgramCommon.bindAttributePosition(gc, this.exec, array);
      KShadingProgramCommon.bindTexturesAttributesMaterial(
        gc,
        this.exec,
        this.required,
        m.getMaterial(),
        array);

      try {
        KShadingProgramCommon.renderWithIndices(gc, this.exec, indices);
      } catch (final ConstraintError x) {
        this.log.error("Program " + this.type.getName() + " raised error");
        throw x;
      }
    } finally {
      gc.arrayBufferUnbind();
    }
  }
}
