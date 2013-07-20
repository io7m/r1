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

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;

public final class SBObjectDescription
{
  private final @Nonnull Integer                 id;
  private final @Nonnull RVectorI3F<RSpaceWorld> position;
  private final @Nonnull RVectorI3F<SBDegrees>   orientation;
  private final @CheckForNull File               diffuse_texture;
  private final @CheckForNull File               normal_texture;
  private final @CheckForNull File               specular_texture;
  private final @Nonnull File                    model;
  private final @Nonnull String                  model_object;

  SBObjectDescription(
    final @Nonnull Integer id,
    final @Nonnull File model,
    final @Nonnull String model_object,
    final @Nonnull RVectorI3F<RSpaceWorld> position,
    final @Nonnull RVectorI3F<SBDegrees> orientation,
    final @CheckForNull File diffuse_texture,
    final @CheckForNull File normal_texture,
    final @CheckForNull File specular_texture)
  {
    this.id = id;
    this.model = model;
    this.model_object = model_object;
    this.position = position;
    this.orientation = orientation;
    this.diffuse_texture = diffuse_texture;
    this.normal_texture = normal_texture;
    this.specular_texture = specular_texture;
  }

  @CheckForNull File getDiffuseTexture()
  {
    return this.diffuse_texture;
  }

  @Nonnull Integer getID()
  {
    return this.id;
  }

  @Nonnull File getModel()
  {
    return this.model;
  }

  @Nonnull String getModelObject()
  {
    return this.model_object;
  }

  @CheckForNull File getNormalTexture()
  {
    return this.normal_texture;
  }

  RVectorReadable3F<SBDegrees> getOrientation()
  {
    return this.orientation;
  }

  RVectorReadable3F<RSpaceWorld> getPosition()
  {
    return this.position;
  }

  @CheckForNull File getSpecularTexture()
  {
    return this.specular_texture;
  }

  @Override public String toString()
  {
    return "[SBObjectDescription " + this.id + "]";
  }
}
