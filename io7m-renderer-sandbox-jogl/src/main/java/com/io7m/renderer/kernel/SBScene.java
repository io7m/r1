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

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

@Immutable final class SBScene
{
  public static @Nonnull SBScene empty()
  {
    final PMap<String, SBTexture> textures = HashTreePMap.empty();
    final PMap<String, SBModel> meshes = HashTreePMap.empty();
    final PMap<Integer, KLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstance> instances = HashTreePMap.empty();
    return new SBScene(textures, meshes, lights, instances);
  }

  private final @Nonnull PMap<String, SBTexture>   textures;
  private final @Nonnull PMap<String, SBModel>     models;
  private final @Nonnull PMap<Integer, KLight>     lights;
  private final @Nonnull PMap<Integer, SBInstance> instances;

  private SBScene(
    final @Nonnull PMap<String, SBTexture> textures,
    final @Nonnull PMap<String, SBModel> models,
    final @Nonnull PMap<Integer, KLight> lights,
    final @Nonnull PMap<Integer, SBInstance> instances)
  {
    this.textures = textures;
    this.models = models;
    this.lights = lights;
    this.instances = instances;
  }

  public @Nonnull SBScene addInstance(
    final @Nonnull SBInstance instance)
  {
    return new SBScene(
      this.textures,
      this.models,
      this.lights,
      this.instances.plus(instance.getID(), instance));
  }

  public @Nonnull SBScene addLight(
    final @Nonnull KLight light)
  {
    return new SBScene(this.textures, this.models, this.lights.plus(
      light.getID(),
      light), this.instances);
  }

  public @Nonnull SBScene addModel(
    final @Nonnull SBModel model)
  {
    return new SBScene(
      this.textures,
      this.models.plus(model.getName(), model),
      this.lights,
      this.instances);
  }

  public @Nonnull SBScene addTexture(
    final @Nonnull SBTexture texture)
  {
    return new SBScene(
      this.textures.plus(texture.getName(), texture),
      this.models,
      this.lights,
      this.instances);
  }

  public @CheckForNull SBTexture getTexture(
    final @Nonnull String texture)
  {
    return this.textures.get(texture);
  }
}
