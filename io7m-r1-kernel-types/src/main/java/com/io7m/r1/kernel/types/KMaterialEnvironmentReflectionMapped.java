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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.TextureCubeStaticUsableType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.types.RException;

/**
 * <p>
 * An environment-mapped reflection with the environment multiplied by the
 * specular map.
 * </p>
 * <p>
 * Note that this obviously requires the material to have a specular map
 * assigned.
 * </p>
 */

@EqualityReference public final class KMaterialEnvironmentReflectionMapped implements
  KMaterialEnvironmentType
{
  /**
   * Construct a new environment reflection.
   * 
   * @param in_mix
   *          The mix factor, with <code>1.0</code> resulting in only the
   *          environment being visible, and <code>0</code> resulting in only
   *          the underlying texture being visible.
   * @param in_texture
   *          The environment texture.
   * @return Environment properties.
   */

  public static KMaterialEnvironmentReflectionMapped reflectionMapped(
    final float in_mix,
    final TextureCubeStaticUsableType in_texture)
  {
    return new KMaterialEnvironmentReflectionMapped(in_mix, in_texture);
  }

  private final float                       mix;
  private final TextureCubeStaticUsableType texture;

  private KMaterialEnvironmentReflectionMapped(
    final float in_mix,
    final TextureCubeStaticUsableType in_texture)
  {
    this.mix = in_mix;
    this.texture = NullCheck.notNull(in_texture, "Texture");
  }

  @Override public String codeGet()
  {
    return "EnvRM";
  }

  @Override public
    <A, E extends Throwable, V extends KMaterialEnvironmentVisitorType<A, E>>
    A
    environmentAccept(
      final V v)
      throws E,
        RException
  {
    return v.reflectionMapped(this);
  }

  /**
   * @return The mix factor.
   */

  public float getMix()
  {
    return this.mix;
  }

  /**
   * @return The texture.
   */

  public TextureCubeStaticUsableType getTexture()
  {
    return this.texture;
  }

  @Override public boolean materialRequiresUVCoordinates()
  {
    return true;
  }

  @Override public int texturesGetRequired()
  {
    return 1;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("[KMaterialEnvironmentReflectionMapped mix=");
    b.append(this.mix);
    b.append(" texture=");
    b.append(this.texture);
    b.append("]");
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
