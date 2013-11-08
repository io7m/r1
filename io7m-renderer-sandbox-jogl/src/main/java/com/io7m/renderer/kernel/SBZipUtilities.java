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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;

final class SBZipUtilities
{
  /**
   * Unpack the zip file identified by <code>zip_stream</code> to the
   * directory <code>outdir</code>.
   */

  private static void copyZipStreamUnpack(
    final @Nonnull Log log,
    final @Nonnull ZipInputStream zip_stream,
    final @Nonnull File outdir)
    throws IOException
  {
    for (;;) {
      final ZipEntry entry = zip_stream.getNextEntry();
      if (entry == null) {
        return;
      }

      final long time = entry.getTime();

      final File output_file = new File(outdir, entry.getName());
      if (entry.isDirectory()) {
        log.debug("test-data: unzip: Creating directory " + output_file);
        if (output_file.mkdirs() == false) {
          throw new IOException("unzip: could not create directory: "
            + output_file);
        }
      } else {
        log.debug("unzip: Creating file " + output_file);

        final File parent = output_file.getParentFile();
        log.debug("unzip: Creating parent directory " + parent);
        if (parent.exists() == false) {
          if (parent.mkdirs() == false) {
            throw new IOException("unzip: could not create directory: "
              + parent);
          }
        }

        log.debug(" unzip: Creating file " + output_file);
        SBIOUtilities.copyStreamOut(zip_stream, output_file);
      }

      /**
       * Force modification time to match that of the zip file.
       */

      log.debug("unzip: Setting time to " + time + " on file " + output_file);
      if (output_file.setLastModified(time) == false) {
        throw new IOException("unzip: could not set time on file: "
          + output_file);
      }

      /**
       * Reset the modification time of the parent directory.
       */

      final File parent = output_file.getParentFile();
      log.debug("unzip: Setting time to "
        + time
        + " on parent directory "
        + parent);
      if (parent.setLastModified(time) == false) {
        throw new IOException("unzip: could not set time on directory: "
          + parent);
      }

      SBIOUtilities.deleteOnExit(log, output_file);
      zip_stream.closeEntry();
    }
  }

  public static void main(
    final String args[])
    throws IOException
  {
    if (args.length != 2) {
      System.err.println("usage: output.zip file");
      System.exit(1);
    }

    final Properties props = new Properties();
    props.setProperty("com.io7m.renderer.logs.sandbox", "true");
    props.setProperty("com.io7m.renderer.sandbox.level", "LOG_DEBUG");
    final Log log = new Log(props, "com.io7m.renderer", "sandbox");

    final File file = new File(args[0]);
    final File root = new File(args[1]);

    final ZipOutputStream fo =
      new ZipOutputStream(new FileOutputStream(file));
    fo.setLevel(9);

    SBZipUtilities.zipToStream(log, fo, root);

    fo.finish();
    fo.flush();
    fo.close();
  }

  /**
   * Unzip <code>file</code> to <code>output</code>.
   */

  static void unzip(
    final @Nonnull Log log,
    final @Nonnull File file,
    final @Nonnull File output)
    throws FileNotFoundException,
      IOException
  {
    final Log zlog = new Log(log, "unzip");
    final ZipInputStream stream =
      new ZipInputStream(new FileInputStream(file));
    try {
      SBZipUtilities.copyZipStreamUnpack(zlog, stream, output);
    } finally {
      stream.close();
    }
  }

  static @Nonnull File unzipToTemporary(
    final @Nonnull Log log,
    final @Nonnull File file,
    final @Nonnull String prefix,
    final int attempts)
    throws IOException,
      ConstraintError
  {
    final File output =
      SBIOUtilities.makeTemporaryDirectory(log, prefix, attempts);
    SBZipUtilities.unzip(log, file, output);
    return output;
  }

  static void zipToStream(
    final @Nonnull Log log,
    final @Nonnull ZipOutputStream fo,
    final @Nonnull File root)
    throws IOException
  {
    final byte[] buffer = new byte[8192];
    SBZipUtilities.zipToStreamRecursive(log, fo, buffer, "", root);
  }

  private static void zipToStreamRecursive(
    final @Nonnull Log log,
    final @Nonnull ZipOutputStream fo,
    final @Nonnull byte[] buffer,
    final @Nonnull String path,
    final @Nonnull File actual)
    throws IOException
  {
    if (log.enabled(Level.LOG_DEBUG)) {
      final StringBuilder s = new StringBuilder();
      s.append("zip ");
      s.append(path);
      s.append(" ");
      s.append(actual.toString());
      log.debug(s.toString());
    }

    if (actual.isFile()) {
      final FileInputStream stream = new FileInputStream(actual);
      try {
        final ZipEntry entry = new ZipEntry(path);
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
    } else {
      final String[] files = actual.list();
      if (files == null) {
        return;
      }

      if (path.isEmpty() == false) {
        final ZipEntry entry = new ZipEntry(path + "/");
        fo.putNextEntry(entry);
        fo.flush();
        fo.closeEntry();
      }

      for (final String name : files) {
        final String path_new = (path + "/" + name).replaceFirst("^/", "");
        final File actual_new = new File(actual, name);
        SBZipUtilities.zipToStreamRecursive(
          log,
          fo,
          buffer,
          path_new,
          actual_new);
      }
    }
  }
}
