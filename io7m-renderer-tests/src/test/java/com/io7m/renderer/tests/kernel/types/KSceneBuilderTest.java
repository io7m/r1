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

package com.io7m.renderer.tests.kernel.types;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jnull.NonNull;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KMeshWithMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentLitType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KMeshWithMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialAlbedo;
import com.io7m.renderer.kernel.types.KMaterialAlpha;
import com.io7m.renderer.kernel.types.KMaterialAlphaOpacityType;
import com.io7m.renderer.kernel.types.KMaterialEmissive;
import com.io7m.renderer.kernel.types.KMaterialEnvironment;
import com.io7m.renderer.kernel.types.KMaterialNormal;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KTransformMatrix4x4;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.tests.FakeArrayBuffer;
import com.io7m.renderer.tests.FakeIndexBuffer;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

public final class KSceneBuilderTest extends KSceneBuilderContract
{
  @Override protected @NonNull
    KInstanceOpaqueType
    getTransformedOpaque(
      final int i)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_m =
        RMatrixI3x3F.identity();
      final KMaterialNormal normal = KMaterialNormal.newNormalUnmapped();
      final KMaterialAlbedo albedo =
        KMaterialAlbedo.newAlbedoUntextured(new RVectorI4F<RSpaceRGBAType>(
          1.0f,
          1.0f,
          1.0f,
          1.0f));
      final KMaterialEmissive emissive = KMaterialEmissive.newEmissiveNone();
      final KMaterialEnvironment env =
        KMaterialEnvironment.newEnvironmentUnmapped();
      final KMaterialSpecular spec =
        KMaterialSpecular.newSpecularUnmapped(new RVectorI3F<RSpaceRGBType>(
          0.0f,
          0.0f,
          0.0f), 0.0f);
      final KMaterialOpaqueRegular mat =
        KMaterialOpaqueRegular.newMaterial(
          uv_m,
          normal,
          albedo,
          emissive,
          env,
          spec);

      final ArrayDescriptorBuilderType tb = ArrayDescriptor.newBuilder();
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      final ArrayDescriptor t = tb.build();

      final ArrayBufferType array =
        new FakeArrayBuffer(
          i,
          new RangeInclusiveL(0, 99),
          t,
          UsageHint.USAGE_STATIC_DRAW);

      final IndexBufferType indices =
        new FakeIndexBuffer(
          i,
          new RangeInclusiveL(0, 99),
          UsageHint.USAGE_STATIC_DRAW,
          JCGLUnsignedType.TYPE_UNSIGNED_INT);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_lower, bounds_upper);

      final KMeshWithMaterialOpaqueRegular io =
        KMeshWithMaterialOpaqueRegular.newInstance(
          mat,
          mesh,
          KFaceSelection.FACE_RENDER_FRONT);

      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      return KInstanceOpaqueRegular.newInstance(
        io,
        transform,
        uv_m);
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override protected @NonNull KSceneBuilderWithCreateType newBuilder()
  {
    final RMatrixI4x4F<RTransformProjectionType> projection =
      RMatrixI4x4F.identity();
    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();
    final KCamera camera = KCamera.newCamera(view, projection);
    return KScene.newBuilder(camera);
  }

  @Override protected
    KInstanceTranslucentLitType
    getTransformedTranslucent(
      final int i)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_m =
        RMatrixI3x3F.identity();
      final KMaterialNormal normal = KMaterialNormal.newNormalUnmapped();
      final KMaterialAlbedo albedo =
        KMaterialAlbedo.newAlbedoUntextured(new RVectorI4F<RSpaceRGBAType>(
          1.0f,
          1.0f,
          1.0f,
          1.0f));
      final KMaterialEmissive emissive = KMaterialEmissive.newEmissiveNone();
      final KMaterialEnvironment env =
        KMaterialEnvironment.newEnvironmentUnmapped();
      final KMaterialSpecular spec =
        KMaterialSpecular.newSpecularUnmapped(new RVectorI3F<RSpaceRGBType>(
          0.0f,
          0.0f,
          0.0f), 0.0f);
      final KMaterialAlpha alpha =
        KMaterialAlpha.newAlpha(
          KMaterialAlphaOpacityType.ALPHA_OPACITY_CONSTANT,
          1.0f);
      final KMaterialTranslucentRegular mat =
        KMaterialTranslucentRegular.newMaterial(
          uv_m,
          albedo,
          alpha,
          emissive,
          env,
          normal,
          spec);

      final ArrayDescriptorBuilderType tb = ArrayDescriptor.newBuilder();
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      final ArrayDescriptor t = tb.build();

      final ArrayBufferType array =
        new FakeArrayBuffer(
          i,
          new RangeInclusiveL(0, 99),
          t,
          UsageHint.USAGE_STATIC_DRAW);

      final IndexBufferType indices =
        new FakeIndexBuffer(
          i,
          new RangeInclusiveL(0, 99),
          UsageHint.USAGE_STATIC_DRAW,
          JCGLUnsignedType.TYPE_UNSIGNED_INT);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_lower, bounds_upper);

      final KMeshWithMaterialTranslucentRegular io =
        KMeshWithMaterialTranslucentRegular.newInstance(
          mat,
          mesh,
          KFaceSelection.FACE_RENDER_FRONT);

      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      return KInstanceTranslucentRegular.newInstance(
        io,
        transform,
        uv_m);
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    }
  }
}
