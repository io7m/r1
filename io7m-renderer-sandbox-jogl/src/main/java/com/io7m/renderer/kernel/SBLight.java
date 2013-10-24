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
import com.io7m.renderer.kernel.KLight.KDirectional;
import com.io7m.renderer.kernel.KLight.KProjective;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionDirectional;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionProjective;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionSpherical;

abstract class SBLight
{
  @Immutable public static final class SBLightDirectional extends SBLight
  {
    private final @Nonnull SBLightDescriptionDirectional description;
    private final @Nonnull KDirectional                  light;

    @SuppressWarnings("synthetic-access") SBLightDirectional(
      final @Nonnull SBLightDescription.SBLightDescriptionDirectional d)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_DIRECTIONAL);
      this.description = d;
      this.light = d.getLight();
    }

    @Override SBLightDescription getDescription()
    {
      return this.description;
    }

    @Override KDirectional getLight()
    {
      return this.light;
    }

    @Override Integer getID()
    {
      return this.light.getID();
    }
  }

  @Immutable public static final class SBLightProjective extends SBLight
  {
    private final @Nonnull SBLightDescriptionProjective description;
    private final @Nonnull KProjective                  light;

    @SuppressWarnings("synthetic-access") SBLightProjective(
      final @Nonnull SBLightDescription.SBLightDescriptionProjective d,
      final @Nonnull KProjective light)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_PROJECTIVE);
      this.description = d;
      this.light = light;
    }

    @Override SBLightDescriptionProjective getDescription()
    {
      return this.description;
    }

    @Override KProjective getLight()
    {
      return this.light;
    }

    @Override Integer getID()
    {
      return this.light.getID();
    }
  }

  @Immutable public static final class SBLightSpherical extends SBLight
  {
    private final @Nonnull SBLightDescriptionSpherical description;
    private final @Nonnull KSphere                     light;

    @SuppressWarnings("synthetic-access") SBLightSpherical(
      final @Nonnull SBLightDescription.SBLightDescriptionSpherical d)
      throws ConstraintError
    {
      super(KLight.Type.LIGHT_SPHERE);
      this.description = d;
      this.light = d.getLight();
    }

    @Override SBLightDescription getDescription()
    {
      return this.description;
    }

    @Override KSphere getLight()
    {
      return this.light;
    }

    @Override Integer getID()
    {
      return this.light.getID();
    }
  }

  private final @Nonnull KLight.Type type;

  private SBLight(
    final @Nonnull KLight.Type type)
    throws ConstraintError
  {
    this.type = Constraints.constrainNotNull(type, "Type");
  }

  abstract @Nonnull SBLightDescription getDescription();

  abstract @Nonnull KLight getLight();

  public KLight.Type getType()
  {
    return this.type;
  }

  abstract @Nonnull Integer getID();
}
