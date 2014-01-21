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

package com.io7m.renderer.kernel;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jcanephora.TextureCubeStatic;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.renderer.RException;
import com.io7m.renderer.TestContext;
import com.io7m.renderer.TestContract;

public abstract class KTextureUnitAllocatorContract extends TestContract
{
  @Test public void testInit()
    throws JCGLException,
      ConstraintError
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    Assert.assertTrue(a.hasEnoughUnits(units.size() - 1));
  }

  @Test public void testNoUseCount()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    final AtomicInteger called = new AtomicInteger(0);

    a.withContext(new KTextureUnitWith() {
      @Override public void run(
        final @Nonnull KTextureUnitContext c0)
        throws ConstraintError,
          JCGLException,
          RException
      {
        called.incrementAndGet();
        c0.withContext(new KTextureUnitWith() {
          @Override public void run(
            final @Nonnull KTextureUnitContext c1)
            throws ConstraintError,
              JCGLException,
              RException
          {
            called.incrementAndGet();
            c1.withContext(new KTextureUnitWith() {
              @Override public void run(
                final @Nonnull KTextureUnitContext c2)
                throws ConstraintError,
                  JCGLException,
                  RException
              {
                called.incrementAndGet();
                c2.withContext(new KTextureUnitWith() {
                  @Override public void run(
                    final @Nonnull KTextureUnitContext c3)
                    throws ConstraintError,
                      JCGLException,
                      RException
                  {
                    called.incrementAndGet();
                    c3.withContext(new KTextureUnitWith() {
                      @Override public void run(
                        final @Nonnull KTextureUnitContext c4)
                        throws ConstraintError,
                          JCGLException,
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

  @Test(expected = ConstraintError.class) public void testUseSelf_0()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    a.withContext(new KTextureUnitWith() {
      @Override public void run(
        final KTextureUnitContext _)
        throws ConstraintError,
          JCGLException,
          RException
      {
        a.withContext(new KTextureUnitWith() {
          @Override public void run(
            final KTextureUnitContext __)
            throws ConstraintError,
              JCGLException,
              RException
          {
            throw new UnreachableCodeException();
          }
        });
      }
    });
  }

  @Test(expected = ConstraintError.class) public void testUseSelf_1()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    a.withContext(new KTextureUnitWith() {
      @Override public void run(
        final KTextureUnitContext c0)
        throws ConstraintError,
          JCGLException,
          RException
      {
        c0.withContext(new KTextureUnitWith() {
          @Override public void run(
            final KTextureUnitContext c1)
            throws ConstraintError,
              JCGLException,
              RException
          {
            c0.withContext(new KTextureUnitWith() {
              @Override public void run(
                final KTextureUnitContext c2)
                throws ConstraintError,
                  JCGLException,
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

  @Test public void testUseAll_2D_0()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    final AtomicInteger called = new AtomicInteger(0);

    final Texture2DStatic t =
      g.getGLCommon().texture2DStaticAllocateRGB8(
        "t",
        4,
        4,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicReference<KTextureUnitContextInitial> previous =
      new AtomicReference<KTextureUnitContextInitial>(a);

    for (int index = 0; index < units.size(); ++index) {
      final int current = index;
      final KTextureUnitContextInitial p = previous.get();
      p.withContext(new KTextureUnitWith() {
        @SuppressWarnings("boxing") @Override public void run(
          final KTextureUnitContext context)
          throws ConstraintError,
            JCGLException,
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

  @Test(expected = ConstraintError.class) public void testUseAll_2D_1()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final Texture2DStatic t =
      g.getGLCommon().texture2DStaticAllocateRGB8(
        "t",
        4,
        4,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicReference<KTextureUnitContextInitial> previous =
      new AtomicReference<KTextureUnitContextInitial>(a);

    for (int index = 0; index < (units.size() + 2); ++index) {
      final KTextureUnitContextInitial p = previous.get();
      p.withContext(new KTextureUnitWith() {
        @Override public void run(
          final KTextureUnitContext context)
          throws ConstraintError,
            JCGLException,
            RException
        {
          final TextureUnit u = context.withTexture2D(t);
          System.out.println(String.format("Context: bound %s", u));
          previous.set(context);
        }
      });
    }
  }

  @Test public void testUseAll_2D_2()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final Texture2DStatic t =
      g.getGLCommon().texture2DStaticAllocateRGB8(
        "t",
        4,
        4,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicBoolean caught = new AtomicBoolean(false);

    a.withContext(new KTextureUnitWith() {
      @Override public void run(
        final @Nonnull KTextureUnitContext c0)
        throws ConstraintError,
          JCGLException,
          RException
      {
        Assert.assertEquals(0, c0.getTextureCountForContext());
        Assert.assertEquals(0, c0.getTextureCountTotal());

        c0.withTexture2D(t);
        c0.withTexture2D(t);

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());

        c0.withContext(new KTextureUnitWith() {
          @Override public void run(
            final @Nonnull KTextureUnitContext c1)
            throws ConstraintError,
              JCGLException,
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

            c1.withContext(new KTextureUnitWith() {
              @Override public void run(
                final @Nonnull KTextureUnitContext c2)
                throws ConstraintError,
                  JCGLException,
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

                c2.withContext(new KTextureUnitWith() {
                  @Override public void run(
                    final @Nonnull KTextureUnitContext c3)
                    throws ConstraintError,
                      JCGLException,
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

                    c3.withContext(new KTextureUnitWith() {
                      @Override public void run(
                        final @Nonnull KTextureUnitContext context)
                        throws ConstraintError,
                          JCGLException,
                          RException
                      {
                        try {
                          context.withTexture2D(t);
                        } catch (final ConstraintError x) {
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
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);
    final AtomicInteger called = new AtomicInteger(0);

    final TextureCubeStatic t =
      g.getGLCommon().textureCubeStaticAllocateRGBA8(
        "t",
        4,
        TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicReference<KTextureUnitContextInitial> previous =
      new AtomicReference<KTextureUnitContextInitial>(a);

    for (int index = 0; index < units.size(); ++index) {
      final int current = index;
      final KTextureUnitContextInitial p = previous.get();
      p.withContext(new KTextureUnitWith() {
        @SuppressWarnings("boxing") @Override public void run(
          final KTextureUnitContext context)
          throws ConstraintError,
            JCGLException,
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

  @Test(expected = ConstraintError.class) public void testUseAll_Cube_1()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();
    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final TextureCubeStatic t =
      g.getGLCommon().textureCubeStaticAllocateRGBA8(
        "t",
        4,
        TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicReference<KTextureUnitContextInitial> previous =
      new AtomicReference<KTextureUnitContextInitial>(a);

    for (int index = 0; index < (units.size() + 2); ++index) {
      final KTextureUnitContextInitial p = previous.get();
      p.withContext(new KTextureUnitWith() {
        @Override public void run(
          final KTextureUnitContext context)
          throws ConstraintError,
            JCGLException,
            RException
        {
          final TextureUnit u = context.withTextureCube(t);
          System.out.println(String.format("Context: bound %s", u));
          previous.set(context);
        }
      });
    }
  }

  @Test public void testUseAll_Cube_2()
    throws JCGLException,
      ConstraintError,
      RException
  {
    final TestContext tc = this.newTestContext(8);
    final JCGLImplementation g = tc.getGLImplementation();
    final List<TextureUnit> units = g.getGLCommon().textureGetUnits();

    Assume.assumeTrue("8 texture units are available", units.size() == 8);

    final KTextureUnitAllocator a = KTextureUnitAllocator.newAllocator(g);

    final TextureCubeStatic t =
      g.getGLCommon().textureCubeStaticAllocateRGBA8(
        "t",
        4,
        TextureWrapR.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapS.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureWrapT.TEXTURE_WRAP_CLAMP_TO_EDGE,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

    final AtomicBoolean caught = new AtomicBoolean(false);

    a.withContext(new KTextureUnitWith() {
      @Override public void run(
        final @Nonnull KTextureUnitContext c0)
        throws ConstraintError,
          JCGLException,
          RException
      {
        Assert.assertEquals(0, c0.getTextureCountForContext());
        Assert.assertEquals(0, c0.getTextureCountTotal());

        c0.withTextureCube(t);
        c0.withTextureCube(t);

        Assert.assertEquals(2, c0.getTextureCountForContext());
        Assert.assertEquals(2, c0.getTextureCountTotal());

        c0.withContext(new KTextureUnitWith() {
          @Override public void run(
            final @Nonnull KTextureUnitContext c1)
            throws ConstraintError,
              JCGLException,
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

            c1.withContext(new KTextureUnitWith() {
              @Override public void run(
                final @Nonnull KTextureUnitContext c2)
                throws ConstraintError,
                  JCGLException,
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

                c2.withContext(new KTextureUnitWith() {
                  @Override public void run(
                    final @Nonnull KTextureUnitContext c3)
                    throws ConstraintError,
                      JCGLException,
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

                    c3.withContext(new KTextureUnitWith() {
                      @Override public void run(
                        final @Nonnull KTextureUnitContext context)
                        throws ConstraintError,
                          JCGLException,
                          RException
                      {
                        try {
                          context.withTextureCube(t);
                        } catch (final ConstraintError x) {
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
}
