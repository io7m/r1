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
import java.io.FileOutputStream;
import java.io.IOException;

import com.io7m.jlog.LogUsableType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionIO;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.rmb.RBExporter;

/**
 * Exporter for the RMB format.
 */

public final class RMeshToolExporterRMB implements RMeshToolExporterType
{
  /**
   * Construct an exporter.
   */

  public RMeshToolExporterRMB()
  {
    // Nothing
  }

  @Override public String exporterGetHumanName()
  {
    return "RMB";
  }

  @Override public String exporterGetShortName()
  {
    return "rmb";
  }

  @Override public String exporterGetSuffix()
  {
    return "rmb";
  }

  @Override public void exportFile(
    final File file,
    final RMeshTangents mesh,
    final LogUsableType log)
    throws RException
  {
    try {
      final FileOutputStream stream = new FileOutputStream(file);
      try {
        final RBExporter ex = RBExporter.newExporter();
        ex.toStream(mesh, stream);
        stream.flush();
      } finally {
        stream.close();
      }
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }
}
