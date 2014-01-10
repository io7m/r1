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

import java.io.IOException;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.RException;

public abstract class KAbstractRenderer implements KRenderer
{
  public static abstract class KAbstractRendererDebug extends
    KAbstractRenderer implements KRendererDebug
  {
    protected KAbstractRendererDebug(
      final @Nonnull String name)
    {
      super(name);
    }

    @Override public final
      <A, E extends Throwable, V extends KRendererVisitor<A, E>>
      A
      rendererVisitableAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          ConstraintError,
          IOException,
          KXMLException,
          RException
    {
      Constraints.constrainNotNull(v, "Visitor");
      return v.rendererVisitDebug(this);
    }
  }

  public static abstract class KAbstractRendererDeferred extends
    KAbstractRenderer implements KRendererDeferred
  {
    protected KAbstractRendererDeferred(
      final @Nonnull String name)
    {
      super(name);
    }

    @Override public final
      <A, E extends Throwable, V extends KRendererVisitor<A, E>>
      A
      rendererVisitableAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          ConstraintError,
          IOException,
          KXMLException,
          RException
    {
      Constraints.constrainNotNull(v, "Visitor");
      return v.rendererVisitDeferred(this);
    }
  }

  public static abstract class KAbstractRendererForward extends
    KAbstractRenderer implements KRendererForward
  {
    protected KAbstractRendererForward(
      final @Nonnull String name)
    {
      super(name);
    }

    @Override public final
      <A, E extends Throwable, V extends KRendererVisitor<A, E>>
      A
      rendererVisitableAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          ConstraintError,
          IOException,
          KXMLException,
          RException
    {
      Constraints.constrainNotNull(v, "Visitor");
      return v.rendererVisitForward(this);
    }
  }

  private final @Nonnull String name;

  protected KAbstractRenderer(
    final @Nonnull String name)
  {
    this.name = name;
  }

  @Override public final @Nonnull String rendererGetName()
  {
    return this.name;
  }
}
