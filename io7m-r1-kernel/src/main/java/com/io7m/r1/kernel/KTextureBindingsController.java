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

package com.io7m.r1.kernel;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.TextureUsableType;
import com.io7m.jcanephora.TextureUsableVisitorType;
import com.io7m.jcanephora.api.JCGLTextureUnitsType;
import com.io7m.jcanephora.api.JCGLTextures2DStaticCommonType;
import com.io7m.jcanephora.api.JCGLTexturesCubeStaticCommonType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.PartialProcedureType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorNotCurrent;
import com.io7m.r1.exceptions.RExceptionUnitAllocatorOutOfUnits;

/**
 * The default implementation of the {@link KTextureBindingsControllerType}
 * type.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KTextureBindingsController implements
  KTextureBindingsControllerType
{
  @EqualityReference private final class Context implements
    KTextureBindingsContextType
  {
    private final TextureUsableType[] bindings;
    private int                       bindings_next;

    Context(
      final TextureUsableType[] in_bindings,
      final int in_bindings_start)
    {
      this.bindings = in_bindings;
      this.bindings_next = in_bindings_start;
    }

    private void checkCurrent()
      throws RExceptionUnitAllocatorNotCurrent
    {
      if (this != KTextureBindingsController.this.contexts.peek()) {
        throw new RExceptionUnitAllocatorNotCurrent(
          "Texture unit context is not current");
      }
    }

    @Override public TextureUnitType withTexture2D(
      final Texture2DStaticUsableType t)
      throws RException
    {
      this.checkCurrent();

      final List<TextureUnitType> us = KTextureBindingsController.this.units;
      if (this.bindings_next < us.size()) {
        final TextureUnitType u =
          KTextureBindingsController.this.contextBindTexture2D(
            this.bindings_next,
            t);
        this.bindings[this.bindings_next] = t;
        ++this.bindings_next;
        return u;
      }

      throw new RExceptionUnitAllocatorOutOfUnits("Out of texture units");
    }

    @Override public TextureUnitType withTextureCube(
      final TextureCubeStaticUsableType t)
      throws RException
    {
      this.checkCurrent();

      final List<TextureUnitType> us = KTextureBindingsController.this.units;
      if (this.bindings_next < us.size()) {
        final TextureUnitType u =
          KTextureBindingsController.this.contextBindTextureCube(
            this.bindings_next,
            t);
        this.bindings[this.bindings_next] = t;
        ++this.bindings_next;
        return u;
      }

      throw new RExceptionUnitAllocatorOutOfUnits("Out of texture units");
    }
  }

  /**
   * Construct a new texture binding controller.
   *
   * @param <G>
   *          The type of OpenGL interface
   * @param g
   *          An OpenGL interface
   * @return A new texture binding controller.
   */

  public static
    <G extends JCGLTextureUnitsType & JCGLTextures2DStaticCommonType & JCGLTexturesCubeStaticCommonType>
    KTextureBindingsControllerType
    newBindings(
      final G g)
  {
    return new KTextureBindingsController(g);
  }

  private final Deque<Context>                   contexts;
  private final TextureUsableType[]              current;
  private final TextureUsableType[]              empty;
  private final JCGLTextures2DStaticCommonType   t2d;
  private final JCGLTexturesCubeStaticCommonType tc;
  private final List<TextureUnitType>            units;

  private <G extends JCGLTextureUnitsType & JCGLTextures2DStaticCommonType & JCGLTexturesCubeStaticCommonType> KTextureBindingsController(
    final G g)
  {
    NullCheck.notNull(g, "OpenGL interface");
    this.units = g.textureGetUnits();
    this.current = new TextureUsableType[this.units.size()];
    this.empty = new TextureUsableType[this.units.size()];
    this.contexts = new LinkedList<Context>();
    this.t2d = g;
    this.tc = g;
  }

  private TextureUnitType contextBindTexture2D(
    final int index,
    final Texture2DStaticUsableType t)
  {
    final TextureUnitType u = NullCheck.notNull(this.units.get(index));
    this.current[index] = t;
    this.t2d.texture2DStaticBind(u, t);
    return u;
  }

  private TextureUnitType contextBindTextureCube(
    final int index,
    final TextureCubeStaticUsableType t)
  {
    final TextureUnitType u = NullCheck.notNull(this.units.get(index));
    this.current[index] = t;
    this.tc.textureCubeStaticBind(u, t);
    return u;
  }

  private void contextSwitch(
    final TextureUsableType[] next)
  {
    for (int index = 0; index < next.length; ++index) {
      final int ci = index;
      final TextureUsableType cb = this.current[index];
      final TextureUsableType nb = next[index];

      if (cb != nb) {
        if (nb == null) {
          this.contextUnbind(index);
        } else {
          nb
            .textureUsableAccept(new TextureUsableVisitorType<Unit, UnreachableCodeException>() {
              @Override public Unit texture2D(
                final Texture2DStaticUsableType t)
              {
                KTextureBindingsController.this.contextBindTexture2D(ci, t);
                return Unit.unit();
              }

              @Override public Unit textureCube(
                final TextureCubeStaticUsableType t)
              {
                KTextureBindingsController.this.contextBindTextureCube(ci, t);
                return Unit.unit();
              }
            });
        }
      }
    }
  }

  private void contextUnbind(
    final int index)
  {
    final TextureUnitType u = NullCheck.notNull(this.units.get(index));
    this.current[index] = null;
    this.t2d.texture2DStaticUnbind(u);
  }

  private void execute(
    final PartialProcedureType<KTextureBindingsContextType, RException> f,
    final Context c)
    throws RException
  {
    final Context previous = this.contexts.peek();
    this.contexts.push(c);
    try {
      this.contextSwitch(c.bindings);
      f.call(c);
    } finally {
      this.contexts.pop();
      if (previous != null) {
        this.contextSwitch(previous.bindings);
      } else {
        this.contextSwitch(this.empty);
      }
    }
  }

  @Override public void withNewAppendingContext(
    final PartialProcedureType<KTextureBindingsContextType, RException> f)
    throws RException
  {
    NullCheck.notNull(f, "Function");

    final int new_bindings_used;
    final TextureUsableType[] new_bindings;
    final Context previous = this.contexts.peek();
    if (previous == null) {
      new_bindings_used = 0;
      new_bindings = new TextureUsableType[this.current.length];
    } else {
      new_bindings_used = previous.bindings_next;
      new_bindings =
        NullCheck.notNull(Arrays.copyOf(
          previous.bindings,
          this.current.length));
    }

    final Context c = new Context(new_bindings, new_bindings_used);
    this.execute(f, c);
  }

  @Override public void withNewEmptyContext(
    final PartialProcedureType<KTextureBindingsContextType, RException> f)
    throws RException
  {
    NullCheck.notNull(f, "Function");
    final Context c =
      new Context(new TextureUsableType[this.current.length], 0);
    this.execute(f, c);
  }
}
