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

import java.util.Map;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Labels for normal-mapping properties.
 */

public enum KMaterialNormalLabel
  implements
  KTexturesRequiredType,
  KLabelType
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
    final ArrayBufferUsableType a,
    final KMaterialNormal normal)
  {
    final ArrayDescriptor d = a.arrayGetDescriptor();
    final Map<String, ArrayAttributeDescriptor> da = d.getAttributes();

    if (da.containsKey(KMeshAttributes.ATTRIBUTE_UV.getName())) {
      if (da.containsKey(KMeshAttributes.ATTRIBUTE_TANGENT4.getName())) {
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
   */

  @SuppressWarnings("synthetic-access") public static
    KMaterialNormalLabel
    fromInstance(
      final KInstanceType instance)
  {
    NullCheck.notNull(instance, "Instance");

    final KMeshReadableType mesh = instance.instanceGetMesh();
    final ArrayBufferUsableType a = mesh.getArrayBuffer();

    try {
      return instance
        .instanceAccept(new KInstanceVisitorType<KMaterialNormalLabel, UnreachableCodeException>() {
          @Override public KMaterialNormalLabel instanceOpaqueAlphaDepth(
            final KInstanceOpaqueAlphaDepth i)
          {
            final KMaterialOpaqueType material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public KMaterialNormalLabel instanceOpaqueRegular(
            final KInstanceOpaqueRegular i)
          {
            final KMaterialOpaqueType material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public
            KMaterialNormalLabel
            instanceTranslucentRefractive(
              final KInstanceTranslucentRefractive i)
          {
            final KMaterialTranslucentType material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public KMaterialNormalLabel instanceTranslucentRegular(
            final KInstanceTranslucentRegular i)
          {
            final KMaterialTranslucentType material = i.instanceGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromInstanceData(a, normal);
          }

          @Override public
            KMaterialNormalLabel
            instanceTranslucentSpecularOnly(
              final KInstanceTranslucentSpecularOnly i)
              throws RException,
                JCGLException
          {
            final KMaterialTranslucentType material = i.instanceGetMaterial();
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

  private static KMaterialNormalLabel fromInstanceData(
    final ArrayBufferUsableType a,
    final KMaterialNormal normal)
  {
    final ArrayDescriptor d = a.arrayGetDescriptor();
    final Map<String, ArrayAttributeDescriptor> da = d.getAttributes();

    if (da.containsKey(KMeshAttributes.ATTRIBUTE_NORMAL.getName())) {
      if (KMaterialNormalLabel.canNormalMap(a, normal)) {
        return KMaterialNormalLabel.NORMAL_MAPPED;
      }
      return KMaterialNormalLabel.NORMAL_VERTEX;
    }
    return KMaterialNormalLabel.NORMAL_NONE;
  }

  private final String code;
  private int          textures_required;

  private KMaterialNormalLabel(
    final String in_code,
    final int in_textures_required)
  {
    this.code = in_code;
    this.textures_required = in_textures_required;
  }

  @Override public String labelGetCode()
  {
    return this.code;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }
}
