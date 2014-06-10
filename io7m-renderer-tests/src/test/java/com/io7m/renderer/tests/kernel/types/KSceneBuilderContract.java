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
import com.io7m.jtensors.MatrixM4x4F;
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
import com.io7m.renderer.kernel.types.KProjection;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneBuilderWithCreateType;
import com.io7m.renderer.kernel.types.KSceneLightGroup;
import com.io7m.renderer.kernel.types.KSceneLightGroupBuilderType;
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
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.renderer.types.RExceptionLightGroupLacksInstances;
import com.io7m.renderer.types.RExceptionLightGroupLacksLights;
import com.io7m.renderer.types.RExceptionLightMissingShadow;

@SuppressWarnings({ "unchecked", "null" }) public abstract class KSceneBuilderContract
{
  private static KLightProjective getShadowLight()
  {
    try {
      final FakeCapabilities caps = new FakeCapabilities();
      final KProjectionType projection =
        KProjection.fromFrustumWithContext(
          new MatrixM4x4F(),
          -1.0f,
          1.0f,
          -1.0f,
          1.0f,
          1.0f,
          100.0f);

      final KLightProjectiveBuilderType b =
        KLightProjective.newBuilder(
          FakeTexture2DStatic.getDefault(),
          projection);
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

    b.sceneAddShadowCaster((KLightType) TestUtilities.actuallyNull(), i);
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullInvisibleWithShadow_1()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KLightType l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddShadowCaster(
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

  @Test(expected = RExceptionLightMissingShadow.class) public
    void
    testSceneAddShadowCaster_NoShadow0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddShadowCaster(l0, o);
  }

  @Test(expected = RExceptionLightMissingShadow.class) public
    void
    testSceneAddShadowCaster_WithoutShadow0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();

    b.sceneAddShadowCaster(l0, o0);
  }

  @Test public void testSceneAddShadowCaster_0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddShadowCaster(l0, o0);
    final KScene s = b.sceneCreate();

    final Map<KLightType, Set<KInstanceOpaqueType>> casters =
      s.getShadows().getShadowCasters();
    Assert.assertEquals(1, casters.size());
    Assert.assertTrue(casters.containsKey(l0));
    Assert.assertEquals(o0, casters.get(l0).iterator().next());
  }

  @Test(expected = NullCheckException.class) public
    void
    testNullLightGroup_0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    b.sceneNewLightGroup((String) TestUtilities.actuallyNull());
  }

  @Test(expected = RExceptionLightGroupLacksLights.class) public
    void
    testSceneLightGroup_NoLights0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KSceneLightGroupBuilderType gb = b.sceneNewLightGroup("g");
    final KInstanceOpaqueType o0 = this.getOpaque();
    gb.groupAddInstance(o0);
    b.sceneCreate();
  }

  @Test(expected = RExceptionLightGroupLacksInstances.class) public
    void
    testSceneLightGroup_NoInstances0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KSceneLightGroupBuilderType gb = b.sceneNewLightGroup("g");
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();
    gb.groupAddLight(l0);
    b.sceneCreate();
  }

  @Test(expected = RExceptionInstanceAlreadyUnlit.class) public
    void
    testSceneLightGroup_AlreadyUnlit0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();
    b.sceneAddOpaqueUnlit(o);

    final KSceneLightGroupBuilderType gb = b.sceneNewLightGroup("g");
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();
    gb.groupAddLight(l0);
    gb.groupAddInstance(o);
  }

  @Test(expected = RExceptionInstanceAlreadyLit.class) public
    void
    testSceneLightGroup_AlreadyLit0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o = this.getOpaque();

    final KSceneLightGroupBuilderType gb = b.sceneNewLightGroup("g");
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();
    gb.groupAddLight(l0);
    gb.groupAddInstance(o);

    b.sceneAddOpaqueUnlit(o);
  }

  @Test(expected = RExceptionLightGroupAlreadyAdded.class) public
    void
    testSceneLightGroup_Duplicate0()
      throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    b.sceneNewLightGroup("g");
    b.sceneNewLightGroup("g");
  }

  @Test public void testSceneLightGroup_Lights0()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KSceneLightGroupBuilderType gb = b.sceneNewLightGroup("g");
    final KLightSphere l0 = KSceneBuilderContract.getSphericalLight();
    gb.groupAddLight(l0);
    final KLightType l1 = KSceneBuilderContract.getShadowLight();
    gb.groupAddLight(l1);
    final KInstanceOpaqueType o = this.getOpaque();
    gb.groupAddInstance(o);

    final KScene s = b.sceneCreate();
    final Map<String, KSceneLightGroup> gs = s.getLightGroups();
    Assert.assertEquals(1, gs.size());

    final KSceneLightGroup g = gs.get("g");
    Assert.assertEquals(2, g.getLights().size());
    Assert.assertTrue(g.getLights().contains(l0));
    Assert.assertTrue(g.getLights().contains(l1));
    Assert.assertEquals(1, g.getInstances().size());
    Assert.assertTrue(g.getInstances().contains(o));
  }

  @Test public void testSceneAddShadowCaster_1()
    throws RException
  {
    final KSceneBuilderWithCreateType b = this.newBuilder();
    final KInstanceOpaqueType o0 = this.getOpaque();
    final KInstanceOpaqueType o1 = this.getOpaque();
    final KLightProjective l0 = KSceneBuilderContract.getShadowLight();

    b.sceneAddShadowCaster(l0, o0);
    b.sceneAddShadowCaster(l0, o1);
    final KScene s = b.sceneCreate();

    final Map<KLightType, Set<KInstanceOpaqueType>> casters =
      s.getShadows().getShadowCasters();
    Assert.assertEquals(1, casters.size());
    Assert.assertTrue(casters.containsKey(l0));

    final Set<KInstanceOpaqueType> bl = casters.get(l0);
    Assert.assertEquals(2, bl.size());
    Assert.assertTrue(bl.contains(o0));
    Assert.assertTrue(bl.contains(o1));
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
}
