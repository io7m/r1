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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ValidityException;

import com.io7m.jaux.CheckedMath;
import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Log;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.KLight.KDirectional;

final class SBSceneState implements
  SBSceneStateDeletion,
  SBSceneStateLights,
  SBSceneStateMeshes,
  SBSceneStateObjects,
  SBSceneStateRenderer,
  SBSceneStateTextures
{
  static class SBSceneNormalized
  {
    private static final @Nonnull URI    SCENE_URI;
    private static final @Nonnull String SCENE_VERSION;

    static {
      try {
        SCENE_URI = new URI("http://io7m.com/software/renderer");
        SCENE_VERSION = "1";
      } catch (final URISyntaxException e) {
        throw new UnreachableCodeException();
      }
    }

    private static @Nonnull Element lightToXML(
      final @Nonnull KLight light)
    {
      final String uri = SBSceneNormalized.SCENE_URI.toString();

      final Element eid = new Element("s:id", uri);
      eid.appendChild(light.getID().toString());

      final Element ei = new Element("s:intensity", uri);
      ei.appendChild(Float.toString(light.getIntensity()));

      final Element ec = new Element("s:colour", uri);
      final Element ecr = new Element("s:r", uri);
      ecr.appendChild(Float.toString(light.getColour().getXF()));
      final Element ecg = new Element("s:g", uri);
      ecg.appendChild(Float.toString(light.getColour().getYF()));
      final Element ecb = new Element("s:b", uri);
      ecb.appendChild(Float.toString(light.getColour().getZF()));
      ec.appendChild(ecr);
      ec.appendChild(ecg);
      ec.appendChild(ecb);

      switch (light.getType()) {
        case LIGHT_CONE:
        {
          throw new UnimplementedCodeException();
        }
        case LIGHT_DIRECTIONAL:
        {
          final KDirectional d = (KLight.KDirectional) light;
          final Element e = new Element("s:light-directional", uri);

          final Element ed = new Element("s:direction", uri);
          final Element edx = new Element("s:x", uri);
          edx.appendChild(Float.toString(d.getDirection().getXF()));
          final Element edy = new Element("s:y", uri);
          edy.appendChild(Float.toString(d.getDirection().getYF()));
          final Element edz = new Element("s:z", uri);
          edz.appendChild(Float.toString(d.getDirection().getZF()));
          ed.appendChild(edx);
          ed.appendChild(edy);
          ed.appendChild(edz);

          e.appendChild(eid);
          e.appendChild(ec);
          e.appendChild(ei);
          e.appendChild(ed);
          return e;
        }
        case LIGHT_POINT:
        {
          throw new UnimplementedCodeException();
        }
      }

      throw new UnreachableCodeException();
    }

    private static @Nonnull SBObjectDescription loadInstance(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
      throws ValidityException
    {
      assert e.getLocalName().equals("instance");

      final Element eid =
        SBXMLUtilities.getChild(e, "id", SBSceneNormalized.SCENE_URI);

      final Element ep =
        SBXMLUtilities.getChild(e, "position", SBSceneNormalized.SCENE_URI);
      final Element eo =
        SBXMLUtilities
          .getChild(e, "orientation", SBSceneNormalized.SCENE_URI);

      final Element ed =
        SBXMLUtilities.getChild(e, "diffuse", SBSceneNormalized.SCENE_URI);
      final Element en =
        SBXMLUtilities.getChild(e, "normal", SBSceneNormalized.SCENE_URI);
      final Element es =
        SBXMLUtilities.getChild(e, "specular", SBSceneNormalized.SCENE_URI);

      final Element em =
        SBXMLUtilities.getChild(e, "model", SBSceneNormalized.SCENE_URI);
      final Element emm =
        SBXMLUtilities.getChild(e, "mesh", SBSceneNormalized.SCENE_URI);

      final Integer id = SBXMLUtilities.getInteger(eid);

      final RVectorI3F<RSpaceWorld> position =
        SBXMLUtilities.getVector3f(ep, SBSceneNormalized.SCENE_URI);
      final RVectorI3F<SBDegrees> orientation =
        SBXMLUtilities.getVector3f(eo, SBSceneNormalized.SCENE_URI);

      final File diffuse =
        (ed.getValue().length() == 0) ? null : new File(
          root_directory,
          ed.getValue());

      final File normal =
        (en.getValue().length() == 0) ? null : new File(
          root_directory,
          en.getValue());

      final File specular =
        (es.getValue().length() == 0) ? null : new File(
          root_directory,
          es.getValue());

      final File model =
        new File(root_directory, SBXMLUtilities.getNonEmptyString(em));
      final String object = SBXMLUtilities.getNonEmptyString(emm);

      return new SBObjectDescription(
        id,
        model,
        object,
        position,
        orientation,
        diffuse,
        normal,
        specular);
    }

    private static @Nonnull KLight.KDirectional loadLight(
      final @Nonnull Element e)
      throws ValidityException
    {
      if (e.getLocalName().equals("light-directional")) {
        return SBSceneNormalized.loadLightDirectional(e);
      }

      throw new UnimplementedCodeException();
    }

    private static @Nonnull KLight.KDirectional loadLightDirectional(
      final @Nonnull Element e)
      throws ValidityException
    {
      assert e.getLocalName().equals("light-directional");

      final Element ed =
        SBXMLUtilities.getChild(e, "direction", SBSceneNormalized.SCENE_URI);
      final Element ec =
        SBXMLUtilities.getChild(e, "colour", SBSceneNormalized.SCENE_URI);
      final Element ei =
        SBXMLUtilities.getChild(e, "intensity", SBSceneNormalized.SCENE_URI);
      final Element eid =
        SBXMLUtilities.getChild(e, "id", SBSceneNormalized.SCENE_URI);

      final RVectorI3F<RSpaceWorld> direction =
        SBXMLUtilities.getVector3f(ed, SBSceneNormalized.SCENE_URI);
      final RVectorI3F<RSpaceRGB> colour =
        SBXMLUtilities.getRGB(ec, SBSceneNormalized.SCENE_URI);
      final Integer id = SBXMLUtilities.getInteger(eid);
      final float intensity = SBXMLUtilities.getFloat(ei);

      return new KLight.KDirectional(id, direction, colour, intensity);
    }

    private static @Nonnull File loadMesh(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
    {
      assert e.getLocalName().equals("mesh");
      return new File(root_directory, e.getValue());
    }

    private static @Nonnull File loadTexture(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
    {
      assert e.getLocalName().equals("texture");
      return new File(root_directory, e.getValue());
    }

    private final @Nonnull SortedSet<String>              flat_names;
    private final @Nonnull Map<String, File>              flat_to_files;
    private final @Nonnull Map<File, String>              files_to_flat;
    private final @Nonnull Log                            log;
    private final @Nonnull SortedSet<String>              textures;
    private final @Nonnull Map<String, SortedSet<String>> meshes;
    private final @Nonnull ArrayList<KLight>              lights;
    private final @Nonnull ArrayList<SBObjectDescription> instances;

    public SBSceneNormalized(
      final @Nonnull Log log,
      final @Nonnull File root_directory,
      final @Nonnull Document doc)
      throws ValidityException
    {
      this.log = new Log(log, "normalizer");
      this.flat_to_files = new HashMap<String, File>();
      this.files_to_flat = new HashMap<File, String>();
      this.flat_names = new TreeSet<String>();
      this.textures = new TreeSet<String>();
      this.meshes = new HashMap<String, SortedSet<String>>();
      this.lights = new ArrayList<KLight>();
      this.instances = new ArrayList<SBObjectDescription>();

      final Element root = doc.getRootElement();
      SBXMLUtilities.checkIsElement(
        root,
        "scene",
        SBSceneNormalized.SCENE_URI);

      final Element e_lights =
        SBXMLUtilities.getChild(root, "lights", SBSceneNormalized.SCENE_URI);
      final Element e_textures =
        SBXMLUtilities
          .getChild(root, "textures", SBSceneNormalized.SCENE_URI);
      final Element e_meshes =
        SBXMLUtilities.getChild(root, "meshes", SBSceneNormalized.SCENE_URI);
      final Element e_instances =
        SBXMLUtilities.getChild(
          root,
          "instances",
          SBSceneNormalized.SCENE_URI);

      this.loadLights(e_lights);
      this.loadTextures(root_directory, e_textures);
      this.loadMeshes(root_directory, e_meshes);
      this.loadInstances(root_directory, e_instances);
    }

    SBSceneNormalized(
      final @Nonnull Log log,
      final @Nonnull SBSceneState state)
    {
      this.log = new Log(log, "normalizer");
      this.flat_to_files = new HashMap<String, File>();
      this.files_to_flat = new HashMap<File, String>();
      this.flat_names = new TreeSet<String>();
      this.textures = new TreeSet<String>();
      this.meshes = new HashMap<String, SortedSet<String>>();
      this.lights = new ArrayList<KLight>();
      this.instances = new ArrayList<SBObjectDescription>();

      this.normalizeAndSaveTextures(state);
      this.normalizeAndSaveMeshes(state);
      this.saveLights(state);
      this.saveObjects(state);
    }

    private @Nonnull String addName(
      final @Nonnull File file)
    {
      String suffix = null;
      String prefix = null;

      if (this.flat_names.contains(file.getName()) == false) {
        this.putName(file, file.getName());
        return file.getName();
      }

      final String name = file.getName();
      final int dot = name.lastIndexOf('.');
      if (dot != -1) {
        suffix = name.substring(dot + 1);
        prefix = name.substring(0, dot);
      } else {
        prefix = name;
      }

      final StringBuilder buffer = new StringBuilder();
      long n = 0;

      /**
       * Loop terminates when a unique name is found, or n overflows.
       */

      for (;;) {
        buffer.setLength(0);
        buffer.append(prefix);
        buffer.append("_");
        buffer.append(n);
        n = CheckedMath.add(n, 1);

        if (suffix != null) {
          buffer.append(".");
          buffer.append(suffix);
        }
        if (this.flat_names.contains(buffer.toString()) == false) {
          this.putName(file, buffer.toString());
          return buffer.toString();
        }
      }
    }

    @Nonnull Map<String, File> getFileMappings()
    {
      return this.flat_to_files;
    }

    @Nonnull Collection<KLight> getLights()
    {
      return this.lights;
    }

    @Nonnull Collection<File> getMeshes()
    {
      final ArrayList<File> files = new ArrayList<File>();
      for (final String file : this.meshes.keySet()) {
        files.add(new File(file));
      }
      return files;
    }

    @Nonnull Collection<SBObjectDescription> getObjects()
    {
      return this.instances;
    }

    @Nonnull Collection<File> getTextures()
    {
      final ArrayList<File> files = new ArrayList<File>();
      for (final String file : this.textures) {
        files.add(new File(file));
      }
      return files;
    }

    private @Nonnull Element instanceToXML(
      final @Nonnull SBObjectDescription o)
    {
      final String uri = SBSceneNormalized.SCENE_URI.toString();
      final Element e = new Element("s:instance", uri);

      final Element eid = new Element("s:id", uri);
      eid.appendChild(o.getID().toString());

      final Element eo = new Element("s:orientation", uri);
      final Element eox = new Element("s:x", uri);
      eox.appendChild(Float.toString(o.getOrientation().getXF()));
      final Element eoy = new Element("s:y", uri);
      eoy.appendChild(Float.toString(o.getOrientation().getYF()));
      final Element eoz = new Element("s:z", uri);
      eoz.appendChild(Float.toString(o.getOrientation().getZF()));
      eo.appendChild(eox);
      eo.appendChild(eoy);
      eo.appendChild(eoz);

      final Element ep = new Element("s:position", uri);
      final Element epx = new Element("s:x", uri);
      epx.appendChild(Float.toString(o.getPosition().getXF()));
      final Element epy = new Element("s:y", uri);
      epy.appendChild(Float.toString(o.getPosition().getYF()));
      final Element epz = new Element("s:z", uri);
      epz.appendChild(Float.toString(o.getPosition().getZF()));
      ep.appendChild(epx);
      ep.appendChild(epy);
      ep.appendChild(epz);

      final Element emo = new Element("s:model", uri);
      emo.appendChild(this.files_to_flat.get(o.getModel()));
      final Element eme = new Element("s:mesh", uri);
      eme.appendChild(o.getModelObject());

      final Element ed = new Element("s:diffuse", uri);
      if (o.getDiffuseTexture() != null) {
        ed.appendChild(this.files_to_flat.get(o.getDiffuseTexture()));
      }
      final Element en = new Element("s:normal", uri);
      if (o.getNormalTexture() != null) {
        en.appendChild(this.files_to_flat.get(o.getNormalTexture()));
      }
      final Element es = new Element("s:specular", uri);
      if (o.getSpecularTexture() != null) {
        es.appendChild(this.files_to_flat.get(o.getSpecularTexture()));
      }

      e.appendChild(eid);
      e.appendChild(eo);
      e.appendChild(ep);
      e.appendChild(emo);
      e.appendChild(eme);
      e.appendChild(ed);
      e.appendChild(en);
      e.appendChild(es);
      return e;
    }

    private void loadInstances(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
      throws ValidityException
    {
      assert e.getLocalName().equals("instances");

      final Elements ec = e.getChildElements();
      for (int index = 0; index < ec.size(); ++index) {
        final SBObjectDescription o =
          SBSceneNormalized.loadInstance(root_directory, ec.get(index));
        this.instances.add(o);
      }
    }

    private void loadLights(
      final @Nonnull Element e)
      throws ValidityException
    {
      assert e.getLocalName().equals("lights");

      final Elements ec = e.getChildElements();
      for (int index = 0; index < ec.size(); ++index) {
        final KDirectional light = SBSceneNormalized.loadLight(ec.get(index));
        this.lights.add(light);
      }
    }

    private void loadMeshes(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
    {
      assert e.getLocalName().equals("meshes");

      final Elements ec = e.getChildElements();
      for (int index = 0; index < ec.size(); ++index) {
        final File m =
          SBSceneNormalized.loadMesh(root_directory, ec.get(index));
        this.meshes.put(m.toString(), new TreeSet<String>());
      }
    }

    private void loadTextures(
      final @Nonnull File root_directory,
      final @Nonnull Element e)
    {
      assert e.getLocalName().equals("textures");

      final Elements ec = e.getChildElements();
      for (int index = 0; index < ec.size(); ++index) {
        final File t =
          SBSceneNormalized.loadTexture(root_directory, ec.get(index));
        this.textures.add(t.toString());
      }
    }

    @SuppressWarnings("synthetic-access") private
      void
      normalizeAndSaveMeshes(
        final SBSceneState state)
    {
      for (final Entry<File, ConcurrentHashMap<String, SBMesh>> e : state.meshes
        .entrySet()) {
        final File file = e.getKey();
        final Collection<SBMesh> ms = e.getValue().values();

        final TreeSet<String> mesh_names = new TreeSet<String>();
        for (final SBMesh m : ms) {
          mesh_names.add(m.getName());
        }

        final String name = this.addName(file);
        assert this.meshes.containsKey(name) == false;
        this.meshes.put(name, mesh_names);

        this.log.debug("Saved mesh " + name + " containing " + mesh_names);
      }
    }

    @SuppressWarnings("synthetic-access") private
      void
      normalizeAndSaveTextures(
        final SBSceneState state)
    {
      for (final File file : state.textures.keySet()) {
        final String name = this.addName(file);
        assert this.textures.contains(name) == false;
        this.textures.add(name);
        this.log.debug("Saved texture " + name);
      }
    }

    private void putName(
      final @Nonnull File file,
      final @Nonnull String flat)
    {
      assert this.flat_names.contains(flat) == false;
      assert this.flat_to_files.containsKey(flat) == false;

      this.flat_names.add(flat);
      this.flat_to_files.put(flat, file);
      this.files_to_flat.put(file, flat);

      this.log.debug("Map " + flat + " <-> " + file);
    }

    @SuppressWarnings("synthetic-access") private void saveLights(
      final @Nonnull SBSceneState state)
    {
      for (final KLight light : state.light_descriptions.values()) {
        this.lights.add(light);
        this.log.debug("Saved light " + light);
      }
    }

    @SuppressWarnings("synthetic-access") private void saveObjects(
      final @Nonnull SBSceneState state)
    {
      for (final SBObjectDescription o : state.object_instances.values()) {
        this.instances.add(o);
        this.log.debug("Saved object " + o);
      }
    }

    @Nonnull Element toXML()
    {
      final String uri = SBSceneNormalized.SCENE_URI.toString();

      final Element e_scene = new Element("s:scene", uri);
      e_scene.addAttribute(new Attribute(
        "s:version",
        uri,
        SBSceneNormalized.SCENE_VERSION));

      final Element e_textures = new Element("s:textures", uri);
      for (final String t : this.textures) {
        final Element e_texture = new Element("s:texture", uri);
        e_texture.appendChild(t);
        e_textures.appendChild(e_texture);
      }

      final Element e_meshes = new Element("s:meshes", uri);
      for (final String mesh : this.meshes.keySet()) {
        final Element e_mesh = new Element("s:mesh", uri);
        e_mesh.appendChild(mesh);
        e_meshes.appendChild(e_mesh);
      }

      final Element e_lights = new Element("s:lights", uri);
      for (final KLight light : this.lights) {
        final Element e_light = SBSceneNormalized.lightToXML(light);
        e_lights.appendChild(e_light);
      }

      final Element e_instances = new Element("s:instances", uri);
      for (final SBObjectDescription o : this.instances) {
        final Element e_instance = this.instanceToXML(o);
        e_instances.appendChild(e_instance);
      }

      e_scene.appendChild(e_textures);
      e_scene.appendChild(e_meshes);
      e_scene.appendChild(e_lights);
      e_scene.appendChild(e_instances);
      return e_scene;
    }
  }

  private final @Nonnull ConcurrentHashMap<Integer, KLight>                            light_descriptions;
  private final @Nonnull AtomicInteger                                                 light_id_pool;
  private final @Nonnull Log                                                           log;
  private final @Nonnull ConcurrentHashMap<File, Pair<BufferedImage, Texture2DStatic>> textures;
  private final @Nonnull ConcurrentHashMap<File, ConcurrentHashMap<String, SBMesh>>    meshes;
  private final @Nonnull AtomicInteger                                                 object_id_pool;
  private final @Nonnull ConcurrentHashMap<Integer, SBObjectDescription>               object_instances;

  SBSceneState(
    final @Nonnull Log log)
  {
    this.log = new Log(log, "scene");
    this.light_descriptions = new ConcurrentHashMap<Integer, KLight>();
    this.light_id_pool = new AtomicInteger(0);
    this.textures =
      new ConcurrentHashMap<File, Pair<BufferedImage, Texture2DStatic>>();
    this.meshes =
      new ConcurrentHashMap<File, ConcurrentHashMap<String, SBMesh>>();
    this.object_id_pool = new AtomicInteger(0);
    this.object_instances =
      new ConcurrentHashMap<Integer, SBObjectDescription>();
  }

  @Override synchronized public @Nonnull
    Pair<List<Texture2DStatic>, List<SBMesh>>
    deleteAll()
  {
    this.log.debug("Deleting everything");

    final List<Texture2DStatic> tr = new LinkedList<Texture2DStatic>();
    final List<SBMesh> mr = new LinkedList<SBMesh>();

    for (final Pair<BufferedImage, Texture2DStatic> p : this.textures
      .values()) {
      tr.add(p.second);
    }

    for (final ConcurrentHashMap<String, SBMesh> mmap : this.meshes.values()) {
      mr.addAll(mmap.values());
    }

    this.light_descriptions.clear();
    this.meshes.clear();
    this.object_instances.clear();
    this.textures.clear();

    return new Pair<List<Texture2DStatic>, List<SBMesh>>(tr, mr);
  }

  @Override synchronized public void lightAdd(
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

  @Override synchronized public boolean lightExists(
    final @Nonnull Integer id)
  {
    return this.light_descriptions.containsKey(id);
  }

  @Override synchronized public @Nonnull Integer lightFreshID()
  {
    return Integer.valueOf(this.light_id_pool.getAndIncrement());
  }

  @Override synchronized public @CheckForNull KLight lightGet(
    final @Nonnull Integer key)
  {
    return this.light_descriptions.get(key);
  }

  @Override synchronized public void lightRemove(
    final @Nonnull Integer id)
  {
    this.log.debug("Removing light " + id);
    this.light_descriptions.remove(id);
  }

  @Override synchronized public @Nonnull List<KLight> lightsGetAll()
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

  @Override synchronized public void meshAdd(
    final @Nonnull File model,
    final @Nonnull SortedSet<SBMesh> objects)
  {
    if (this.meshes.containsKey(model)) {
      this.log.debug("Replacing model " + model);
    } else {
      this.log.debug("Adding model " + model);
    }

    final ConcurrentHashMap<String, SBMesh> ms =
      new ConcurrentHashMap<String, SBMesh>();
    for (final SBMesh m : objects) {
      this.log.debug("Adding mesh " + m.getName() + " for model " + model);
      ms.put(m.getName(), m);
    }

    this.meshes.put(model, ms);
  }

  @Override synchronized public @Nonnull
    Map<File, SortedSet<String>>
    meshesGet()
  {
    final HashMap<File, SortedSet<String>> results =
      new HashMap<File, SortedSet<String>>();

    for (final Entry<File, ConcurrentHashMap<String, SBMesh>> e : this.meshes
      .entrySet()) {
      final File f = e.getKey();
      final Collection<SBMesh> ms = e.getValue().values();
      final TreeSet<String> ns = new TreeSet<String>();
      for (final SBMesh m : ms) {
        ns.add(m.getName());
      }
      results.put(f, ns);
    }

    return results;
  }

  @Override synchronized public void objectAdd(
    final @Nonnull SBObjectDescription object)
  {
    if (this.object_instances.containsKey(object.getID())) {
      this.log.debug("Replacing object " + object.getID());
    } else {
      this.log.debug("Adding object " + object.getID());
    }

    this.object_instances.put(object.getID(), object);
  }

  @Override synchronized public void objectDelete(
    final @Nonnull Integer id)
  {
    this.object_instances.remove(id);
  }

  @Override synchronized public boolean objectExists(
    final @Nonnull Integer id)
  {
    return this.object_instances.containsKey(id);
  }

  @Override synchronized public Integer objectFreshID()
  {
    return Integer.valueOf(this.object_id_pool.getAndIncrement());
  }

  @Override synchronized public @CheckForNull SBObjectDescription objectGet(
    final @Nonnull Integer id)
  {
    return this.object_instances.get(id);
  }

  @Override synchronized public @Nonnull
    List<SBObjectDescription>
    objectsGetAll()
  {
    final ArrayList<SBObjectDescription> os =
      new ArrayList<SBObjectDescription>();
    for (final SBObjectDescription o : this.object_instances.values()) {
      os.add(o);
    }
    return os;
  }

  @Override synchronized public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Pair<BufferedImage, Texture2DStatic> texture)
  {
    this.textures.put(file, texture);
  }

  @Override synchronized public @Nonnull
    Map<File, BufferedImage>
    texturesGet()
  {
    final HashMap<File, BufferedImage> m = new HashMap<File, BufferedImage>();
    final Set<Entry<File, Pair<BufferedImage, Texture2DStatic>>> es =
      this.textures.entrySet();

    for (final Entry<File, Pair<BufferedImage, Texture2DStatic>> e : es) {
      m.put(e.getKey(), e.getValue().first);
    }

    return m;
  }

  @Override synchronized public
    Pair<Set<KLight>, Set<KMeshInstance>>
    rendererGetScene()
  {
    final HashSet<KLight> lights =
      new HashSet<KLight>(this.light_descriptions.values());
    final HashSet<KMeshInstance> instances = new HashSet<KMeshInstance>();

    for (final SBObjectDescription odesc : this.object_instances.values()) {
      final Integer id = odesc.getID();

      final File m = odesc.getModel();
      assert m != null;
      final ConcurrentHashMap<String, SBMesh> mesh_set = this.meshes.get(m);
      assert mesh_set != null;
      final String mo = odesc.getModelObject();
      assert mo != null;
      final SBMesh mesh = mesh_set.get(mo);
      assert mesh != null;

      final VectorReadable3F translation = odesc.getPosition();
      final QuaternionM4F orientation = new QuaternionM4F();
      final VectorReadable3F diffuse = new VectorI3F(1, 1, 1);

      final List<Texture2DStatic> diffuse_maps =
        new LinkedList<Texture2DStatic>();
      if (odesc.getDiffuseTexture() != null) {
        final Pair<BufferedImage, Texture2DStatic> p =
          this.textures.get(odesc.getDiffuseTexture());
        assert p != null;
        diffuse_maps.add(p.second);
      }

      final Option<Texture2DStatic> normal_map =
        (odesc.getNormalTexture() != null)
          ? new Option.Some<Texture2DStatic>(this.textures.get(odesc
            .getNormalTexture()).second) : new Option.None<Texture2DStatic>();

      final Option<Texture2DStatic> specular_map =
        (odesc.getSpecularTexture() != null)
          ? new Option.Some<Texture2DStatic>(this.textures.get(odesc
            .getSpecularTexture()).second)
          : new Option.None<Texture2DStatic>();

      final KMaterial material =
        new KMaterial(diffuse, diffuse_maps, normal_map, specular_map);
      final KTransform transform = new KTransform(translation, orientation);

      final KMeshInstance mi =
        new KMeshInstance(
          id,
          transform,
          mesh.getArrayBuffer(),
          mesh.getIndexBuffer(),
          material);
      instances.add(mi);
    }

    return new Pair<Set<KLight>, Set<KMeshInstance>>(lights, instances);
  }
}

interface SBSceneStateDeletion
{
  public @Nonnull Pair<List<Texture2DStatic>, List<SBMesh>> deleteAll();
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

  public void objectDelete(
    final @Nonnull Integer id);

  public boolean objectExists(
    final @Nonnull Integer id);

  public @Nonnull Integer objectFreshID();

  public @CheckForNull SBObjectDescription objectGet(
    final @Nonnull Integer id);

  public @Nonnull List<SBObjectDescription> objectsGetAll();
}

interface SBSceneStateRenderer
{
  public @Nonnull Pair<Set<KLight>, Set<KMeshInstance>> rendererGetScene();
}

interface SBSceneStateTextures
{
  public void textureAdd(
    final @Nonnull File file,
    final @Nonnull Pair<BufferedImage, Texture2DStatic> texture);

  public @Nonnull Map<File, BufferedImage> texturesGet();
}
