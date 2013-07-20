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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.io7m.jaux.UnimplementedCodeException;
import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.SBException.SBExceptionImageLoading;
import com.io7m.renderer.kernel.SBSceneState.SBSceneNormalized;

public final class SBSceneController implements
  SBSceneControllerTextures,
  SBSceneControllerLights,
  SBSceneControllerMeshes,
  SBSceneControllerObjects,
  SBSceneControllerIO
{
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

  private final @Nonnull SBSceneState state;
  private final @Nonnull Log          log;
  protected final @Nonnull Log        log_textures;
  private final @Nonnull SBGLRenderer renderer;
  private final @Nonnull Executor     exec_pool;

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
  }

  @Override public void lightAdd(
    final @Nonnull KLight light)
  {
    this.state.lightAdd(light);
  }

  @Override public boolean lightExists(
    final @Nonnull Integer id)
  {
    return this.state.lightExists(id);
  }

  @Override public @Nonnull Integer lightFreshID()
  {
    return this.state.lightFreshID();
  }

  @Override public @CheckForNull KLight lightGet(
    final @Nonnull Integer id)
  {
    return this.state.lightGet(id);
  }

  @Override public void lightRemove(
    final @Nonnull Integer id)
  {
    this.state.lightRemove(id);
  }

  @Override public @Nonnull List<KLight> lightsGetAll()
  {
    return this.state.lightsGetAll();
  }

  @Override public @Nonnull Map<File, SortedSet<String>> meshesGet()
  {
    return this.state.meshesGet();
  }

  @Override public @Nonnull Future<Pair<File, SortedSet<String>>> meshLoad(
    final @Nonnull File file)
  {
    final FutureTask<Pair<File, SortedSet<String>>> f =
      new FutureTask<Pair<File, SortedSet<String>>>(
        new Callable<Pair<File, SortedSet<String>>>() {
          @SuppressWarnings("synthetic-access") @Override public @Nonnull
            Pair<File, SortedSet<String>>
            call()
              throws Exception
          {
            FileInputStream stream = null;

            try {
              stream = new FileInputStream(file);

              final Future<Pair<File, SortedSet<SBMesh>>> f_mesh =
                SBSceneController.this.renderer.meshLoad(file, stream);

              final Pair<File, SortedSet<SBMesh>> v = f_mesh.get();
              SBSceneController.this.state.meshAdd(v.first, v.second);

              final SortedSet<String> names = new TreeSet<String>();
              for (final SBMesh mesh : v.second) {
                names.add(mesh.getName());
              }

              return new Pair<File, SortedSet<String>>(file, names);
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
        });

    this.exec_pool.execute(f);
    return f;
  }

  @Override public void objectAdd(
    @Nonnull final SBObjectDescription object)
  {
    this.state.objectAdd(object);
  }

  @Override public boolean objectExists(
    @Nonnull final Integer id)
  {
    return this.state.objectExists(id);
  }

  @Override public @Nonnull Integer objectFreshID()
  {
    return this.state.objectFreshID();
  }

  @Override public @Nonnull SBObjectDescription objectGet(
    final @Nonnull Integer id)
  {
    return this.state.objectGet(id);
  }

  @Override public void objectRemove(
    @Nonnull final Integer id)
  {
    throw new UnimplementedCodeException();
  }

  @Override public @Nonnull List<SBObjectDescription> objectsGetAll()
  {
    return this.state.objectsGetAll();
  }

  @Override public @Nonnull Future<BufferedImage> textureLoad(
    final @Nonnull File file)
  {
    final FutureTask<BufferedImage> f =
      new FutureTask<BufferedImage>(new Callable<BufferedImage>() {
        @SuppressWarnings("synthetic-access") @Override public
          BufferedImage
          call()
            throws Exception
        {
          return SBSceneController.this.textureLoadActual(file);
        }
      });

    this.exec_pool.execute(f);
    return f;
  }

  private BufferedImage textureLoadActual(
    final File file)
    throws FileNotFoundException,
      InterruptedException,
      ExecutionException
  {
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);

      final Future<BufferedImage> f_image =
        SBSceneController.this.textureLoadImageIO(file);
      final Future<Texture2DStaticUsable> f_texture =
        SBSceneController.this.renderer.textureLoad(file, stream);

      final Texture2DStaticUsable rf = f_texture.get();
      final BufferedImage ri = f_image.get();

      SBSceneController.this.state.textureAdd(
        file,
        new Pair<BufferedImage, Texture2DStaticUsable>(ri, rf));
      return ri;
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

  @Override public @Nonnull Map<File, BufferedImage> texturesGet()
  {
    return this.state.texturesGet();
  }
}

interface SBSceneControllerIO
{
  public @Nonnull Future<Void> ioSaveScene(
    final @Nonnull File file);
}

interface SBSceneControllerLights
{
  public void lightAdd(
    final @Nonnull KLight light);

  public boolean lightExists(
    final @Nonnull Integer id);

  public @Nonnull Integer lightFreshID();

  public @CheckForNull KLight lightGet(
    final @Nonnull Integer id);

  public void lightRemove(
    final @Nonnull Integer id);

  public @Nonnull List<KLight> lightsGetAll();
}

interface SBSceneControllerMeshes
{
  public @Nonnull Map<File, SortedSet<String>> meshesGet();

  public @Nonnull Future<Pair<File, SortedSet<String>>> meshLoad(
    final @Nonnull File file);
}

interface SBSceneControllerObjects
{
  public void objectAdd(
    final @Nonnull SBObjectDescription object);

  public boolean objectExists(
    final @Nonnull Integer id);

  public @Nonnull Integer objectFreshID();

  public @Nonnull SBObjectDescription objectGet(
    final @Nonnull Integer id);

  public void objectRemove(
    final @Nonnull Integer id);

  public @Nonnull List<SBObjectDescription> objectsGetAll();
}

interface SBSceneControllerTextures
{
  public @Nonnull Future<BufferedImage> textureLoad(
    final @Nonnull File file);

  public @Nonnull Map<File, BufferedImage> texturesGet();
}
