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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.TextureCubeStaticUsable;

/**
 * Material properties related to surface environment mapping.
 */

@Immutable public final class KMaterialEnvironment implements
  KTexturesRequiredType
{
  private static final @Nonnull KMaterialEnvironment EMPTY;

  static {
    final Option<TextureCubeStaticUsable> none = Option.none();
    try {
      EMPTY = new KMaterialEnvironment(0.0f, none, false);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Construct new environment mapping properties.
   * 
   * @param in_mix
   *          The mix factor between the reflected environment and the rest of
   *          the material
   * @param in_texture
   *          The environment map
   * @param in_mix_mapped
   *          Whether or not to sample the environment intensity from the
   *          specular map
   * @return New environment mapping properties
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KMaterialEnvironment newEnvironmentMapped(
    final float in_mix,
    final @Nonnull TextureCubeStaticUsable in_texture,
    final boolean in_mix_mapped)
    throws ConstraintError
  {
    return new KMaterialEnvironment(in_mix, Option.some(Constraints
      .constrainNotNull(in_texture, "Texture")), in_mix_mapped);
  }

  /**
   * @return New environment mapping parameters representing a lack of
   *         environment mapping
   */

  public static @Nonnull KMaterialEnvironment newEnvironmentUnmapped()
  {
    return KMaterialEnvironment.EMPTY;
  }

  private final float                                    mix;
  private final boolean                                  mix_mapped;
  private final @Nonnull Option<TextureCubeStaticUsable> texture;
  private final int                                      textures_required;

  private KMaterialEnvironment(
    final float in_mix,
    final @Nonnull Option<TextureCubeStaticUsable> in_texture,
    final boolean in_mix_mapped)
    throws ConstraintError
  {
    this.mix = in_mix;
    this.texture = Constraints.constrainNotNull(in_texture, "Texture");
    this.mix_mapped = in_mix_mapped;
    this.textures_required = this.texture.isSome() ? 1 : 0;
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KMaterialEnvironment other = (KMaterialEnvironment) obj;
    return (Float.floatToIntBits(this.mix) == Float.floatToIntBits(other.mix))
      && (this.mix_mapped == other.mix_mapped)
      && (this.texture.equals(other.texture));
  }

  /**
   * @return The mix factor between the surface albedo and environment map,
   *         with <code>0.0</code> resulting in no environment map, and
   *         <code>1.0</code> resulting in the surface albedo being taken only
   *         from the environment map.
   */

  public float getMix()
  {
    return this.mix;
  }

  /**
   * @return <code>true</code> if the environment mix will be sampled from the
   *         specular map, if any.
   */

  public boolean getMixMapped()
  {
    return this.mix_mapped;
  }

  /**
   * @return The environment cube map texture, if any
   */

  public @Nonnull Option<TextureCubeStaticUsable> getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + Float.floatToIntBits(this.mix);
    result = (prime * result) + (this.mix_mapped ? 1234 : 4321);
    result = (prime * result) + (this.texture.hashCode());
    return result;
  }

  @Override public int texturesGetRequired()
  {
    return this.textures_required;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[KMaterialEnvironment mix=");
    builder.append(this.mix);
    builder.append(" mix_mapped=");
    builder.append(this.mix_mapped);
    builder.append(" texture=");
    builder.append(this.texture);
    builder.append("]");
    return builder.toString();
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param t
   *          The new map
   * @return The current material with <code>map == t</code>.
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public @Nonnull KMaterialEnvironment withMap(
    final @Nonnull TextureCubeStaticUsable t)
    throws ConstraintError
  {
    return KMaterialEnvironment.newEnvironmentMapped(
      this.mix,
      t,
      this.mix_mapped);
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @param m
   *          The new mix value
   * @return The current material with <code>mix == m</code>.
   */

  public @Nonnull KMaterialEnvironment withMix(
    final float m)
  {
    try {
      return new KMaterialEnvironment(m, this.texture, this.mix_mapped);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @return The current material with specular map sampling enabled
   */

  public @Nonnull KMaterialEnvironment withMixMapped()
  {
    try {
      return new KMaterialEnvironment(this.mix, this.texture, true);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @return The current material without a map
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  @SuppressWarnings("static-method") public @Nonnull
    KMaterialEnvironment
    withoutMap()
      throws ConstraintError
  {
    return KMaterialEnvironment.newEnvironmentUnmapped();
  }

  /**
   * Return a material representing the current material with the given
   * modification.
   * 
   * @return The current material without specular map sampling enabled
   */

  public @Nonnull KMaterialEnvironment withoutMixMapped()
  {
    try {
      return new KMaterialEnvironment(this.mix, this.texture, false);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }
}
