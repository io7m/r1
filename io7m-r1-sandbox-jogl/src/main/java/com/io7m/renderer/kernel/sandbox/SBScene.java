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

package com.io7m.renderer.kernel.sandbox;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.io7m.jfunctional.Pair;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jvvfs.PathVirtual;

final class SBScene
{
  private static Integer currentMaxID(
    final Integer x,
    final Integer y)
  {
    return Integer.valueOf(Math.max(x.intValue(), y.intValue()));
  }

  public static SBScene empty()
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

  private final Integer                           instance_id_pool;
  private final PMap<Integer, SBInstance>         instances;
  private final Integer                           light_id_pool;
  private final PMap<Integer, SBLight>            lights;
  private final Integer                           material_id_pool;
  private final PMap<Integer, SBMaterial>         materials;
  private final PMap<PathVirtual, SBMesh>         meshes;
  private final PMap<PathVirtual, SBTextureCube>  textures_cube;
  private final PMap<PathVirtual, SBTexture2D<?>> textures2d;

  private SBScene(
    final PMap<PathVirtual, SBTexture2D<?>> in_textures2d,
    final PMap<PathVirtual, SBTextureCube> in_textures_cube,
    final PMap<PathVirtual, SBMesh> in_meshes,
    final PMap<Integer, SBMaterial> in_materials,
    final Integer in_material_id_pool,
    final PMap<Integer, SBLight> in_lights,
    final Integer in_light_id_pool,
    final PMap<Integer, SBInstance> in_instances,
    final Integer in_instance_id_pool)
  {
    this.textures2d = in_textures2d;
    this.textures_cube = in_textures_cube;
    this.meshes = in_meshes;
    this.materials = in_materials;
    this.lights = in_lights;
    this.light_id_pool = in_light_id_pool;
    this.material_id_pool = in_material_id_pool;
    this.instances = in_instances;
    this.instance_id_pool = in_instance_id_pool;
  }

  public SBScene instanceAdd(
    final SBInstance instance)

  {
    NullCheck.notNull(instance, "Instance");

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
    final Integer id)

  {
    NullCheck.notNull(id, "Instance");
    return this.instances.containsKey(id);
  }

  public Pair<SBScene, Integer> instanceFreshID()
  {
    final Integer id = Integer.valueOf(this.instance_id_pool.intValue() + 1);

    return Pair.pair(new SBScene(
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

  public SBInstance instanceGet(
    final Integer id)

  {
    NullCheck.notNull(id, "Instance");
    return this.instances.get(id);
  }

  public Collection<SBInstance> instancesGet()
  {
    return this.instances.values();
  }

  public SBScene lightAdd(
    final SBLight light)

  {
    NullCheck.notNull(light, "Light");
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
    final Integer id)

  {
    NullCheck.notNull(id, "Light");
    return this.lights.containsKey(id);
  }

  public Pair<SBScene, Integer> lightFreshID()
  {
    final Integer id = Integer.valueOf(this.light_id_pool.intValue() + 1);
    return Pair.pair(new SBScene(
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

  public @Nullable SBLight lightGet(
    final Integer id)

  {
    NullCheck.notNull(id, "Light");
    return this.lights.get(id);
  }

  public SBScene lightRemove(
    final Integer id)

  {
    NullCheck.notNull(id, "Light");
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

  public Collection<SBLight> lightsGet()
  {
    return this.lights.values();
  }

  public SBSceneDescription makeDescription()
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
    final Integer id)

  {
    NullCheck.notNull(id, "ID");
    return this.materials.containsKey(id);
  }

  public Pair<SBScene, Integer> materialFreshID()
  {
    final Integer id = Integer.valueOf(this.material_id_pool.intValue() + 1);
    return Pair.pair(new SBScene(
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

  public SBMaterial materialGet(
    final Integer id)
  {
    NullCheck.notNull(id, "ID");

    if (this.materials.containsKey(id) == false) {
      throw new NoSuchElementException("No such material for id " + id);
    }

    return this.materials.get(id);
  }

  public SBScene materialPut(
    final SBMaterial material)

  {
    NullCheck.notNull(material, "Material");
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

  public Collection<SBMaterial> materialsGet()
  {
    return this.materials.values();
  }

  public SBScene meshAdd(
    final SBMesh mesh)

  {
    NullCheck.notNull(mesh, "Mesh");

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

  public Map<PathVirtual, SBMesh> meshesGet()
  {
    return this.meshes;
  }

  public @Nullable SBMesh meshGet(
    final PathVirtual name)

  {
    NullCheck.notNull(name, "Mesh");
    return this.meshes.get(name);
  }

  public SBScene removeInstance(
    final Integer id)

  {
    NullCheck.notNull(id, "ID");

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

  public SBScene texture2DAdd(
    final SBTexture2D<?> texture)

  {
    NullCheck.notNull(texture, "Texture");

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

  @SuppressWarnings("unchecked") public @Nullable
    <T extends SBTexture2DKind>
    SBTexture2D<T>
    texture2DGet(
      final PathVirtual texture)
  {
    NullCheck.notNull(texture, "Texture");
    return (SBTexture2D<T>) this.textures2d.get(texture);
  }

  public SBScene textureCubeAdd(
    final SBTextureCube texture)
  {
    NullCheck.notNull(texture, "Texture");
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

  public @Nullable SBTextureCube textureCubeGet(
    final PathVirtual texture)
  {
    NullCheck.notNull(texture, "Texture");
    return this.textures_cube.get(texture);
  }

  public Map<PathVirtual, SBTexture2D<?>> textures2DGet()
  {
    return this.textures2d;
  }

  public Map<PathVirtual, SBTextureCube> texturesCubeGet()
  {
    return this.textures_cube;
  }
}
