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

import com.io7m.jcanephora.Texture2DReadableData;
import com.io7m.jlog.Log;

public final class SBTextureUtilities
{
  static void textureDump(
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

  static void textureDumpTimestampedTemporary(
    final @Nonnull Texture2DReadableData r,
    final @Nonnull String name,
    final @Nonnull Log log)
    throws FileNotFoundException,
      IOException
  {
    final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
    final String actual =
      String.format("%s-%s.raw", name, fmt.format(cal.getTime()));
    final File out_file =
      new File(System.getProperty("java.io.tmpdir"), actual);
    log.debug("Writing texture to " + out_file);
    SBTextureUtilities.textureDump(r, out_file);
  }
}
