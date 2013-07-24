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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;

import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Log;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.kernel.SBException.SBExceptionImageLoading;
import com.io7m.renderer.kernel.SBZipUtilities.TemporaryDirectory;

interface SBSceneChangeListener
{
  public void sceneChanged();
}

interface SBSceneChangeListenerRegistration
{
  public void changeListenerAdd(
    final @Nonnull SBSceneChangeListener listener);
}

public final class SBSceneController implements
  SBSceneControllerIO,
  SBSceneControllerLights,
  SBSceneControllerModels,
  SBSceneControllerInstances,
  SBSceneControllerRenderer,
  SBSceneControllerRendererControl,
  SBSceneControllerTextures
{
  private static void ioSaveSceneActualCopyFiles(
    final @Nonnull Log log,
    final @Nonnull SBSceneDescription scene_desc_current,
    final @Nonnull SBSceneDescription scene_desc_saving,
    final @Nonnull ZipOutputStream fo)
    throws IOException
  {
    final byte[] buffer = new byte[8192];

    for (final SBTextureDescription source : scene_desc_current
      .getTextureDescriptions()) {

      final SBTextureDescription target =
        scene_desc_saving.getTexture(source.getName());

      SBSceneController.ioSaveSceneCopyFileIntoZip(
        log,
        fo,
        buffer,
        source.getFile(),
        target.getFile());
    }

    for (final SBModelDescription source : scene_desc_current
      .getModelDescriptions()) {

      final SBModelDescription target =
        scene_desc_saving.getModel(source.getName());

      SBSceneController.ioSaveSceneCopyFileIntoZip(
        log,
        fo,
        buffer,
        source.getFile(),
        target.getFile());
    }
  }

  private static void ioSaveSceneActualSerializeXML(
    final @Nonnull SBSceneDescription scene,
    final @Nonnull ZipOutputStream fo)
    throws UnsupportedEncodingException,
      IOException
  {
    final Element xml = scene.toXML();
    final Document doc = new Document(xml);
    final Serializer s = new Serializer(fo, "UTF-8");
    s.setIndent(2);
    s.setMaxLength(80);

    final ZipEntry entry = new ZipEntry("scene.xml");
    fo.putNextEntry(entry);
    s.write(doc);
    fo.closeEntry();
  }

  private static void ioSaveSceneCopyFileIntoZip(
    final @Nonnull Log log,
    final @Nonnull ZipOutputStream fo,
    final @Nonnull byte[] buffer,
    final @Nonnull File file_input,
    final @Nonnull File file_output)
    throws FileNotFoundException,
      IOException
  {
    log.debug("Copying " + file_input + " into zip at " + file_output);

    final FileInputStream stream = new FileInputStream(file_input);
    try {
      final ZipEntry entry = new ZipEntry(file_output.toString());
      fo.putNextEntry(entry);

      for (;;) {
        final int r = stream.read(buffer);
        if (r == -1) {
          break;
        }
        fo.write(buffer, 0, r);
      }

      fo.flush();
      fo.closeEntry();
    } finally {
      stream.close();
    }
  }

  private static @Nonnull BufferedImage textureLoadImageIOActual(
    final @Nonnull File file)
    throws FileNotFoundException,
      IOException,
      SBExceptionImageLoading
  {
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(file);
      final BufferedImage image = ImageIO.read(stream);
      if (null == image) {
        throw new SBException.SBExceptionImageLoading(
          file,
          "Unable to parse image");
      }
      return image;
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  private final @Nonnull Log                               log;
  protected final @Nonnull Log                             log_textures;
  private final @Nonnull SBGLRenderer                      renderer;
  private final @Nonnull Executor                          exec_pool;
  private final @Nonnull LinkedList<SBSceneChangeListener> listeners;
  private final @Nonnull SBUniqueNames                     names;
  private final @Nonnull AtomicReference<SBScene>          scene_current;

  public SBSceneController(
    final @Nonnull SBGLRenderer renderer,
    final @Nonnull Log log)
  {
    this.log = new Log(log, "control");
    this.log_textures = new Log(this.log, "textures");
    this.renderer = renderer;
    this.exec_pool = Executors.newCachedThreadPool();
    this.listeners = new LinkedList<SBSceneChangeListener>();
    this.names = new SBUniqueNames();
    this.scene_current = new AtomicReference<SBScene>(SBScene.empty());
  }

  @Override public void changeListenerAdd(
    final @Nonnull SBSceneChangeListener listener)
  {
    this.log.debug("Registered change listener " + listener);
    this.listeners.add(listener);
  }

  @Override public @Nonnull Future<Void> ioLoadScene(
    final @Nonnull File file)
  {
    final FutureTask<Void> f = new FutureTask<Void>(new Callable<Void>() {
      @SuppressWarnings("synthetic-access") @Override public Void call()
        throws Exception
      {
        SBSceneController.this.ioLoadSceneActual(file);
        return null;
      }
    });

    this.exec_pool.execute(f);
    return f;
  }

  private void ioLoadSceneActual(
    final @Nonnull File file)
    throws FileNotFoundException,
      IOException,
      ValidityException,
      ParsingException,
      InterruptedException,
      ExecutionException
  {
    SBSceneController.this.log.debug("Loading scene from " + file);

    final TemporaryDirectory d = SBZipUtilities.unzip(this.log, file);

    final Builder parser = new Builder();
    final Document doc = parser.build(new File(d.getFile(), "scene.xml"));

    final SBSceneDescription desc =
      SBSceneDescription.fromXML(d, doc.getRootElement());

    SBScene scene = SBScene.empty();

    for (final SBTextureDescription t : desc.getTextureDescriptions()) {
      final Future<SBTexture> tf = this.textureLoad(t);
      scene = scene.textureAdd(tf.get());
    }

    for (final SBModelDescription m : desc.getModelDescriptions()) {
      final Future<SBModel> mf = this.modelLoad(m);
      scene = scene.modelAdd(mf.get());
    }

    for (final KLight light : desc.getLights()) {
      scene = scene.lightAdd(light);
    }

    for (final SBInstanceDescription idesc : desc.getInstanceDescriptions()) {
      scene = scene.instanceAddByDescription(idesc);
    }

    this.stateUpdate(scene);
  }

  @Override public @Nonnull Future<Void> ioSaveScene(
    final @Nonnull File file)
  {
    final FutureTask<Void> f = new FutureTask<Void>(new Callable<Void>() {
      @SuppressWarnings("synthetic-access") @Override public Void call()
        throws Exception
      {
        SBSceneController.this.ioSaveSceneActual(file);
        return null;
      }
    });

    this.exec_pool.execute(f);
    return f;
  }

  private void ioSaveSceneActual(
    final @Nonnull File file)
    throws UnsupportedEncodingException,
      IOException
  {
    SBSceneController.this.log.debug("Writing scene to " + file);

    final SBScene scene = this.scene_current.get();
    final SBSceneDescription scene_desc_current =
      scene.makeDescription(false);
    final SBSceneDescription scene_desc_saving = scene.makeDescription(true);

    final ZipOutputStream fo =
      new ZipOutputStream(new FileOutputStream(file));
    fo.setLevel(9);

    SBSceneController.ioSaveSceneActualSerializeXML(scene_desc_saving, fo);
    SBSceneController.ioSaveSceneActualCopyFiles(
      this.log,
      scene_desc_current,
      scene_desc_saving,
      fo);

    fo.finish();
    fo.flush();
    fo.close();

    SBSceneController.this.log.debug("Wrote scene to " + file);
  }

  private @Nonnull Future<SBModel> modelLoad(
    final @Nonnull SBModelDescription m)
  {
    final FutureTask<SBModel> f =
      new FutureTask<SBModel>(new Callable<SBModel>() {
        @SuppressWarnings("synthetic-access") @Override public SBModel call()
          throws Exception
        {
          return SBSceneController.this.modelLoadActual(m);
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  private @Nonnull SBModel modelLoadActual(
    final @Nonnull SBModelDescription description)
    throws FileNotFoundException,
      InterruptedException,
      ExecutionException
  {
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(description.getFile());

      final Future<SBModel> f_model =
        SBSceneController.this.renderer.modelLoad(description, stream);

      return f_model.get();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override public @Nonnull
    Pair<Collection<KLight>, Collection<KMeshInstance>>
    rendererGetScene()
  {
    final SBScene scene = this.scene_current.get();

    final Collection<KLight> lights = scene.lightsGet();
    MapPSet<KMeshInstance> meshes = HashTreePSet.empty();

    for (final SBInstance i : scene.instancesGet()) {
      final SBModel model = scene.modelGet(i.getModel());
      final SBMesh mesh = model.getMesh(i.getModelObject());

      final QuaternionM4F orientation = new QuaternionM4F();
      final Integer id = i.getID();

      final KTransform transform =
        new KTransform(i.getPosition(), orientation);

      final RVectorI3F<RSpaceRGB> diffuse =
        new RVectorI3F<RSpaceRGB>(1.0f, 1.0f, 1.0f);

      final List<Texture2DStatic> diffuse_maps =
        new LinkedList<Texture2DStatic>();
      if (i.getDiffuse() != null) {
        diffuse_maps.add(i.getDiffuse().getTexture());
      }

      final Option<Texture2DStatic> normal_map =
        (i.getNormal() == null)
          ? new Option.None<Texture2DStatic>()
          : new Option.Some<Texture2DStatic>(i.getNormal().getTexture());
      final Option<Texture2DStatic> specular_map =
        (i.getSpecular() == null)
          ? new Option.None<Texture2DStatic>()
          : new Option.Some<Texture2DStatic>(i.getSpecular().getTexture());

      final KMaterial material =
        new KMaterial(diffuse, diffuse_maps, normal_map, specular_map);

      meshes =
        meshes.plus(new KMeshInstance(
          id,
          transform,
          mesh.getArrayBuffer(),
          mesh.getIndexBuffer(),
          material));
    }

    return new Pair<Collection<KLight>, Collection<KMeshInstance>>(
      lights,
      meshes);
  }

  @Override public void rendererSetType(
    final @Nonnull SBRendererType type)
  {
    this.log.debug("Selecting renderer " + type);
    this.renderer.setRenderer(type);
  }

  @Override public void rendererShowAxes(
    final boolean enabled)
  {
    this.renderer.setShowAxes(enabled);
  }

  @Override public void rendererShowGrid(
    final boolean enabled)
  {
    this.renderer.setShowGrid(enabled);
  }

  @Override public void sceneInstanceAdd(
    final @Nonnull SBInstance instance)
  {
    this.stateUpdate(this.scene_current.get().instanceAdd(instance));
  }

  @Override public void sceneInstanceAddByDescription(
    final @Nonnull SBInstanceDescription desc)
  {
    this.stateUpdate(this.scene_current.get().instanceAddByDescription(desc));
  }

  @Override public boolean sceneInstanceExists(
    final @Nonnull Integer id)
  {
    return this.scene_current.get().instanceExists(id);
  }

  @Override public @Nonnull Integer sceneInstanceFreshID()
  {
    final Pair<SBScene, Integer> p =
      this.scene_current.get().instanceFreshID();
    this.stateUpdate(p.first);
    return p.second;
  }

  @Override public @Nonnull SBInstance sceneInstanceGet(
    final @Nonnull Integer id)
  {
    return this.scene_current.get().instanceGet(id);
  }

  @Override public void sceneInstanceRemove(
    final @Nonnull Integer id)
  {
    this.stateUpdate(this.scene_current.get().removeInstance(id));
  }

  @Override public @Nonnull Collection<SBInstance> sceneInstancesGetAll()
  {
    return this.scene_current.get().instancesGet();
  }

  @Override public void sceneLightAdd(
    final @Nonnull KLight light)
  {
    this.stateUpdate(this.scene_current.get().lightAdd(light));
  }

  @Override public boolean sceneLightExists(
    final @Nonnull Integer id)
  {
    return this.scene_current.get().lightExists(id);
  }

  @Override public @Nonnull Integer sceneLightFreshID()
  {
    final Pair<SBScene, Integer> p = this.scene_current.get().lightFreshID();
    this.stateUpdate(p.first);
    return p.second;
  }

  @Override public @CheckForNull KLight sceneLightGet(
    final @Nonnull Integer id)
  {
    return this.scene_current.get().lightGet(id);
  }

  @Override public void sceneLightRemove(
    final @Nonnull Integer id)
  {
    this.stateUpdate(this.scene_current.get().lightRemove(id));
  }

  @Override public @Nonnull Collection<KLight> sceneLightsGetAll()
  {
    return this.scene_current.get().lightsGet();
  }

  @Override public @Nonnull Future<SBModel> sceneModelLoad(
    final @Nonnull File file)
  {
    final SBModelDescription desc =
      new SBModelDescription(file, this.names.get(file.getName()));

    final FutureTask<SBModel> f =
      new FutureTask<SBModel>(new Callable<SBModel>() {
        @SuppressWarnings("synthetic-access") @Override public SBModel call()
          throws Exception
        {
          final SBModel m = SBSceneController.this.modelLoadActual(desc);
          SBSceneController.this
            .stateUpdate(SBSceneController.this.scene_current.get().modelAdd(
              m));
          return m;
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull Map<String, SBModel> sceneModelsGet()
  {
    return this.scene_current.get().modelsGet();
  }

  @Override public @Nonnull Future<SBTexture> sceneTextureLoad(
    final @Nonnull File file)
  {
    final SBTextureDescription desc =
      new SBTextureDescription(file, this.names.get(file.getName()));

    final FutureTask<SBTexture> f =
      new FutureTask<SBTexture>(new Callable<SBTexture>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTexture
          call()
            throws Exception
        {
          final SBTexture t = SBSceneController.this.textureLoadActual(desc);
          SBSceneController.this
            .stateUpdate(SBSceneController.this.scene_current
              .get()
              .textureAdd(t));
          return t;
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public @Nonnull Map<String, SBTexture> sceneTexturesGet()
  {
    return this.scene_current.get().texturesGet();
  }

  private void stateChangedNotifyListeners()
  {
    for (final SBSceneChangeListener l : this.listeners) {
      l.sceneChanged();
    }
  }

  private void stateUpdate(
    final @Nonnull SBScene scene)
  {
    this.log.debug("scene state updated");
    this.scene_current.set(scene);
    this.stateChangedNotifyListeners();
  }

  private @Nonnull Future<SBTexture> textureLoad(
    final @Nonnull SBTextureDescription t)
  {
    final FutureTask<SBTexture> f =
      new FutureTask<SBTexture>(new Callable<SBTexture>() {
        @SuppressWarnings("synthetic-access") @Override public
          SBTexture
          call()
            throws Exception
        {
          return SBSceneController.this.textureLoadActual(t);
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  private @Nonnull SBTexture textureLoadActual(
    final @Nonnull SBTextureDescription description)
    throws FileNotFoundException,
      InterruptedException,
      ExecutionException
  {
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(description.getFile());

      final Future<BufferedImage> f_image =
        SBSceneController.this.textureLoadImageIO(description.getFile());
      final Future<Texture2DStatic> f_texture =
        SBSceneController.this.renderer.textureLoad(
          description.getName(),
          stream);

      final Texture2DStatic rf = f_texture.get();
      final BufferedImage ri = f_image.get();
      return new SBTexture(rf, ri, description);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private Future<BufferedImage> textureLoadImageIO(
    final @Nonnull File file)
  {
    final FutureTask<BufferedImage> f =
      new FutureTask<BufferedImage>(new Callable<BufferedImage>() {
        @SuppressWarnings("synthetic-access") @Override public
          BufferedImage
          call()
            throws Exception
        {
          return SBSceneController.textureLoadImageIOActual(file);
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public void rendererSetBackgroundColour(
    final float r,
    final float g,
    final float b)
  {
    this.renderer.setBackgroundColour(r, g, b);
  }
}

interface SBSceneControllerInstances extends
  SBSceneChangeListenerRegistration
{
  public void sceneInstanceAdd(
    final @Nonnull SBInstance instance);

  public void sceneInstanceAddByDescription(
    final @Nonnull SBInstanceDescription desc);

  public boolean sceneInstanceExists(
    final @Nonnull Integer id);

  public @Nonnull Integer sceneInstanceFreshID();

  public @CheckForNull SBInstance sceneInstanceGet(
    final @Nonnull Integer id);

  public void sceneInstanceRemove(
    final @Nonnull Integer id);

  public @Nonnull Collection<SBInstance> sceneInstancesGetAll();
}

interface SBSceneControllerIO
{
  public @Nonnull Future<Void> ioLoadScene(
    final @Nonnull File file);

  public @Nonnull Future<Void> ioSaveScene(
    final @Nonnull File file);
}

interface SBSceneControllerLights extends SBSceneChangeListenerRegistration
{
  public void sceneLightAdd(
    final @Nonnull KLight light);

  public boolean sceneLightExists(
    final @Nonnull Integer id);

  public @Nonnull Integer sceneLightFreshID();

  public @CheckForNull KLight sceneLightGet(
    final @Nonnull Integer id);

  public void sceneLightRemove(
    final @Nonnull Integer id);

  public @Nonnull Collection<KLight> sceneLightsGetAll();
}

interface SBSceneControllerModels extends SBSceneChangeListenerRegistration
{
  public @Nonnull Future<SBModel> sceneModelLoad(
    final @Nonnull File file);

  public @Nonnull Map<String, SBModel> sceneModelsGet();
}

interface SBSceneControllerRenderer
{
  public @Nonnull
    Pair<Collection<KLight>, Collection<KMeshInstance>>
    rendererGetScene();
}

interface SBSceneControllerRendererControl
{
  public void rendererSetType(
    final @Nonnull SBRendererType type);

  public void rendererShowAxes(
    final boolean enabled);

  public void rendererShowGrid(
    final boolean enabled);

  public void rendererSetBackgroundColour(
    float r,
    float g,
    float b);
}

interface SBSceneControllerTextures extends SBSceneChangeListenerRegistration
{
  public @Nonnull Future<SBTexture> sceneTextureLoad(
    final @Nonnull File file);

  public @Nonnull Map<String, SBTexture> sceneTexturesGet();
}
