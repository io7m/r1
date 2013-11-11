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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option.None;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformProjection;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.KLight.Type;

abstract class SBLightDescription
{
  @Immutable static final class SBLightDescriptionDirectional extends
    SBLightDescription
  {
    private final @Nonnull KLight.KDirectional actual;

    @SuppressWarnings("synthetic-access") SBLightDescriptionDirectional(
      final @Nonnull KLight.KDirectional actual)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_DIRECTIONAL);
      this.actual = actual;
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
      final SBLightDescriptionDirectional other =
        (SBLightDescriptionDirectional) obj;
      if (!this.actual.equals(other.actual)) {
        return false;
      }
      return true;
    }

    @Override public Integer getID()
    {
      return this.actual.getID();
    }

    public KDirectional getLight()
    {
      return this.actual;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result =
        (prime * result)
          + ((this.actual == null) ? 0 : this.actual.hashCode());
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SBLightDescriptionDirectional ");
      builder.append(this.actual);
      builder.append("]");
      return builder.toString();
    }
  }

  @Immutable static final class SBLightDescriptionProjective extends
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

    @SuppressWarnings("synthetic-access") SBLightDescriptionProjective(
      final @Nonnull QuaternionI4F orientation,
      final @Nonnull RVectorI3F<RSpaceWorld> position,
      final float falloff,
      final @Nonnull SBProjectionDescription projection,
      final @Nonnull PathVirtual texture,
      final @Nonnull RVectorI3F<RSpaceRGB> colour,
      final float intensity,
      final @Nonnull Integer id)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_PROJECTIVE);

      this.orientation = orientation;
      this.position = position;
      this.falloff = falloff;
      this.texture = texture;
      this.colour = colour;
      this.intensity = intensity;
      this.id = id;
      this.projection = projection;

      final MatrixM4x4F temporary = new MatrixM4x4F();
      this.projection_matrix = projection.makeProjectionMatrix(temporary);
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
      if (!this.texture.equals(other.texture)) {
        return false;
      }
      return true;
    }

    public RVectorI3F<RSpaceRGB> getColour()
    {
      return this.colour;
    }

    public float getFalloff()
    {
      return this.falloff;
    }

    public Integer getId()
    {
      return this.id;
    }

    @Override public Integer getID()
    {
      return this.id;
    }

    public float getIntensity()
    {
      return this.intensity;
    }

    KProjective getLight(
      final @Nonnull SBTexture2D<SBTexture2DKindAlbedo> t)
    {
      return KLight.KProjective.make(
        this.id,
        t.getTexture(),
        this.position,
        this.orientation,
        this.colour,
        this.intensity,
        (float) this.projection.getFar(),
        this.falloff,
        this.projection_matrix,
        new None<KShadow>());
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
      result = (prime * result) + this.colour.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.falloff);
      result = (prime * result) + this.id.hashCode();
      result = (prime * result) + Float.floatToIntBits(this.intensity);
      result = (prime * result) + this.orientation.hashCode();
      result = (prime * result) + this.position.hashCode();
      result = (prime * result) + this.projection.hashCode();
      result = (prime * result) + this.projection_matrix.hashCode();
      result = (prime * result) + this.texture.hashCode();
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SBLightDescriptionProjective ");
      builder.append(this.orientation);
      builder.append(" position=");
      builder.append(this.position);
      builder.append(" falloff=");
      builder.append(this.falloff);
      builder.append(" texture=");
      builder.append(this.texture);
      builder.append(" colour=");
      builder.append(this.colour);
      builder.append(" intensity=");
      builder.append(this.intensity);
      builder.append(" id=");
      builder.append(this.id);
      builder.append(" projection=");
      builder.append(this.projection);
      builder.append(" projection_matrix=");
      builder.append(this.projection_matrix);
      builder.append("]");
      return builder.toString();
    }
  }

  @Immutable static final class SBLightDescriptionSpherical extends
    SBLightDescription
  {
    private final @Nonnull KLight.KSphere actual;

    @SuppressWarnings("synthetic-access") SBLightDescriptionSpherical(
      final @Nonnull KLight.KSphere actual)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_SPHERE);
      this.actual = actual;
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
      final SBLightDescriptionSpherical other =
        (SBLightDescriptionSpherical) obj;
      if (this.actual == null) {
        if (other.actual != null) {
          return false;
        }
      } else if (!this.actual.equals(other.actual)) {
        return false;
      }
      return true;
    }

    @Override public Integer getID()
    {
      return this.actual.getID();
    }

    public KSphere getLight()
    {
      return this.actual;
    }

    @Override public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result =
        (prime * result)
          + ((this.actual == null) ? 0 : this.actual.hashCode());
      return result;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[SBLightDescriptionSpherical ");
      builder.append(this.actual);
      builder.append("]");
      return builder.toString();
    }
  }

  private final @Nonnull KLight.Type type;

  private SBLightDescription(
    final @Nonnull KLight.Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  abstract public @Nonnull Integer getID();

  public @Nonnull Type getType()
  {
    return this.type;
  }
}
