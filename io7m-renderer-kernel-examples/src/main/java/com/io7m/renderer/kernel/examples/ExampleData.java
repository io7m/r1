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

package com.io7m.renderer.kernel.examples;

import java.io.InputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.io7m.jaux.UnreachableCodeException;

/**
 * Functions for retrieving example data.
 */

public final class ExampleData
{
  /**
   * Retrieve the base results directory.
   * 
   * @param renderer
   *          The renderer
   * @param scene
   *          The scene
   * @return The path to the results directory
   */

  public static @Nonnull String getDataPath(
    final @Nonnull ExampleRendererType renderer,
    final @Nonnull ExampleSceneType scene)
  {
    final StringBuilder name = new StringBuilder();
    final String scene_name = scene.exampleGetName();
    final String render_name = renderer.rendererGetName();

    name.append("/com/io7m/renderer/kernel/examples/results/");
    name.append(scene_name);
    name.append("/");
    name.append(render_name);
    return name.toString();
  }

  /**
   * Return the image for the given view of the given scene as rendered by the
   * given renderer.
   * 
   * @param renderer
   *          The renderer
   * @param scene
   *          The scene
   * @param view
   *          The view
   * @return The name of the image file, or <code>null</code> if nonexistent
   */

  @SuppressWarnings("boxing") public static @Nonnull String getResultFile(
    final @Nonnull ExampleRendererType renderer,
    final @Nonnull ExampleSceneType scene,
    final int view)
  {
    return String.format(
      "%s/%d.png",
      ExampleData.getDataPath(renderer, scene),
      view);
  }

  /**
   * Return the image for the given view of the given scene as rendered by the
   * given renderer.
   * 
   * @param renderer
   *          The renderer
   * @param scene
   *          The scene
   * @param view
   *          The view
   * @return An image stream, or <code>null</code> if nonexistent
   */

  public static @CheckForNull InputStream getResultImageStream(
    final @Nonnull ExampleRendererType renderer,
    final @Nonnull ExampleSceneType scene,
    final int view)
  {
    return ExampleData.class.getResourceAsStream(ExampleData.getResultFile(
      renderer,
      scene,
      view));
  }

  private ExampleData()
  {
    throw new UnreachableCodeException();
  }
}
