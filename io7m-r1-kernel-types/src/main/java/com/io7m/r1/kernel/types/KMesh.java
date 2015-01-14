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

package com.io7m.r1.kernel.types;

import java.util.Map;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.JCGLResourceSizedType;
import com.io7m.jcanephora.JCGLResourceUsableType;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RExceptionMeshMissingNormals;
import com.io7m.r1.exceptions.RExceptionMeshMissingPositions;
import com.io7m.r1.exceptions.RExceptionMeshMissingTangents;
import com.io7m.r1.exceptions.RExceptionMeshMissingUVs;

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
 * <li>The array buffer must have per-vertex normals of type
 * {@link KMeshAttributes#ATTRIBUTE_NORMAL}.</li>
 * <li>The array buffer must have texture coordinates of type
 * {@link KMeshAttributes#ATTRIBUTE_UV}.</li>
 * <li>The array buffer must have per-vertex tangents of type
 * {@link KMeshAttributes#ATTRIBUTE_TANGENT4}.</li>
 * </ul>
 */

@EqualityStructural public final class KMesh implements
  KMeshReadableType,
  JCGLResourceUsableType,
  JCGLResourceSizedType
{
  /**
   * @return The standard array descriptor type for meshes.
   */

  public static ArrayDescriptor getStandardDescriptor()
  {
    try {
      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
      b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
      final ArrayDescriptor type = b.build();
      return type;
    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static boolean hasNormals(
    final Map<String, ArrayAttributeDescriptor> as)
  {
    final ArrayAttributeDescriptor ta =
      as.get(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
    if (ta != null) {
      return ta.equals(KMeshAttributes.ATTRIBUTE_NORMAL);
    }
    return false;
  }

  private static boolean hasPositions(
    final Map<String, ArrayAttributeDescriptor> as)
  {
    final ArrayAttributeDescriptor ta =
      as.get(KMeshAttributes.ATTRIBUTE_POSITION.getName());
    if (ta != null) {
      return ta.equals(KMeshAttributes.ATTRIBUTE_POSITION);
    }
    return false;
  }

  private static boolean hasTangents(
    final Map<String, ArrayAttributeDescriptor> as)
  {
    final ArrayAttributeDescriptor ta =
      as.get(KMeshAttributes.ATTRIBUTE_TANGENT4.getName());
    if (ta != null) {
      return ta.equals(KMeshAttributes.ATTRIBUTE_TANGENT4);
    }
    return false;
  }

  private static boolean hasUVs(
    final Map<String, ArrayAttributeDescriptor> as)
  {
    final ArrayAttributeDescriptor ta =
      as.get(KMeshAttributes.ATTRIBUTE_UV.getName());
    if (ta != null) {
      return ta.equals(KMeshAttributes.ATTRIBUTE_UV);
    }
    return false;
  }

  /**
   * Construct a new mesh.
   *
   * @param in_array
   *          The array buffer of vertex data
   * @param in_indices
   *          The index buffer
   *
   * @return A new mesh
   * @throws RExceptionMeshMissingTangents
   *           If the array does not have tangent vectors.
   * @throws RExceptionMeshMissingNormals
   *           If the array does not have normal vectors.
   * @throws RExceptionMeshMissingUVs
   *           If the array does not have UV coordinates.
   * @throws RExceptionMeshMissingPositions
   *           If the array does not have positions.
   */

  public static KMesh newMesh(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices)
    throws RExceptionMeshMissingUVs,
      RExceptionMeshMissingNormals,
      RExceptionMeshMissingTangents,
      RExceptionMeshMissingPositions
  {
    return new KMesh(in_array, in_indices);
  }

  private final ArrayBufferType array;
  private boolean               deleted;
  private final IndexBufferType indices;

  private KMesh(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices)
    throws RExceptionMeshMissingUVs,
      RExceptionMeshMissingNormals,
      RExceptionMeshMissingTangents,
      RExceptionMeshMissingPositions
  {
    this.array = NullCheck.notNull(in_array, "Array");
    this.indices = NullCheck.notNull(in_indices, "Indices");

    final ArrayDescriptor d = this.array.arrayGetDescriptor();
    final Map<String, ArrayAttributeDescriptor> as = d.getAttributes();

    if (KMesh.hasPositions(as) == false) {
      throw RExceptionMeshMissingPositions.fromArray(this.array);
    }
    if (KMesh.hasUVs(as) == false) {
      throw RExceptionMeshMissingUVs.fromArray(this.array);
    }
    if (KMesh.hasNormals(as) == false) {
      throw RExceptionMeshMissingNormals.fromArray(this.array);
    }
    if (KMesh.hasTangents(as) == false) {
      throw RExceptionMeshMissingTangents.fromArray(this.array);
    }
  }

  /**
   * Delete the mesh.
   *
   * @param <G>
   *          The OpenGL capabilities required
   * @param gc
   *          The OpenGL interface
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> void delete(
    final G gc)
    throws JCGLException
  {
    NullCheck.notNull(gc, "GL interface");

    try {
      gc.arrayBufferDelete(this.array);
      gc.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
    }
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMesh other = (KMesh) obj;
    return this.array.equals(other.array)
      && (this.deleted == other.deleted)
      && this.indices.equals(other.indices);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.array.hashCode();
    result = (prime * result) + (this.deleted ? 1231 : 1237);
    result = (prime * result) + this.indices.hashCode();
    return result;
  }

  @Override public ArrayBufferUsableType meshGetArrayBuffer()
  {
    return this.array;
  }

  @Override public IndexBufferUsableType meshGetIndexBuffer()
  {
    return this.indices;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.array.resourceGetSizeBytes()
      + this.indices.resourceGetSizeBytes();
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }
}
