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

package com.io7m.renderer.kernel.sandbox;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionFrustum;
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionOrthographic;
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionPerspective;
import com.io7m.renderer.kernel.types.KMeshAttributes;

public abstract class SBVisibleProjection
{
  private static class SBVisibleProjectionFrustum extends SBVisibleProjection
  {
    private final ArrayBufferType array;
    private final IndexBufferType indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> SBVisibleProjectionFrustum(
      final G g,
      final SBProjectionFrustum p,
      final LogUsableType log)
      throws JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_FRUSTUM);

      if (log.wouldLog(LogLevel.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible frustum ");
        m.append(p);
        final String r = m.toString();
        assert r != null;
        log.debug(r);
      }

      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayDescriptor type = b.build();

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_DRAW);
      this.indices =
        g.indexBufferAllocate(this.array, 34, UsageHint.USAGE_STATIC_DRAW);

      final ArrayBufferUpdateUnmappedType array_map =
        ArrayBufferUpdateUnmapped.newUpdateReplacingAll(this.array);
      final IndexBufferUpdateUnmappedType index_map =
        IndexBufferUpdateUnmapped.newReplacing(this.indices);

      final CursorWritable3fType pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndexType ic = index_map.getCursor();

      final float near_z = (float) p.getNear();
      final float near_x_min = (float) p.getLeft();
      final float near_x_max = (float) p.getRight();
      final float near_y_min = (float) p.getBottom();
      final float near_y_max = (float) p.getTop();

      final float far_z = (float) p.getFar();
      final float far_x_min = near_x_min * (far_z / near_z);
      final float far_x_max = near_x_max * (far_z / near_z);
      final float far_y_min = near_y_min * (far_z / near_z);
      final float far_y_max = near_y_max * (far_z / near_z);

      pc.put3f(0.0f, 0.0f, 0.0f);
      pc.put3f(near_x_min, near_y_min, -near_z);
      pc.put3f(near_x_min, near_y_max, -near_z);
      pc.put3f(near_x_max, near_y_max, -near_z);
      pc.put3f(near_x_max, near_y_min, -near_z);
      pc.put3f(far_x_min, far_y_min, -far_z);
      pc.put3f(far_x_min, far_y_max, -far_z);
      pc.put3f(far_x_max, far_y_max, -far_z);
      pc.put3f(far_x_max, far_y_min, -far_z);
      pc.put3f(0, 0, -far_z);

      /**
       * Lines from origin to near plane
       */

      ic.putIndex(0);
      ic.putIndex(1);

      ic.putIndex(0);
      ic.putIndex(2);

      ic.putIndex(0);
      ic.putIndex(3);

      ic.putIndex(0);
      ic.putIndex(4);

      /**
       * Lines from origin to far plane
       */

      ic.putIndex(0);
      ic.putIndex(5);

      ic.putIndex(0);
      ic.putIndex(6);

      ic.putIndex(0);
      ic.putIndex(7);

      ic.putIndex(0);
      ic.putIndex(8);

      /**
       * Frame the near end.
       */

      ic.putIndex(1);
      ic.putIndex(2);

      ic.putIndex(1);
      ic.putIndex(4);

      ic.putIndex(2);
      ic.putIndex(3);

      ic.putIndex(3);
      ic.putIndex(4);

      /**
       * Frame the far end.
       */

      ic.putIndex(5);
      ic.putIndex(6);

      ic.putIndex(7);
      ic.putIndex(8);

      ic.putIndex(5);
      ic.putIndex(8);

      ic.putIndex(6);
      ic.putIndex(7);

      /**
       * A line from the origin to the end of the box
       */

      ic.putIndex(0);
      ic.putIndex(9);

      g.indexBufferUpdate(index_map);
      g.arrayBufferBind(this.array);
      try {
        g.arrayBufferUpdate(array_map);
      } finally {
        g.arrayBufferUnbind();
      }
    }

    @Override public
      <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
      void
      delete(
        final G g)
        throws JCGLException
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public ArrayBufferUsableType getArrayBuffer()
    {
      return this.array;
    }

    @Override public IndexBufferUsableType getIndexBuffer()
    {
      return this.indices;
    }
  }

  private static class SBVisibleProjectionOrthographic extends
    SBVisibleProjection
  {
    private final ArrayBufferType array;
    private final IndexBufferType indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> SBVisibleProjectionOrthographic(
      final G g,
      final SBProjectionOrthographic p,
      final LogUsableType log)
      throws JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC);

      if (log.wouldLog(LogLevel.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible orthographic ");
        m.append(p);
        final String r = m.toString();
        assert r != null;
        log.debug(r);
      }

      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayDescriptor type = b.build();

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_DRAW);
      this.indices =
        g.indexBufferAllocate(this.array, 34, UsageHint.USAGE_STATIC_DRAW);

      final ArrayBufferUpdateUnmappedType array_map =
        ArrayBufferUpdateUnmapped.newUpdateReplacingAll(this.array);
      final IndexBufferUpdateUnmappedType index_map =
        IndexBufferUpdateUnmapped.newReplacing(this.indices);

      final CursorWritable3fType pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndexType ic = index_map.getCursor();

      final float near_x_min = (float) p.getLeft();
      final float near_x_max = (float) p.getRight();
      final float near_y_min = (float) p.getBottom();
      final float near_y_max = (float) p.getTop();
      final float far_x_min = near_x_min;
      final float far_x_max = near_x_max;
      final float far_y_min = near_y_min;
      final float far_y_max = near_y_max;
      final float near_z = (float) p.getNear();
      final float far_z = (float) p.getFar();

      pc.put3f(0.0f, 0.0f, 0.0f);
      pc.put3f(near_x_min, near_y_min, -near_z);
      pc.put3f(near_x_min, near_y_max, -near_z);
      pc.put3f(near_x_max, near_y_max, -near_z);
      pc.put3f(near_x_max, near_y_min, -near_z);
      pc.put3f(far_x_min, far_y_min, -far_z);
      pc.put3f(far_x_min, far_y_max, -far_z);
      pc.put3f(far_x_max, far_y_max, -far_z);
      pc.put3f(far_x_max, far_y_min, -far_z);
      pc.put3f(0, 0, -far_z);

      /**
       * Lines from origin to near plane
       */

      ic.putIndex(0);
      ic.putIndex(1);

      ic.putIndex(0);
      ic.putIndex(2);

      ic.putIndex(0);
      ic.putIndex(3);

      ic.putIndex(0);
      ic.putIndex(4);

      /**
       * Frame the near end.
       */

      ic.putIndex(1);
      ic.putIndex(2);

      ic.putIndex(1);
      ic.putIndex(4);

      ic.putIndex(2);
      ic.putIndex(3);

      ic.putIndex(3);
      ic.putIndex(4);

      /**
       * Frame the far end.
       */

      ic.putIndex(5);
      ic.putIndex(6);

      ic.putIndex(7);
      ic.putIndex(8);

      ic.putIndex(5);
      ic.putIndex(8);

      ic.putIndex(6);
      ic.putIndex(7);

      /**
       * Lines from the near plane to the far plane.
       */

      ic.putIndex(1);
      ic.putIndex(5);

      ic.putIndex(2);
      ic.putIndex(6);

      ic.putIndex(3);
      ic.putIndex(7);

      ic.putIndex(4);
      ic.putIndex(8);

      /**
       * A line from the origin to the end of the box
       */

      ic.putIndex(0);
      ic.putIndex(9);

      g.arrayBufferBind(this.array);
      try {
        g.arrayBufferUpdate(array_map);
      } finally {
        g.arrayBufferUnbind();
      }
      g.indexBufferUpdate(index_map);
    }

    @Override public
      <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
      void
      delete(
        final G g)
        throws JCGLException
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public ArrayBufferUsableType getArrayBuffer()
    {
      return this.array;
    }

    @Override public IndexBufferUsableType getIndexBuffer()
    {
      return this.indices;
    }
  }

  private static class SBVisibleProjectionPerspective extends
    SBVisibleProjection
  {
    private final ArrayBufferType array;
    private final IndexBufferType indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffersType & JCGLIndexBuffersType> SBVisibleProjectionPerspective(
      final G g,
      final SBProjectionPerspective p,
      final LogUsableType log)
      throws JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_PERSPECTIVE);

      if (log.wouldLog(LogLevel.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible perspective ");
        m.append(p);
        final String r = m.toString();
        assert r != null;
        log.debug(r);
      }

      final float near_z = (float) p.getNear();
      final float near_x =
        (float) (near_z * Math.tan(p.getHorizontalFOV() / 2.0f));
      final float near_y = (float) (near_x / p.getAspect());

      final float far_z = (float) p.getFar();
      final float far_x =
        (float) (far_z * Math.tan(p.getHorizontalFOV() / 2.0f));
      final float far_y = (float) (far_x / p.getAspect());

      final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
      b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayDescriptor type = b.build();

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_DRAW);
      this.indices =
        g.indexBufferAllocate(this.array, 34, UsageHint.USAGE_STATIC_DRAW);

      final ArrayBufferUpdateUnmappedType array_map =
        ArrayBufferUpdateUnmapped.newUpdateReplacingAll(this.array);
      final IndexBufferUpdateUnmappedType index_map =
        IndexBufferUpdateUnmapped.newReplacing(this.indices);

      final CursorWritable3fType pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndexType ic = index_map.getCursor();

      pc.put3f(0.0f, 0.0f, 0.0f);
      pc.put3f(-near_x, -near_y, -near_z);
      pc.put3f(-near_x, +near_y, -near_z);
      pc.put3f(+near_x, +near_y, -near_z);
      pc.put3f(+near_x, -near_y, -near_z);
      pc.put3f(-far_x, -far_y, -far_z);
      pc.put3f(-far_x, +far_y, -far_z);
      pc.put3f(+far_x, +far_y, -far_z);
      pc.put3f(+far_x, -far_y, -far_z);
      pc.put3f(0, 0, -far_z);

      /**
       * Lines from origin to near plane
       */

      ic.putIndex(0);
      ic.putIndex(1);

      ic.putIndex(0);
      ic.putIndex(2);

      ic.putIndex(0);
      ic.putIndex(3);

      ic.putIndex(0);
      ic.putIndex(4);

      /**
       * Lines from origin to far plane
       */

      ic.putIndex(0);
      ic.putIndex(5);

      ic.putIndex(0);
      ic.putIndex(6);

      ic.putIndex(0);
      ic.putIndex(7);

      ic.putIndex(0);
      ic.putIndex(8);

      /**
       * Frame the near end.
       */

      ic.putIndex(1);
      ic.putIndex(2);

      ic.putIndex(1);
      ic.putIndex(4);

      ic.putIndex(2);
      ic.putIndex(3);

      ic.putIndex(3);
      ic.putIndex(4);

      /**
       * Frame the far end.
       */

      ic.putIndex(5);
      ic.putIndex(6);

      ic.putIndex(7);
      ic.putIndex(8);

      ic.putIndex(5);
      ic.putIndex(8);

      ic.putIndex(6);
      ic.putIndex(7);

      /**
       * A line from the origin to the end of the frustum
       */

      ic.putIndex(0);
      ic.putIndex(9);

      g.arrayBufferBind(this.array);
      try {
        g.arrayBufferUpdate(array_map);
      } finally {
        g.arrayBufferUnbind();
      }
      g.indexBufferUpdate(index_map);
    }

    @Override public
      <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
      void
      delete(
        final G g)
        throws JCGLException
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public ArrayBufferUsableType getArrayBuffer()
    {
      return this.array;
    }

    @Override public IndexBufferUsableType getIndexBuffer()
    {
      return this.indices;
    }
  }

  public static
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    SBVisibleProjection
    make(
      final G g,
      final SBProjectionDescription p,
      final LogUsableType log)
      throws JCGLException
  {
    switch (p.getType()) {
      case PROJECTION_FRUSTUM:
      {
        return new SBVisibleProjectionFrustum(g, (SBProjectionFrustum) p, log);
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        return new SBVisibleProjectionOrthographic(
          g,
          (SBProjectionOrthographic) p,
          log);
      }
      case PROJECTION_PERSPECTIVE:
      {
        return new SBVisibleProjectionPerspective(
          g,
          (SBProjectionPerspective) p,
          log);
      }
    }

    throw new UnreachableCodeException();
  }

  private final SBProjectionDescription.Type type;

  private SBVisibleProjection(
    final SBProjectionDescription.Type in_type)
  {
    this.type = NullCheck.notNull(in_type, "Type");
  }

  public abstract
    <G extends JCGLArrayBuffersType & JCGLIndexBuffersType>
    void
    delete(
      final G g)
      throws JCGLException;

  public abstract ArrayBufferUsableType getArrayBuffer();

  public abstract IndexBufferUsableType getIndexBuffer();

  public SBProjectionDescription.Type getType()
  {
    return this.type;
  }
}
