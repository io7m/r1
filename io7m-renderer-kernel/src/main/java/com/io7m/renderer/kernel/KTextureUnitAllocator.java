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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLRuntimeException;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jcanephora.TextureCubeStaticUsable;
import com.io7m.jcanephora.TextureUnit;
import com.io7m.renderer.types.RException;

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

public final class KTextureUnitAllocator implements
  KTextureUnitContextInitialType
{
  private final class Context implements KTextureUnitContextType
  {
    private int       count;
    private final int first;
    private boolean   has_child;

    Context(
      final int in_first)
    {
      this.first = in_first;
      this.count = 0;
      this.has_child = false;
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
        ConstraintError,
        RException
    {
      Constraints.constrainArbitrary(
        this.has_child == false,
        "Context has no child");

      try {
        this.has_child = true;
        final Context c = new Context(this.first + this.count);
        f.run(c);
        KTextureUnitAllocator.this.texturesRemoved(c
          .getTextureCountForContext());
      } finally {
        this.has_child = false;
      }
    }

    @SuppressWarnings("synthetic-access") @Override public
      TextureUnit
      withTexture2D(
        final @Nonnull Texture2DStaticUsable t)
        throws ConstraintError,
          JCGLRuntimeException
    {
      final TextureUnit unit =
        KTextureUnitAllocator.this.texturesUnitGet(this.first + this.count);
      KTextureUnitAllocator.this.texturesAdded(1);
      ++this.count;
      KTextureUnitAllocator.this.gc.texture2DStaticBind(unit, t);
      return unit;
    }

    @SuppressWarnings("synthetic-access") @Override public
      TextureUnit
      withTextureCube(
        final @Nonnull TextureCubeStaticUsable t)
        throws ConstraintError,
          JCGLRuntimeException
    {
      final TextureUnit unit =
        KTextureUnitAllocator.this.texturesUnitGet(this.first + this.count);
      KTextureUnitAllocator.this.texturesAdded(1);
      ++this.count;
      KTextureUnitAllocator.this.gc.textureCubeStaticBind(unit, t);
      return unit;
    }
  }

  /**
   * Construct a new texture unit allocator.
   * 
   * @param gi
   *          The OpenGL interface
   * @return A new allocator
   * @throws JCGLException
   *           If an OpenGL error occurs
   */

  public static @Nonnull KTextureUnitAllocator newAllocator(
    final @Nonnull JCGLImplementation gi)
    throws JCGLException
  {
    return new KTextureUnitAllocator(gi);
  }

  private int                                allocated;
  private final @Nonnull JCGLInterfaceCommon gc;
  private boolean                            in_use;
  private final @Nonnull List<TextureUnit>   units;

  private KTextureUnitAllocator(
    final @Nonnull JCGLImplementation gi)
    throws JCGLException
  {
    this.gc = gi.getGLCommon();
    this.units = this.gc.textureGetUnits();
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

  protected @Nonnull TextureUnit texturesUnitGet(
    final int index)
    throws ConstraintError
  {
    Constraints.constrainArbitrary(
      KTextureUnitAllocator.this.hasEnoughUnits(index + 1),
      "Enough texture units available");

    return this.units.get(index);
  }

  @Override public void withContext(
    final @Nonnull KTextureUnitWithType f)
    throws JCGLException,
      ConstraintError,
      RException
  {
    Constraints.constrainArbitrary(
      this.in_use == false,
      "Allocator not already in use");

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
