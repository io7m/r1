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
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jnull.NonNull;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KCamera;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoType;
import com.io7m.renderer.kernel.types.KMaterialAlbedoUntextured;
import com.io7m.renderer.kernel.types.KMaterialAlphaConstant;
import com.io7m.renderer.kernel.types.KMaterialAlphaType;
import com.io7m.renderer.kernel.types.KMaterialDepthConstant;
import com.io7m.renderer.kernel.types.KMaterialDepthType;
import com.io7m.renderer.kernel.types.KMaterialEmissiveNone;
import com.io7m.renderer.kernel.types.KMaterialEmissiveType;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentNone;
import com.io7m.renderer.kernel.types.KMaterialEnvironmentType;
import com.io7m.renderer.kernel.types.KMaterialNormalType;
import com.io7m.renderer.kernel.types.KMaterialNormalVertex;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialSpecularNone;
import com.io7m.renderer.kernel.types.KMaterialSpecularType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.kernel.types.KMeshReadableType;
import com.io7m.renderer.kernel.types.KProjectionFrustum;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KTransformMatrix4x4;
import com.io7m.renderer.kernel.types.KTransformType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMaterialMissingAlbedoTexture;
import com.io7m.renderer.types.RExceptionMaterialMissingSpecularTexture;
import com.io7m.renderer.types.RExceptionMeshMissingNormals;
import com.io7m.renderer.types.RExceptionMeshMissingPositions;
import com.io7m.renderer.types.RExceptionMeshMissingTangents;
import com.io7m.renderer.types.RExceptionMeshMissingUVs;
import com.io7m.renderer.types.RMatrixI3x3F;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceRGBAType;
import com.io7m.renderer.types.RTransformModelType;
import com.io7m.renderer.types.RTransformTextureType;
import com.io7m.renderer.types.RTransformViewType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

public final class KSceneBuilderTest extends KSceneBuilderContract
{
  @Override protected @NonNull KInstanceOpaqueType getOpaque(
    final JCGLImplementationType g)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_m =
        RMatrixI3x3F.identity();
      final KMaterialNormalType normal = KMaterialNormalVertex.vertex();
      final KMaterialAlbedoType albedo =
        KMaterialAlbedoUntextured.untextured(new RVectorI4F<RSpaceRGBAType>(
          1.0f,
          1.0f,
          1.0f,
          1.0f));
      final KMaterialEmissiveType emissive = KMaterialEmissiveNone.none();
      final KMaterialEnvironmentType env = KMaterialEnvironmentNone.none();
      final KMaterialSpecularType spec = KMaterialSpecularNone.none();
      final KMaterialDepthType depth = KMaterialDepthConstant.constant();
      final KMaterialOpaqueRegular mat =
        KMaterialOpaqueRegular.newMaterial(
          uv_m,
          albedo,
          depth,
          emissive,
          env,
          normal,
          spec);

      final ArrayDescriptorBuilderType tb = ArrayDescriptor.newBuilder();
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      final ArrayDescriptor t = tb.build();

      final JCGLInterfaceCommonType gc = g.getGLCommon();
      final ArrayBufferType array =
        gc.arrayBufferAllocate(100, t, UsageHint.USAGE_STATIC_DRAW);
      final IndexBufferType indices =
        gc.indexBufferAllocateType(
          JCGLUnsignedType.TYPE_UNSIGNED_INT,
          100,
          UsageHint.USAGE_STATIC_DRAW);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_lower, bounds_upper);

      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);

      return KInstanceOpaqueRegular.newInstance(
        mesh,
        mat,
        transform,
        uv_m,
        KFaceSelection.FACE_RENDER_FRONT);
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingUVs e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingNormals e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingTangents e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingPositions e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingAlbedoTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingSpecularTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override protected @NonNull KSceneBuilderWithCreateType newBuilder()
  {
    final KProjectionType projection =
      KProjectionFrustum.newProjection(
        new MatrixM4x4F(),
        -1.0f,
        1.0f,
        -1.0f,
        1.0f,
        1.0f,
        100.0f);
    final RMatrixI4x4F<RTransformViewType> view = RMatrixI4x4F.identity();
    final KCamera camera = KCamera.newCamera(view, projection);
    return KScene.newBuilder(camera);
  }

  @Override protected KInstanceTranslucentRegular getTranslucent(
    final JCGLImplementationType g)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_m =
        RMatrixI3x3F.identity();
      final KMaterialNormalType normal = KMaterialNormalVertex.vertex();
      final KMaterialAlbedoType albedo =
        KMaterialAlbedoUntextured.untextured(new RVectorI4F<RSpaceRGBAType>(
          1.0f,
          1.0f,
          1.0f,
          1.0f));
      final KMaterialEmissiveType emissive = KMaterialEmissiveNone.none();
      final KMaterialEnvironmentType env = KMaterialEnvironmentNone.none();
      final KMaterialSpecularType spec = KMaterialSpecularNone.none();
      final KMaterialAlphaType alpha = KMaterialAlphaConstant.constant(1.0f);
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
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      final ArrayDescriptor t = tb.build();

      final JCGLInterfaceCommonType gc = g.getGLCommon();
      final ArrayBufferType array =
        gc.arrayBufferAllocate(100, t, UsageHint.USAGE_STATIC_DRAW);
      final IndexBufferType indices =
        gc.indexBufferAllocateType(
          JCGLUnsignedType.TYPE_UNSIGNED_INT,
          100,
          UsageHint.USAGE_STATIC_DRAW);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_lower, bounds_upper);

      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      return KInstanceTranslucentRegular.newInstance(
        mesh,
        mat,
        transform,
        uv_m,
        KFaceSelection.FACE_RENDER_FRONT);
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingUVs e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingNormals e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingTangents e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingPositions e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingSpecularTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override protected KInstanceTranslucentUnlitType getTranslucentUnlit(
    final JCGLImplementationType g)
  {
    try {
      final RMatrixI3x3F<RTransformTextureType> uv_m =
        RMatrixI3x3F.identity();
      final KMaterialNormalType normal = KMaterialNormalVertex.vertex();
      final KMaterialAlbedoType albedo =
        KMaterialAlbedoUntextured.untextured(new RVectorI4F<RSpaceRGBAType>(
          1.0f,
          1.0f,
          1.0f,
          1.0f));
      final KMaterialEmissiveType emissive = KMaterialEmissiveNone.none();
      final KMaterialEnvironmentType env = KMaterialEnvironmentNone.none();
      final KMaterialSpecularType spec = KMaterialSpecularNone.none();
      final KMaterialAlphaType alpha = KMaterialAlphaConstant.constant(1.0f);
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
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      tb.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      final ArrayDescriptor t = tb.build();

      final JCGLInterfaceCommonType gc = g.getGLCommon();
      final ArrayBufferType array =
        gc.arrayBufferAllocate(100, t, UsageHint.USAGE_STATIC_DRAW);
      final IndexBufferType indices =
        gc.indexBufferAllocateType(
          JCGLUnsignedType.TYPE_UNSIGNED_INT,
          100,
          UsageHint.USAGE_STATIC_DRAW);

      final RVectorI3F<RSpaceObjectType> bounds_lower =
        new RVectorI3F<RSpaceObjectType>(0.0f, 0.0f, 0.0f);
      final RVectorI3F<RSpaceObjectType> bounds_upper =
        new RVectorI3F<RSpaceObjectType>(1.0f, 1.0f, 1.0f);
      final KMeshReadableType mesh =
        KMesh.newMesh(array, indices, bounds_lower, bounds_upper);

      final RMatrixI4x4F<RTransformModelType> model = RMatrixI4x4F.identity();
      final KTransformType transform =
        KTransformMatrix4x4.newTransform(model);
      return KInstanceTranslucentRegular.newInstance(
        mesh,
        mat,
        transform,
        uv_m,
        KFaceSelection.FACE_RENDER_FRONT);
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingUVs e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingNormals e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingTangents e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMeshMissingPositions e) {
      throw new UnreachableCodeException(e);
    } catch (final RExceptionMaterialMissingSpecularTexture e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }
}
