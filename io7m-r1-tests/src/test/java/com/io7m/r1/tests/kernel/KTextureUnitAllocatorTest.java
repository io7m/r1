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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureFormat;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionResource;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorActive;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorMultipleChildren;
import com.io7m.r1.kernel.KTextureUnitAllocator;
import com.io7m.r1.kernel.KTextureUnitContextInitialType;
import com.io7m.r1.kernel.KTextureUnitContextType;
import com.io7m.r1.kernel.KTextureUnitWithType;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.RFakeTextures2DStatic;
import com.io7m.r1.tests.RFakeTexturesCubeStatic;

@SuppressWarnings({ "null", "static-method" }) public final class KTextureUnitAllocatorTest
{
  @Test public void testBindings_0()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    final Texture2DStaticType t1 = RFakeTextures2DStatic.newWithName(g, "t1");
    final Texture2DStaticType t2 = RFakeTextures2DStatic.newWithName(g, "t2");
    final Texture2DStaticType t3 = RFakeTextures2DStatic.newWithName(g, "t3");

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        c0.withTexture2D(t0);
        c0.withTexture2D(t1);
        c0.withTexture2D(t2);
        c0.withTexture2D(t3);

        Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
      }
    });

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
  }

  @Test public void testBindings_1()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    final Texture2DStaticType t1 = RFakeTextures2DStatic.newWithName(g, "t1");
    final Texture2DStaticType t2 = RFakeTextures2DStatic.newWithName(g, "t2");
    final Texture2DStaticType t3 = RFakeTextures2DStatic.newWithName(g, "t3");

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        c0.withTexture2D(t0);
        c0.withTexture2D(t1);
        c0.withTexture2D(t2);
        c0.withTexture2D(t3);

        Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            c1.withTexture2D(t0);
            c1.withTexture2D(t1);
            c1.withTexture2D(t2);
            c1.withTexture2D(t3);

            Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(4)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(5)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(6)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(7)));
          }
        });

        Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
      }
    });

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
  }

  @Test public void testBindings_2()
    throws Exception
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

    final Texture2DStaticType t0 = RFakeTextures2DStatic.newWithName(g, "t0");
    final Texture2DStaticType t1 = RFakeTextures2DStatic.newWithName(g, "t1");
    final Texture2DStaticType t2 = RFakeTextures2DStatic.newWithName(g, "t2");

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        c0.withTexture2D(t0);
        c0.withTexture2D(t1);
        c0.withTexture2D(t2);

        Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            c1.withTexture2D(t0);
            c1.withTexture2D(t1);
            c1.withTexture2D(t2);

            Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(4)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(5)));
            Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
            Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));

            c1.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType c2)
                throws JCGLException,
                  RException
              {
                c2.withTexture2D(t0);
                c2.withTexture2D(t1);

                Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(4)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(5)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(6)));
                Assert.assertTrue(gc.textureUnitIsBound(units.get(7)));
              }
            });

            Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(3)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(4)));
            Assert.assertTrue(gc.textureUnitIsBound(units.get(5)));
            Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
            Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
          }
        });

        Assert.assertTrue(gc.textureUnitIsBound(units.get(0)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(1)));
        Assert.assertTrue(gc.textureUnitIsBound(units.get(2)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
        Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
      }
    });

    Assert.assertFalse(gc.textureUnitIsBound(units.get(0)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(1)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(2)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(3)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(4)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(5)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(6)));
    Assert.assertFalse(gc.textureUnitIsBound(units.get(7)));
  }

  @Test public void testInit()
    throws JCGLException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();

    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);
    Assert.assertTrue(a.hasEnoughUnits(units.size() - 1));
  }

  @Test public void testNoUseCount()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    final AtomicInteger called = new AtomicInteger(0);

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        called.incrementAndGet();
        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            called.incrementAndGet();
            c1.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType c2)
                throws JCGLException,
                  RException
              {
                called.incrementAndGet();
                c2.withContext(new KTextureUnitWithType() {
                  @Override public void run(
                    final KTextureUnitContextType c3)
                    throws JCGLException,
                      RException
                  {
                    called.incrementAndGet();
                    c3.withContext(new KTextureUnitWithType() {
                      @Override public void run(
                        final KTextureUnitContextType c4)
                        throws JCGLException,
                          RException
                      {
                        called.incrementAndGet();
                        Assert.assertEquals(0, c4.getTextureCountForContext());
                        Assert.assertEquals(0, c4.getTextureCountTotal());
                      }
                    });
                  }
                });
              }
            });
          }
        });
      }
    });

    Assert.assertEquals(5, called.get());
  }

  @Test public void testUseAll_2D_0()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    final AtomicInteger called = new AtomicInteger(0);
    final Texture2DStaticType t = RFakeTextures2DStatic.newWithName(g, "any");

    final AtomicReference<KTextureUnitContextInitialType> previous =
      new AtomicReference<KTextureUnitContextInitialType>(a);

    for (int index = 0; index < units.size(); ++index) {
      final int current = index;
      final KTextureUnitContextInitialType p = previous.get();
      p.withContext(new KTextureUnitWithType() {
        @SuppressWarnings("boxing") @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          context.withTexture2D(t);
          called.incrementAndGet();
          System.out.println(String.format("Context: %d", current));
          previous.set(context);
        }
      });
    }

    Assert.assertEquals(units.size(), called.get());
  }

  @Test(expected = RExceptionResource.class) public void testUseAll_2D_1()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    final Texture2DStaticType t = RFakeTextures2DStatic.newWithName(g, "any");

    final AtomicReference<KTextureUnitContextInitialType> previous =
      new AtomicReference<KTextureUnitContextInitialType>(a);

    for (int index = 0; index < (units.size() + 2); ++index) {
      final KTextureUnitContextInitialType p = previous.get();
      p.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          final TextureUnitType u = context.withTexture2D(t);
          System.out.println(String.format("Context: bound %s", u));
          previous.set(context);
        }
      });
    }
  }

  @Test public void testUseAll_2D_2()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final Texture2DStaticType t = new Texture2DStaticType() {
      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public long resourceGetSizeBytes()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public boolean resourceIsDeleted()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMagnification
        textureGetMagnificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    };

    final AtomicBoolean caught = new AtomicBoolean(false);

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        Assert.assertEquals(0, c0.getTextureCountForContext());
        Assert.assertEquals(0, c0.getTextureCountTotal());

        c0.withTexture2D(t);
        c0.withTexture2D(t);

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());

        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(2, c0.getTextureCountTotal());
            Assert.assertEquals(0, c1.getTextureCountForContext());
            Assert.assertEquals(2, c1.getTextureCountTotal());

            c1.withTexture2D(t);
            c1.withTexture2D(t);

            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(4, c0.getTextureCountTotal());
            Assert.assertEquals(2, c1.getTextureCountForContext());
            Assert.assertEquals(4, c1.getTextureCountTotal());

            c1.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType c2)
                throws JCGLException,
                  RException
              {
                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(4, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(4, c1.getTextureCountTotal());
                Assert.assertEquals(0, c2.getTextureCountForContext());
                Assert.assertEquals(4, c2.getTextureCountTotal());

                c2.withTexture2D(t);
                c2.withTexture2D(t);

                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(6, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(6, c1.getTextureCountTotal());
                Assert.assertEquals(2, c2.getTextureCountForContext());
                Assert.assertEquals(6, c2.getTextureCountTotal());

                c2.withContext(new KTextureUnitWithType() {
                  @Override public void run(
                    final KTextureUnitContextType c3)
                    throws JCGLException,
                      RException
                  {
                    Assert.assertEquals(2, c0.getTextureCountForContext());
                    Assert.assertEquals(6, c0.getTextureCountTotal());
                    Assert.assertEquals(2, c1.getTextureCountForContext());
                    Assert.assertEquals(6, c1.getTextureCountTotal());
                    Assert.assertEquals(2, c2.getTextureCountForContext());
                    Assert.assertEquals(6, c2.getTextureCountTotal());

                    c3.withTexture2D(t);
                    c3.withTexture2D(t);

                    Assert.assertEquals(2, c0.getTextureCountForContext());
                    Assert.assertEquals(8, c0.getTextureCountTotal());
                    Assert.assertEquals(2, c1.getTextureCountForContext());
                    Assert.assertEquals(8, c1.getTextureCountTotal());
                    Assert.assertEquals(2, c2.getTextureCountForContext());
                    Assert.assertEquals(8, c2.getTextureCountTotal());
                    Assert.assertEquals(2, c3.getTextureCountForContext());
                    Assert.assertEquals(8, c3.getTextureCountTotal());

                    c3.withContext(new KTextureUnitWithType() {
                      @Override public void run(
                        final KTextureUnitContextType context)
                        throws JCGLException,
                          RException
                      {
                        try {
                          context.withTexture2D(t);
                        } catch (final RExceptionResource x) {
                          caught.set(true);
                        }
                      }
                    });
                  }
                });

                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(6, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(6, c1.getTextureCountTotal());
                Assert.assertEquals(2, c2.getTextureCountForContext());
                Assert.assertEquals(6, c2.getTextureCountTotal());
              }
            });

            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(4, c0.getTextureCountTotal());
            Assert.assertEquals(2, c1.getTextureCountForContext());
            Assert.assertEquals(4, c1.getTextureCountTotal());
          }
        });

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());
      }
    });

    Assert.assertEquals(0, a.getTextureCountForContext());
    Assert.assertEquals(0, a.getTextureCountTotal());
    Assert.assertTrue(caught.get());
  }

  @Test public void testUseAll_Cube_0()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    final AtomicInteger called = new AtomicInteger(0);
    final TextureCubeStaticType t = RFakeTexturesCubeStatic.newAnything(g);

    final AtomicReference<KTextureUnitContextInitialType> previous =
      new AtomicReference<KTextureUnitContextInitialType>(a);

    for (int index = 0; index < units.size(); ++index) {
      final int current = index;
      final KTextureUnitContextInitialType p = previous.get();
      p.withContext(new KTextureUnitWithType() {
        @SuppressWarnings("boxing") @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          context.withTextureCube(t);
          called.incrementAndGet();
          System.out.println(String.format("Context: %d", current));
          previous.set(context);
        }
      });
    }

    Assert.assertEquals(units.size(), called.get());
  }

  @Test(expected = RExceptionResource.class) public void testUseAll_Cube_1()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    final TextureCubeStaticType t = RFakeTexturesCubeStatic.newAnything(g);

    final AtomicReference<KTextureUnitContextInitialType> previous =
      new AtomicReference<KTextureUnitContextInitialType>(a);

    for (int index = 0; index < (units.size() + 2); ++index) {
      final KTextureUnitContextInitialType p = previous.get();
      p.withContext(new KTextureUnitWithType() {
        @Override public void run(
          final KTextureUnitContextType context)
          throws JCGLException,
            RException
        {
          final TextureUnitType u = context.withTextureCube(t);
          System.out.println(String.format("Context: bound %s", u));
          previous.set(context);
        }
      });
    }
  }

  @Test public void testUseAll_Cube_2()
    throws JCGLException,
      RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final List<TextureUnitType> units = gc.textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final TextureCubeStaticType t = new TextureCubeStaticType() {
      @Override public int getGLName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public long resourceGetSizeBytes()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public boolean resourceIsDeleted()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public AreaInclusive textureGetArea()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureFormat textureGetFormat()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetHeight()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMagnification
        textureGetMagnificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public
        TextureFilterMinification
        textureGetMinificationFilter()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public String textureGetName()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeX()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public RangeInclusiveL textureGetRangeY()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public int textureGetWidth()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapR textureGetWrapR()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapS textureGetWrapS()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }

      @Override public TextureWrapT textureGetWrapT()
      {
        // TODO Auto-generated method stub
        throw new UnimplementedCodeException();
      }
    };

    final AtomicBoolean caught = new AtomicBoolean(false);

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        Assert.assertEquals(0, c0.getTextureCountForContext());
        Assert.assertEquals(0, c0.getTextureCountTotal());

        c0.withTextureCube(t);
        c0.withTextureCube(t);

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());

        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(2, c0.getTextureCountTotal());
            Assert.assertEquals(0, c1.getTextureCountForContext());
            Assert.assertEquals(2, c1.getTextureCountTotal());

            c1.withTextureCube(t);
            c1.withTextureCube(t);

            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(4, c0.getTextureCountTotal());
            Assert.assertEquals(2, c1.getTextureCountForContext());
            Assert.assertEquals(4, c1.getTextureCountTotal());

            c1.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType c2)
                throws JCGLException,
                  RException
              {
                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(4, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(4, c1.getTextureCountTotal());
                Assert.assertEquals(0, c2.getTextureCountForContext());
                Assert.assertEquals(4, c2.getTextureCountTotal());

                c2.withTextureCube(t);
                c2.withTextureCube(t);

                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(6, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(6, c1.getTextureCountTotal());
                Assert.assertEquals(2, c2.getTextureCountForContext());
                Assert.assertEquals(6, c2.getTextureCountTotal());

                c2.withContext(new KTextureUnitWithType() {
                  @Override public void run(
                    final KTextureUnitContextType c3)
                    throws JCGLException,
                      RException
                  {
                    Assert.assertEquals(2, c0.getTextureCountForContext());
                    Assert.assertEquals(6, c0.getTextureCountTotal());
                    Assert.assertEquals(2, c1.getTextureCountForContext());
                    Assert.assertEquals(6, c1.getTextureCountTotal());
                    Assert.assertEquals(2, c2.getTextureCountForContext());
                    Assert.assertEquals(6, c2.getTextureCountTotal());

                    c3.withTextureCube(t);
                    c3.withTextureCube(t);

                    Assert.assertEquals(2, c0.getTextureCountForContext());
                    Assert.assertEquals(8, c0.getTextureCountTotal());
                    Assert.assertEquals(2, c1.getTextureCountForContext());
                    Assert.assertEquals(8, c1.getTextureCountTotal());
                    Assert.assertEquals(2, c2.getTextureCountForContext());
                    Assert.assertEquals(8, c2.getTextureCountTotal());
                    Assert.assertEquals(2, c3.getTextureCountForContext());
                    Assert.assertEquals(8, c3.getTextureCountTotal());

                    c3.withContext(new KTextureUnitWithType() {
                      @Override public void run(
                        final KTextureUnitContextType context)
                        throws JCGLException,
                          RException
                      {
                        try {
                          context.withTextureCube(t);
                        } catch (final RExceptionResource x) {
                          caught.set(true);
                        }
                      }
                    });
                  }
                });

                Assert.assertEquals(2, c0.getTextureCountForContext());
                Assert.assertEquals(6, c0.getTextureCountTotal());
                Assert.assertEquals(2, c1.getTextureCountForContext());
                Assert.assertEquals(6, c1.getTextureCountTotal());
                Assert.assertEquals(2, c2.getTextureCountForContext());
                Assert.assertEquals(6, c2.getTextureCountTotal());
              }
            });

            Assert.assertEquals(2, c0.getTextureCountForContext());
            Assert.assertEquals(4, c0.getTextureCountTotal());
            Assert.assertEquals(2, c1.getTextureCountForContext());
            Assert.assertEquals(4, c1.getTextureCountTotal());
          }
        });

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());
      }
    });

    Assert.assertEquals(0, a.getTextureCountForContext());
    Assert.assertEquals(0, a.getTextureCountTotal());
    Assert.assertTrue(caught.get());
  }

  @Test(expected = RExceptionUnitAllocatorActive.class) public
    void
    testUseSelf_0()
      throws JCGLException,
        RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType _)
        throws JCGLException,
          RException
      {
        a.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType __)
            throws JCGLException,
              RException
          {
            throw new UnreachableCodeException();
          }
        });
      }
    });
  }

  @Test(expected = RExceptionUnitAllocatorMultipleChildren.class) public
    void
    testUseSelf_1()
      throws JCGLException,
        RException
  {
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType g =
      RFakeGL.newFakeGL30(RFakeShaderControllers.newNull(), none);
    final JCGLInterfaceCommonType gc = g.getGLCommon();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(gc);

    a.withContext(new KTextureUnitWithType() {
      @Override public void run(
        final KTextureUnitContextType c0)
        throws JCGLException,
          RException
      {
        c0.withContext(new KTextureUnitWithType() {
          @Override public void run(
            final KTextureUnitContextType c1)
            throws JCGLException,
              RException
          {
            c0.withContext(new KTextureUnitWithType() {
              @Override public void run(
                final KTextureUnitContextType c2)
                throws JCGLException,
                  RException
              {
                throw new UnreachableCodeException();
              }
            });
          }
        });
      }
    });
  }
}
