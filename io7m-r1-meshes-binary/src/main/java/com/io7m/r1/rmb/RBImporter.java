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

package com.io7m.r1.rmb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorM3F;
import com.io7m.r1.meshes.RMeshParserEventsType;
import com.io7m.r1.types.RBException;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RVectorI2F;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * A mesh parser implementation that parses a binary file and delivers events
 * to a given {@link RMeshParserEventsType} interface.
 *
 * @param <E>
 *          The type of exceptions raised by the event interface.
 */

@EqualityReference public final class RBImporter<E extends Throwable>
{
  /**
   * Construct a new parser from the given stream.
   *
   * @param <E>
   *          The type of exceptions raised by the parser.
   * @param s
   *          The input stream.
   * @param events
   *          The parser events interface.
   * @param log
   *          A log interface.
   *
   * @return A parser.
   *
   *
   * @throws E
   *           If required.
   * @throws RException
   *           On errors.
   */

  public static <E extends Throwable> RBImporter<E> parseFromStream(
    final InputStream s,
    final RMeshParserEventsType<E> events,
    final LogUsableType log)
    throws E,
      RException
  {
    return new RBImporter<E>(new BufferedInputStream(s, 65536), events, log);
  }

  private static RVectorI3F<RSpaceObjectType> readNormal(
    final ByteBuffer temp,
    final InputStream in_s)
    throws RBException,
      IOException
  {
    final float x = RBParsing.readFloat32(temp, in_s);
    final float y = RBParsing.readFloat32(temp, in_s);
    final float z = RBParsing.readFloat32(temp, in_s);
    return new RVectorI3F<RSpaceObjectType>(x, y, z);
  }

  private static RVectorI3F<RSpaceObjectType> readPosition(
    final ByteBuffer temp,
    final InputStream in_s)
    throws RBException,
      IOException
  {
    final float x = RBParsing.readFloat32(temp, in_s);
    final float y = RBParsing.readFloat32(temp, in_s);
    final float z = RBParsing.readFloat32(temp, in_s);
    return new RVectorI3F<RSpaceObjectType>(x, y, z);
  }

  private static RVectorI4F<RSpaceObjectType> readTangent(
    final ByteBuffer temp,
    final InputStream in_s)
    throws RBException,
      IOException
  {
    final float x = RBParsing.readFloat32(temp, in_s);
    final float y = RBParsing.readFloat32(temp, in_s);
    final float z = RBParsing.readFloat32(temp, in_s);
    final float w = RBParsing.readFloat32(temp, in_s);
    return new RVectorI4F<RSpaceObjectType>(x, y, z, w);
  }

  private static RVectorI2F<RSpaceTextureType> readUV(
    final ByteBuffer temp,
    final InputStream in_s)
    throws RBException,
      IOException
  {
    final float x = RBParsing.readFloat32(temp, in_s);
    final float y = RBParsing.readFloat32(temp, in_s);
    return new RVectorI2F<RSpaceTextureType>(x, y);
  }

  private final RMeshParserEventsType<E> events;
  private final ByteBuffer               temp;

  private RBImporter(
    final InputStream in_s,
    final RMeshParserEventsType<E> in_events,
    final LogUsableType in_log)
    throws E,
      RException
  {
    NullCheck.notNull(in_s, "Stream");

    this.events = NullCheck.notNull(in_events, "Events");
    this.temp = NullCheck.notNull(ByteBuffer.allocate(8));

    try {
      this.events.eventMeshStarted();

      final RBInfo info = RBInfo.parseFromStream(in_log, in_s);

      this.events.eventMeshName(info.getName());
      this.readVertices(in_s, info, in_log.with("vertices"));
      this.readTriangles(in_s, info, in_log.with("triangles"));

    } catch (final RException e) {
      in_events.eventError(e);
      throw e;
    } catch (final IOException e) {
      in_events.eventError(e);
      throw RExceptionIO.fromIOException(e);
    } finally {
      in_events.eventMeshEnded();
    }
  }

  private void readTriangles(
    final InputStream in_s,
    final RBInfo info,
    final LogType log)
    throws E,
      RBException,
      IOException
  {
    final long count = info.getTriangleCount();

    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      log.debug(String.format("reading %d triangles", count));
    }

    this.events.eventMeshTrianglesStarted((int) count);

    for (int index = 0; index < count; ++index) {
      if (log.wouldLog(LogLevel.LOG_DEBUG)) {
        log.debug(String.format("reading triangle %d", index));
      }

      final long v0 = RBParsing.readUnsigned32(this.temp, in_s);
      final long v1 = RBParsing.readUnsigned32(this.temp, in_s);
      final long v2 = RBParsing.readUnsigned32(this.temp, in_s);
      this.events.eventMeshTriangle(index, v0, v1, v2);
    }

    this.events.eventMeshTrianglesEnded();
  }

  private void readVertices(
    final InputStream in_s,
    final RBInfo info,
    final LogType log)
    throws E,
      RBException,
      IOException
  {
    final long count = info.getVertexCount();

    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      log.debug(String.format("reading %d vertices", count));
    }

    this.events.eventMeshVerticesStarted((int) count);

    final VectorM3F bounds_lower =
      new VectorM3F(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    final VectorM3F bounds_upper =
      new VectorM3F(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    for (int index = 0; index < count; ++index) {
      if (log.wouldLog(LogLevel.LOG_DEBUG)) {
        log.debug(String.format("reading vertex %d", index));
      }

      this.events.eventMeshVertexStarted(index);

      final RVectorI3F<RSpaceObjectType> position =
        RBImporter.readPosition(this.temp, in_s);
      final RVectorI3F<RSpaceObjectType> normal =
        RBImporter.readNormal(this.temp, in_s);
      final RVectorI4F<RSpaceObjectType> tangent =
        RBImporter.readTangent(this.temp, in_s);
      final RVectorI2F<RSpaceTextureType> uv =
        RBImporter.readUV(this.temp, in_s);

      bounds_lower.set3F(
        Math.min(position.getXF(), bounds_lower.getXF()),
        Math.min(position.getYF(), bounds_lower.getYF()),
        Math.min(position.getZF(), bounds_lower.getZF()));

      bounds_upper.set3F(
        Math.max(position.getXF(), bounds_upper.getXF()),
        Math.max(position.getYF(), bounds_upper.getYF()),
        Math.max(position.getZF(), bounds_upper.getZF()));

      this.events.eventMeshVertexPosition(index, position);
      this.events.eventMeshVertexNormal(index, normal);
      this.events.eventMeshVertexTangent4f(index, tangent);
      this.events.eventMeshVertexUV(index, uv);
      this.events.eventMeshVertexEnded(index);
    }

    this.events.eventMeshVerticesEnded(
      new RVectorI3F<RSpaceObjectType>(bounds_lower.getXF(), bounds_lower
        .getYF(), bounds_lower.getZF()),
      new RVectorI3F<RSpaceObjectType>(bounds_upper.getXF(), bounds_upper
        .getYF(), bounds_upper.getZF()));
  }
}
