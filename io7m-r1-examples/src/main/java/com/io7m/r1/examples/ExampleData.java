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

import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

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

  public static String getDataPath(
    final ExampleRendererType renderer,
    final ExampleType scene)
  {
    final StringBuilder name = new StringBuilder();
    final String scene_name = scene.exampleGetName();
    final String render_name = renderer.exampleRendererGetName().toString();

    name.append("/com/io7m/r1/examples/results/");
    name.append(scene_name);
    name.append("/");
    name.append(render_name);
    final String r = name.toString();
    assert r != null;
    return r;
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

  @SuppressWarnings("boxing") public static String getResultFile(
    final ExampleRendererType renderer,
    final ExampleType scene,
    final int view)
  {
    final String r =
      String.format(
        "%s/%d.png",
        ExampleData.getDataPath(renderer, scene),
        view);
    assert r != null;
    return r;
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

  public static @Nullable InputStream getResultImageStream(
    final ExampleRendererType renderer,
    final ExampleType scene,
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
