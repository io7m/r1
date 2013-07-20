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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlog.Log;

final class SBSceneState implements
  SBSceneStateLights,
  SBSceneStateMeshes,
  SBSceneStateTextures,
  SBSceneStateObjects
{
  private final @Nonnull ConcurrentHashMap<Integer, KLight>                                  light_descriptions;
  private final @Nonnull AtomicInteger                                                       light_id_pool;
  private final @Nonnull Log                                                                 log;
  private final @Nonnull ConcurrentHashMap<File, Pair<BufferedImage, Texture2DStaticUsable>> textures;
  private final @Nonnull ConcurrentHashMap<File, SortedSet<SBMesh>>                          meshes;
  private final @Nonnull AtomicInteger                                                       object_id_pool;
  private final @Nonnull ConcurrentHashMap<Integer, SBObjectDescription>                     object_instances;

  SBSceneState(
    final @Nonnull Log log)
  {
    this.log = new Log(log, "scene");
    this.light_descriptions = new ConcurrentHashMap<Integer, KLight>();
    this.light_id_pool = new AtomicInteger(0);
    this.textures =
      new ConcurrentHashMap<File, Pair<BufferedImage, Texture2DStaticUsable>>();
    this.meshes = new ConcurrentHashMap<File, SortedSet<SBMesh>>();
    this.object_id_pool = new AtomicInteger(0);
    this.object_instances =
      new ConcurrentHashMap<Integer, SBObjectDescription>();
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

  @Override public @Nonnull List<KLight> lightsGetAll()
  {
    final ArrayList<KLight> ls = new ArrayList<KLight>();
    for (final KLight l : this.light_descriptions.values()) {
      ls.add(l);
    }
    return ls;
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
    throw new UnimplementedCodeException();
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
    throw new UnimplementedCodeException();
  }

  @Override public void meshAdd(
    final @Nonnull File model,
    final @Nonnull SortedSet<SBMesh> objects)
  {
    if (this.meshes.containsKey(model)) {
      this.log.debug("Replacing model " + model);
    } else {
      this.log.debug("Adding model " + model);
    }

    this.meshes.put(model, objects);
  }

  @Override public @Nonnull Map<File, SortedSet<String>> meshesGet()
  {
    final HashMap<File, SortedSet<String>> results =
      new HashMap<File, SortedSet<String>>();

    for (final Entry<File, SortedSet<SBMesh>> e : this.meshes.entrySet()) {
      final File f = e.getKey();
      final SortedSet<SBMesh> ms = e.getValue();
      final TreeSet<String> ns = new TreeSet<String>();
      for (final SBMesh m : ms) {
        ns.add(m.getName());
      }
      results.put(f, ns);
    }

    return results;
  }

  @Override public void objectAdd(
    final @Nonnull SBObjectDescription object)
  {
    if (this.object_instances.containsKey(object.getID())) {
      this.log.debug("Replacing object " + object.getID());
    } else {
      this.log.debug("Adding object " + object.getID());
    }

    this.object_instances.put(object.getID(), object);
  }

  @Override public boolean objectExists(
    final @Nonnull Integer id)
  {
    return this.object_instances.containsKey(id);
  }

  @Override public Integer objectFreshID()
  {
    return Integer.valueOf(this.object_id_pool.getAndIncrement());
  }

  @Override public @CheckForNull SBObjectDescription objectGet(
    final @Nonnull Integer id)
  {
    return this.object_instances.get(id);
  }

  @Override public @Nonnull List<SBObjectDescription> objectsGetAll()
  {
    final ArrayList<SBObjectDescription> os =
      new ArrayList<SBObjectDescription>();
    for (final SBObjectDescription o : this.object_instances.values()) {
      os.add(o);
    }
    return os;
  }

  @Override public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Pair<BufferedImage, Texture2DStaticUsable> texture)
  {
    this.textures.put(file, texture);
  }

  @Override public @Nonnull Map<File, BufferedImage> texturesGet()
  {
    final HashMap<File, BufferedImage> m = new HashMap<File, BufferedImage>();
    final Set<Entry<File, Pair<BufferedImage, Texture2DStaticUsable>>> es =
      this.textures.entrySet();

    for (final Entry<File, Pair<BufferedImage, Texture2DStaticUsable>> e : es) {
      m.put(e.getKey(), e.getValue().first);
    }

    return m;
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

  public @Nonnull List<KLight> lightsGetAll();
}

interface SBSceneStateMeshes
{
  public void meshAdd(
    final @Nonnull File model,
    final @Nonnull SortedSet<SBMesh> objects);

  public @Nonnull Map<File, SortedSet<String>> meshesGet();
}

interface SBSceneStateObjects
{
  public void objectAdd(
    final @Nonnull SBObjectDescription object);

  public boolean objectExists(
    final @Nonnull Integer id);

  public @Nonnull Integer objectFreshID();

  public @CheckForNull SBObjectDescription objectGet(
    final @Nonnull Integer id);

  public @Nonnull List<SBObjectDescription> objectsGetAll();
}

interface SBSceneStateTextures
{
  public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Pair<BufferedImage, Texture2DStaticUsable> texture);

  public @Nonnull Map<File, BufferedImage> texturesGet();
}
