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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.RException;

abstract class KTranslucent implements KTranslucentVisitable
{
  static final class KTranslucentLit extends KTranslucent
  {
    private final @Nonnull KMeshInstanceTransformed instance;
    private final @Nonnull KMaterialForwardLabel    label;
    private final @Nonnull List<KLight>             lights;

    KTranslucentLit(
      final @Nonnull KMeshInstanceTransformed instance,
      final @Nonnull KMaterialForwardLabel label,
      final @Nonnull List<KLight> lights)
    {
      this.instance = instance;
      this.label = label;
      this.lights = lights;
    }

    @Nonnull KMeshInstanceTransformed getInstance()
    {
      return this.instance;
    }

    @Nonnull KMaterialForwardLabel getLabel()
    {
      return this.label;
    }

    @Nonnull List<KLight> getLights()
    {
      return this.lights;
    }

    @Override public
      <A, E extends Throwable, V extends KTranslucentVisitor<A, E>>
      A
      translucentAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          RException,
          ConstraintError
    {
      return v.translucentVisitLit(this);
    }
  }

  static final class KTranslucentUnlit extends KTranslucent
  {
    private final @Nonnull KMeshInstanceTransformed instance;
    private final @Nonnull KMaterialForwardLabel    label;

    KTranslucentUnlit(
      final @Nonnull KMeshInstanceTransformed instance,
      final @Nonnull KMaterialForwardLabel label)
    {
      this.instance = instance;
      this.label = label;
    }

    public @Nonnull KMeshInstanceTransformed getInstance()
    {
      return this.instance;
    }

    public @Nonnull KMaterialForwardLabel getLabel()
    {
      return this.label;
    }

    @Override public
      <A, E extends Throwable, V extends KTranslucentVisitor<A, E>>
      A
      translucentAccept(
        final @Nonnull V v)
        throws E,
          JCGLException,
          RException,
          ConstraintError
    {
      return v.translucentVisitUnlit(this);
    }
  }
}
