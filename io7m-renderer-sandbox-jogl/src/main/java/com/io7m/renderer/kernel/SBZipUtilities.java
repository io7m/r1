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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

import com.io7m.jlog.Log;

final class SBZipUtilities
{
  interface BaseDirectory
  {
    @Nonnull File getFile();
  }

  static class TemporaryDirectory implements BaseDirectory
  {
    private final @Nonnull File file;

    /**
     * Request a new temporary directory, create it, tell the JVM to remove it
     * on exit.
     */

    TemporaryDirectory(
      final @Nonnull Log log)
    {
      final SecureRandom r = new SecureRandom();
      final byte[] bytes = new byte[16];
      r.nextBytes(bytes);

      final String tmpdir_name = System.getProperty("java.io.tmpdir");
      if (tmpdir_name == null) {
        throw new AssertionError("System property java.io.tmpdir is unset!");
      }

      final File tmpdir = new File(tmpdir_name);
      final StringBuilder name = new StringBuilder();
      name.append("sandbox-");
      for (final byte b : bytes) {
        name.append(String.format("%x", Byte.valueOf(b)));
      }

      this.file = new File(tmpdir, name.toString());
      log.debug("Creating temporary directory: " + this.file);

      if (this.file.toString().length() < 2) {
        throw new AssertionError(
          "Paranoia: temporary directory name is too short");
      }
      if (this.file.exists()) {
        throw new AssertionError("Temporary directory "
          + this.file
          + " already exists");
      }
      if (this.file.mkdirs() == false) {
        throw new AssertionError("Temporary directory "
          + this.file
          + " could not be created");
      }

      SBZipUtilities.deleteOnExit(log, this.file);
    }

    @Override public @Nonnull File getFile()
    {
      return this.file;
    }
  }

  /**
   * Copy the contents of <code>stream</code> to the file <code>out</code>.
   */

  private static void copyStreamOut(
    final @Nonnull InputStream input,
    final @Nonnull File out)
    throws FileNotFoundException,
      IOException
  {
    FileOutputStream stream = null;

    try {
      stream = new FileOutputStream(out);
      SBZipUtilities.copyStreams(input, stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Copy the contents of stream <code>input<code> to <code>output</code>.
   */

  private static void copyStreams(
    final @Nonnull InputStream input,
    final @Nonnull OutputStream output)
    throws IOException
  {
    final byte buffer[] = new byte[8192];

    for (;;) {
      final int r = input.read(buffer);
      if (r == -1) {
        output.flush();
        return;
      }
      output.write(buffer, 0, r);
    }
  }

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
        SBZipUtilities.copyStreamOut(zip_stream, output_file);
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

      SBZipUtilities.deleteOnExit(log, output_file);
      zip_stream.closeEntry();
    }
  }

  static void deleteOnExit(
    final @Nonnull Log log,
    final @Nonnull File file)
  {
    log.debug("Marking for deletion: " + file);
    file.deleteOnExit();
  }

  /**
   * Unpack all zip files given in the resource lists above to a temporary
   * directory prior to test execution.
   */

  static @Nonnull TemporaryDirectory unzip(
    final @Nonnull Log log,
    final @Nonnull File file)
    throws FileNotFoundException,
      IOException
  {
    final Log zlog = new Log(log, "unzip");
    final TemporaryDirectory d = new TemporaryDirectory(zlog);

    final ZipInputStream stream =
      new ZipInputStream(new FileInputStream(file));
    try {
      SBZipUtilities.copyZipStreamUnpack(zlog, stream, d.getFile());
    } finally {
      stream.close();
    }

    return d;
  }
}
