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

package com.io7m.r1.examples.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import nu.xom.Document;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionIO;
import com.io7m.r1.kernel.types.KMesh;
import com.io7m.r1.meshes.RMeshParserEventsVBO;
import com.io7m.r1.rmb.RBImporter;
import com.io7m.r1.xml.rmx.RXMLMeshDocument;
import com.io7m.r1.xml.rmx.RXMLMeshParser;

/**
 * A simple mesh cache.
 */

public final class EMeshCache
{
  private final Map<String, KMesh>     meshes;
  private final LogUsableType          glog;
  private final JCGLImplementationType gi;

  /**
   * Initialize the cache.
   *
   * @param in_gi
   *          A GL interface.
   * @param in_log
   *          A log interface.
   */

  public EMeshCache(
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.gi = NullCheck.notNull(in_gi, "GL");
    this.glog = NullCheck.notNull(in_log, "Log").with("cube-cache");
    this.meshes = new HashMap<String, KMesh>();
  }

  private static InputStream openMesh(
    final String name)
    throws IOException
  {
    final InputStream raw_stream =
      EMeshCache.class.getResourceAsStream(String.format(
        "/com/io7m/r1/examples/%s",
        name));
    assert raw_stream != null;

    final InputStream stream;
    if (name.endsWith("z")) {
      stream = new GZIPInputStream(raw_stream);
    } else {
      stream = raw_stream;
    }

    return stream;
  }

  /**
   * Load a mesh with the given unqualified name.
   *
   * @param name
   *          The name.
   * @return A mesh.
   * @throws RException
   *           If an error occurs.
   */

  public KMesh loadMesh(
    final String name)
    throws RException
  {
    if (this.meshes.containsKey(name)) {
      return NullCheck.notNull(this.meshes.get(name));
    }

    final StringBuilder message = new StringBuilder();
    message.setLength(0);
    message.append("Loading mesh from ");
    message.append(name);
    {
      final String s = message.toString();
      assert s != null;
      this.glog.debug(s);
    }

    final JCGLImplementationType g = this.gi;
    assert g != null;
    final JCGLInterfaceCommonType gl = g.getGLCommon();
    InputStream stream = null;

    try {
      stream = EMeshCache.openMesh(name);

      final RMeshParserEventsVBO<JCGLInterfaceCommonType> events =
        RMeshParserEventsVBO.newEvents(gl, UsageHint.USAGE_STATIC_DRAW);

      if (name.endsWith(".rmbz") || name.endsWith(".rmb")) {
        RBImporter.parseFromStream(stream, events, this.glog);
      } else {
        final Document document =
          RXMLMeshDocument.parseFromStreamValidating(stream);
        RXMLMeshParser.parseFromDocument(document, events);
      }

      final ArrayBufferType array = events.getArray();
      final IndexBufferType index = events.getIndices();
      final KMesh km = KMesh.newMesh(array, index);
      this.meshes.put(name, km);

      message.setLength(0);
      message.append("Loaded mesh ");
      message.append(name);
      {
        final String s = message.toString();
        assert s != null;
        this.glog.debug(s);
      }

      return km;
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
