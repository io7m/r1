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

package com.io7m.r1.meshes.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.rmb.RBInfo;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;

/**
 * RMBZ mesh importer.
 */

public final class RMeshToolImporterRMBZ implements RMeshToolImporterType
{
  /**
   * Construct an importer.
   */

  public RMeshToolImporterRMBZ()
  {
    // Nothing.
  }

  @Override public String importerGetHumanName()
  {
    return "RMBZ";
  }

  @Override public String importerGetShortName()
  {
    return "rmbz";
  }

  @Override public String importerGetSuffix()
  {
    return "rmbz";
  }

  @Override public RMeshTangents importFile(
    final File file,
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log)
    throws RException
  {
    InputStream stream = null;

    try {
      stream = new GZIPInputStream(new FileInputStream(file));

      return RMeshToolImporterRMB.parseMeshTangents(
        mesh_name,
        mesh_change_name,
        log,
        stream);
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (final IOException e) {
        throw RExceptionIO.fromIOException(e);
      }
    }
  }

  @Override public void showFile(
    final File file,
    final PrintWriter output,
    final LogUsableType log)
    throws RException
  {
    try {
      final InputStream stream =
        new GZIPInputStream(new FileInputStream(file));
      final RBInfo info = RBInfo.parseFromStream(log, stream);
      output.printf(
        "%s : %d triangles : %d vertices : version %d\n",
        info.getName(),
        info.getTriangleCount(),
        info.getVertexCount(),
        info.getVersion());
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }
}
