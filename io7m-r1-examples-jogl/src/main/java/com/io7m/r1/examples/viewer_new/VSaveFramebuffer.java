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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.swing.SwingWorker;

import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.ExampleRendererName;
import com.io7m.r1.examples.ExampleTypeEnum;
import com.jogamp.common.nio.Buffers;

/**
 * Functions to save the contents of the framebuffer to image files.
 */

@SuppressWarnings("boxing") public final class VSaveFramebuffer
{
  /**
   * Save the current scene image.
   *
   * @param in_config
   *          The configuration
   * @param gl
   *          The current OpenGL interface
   * @param type
   *          The type of example
   * @param width
   *          The width
   * @param height
   *          The height
   * @param example_name
   *          The example name
   * @param renderer_name
   *          The renderer name
   * @param view_index
   *          The view index
   * @param in_log
   *          A log interface
   */

  public static void saveSceneImage(
    final VExampleConfig in_config,
    final GL gl,
    final int width,
    final int height,
    final ExampleTypeEnum type,
    final String example_name,
    final ExampleRendererName renderer_name,
    final int view_index,
    final LogUsableType in_log)
  {
    {
      final String message =
        String.format("downloading %d x %d framebuffer image", width, height);
      assert message != null;
      in_log.debug(message);
    }

    final ByteBuffer pixels = Buffers.newDirectByteBuffer(width * 4 * height);

    gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
    gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
    gl.glReadPixels(
      0,
      0,
      width,
      height,
      GL.GL_RGBA,
      GL.GL_UNSIGNED_BYTE,
      pixels);

    final StringBuilder path_b = new StringBuilder();
    path_b.append(in_config.getReplacementResultsDirectory());
    path_b.append(File.separator);
    path_b.append(type.getDirectoryName());
    path_b.append(File.separator);
    path_b.append(example_name);
    path_b.append(File.separator);
    path_b.append(renderer_name.toString());
    path_b.append(File.separator);
    path_b.append(view_index);
    path_b.append(".png");
    final File out = new File(path_b.toString());

    final SwingWorker<Unit, Unit> worker = new SwingWorker<Unit, Unit>() {
      @Override protected Unit doInBackground()
        throws Exception
      {
        final String message = String.format("writing image %s", out);
        assert message != null;
        in_log.debug(message);

        final BufferedImage image =
          new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D gr = image.createGraphics();

        /**
         * Flip image.
         */

        final int span = width * 4;
        for (int y = 0; y < height; ++y) {
          for (int x = 0; x < width; ++x) {
            final int index = (x * 4) + (y * span);
            final int pixel = pixels.getInt(index);

            final float r = ((pixel >> 0) & 0xFF) / 256.0f;
            final float g = ((pixel >> 8) & 0xFF) / 256.0f;
            final float b = ((pixel >> 16) & 0xFF) / 256.0f;
            final float a = ((pixel >> 24) & 0xFF) / 256.0f;

            gr.setColor(new Color(r, g, b, a));
            gr.drawLine(x, height - y, x, height - y);
          }
        }

        out.getParentFile().mkdirs();
        final FileOutputStream stream = new FileOutputStream(out);
        ImageIO.write(image, "PNG", stream);
        stream.close();
        return Unit.unit();
      }

      @Override protected void done()
      {
        try {
          this.get();
          final String m =
            String.format("image %s written successfully", out);
          assert m != null;
          in_log.debug(m);
        } catch (final InterruptedException e) {
          VErrorBox.showErrorLater(in_log, e);
        } catch (final ExecutionException e) {
          VErrorBox.showErrorLater(in_log, e);
        }
      }
    };

    worker.execute();
  }

  private VSaveFramebuffer()
  {
    throw new UnreachableCodeException();
  }
}
