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

import java.math.BigInteger;

import com.io7m.jcache.JCacheLoaderType;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.CursorWritable2fType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.ResourceCheck;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;

/**
 * A unit quad, from <code>(-1, -1, -1)</code> to <code>(1, 1, -1)</code>,
 * oriented towards <code>+Z</code>.
 */

@EqualityReference public final class KUnitQuad implements
  KUnitQuadUsableType
{
  /**
   * Construct a new {@link JCacheLoaderType} that produces new
   * {@link KUnitQuad} instances as required.
   *
   * @param <G>
   *          The precise type of OpenGL interface required
   * @param gl
   *          The OpenGL interface
   * @param log
   *          A log interface
   * @return A cache loader
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    JCacheLoaderType<Unit, KUnitQuad, RException>
    newCacheLoader(
      final G gl,
      final LogUsableType log)
  {
    return new JCacheLoaderType<Unit, KUnitQuad, RException>() {
      @Override public void cacheValueClose(
        final KUnitQuad v)
        throws RException
      {
        v.delete(gl);
      }

      @Override public KUnitQuad cacheValueLoad(
        final Unit unused)
        throws RException
      {
        return KUnitQuad.newQuad(gl, log);
      }

      @Override public BigInteger cacheValueSizeOf(
        final KUnitQuad unused)
      {
        final BigInteger one = BigInteger.ONE;
        assert one != null;
        return one;
      }
    };
  }

  /**
   * Construct a new unit quad.
   *
   * @param gl
   *          The OpenGL interface
   * @param log
   *          A log handle
   * @param <G>
   *          The type of OpenGL interface
   * @return A new unit quad
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KUnitQuad
    newQuad(
      final G gl,
      final LogUsableType log)
      throws JCGLException
  {
    NullCheck.notNull(gl, "OpenGL interface");
    NullCheck.notNull(log, "Log handle");

    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      log.debug("Allocate unit quad");
    }

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    final ArrayDescriptor array_type = b.build();

    final ArrayBufferType array =
      gl.arrayBufferAllocate(4, array_type, UsageHint.USAGE_STATIC_DRAW);

    final ArrayBufferUpdateUnmappedType array_data =
      ArrayBufferUpdateUnmapped.newUpdateReplacingAll(array);

    {
      final CursorWritable3fType pos_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritable3fType norm_cursor =
        array_data.getCursor3f(KMeshAttributes.ATTRIBUTE_NORMAL.getName());
      final CursorWritable2fType uv_cursor =
        array_data.getCursor2f(KMeshAttributes.ATTRIBUTE_UV.getName());

      pos_cursor.put3f(-1, 1, -1);
      pos_cursor.put3f(-1, -1, -1);
      pos_cursor.put3f(1, -1, -1);
      pos_cursor.put3f(1, 1, -1);

      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);
      norm_cursor.put3f(0, 0, 1);

      uv_cursor.put2f(0.0f, 1.0f);
      uv_cursor.put2f(0.0f, 0.0f);
      uv_cursor.put2f(1.0f, 0.0f);
      uv_cursor.put2f(1.0f, 1.0f);
    }

    gl.arrayBufferBind(array);
    gl.arrayBufferUpdate(array_data);

    final IndexBufferType indices =
      gl.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_SHORT,
        6,
        UsageHint.USAGE_STATIC_DRAW);

    final IndexBufferUpdateUnmappedType indices_data =
      IndexBufferUpdateUnmapped.newReplacing(indices);

    {
      final CursorWritableIndexType ind_cursor = indices_data.getCursor();
      ind_cursor.putIndex(0);
      ind_cursor.putIndex(1);
      ind_cursor.putIndex(2);

      ind_cursor.putIndex(0);
      ind_cursor.putIndex(2);
      ind_cursor.putIndex(3);
    }

    gl.indexBufferUpdate(indices_data);
    gl.arrayBufferUnbind();

    return new KUnitQuad(array, indices);
  }

  private final ArrayBufferType array;
  private boolean               deleted;
  private final IndexBufferType indices;

  private KUnitQuad(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices)
  {
    this.array = in_array;
    this.indices = in_indices;
    this.deleted = false;
  }

  /**
   * Delete all resources associated with the quad.
   *
   * @param <G>
   *          The precise type of OpenGL interface required.
   * @param gc
   *          The OpenGL interface
   * @throws JCGLException
   *           If an error occurs
   */

  public <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> void delete(
    final G gc)
    throws JCGLException
  {
    ResourceCheck.notDeleted(this);

    try {
      gc.arrayBufferDelete(this.array);
      gc.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
    }
  }

  /**
   * @return The array buffer that backs the quad
   */

  @Override public ArrayBufferUsableType getArray()
  {
    return this.array;
  }

  /**
   * @return The index buffer that backs the quad
   */

  @Override public IndexBufferUsableType getIndices()
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

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KUnitQuad ");
    builder.append(this.array);
    builder.append(" ");
    builder.append(this.indices);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
