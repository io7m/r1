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
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorReadable3FType;

/**
 * <p>
 * A polygon mesh on the GPU.
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
 * {@link KMeshAttributes#ATTRIBUTE_TANGENT3}.</li>
 * <ul>
 */

@Immutable public final class KMesh
{
  /**
   * Construct a new mesh.
   * 
   * @param in_array
   *          The array buffer of vertex data
   * @param in_indices
   *          The index buffer
   * @param in_bounds_lower
   *          The object-space lower bound
   * @param in_bounds_upper
   *          The object-space upper bound
   * @return A new mesh
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMesh newMesh(
    final @Nonnull ArrayBuffer in_array,
    final @Nonnull IndexBuffer in_indices,
    final @Nonnull RVectorI3F<RSpaceObjectType> in_bounds_lower,
    final @Nonnull RVectorI3F<RSpaceObjectType> in_bounds_upper)
    throws ConstraintError
  {
    return new KMesh(in_array, in_indices, in_bounds_lower, in_bounds_upper);
  }

  private final @Nonnull ArrayBuffer              array;
  private final @Nonnull RVectorI3F<RSpaceObjectType> bounds_lower;
  private final @Nonnull RVectorI3F<RSpaceObjectType> bounds_upper;
  private final @Nonnull IndexBuffer              indices;

  private KMesh(
    final @Nonnull ArrayBuffer in_array,
    final @Nonnull IndexBuffer in_indices,
    final @Nonnull RVectorI3F<RSpaceObjectType> in_bounds_lower,
    final @Nonnull RVectorI3F<RSpaceObjectType> in_bounds_upper)
    throws ConstraintError
  {
    this.array = Constraints.constrainNotNull(in_array, "Array");
    this.indices = Constraints.constrainNotNull(in_indices, "Indices");
    this.bounds_lower =
      Constraints.constrainNotNull(in_bounds_lower, "Lower bounds");
    this.bounds_upper =
      Constraints.constrainNotNull(in_bounds_upper, "Upper bounds");
  }

  /**
   * @return The array buffer that holds the mesh data
   */

  public @Nonnull ArrayBuffer getArrayBuffer()
  {
    return this.array;
  }

  /**
   * @return The lower bound, in object space, of all vertex positions in the
   *         mesh
   */

  public @Nonnull RVectorReadable3FType<RSpaceObjectType> getBoundsLower()
  {
    return this.bounds_lower;
  }

  /**
   * @return The upper bound, in object space, of all vertex positions in the
   *         mesh
   */

  public @Nonnull RVectorReadable3FType<RSpaceObjectType> getBoundsUpper()
  {
    return this.bounds_upper;
  }

  /**
   * @return The index buffer describing primitives in the mesh data
   */

  public @Nonnull IndexBuffer getIndexBuffer()
  {
    return this.indices;
  }
}
