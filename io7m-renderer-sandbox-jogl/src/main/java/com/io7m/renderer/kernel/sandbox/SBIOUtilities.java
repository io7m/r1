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

package com.io7m.renderer.kernel.sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;

public final class SBIOUtilities
{
  /**
   * Copy the contents of <code>in</code> to <code>out</code>.
   */

  static void copyFile(
    final @Nonnull File in,
    final @Nonnull File out)
    throws FileNotFoundException,
      IOException
  {
    final FileInputStream stream = new FileInputStream(in);
    try {
      SBIOUtilities.copyStreamOut(stream, out);
    } finally {
      stream.close();
    }
  }

  /**
   * Copy the contents of <code>stream</code> to the file <code>out</code>.
   */

  static void copyStreamOut(
    final @Nonnull InputStream input,
    final @Nonnull File out)
    throws FileNotFoundException,
      IOException
  {
    FileOutputStream stream = null;

    try {
      stream = new FileOutputStream(out);
      SBIOUtilities.copyStreams(input, stream);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Copy the contents of stream <code>input<code> to <code>output</code>.
   */

  static void copyStreams(
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
   * Mark <code>file</code> as being deleted upon exiting the JVM.
   */

  static void deleteOnExit(
    final @Nonnull Log log,
    final @Nonnull File file)
  {
    log.debug("Marking for deletion: " + file);
    file.deleteOnExit();
  }

  /**
   * Get the path to a possibly user-specific temporary directory, falling
   * back to <code>java.io.tmpdir</code> if the current platform does not
   * provide something better.
   */

  static @Nonnull File getTemporaryDirectoryPath()
    throws ConstraintError
  {
    {
      final String tmpdir_name = System.getenv("XDG_CACHE_HOME");
      if (tmpdir_name != null) {
        return new File(tmpdir_name);
      }
    }

    {
      final String tmpdir_name = System.getProperty("java.io.tmpdir");
      Constraints.constrainNotNull(tmpdir_name, "Property java.io.tmpdir");
      return new File(tmpdir_name);
    }
  }

  /**
   * Make up to <code>attempts</code> attempts to create a new temporary
   * directory with an unpredictable name prefixed with <code>prefix</code>.
   */

  static @Nonnull File makeTemporaryDirectory(
    final @Nonnull Log log,
    final @Nonnull String prefix,
    final int attempts)
    throws IOException,
      ConstraintError
  {
    Constraints.constrainNotNull(log, "Log");
    Constraints.constrainNotNull(prefix, "Prefix");
    Constraints.constrainRange(attempts, 1, Integer.MAX_VALUE, "Attempts");

    final SecureRandom r = new SecureRandom();
    final byte[] bytes = new byte[16];

    for (int index = 0; index < attempts; ++index) {
      r.nextBytes(bytes);

      final File tmpdir = SBIOUtilities.getTemporaryDirectoryPath();
      final StringBuilder name = new StringBuilder();
      name.append(prefix);
      for (final byte b : bytes) {
        name.append(String.format("%x", Byte.valueOf(b)));
      }

      final File file = new File(tmpdir, name.toString());
      log.debug("Creating temporary directory: " + file);

      if (file.toString().length() < 2) {
        throw new IOException(
          "Paranoia: temporary directory name is too short");
      }
      if (file.exists()) {
        continue;
      }
      if (file.mkdirs() == false) {
        throw new IOException("Temporary directory "
          + file
          + " could not be created");
      }

      SBIOUtilities.deleteOnExit(log, file);
      return file;
    }

    throw new IOException(
      "Number of attempts exceeded: Could not create temporary directory");
  }

}
