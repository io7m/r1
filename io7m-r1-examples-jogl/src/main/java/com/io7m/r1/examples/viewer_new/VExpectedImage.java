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

package com.io7m.r1.examples.viewer_new;

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
import com.io7m.r1.examples.ExampleType;

/**
 * An image display, reading images from a given cache.
 */

@SuppressWarnings({ "boxing", "synthetic-access" }) public final class VExpectedImage extends
  JLabel
{
  private static final long                        serialVersionUID;

  static {
    serialVersionUID = 1636894751686016138L;
  }

  private final WeakHashMap<String, BufferedImage> expected_image_cache;
  private final AtomicInteger                      current_view_index;
  private final LogUsableType                      rlog;
  private final ExampleType                        example;
  private final ExampleImages<BufferedImage>       example_images;
  private @Nullable ExampleRendererName            previous;

  /**
   * Construct a cache.
   *
   * @param in_log
   *          A log interface
   * @param in_example_images
   *          The example images
   * @param in_example
   *          The example
   * @param in_current_view_index
   *          The current view index
   */

  public VExpectedImage(
    final LogUsableType in_log,
    final ExampleImages<BufferedImage> in_example_images,
    final ExampleType in_example,
    final AtomicInteger in_current_view_index)
  {
    this.rlog = in_log.with("results");
    this.example = NullCheck.notNull(in_example, "Example");
    this.current_view_index =
      NullCheck.notNull(in_current_view_index, "View index");
    this.example_images =
      NullCheck.notNull(in_example_images, "Example images");
    this.expected_image_cache = new WeakHashMap<String, BufferedImage>();
    this.previous = null;
  }

  /**
   * Update the displayed image with the same renderer as the last time
   * {@link #update(ExampleRendererName)} was called.
   *
   * @throws Exception
   *           On errors
   */

  public void updateWithSameRenderer()
    throws Exception
  {
    if (this.previous != null) {
      this.update(this.previous);
    }
  }

  /**
   * Update the displayed image for the given renderer
   *
   * @param name
   *          The renderer
   * @throws Exception
   *           On errors
   */

  public void update(
    final ExampleRendererName name)
    throws Exception
  {
    this.previous = NullCheck.notNull(name, "Renderer");

    final int view_index = this.current_view_index.get();
    final Class<? extends ExampleType> sc = this.example.getClass();

    {
      final String message =
        String.format(
          "Switching to view %d for renderer %s",
          view_index,
          name);
      assert message != null;
      this.rlog.debug(message);
    }

    final StringBuilder b = new StringBuilder();
    b.append(name);
    b.append(":");
    b.append(sc.getCanonicalName());
    b.append(":");
    b.append(view_index);
    final String key = b.toString();

    if (this.expected_image_cache.get(key) != null) {
      {
        final String message = String.format("Got cached image %s", key);
        assert message != null;
        this.rlog.debug(message);
      }
      final BufferedImage r = this.expected_image_cache.get(key);
      this.setIcon(new ImageIcon(r));
    } else {
      final OptionType<BufferedImage> r =
        this.example_images.getImage(name, this.example, view_index);
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
