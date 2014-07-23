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

import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Primitives;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.batchexec.JCBProgramProcedureType;
import com.io7m.jcanephora.batchexec.JCBProgramType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesInstanceType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueVisitorType;
import com.io7m.renderer.kernel.types.KMaterialNormalMapped;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialNormalVisitorType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.types.RException;

/**
 * Functions common to forward renderers.
 */

@EqualityReference final class KRendererForwardOpaqueCommon
{
  /**
   * Render a specific opaque instance, assuming all program state for the
   * current light (if any) has been configured.
   */

  static void renderOpaqueInstance(
    final JCGLInterfaceCommonType gc,
    final KTextureUnitContextType units,
    final MatricesInstanceType mwi,
    final JCBProgramType program,
    final KInstanceOpaqueType instance)
    throws JCGLException,
      RException
  {
    units.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType context)
        throws JCGLException,
          RException
      {
        final KMeshReadableType mesh = instance.instanceGetMesh();
        final ArrayBufferUsableType array = mesh.meshGetArrayBuffer();
        final IndexBufferUsableType indices = mesh.meshGetIndexBuffer();
        final KMaterialOpaqueRegular material =
          instance
            .opaqueAccept(new KInstanceOpaqueVisitorType<KMaterialOpaqueRegular, UnreachableCodeException>() {
              @Override public KMaterialOpaqueRegular regular(
                final KInstanceOpaqueRegular o)
              {
                return o.getMaterial();
              }
            });

        KRendererCommon.renderConfigureFaceCulling(
          gc,
          instance.instanceGetFaceSelection());

        KShadingProgramCommon.putMatrixProjectionReuse(program);
        KRendererCommon.putInstanceMatricesRegular(program, mwi, material);
        KRendererCommon
          .putInstanceTexturesRegular(context, program, material);
        KRendererCommon.putMaterialRegular(program, material);

        try {
          gc.arrayBufferBind(array);
          KShadingProgramCommon
            .bindAttributePositionUnchecked(program, array);

          material.materialGetNormal().normalAccept(
            new KMaterialNormalVisitorType<Unit, JCGLException>() {
              @Override public Unit mapped(
                final KMaterialNormalMapped m)
                throws RException,
                  JCGLException
              {
                KShadingProgramCommon.bindAttributeTangent4(program, array);
                KShadingProgramCommon.bindAttributeNormal(program, array);
                return Unit.unit();
              }

              @Override public Unit vertex(
                final KMaterialNormalVertex m)
                throws RException,
                  JCGLException
              {
                KShadingProgramCommon.bindAttributeNormal(program, array);
                return Unit.unit();
              }
            });

          if (material.materialRequiresUVCoordinates()) {
            KShadingProgramCommon.bindAttributeUVUnchecked(program, array);
          }

          KRendererCommon.renderConfigureFaceCulling(
            gc,
            instance.instanceGetFaceSelection());

          program
            .programExecute(new JCBProgramProcedureType<JCGLException>() {
              @Override public void call()
                throws JCGLException
              {
                gc.drawElements(Primitives.PRIMITIVE_TRIANGLES, indices);
              }
            });

        } finally {
          gc.arrayBufferUnbind();
        }
      }
    });
  }

  private KRendererForwardOpaqueCommon()
  {
    throw new UnreachableCodeException();
  }
}
