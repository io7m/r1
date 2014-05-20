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

package com.io7m.renderer.tests.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticType;
import com.io7m.jcanephora.TextureCubeStaticType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jnull.NonNull;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.KTextureUnitAllocator;
import com.io7m.renderer.kernel.KTextureUnitContextInitialType;
import com.io7m.renderer.kernel.KTextureUnitContextType;
import com.io7m.renderer.kernel.KTextureUnitWithType;
import com.io7m.renderer.tests.FakeTexture2DStatic;
import com.io7m.renderer.tests.FakeTextureCubeStatic;
import com.io7m.renderer.tests.FakeTextureUnit;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionUserError;
import com.io7m.renderer.types.RExceptionResource;

@SuppressWarnings("static-method") public final class KTextureUnitAllocatorTest
{
  private static @NonNull List<TextureUnitType> makeUnits(
    final int i)
  {
    final List<TextureUnitType> u = new ArrayList<TextureUnitType>(i);
    for (int k = 0; k < i; ++k) {
      u.add(FakeTextureUnit.newUnit(k));
    }
    return u;
  }

  @Test public void testInit()
    throws JCGLException
  {
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    Assert.assertTrue(a.hasEnoughUnits(units.size() - 1));
  }

  @Test public void testNoUseCount()
    throws JCGLException,
      RException
  {
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

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
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    final AtomicInteger called = new AtomicInteger(0);

    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();

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
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();

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

  //
  // private static Texture2DStaticUsable makeTexture2D(
  // final JCGLImplementation gi)
  // throws JCGLException,
  // ConstraintError
  // {
  // return gi
  // .implementationAccept(new
  // JCGLImplementationVisitor<Texture2DStaticUsable, JCGLException>() {
  //
  // @Override public Texture2DStaticUsable implementationIsGL2(
  // final JCGLInterfaceGL2 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.texture2DStaticAllocateRGB8(
  // "texture",
  // 128,
  // 128,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public Texture2DStaticUsable implementationIsGL3(
  // final JCGLInterfaceGL3 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.texture2DStaticAllocateRGB8(
  // "texture",
  // 128,
  // 128,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public Texture2DStaticUsable implementationIsGLES2(
  // final JCGLInterfaceGLES2 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.texture2DStaticAllocateRGB565(
  // "texture",
  // 128,
  // 128,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public Texture2DStaticUsable implementationIsGLES3(
  // final JCGLInterfaceGLES3 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.texture2DStaticAllocateRGB8(
  // "texture",
  // 128,
  // 128,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  // });
  // }
  //
  // private static TextureCubeStaticUsable makeTextureCube(
  // final JCGLImplementation gi)
  // throws JCGLException,
  // ConstraintError
  // {
  // return gi
  // .implementationAccept(new
  // JCGLImplementationVisitor<TextureCubeStaticUsable, JCGLException>() {
  //
  // @Override public TextureCubeStaticUsable implementationIsGL2(
  // final JCGLInterfaceGL2 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.textureCubeStaticAllocateRGB8(
  // "texture",
  // 128,
  // TextureWrapR.TEXTURE_WRAP_REPEAT,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public TextureCubeStaticUsable implementationIsGL3(
  // final JCGLInterfaceGL3 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.textureCubeStaticAllocateRGB8(
  // "texture",
  // 128,
  // TextureWrapR.TEXTURE_WRAP_REPEAT,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public TextureCubeStaticUsable implementationIsGLES2(
  // final JCGLInterfaceGLES2 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.textureCubeStaticAllocateRGB565(
  // "texture",
  // 128,
  // TextureWrapR.TEXTURE_WRAP_REPEAT,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  //
  // @Override public TextureCubeStaticUsable implementationIsGLES3(
  // final JCGLInterfaceGLES3 gl)
  // throws JCGLException,
  // ConstraintError,
  // JCGLException
  // {
  // return gl.textureCubeStaticAllocateRGB8(
  // "texture",
  // 128,
  // TextureWrapR.TEXTURE_WRAP_REPEAT,
  // TextureWrapS.TEXTURE_WRAP_REPEAT,
  // TextureWrapT.TEXTURE_WRAP_REPEAT,
  // TextureFilterMinification.TEXTURE_FILTER_LINEAR,
  // TextureFilterMagnification.TEXTURE_FILTER_LINEAR);
  // }
  // });
  // }
  //

  @Test public void testUseAll_2D_2()
    throws JCGLException,
      RException
  {
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final Texture2DStaticType t = FakeTexture2DStatic.getDefault();

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
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    final AtomicInteger called = new AtomicInteger(0);
    final TextureCubeStaticType t = FakeTextureCubeStatic.getDefault();

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
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final TextureCubeStaticType t = FakeTextureCubeStatic.getDefault();

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
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final TextureCubeStaticType t = FakeTextureCubeStatic.getDefault();

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

  @Test(expected = RExceptionUserError.class) public void testUseSelf_0()
    throws JCGLException,
      RException
  {
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

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

  @Test(expected = RExceptionUserError.class) public void testUseSelf_1()
    throws JCGLException,
      RException
  {
    final List<TextureUnitType> units =
      KTextureUnitAllocatorTest.makeUnits(8);
    final KJCGLTextureFake g = new KJCGLTextureFake(units);
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

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
