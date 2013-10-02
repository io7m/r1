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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;

/**
 * Object materials.
 */

@Immutable public final class KMaterial
{
  private final @Nonnull VectorI3F               diffuse;
  private final @Nonnull Option<Texture2DStatic> texture_diffuse_0;
  private final @Nonnull Option<Texture2DStatic> texture_diffuse_1;
  private final @Nonnull Option<Texture2DStatic> texture_normal;
  private final @Nonnull Option<Texture2DStatic> texture_specular;
  private final float                            specular_exponent;

  KMaterial(
    final @Nonnull VectorReadable3F diffuse,
    final @Nonnull Option<Texture2DStatic> texture_diffuse_0,
    final @Nonnull Option<Texture2DStatic> texture_diffuse_1,
    final @Nonnull Option<Texture2DStatic> texture_normal,
    final @Nonnull Option<Texture2DStatic> texture_specular,
    final float specular_exponent)
  {
    this.diffuse = new VectorI3F(diffuse);
    this.texture_diffuse_0 = texture_diffuse_0;
    this.texture_diffuse_1 = texture_diffuse_1;
    this.texture_normal = texture_normal;
    this.texture_specular = texture_specular;
    this.specular_exponent = specular_exponent;
  }

  public float getSpecularExponent()
  {
    return this.specular_exponent;
  }

  public @Nonnull VectorReadable3F getDiffuse()
  {
    return this.diffuse;
  }

  public @Nonnull Option<Texture2DStatic> getTextureDiffuse0()
  {
    return this.texture_diffuse_0;
  }

  public @Nonnull Option<Texture2DStatic> getTextureDiffuse1()
  {
    return this.texture_diffuse_1;
  }

  public @Nonnull Option<Texture2DStatic> getTextureNormal()
  {
    return this.texture_normal;
  }

  public @Nonnull Option<Texture2DStatic> getTextureSpecular()
  {
    return this.texture_specular;
  }
}
