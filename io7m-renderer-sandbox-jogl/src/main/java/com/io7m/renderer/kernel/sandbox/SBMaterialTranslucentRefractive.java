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

package com.io7m.renderer.kernel.sandbox;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.types.RException;

public final class SBMaterialTranslucentRefractive implements
  SBMaterialTranslucent
{
  private final @Nonnull SBMaterialDescriptionTranslucentRefractive description;
  private final @Nonnull Integer                                    id;
  private final @CheckForNull SBTexture2D<SBTexture2DKindNormal>    map_normal;

  public SBMaterialTranslucentRefractive(
    final @Nonnull Integer in_id,
    final @Nonnull SBMaterialDescriptionTranslucentRefractive in_description,
    final @CheckForNull SBTexture2D<SBTexture2DKindNormal> in_map_normal)
  {
    this.id = in_id;
    this.description = in_description;
    this.map_normal = in_map_normal;
  }

  @Override public @Nonnull Integer materialGetID()
  {
    return this.id;
  }

  @Override public @Nonnull String materialGetName()
  {
    return this.description.materialDescriptionGetName();
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialTranslucentVisitor<A, E>>
    A
    materialTranslucentVisitableAccept(
      final V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialVisitTranslucentRefractive(this);
  }

  @Override public
    SBMaterialDescriptionTranslucentRefractive
    materialGetDescription()
  {
    return this.description;
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialVisitor<A, E>>
    A
    materialVisitableAccept(
      final V v)
      throws E,
        RException,
        ConstraintError
  {
    return v.materialVisitTranslucent(this);
  }
}
