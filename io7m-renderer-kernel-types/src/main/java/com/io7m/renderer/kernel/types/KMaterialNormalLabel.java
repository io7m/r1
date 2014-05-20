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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jfunctional.OptionType;
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

  private static KMaterialNormalLabel fromData(
    final KMeshReadableType mesh,
    final KMaterialNormal normal)
  {
    if (mesh.meshHasNormals()) {
      if (mesh.meshHasUVs() && mesh.meshHasTangents()) {
        if (normal.getTexture().isSome()) {
          return KMaterialNormalLabel.NORMAL_MAPPED;
        }
      }
      return KMaterialNormalLabel.NORMAL_VERTEX;
    }

    return KMaterialNormalLabel.NORMAL_NONE;
  }

  /**
   * Derive a normal label for the given mesh and material.
   * 
   * @param mwm
   *          The mesh and material
   * @return A normal label
   */

  @SuppressWarnings("synthetic-access") public static
    KMaterialNormalLabel
    fromMeshAndMaterial(
      final KMeshWithMaterialType mwm)
  {
    NullCheck.notNull(mwm, "Mesh");

    final KMeshReadableType mesh = mwm.meshGetMesh();

    try {
      return mwm
        .meshWithMaterialAccept(new KMeshWithMaterialVisitorType<KMaterialNormalLabel, UnreachableCodeException>() {
          @Override public
            KMaterialNormalLabel
            meshWithMaterialOpaqueAlphaDepth(
              final KMeshWithMaterialOpaqueAlphaDepth i)
              throws RException
          {
            final KMaterialOpaqueType material = i.meshGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromData(mesh, normal);
          }

          @Override public
            KMaterialNormalLabel
            meshWithMaterialOpaqueRegular(
              final KMeshWithMaterialOpaqueRegular i)
              throws RException
          {
            final KMaterialOpaqueType material = i.meshGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromData(mesh, normal);
          }

          @Override public
            KMaterialNormalLabel
            meshWithMaterialTranslucentRefractive(
              final KMeshWithMaterialTranslucentRefractive i)
              throws RException
          {
            final KMaterialTranslucentType material = i.getMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromData(mesh, normal);
          }

          @Override public
            KMaterialNormalLabel
            meshWithMaterialTranslucentRegular(
              final KMeshWithMaterialTranslucentRegular i)
              throws RException
          {
            final KMaterialTranslucentType material = i.meshGetMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromData(mesh, normal);
          }

          @Override public
            KMaterialNormalLabel
            meshWithMaterialTranslucentSpecularOnly(
              final KMeshWithMaterialTranslucentSpecularOnly i)
              throws RException
          {
            final KMaterialTranslucentType material = i.getMaterial();
            final KMaterialNormal normal = material.materialGetNormal();
            return KMaterialNormalLabel.fromData(mesh, normal);
          }
        });
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Check whether two materials would derive the same label for an arbitrary
   * mesh <code>m</code>.
   * 
   * @param ma
   *          The first material
   * @param mb
   *          The second material
   * @return <code>true</code> if applying <code>ma</code> to a mesh
   *         <code>m</code> would yield the same label as applying
   *         <code>mb</code> to <code>m</code>.
   */

  public static boolean wouldDeriveSameLabel(
    final KMaterialNormal ma,
    final KMaterialNormal mb)
  {
    NullCheck.notNull(ma, "Material A");
    NullCheck.notNull(mb, "Material B");

    if (ma == mb) {
      return true;
    }
    final OptionType<Texture2DStaticUsableType> mat = ma.getTexture();
    final OptionType<Texture2DStaticUsableType> mbt = mb.getTexture();
    if (mat.isNone() && mbt.isNone()) {
      return true;
    }
    if (mat.isSome() && mbt.isSome()) {
      return true;
    }
    return false;
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
