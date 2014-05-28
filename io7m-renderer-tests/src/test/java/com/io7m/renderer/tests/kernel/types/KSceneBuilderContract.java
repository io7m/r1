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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheckException;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KDepthPrecision;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentLitType;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRefractive;
import com.io7m.renderer.kernel.types.KInstanceTranslucentRegular;
import com.io7m.renderer.kernel.types.KInstanceTranslucentUnlitType;
import com.io7m.renderer.kernel.types.KInstanceType;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KSceneOpaques;
import com.io7m.renderer.kernel.types.KSceneShadows;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KTranslucentRegularLit;
import com.io7m.renderer.kernel.types.KTranslucentSpecularOnlyLit;
import com.io7m.renderer.kernel.types.KTranslucentType;
import com.io7m.renderer.kernel.types.KTranslucentVisitorType;
import com.io7m.renderer.tests.FakeCapabilities;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.utilities.TestUtilities;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyShadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnshadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyVisible;
import com.io7m.renderer.types.RExceptionLightMissingShadow;

@SuppressWarnings({ "unchecked", "null" }) public abstract class KSceneBuilderContract
{
  private static KLightProjective getShadowLight()
  {
    try {
      final FakeCapabilities caps = new FakeCapabilities();
      final KLightProjectiveBuilderType b = KLightProjective.newBuilder();
      b.setTexture(FakeTexture2DStatic.getDefault());
      final RangeInclusiveL r = new RangeInclusiveL(0, 99);
      final KFramebufferDepthDescription fbd =
        KFramebufferDepthDescription.newDescription(
          new AreaInclusive(r, r),
          TextureFilterMagnification.TEXTURE_FILTER_NEAREST,
          TextureFilterMinification.TEXTURE_FILTER_NEAREST,
          KDepthPrecision.DEPTH_PRECISION_16);
      final KShadowMapBasicDescription description =
        KShadowMapBasicDescription.newDescription(fbd, 2);
      b.setShadow(KShadowMappedBasic.newMappedBasic(0.1f, 0.2f, description));
      return b.build(caps);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static @NonNull KLightSphere getSphericalLight()
  {
    return KLightSphere.newBuilder().build();
  }

  protected abstract @NonNull KInstanceOpaqueType getOpaque();

  protected abstract @NonNull KInstanceTranslucentRegular getTranslucent();

  protected abstract KInstanceTranslucentUnlitType getTranslucentUnlit();

  protected abstract @NonNull KSceneBuilderWithCreateType newBuilder();

  @Test(expected = NullCheckException.class) public
    void
    testNullInvisibleWithShadow_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType i = this.getOpaque();

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
    final KInstanceOpaqueType i = this.getOpaque();

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
    final KInstanceOpaqueType i = this.getOpaque();

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
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceTranslucentLitType i = this.getTranslucent();

    b.sceneAddTranslucentLit(
      i,
      (Set<KLightType>) TestUtilities.actuallyNull());
  }

  @Test(expected = RExceptionInstanceAlreadyVisible.class) public
    void
    testSceneAddInvisibleWithShadow_AlreadyShadowed0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddInvisibleWithShadow(l0, o0);
  }

  @Test(expected = RExceptionLightMissingShadow.class) public
    void
    testSceneAddInvisibleWithShadow_NoShadow0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddInvisibleWithShadow(l0, o);
  }

  @Test public void testSceneAddInvisibleWithShadow_NotShadowed0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
    b.sceneAddInvisibleWithShadow(l0, o1);

    {
      final Set<KInstanceType> m = b.sceneGetInstances();
      Assert.assertEquals(2, m.size());
      Assert.assertTrue(m.contains(o0));
      Assert.assertTrue(m.contains(o1));
    }

    {
      final Set<KInstanceOpaqueType> m =
        b.sceneGetInstancesOpaqueLitVisible();
      Assert.assertEquals(1, m.size());
      Assert.assertTrue(m.contains(o0));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> m =
        b.sceneGetInstancesOpaqueLitVisibleByLight();
      final Set<KInstanceOpaqueType> os = m.get(l0);
      Assert.assertEquals(1, os.size());
      Assert.assertTrue(os.contains(o0));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> m =
        b.sceneGetInstancesOpaqueShadowCastingByLight();
      final Set<KInstanceOpaqueType> os = m.get(l0);
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }
  }

  @Test public void testSceneAddInvisibleWithShadow_Shadow0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddInvisibleWithShadow(l0, o);

    {
      final Set<KInstanceType> os = b.sceneGetInstances();
      Assert.assertEquals(1, os.size());
      Assert.assertTrue(os.contains(o));
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueLitVisible().isEmpty());
    }

    {
      Assert.assertTrue(b
        .sceneGetInstancesOpaqueLitVisibleByLight()
        .isEmpty());
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueUnlit().isEmpty());
    }

    {
      final Set<KLightType> ls = b.sceneGetLightsShadowCasting();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }
  }

  @Test public void testSceneAddInvisibleWithShadow_Shadow1()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddInvisibleWithShadow(l0, o);
    b.sceneAddInvisibleWithShadow(l0, o);

    {
      final Set<KInstanceType> os = b.sceneGetInstances();
      Assert.assertEquals(1, os.size());
      Assert.assertTrue(os.contains(o));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> os =
        b.sceneGetInstancesOpaqueShadowCastingByLight();
      Assert.assertEquals(1, os.size());

      {
        final Set<KInstanceOpaqueType> is = os.get(l0);
        Assert.assertEquals(1, is.size());
        Assert.assertTrue(is.contains(o));
      }
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueLitVisible().isEmpty());
    }

    {
      Assert.assertTrue(b
        .sceneGetInstancesOpaqueLitVisibleByLight()
        .isEmpty());
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueUnlit().isEmpty());
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }

    {
      final Set<KLightType> ls = b.sceneGetLightsShadowCasting();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }
  }

  @Test public void testSceneAddInvisibleWithShadow_Shadow2()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddInvisibleWithShadow(l0, o0);
    b.sceneAddInvisibleWithShadow(l0, o1);

    {
      final Set<KInstanceType> os = b.sceneGetInstances();
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> os =
        b.sceneGetInstancesOpaqueShadowCastingByLight();
      Assert.assertEquals(1, os.size());

      {
        final Set<KInstanceOpaqueType> is = os.get(l0);
        Assert.assertEquals(2, is.size());
        Assert.assertTrue(is.contains(o0));
        Assert.assertTrue(is.contains(o1));
      }
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueLitVisible().isEmpty());
    }

    {
      Assert.assertTrue(b
        .sceneGetInstancesOpaqueLitVisibleByLight()
        .isEmpty());
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueUnlit().isEmpty());
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }

    {
      final Set<KLightType> ls = b.sceneGetLightsShadowCasting();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }
  }

  @Test(expected = RExceptionLightMissingShadow.class) public
    void
    testSceneAddInvisibleWithShadow_WithoutShadow0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddInvisibleWithShadow(l0, o0);
  }

  @Test public void testSceneAddTranslucentLit_0()
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceTranslucentLitType t = this.getTranslucent();

    final Set<KLightType> lights = new HashSet<KLightType>();
    lights.add(KSceneBuilderContract.getSphericalLight());
    lights.add(KSceneBuilderContract.getSphericalLight());
    lights.add(KSceneBuilderContract.getSphericalLight());

    b.sceneAddTranslucentLit(t, lights);

    final List<KTranslucentType> ts = b.sceneGetTranslucents();
    Assert.assertEquals(1, ts.size());

    final KTranslucentType kt = ts.get(0);
    final KTranslucentVisitorType<Unit, UnreachableCodeException> v =
      new KTranslucentVisitorType<Unit, UnreachableCodeException>() {
        @Override public Unit refractive(
          final KInstanceTranslucentRefractive tt)
          throws RException
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit regularLit(
          final KTranslucentRegularLit tt)
          throws RException
        {
          Assert.assertEquals(tt.translucentGetInstance(), t);
          Assert.assertEquals(lights, tt.translucentGetLights());
          return Unit.unit();
        }

        @Override public Unit regularUnlit(
          final KInstanceTranslucentRegular tt)
          throws RException
        {
          throw new UnreachableCodeException();
        }

        @Override public Unit specularOnly(
          final KTranslucentSpecularOnlyLit tt)
          throws RException
        {
          throw new UnreachableCodeException();
        }
      };

    try {
      kt.translucentAccept(v);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }

    {
      final Set<KInstanceType> is = b.sceneGetInstances();
      Assert.assertEquals(1, is.size());
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(3, ls.size());
    }
  }

  @Test public void testSceneAddTranslucentUnlit_0()
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceTranslucentUnlitType t = this.getTranslucentUnlit();

    b.sceneAddTranslucentUnlit(t);
    b.sceneAddTranslucentUnlit(t);
    b.sceneAddTranslucentUnlit(t);

    final List<KTranslucentType> ts = b.sceneGetTranslucents();
    Assert.assertEquals(3, ts.size());
    Assert.assertEquals(ts.get(0), t);
    Assert.assertEquals(ts.get(1), t);
    Assert.assertEquals(ts.get(2), t);

    {
      final Set<KInstanceType> is = b.sceneGetInstances();
      Assert.assertEquals(1, is.size());
    }
  }

  @Test(expected = NullCheckException.class) public
    void
    testSceneAddTranslucentUnlit_Null0()
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();

    b.sceneAddTranslucentUnlit((KInstanceTranslucentUnlitType) TestUtilities
      .actuallyNull());
  }

  @Test public void testSceneOpaqueLitVisibleWithoutShadow_0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
    b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o1);

    {
      final Set<KInstanceType> os = b.sceneGetInstances();
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> os =
        b.sceneGetInstancesOpaqueShadowCastingByLight();
      Assert.assertTrue(os.isEmpty());
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> lo =
        b.sceneGetInstancesOpaqueLitVisibleByLight();
      Assert.assertEquals(1, lo.size());

      final Set<KInstanceOpaqueType> is = lo.get(l0);
      Assert.assertEquals(2, is.size());
      Assert.assertTrue(is.contains(o0));
      Assert.assertTrue(is.contains(o1));
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueUnlit().isEmpty());
    }

    {
      final Set<KInstanceOpaqueType> os =
        b.sceneGetInstancesOpaqueLitVisible();
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }

    {
      final Set<KLightType> ls = b.sceneGetLightsShadowCasting();
      Assert.assertTrue(ls.isEmpty());
    }
  }

  @Test(expected = RExceptionInstanceAlreadyUnlit.class) public
    void
    testSceneOpaqueLitVisibleWithoutShadow_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueUnlit(o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
  }

  @Test public void testSceneOpaqueLitVisibleWithShadow_0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o1);

    {
      final Set<KInstanceType> os = b.sceneGetInstances();
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> os =
        b.sceneGetInstancesOpaqueShadowCastingByLight();
      Assert.assertEquals(1, os.size());

      final Set<KInstanceOpaqueType> is = os.get(l0);
      Assert.assertEquals(2, is.size());
      Assert.assertTrue(is.contains(o0));
      Assert.assertTrue(is.contains(o1));
    }

    {
      final Map<KLightType, Set<KInstanceOpaqueType>> lo =
        b.sceneGetInstancesOpaqueLitVisibleByLight();
      Assert.assertEquals(1, lo.size());

      final Set<KInstanceOpaqueType> is = lo.get(l0);
      Assert.assertEquals(2, is.size());
      Assert.assertTrue(is.contains(o0));
      Assert.assertTrue(is.contains(o1));
    }

    {
      Assert.assertTrue(b.sceneGetInstancesOpaqueUnlit().isEmpty());
    }

    {
      final Set<KInstanceOpaqueType> os =
        b.sceneGetInstancesOpaqueLitVisible();
      Assert.assertEquals(2, os.size());
      Assert.assertTrue(os.contains(o0));
      Assert.assertTrue(os.contains(o1));
    }

    {
      final Set<KLightType> ls = b.sceneGetLights();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }

    {
      final Set<KLightType> ls = b.sceneGetLightsShadowCasting();
      Assert.assertEquals(1, ls.size());
      Assert.assertTrue(ls.contains(l0));
    }
  }

  @Test(expected = RExceptionInstanceAlreadyShadowed.class) public
    void
    testSceneOpaqueLitVisibleWithShadowAlreadyShadowed_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
  }

  @Test(expected = RExceptionInstanceAlreadyUnlit.class) public
    void
    testSceneOpaqueLitVisibleWithShadowAlreadyUnlit_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueUnlit(o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
  }

  @Test(expected = RExceptionInstanceAlreadyUnshadowed.class) public
    void
    testSceneOpaqueLitVisibleWithShadowAlreadyUnshadowed_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
  }

  @Test(expected = RExceptionInstanceAlreadyUnshadowed.class) public
    void
    testSceneOpaqueLitVisibleWithShadowAlreadyUnshadowed_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
      b.sceneAddOpaqueLitVisibleWithShadow(l0, o1);
    } catch (final Exception x) {
      throw new UnreachableCodeException(x);
    }
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
  }

  @Test(expected = RExceptionLightMissingShadow.class) public
    void
    testSceneOpaqueLitVisibleWithShadowMissing_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddOpaqueLitVisibleWithShadow(l0, o0);
  }

  @Test(expected = RExceptionInstanceAlreadyLit.class) public
    void
    testSceneOpaqueUnlit_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    try {
      b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
    } catch (final Exception e) {
      throw new UnreachableCodeException(e);
    }
    b.sceneAddOpaqueUnlit(o0);
  }

  @Test public void testSceneBuild_0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();

    final KInstanceOpaqueType o0_shadow = this.getOpaque();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KInstanceOpaqueType o2 = this.getOpaque();
    final KInstanceOpaqueType o3 = this.getOpaque();
    final KInstanceTranslucentUnlitType t0 = this.getTranslucentUnlit();
    final KInstanceTranslucentLitType t1 = this.getTranslucent();

    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();
    final KLightSphere l1 = KSceneBuilderContract.getSphericalLight();
    final KLightSphere l2 = KSceneBuilderContract.getSphericalLight();
    final Set<KLightType> lights = new HashSet<KLightType>();
    lights.add(l0);
    lights.add(l1);
    lights.add(l2);

    b.sceneAddInvisibleWithShadow(l0, o0_shadow);
    b.sceneAddOpaqueLitVisibleWithoutShadow(l0, o0);
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o1);
    b.sceneAddOpaqueLitVisibleWithShadow(l0, o2);
    b.sceneAddOpaqueLitVisibleWithoutShadow(l1, o0);
    b.sceneAddOpaqueLitVisibleWithoutShadow(l1, o1);
    b.sceneAddOpaqueUnlit(o3);
    b.sceneAddTranslucentUnlit(t0);
    b.sceneAddTranslucentLit(t1, lights);

    final KScene s = b.sceneCreate();
    Assert.assertEquals(s.getCamera(), b.sceneGetCamera());

    {
      final KSceneOpaques so = s.getOpaques();
      final Set<KInstanceOpaqueType> uli = so.getUnlitInstances();
      Assert.assertEquals(1, uli.size());
      Assert.assertTrue(uli.contains(o3));

      final Map<KLightType, Set<KInstanceOpaqueType>> li =
        so.getLitInstances();
      Assert.assertEquals(2, li.size());

      final Set<KInstanceOpaqueType> l0i = li.get(l0);
      Assert.assertEquals(3, l0i.size());
      Assert.assertTrue(l0i.contains(o0));
      Assert.assertTrue(l0i.contains(o1));
      Assert.assertTrue(l0i.contains(o2));

      final Set<KInstanceOpaqueType> l1i = li.get(l1);
      Assert.assertEquals(2, l1i.size());
      Assert.assertTrue(l1i.contains(o0));
      Assert.assertTrue(l1i.contains(o1));
    }

    {
      final KSceneShadows ss = s.getShadows();
      final Map<KLightType, Set<KInstanceOpaqueType>> sc =
        ss.getShadowCasters();

      Assert.assertEquals(1, sc.size());
      final Set<KInstanceOpaqueType> l0i = sc.get(l0);
      Assert.assertEquals(3, l0i.size());
      Assert.assertTrue(l0i.contains(o0_shadow));
      Assert.assertTrue(l0i.contains(o1));
      Assert.assertTrue(l0i.contains(o2));
    }

    {
      final Set<KInstanceType> sv = s.getVisibleInstances();
      Assert.assertEquals(6, sv.size());
      Assert.assertTrue(sv.contains(o0));
      Assert.assertTrue(sv.contains(o1));
      Assert.assertTrue(sv.contains(o2));
      Assert.assertTrue(sv.contains(o3));
      Assert.assertTrue(sv.contains(t0));
      Assert.assertTrue(sv.contains(t1));
    }

    {
      final List<KTranslucentType> kt = s.getTranslucents();
      Assert.assertEquals(2, kt.size());

      final KInstanceTranslucentUnlitType tt0 =
        (KInstanceTranslucentUnlitType) kt.get(0);
      Assert.assertEquals(tt0, t0);

      final KTranslucentRegularLit tt1 = (KTranslucentRegularLit) kt.get(1);
      final KInstanceTranslucentRegular tt1_r = tt1.translucentGetInstance();
      Assert.assertEquals(tt1_r, t1);
      Assert.assertEquals(lights, tt1.translucentGetLights());
    }
  }
}
