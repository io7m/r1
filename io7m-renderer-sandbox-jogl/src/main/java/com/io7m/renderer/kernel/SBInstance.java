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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;

@Immutable final class SBInstance
{
  private final @Nonnull SBInstanceDescription description;
  private final @CheckForNull SBTexture        map_diffuse;
  private final @CheckForNull SBTexture        map_normal;
  private final @CheckForNull SBTexture        map_specular;

  public SBInstance(
    final @Nonnull SBInstanceDescription description,
    final @CheckForNull SBTexture map_diffuse,
    final @CheckForNull SBTexture map_normal,
    final @CheckForNull SBTexture map_specular)
  {
    this.description = description;
    this.map_diffuse = map_diffuse;
    this.map_normal = map_normal;
    this.map_specular = map_specular;
  }

  public @Nonnull SBInstanceDescription getDescription()
  {
    return this.description;
  }

  public @CheckForNull SBTexture getDiffuseMap()
  {
    return this.map_diffuse;
  }

  public @Nonnull Integer getID()
  {
    return this.description.getID();
  }

  public @Nonnull String getMesh()
  {
    return this.description.getMesh();
  }

  public @CheckForNull SBTexture getNormalMap()
  {
    return this.map_normal;
  }

  public @Nonnull RVectorI3F<SBDegrees> getOrientation()
  {
    return this.description.getOrientation();
  }

  public @Nonnull RVectorI3F<RSpaceWorld> getPosition()
  {
    return this.description.getPosition();
  }

  public @CheckForNull SBTexture getSpecularMap()
  {
    return this.map_specular;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBInstance  ");
    builder.append(this.getID());
    builder.append("]");
    return builder.toString();
  }
}
