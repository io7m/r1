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
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionAttributeDuplicate;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionJCGL;

/**
 * A frustum mesh.
 */

@EqualityReference public final class KFrustumMesh implements
  KFrustumMeshUsableType
{
  private static final JCGLUnsignedType INDICES_TYPE;
  private static final long             ARRAY_SIZE_BYTES;
  private static final ArrayDescriptor  ARRAY_TYPE;
  private static final int              ARRAY_VERTEX_COUNT;
  private static final int              INDICES_COUNT;
  private static final int              INDICES_SIZE_BYTES;
  private static final int              INDICES_TRIANGLE_COUNT;

  static {
    try {
      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      ARRAY_TYPE = b.build();

      ARRAY_VERTEX_COUNT = 8;
      ARRAY_SIZE_BYTES =
        KFrustumMesh.ARRAY_VERTEX_COUNT
          * KFrustumMesh.ARRAY_TYPE.getElementSizeBytes();

      INDICES_TYPE = JCGLUnsignedType.TYPE_UNSIGNED_SHORT;
      INDICES_TRIANGLE_COUNT = 12;
      INDICES_COUNT = KFrustumMesh.INDICES_TRIANGLE_COUNT * 3;

      INDICES_SIZE_BYTES =
        KFrustumMesh.INDICES_COUNT * KFrustumMesh.INDICES_TYPE.getSizeBytes();

    } catch (final JCGLExceptionAttributeDuplicate e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * @return The size in bytes of the data backing the frustum mesh.
   */

  public static long getFrustumMeshSizeBytes()
  {
    return KFrustumMesh.ARRAY_SIZE_BYTES + KFrustumMesh.INDICES_SIZE_BYTES;
  }

  /**
   * Construct a new {@link JCacheLoaderType} that produces new
   * {@link KFrustumMesh} instances as required.
   *
   * @param <G>
   *          The precise type of OpenGL interface required
   * @param au_cons
   *          An array buffer update constructor.
   * @param iu_cons
   *          An index buffer update constructor.
   * @param g
   *          The OpenGL interface
   * @param log
   *          A log interface
   * @return A cache loader
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    JCacheLoaderType<KProjectionType, KFrustumMesh, RException>
    newCacheLoader(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final LogUsableType log)
  {
    return new JCacheLoaderType<KProjectionType, KFrustumMesh, RException>() {
      @Override public void cacheValueClose(
        final KFrustumMesh m)
        throws RException
      {
        try {
          m.delete(g);
        } catch (final JCGLException e) {
          throw RExceptionJCGL.fromJCGLException(e);
        }
      }

      @Override public KFrustumMesh cacheValueLoad(
        final KProjectionType p)
        throws RException
      {
        try {
          if (log.wouldLog(LogLevel.LOG_DEBUG)) {
            final String s =
              String.format("constructing frustum mesh for %s", p);
            assert s != null;
            log.debug(s);
          }
          return KFrustumMesh.newFromGeneral(g, au_cons, iu_cons, p);
        } catch (final JCGLException e) {
          throw RExceptionJCGL.fromJCGLException(e);
        }
      }

      @Override public BigInteger cacheValueSizeOf(
        final KFrustumMesh m)
      {
        final long s = m.resourceGetSizeBytes();
        final BigInteger r = BigInteger.valueOf(s);
        assert r != null;
        return r;
      }
    };
  }

  /**
   * Construct a frustum mesh from the given projection.
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param au_cons
   *          An array buffer update constructor.
   * @param iu_cons
   *          An index buffer update constructor.
   * @param g
   *          The OpenGL interface.
   * @param p
   *          The projection.
   * @return A frustum mesh.
   * @throws JCGLException
   *           On errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMesh
    newFromFOV(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final KProjectionFOV p)
      throws JCGLException
  {
    final float near_z = p.projectionGetZNear();
    final float near_x =
      (float) (near_z * Math.tan(p.getHorizontalFOV() / 2.0f));
    final float near_y = near_x / p.getAspectRatio();

    final float far_z = p.projectionGetZFar();
    final float far_x =
      (float) (far_z * Math.tan(p.getHorizontalFOV() / 2.0f));
    final float far_y = far_x / p.getAspectRatio();

    final float near_x_min = -near_x;
    final float near_x_max = near_x;
    final float near_y_min = -near_y;
    final float near_y_max = near_y;

    final float far_x_min = -far_x;
    final float far_x_max = far_x;
    final float far_y_min = -far_y;
    final float far_y_max = far_y;

    return KFrustumMesh.newMesh(
      g,
      au_cons,
      iu_cons,
      near_x_min,
      near_x_max,
      near_y_min,
      near_y_max,
      -near_z,
      far_x_min,
      far_x_max,
      far_y_min,
      far_y_max,
      -far_z);
  }

  /**
   * Construct a frustum mesh from the given projection.
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param au_cons
   *          An array buffer update constructor.
   * @param iu_cons
   *          An index buffer update constructor.
   * @param g
   *          The OpenGL interface.
   * @param p
   *          The projection.
   * @return A frustum mesh.
   * @throws JCGLException
   *           On errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMesh
    newFromFrustum(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final KProjectionFrustum p)
      throws JCGLException
  {
    final float near_z = p.projectionGetZNear();
    final float near_x_min = p.projectionGetXMinimum();
    final float near_x_max = p.projectionGetXMaximum();
    final float near_y_min = p.projectionGetYMinimum();
    final float near_y_max = p.projectionGetYMaximum();

    final float far_z = p.projectionGetZFar();
    final float far_x_min = near_x_min * (far_z / near_z);
    final float far_x_max = near_x_max * (far_z / near_z);
    final float far_y_min = near_y_min * (far_z / near_z);
    final float far_y_max = near_y_max * (far_z / near_z);

    return KFrustumMesh.newMesh(
      g,
      au_cons,
      iu_cons,
      near_x_min,
      near_x_max,
      near_y_min,
      near_y_max,
      -near_z,
      far_x_min,
      far_x_max,
      far_y_min,
      far_y_max,
      -far_z);
  }

  /**
   * Construct a frustum mesh from the given projection.
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param au_cons
   *          An array buffer update constructor.
   * @param iu_cons
   *          An index buffer update constructor.
   * @param g
   *          The OpenGL interface.
   * @param p
   *          The projection.
   * @return A frustum mesh.
   * @throws JCGLException
   *           On errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMesh
    newFromGeneral(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final KProjectionType p)
      throws JCGLException
  {
    try {
      return p
        .projectionAccept(new KProjectionVisitorType<KFrustumMesh, JCGLException>() {
          @Override public KFrustumMesh fov(
            final KProjectionFOV pf)
            throws JCGLException
          {
            return KFrustumMesh.newFromFOV(g, au_cons, iu_cons, pf);
          }

          @Override public KFrustumMesh frustum(
            final KProjectionFrustum pf)
            throws JCGLException
          {
            return KFrustumMesh.newFromFrustum(g, au_cons, iu_cons, pf);
          }

          @Override public KFrustumMesh orthographic(
            final KProjectionOrthographic po)
            throws JCGLException
          {
            return KFrustumMesh.newFromOrthographic(g, au_cons, iu_cons, po);
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Construct a frustum mesh from the given projection.
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param au_cons
   *          An array buffer update constructor.
   * @param iu_cons
   *          An index buffer update constructor.
   * @param g
   *          The OpenGL interface.
   * @param p
   *          The projection.
   * @return A frustum mesh.
   * @throws JCGLException
   *           On errors.
   */

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMesh
    newFromOrthographic(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final KProjectionOrthographic p)
      throws JCGLException
  {
    final float near_x_min = p.projectionGetXMinimum();
    final float near_x_max = p.projectionGetXMaximum();
    final float near_y_min = p.projectionGetYMinimum();
    final float near_y_max = p.projectionGetYMaximum();

    final float far_x_min = p.projectionGetXMinimum();
    final float far_x_max = p.projectionGetXMaximum();
    final float far_y_min = p.projectionGetYMinimum();
    final float far_y_max = p.projectionGetYMaximum();

    final float near_z = -p.projectionGetZNear();
    final float far_z = -p.projectionGetZFar();

    return KFrustumMesh.newMesh(
      g,
      au_cons,
      iu_cons,
      near_x_min,
      near_x_max,
      near_y_min,
      near_y_max,
      near_z,
      far_x_min,
      far_x_max,
      far_y_min,
      far_y_max,
      far_z);
  }

  private static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    KFrustumMesh
    newMesh(
      final G g,
      final ArrayBufferUpdateUnmappedConstructorType au_cons,
      final IndexBufferUpdateUnmappedConstructorType iu_cons,
      final float near_x_min,
      final float near_x_max,
      final float near_y_min,
      final float near_y_max,
      final float near_z,
      final float far_x_min,
      final float far_x_max,
      final float far_y_min,
      final float far_y_max,
      final float far_z)
      throws JCGLException
  {
    final ArrayBufferType a =
      g.arrayBufferAllocate(
        KFrustumMesh.ARRAY_VERTEX_COUNT,
        KFrustumMesh.ARRAY_TYPE,
        UsageHint.USAGE_STATIC_DRAW);
    final ArrayBufferUpdateUnmappedType am = au_cons.newUpdateReplacingAll(a);
    final CursorWritable3fType pc =
      am.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());

    pc.put3f(near_x_min, near_y_min, near_z);
    pc.put3f(near_x_min, near_y_max, near_z);
    pc.put3f(near_x_max, near_y_max, near_z);
    pc.put3f(near_x_max, near_y_min, near_z);

    pc.put3f(far_x_min, far_y_min, far_z);
    pc.put3f(far_x_min, far_y_max, far_z);
    pc.put3f(far_x_max, far_y_max, far_z);
    pc.put3f(far_x_max, far_y_min, far_z);

    final IndexBufferType i =
      g.indexBufferAllocateType(
        KFrustumMesh.INDICES_TYPE,
        KFrustumMesh.INDICES_COUNT,
        UsageHint.USAGE_STATIC_DRAW);

    final IndexBufferUpdateUnmappedType im = iu_cons.newReplacing(i);
    final CursorWritableIndexType ic = im.getCursor();

    /**
     * Front side triangles.
     */

    KFrustumMesh.triangle(ic, 1, 0, 3);
    KFrustumMesh.triangle(ic, 1, 3, 2);

    /**
     * Left side triangles.
     */

    KFrustumMesh.triangle(ic, 5, 4, 0);
    KFrustumMesh.triangle(ic, 5, 0, 1);

    /**
     * Right side triangles.
     */

    KFrustumMesh.triangle(ic, 6, 3, 7);
    KFrustumMesh.triangle(ic, 6, 2, 3);

    /**
     * Top side triangles.
     */

    KFrustumMesh.triangle(ic, 5, 1, 2);
    KFrustumMesh.triangle(ic, 5, 2, 6);

    /**
     * Bottom side triangles.
     */

    KFrustumMesh.triangle(ic, 4, 3, 0);
    KFrustumMesh.triangle(ic, 4, 7, 3);

    /**
     * Back side triangles.
     */

    KFrustumMesh.triangle(ic, 5, 7, 4);
    KFrustumMesh.triangle(ic, 6, 7, 5);

    assert ic.hasNext() == false;
    assert pc.hasNext() == false;

    g.arrayBufferBind(a);
    try {
      g.arrayBufferUpdate(am);
    } finally {
      g.arrayBufferUnbind();
    }
    g.indexBufferUpdate(im);

    return new KFrustumMesh(
      a,
      i,
      near_x_min,
      near_x_max,
      near_y_min,
      near_y_max,
      near_z,
      far_x_min,
      far_x_max,
      far_y_min,
      far_y_max,
      far_z);
  }

  private static void triangle(
    final CursorWritableIndexType ic,
    final long v0,
    final long v1,
    final long v2)
  {
    ic.putIndex(v0);
    ic.putIndex(v1);
    ic.putIndex(v2);
  }

  private final ArrayBufferType array;
  private boolean               deleted;
  private final float           far_x_max;
  private final float           far_x_min;
  private final float           far_y_max;
  private final float           far_y_min;
  private final float           far_z;
  private final IndexBufferType indices;
  private final float           near_x_max;
  private final float           near_x_min;
  private final float           near_y_max;
  private final float           near_y_min;
  private final float           near_z;
  private final long            size;

  private KFrustumMesh(
    final ArrayBufferType in_array,
    final IndexBufferType in_indices,
    final float in_near_x_min,
    final float in_near_x_max,
    final float in_near_y_min,
    final float in_near_y_max,
    final float in_near_z,
    final float in_far_x_min,
    final float in_far_x_max,
    final float in_far_y_min,
    final float in_far_y_max,
    final float in_far_z)
  {
    this.array = NullCheck.notNull(in_array, "Array");
    this.indices = NullCheck.notNull(in_indices, "Indices");
    this.deleted = false;
    this.size =
      this.array.resourceGetSizeBytes() + this.indices.resourceGetSizeBytes();

    this.near_x_min = in_near_x_min;
    this.near_x_max = in_near_x_max;
    this.near_y_min = in_near_y_min;
    this.near_y_max = in_near_y_max;
    this.near_z = in_near_z;

    this.far_x_min = in_far_x_min;
    this.far_x_max = in_far_x_max;
    this.far_y_min = in_far_y_min;
    this.far_y_max = in_far_y_max;
    this.far_z = in_far_z;
  }

  /**
   * <p>
   * Delete all resources associated with the given mesh.
   * </p>
   *
   * @param <G>
   *          The precise type of OpenGL interface.
   * @param g
   *          The OpenGL interface.
   * @throws JCGLException
   *           On errors.
   */

  public <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> void delete(
    final G g)
    throws JCGLException
  {
    try {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    } finally {
      this.deleted = true;
    }
  }

  @Override public ArrayBufferUsableType getArray()
  {
    return this.array;
  }

  /**
   * @return The maximum X value on the far plane.
   */

  public float getFarXMaximum()
  {
    return this.far_x_max;
  }

  /**
   * @return The minimum X value on the far plane.
   */

  public float getFarXMinimum()
  {
    return this.far_x_min;
  }

  /**
   * @return The maximum Y value on the far plane.
   */

  public float getFarYMaximum()
  {
    return this.far_y_max;
  }

  /**
   * @return The minimum Y value on the far plane.
   */

  public float getFarYMinimum()
  {
    return this.far_y_min;
  }

  /**
   * @return The Z value on the far plane.
   */

  public float getFarZ()
  {
    return this.far_z;
  }

  @Override public IndexBufferUsableType getIndices()
  {
    return this.indices;
  }

  /**
   * @return The maximum X value on the near plane.
   */

  public float getNearXMaximum()
  {
    return this.near_x_max;
  }

  /**
   * @return The minimum X value on the near plane.
   */

  public float getNearXMinimum()
  {
    return this.near_x_min;
  }

  /**
   * @return The maximum Y value on the near plane.
   */

  public float getNearYMaximum()
  {
    return this.near_y_max;
  }

  /**
   * @return The minimum Y value on the near plane.
   */

  public float getNearYMinimum()
  {
    return this.near_y_min;
  }

  /**
   * @return The Z value of the near plane.
   */

  public float getNearZ()
  {
    return this.near_z;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.size;
  }

  @Override public boolean resourceIsDeleted()
  {
    return this.deleted;
  }
}
