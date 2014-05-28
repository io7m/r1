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

package com.io7m.renderer.tests.kernel.types;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionDeleted;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.kernel.types.KMeshAttributes;
import com.io7m.renderer.tests.FakeArrayBuffer;
import com.io7m.renderer.tests.FakeIndexBuffer;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionMeshMissingNormals;
import com.io7m.renderer.types.RExceptionMeshMissingPositions;
import com.io7m.renderer.types.RExceptionMeshMissingTangents;
import com.io7m.renderer.types.RExceptionMeshMissingUVs;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;

@SuppressWarnings("static-method") public final class KMeshTest
{
  private static final class FakeGL implements
    JCGLArrayBuffersType,
    JCGLIndexBuffersType
  {
    public FakeGL()
    {
      // Nothing.
    }

    @Override public ArrayBufferType arrayBufferAllocate(
      final long elements,
      final ArrayDescriptor descriptor,
      final UsageHint usage)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void arrayBufferBind(
      final ArrayBufferUsableType buffer)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void arrayBufferDelete(
      final ArrayBufferType id)
      throws JCGLException,
        JCGLExceptionDeleted
    {
      // Nothing.
    }

    @Override public boolean arrayBufferIsBound(
      final ArrayBufferUsableType id)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void arrayBufferUnbind()
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void arrayBufferUpdate(
      final ArrayBufferUpdateUnmappedType data)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public IndexBufferType indexBufferAllocate(
      final ArrayBufferUsableType buffer,
      final int indices,
      final UsageHint usage)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public IndexBufferType indexBufferAllocateType(
      final JCGLUnsignedType type,
      final int indices,
      final UsageHint usage)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void indexBufferDelete(
      final IndexBufferType id)
      throws JCGLException
    {
      // Nothing.
    }

    @Override public void indexBufferUpdate(
      final IndexBufferUpdateUnmappedType data)
      throws JCGLException
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }
  }

  @Test public void testCorrect()
    throws JCGLException,
      RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      new FakeArrayBuffer(
        0,
        new RangeInclusiveL(0, 99),
        type,
        UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      new FakeIndexBuffer(
        0,
        new RangeInclusiveL(0, 99),
        UsageHint.USAGE_STATIC_DRAW,
        JCGLUnsignedType.TYPE_UNSIGNED_INT);
    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();

    final KMesh k = KMesh.newMesh(array, indices, lower, upper);
    Assert.assertEquals(array, k.meshGetArrayBuffer());
    Assert.assertEquals(indices, k.meshGetIndexBuffer());
    Assert.assertEquals(lower, k.meshGetBoundsLower());
    Assert.assertEquals(upper, k.meshGetBoundsUpper());
    Assert.assertFalse(k.resourceIsDeleted());

    final FakeGL gc = new FakeGL();
    k.delete(gc);

    Assert.assertTrue(k.resourceIsDeleted());
  }

  @Test(expected = RExceptionMeshMissingNormals.class) public
    void
    testMissingNormals()
      throws JCGLException,
        RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      new FakeArrayBuffer(
        0,
        new RangeInclusiveL(0, 99),
        type,
        UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      new FakeIndexBuffer(
        0,
        new RangeInclusiveL(0, 99),
        UsageHint.USAGE_STATIC_DRAW,
        JCGLUnsignedType.TYPE_UNSIGNED_INT);
    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();

    KMesh.newMesh(array, indices, lower, upper);
  }

  @Test(expected = RExceptionMeshMissingPositions.class) public
    void
    testMissingPositions()
      throws JCGLException,
        RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      new FakeArrayBuffer(
        0,
        new RangeInclusiveL(0, 99),
        type,
        UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      new FakeIndexBuffer(
        0,
        new RangeInclusiveL(0, 99),
        UsageHint.USAGE_STATIC_DRAW,
        JCGLUnsignedType.TYPE_UNSIGNED_INT);
    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();

    KMesh.newMesh(array, indices, lower, upper);
  }

  @Test(expected = RExceptionMeshMissingTangents.class) public
    void
    testMissingTangents()
      throws JCGLException,
        RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      new FakeArrayBuffer(
        0,
        new RangeInclusiveL(0, 99),
        type,
        UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      new FakeIndexBuffer(
        0,
        new RangeInclusiveL(0, 99),
        UsageHint.USAGE_STATIC_DRAW,
        JCGLUnsignedType.TYPE_UNSIGNED_INT);
    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();

    KMesh.newMesh(array, indices, lower, upper);
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public
    void
    testMissingUVs()
      throws JCGLException,
        RException
  {
    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_NORMAL);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      new FakeArrayBuffer(
        0,
        new RangeInclusiveL(0, 99),
        type,
        UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      new FakeIndexBuffer(
        0,
        new RangeInclusiveL(0, 99),
        UsageHint.USAGE_STATIC_DRAW,
        JCGLUnsignedType.TYPE_UNSIGNED_INT);
    final RVectorI3F<RSpaceObjectType> lower = RVectorI3F.zero();
    final RVectorI3F<RSpaceObjectType> upper = RVectorI3F.zero();

    KMesh.newMesh(array, indices, lower, upper);
  }
}
