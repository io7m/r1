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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
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

import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStatic;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.SBException.SBExceptionImageLoading;
import com.io7m.renderer.kernel.SBSceneState.SBSceneNormalized;
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
  SBSceneControllerMeshes,
  SBSceneControllerObjects,
  SBSceneControllerRenderer,
  SBSceneControllerTextures
{
  private static void ioSaveSceneActualCopyFiles(
    final @Nonnull SBSceneNormalized nstate,
    final @Nonnull ZipOutputStream fo)
    throws IOException
  {
    final Map<String, File> mappings = nstate.getFileMappings();
    final byte[] buffer = new byte[8192];

    for (final Entry<String, File> e : mappings.entrySet()) {
      final String file_output = e.getKey();
      final File file_input = e.getValue();

      final FileInputStream stream = new FileInputStream(file_input);
      try {
        final ZipEntry entry = new ZipEntry(file_output);
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
  }

  private static void ioSaveSceneActualSerializeXML(
    final @Nonnull SBSceneNormalized nstate,
    final @Nonnull ZipOutputStream fo)
    throws UnsupportedEncodingException,
      IOException
  {
    final Element xml = nstate.toXML();
    final Document doc = new Document(xml);
    final Serializer s = new Serializer(fo, "UTF-8");
    s.setIndent(2);
    s.setMaxLength(80);

    final ZipEntry entry = new ZipEntry("scene.xml");
    fo.putNextEntry(entry);
    s.write(doc);
    fo.closeEntry();
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

  private final @Nonnull SBSceneState                      state;
  private final @Nonnull Log                               log;
  protected final @Nonnull Log                             log_textures;
  private final @Nonnull SBGLRenderer                      renderer;
  private final @Nonnull Executor                          exec_pool;
  private final @Nonnull LinkedList<SBSceneChangeListener> listeners;
  private final @Nonnull SBUniqueNames                     names;
  private final @Nonnull AtomicReference<SBScene>          scene_current;

  public SBSceneController(
    final @Nonnull SBSceneState state,
    final @Nonnull SBGLRenderer renderer,
    final @Nonnull Log log)
  {
    this.log = new Log(log, "control");
    this.log_textures = new Log(this.log, "textures");
    this.state = state;
    this.renderer = renderer;
    this.exec_pool = Executors.newCachedThreadPool();
    this.listeners = new LinkedList<SBSceneChangeListener>();
    this.names = new SBUniqueNames();
    this.scene_current = new AtomicReference<SBScene>();
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
      SBSceneDescription.fromXML(doc.getRootElement());

    SBScene scene = SBScene.empty();

    for (final SBTextureDescription t : desc.getTextureDescriptions()) {
      final Future<SBTexture> tf = this.textureLoad(t);
      scene = scene.addTexture(tf.get());
    }

    for (final SBModelDescription m : desc.getModelDescriptions()) {
      final Future<SBModel> mf = this.modelLoad(m);
      scene = scene.addModel(mf.get());
    }

    for (final KLight light : desc.getLights()) {
      scene = scene.addLight(light);
    }

    for (final SBInstanceDescription o : desc.getInstanceDescriptions()) {
      final SBTexture diff =
        (o.getDiffuse() == null) ? null : scene.getTexture(o.getDiffuse());
      final SBTexture norm =
        (o.getNormal() == null) ? null : scene.getTexture(o.getNormal());
      final SBTexture spec =
        (o.getSpecular() == null) ? null : scene.getTexture(o.getSpecular());

      final SBInstance instance =
        new SBInstance(
          o.getID(),
          o.getPosition(),
          o.getOrientation(),
          o.getModel(),
          o.getModelObject(),
          diff,
          norm,
          spec);

      scene = scene.addInstance(instance);
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
    throws FileNotFoundException,
      UnsupportedEncodingException,
      IOException
  {
    SBSceneController.this.log.debug("Writing scene to " + file);

    final SBSceneNormalized nstate =
      new SBSceneState.SBSceneNormalized(
        SBSceneController.this.log,
        SBSceneController.this.state);

    final ZipOutputStream fo =
      new ZipOutputStream(new FileOutputStream(file));
    fo.setLevel(9);

    SBSceneController.ioSaveSceneActualSerializeXML(nstate, fo);
    SBSceneController.ioSaveSceneActualCopyFiles(nstate, fo);

    fo.finish();
    fo.flush();
    fo.close();

    SBSceneController.this.log.debug("Scene written to " + file);

    this.stateChangedNotifyListeners();
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

  @Override public @Nonnull
    Pair<Set<KLight>, Set<KMeshInstance>>
    rendererGetScene()
  {
    throw new UnimplementedCodeException();
  }

  private void stateChangedNotifyListeners()
  {
    for (final SBSceneChangeListener l : this.listeners) {
      l.sceneChanged();
    }
  }

  @Override public void sceneLightAdd(
    final @Nonnull KLight light)
  {
    throw new UnimplementedCodeException();
  }

  @Override public boolean sceneLightExists(
    final @Nonnull Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Integer sceneLightFreshID()
  {
    throw new UnimplementedCodeException();
  }

  @Override public @CheckForNull KLight sceneLightGet(
    final @Nonnull Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public void sceneLightRemove(
    final @Nonnull Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull List<KLight> sceneLightsGetAll()
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Map<File, SortedSet<String>> sceneMeshesGet()
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Future<SBModel> sceneMeshLoad(
    final @Nonnull File file)
  {
    throw new UnimplementedCodeException();
  }

  @Override public void sceneObjectAdd(
    @Nonnull final SBObjectDescription object)
  {
    throw new UnimplementedCodeException();
  }

  @Override public boolean sceneObjectExists(
    @Nonnull final Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Integer sceneObjectFreshID()
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull SBObjectDescription sceneObjectGet(
    final @Nonnull Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public void sceneObjectRemove(
    @Nonnull final Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull List<SBObjectDescription> sceneObjectsGetAll()
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Future<SBTexture> sceneTextureLoad(
    final @Nonnull File file)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull Map<File, BufferedImage> sceneTexturesGet()
  {
    throw new UnimplementedCodeException();
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

  public @Nonnull List<KLight> sceneLightsGetAll();
}

interface SBSceneControllerMeshes extends SBSceneChangeListenerRegistration
{
  public @Nonnull Map<File, SortedSet<String>> sceneMeshesGet();

  public @Nonnull Future<SBModel> sceneMeshLoad(
    final @Nonnull File file);
}

interface SBSceneControllerObjects extends SBSceneChangeListenerRegistration
{
  public void sceneObjectAdd(
    final @Nonnull SBObjectDescription object);

  public boolean sceneObjectExists(
    final @Nonnull Integer id);

  public @Nonnull Integer sceneObjectFreshID();

  public @Nonnull SBObjectDescription sceneObjectGet(
    final @Nonnull Integer id);

  public void sceneObjectRemove(
    final @Nonnull Integer id);

  public @Nonnull List<SBObjectDescription> sceneObjectsGetAll();
}

interface SBSceneControllerRenderer
{
  public @Nonnull Pair<Set<KLight>, Set<KMeshInstance>> rendererGetScene();
}

interface SBSceneControllerTextures extends SBSceneChangeListenerRegistration
{
  public @Nonnull Future<SBTexture> sceneTextureLoad(
    final @Nonnull File file);

  public @Nonnull Map<File, BufferedImage> sceneTexturesGet();
}
