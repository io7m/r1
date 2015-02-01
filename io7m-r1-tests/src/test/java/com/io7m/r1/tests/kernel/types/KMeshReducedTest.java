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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.ArrayDescriptorBuilderType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionMeshMissingPositions;
import com.io7m.r1.exceptions.RExceptionMeshMissingUVs;
import com.io7m.r1.kernel.types.KMeshAttributes;
import com.io7m.r1.kernel.types.KMeshReduced;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;

@SuppressWarnings("static-method") public final class KMeshReducedTest
{
  @Test public void testCorrect()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLInterfaceCommonType gc =
      RFakeGL
        .newFakeGL30(RFakeShaderControllers.newNull(), none)
        .getGLCommon();

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      gc.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        1,
        UsageHint.USAGE_STATIC_DRAW);

    final KMeshReduced k = KMeshReduced.newMesh(array, indices);
    Assert.assertEquals(array, k.meshGetArrayBuffer());
    Assert.assertEquals(indices, k.meshGetIndexBuffer());
    Assert.assertFalse(k.resourceIsDeleted());

    k.delete(gc);

    Assert.assertTrue(k.resourceIsDeleted());
  }

  @Test(expected = RExceptionMeshMissingPositions.class) public
    void
    testMissingPositions()
      throws JCGLException,
        RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLInterfaceCommonType gc =
      RFakeGL
        .newFakeGL30(RFakeShaderControllers.newNull(), none)
        .getGLCommon();

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_TANGENT4);
    b.addAttribute(KMeshAttributes.ATTRIBUTE_UV);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      gc.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        1,
        UsageHint.USAGE_STATIC_DRAW);

    KMeshReduced.newMesh(array, indices);
  }

  @Test(expected = RExceptionMeshMissingUVs.class) public
    void
    testMissingUVs()
      throws JCGLException,
        RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLInterfaceCommonType gc =
      RFakeGL
        .newFakeGL30(RFakeShaderControllers.newNull(), none)
        .getGLCommon();

    final ArrayDescriptorBuilderType b = ArrayDescriptor.newBuilder();
    b.addAttribute(KMeshAttributes.ATTRIBUTE_POSITION);

    final ArrayDescriptor type = b.build();
    final ArrayBufferType array =
      gc.arrayBufferAllocate(1, type, UsageHint.USAGE_STATIC_DRAW);
    final IndexBufferType indices =
      gc.indexBufferAllocateType(
        JCGLUnsignedType.TYPE_UNSIGNED_INT,
        1,
        UsageHint.USAGE_STATIC_DRAW);

    KMeshReduced.newMesh(array, indices);
  }
}
