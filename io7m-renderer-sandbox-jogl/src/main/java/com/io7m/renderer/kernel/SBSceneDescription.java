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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import com.io7m.jvvfs.PathVirtual;

@Immutable final class SBSceneDescription
{
  static final int SCENE_XML_VERSION;

  static {
    SCENE_XML_VERSION = 15;
  }

  public static @Nonnull SBSceneDescription empty()
  {
    final PSet<SBTexture2DDescription> textures_2d = HashTreePSet.empty();
    final PSet<SBTextureCubeDescription> textures_cube = HashTreePSet.empty();
    final PSet<PathVirtual> meshes = HashTreePSet.empty();
    final PMap<Integer, SBLightDescription> lights = HashTreePMap.empty();
    final PMap<Integer, SBMaterialDescription> materials =
      HashTreePMap.empty();
    final PMap<Integer, SBInstance> instances = HashTreePMap.empty();

    return new SBSceneDescription(
      SBSceneDescription.SCENE_XML_VERSION,
      textures_2d,
      textures_cube,
      materials,
      meshes,
      lights,
      instances);
  }

  private final @Nonnull PSet<SBTexture2DDescription>         textures_2d;
  private final @Nonnull PSet<SBTextureCubeDescription>       textures_cube;
  private final @Nonnull PSet<PathVirtual>                    meshes;
  private final @Nonnull PMap<Integer, SBMaterialDescription> materials;
  private final @Nonnull PMap<Integer, SBLightDescription>    lights;
  private final @Nonnull PMap<Integer, SBInstance>            instances;
  private final int                                           schema_version;

  private SBSceneDescription(
    final int schema_version,
    final @Nonnull PSet<SBTexture2DDescription> textures_2d,
    final @Nonnull PSet<SBTextureCubeDescription> textures_cube,
    final @Nonnull PMap<Integer, SBMaterialDescription> materials,
    final @Nonnull PSet<PathVirtual> meshes,
    final @Nonnull PMap<Integer, SBLightDescription> lights,
    final @Nonnull PMap<Integer, SBInstance> instances)
  {
    this.schema_version = schema_version;
    this.textures_2d = textures_2d;
    this.textures_cube = textures_cube;
    this.materials = materials;
    this.meshes = meshes;
    this.lights = lights;
    this.instances = instances;
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
    final SBSceneDescription other = (SBSceneDescription) obj;
    if (!this.instances.equals(other.instances)) {
      return false;
    }
    if (!this.lights.equals(other.lights)) {
      return false;
    }
    if (!this.meshes.equals(other.meshes)) {
      return false;
    }
    if (!this.textures_2d.equals(other.textures_2d)) {
      return false;
    }
    if (!this.textures_cube.equals(other.textures_cube)) {
      return false;
    }
    return true;
  }

  public @Nonnull Collection<SBInstance> getInstances()
  {
    return this.instances.values();
  }

  public @Nonnull Collection<SBLightDescription> getLights()
  {
    return this.lights.values();
  }

  public @Nonnull PMap<Integer, SBMaterialDescription> getMaterials()
  {
    return this.materials;
  }

  public @Nonnull PSet<PathVirtual> getMeshes()
  {
    return this.meshes;
  }

  public int getSchemaVersion()
  {
    return this.schema_version;
  }

  public @Nonnull PSet<SBTexture2DDescription> getTextures2D()
  {
    return this.textures_2d;
  }

  public @Nonnull PSet<SBTextureCubeDescription> getTexturesCube()
  {
    return this.textures_cube;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.instances.hashCode();
    result = (prime * result) + this.lights.hashCode();
    result = (prime * result) + this.meshes.hashCode();
    result = (prime * result) + this.textures_2d.hashCode();
    return result;
  }

  public @Nonnull SBSceneDescription instanceAdd(
    final @Nonnull SBInstance d)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d,
      this.textures_cube,
      this.materials,
      this.meshes,
      this.lights,
      this.instances.plus(d.getID(), d));
  }

  public @Nonnull SBSceneDescription lightAdd(
    final @Nonnull SBLightDescription l)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d,
      this.textures_cube,
      this.materials,
      this.meshes,
      this.lights.plus(l.lightGetID(), l),
      this.instances);
  }

  public @Nonnull SBSceneDescription meshAdd(
    final @Nonnull PathVirtual d)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d,
      this.textures_cube,
      this.materials,
      this.meshes.plus(d),
      this.lights,
      this.instances);
  }

  public @Nonnull SBSceneDescription texture2DAdd(
    final @Nonnull SBTexture2DDescription t)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d.plus(t),
      this.textures_cube,
      this.materials,
      this.meshes,
      this.lights,
      this.instances);
  }

  public @Nonnull SBSceneDescription materialAdd(
    final @Nonnull Integer id,
    final @Nonnull SBMaterialDescription t)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d,
      this.textures_cube,
      this.materials.plus(id, t),
      this.meshes,
      this.lights,
      this.instances);
  }

  public @Nonnull SBSceneDescription textureCubeAdd(
    final @Nonnull SBTextureCubeDescription t)
  {
    return new SBSceneDescription(
      this.schema_version,
      this.textures_2d,
      this.textures_cube.plus(t),
      this.materials,
      this.meshes,
      this.lights,
      this.instances);
  }
}
