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

package com.io7m.renderer.rmb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;
import com.io7m.renderer.types.RBException;
import com.io7m.renderer.types.RBExceptionInvalidMagicNumber;
import com.io7m.renderer.types.RBExceptionUnsupportedVersion;

/**
 * Information about an RMB mesh.
 */

@SuppressWarnings("boxing") @EqualityStructural public final class RBInfo
{
  /**
   * Parse mesh info from the given stream.
   *
   * @param stream
   *          The stream.
   * @param log
   *          A log interface.
   * @return Mesh info.
   * @throws RBException
   *           On file format errors.
   * @throws IOException
   *           On I/O errors.
   */

  public static RBInfo parseFromStream(
    final LogUsableType log,
    final InputStream stream)
    throws RBException,
      IOException
  {
    NullCheck.notNull(stream, "Stream");

    final ByteBuffer temp = ByteBuffer.allocate(8);
    assert temp != null;

    RBInfo.readMagicNumber(temp, stream, log);
    final long version = RBInfo.readVersion(stream, temp, log);
    final String name = RBParsing.readUTF8String(stream, temp, log);
    final long vertex_count = RBParsing.readUnsigned32(temp, stream);
    final long triangle_count = RBParsing.readUnsigned32(temp, stream);
    return new RBInfo(version, triangle_count, vertex_count, name);
  }

  private static void readMagicNumber(
    final ByteBuffer in_temp,
    final InputStream in_s,
    final LogUsableType log)
    throws IOException,
      RBExceptionInvalidMagicNumber
  {
    log.debug("reading 4-byte magic number");

    final byte[] a = in_temp.array();
    assert a != null;
    final int r = in_s.read(a, 0, 4);

    if (r != 4) {
      final StringBuilder m = new StringBuilder();
      m.append("Invalid magic number.\n");
      m.append("  Expected: ");
      // CHECKSTYLE:OFF
      m.append(RBInfo.showBytes(new byte[] { 'R', 'M', 'B', 0x0A }));
      // CHECKSTYLE:ON
      m.append("  Got:      ");
      m.append(RBInfo.showBytes(a));
      m.append("\n");
      final String s = m.toString();
      assert s != null;
      throw new RBExceptionInvalidMagicNumber(s);
    }
  }

  private static long readVersion(
    final InputStream stream,
    final ByteBuffer temp,
    final LogUsableType log)
    throws IOException,
      RBException,
      RBExceptionUnsupportedVersion
  {
    log.debug("reading 4-byte version");

    final long version = RBParsing.readUnsigned32(temp, stream);
    if (version != RBConstants.RMB_VERSION) {
      final StringBuilder m = new StringBuilder();
      m.append("Unsupported RMB mesh version.\n");
      m.append("  Got: ");
      m.append(version);
      m.append("  Supported versions: ");
      m.append(RBConstants.RMB_VERSION);
      m.append("\n");
      final String s = m.toString();
      assert s != null;
      throw new RBExceptionUnsupportedVersion(s);
    }
    return version;
  }

  private static String showBytes(
    final byte[] a)
  {
    final StringBuilder s = new StringBuilder();
    s.append("[");
    for (int index = 0; index < a.length; ++index) {
      final byte c = a[index];
      s.append(String.format("0x%02X", c));
      if ((index + 1) != a.length) {
        s.append(" ");
      }
    }
    s.append("]");
    final String r = s.toString();
    assert r != null;
    return r;
  }

  private final String name;
  private final long   triangle_count;
  private final long   version;
  private final long   vertex_count;

  private RBInfo(
    final long in_version,
    final long in_triangle_count,
    final long in_vertex_count,
    final String in_name)
  {
    this.version =
      RangeCheck.checkGreater(in_version, "Version", 0L, "Minimum version");

    this.triangle_count =
      RangeCheck.checkGreater(
        in_triangle_count,
        "Triangle count",
        0,
        "Minimum count");
    this.vertex_count =
      RangeCheck.checkGreater(
        in_vertex_count,
        "Vertex count",
        0,
        "Minimum count");
    this.name = NullCheck.notNull(in_name, "Name");
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final RBInfo other = (RBInfo) obj;
    return this.name.equals(other.name)
      && (this.version == other.version)
      && (this.triangle_count == other.triangle_count)
      && (this.vertex_count == other.vertex_count);
  }

  /**
   * @return The name of the mesh.
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * @return The number of triangles in the mesh.
   */

  public long getTriangleCount()
  {
    return this.triangle_count;
  }

  /**
   * @return The version of the RMB format from which mesh data was parsed.
   */

  public long getVersion()
  {
    return this.version;
  }

  /**
   * @return The number of vertices in the mesh.
   */

  public long getVertexCount()
  {
    return this.vertex_count;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.name.hashCode();
    result = (prime * result) + (int) (this.version ^ (this.version >>> 32));
    result =
      (prime * result)
        + (int) (this.triangle_count ^ (this.triangle_count >>> 32));
    result =
      (prime * result)
        + (int) (this.vertex_count ^ (this.vertex_count >>> 32));
    return result;
  }
}
