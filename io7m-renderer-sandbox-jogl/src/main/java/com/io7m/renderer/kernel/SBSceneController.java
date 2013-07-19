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
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import com.io7m.jaux.functional.Pair;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlog.Log;

public final class SBSceneController implements
  SBSceneControllerTextures,
  SBSceneControllerLights,
  SBSceneControllerMeshes
{
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

  private Future<BufferedImage> textureLoadImageIO(
    final @Nonnull File file)
  {
    final FutureTask<BufferedImage> f =
      new FutureTask<BufferedImage>(new Callable<BufferedImage>() {
        @Override public BufferedImage call()
          throws Exception
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
      });

    this.exec_pool.execute(f);
    return f;
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
          FileInputStream stream = null;

          try {
            stream = new FileInputStream(file);

            final Future<BufferedImage> f_image =
              SBSceneController.this.textureLoadImageIO(file);
            final Future<Texture2DStaticUsable> f_texture =
              SBSceneController.this.renderer.textureLoad(file, stream);

            final Texture2DStaticUsable rf = f_texture.get();
            final BufferedImage ri = f_image.get();

            SBSceneController.this.state.textureAdd(file, rf);
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
      });

    this.exec_pool.execute(f);
    return f;
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
}

interface SBSceneControllerLights
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

interface SBSceneControllerMeshes
{
  public @Nonnull Future<Pair<File, SortedSet<String>>> meshLoad(
    final @Nonnull File file);
}

interface SBSceneControllerTextures
{
  public @Nonnull Future<BufferedImage> textureLoad(
    final @Nonnull File file);
}
