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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheckException;
import com.io7m.renderer.kernel.types.KInstanceBuilderType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueRegular;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentLitType;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightSphereBuilderType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.tests.utilities.TestUtilities;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionInstancesNotDistinct;
import com.io7m.renderer.types.RExceptionLightsNotDistinct;

@SuppressWarnings("unchecked") public abstract class KSceneBuilderContract
{
  private static @NonNull KLightSphere getSphericalLight()
  {
    return KLightSphere.newBuilder().build();
  }

  protected abstract @NonNull KInstanceOpaqueRegular getTransformedOpaque(
    int i);

  protected abstract @NonNull
    KInstanceTranslucentLitType
    getTransformedTranslucent(
      int i);

  protected abstract @NonNull KSceneBuilderWithCreateType newBuilder();

  @Test(expected = NullCheckException.class) public
    void
    testNullInvisibleWithShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType i = this.getTransformedOpaque(0);

    b.sceneAddInvisibleWithShadow(
      (KLightType) TestUtilities.actuallyNull(),
      i);
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullInvisibleWithShadow_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KLightType l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddInvisibleWithShadow(
      l0,
      (KInstanceOpaqueType) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullOpaqueLitVisibleWithoutShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType i = this.getTransformedOpaque(0);

    b.sceneAddOpaqueLitVisibleWithoutShadow(
      (KLightType) TestUtilities.actuallyNull(),
      i);
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullOpaqueLitVisibleWithoutShadow_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KLightType l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddOpaqueLitVisibleWithoutShadow(
      l0,
      (KInstanceOpaqueType) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullOpaqueLitVisibleWithShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType i = this.getTransformedOpaque(0);

    b.sceneAddOpaqueLitVisibleWithShadow(
      (KLightType) TestUtilities.actuallyNull(),
      i);
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullOpaqueLitVisibleWithShadow_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KLightType l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddOpaqueLitVisibleWithShadow(
      l0,
      (KInstanceOpaqueType) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullOpaqueUnlit_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();

    b.sceneAddOpaqueUnlit((KInstanceOpaqueType) TestUtilities.actuallyNull());
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullTranslucentLit_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final HashSet<KLightType> s = new HashSet<KLightType>();

    b.sceneAddTranslucentLit(
      (KInstanceTranslucentLitType) TestUtilities.actuallyNull(),
      s);
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullTranslucentLit_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceTranslucentLitType i = this.getTransformedTranslucent(0);

    b.sceneAddTranslucentLit(
      i,
      (Set<KLightType>) TestUtilities.actuallyNull());
  }

  @Test(expected = RExceptionLightsNotDistinct.class) public
    void
    testLightNotDistinctInvisibleWithShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType i = this.getTransformedOpaque(0);

    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();
    final KLightSphereBuilderType lb = KLightSphere.newBuilderWithSameID(l0);
    final KLightSphere l1 = lb.build();

    Assert.assertEquals(l0.lightGetID(), l1.lightGetID());

    b.sceneAddInvisibleWithShadow(l0, i);
    b.sceneAddInvisibleWithShadow(l1, i);
  }

  @Test(expected = RExceptionInstancesNotDistinct.class) public
    void
    testInstanceNotDistinctInvisibleWithShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueRegular i = this.getTransformedOpaque(0);
    final KInstanceBuilderType<KInstanceOpaqueRegular> ib =
      KInstanceOpaqueRegular.newBuilderWithSameID(i);

    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddInvisibleWithShadow(l0, i);
    b.sceneAddInvisibleWithShadow(l0, i);
  }
}
