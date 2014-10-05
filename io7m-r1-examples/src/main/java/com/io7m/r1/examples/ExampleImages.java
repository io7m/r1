/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

package com.io7m.r1.examples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemError.Code;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;

/**
 * Available example images.
 *
 * @param <T>
 *          The precise type of loaded images.
 */

public final class ExampleImages<T>
{
  private static final PathVirtual        SCENES_BASE;
  private static final String             SCENES_BASE_TEXT;

  static {
    try {
      SCENES_BASE_TEXT = "/com/io7m/r1/examples/results/";
      SCENES_BASE = PathVirtual.ofString(ExampleImages.SCENES_BASE_TEXT);
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final FilesystemType            filesystem;
  private final ExampleImageLoaderType<T> loader;

  /**
   * Construct images with the given loader.
   *
   * @param in_loader
   *          The loader.
   * @param in_log
   *          A log interface.
   */

  public ExampleImages(
    final ExampleImageLoaderType<T> in_loader,
    final LogUsableType in_log)
  {
    try {
      this.filesystem = Filesystem.makeWithoutArchiveDirectory(in_log);
      this.filesystem.mountClasspathArchive(
        ExampleImages.class,
        PathVirtual.ROOT);
      this.loader = NullCheck.notNull(in_loader, "Loader");
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * Retrieve the image for the renderer, scene, and view.
   *
   * @param renderer
   *          The renderer.
   * @param scene
   *          The scene.
   * @param view
   *          The view.
   * @return A loaded image.
   * @throws Exception
   *           On errors.
   */

  public synchronized OptionType<T> getImage(
    final ExampleRendererName renderer,
    final Class<? extends ExampleSceneType> scene,
    final int view)
    throws Exception
  {
    final PathVirtual path_scene =
      ExampleImages.SCENES_BASE.appendName(scene.getCanonicalName());
    final PathVirtual path_renderer =
      path_scene.appendName(renderer.toString());
    final PathVirtual path_view =
      path_renderer.appendName(String.format("%d.png", view));

    try {
      final InputStream stream = this.filesystem.openFile(path_view);
      try {
        return Option
          .some(this.loader.loadImage(path_view.toString(), stream));
      } finally {
        stream.close();
      }
    } catch (final FilesystemError e) {
      if (e.getCode() != Code.FS_ERROR_NONEXISTENT) {
        throw e;
      }
    }

    return Option.none();
  }

  /**
   * Retrieve the list of renderers for which results exist for the given
   * scene.
   *
   * @param scene
   *          The scene.
   * @return A list of renderers.
   * @throws Exception
   *           On errors.
   */

  public synchronized List<ExampleRendererName> getSceneRenderers(
    final Class<? extends ExampleSceneType> scene)
    throws Exception
  {
    try {
      final PathVirtual path_scene =
        ExampleImages.SCENES_BASE.appendName(scene.getCanonicalName());
      final SortedSet<String> renderers =
        this.filesystem.listDirectory(path_scene);

      final List<ExampleRendererName> names =
        new ArrayList<ExampleRendererName>();

      for (final String r : renderers) {
        names.add(new ExampleRendererName(r));
      }

      return names;
    } catch (final FilesystemError e) {
      if (e.getCode() != Code.FS_ERROR_NONEXISTENT) {
        throw e;
      }
    }

    return new ArrayList<ExampleRendererName>();
  }
}
