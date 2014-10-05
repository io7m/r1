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

package com.io7m.r1.examples.viewer;

import java.awt.image.BufferedImage;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.examples.ExampleImages;
import com.io7m.r1.examples.ExampleRendererName;
import com.io7m.r1.examples.ExampleSceneType;

/**
 * A simple image display that caches loaded images.
 */

final class VExpectedImage extends JLabel
{
  private static final long                        serialVersionUID;

  static {
    serialVersionUID = 1636894751686016138L;
  }

  private final WeakHashMap<String, BufferedImage> expected_image_cache;
  private final AtomicInteger                      current_view_index;
  private final LogUsableType                      rlog;
  private final ExampleSceneType                   scene;
  private final ExampleImages<BufferedImage>       example_images;
  private @Nullable ExampleRendererName            previous;

  public VExpectedImage(
    final LogUsableType in_log,
    final ExampleImages<BufferedImage> in_example_images,
    final ExampleSceneType in_scene,
    final AtomicInteger in_current_view_index)
  {
    this.rlog = in_log.with("results");
    this.scene = NullCheck.notNull(in_scene, "Scene");
    this.current_view_index =
      NullCheck.notNull(in_current_view_index, "View index");
    this.example_images =
      NullCheck.notNull(in_example_images, "Example images");
    this.expected_image_cache = new WeakHashMap<String, BufferedImage>();
    this.previous = null;
  }

  public void updateWithSameRenderer()
    throws Exception
  {
    if (this.previous != null) {
      this.update(this.previous);
    }
  }

  public void update(
    final ExampleRendererName name)
    throws Exception
  {
    this.previous = NullCheck.notNull(name, "Renderer");

    final int view_index = this.current_view_index.get();
    final Class<? extends ExampleSceneType> sc = this.scene.getClass();

    this.rlog.debug(String.format(
      "Switching to view %d for renderer %s",
      view_index,
      name));

    final StringBuilder b = new StringBuilder();
    b.append(name);
    b.append(":");
    b.append(sc.getCanonicalName());
    b.append(":");
    b.append(view_index);
    final String key = b.toString();

    if (this.expected_image_cache.get(key) != null) {
      this.rlog.debug(String.format("Got cached image %s", key));
      final BufferedImage r = this.expected_image_cache.get(key);
      this.setIcon(new ImageIcon(r));
    } else {
      final OptionType<BufferedImage> r =
        this.example_images.getImage(name, sc, view_index);
      r.accept(new OptionVisitorType<BufferedImage, Unit>() {
        @Override public Unit none(
          final None<BufferedImage> n)
        {
          VExpectedImage.this.setIcon(null);
          return Unit.unit();
        }

        @Override public Unit some(
          final Some<BufferedImage> s)
        {
          final BufferedImage image = s.get();
          VExpectedImage.this.expected_image_cache.put(key, image);
          VExpectedImage.this.setIcon(new ImageIcon(image));
          return Unit.unit();
        }
      });
    }
  }
}
