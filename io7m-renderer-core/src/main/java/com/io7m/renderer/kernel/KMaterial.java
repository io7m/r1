/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;

/**
 * Object materials.
 */

@Immutable final class KMaterial
{
  private final @Nonnull VectorI3F               diffuse;
  private final @Nonnull List<Texture2DStatic>   diffuse_maps;
  private final @Nonnull Option<Texture2DStatic> normal_map;
  private final @Nonnull Option<Texture2DStatic> specular_map;

  KMaterial(
    final @Nonnull VectorReadable3F diffuse,
    final @Nonnull List<Texture2DStatic> diffuse_maps,
    final @Nonnull Option<Texture2DStatic> normal_map,
    final @Nonnull Option<Texture2DStatic> specular_map)
  {
    this.diffuse = new VectorI3F(diffuse);
    this.diffuse_maps = diffuse_maps;
    this.normal_map = normal_map;
    this.specular_map = specular_map;
  }

  @Nonnull VectorReadable3F getDiffuse()
  {
    return this.diffuse;
  }

  @Nonnull List<Texture2DStatic> getDiffuseMaps()
  {
    return this.diffuse_maps;
  }

  @Nonnull Option<Texture2DStatic> getNormalMap()
  {
    return this.normal_map;
  }

  @Nonnull Option<Texture2DStatic> getSpecularMap()
  {
    return this.specular_map;
  }
}
