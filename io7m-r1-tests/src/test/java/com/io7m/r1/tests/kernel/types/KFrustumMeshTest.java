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

package com.io7m.r1.tests.kernel.types;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmapped;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.CursorWritable2fType;
import com.io7m.jcanephora.CursorWritable3fType;
import com.io7m.jcanephora.CursorWritable4fType;
import com.io7m.jcanephora.CursorWritableIndexType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmapped;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedConstructorType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.IndexBufferUsableType;
import com.io7m.jcanephora.JCGLExceptionAttributeMissing;
import com.io7m.jcanephora.JCGLExceptionDeleted;
import com.io7m.jcanephora.JCGLExceptionTypeError;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jranges.RangeCheckException;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.r1.kernel.types.KFrustumMesh;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.kernel.types.KProjectionFrustum;
import com.io7m.r1.kernel.types.KProjectionOrthographic;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;

@SuppressWarnings({ "boxing", "static-method" }) public final class KFrustumMeshTest
{
  @Test public void testDumpObj()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        10.0f);

    final ArrayBufferUpdateUnmappedConstructorType au_cons =
      new ArrayBufferUpdateUnmappedConstructorType() {
        @Override public ArrayBufferUpdateUnmappedType newUpdateReplacingAll(
          final ArrayBufferType array)
          throws JCGLExceptionDeleted
        {
          return new ArrayBufferUpdateUnmappedType() {
            @Override public ArrayBufferUsableType getArrayBuffer()
            {
              return array;
            }

            @Override public CursorWritable2fType getCursor2f(
              final String attribute_name)
              throws JCGLExceptionAttributeMissing,
                JCGLExceptionTypeError
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public CursorWritable3fType getCursor3f(
              final String attribute_name)
              throws JCGLExceptionAttributeMissing,
                JCGLExceptionTypeError
            {
              return new CursorWritable3fType() {

                long current = 0;
                long max     = array.bufferGetRange().getUpper();

                @Override public boolean hasNext()
                {
                  return this.current < this.max;
                }

                @Override public boolean isValid()
                {
                  return this.current <= this.max;
                }

                @Override public void next()
                {
                  this.current = this.current + 1;
                }

                @Override public void put3f(
                  final float x,
                  final float y,
                  final float z)
                {
                  System.out.printf("v %f %f %f\n", x, y, z);
                  this.next();
                }

                @Override public void seekTo(
                  final long index)
                {
                  this.current = index;
                }
              };
            }

            @Override public CursorWritable4fType getCursor4f(
              final String attribute_name)
              throws JCGLExceptionAttributeMissing,
                JCGLExceptionTypeError
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public ByteBuffer getTargetData()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public long getTargetDataOffset()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public long getTargetDataSize()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }
          };
        }

        @Override public
          ArrayBufferUpdateUnmappedType
          newUpdateReplacingRange(
            final ArrayBufferType array,
            final RangeInclusiveL range)
            throws RangeCheckException,
              JCGLExceptionDeleted
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }
      };

    final IndexBufferUpdateUnmappedConstructorType iu_cons =
      new IndexBufferUpdateUnmappedConstructorType() {
        @Override public IndexBufferUpdateUnmappedType newReplacing(
          final IndexBufferType b)
        {
          return new IndexBufferUpdateUnmappedType() {
            @Override public CursorWritableIndexType getCursor()
            {
              return new CursorWritableIndexType() {

                long current  = 0;
                long max      = b.bufferGetRange().getUpper();
                long v0       = -1;
                long v1       = -1;
                long v2       = -1;
                int  vertices = 0;

                @Override public boolean hasNext()
                {
                  return this.current < this.max;
                }

                @Override public boolean isValid()
                {
                  return this.current <= this.max;
                }

                @Override public void next()
                {
                  this.current = this.current + 1;
                }

                @Override public void putIndex(
                  final long index)
                {
                  switch (this.vertices) {
                    case 0:
                    {
                      this.vertices = this.vertices + 1;
                      this.v0 = index + 1;
                      break;
                    }
                    case 1:
                    {
                      this.vertices = this.vertices + 1;
                      this.v1 = index + 1;
                      break;
                    }
                    case 2:
                    {
                      this.vertices = this.vertices + 1;
                      this.v2 = index + 1;
                      break;
                    }
                  }

                  if (this.vertices == 3) {
                    System.out.printf(
                      "f %d %d %d\n",
                      this.v0,
                      this.v1,
                      this.v2);
                    this.vertices = 0;
                    this.v0 = -1;
                    this.v1 = -1;
                    this.v2 = -1;
                  }

                  this.next();
                }

                @Override public void seekTo(
                  final long index)
                {
                  this.current = index;
                }
              };
            }

            @Override public IndexBufferUsableType getIndexBuffer()
            {
              return b;
            }

            @Override public ByteBuffer getTargetData()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public long getTargetDataOffset()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }

            @Override public long getTargetDataSize()
            {
              // TODO Auto-generated method stub
              throw new UnimplementedCodeException();
            }
          };
        }

        @Override public IndexBufferUpdateUnmappedType newUpdating(
          final IndexBufferType b,
          final RangeInclusiveL r)
          throws RangeCheckException
        {
          // TODO Auto-generated method stub
          throw new UnimplementedCodeException();
        }
      };

    System.out.printf("o Test\n");
    KFrustumMesh.newFromFOV(g.getGLCommon(), au_cons, iu_cons, p);
  }

  @Test public void testFOV()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final KProjectionFOV p =
      KProjectionFOV.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        (float) Math.toRadians(90.0f),
        1.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m =
      KFrustumMesh.newFromFOV(
        g.getGLCommon(),
        ArrayBufferUpdateUnmapped.newConstructor(),
        IndexBufferUpdateUnmapped.newConstructor(),
        p);

    Assert.assertEquals(-1.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-10.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }

  @Test public void testFrustum()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final KProjectionFrustum p =
      KProjectionFrustum.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        -1.0f,
        1.0f,
        -2.0f,
        2.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m =
      KFrustumMesh.newFromFrustum(
        g.getGLCommon(),
        ArrayBufferUpdateUnmapped.newConstructor(),
        IndexBufferUpdateUnmapped.newConstructor(),
        p);

    Assert.assertEquals(-1.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(1.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-2.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-10.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(10.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-20.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(20.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }

  @Test public void testOrthographic()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);

    final KProjectionOrthographic p =
      KProjectionOrthographic.newProjection(
        new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
        -2.0f,
        2.0f,
        -3.0f,
        3.0f,
        1.0f,
        10.0f);
    final KFrustumMesh m =
      KFrustumMesh.newFromOrthographic(
        g.getGLCommon(),
        ArrayBufferUpdateUnmapped.newConstructor(),
        IndexBufferUpdateUnmapped.newConstructor(),
        p);

    Assert.assertEquals(-2.0f, m.getNearXMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getNearXMaximum(), 0.0f);
    Assert.assertEquals(-3.0f, m.getNearYMinimum(), 0.0f);
    Assert.assertEquals(3.0f, m.getNearYMaximum(), 0.0f);
    Assert.assertEquals(-1.0f, m.getNearZ(), 0.0f);

    Assert.assertEquals(-2.0f, m.getFarXMinimum(), 0.0f);
    Assert.assertEquals(2.0f, m.getFarXMaximum(), 0.0f);
    Assert.assertEquals(-3.0f, m.getFarYMinimum(), 0.0f);
    Assert.assertEquals(3.0f, m.getFarYMaximum(), 0.0f);
    Assert.assertEquals(-10.0f, m.getFarZ(), 0.0f);

    final IndexBufferUsableType mi = m.getIndices();
    final ArrayBufferUsableType ma = m.getArray();
    Assert.assertEquals(
      KFrustumMesh.getFrustumMeshSizeBytes(),
      ma.resourceGetSizeBytes() + mi.resourceGetSizeBytes());
  }
}
