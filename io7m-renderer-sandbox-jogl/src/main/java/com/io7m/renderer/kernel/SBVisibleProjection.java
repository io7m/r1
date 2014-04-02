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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBuffer;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.ArrayBufferTypeDescriptor;
import com.io7m.jcanephora.ArrayBufferUsable;
import com.io7m.jcanephora.ArrayBufferWritableData;
import com.io7m.jcanephora.CursorWritable3f;
import com.io7m.jcanephora.CursorWritableIndex;
import com.io7m.jcanephora.IndexBuffer;
import com.io7m.jcanephora.IndexBufferUsable;
import com.io7m.jcanephora.IndexBufferWritableData;
import com.io7m.jcanephora.JCGLArrayBuffers;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLIndexBuffers;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionFrustum;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionOrthographic;
import com.io7m.renderer.kernel.SBProjectionDescription.SBProjectionPerspective;
import com.io7m.renderer.kernel.types.KMeshAttributes;

public abstract class SBVisibleProjection
{
  private static class SBVisibleProjectionFrustum extends SBVisibleProjection
  {
    private final @Nonnull ArrayBuffer array;
    private final @Nonnull IndexBuffer indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffers & JCGLIndexBuffers> SBVisibleProjectionFrustum(
      final @Nonnull G g,
      final @Nonnull SBProjectionFrustum p,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_FRUSTUM);

      if (log.enabled(Level.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible frustum ");
        m.append(p);
        log.debug(m.toString());
      }

      final List<ArrayBufferAttributeDescriptor> abs =
        new ArrayList<ArrayBufferAttributeDescriptor>();
      abs.add(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayBufferTypeDescriptor type =
        new ArrayBufferTypeDescriptor(abs);

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_READ);
      this.indices = g.indexBufferAllocate(this.array, 34);

      final ArrayBufferWritableData array_map =
        new ArrayBufferWritableData(this.array);
      final IndexBufferWritableData index_map =
        new IndexBufferWritableData(this.indices);

      final CursorWritable3f pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndex ic = index_map.getCursor();

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
      <G extends JCGLArrayBuffers & JCGLIndexBuffers>
      void
      delete(
        final @Nonnull G g)
        throws JCGLException,
          ConstraintError
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public @Nonnull ArrayBufferUsable getArrayBuffer()
    {
      return this.array;
    }

    @Override public @Nonnull IndexBufferUsable getIndexBuffer()
    {
      return this.indices;
    }
  }

  private static class SBVisibleProjectionOrthographic extends
    SBVisibleProjection
  {
    private final @Nonnull ArrayBuffer array;
    private final @Nonnull IndexBuffer indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffers & JCGLIndexBuffers> SBVisibleProjectionOrthographic(
      final @Nonnull G g,
      final @Nonnull SBProjectionOrthographic p,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC);

      if (log.enabled(Level.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible orthographic ");
        m.append(p);
        log.debug(m.toString());
      }

      final List<ArrayBufferAttributeDescriptor> abs =
        new ArrayList<ArrayBufferAttributeDescriptor>();
      abs.add(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayBufferTypeDescriptor type =
        new ArrayBufferTypeDescriptor(abs);

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_READ);
      this.indices = g.indexBufferAllocate(this.array, 34);

      final ArrayBufferWritableData array_map =
        new ArrayBufferWritableData(this.array);
      final IndexBufferWritableData index_map =
        new IndexBufferWritableData(this.indices);

      final CursorWritable3f pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndex ic = index_map.getCursor();

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
      <G extends JCGLArrayBuffers & JCGLIndexBuffers>
      void
      delete(
        final @Nonnull G g)
        throws JCGLException,
          ConstraintError
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public @Nonnull ArrayBufferUsable getArrayBuffer()
    {
      return this.array;
    }

    @Override public @Nonnull IndexBufferUsable getIndexBuffer()
    {
      return this.indices;
    }
  }

  private static class SBVisibleProjectionPerspective extends
    SBVisibleProjection
  {
    private final @Nonnull ArrayBuffer array;
    private final @Nonnull IndexBuffer indices;

    @SuppressWarnings("synthetic-access") <G extends JCGLArrayBuffers & JCGLIndexBuffers> SBVisibleProjectionPerspective(
      final @Nonnull G g,
      final @Nonnull SBProjectionPerspective p,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLException
    {
      super(SBProjectionDescription.Type.PROJECTION_PERSPECTIVE);

      if (log.enabled(Level.LOG_DEBUG)) {
        final StringBuilder m = new StringBuilder();
        m.append("Allocate visible perspective ");
        m.append(p);
        log.debug(m.toString());
      }

      final float near_z = (float) p.getNear();
      final float near_x =
        (float) (near_z * Math.tan(p.getHorizontalFOV() / 2.0f));
      final float near_y = (float) (near_x / p.getAspect());

      final float far_z = (float) p.getFar();
      final float far_x =
        (float) (far_z * Math.tan(p.getHorizontalFOV() / 2.0f));
      final float far_y = (float) (far_x / p.getAspect());

      final List<ArrayBufferAttributeDescriptor> abs =
        new ArrayList<ArrayBufferAttributeDescriptor>();
      abs.add(KMeshAttributes.ATTRIBUTE_POSITION);
      final ArrayBufferTypeDescriptor type =
        new ArrayBufferTypeDescriptor(abs);

      this.array =
        g.arrayBufferAllocate(10, type, UsageHint.USAGE_STATIC_READ);
      this.indices = g.indexBufferAllocate(this.array, 34);

      final ArrayBufferWritableData array_map =
        new ArrayBufferWritableData(this.array);
      final IndexBufferWritableData index_map =
        new IndexBufferWritableData(this.indices);

      final CursorWritable3f pc =
        array_map.getCursor3f(KMeshAttributes.ATTRIBUTE_POSITION.getName());
      final CursorWritableIndex ic = index_map.getCursor();

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
      <G extends JCGLArrayBuffers & JCGLIndexBuffers>
      void
      delete(
        final @Nonnull G g)
        throws JCGLException,
          ConstraintError
    {
      g.arrayBufferDelete(this.array);
      g.indexBufferDelete(this.indices);
    }

    @Override public @Nonnull ArrayBufferUsable getArrayBuffer()
    {
      return this.array;
    }

    @Override public @Nonnull IndexBufferUsable getIndexBuffer()
    {
      return this.indices;
    }
  }

  public static @Nonnull
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    SBVisibleProjection
    make(
      final @Nonnull G g,
      final @Nonnull SBProjectionDescription p,
      final @Nonnull Log log)
      throws ConstraintError,
        JCGLException
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

  private final @Nonnull SBProjectionDescription.Type type;

  private SBVisibleProjection(
    final @Nonnull SBProjectionDescription.Type in_type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(in_type, "Type");
  }

  public abstract
    <G extends JCGLArrayBuffers & JCGLIndexBuffers>
    void
    delete(
      final @Nonnull G g)
      throws JCGLException,
        ConstraintError;

  public abstract @Nonnull ArrayBufferUsable getArrayBuffer();

  public abstract @Nonnull IndexBufferUsable getIndexBuffer();

  public @Nonnull SBProjectionDescription.Type getType()
  {
    return this.type;
  }
}
