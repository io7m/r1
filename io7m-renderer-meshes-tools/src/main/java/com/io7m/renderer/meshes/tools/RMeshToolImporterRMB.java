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

package com.io7m.renderer.meshes.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.renderer.meshes.RMeshTangents;
import com.io7m.renderer.rmb.RBInfo;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;

/**
 * RMB mesh importer.
 */

public final class RMeshToolImporterRMB implements RMeshToolImporterType
{
  /**
   * Construct an importer.
   */

  public RMeshToolImporterRMB()
  {
    // Nothing.
  }

  @Override public String importerGetHumanName()
  {
    return "RMB";
  }

  @Override public String importerGetShortName()
  {
    return "rmb";
  }

  @Override public String importerGetSuffix()
  {
    return "rmb";
  }

  @Override public RMeshTangents importFile(
    final File input,
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log)
    throws RException
  {
    throw new UnimplementedCodeException();
  }

  @Override public void showFile(
    final File file,
    final PrintWriter output,
    final LogUsableType log)
    throws RException
  {
    try {
      final InputStream stream = new FileInputStream(file);
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
