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

package com.io7m.r1.tests.kernel;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorNotCurrent;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorOutOfUnits;
import com.io7m.r1.kernel.KTextureBindingsContextType;
import com.io7m.r1.kernel.KTextureBindingsController;
import com.io7m.r1.kernel.KTextureBindingsControllerType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;

@SuppressWarnings({ "null", "static-method" }) public final class KTextureBindingsControllerTest
{
  @Test public void testBindingsNew_0()
    throws Exception
  {
    final JCGLSoftRestrictionsType r = new JCGLSoftRestrictionsType() {
      @Override public int restrictTextureUnitCount(
        final int count)
      {
        return 4;
      }

      @Override public boolean restrictExtensionVisibility(
        final String name)
      {
        return true;
      }
    };
    final OptionType<JCGLSoftRestrictionsType> some = Option.some(r);

    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), some);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureBindingsControllerType bc =
      KTextureBindingsController.newBindings(gc);

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    final Texture2DStaticType t1 = RFakeTextures2DStatic.newWithName(g, "t1");
    final Texture2DStaticType t2 = RFakeTextures2DStatic.newWithName(g, "t2");
    final Texture2DStaticType t3 = RFakeTextures2DStatic.newWithName(g, "t3");
    final TextureCubeStaticType tc0 = RFakeTexturesCubeStatic.newAnything(g);

    Assert.assertEquals(4, units.size());

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

    bc
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c0)
          throws RException
        {
          Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

          c0.withTexture2D(t0);
          c0.withTexture2D(t1);
          c0.withTexture2D(t2);
          c0.withTextureCube(tc0);

          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(1), t1));
          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(2), t2));
          Assert.assertTrue(gc.textureCubeStaticIsBound(units.get(3), tc0));

          bc
            .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
              @Override public void call(
                final KTextureBindingsContextType c1)
                throws RException
              {
                Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

                c1.withTexture2D(t3);

                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t3));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
              }
            });

          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(1), t1));
          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(2), t2));
          Assert.assertTrue(gc.textureCubeStaticIsBound(units.get(3), tc0));
        }
      });

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
  }

  @Test(expected = RExceptionUnitAllocatorNotCurrent.class) public
    void
    testBindingsNotCurrent_0()
      throws Exception
  {
    final JCGLSoftRestrictionsType r = new JCGLSoftRestrictionsType() {
      @Override public int restrictTextureUnitCount(
        final int count)
      {
        return 4;
      }

      @Override public boolean restrictExtensionVisibility(
        final String name)
      {
        return true;
      }
    };
    final OptionType<JCGLSoftRestrictionsType> some = Option.some(r);

    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), some);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureBindingsControllerType bc =
      KTextureBindingsController.newBindings(gc);

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    Assert.assertEquals(4, units.size());

    bc
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c0)
          throws RException
        {
          bc
            .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
              @Override public void call(
                final KTextureBindingsContextType c1)
                throws RException
              {
                c0.withTexture2D(t0);
              }
            });
        }
      });
  }

  @Test public void testBindingsNew_1()
    throws Exception
  {
    final JCGLSoftRestrictionsType r = new JCGLSoftRestrictionsType() {
      @Override public int restrictTextureUnitCount(
        final int count)
      {
        return 4;
      }

      @Override public boolean restrictExtensionVisibility(
        final String name)
      {
        return true;
      }
    };
    final OptionType<JCGLSoftRestrictionsType> some = Option.some(r);

    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), some);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureBindingsControllerType bc =
      KTextureBindingsController.newBindings(gc);

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    final Texture2DStaticType t1 = RFakeTextures2DStatic.newWithName(g, "t1");
    final Texture2DStaticType t2 = RFakeTextures2DStatic.newWithName(g, "t2");
    final TextureCubeStaticType tc0 = RFakeTexturesCubeStatic.newAnything(g);

    Assert.assertEquals(4, units.size());

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

    bc
      .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c0)
          throws RException
        {
          Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

          c0.withTexture2D(t0);

          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

          bc
            .withNewAppendingContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
              @Override public void call(
                final KTextureBindingsContextType c1)
                throws RException
              {
                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

                c1.withTexture2D(t1);

                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(1), t1));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
              }
            });

          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

          bc
            .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
              @Override public void call(
                final KTextureBindingsContextType c1)
                throws RException
              {
                Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
                Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));

                c1.withTexture2D(t0);
                c1.withTexture2D(t1);
                c1.withTexture2D(t2);
                c1.withTextureCube(tc0);

                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(1), t1));
                Assert.assertTrue(gc.texture2DStaticIsBound(units.get(2), t2));
                Assert.assertTrue(gc.textureCubeStaticIsBound(
                  units.get(3),
                  tc0));
              }
            });

          Assert.assertTrue(gc.texture2DStaticIsBound(units.get(0), t0));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
          Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
        }
      });

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
  }

  @Test(expected = RExceptionUnitAllocatorOutOfUnits.class) public
    void
    testBindingsOutOfUnits_0()
      throws Exception
  {
    final JCGLSoftRestrictionsType r = new JCGLSoftRestrictionsType() {
      @Override public int restrictTextureUnitCount(
        final int count)
      {
        return 4;
      }

      @Override public boolean restrictExtensionVisibility(
        final String name)
      {
        return true;
      }
    };
    final OptionType<JCGLSoftRestrictionsType> some = Option.some(r);

    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), some);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureBindingsControllerType bc =
      KTextureBindingsController.newBindings(gc);

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");

    Assert.assertEquals(4, units.size());

    bc
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c0)
          throws RException
        {
          c0.withTexture2D(t0);
          c0.withTexture2D(t0);
          c0.withTexture2D(t0);
          c0.withTexture2D(t0);
          c0.withTexture2D(t0);
        }
      });
  }

  @Test(expected = RExceptionUnitAllocatorOutOfUnits.class) public
    void
    testBindingsOutOfUnits_1()
      throws Exception
  {
    final JCGLSoftRestrictionsType r = new JCGLSoftRestrictionsType() {
      @Override public int restrictTextureUnitCount(
        final int count)
      {
        return 4;
      }

      @Override public boolean restrictExtensionVisibility(
        final String name)
      {
        return true;
      }
    };
    final OptionType<JCGLSoftRestrictionsType> some = Option.some(r);

    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), some);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureBindingsControllerType bc =
      KTextureBindingsController.newBindings(gc);

    final TextureCubeStaticType tc0 = RFakeTexturesCubeStatic.newAnything(g);

    Assert.assertEquals(4, units.size());

    bc
      .withNewEmptyContext(new PartialProcedureType<KTextureBindingsContextType, RException>() {
        @Override public void call(
          final KTextureBindingsContextType c0)
          throws RException
        {
          c0.withTextureCube(tc0);
          c0.withTextureCube(tc0);
          c0.withTextureCube(tc0);
          c0.withTextureCube(tc0);
          c0.withTextureCube(tc0);
        }
      });
  }
}
