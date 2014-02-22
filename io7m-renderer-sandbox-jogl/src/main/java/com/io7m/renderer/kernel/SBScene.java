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
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Pair;
import com.io7m.jvvfs.PathVirtual;

@Immutable final class SBScene
{
  private static @Nonnull Integer currentMaxID(
    final @Nonnull Integer x,
    final @Nonnull Integer y)
  {
    return Integer.valueOf(Math.max(x.intValue(), y.intValue()));
  }

  public static @Nonnull SBScene empty()
  {
    final PMap<PathVirtual, SBTexture2D<?>> textures2d = HashTreePMap.empty();
    final PMap<PathVirtual, SBTextureCube> textures_cube =
      HashTreePMap.empty();
    final PMap<PathVirtual, SBMesh> meshes = HashTreePMap.empty();
    final PMap<Integer, SBLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstance> instances = HashTreePMap.empty();
    final PMap<Integer, SBMaterial> materials = HashTreePMap.empty();

    return new SBScene(
      textures2d,
      textures_cube,
      meshes,
      materials,
      Integer.valueOf(0),
      lights,
      Integer.valueOf(0),
      instances,
      Integer.valueOf(0));
  }

  private final @Nonnull Integer                           instance_id_pool;
  private final @Nonnull PMap<Integer, SBInstance>         instances;
  private final @Nonnull Integer                           light_id_pool;
  private final @Nonnull PMap<Integer, SBLight>            lights;
  private final @Nonnull Integer                           material_id_pool;
  private final @Nonnull PMap<Integer, SBMaterial>         materials;
  private final @Nonnull PMap<PathVirtual, SBMesh>         meshes;
  private final @Nonnull PMap<PathVirtual, SBTextureCube>  textures_cube;
  private final @Nonnull PMap<PathVirtual, SBTexture2D<?>> textures2d;

  private SBScene(
    final @Nonnull PMap<PathVirtual, SBTexture2D<?>> textures2d,
    final @Nonnull PMap<PathVirtual, SBTextureCube> textures_cube,
    final @Nonnull PMap<PathVirtual, SBMesh> meshes,
    final @Nonnull PMap<Integer, SBMaterial> materials,
    final @Nonnull Integer material_id_pool,
    final @Nonnull PMap<Integer, SBLight> lights,
    final @Nonnull Integer light_id_pool,
    final @Nonnull PMap<Integer, SBInstance> instances,
    final @Nonnull Integer instance_id_pool)
  {
    this.textures2d = textures2d;
    this.textures_cube = textures_cube;
    this.meshes = meshes;
    this.materials = materials;
    this.lights = lights;
    this.light_id_pool = light_id_pool;
    this.material_id_pool = material_id_pool;
    this.instances = instances;
    this.instance_id_pool = instance_id_pool;
  }

  public @Nonnull SBScene instanceAdd(
    final @Nonnull SBInstance instance)
    throws ConstraintError
  {
    Constraints.constrainNotNull(instance, "Instance");

    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances.plus(instance.getID(), instance),
      SBScene.currentMaxID(this.instance_id_pool, instance.getID()));
  }

  public boolean instanceExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "Instance");
    return this.instances.containsKey(id);
  }

  public @Nonnull Pair<SBScene, Integer> instanceFreshID()
  {
    final Integer id = Integer.valueOf(this.instance_id_pool.intValue() + 1);

    return new Pair<SBScene, Integer>(new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances,
      id), id);
  }

  public @Nonnull SBInstance instanceGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "Instance");
    return this.instances.get(id);
  }

  public @Nonnull Collection<SBInstance> instancesGet()
  {
    return this.instances.values();
  }

  public @Nonnull SBScene lightAdd(
    final @Nonnull SBLight light)
    throws ConstraintError
  {
    Constraints.constrainNotNull(light, "Light");
    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights.plus(light.getID(), light),
      SBScene.currentMaxID(this.light_id_pool, light.getID()),
      this.instances,
      this.instance_id_pool);
  }

  public boolean lightExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "Light");
    return this.lights.containsKey(id);
  }

  public @Nonnull Pair<SBScene, Integer> lightFreshID()
  {
    final Integer id = Integer.valueOf(this.light_id_pool.intValue() + 1);
    return new Pair<SBScene, Integer>(new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      id,
      this.instances,
      this.instance_id_pool), id);
  }

  public @CheckForNull SBLight lightGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "Light");
    return this.lights.get(id);
  }

  public @Nonnull SBScene lightRemove(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "Light");
    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights.minus(id),
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @Nonnull Collection<SBLight> lightsGet()
  {
    return this.lights.values();
  }

  public @Nonnull SBSceneDescription makeDescription()
  {
    SBSceneDescription desc = SBSceneDescription.empty();

    for (final SBMesh m : this.meshes.values()) {
      desc = desc.meshAdd(m.getPath());
    }

    for (final SBTexture2D<?> t : this.textures2d.values()) {
      desc = desc.texture2DAdd(t.getDescription());
    }

    for (final SBTextureCube t : this.textures_cube.values()) {
      desc = desc.textureCubeAdd(t.getDescription());
    }

    for (final SBLight l : this.lights.values()) {
      desc = desc.lightAdd(l.getDescription());
    }

    for (final Integer mid : this.materials.keySet()) {
      desc =
        desc.materialAdd(mid, this.materials
          .get(mid)
          .materialGetDescription());
    }

    for (final SBInstance i : this.instances.values()) {
      desc = desc.instanceAdd(i);
    }

    return desc;
  }

  public boolean materialExists(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "ID");
    return this.materials.containsKey(id);
  }

  public @Nonnull Pair<SBScene, Integer> materialFreshID()
  {
    final Integer id = Integer.valueOf(this.material_id_pool.intValue() + 1);
    return new Pair<SBScene, Integer>(new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      id,
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool), id);
  }

  public @Nonnull SBMaterial materialGet(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "ID");
    Constraints.constrainArbitrary(
      this.materials.containsKey(id),
      "Material exists");
    return this.materials.get(id);
  }

  public @Nonnull SBScene materialPut(
    final @Nonnull SBMaterial material)
    throws ConstraintError
  {
    Constraints.constrainNotNull(material, "Material");
    final Integer id = material.materialGetID();

    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials.plus(id, material),
      SBScene.currentMaxID(this.material_id_pool, id),
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @Nonnull Collection<SBMaterial> materialsGet()
  {
    return this.materials.values();
  }

  public @Nonnull SBScene meshAdd(
    final @Nonnull SBMesh mesh)
    throws ConstraintError
  {
    Constraints.constrainNotNull(mesh, "Mesh");

    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes.plus(mesh.getPath(), mesh),
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @Nonnull Map<PathVirtual, SBMesh> meshesGet()
  {
    return this.meshes;
  }

  public @CheckForNull SBMesh meshGet(
    final @Nonnull PathVirtual name)
    throws ConstraintError
  {
    Constraints.constrainNotNull(name, "Mesh");
    return this.meshes.get(name);
  }

  public @Nonnull SBScene removeInstance(
    final @Nonnull Integer id)
    throws ConstraintError
  {
    Constraints.constrainNotNull(id, "ID");

    return new SBScene(
      this.textures2d,
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances.minus(id),
      this.instance_id_pool);
  }

  public @Nonnull SBScene texture2DAdd(
    final @Nonnull SBTexture2D<?> texture)
    throws ConstraintError
  {
    Constraints.constrainNotNull(texture, "Texture");

    return new SBScene(
      this.textures2d.plus(texture.getPath(), texture),
      this.textures_cube,
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  @SuppressWarnings("unchecked") public @CheckForNull
    <T extends SBTexture2DKind>
    SBTexture2D<T>
    texture2DGet(
      final @Nonnull PathVirtual texture)
      throws ConstraintError
  {
    Constraints.constrainNotNull(texture, "Texture");
    return (SBTexture2D<T>) this.textures2d.get(texture);
  }

  public @Nonnull SBScene textureCubeAdd(
    final @Nonnull SBTextureCube texture)
    throws ConstraintError
  {
    Constraints.constrainNotNull(texture, "Texture");
    return new SBScene(
      this.textures2d,
      this.textures_cube.plus(texture.getPath(), texture),
      this.meshes,
      this.materials,
      this.material_id_pool,
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @CheckForNull SBTextureCube textureCubeGet(
    final @Nonnull PathVirtual texture)
    throws ConstraintError
  {
    Constraints.constrainNotNull(texture, "Texture");
    return this.textures_cube.get(texture);
  }

  public @Nonnull Map<PathVirtual, SBTexture2D<?>> textures2DGet()
  {
    return this.textures2d;
  }

  public @Nonnull Map<PathVirtual, SBTextureCube> texturesCubeGet()
  {
    return this.textures_cube;
  }
}
