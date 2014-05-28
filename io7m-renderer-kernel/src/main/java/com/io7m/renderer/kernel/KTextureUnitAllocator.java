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

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLTextureUnitsType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticCommonType;
import com.io7m.jcanephora.api.JCGLTexturesCubeStaticCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionUnitAllocatorActive;
import com.io7m.renderer.types.RExceptionUnitAllocatorMultipleChildren;

/**
 * <p>
 * Trivial class for tracking the currently used texture units, and
 * determining whether the current OpenGL implementation can actually support
 * a material that requires a given number of textures.
 * </p>
 * <p>
 * The allocator functions as a stack of contexts managing a fixed number of
 * units <code>u</code>, where each context <code>c</code> can mark units as
 * "allocated" such that
 * <code>∀n. allocated(c₀) + allocated(c₁) ... + allocated(cₙ) &lt;= u</code>
 * (where <code>allocated(c)</code> denotes the number of units marked by
 * <code>c</code>). If a context attempts to allocate more units than are
 * left, an error is raised. A context automatically returns all of its
 * associated allocated units when it is destroyed.
 * </p>
 */

@EqualityReference @SuppressWarnings("synthetic-access") public final class KTextureUnitAllocator implements
  KTextureUnitContextInitialType
{
  @EqualityReference private final class Context implements
    KTextureUnitContextType
  {
    private int               count;
    private final int         first;
    private @Nullable Context child;

    Context(
      final int in_first)
    {
      this.first = in_first;
      this.count = 0;
    }

    @Override public int getTextureCountForContext()
    {
      return this.count;
    }

    @Override public int getTextureCountTotal()
    {
      return KTextureUnitAllocator.this.texturesCurrent();
    }

    @Override public void withContext(
      final KTextureUnitWithType f)
      throws JCGLException,
        RException
    {
      if (this.child != null) {
        throw new RExceptionUnitAllocatorMultipleChildren(
          "Context already has a child (" + this.child + ")");
      }

      try {
        this.child = new Context(this.first + this.count);

        final Context c = new Context(this.first + this.count);
        f.run(c);
        KTextureUnitAllocator.this.texturesRemoved(c
          .getTextureCountForContext());
      } finally {
        this.child = null;
      }
    }

    @Override public TextureUnitType withTexture2D(
      final Texture2DStaticUsableType t)
      throws JCGLException,
        RException
    {
      final TextureUnitType unit =
        KTextureUnitAllocator.this.texturesUnitGet(this.first + this.count);
      KTextureUnitAllocator.this.texturesAdded(1);
      ++this.count;
      KTextureUnitAllocator.this.gc_textures2d.texture2DStaticBind(unit, t);
      return unit;
    }

    @Override public TextureUnitType withTextureCube(
      final TextureCubeStaticUsableType t)
      throws JCGLException,
        RException
    {
      final TextureUnitType unit =
        KTextureUnitAllocator.this.texturesUnitGet(this.first + this.count);
      KTextureUnitAllocator.this.texturesAdded(1);
      ++this.count;
      KTextureUnitAllocator.this.gc_textures_cube.textureCubeStaticBind(
        unit,
        t);
      return unit;
    }
  }

  /**
   * Construct a new texture unit allocator.
   * 
   * @param <G>
   *          The type of GL interfaces
   * @param gi
   *          The OpenGL interface
   * @return A new allocator
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static
    <G extends JCGLTextures2DStaticCommonType & JCGLTexturesCubeStaticCommonType & JCGLTextureUnitsType>
    KTextureUnitAllocator
    newAllocator(
      final G gi)
      throws JCGLException
  {
    return new KTextureUnitAllocator(gi);
  }

  private int                                    allocated;
  private final JCGLTexturesCubeStaticCommonType gc_textures_cube;
  private final JCGLTextures2DStaticCommonType   gc_textures2d;
  private boolean                                in_use;
  private final List<TextureUnitType>            units;

  private <G extends JCGLTextures2DStaticCommonType & JCGLTexturesCubeStaticCommonType & JCGLTextureUnitsType> KTextureUnitAllocator(
    final G in_gc)
    throws JCGLException
  {
    NullCheck.notNull(in_gc, "GL interface");
    this.gc_textures_cube = in_gc;
    this.gc_textures2d = in_gc;
    this.units = in_gc.textureGetUnits();
    this.allocated = 0;
    this.in_use = false;
  }

  @Override public int getTextureCountForContext()
  {
    return 0;
  }

  @Override public int getTextureCountTotal()
  {
    return this.allocated;
  }

  /**
   * @return The maximum available number of texture units
   */

  public int getUnitCount()
  {
    return this.units.size();
  }

  /**
   * @param requested
   *          The number of units required
   * @return <code>true</code> if there are at least <code>requested</code>
   *         units available
   */

  public boolean hasEnoughUnits(
    final int requested)
  {
    return requested <= this.getUnitCount();
  }

  protected void texturesAdded(
    final int x)
  {
    this.allocated += x;
  }

  protected int texturesCurrent()
  {
    return this.allocated;
  }

  protected void texturesRemoved(
    final int x)
  {
    this.allocated -= x;
  }

  protected TextureUnitType texturesUnitGet(
    final int index)
    throws RException
  {
    if (KTextureUnitAllocator.this.hasEnoughUnits(index + 1) == false) {
      throw RException.notEnoughTextureUnits(index + 1, this.units.size());
    }

    final TextureUnitType r = this.units.get(index);
    assert r != null;
    return r;
  }

  @Override public void withContext(
    final KTextureUnitWithType f)
    throws JCGLException,
      RException
  {
    if (this.in_use) {
      throw new RExceptionUnitAllocatorActive("Allocator is already in use");
    }

    try {
      this.in_use = true;
      final Context c = new Context(0);
      f.run(c);
      this.texturesRemoved(c.getTextureCountForContext());
    } finally {
      this.in_use = false;
    }
  }
}
