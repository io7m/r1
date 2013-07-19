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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlog.Log;

final class SBSceneState implements
  SBSceneStateLights,
  SBSceneStateMeshes,
  SBSceneStateTextures
{
  private final @Nonnull ConcurrentHashMap<Integer, KLight>             light_descriptions;
  private final @Nonnull AtomicInteger                                  light_id_pool;
  private final @Nonnull Log                                            log;
  private final @Nonnull ConcurrentHashMap<File, Texture2DStaticUsable> textures;
  private final @Nonnull ConcurrentHashMap<File, SortedSet<SBMesh>>     meshes;

  SBSceneState(
    final @Nonnull Log log)
  {
    this.log = new Log(log, "scene");
    this.light_descriptions = new ConcurrentHashMap<Integer, KLight>();
    this.light_id_pool = new AtomicInteger(0);
    this.textures = new ConcurrentHashMap<File, Texture2DStaticUsable>();
    this.meshes = new ConcurrentHashMap<File, SortedSet<SBMesh>>();
  }

  @Override public void lightAdd(
    final @Nonnull KLight light)
  {
    final Integer id = light.getID();
    if (this.light_descriptions.containsKey(id)) {
      this.log.debug("Replacing light "
        + id
        + " with type "
        + light.getType());
    } else {
      this.log.debug("Adding light " + id + " of type " + light.getType());
    }

    this.light_descriptions.put(id, light);
  }

  @Override public boolean lightExists(
    final @Nonnull Integer id)
  {
    return this.light_descriptions.containsKey(id);
  }

  @Override public @Nonnull Integer lightFreshID()
  {
    return Integer.valueOf(this.light_id_pool.getAndIncrement());
  }

  @Override public @CheckForNull KLight lightGet(
    final @Nonnull Integer key)
  {
    return this.light_descriptions.get(key);
  }

  @Override public void lightRemove(
    final @Nonnull Integer id)
  {
    this.log.debug("Removing light " + id);
    this.light_descriptions.remove(id);
  }

  @Nonnull KScene makeScene()
  {
    final Set<KLight> lights = this.makeSceneLights();
    final Set<KMeshInstance> kmeshes = this.makeSceneMeshes();
    final KCamera camera = this.makeSceneCamera();
    return new KScene(camera, lights, kmeshes);
  }

  private @Nonnull KCamera makeSceneCamera()
  {
    // TODO Auto-generated method stub
    return null;
  }

  private @Nonnull Set<KLight> makeSceneLights()
  {
    final HashSet<KLight> lights = new HashSet<KLight>();

    for (final KLight light : this.light_descriptions.values()) {
      lights.add(light);
    }

    return lights;
  }

  private @Nonnull Set<KMeshInstance> makeSceneMeshes()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override public void meshAdd(
    final @Nonnull File model,
    final @Nonnull SortedSet<SBMesh> objects)
  {
    this.meshes.put(model, objects);
  }

  @Override public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Texture2DStaticUsable texture)
  {
    this.textures.put(file, texture);
  }
}

interface SBSceneStateLights
{
  public void lightAdd(
    final @Nonnull KLight light);

  public boolean lightExists(
    final @Nonnull Integer id);

  public @Nonnull Integer lightFreshID();

  public @CheckForNull KLight lightGet(
    final @Nonnull Integer key);

  public void lightRemove(
    final @Nonnull Integer id);
}

interface SBSceneStateMeshes
{
  public void meshAdd(
    final @Nonnull File model,
    final @Nonnull SortedSet<SBMesh> objects);
}

interface SBSceneStateTextures
{
  public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Texture2DStaticUsable texture);
}
