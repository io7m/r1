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

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionUserError;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RVectorI3F;

final class SBLightDescriptionProjective implements SBLightDescription
{
  private final QuaternionI4F                          orientation;
  private final RVectorI3F<RSpaceWorldType>            position;
  private final float                                  falloff;
  private final PathVirtual                            texture;
  private final RVectorI3F<RSpaceRGBType>              colour;
  private final float                                  intensity;
  private final Integer                                id;
  private final SBProjectionDescription                projection;
  private final RMatrixI4x4F<RTransformProjectionType> projection_matrix;
  private final OptionType<KShadowType>                shadow;

  SBLightDescriptionProjective(
    final QuaternionI4F in_orientation,
    final RVectorI3F<RSpaceWorldType> in_position,
    final float in_falloff,
    final SBProjectionDescription in_projection,
    final PathVirtual in_texture,
    final RVectorI3F<RSpaceRGBType> in_colour,
    final float in_intensity,
    final OptionType<KShadowType> in_shadow,
    final Integer in_id)
  {
    this.orientation = in_orientation;
    this.position = in_position;
    this.falloff = in_falloff;
    this.texture = in_texture;
    this.colour = in_colour;
    this.intensity = in_intensity;
    this.id = in_id;
    this.projection = in_projection;
    this.shadow = in_shadow;

    final MatrixM4x4F temporary = new MatrixM4x4F();
    this.projection_matrix = in_projection.makeProjectionMatrix(temporary);
  }

  @Override public boolean equals(
    final @Nullable Object obj)
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
    final SBLightDescriptionProjective other =
      (SBLightDescriptionProjective) obj;
    if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (Float.floatToIntBits(this.falloff) != Float
      .floatToIntBits(other.falloff)) {
      return false;
    }
    if (!this.id.equals(other.id)) {
      return false;
    }
    if (Float.floatToIntBits(this.intensity) != Float
      .floatToIntBits(other.intensity)) {
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
    if (!this.projection_matrix.equals(other.projection_matrix)) {
      return false;
    }
    if (!this.shadow.equals(other.shadow)) {
      return false;
    }
    if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public OptionType<KShadowType> getShadow()
  {
    return this.shadow;
  }

  public RVectorI3F<RSpaceRGBType> getColour()
  {
    return this.colour;
  }

  public float getFalloff()
  {
    return this.falloff;
  }

  public Integer getID()
  {
    return this.id;
  }

  public float getIntensity()
  {
    return this.intensity;
  }

  KLightProjective getLight(
    final SBTexture2D<SBTexture2DKindAlbedo> t)
    throws RExceptionUserError,
      RException
  {
    final KLightProjectiveBuilderType b = KLightProjective.newBuilder();
    b.setColor(this.colour);
    b.setFalloff(this.falloff);
    b.setIntensity(this.intensity);
    b.setOrientation(this.orientation);
    b.setPosition(this.position);
    b.setProjection(this.projection_matrix);
    b.setRange((float) this.projection.getFar());

    this.getShadow().accept(new OptionVisitorType<KShadowType, Unit>() {
      @Override public Unit none(
        final None<KShadowType> n)
      {
        b.setNoShadow();
        return Unit.unit();
      }

      @Override public Unit some(
        final Some<KShadowType> s)
      {
        b.setShadow(s.get());
        return Unit.unit();
      }
    });

    b.setTexture(t.getTexture());
    return b.build();
  }

  public QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  public RVectorI3F<RSpaceWorldType> getPosition()
  {
    return this.position;
  }

  public SBProjectionDescription getProjection()
  {
    return this.projection;
  }

  public RMatrixI4x4F<RTransformProjectionType> getProjectionMatrix()
  {
    return this.projection_matrix;
  }

  PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.colour.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.falloff);
    result = (prime * result) + this.id.hashCode();
    result = (prime * result) + Float.floatToIntBits(this.intensity);
    result = (prime * result) + this.orientation.hashCode();
    result = (prime * result) + this.position.hashCode();
    result = (prime * result) + this.projection.hashCode();
    result = (prime * result) + this.projection_matrix.hashCode();
    result = (prime * result) + this.shadow.hashCode();
    result = (prime * result) + this.texture.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("SBLightDescriptionProjective [orientation=");
    builder.append(this.orientation);
    builder.append(", position=");
    builder.append(this.position);
    builder.append(", falloff=");
    builder.append(this.falloff);
    builder.append(", texture=");
    builder.append(this.texture);
    builder.append(", colour=");
    builder.append(this.colour);
    builder.append(", intensity=");
    builder.append(this.intensity);
    builder.append(", id=");
    builder.append(this.id);
    builder.append(", projection=");
    builder.append(this.projection);
    builder.append(", projection_matrix=");
    builder.append(this.projection_matrix);
    builder.append(", shadow=");
    builder.append(this.shadow);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }

  @Override public
    <A, E extends Throwable, V extends SBLightDescriptionVisitor<A, E>>
    A
    lightDescriptionVisitableAccept(
      final V v)
      throws RException,
        E
  {
    return v.lightVisitProjective(this);
  }

  @Override public Integer lightGetID()
  {
    return this.id;
  }
}
