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
import java.util.Collection;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import com.io7m.jaux.functional.Pair;

@Immutable final class SBScene
{
  public static @Nonnull SBScene empty()
  {
    final PMap<String, SBTexture> textures = HashTreePMap.empty();
    final PMap<String, SBMesh> meshes = HashTreePMap.empty();
    final PMap<Integer, KLight> lights = HashTreePMap.empty();
    final PMap<Integer, SBInstance> instances = HashTreePMap.empty();
    return new SBScene(
      textures,
      meshes,
      lights,
      Integer.valueOf(0),
      instances,
      Integer.valueOf(0));
  }

  private static @Nonnull Integer currentMaxID(
    final @Nonnull Integer x,
    final @Nonnull Integer y)
  {
    return Integer.valueOf(Math.max(x.intValue(), y.intValue()));
  }

  private final @Nonnull PMap<String, SBTexture>   textures;
  private final @Nonnull PMap<String, SBMesh>      meshes;
  private final @Nonnull PMap<Integer, KLight>     lights;
  private final @Nonnull Integer                   light_id_pool;
  private final @Nonnull PMap<Integer, SBInstance> instances;
  private final @Nonnull Integer                   instance_id_pool;

  private SBScene(
    final @Nonnull PMap<String, SBTexture> textures,
    final @Nonnull PMap<String, SBMesh> meshes,
    final @Nonnull PMap<Integer, KLight> lights,
    final @Nonnull Integer light_id_pool,
    final @Nonnull PMap<Integer, SBInstance> instances,
    final @Nonnull Integer instance_id_pool)
  {
    this.textures = textures;
    this.meshes = meshes;
    this.lights = lights;
    this.light_id_pool = light_id_pool;
    this.instances = instances;
    this.instance_id_pool = instance_id_pool;
  }

  public @Nonnull SBScene instanceAdd(
    final @Nonnull SBInstance instance)
  {
    return new SBScene(
      this.textures,
      this.meshes,
      this.lights,
      this.light_id_pool,
      this.instances.plus(instance.getID(), instance),
      SBScene.currentMaxID(this.instance_id_pool, instance.getID()));
  }

  public boolean instanceExists(
    final @Nonnull Integer id)
  {
    return this.instances.containsKey(id);
  }

  public @Nonnull Pair<SBScene, Integer> instanceFreshID()
  {
    final Integer id = Integer.valueOf(this.instance_id_pool.intValue() + 1);
    return new Pair<SBScene, Integer>(new SBScene(
      this.textures,
      this.meshes,
      this.lights,
      id,
      this.instances,
      this.instance_id_pool), id);
  }

  public @Nonnull SBInstance instanceGet(
    final @Nonnull Integer id)
  {
    return this.instances.get(id);
  }

  public @Nonnull Collection<SBInstance> instancesGet()
  {
    return this.instances.values();
  }

  public @Nonnull SBScene lightAdd(
    final @Nonnull KLight light)
  {
    return new SBScene(
      this.textures,
      this.meshes,
      this.lights.plus(light.getID(), light),
      SBScene.currentMaxID(this.light_id_pool, light.getID()),
      this.instances,
      this.instance_id_pool);
  }

  public boolean lightExists(
    final @Nonnull Integer id)
  {
    return this.lights.containsKey(id);
  }

  public @Nonnull Pair<SBScene, Integer> lightFreshID()
  {
    final Integer id = Integer.valueOf(this.light_id_pool.intValue() + 1);
    return new Pair<SBScene, Integer>(new SBScene(
      this.textures,
      this.meshes,
      this.lights,
      id,
      this.instances,
      this.instance_id_pool), id);
  }

  public @CheckForNull KLight lightGet(
    final @Nonnull Integer id)
  {
    return this.lights.get(id);
  }

  public @Nonnull SBScene lightRemove(
    final @Nonnull Integer id)
  {
    return new SBScene(
      this.textures,
      this.meshes,
      this.lights.minus(id),
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @Nonnull Collection<KLight> lightsGet()
  {
    return this.lights.values();
  }

  public @Nonnull SBScene meshAdd(
    final @Nonnull SBMesh mesh)
  {
    return new SBScene(
      this.textures,
      this.meshes.plus(mesh.getDescription().getName(), mesh),
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @CheckForNull SBMesh meshGet(
    final @Nonnull String name)
  {
    return this.meshes.get(name);
  }

  public @Nonnull Map<String, SBMesh> meshesGet()
  {
    return this.meshes;
  }

  public @Nonnull SBScene removeInstance(
    final @Nonnull Integer id)
  {
    return new SBScene(
      this.textures,
      this.meshes,
      this.lights,
      this.light_id_pool,
      this.instances.minus(id),
      this.instance_id_pool);
  }

  public @Nonnull SBScene textureAdd(
    final @Nonnull SBTexture texture)
  {
    return new SBScene(
      this.textures.plus(texture.getName(), texture),
      this.meshes,
      this.lights,
      this.light_id_pool,
      this.instances,
      this.instance_id_pool);
  }

  public @CheckForNull SBTexture textureGet(
    final @Nonnull String texture)
  {
    return this.textures.get(texture);
  }

  public @Nonnull Map<String, SBTexture> texturesGet()
  {
    return this.textures;
  }

  public @Nonnull SBScene instanceAddByDescription(
    final @Nonnull SBInstanceDescription d)
  {
    final SBTexture diff =
      (d.getDiffuse() == null) ? null : this.textureGet(d.getDiffuse());
    final SBTexture norm =
      (d.getNormal() == null) ? null : this.textureGet(d.getNormal());
    final SBTexture spec =
      (d.getSpecular() == null) ? null : this.textureGet(d.getSpecular());
    final SBInstance instance = new SBInstance(d, diff, norm, spec);
    return this.instanceAdd(instance);
  }

  public @Nonnull SBSceneDescription makeDescription(
    final boolean normalize)
  {
    SBSceneDescription desc = SBSceneDescription.empty();

    for (final KLight l : this.lights.values()) {
      desc = desc.lightAdd(l);
    }

    for (final SBTexture t : this.textures.values()) {
      if (normalize) {
        desc =
          desc.textureAdd(new SBTextureDescription(new File(t.getName()), t
            .getName()));
      } else {
        desc = desc.textureAdd(t.getDescription());
      }
    }

    for (final SBMesh m : this.meshes.values()) {
      if (normalize) {
        final String name = m.getDescription().getName();
        desc =
          desc.meshAdd(new SBMeshDescription(new File(name + ".rmx"), name));
      } else {
        desc = desc.meshAdd(m.getDescription());
      }
    }

    for (final SBInstance i : this.instances.values()) {
      desc = desc.instanceAdd(i.getDescription());
    }

    return desc;
  }
}
