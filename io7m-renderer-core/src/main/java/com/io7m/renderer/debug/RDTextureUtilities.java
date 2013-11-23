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

package com.io7m.renderer.debug;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceGL2;
import com.io7m.jcanephora.JCGLInterfaceGL3;
import com.io7m.jcanephora.Texture2DReadableData;
import com.io7m.jcanephora.Texture2DStaticUsable;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;

public final class RDTextureUtilities
{
  /**
   * Dump the raw pixel data in <code>r</code> to the file <code>file</code>.
   */

  public static void textureDump(
    final @Nonnull Texture2DReadableData r,
    final @Nonnull File file)
    throws FileNotFoundException,
      IOException
  {
    final BufferedOutputStream s =
      new BufferedOutputStream(new FileOutputStream(file));
    final ByteBuffer data = r.getData();
    for (int index = 0; index < data.capacity(); ++index) {
      s.write(data.get(index));
    }
    s.flush();
    s.close();
  }

  /**
   * <p>
   * Dump the raw pixel data of the texture <code>t</code> to the file
   * <code>file</code>.
   * </p>
   * <p>
   * The function does nothing if the current OpenGL implementation does not
   * support texture image fetching.
   * </p>
   */

  public static void textureDump2DStatic(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Texture2DStaticUsable t,
    final @Nonnull File file,
    final @Nonnull Log log)
    throws JCGLException,
      ConstraintError,
      FileNotFoundException,
      IOException
  {
    if (log.enabled(Level.LOG_DEBUG)) {
      final StringBuilder message = new StringBuilder();
      message.append("Dump ");
      message.append(t.getWidth());
      message.append("x");
      message.append(t.getHeight());
      message.append(" ");
      message.append(t.getType());
      message.append(" ");
      message.append(file);
      log.debug(message.toString());
    }

    {
      final Option<JCGLInterfaceGL3> og = gi.getGL3();
      switch (og.type) {
        case OPTION_NONE:
        {
          break;
        }
        case OPTION_SOME:
        {
          final JCGLInterfaceGL3 g =
            ((Option.Some<JCGLInterfaceGL3>) og).value;
          final Texture2DReadableData r = g.texture2DStaticGetImage(t);
          RDTextureUtilities.textureDump(r, file);
          return;
        }
      }
    }

    {
      final Option<JCGLInterfaceGL2> og = gi.getGL2();
      switch (og.type) {
        case OPTION_NONE:
        {
          break;
        }
        case OPTION_SOME:
        {
          final JCGLInterfaceGL2 g =
            ((Option.Some<JCGLInterfaceGL2>) og).value;
          final Texture2DReadableData r = g.texture2DStaticGetImage(t);
          RDTextureUtilities.textureDump(r, file);
          return;
        }
      }
    }

    log.debug("Cannot dump textures on this implementation");
  }

  /**
   * Dump the raw pixel data in <code>r</code> to a timestamped temporary file
   * prefixed with <code>name</code>, in the current temporary directory
   * (according to the <code>java.io.tmpdir</code> system property).
   */

  public static void textureDumpTimestampedTemporary(
    final @Nonnull Texture2DReadableData r,
    final @Nonnull String name)
    throws FileNotFoundException,
      IOException
  {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    final String actual =
      String.format("%s-%s.raw", name, fmt.format(cal.getTime()));
    final File out_file =
      new File(System.getProperty("java.io.tmpdir"), actual);
    RDTextureUtilities.textureDump(r, out_file);
  }

  /**
   * <p>
   * Dump the raw pixel data of the texture <code>t</code> to a timestamped
   * temporary file prefixed with <code>name</code>, in the current temporary
   * directory (according to the <code>java.io.tmpdir</code> system property).
   * </p>
   * <p>
   * The function does nothing if the current OpenGL implementation does not
   * support texture image fetching.
   * </p>
   */

  public static void textureDumpTimestampedTemporary2DStatic(
    final @Nonnull JCGLImplementation gi,
    final @Nonnull Texture2DStaticUsable t,
    final @Nonnull String name,
    final @Nonnull Log log)
    throws FileNotFoundException,
      IOException,
      JCGLException,
      ConstraintError
  {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    final String actual =
      String.format("%s-%s.raw", name, fmt.format(cal.getTime()));
    final File out_file =
      new File(System.getProperty("java.io.tmpdir"), actual);
    RDTextureUtilities.textureDump2DStatic(gi, t, out_file, log);
  }
}
