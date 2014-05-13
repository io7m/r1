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

import com.io7m.jnull.Nullable;
import com.io7m.renderer.types.RException;

public final class SBMaterialTranslucentRegular implements
  SBMaterialTranslucent
{
  private final SBMaterialDescriptionTranslucentRegular        description;
  private final Integer                                        id;
  private final @Nullable SBTexture2D<SBTexture2DKindAlbedo>   map_diffuse;
  private final @Nullable SBTexture2D<SBTexture2DKindEmissive> map_emissive;
  private final @Nullable SBTextureCube                        map_environment;
  private final @Nullable SBTexture2D<SBTexture2DKindNormal>   map_normal;
  private final @Nullable SBTexture2D<SBTexture2DKindSpecular> map_specular;

  public SBMaterialTranslucentRegular(
    final Integer in_id,
    final SBMaterialDescriptionTranslucentRegular in_description,
    final @Nullable SBTexture2D<SBTexture2DKindAlbedo> in_map_diffuse,
    final @Nullable SBTexture2D<SBTexture2DKindEmissive> in_map_emissive,
    final @Nullable SBTextureCube in_map_environment,
    final @Nullable SBTexture2D<SBTexture2DKindNormal> in_map_normal,
    final @Nullable SBTexture2D<SBTexture2DKindSpecular> in_map_specular)
  {
    this.id = in_id;
    this.description = in_description;
    this.map_diffuse = in_map_diffuse;
    this.map_emissive = in_map_emissive;
    this.map_normal = in_map_normal;
    this.map_specular = in_map_specular;
    this.map_environment = in_map_environment;
  }

  @Override public
    SBMaterialDescriptionTranslucentRegular
    materialGetDescription()
  {
    return this.description;
  }

  @Override public Integer materialGetID()
  {
    return this.id;
  }

  @Override public String materialGetName()
  {
    return this.description.materialDescriptionGetName();
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialTranslucentVisitor<A, E>>
    A
    materialTranslucentVisitableAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialVisitTranslucentRegular(this);
  }

  @Override public
    <A, E extends Throwable, V extends SBMaterialVisitor<A, E>>
    A
    materialVisitableAccept(
      final V v)
      throws E,
        RException
  {
    return v.materialVisitTranslucent(this);
  }
}
