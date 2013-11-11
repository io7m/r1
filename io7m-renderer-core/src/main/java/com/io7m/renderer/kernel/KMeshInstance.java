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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RMatrixI3x3F;
import com.io7m.renderer.RTransformTexture;

/**
 * <p>
 * An instance of a polygon mesh on the GPU.
 * </p>
 * <p>
 * The polygon mesh is expected to have the following type(s):
 * </p>
 * <ul>
 * <li>The array buffer must have an attribute of type
 * {@link KMeshAttributes#ATTRIBUTE_POSITION}.</li>
 * <li>If the mesh has per-vertex normals, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_NORMAL}.</li>
 * <li>If the mesh has texture coordinates, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_UV}.</li>
 * <li>If the mesh has per-vertex tangents, they must be of type
 * {@link KMeshAttributes#ATTRIBUTE_TANGENT4}.</li>
 * <ul>
 */

@Immutable public final class KMeshInstance implements KTransformable
{
  private final @Nonnull Integer                           id;
  private final @Nonnull KTransform                        transform;
  private final @Nonnull KMesh                             mesh;
  private final @Nonnull KMaterial                         material;
  private final @Nonnull RMatrixI3x3F<RTransformTexture>   uv_matrix;
  private final @Nonnull KMeshInstanceForwardMaterialLabel forward_label;
  private final @Nonnull KMeshInstanceShadowMaterialLabel  shadow_label;

  public KMeshInstance(
    final @Nonnull Integer id,
    final @Nonnull KTransform transform,
    final @Nonnull RMatrixI3x3F<RTransformTexture> uv_matrix,
    final @Nonnull KMesh mesh,
    final @Nonnull KMaterial material)
    throws ConstraintError
  {
    this.id = id;
    this.transform = transform;
    this.mesh = mesh;
    this.material = material;
    this.uv_matrix = uv_matrix;
    this.forward_label =
      KMeshInstanceForwardMaterialLabel.label(mesh, material);
    this.shadow_label =
      KMeshInstanceShadowMaterialLabel.label(mesh, material);
  }

  public @Nonnull KMeshInstanceForwardMaterialLabel getForwardMaterialLabel()
  {
    return this.forward_label;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public @Nonnull KMaterial getMaterial()
  {
    return this.material;
  }

  public @Nonnull KMesh getMesh()
  {
    return this.mesh;
  }

  @Override public @Nonnull KTransform getTransform()
  {
    return this.transform;
  }

  public @Nonnull RMatrixI3x3F<RTransformTexture> getUVMatrix()
  {
    return this.uv_matrix;
  }
}
