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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KShadow;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RSpaceRGB;
import com.io7m.renderer.types.RSpaceWorld;
import com.io7m.renderer.types.RTransformProjection;
import com.io7m.renderer.types.RVectorI3F;

@Immutable final class SBLightDescriptionProjective implements
  SBLightDescription
{
  private final @Nonnull QuaternionI4F                      orientation;
  private final @Nonnull RVectorI3F<RSpaceWorld>            position;
  private final float                                       falloff;
  private final @Nonnull PathVirtual                        texture;
  private final @Nonnull RVectorI3F<RSpaceRGB>              colour;
  private final float                                       intensity;
  private final @Nonnull Integer                            id;
  private final @Nonnull SBProjectionDescription            projection;
  private final @Nonnull RMatrixI4x4F<RTransformProjection> projection_matrix;
  private final @Nonnull Option<KShadow>                    shadow;

  SBLightDescriptionProjective(
    final @Nonnull QuaternionI4F in_orientation,
    final @Nonnull RVectorI3F<RSpaceWorld> in_position,
    final float in_falloff,
    final @Nonnull SBProjectionDescription in_projection,
    final @Nonnull PathVirtual in_texture,
    final @Nonnull RVectorI3F<RSpaceRGB> in_colour,
    final float in_intensity,
    final @Nonnull Option<KShadow> in_shadow,
    final @Nonnull Integer in_id)
    throws ConstraintError
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
    final SBLightDescriptionProjective other =
      (SBLightDescriptionProjective) obj;
    if (this.colour == null) {
      if (other.colour != null) {
        return false;
      }
    } else if (!this.colour.equals(other.colour)) {
      return false;
    }
    if (Float.floatToIntBits(this.falloff) != Float
      .floatToIntBits(other.falloff)) {
      return false;
    }
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    if (Float.floatToIntBits(this.intensity) != Float
      .floatToIntBits(other.intensity)) {
      return false;
    }
    if (this.orientation == null) {
      if (other.orientation != null) {
        return false;
      }
    } else if (!this.orientation.equals(other.orientation)) {
      return false;
    }
    if (this.position == null) {
      if (other.position != null) {
        return false;
      }
    } else if (!this.position.equals(other.position)) {
      return false;
    }
    if (this.projection == null) {
      if (other.projection != null) {
        return false;
      }
    } else if (!this.projection.equals(other.projection)) {
      return false;
    }
    if (this.projection_matrix == null) {
      if (other.projection_matrix != null) {
        return false;
      }
    } else if (!this.projection_matrix.equals(other.projection_matrix)) {
      return false;
    }
    if (this.shadow == null) {
      if (other.shadow != null) {
        return false;
      }
    } else if (!this.shadow.equals(other.shadow)) {
      return false;
    }
    if (this.texture == null) {
      if (other.texture != null) {
        return false;
      }
    } else if (!this.texture.equals(other.texture)) {
      return false;
    }
    return true;
  }

  public Option<KShadow> getShadow()
  {
    return this.shadow;
  }

  public RVectorI3F<RSpaceRGB> getColour()
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
    final @Nonnull SBTexture2D<SBTexture2DKindAlbedo> t)
    throws ConstraintError
  {
    return KLightProjective.newProjective(
      this.id,
      t.getTexture(),
      this.position,
      this.orientation,
      this.colour,
      this.intensity,
      (float) this.projection.getFar(),
      this.falloff,
      this.projection_matrix,
      this.getShadow());
  }

  public QuaternionI4F getOrientation()
  {
    return this.orientation;
  }

  public RVectorI3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  public @Nonnull SBProjectionDescription getProjection()
  {
    return this.projection;
  }

  public @Nonnull RMatrixI4x4F<RTransformProjection> getProjectionMatrix()
  {
    return this.projection_matrix;
  }

  @Nonnull PathVirtual getTexture()
  {
    return this.texture;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result) + ((this.colour == null) ? 0 : this.colour.hashCode());
    result = (prime * result) + Float.floatToIntBits(this.falloff);
    result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
    result = (prime * result) + Float.floatToIntBits(this.intensity);
    result =
      (prime * result)
        + ((this.orientation == null) ? 0 : this.orientation.hashCode());
    result =
      (prime * result)
        + ((this.position == null) ? 0 : this.position.hashCode());
    result =
      (prime * result)
        + ((this.projection == null) ? 0 : this.projection.hashCode());
    result =
      (prime * result)
        + ((this.projection_matrix == null) ? 0 : this.projection_matrix
          .hashCode());
    result =
      (prime * result) + ((this.shadow == null) ? 0 : this.shadow.hashCode());
    result =
      (prime * result)
        + ((this.texture == null) ? 0 : this.texture.hashCode());
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
    return builder.toString();
  }

  @Override public
    <A, E extends Throwable, V extends SBLightDescriptionVisitor<A, E>>
    A
    lightDescriptionVisitableAccept(
      final V v)
      throws ConstraintError,
        RException,
        E
  {
    return v.lightVisitProjective(this);
  }

  @Override public Integer lightGetID()
  {
    return this.id;
  }
}
