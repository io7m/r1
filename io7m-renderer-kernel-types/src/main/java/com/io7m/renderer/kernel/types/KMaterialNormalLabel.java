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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.types.RException;

/**
 * Labels for normal-mapping properties.
 */

public enum KMaterialNormalLabel
  implements
  KTexturesRequired,
  KLabel
{
  /**
   * Per-fragment normal mapping.
   */

  NORMAL_MAPPED("NM", 1),

  /**
   * No normals.
   */

  NORMAL_NONE("", 0),

  /**
   * Per-vertex normals.
   */

  NORMAL_VERTEX("NV", 0);

  private static boolean canNormalMap(
    final @Nonnull ArrayBufferUsable a,
    final @Nonnull KMaterialNormal normal)
    throws ConstraintError
  {
    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_UV.getName())) {
      if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4.getName())) {
        if (normal.getTexture().isSome()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Derive a normal label for the given instance.
   * 
   * @param instance
   *          The instance
   * @return A normal label
   * @throws ConstraintError
   *           If any parameter is <code>null</code>.
   */

  @SuppressWarnings("synthetic-access") public static @Nonnull
    KMaterialNormalLabel
    fromInstance(
      final @Nonnull KInstance instance)
      throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");

    final KMesh mesh = instance.instanceGetMesh();
    final ArrayBufferUsable a = mesh.getArrayBuffer();

    try {
      return instance
        .instanceVisitableAccept(new KInstanceVisitor<KMaterialNormalLabel, ConstraintError>() {
          @Override public KMaterialNormalLabel instanceVisitOpaqueRegular(
            final @Nonnull KInstanceOpaqueRegular i)
            throws ConstraintError
          {
            final KMaterialOpaque material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public
            KMaterialNormalLabel
            instanceVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceOpaqueAlphaDepth i)
              throws ConstraintError
          {
            final KMaterialOpaque material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public
            KMaterialNormalLabel
            instanceVisitTranslucentRefractive(
              final @Nonnull KInstanceTranslucentRefractive i)
              throws ConstraintError
          {
            final KMaterialTranslucent material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public
            KMaterialNormalLabel
            instanceVisitTranslucentRegular(
              final @Nonnull KInstanceTranslucentRegular i)
              throws ConstraintError
          {
            final KMaterialTranslucent material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }
        });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static @Nonnull KMaterialNormalLabel fromInstanceData(
    final @Nonnull ArrayBufferUsable a,
    final @Nonnull KMaterialNormal normal)
    throws ConstraintError
  {
    if (a.hasAttribute(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
      if (KMaterialNormalLabel.canNormalMap(a, normal)) {
        return KMaterialNormalLabel.NORMAL_MAPPED;
      }
      return KMaterialNormalLabel.NORMAL_VERTEX;
    }
    return KMaterialNormalLabel.NORMAL_NONE;
  }

  private final @Nonnull String code;
  private int                   textures_required;

  private KMaterialNormalLabel(
    final @Nonnull String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public @Nonnull String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
