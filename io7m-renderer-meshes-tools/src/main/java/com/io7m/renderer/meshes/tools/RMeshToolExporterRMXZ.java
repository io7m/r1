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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import com.io7m.jlog.LogUsableType;
import com.io7m.renderer.meshes.RMeshTangents;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;
import com.io7m.renderer.xml.rmx.RXMLExporter;

/**
 * Exporter for the RMXZ format.
 */

public final class RMeshToolExporterRMXZ implements RMeshToolExporterType
{
  /**
   * Construct an exporter.
   */

  public RMeshToolExporterRMXZ()
  {
    // Nothing
  }

  @Override public String exporterGetHumanName()
  {
    return "RMXZ";
  }

  @Override public String exporterGetShortName()
  {
    return "rmxz";
  }

  @Override public String exporterGetSuffix()
  {
    return "rmxz";
  }

  @Override public void exportFile(
    final File file,
    final RMeshTangents mesh,
    final LogUsableType log)
    throws RException
  {
    try {
      final OutputStream stream =
        new GZIPOutputStream(new FileOutputStream(file));
      try {
        final RXMLExporter e = new RXMLExporter(log);
        final Element root = e.toXML(mesh);
        final Document d = new Document(root);
        final Serializer s = new Serializer(stream, "UTF-8");
        s.write(d);
        s.flush();
      } finally {
        stream.close();
      }
    } catch (final IOException e) {
      throw RExceptionIO.fromIOException(e);
    }
  }
}
