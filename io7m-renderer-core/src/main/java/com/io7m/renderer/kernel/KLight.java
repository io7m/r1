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

import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;

/**
 * Lights.
 */

@Immutable public abstract class KLight
{
  @Immutable public static final class KDirectional extends KLight
  {
    private final @Nonnull RVectorReadable3F<RSpaceWorld> direction;

    @SuppressWarnings("synthetic-access") KDirectional(
      final @Nonnull Integer id,
      final @Nonnull RVectorReadable3F<RSpaceWorld> direction,
      final @Nonnull RVectorReadable3F<RSpaceRGB> colour,
      final float intensity)
    {
      super(Type.LIGHT_DIRECTIONAL, id, colour, intensity);
      this.direction = new RVectorI3F<RSpaceWorld>(direction);
    }

    public @Nonnull RVectorReadable3F<RSpaceWorld> getDirection()
    {
      return this.direction;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + this.direction.hashCode();
      return result;
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
      final KDirectional other = (KDirectional) obj;
      if (!this.direction.equals(other.direction)) {
        return false;
      }
      return true;
    }
  }

  @Immutable public static final class KProjective extends KLight
  {
    private final @Nonnull Texture2DStaticUsable              texture;
    private final @Nonnull RVectorReadable3F<RSpaceWorld>     position;
    private final @Nonnull QuaternionI4F                      orientation;
    private final float                                       range;
    private final float                                       falloff;
    private final @Nonnull RMatrixI4x4F<RTransformProjection> projection;

    @SuppressWarnings("synthetic-access") KProjective(
      final @Nonnull Integer id,
      final @Nonnull Texture2DStaticUsable texture,
      final @Nonnull RVectorReadable3F<RSpaceWorld> position,
      final @Nonnull QuaternionI4F orientation,
      final @Nonnull RVectorReadable3F<RSpaceRGB> colour,
      final float intensity,
      final float range,
      final float falloff,
      final @Nonnull RMatrixI4x4F<RTransformProjection> projection)
    {
      super(Type.LIGHT_PROJECTIVE, id, colour, intensity);
      this.position = position;
      this.orientation = orientation;
      this.range = range;
      this.falloff = falloff;
      this.projection = projection;
      this.texture = texture;
    }

    public float getFalloff()
    {
      return this.falloff;
    }

    public @Nonnull QuaternionI4F getOrientation()
    {
      return this.orientation;
    }

    public @Nonnull RVectorReadable3F<RSpaceWorld> getPosition()
    {
      return this.position;
    }

    public @Nonnull RMatrixI4x4F<RTransformProjection> getProjection()
    {
      return this.projection;
    }

    public float getRange()
    {
      return this.range;
    }

    public @Nonnull Texture2DStaticUsable getTexture()
    {
      return this.texture;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + Float.floatToIntBits(this.falloff);
      result = (prime * result) + this.orientation.hashCode();
      result = (prime * result) + this.position.hashCode();
      result = (prime * result) + this.projection.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.range);
      result = (prime * result) + this.texture.hashCode();
      return result;
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
      final KProjective other = (KProjective) obj;
      if (Float.floatToIntBits(this.falloff) != Float
        .floatToIntBits(other.falloff)) {
        return false;
      }
      if (!this.orientation.equals(other.orientation)) {
        return false;
      }
      if (!this.position.equals(other.position)) {
        return false;
      }
      if (!this.projection.equals(other.projection)) {
        return false;
      }
      if (Float.floatToIntBits(this.range) != Float
        .floatToIntBits(other.range)) {
        return false;
      }
      if (!this.texture.equals(other.texture)) {
        return false;
      }
      return true;
    }
  }

  @Immutable public static final class KSphere extends KLight
  {
    private final @Nonnull RVectorReadable3F<RSpaceWorld>                        position;
    private final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius;
    private final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float           falloff;

    @SuppressWarnings("synthetic-access") KSphere(
      final @Nonnull Integer id,
      final @Nonnull RVectorReadable3F<RSpaceRGB> colour,
      final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity,
      final @Nonnull RVectorReadable3F<RSpaceWorld> position,
      final @KSuggestedRangeF(lower = 1.0f, upper = Float.MAX_VALUE) float radius,
      final @KSuggestedRangeF(lower = 1.0f, upper = 64.0f) float falloff)
    {
      super(Type.LIGHT_SPHERE, id, colour, intensity);
      this.position = new RVectorI3F<RSpaceWorld>(position);
      this.radius = radius;
      this.falloff = falloff;
    }

    public float getFalloff()
    {
      return this.falloff;
    }

    public @Nonnull RVectorReadable3F<RSpaceWorld> getPosition()
    {
      return this.position;
    }

    public float getRadius()
    {
      return this.radius;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + Float.floatToIntBits(this.falloff);
      result = (prime * result) + this.position.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.radius);
      return result;
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
      final KSphere other = (KSphere) obj;
      if (Float.floatToIntBits(this.falloff) != Float
        .floatToIntBits(other.falloff)) {
        return false;
      }
      if (!this.position.equals(other.position)) {
        return false;
      }
      if (Float.floatToIntBits(this.radius) != Float
        .floatToIntBits(other.radius)) {
        return false;
      }
      return true;
    }
  }

  static enum Type
  {
    LIGHT_SPHERE("Sphere", "LS"),
    LIGHT_PROJECTIVE("Projective", "LP"),
    LIGHT_DIRECTIONAL("Directional", "LD");

    private final @Nonnull String name;
    private final @Nonnull String code;

    private Type(
      final @Nonnull String name,
      final @Nonnull String code)
    {
      this.name = name;
      this.code = code;
    }

    public @Nonnull String getCode()
    {
      return this.code;
    }

    public @Nonnull String getName()
    {
      return this.name;
    }
  }

  private final @Nonnull Integer                                    id;
  private final @Nonnull Type                                       type;
  private final @Nonnull RVectorI3F<RSpaceRGB>                      colour;
  private final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity;

  private KLight(
    final @Nonnull Type type,
    final @Nonnull Integer id,
    final @Nonnull RVectorReadable3F<RSpaceRGB> color,
    final @KSuggestedRangeF(lower = 0.0f, upper = 1.0f) float intensity)
  {
    this.id = id;
    this.type = type;
    this.colour = new RVectorI3F<RSpaceRGB>(color);
    this.intensity = intensity;
  }

  public @Nonnull RVectorI3F<RSpaceRGB> getColour()
  {
    return this.colour;
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public float getIntensity()
  {
    return this.intensity;
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }
}
