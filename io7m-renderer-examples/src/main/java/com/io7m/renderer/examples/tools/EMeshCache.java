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

package com.io7m.renderer.examples.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import nu.xom.Document;

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.types.KMesh;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionIO;
import com.io7m.renderer.types.RExceptionJCGL;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.xml.rmx.RXMLMeshDocument;
import com.io7m.renderer.xml.rmx.RXMLMeshParserVBO;

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
        "/com/io7m/renderer/examples/%s",
        name));
    assert raw_stream != null;

    final InputStream stream;
    if (name.endsWith(".rmxz")) {
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
      return this.meshes.get(name);
    }

    final StringBuilder message = new StringBuilder();
    message.setLength(0);
    message.append("Loading mesh from ");
    message.append(name);
    this.glog.debug(message.toString());

    final JCGLImplementationType g = this.gi;
    assert g != null;
    final JCGLInterfaceCommonType gl = g.getGLCommon();

    InputStream stream = null;

    try {
      stream = EMeshCache.openMesh(name);

      final Document document =
        RXMLMeshDocument.parseFromStreamValidating(stream);

      final RXMLMeshParserVBO<JCGLInterfaceCommonType> p =
        RXMLMeshParserVBO.parseFromDocument(
          document,
          gl,
          UsageHint.USAGE_STATIC_DRAW);

      final ArrayBufferType array = p.getArrayBuffer();
      final IndexBufferType index = p.getIndexBuffer();
      final RVectorI3F<RSpaceObjectType> lower = p.getBoundsLower();
      final RVectorI3F<RSpaceObjectType> upper = p.getBoundsUpper();
      final KMesh km = KMesh.newMesh(array, index, lower, upper);
      this.meshes.put(name, km);

      message.setLength(0);
      message.append("Loaded mesh ");
      message.append(name);
      this.glog.debug(message.toString());

      message.setLength(0);
      message.append("Mesh lower bound: ");
      message.append(km.meshGetBoundsLower());
      this.glog.debug(message.toString());

      message.setLength(0);
      message.append("Mesh upper bound: ");
      message.append(km.meshGetBoundsUpper());
      this.glog.debug(message.toString());

      return km;
    } catch (final JCGLException e) {
      throw RExceptionJCGL.fromJCGLException(e);
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
