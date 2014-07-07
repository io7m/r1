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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RBException;
import com.io7m.renderer.types.RBExceptionShortRead;
import com.io7m.renderer.types.RBExceptionUnexpectedEOF;

/**
 * Convenient parsing functions.
 */

@EqualityReference final class RBParsing
{
  static float readFloat32(
    final ByteBuffer temp,
    final InputStream s)
    throws IOException,
      RBException
  {
    temp.rewind();
    final byte[] a = temp.array();
    final int r = s.read(a, 0, 4);

    if (r == -1) {
      throw new RBExceptionUnexpectedEOF("Unexpected EOF");
    }

    if (r < 4) {
      final StringBuilder m = new StringBuilder();
      m.append("Expected 4 bytes, got ");
      m.append(r);
      final String ms = m.toString();
      assert ms != null;
      throw new RBExceptionShortRead(ms);
    }

    return temp.getFloat(0);
  }

  static int readUnsigned16(
    final ByteBuffer temp,
    final InputStream s)
    throws IOException,
      RBException
  {
    temp.rewind();
    final byte[] a = temp.array();
    final int r = s.read(a, 0, 2);

    if (r == -1) {
      throw new RBExceptionUnexpectedEOF("Unexpected EOF");
    }

    if (r < 2) {
      final StringBuilder m = new StringBuilder();
      m.append("Expected 2 bytes, got ");
      m.append(r);
      final String ms = m.toString();
      assert ms != null;
      throw new RBExceptionShortRead(ms);
    }

    return Unsigned16.unpackFromBuffer(temp, 0);
  }

  static long readUnsigned32(
    final ByteBuffer temp,
    final InputStream s)
    throws IOException,
      RBException
  {
    temp.rewind();
    final byte[] a = temp.array();
    final int r = s.read(a, 0, 4);

    if (r == -1) {
      throw new RBExceptionUnexpectedEOF("Unexpected EOF");
    }

    if (r < 4) {
      final StringBuilder m = new StringBuilder();
      m.append("Expected 4 bytes, got ");
      m.append(r);
      final String ms = m.toString();
      assert ms != null;
      throw new RBExceptionShortRead(ms);
    }

    return Unsigned32.unpackFromBuffer(temp, 0);
  }

  static String readUTF8String(
    final InputStream stream,
    final ByteBuffer temp,
    final LogUsableType log)
    throws RBException,
      IOException
  {
    log.debug("reading 2-byte name length");

    final int size = RBParsing.readUnsigned16(temp, stream);
    final int name_offset = 2;
    final int aligned_size = ((size + 4 + name_offset) / 4) * 4;
    final int pad = aligned_size - (size + name_offset);

    if (log.wouldLog(LogLevel.LOG_DEBUG)) {
      final String rs =
        String.format(
          "name is %d bytes, aligned size %d (%d padding bytes)",
          size,
          aligned_size,
          pad);
      assert rs != null;
      log.debug(rs);
    }

    /**
     * Read actual name bytes.
     */

    final byte[] name_buffer = new byte[size];
    final int r = stream.read(name_buffer);
    if (r < size) {
      final StringBuilder m = new StringBuilder();
      m.append("Expected ");
      m.append(size);
      m.append(" bytes, got ");
      m.append(r);
      final String s = m.toString();
      assert s != null;
      throw new RBExceptionShortRead(s);
    }

    final String result_name = new String(name_buffer, "UTF-8");

    /**
     * Read trailing padding bytes.
     */

    for (int index = 0; index < pad; ++index) {
      final int rx = stream.read(name_buffer, 0, 1);
      if (rx == -1) {
        throw new RBExceptionUnexpectedEOF("Unexpected EOF");
      }
      if (rx != 1) {
        final StringBuilder m = new StringBuilder();
        m.append("Expected 1 byte, got ");
        m.append(r);
        final String s = m.toString();
        assert s != null;
        throw new RBExceptionShortRead(s);
      }
    }

    return result_name;
  }

  private RBParsing()
  {
    throw new UnreachableCodeException();
  }

}
