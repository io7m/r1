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
import java.io.PrintWriter;

import nu.xom.Document;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.renderer.meshes.RMeshTangents;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParser;

/**
 * RMX mesh importer.
 */

public final class RMeshToolImporterRMX implements RMeshToolImporterType
{
  /**
   * Construct an importer.
   */

  public RMeshToolImporterRMX()
  {
    // Nothing.
  }

  @Override public String importerGetHumanName()
  {
    return "RMX";
  }

  @Override public String importerGetShortName()
  {
    return "rmx";
  }

  @Override public String importerGetSuffix()
  {
    return "rmx";
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
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
      final Document doc = RXMLMeshDocument.parseFromStreamValidating(stream);
      final RMeshToolRMXInfo events = new RMeshToolRMXInfo(log);
      RXMLMeshParser.parseFromDocument(doc, events);

      output.printf(
        "%s : %d triangles : %d vertices\n",
        events.getName(),
        events.getTriangleCount(),
        events.getVertexCount());
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
}
