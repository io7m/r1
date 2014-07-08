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

package com.io7m.renderer.tests.rmb;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jnull.NonNull;
import com.io7m.jnull.NullCheck;
import com.io7m.renderer.meshes.RMeshParserEventsType;
import com.io7m.renderer.rmb.RBImporter;
import com.io7m.renderer.rmb.RBInfo;
import com.io7m.renderer.types.RBExceptionInvalidMagicNumber;
import com.io7m.renderer.types.RBExceptionUnsupportedVersion;
import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;

@SuppressWarnings("static-method") public final class RBImporterTest
{
  class Show implements RMeshParserEventsType<Exception>
  {
    @Override public void eventError(
      final Exception e)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshEnded()
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshName(
      final String name)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshStarted()
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshTriangle(
      final long index,
      final long v0,
      final long v1,
      final long v2)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesEnded()
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshTrianglesStarted(
      final long count)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexEnded(
      final long index)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexNormal(
      final long index,
      final RVectorI3F<RSpaceObjectType> normal)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexPosition(
      final long index,
      final RVectorI3F<RSpaceObjectType> position)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexStarted(
      final long index)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexTangent4f(
      final long index,
      final RVectorI4F<RSpaceObjectType> tangent)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVertexUV(
      final long index,
      final RVectorI2F<RSpaceTextureType> uv)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVerticesEnded(
      final RVectorI3F<RSpaceObjectType> lower,
      final RVectorI3F<RSpaceObjectType> upper)
      throws Exception
    {
      // Nothing
    }

    @Override public void eventMeshVerticesStarted(
      final long count)
      throws Exception
    {
      // Nothing
    }
  }

  private static @NonNull InputStream get(
    final String file)
  {
    return NullCheck.notNull(RBImporterTest.class
      .getResourceAsStream("/com/io7m/renderer/tests/rmb/" + file));
  }

  @Test(expected = RBExceptionInvalidMagicNumber.class) public
    void
    testBadMagic_0()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-magic-0.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test(expected = RBExceptionInvalidMagicNumber.class) public
    void
    testBadMagic_2()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-magic-2.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test(expected = RBExceptionInvalidMagicNumber.class) public
    void
    testBadMagic_3()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-magic-3.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test(expected = RBExceptionInvalidMagicNumber.class) public
    void
    testBadMagic_4()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-magic-4.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test(expected = RBExceptionInvalidMagicNumber.class) public
    void
    testBadMagic_1()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-magic-1.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test(expected = RBExceptionUnsupportedVersion.class) public
    void
    testBadVersion_0()
      throws Exception
  {
    RBImporter.parseFromStream(
      RBImporterTest.get("bad-version-0.rmb"),
      new Show(),
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }

  @Test public void testMinimalEquals_0()
    throws Exception
  {
    final RBInfo rb0 =
      RBInfo.parseFromStream(
        Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"),
        RBImporterTest.get("minimal-0.rmb"));
    final RBInfo rb1 =
      RBInfo.parseFromStream(
        Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"),
        RBImporterTest.get("minimal-0.rmb"));

    Assert.assertEquals(rb0, rb1);
    Assert.assertEquals(1, rb0.getVersion());
  }

  @Test public void testMinimal_0()
    throws Exception
  {
    final RMeshParserEventsType<Exception> events =
      new RMeshParserEventsType<Exception>() {
        @Override public void eventError(
          final Exception e)
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshEnded()
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshName(
          final String name)
          throws Exception
        {
          Assert.assertEquals(name, "A");
        }

        @Override public void eventMeshStarted()
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshTriangle(
          final long index,
          final long v0,
          final long v1,
          final long v2)
          throws Exception
        {
          System.out.printf("%x\n", v0);
          System.out.printf("%x\n", v1);
          System.out.printf("%x\n", v2);

          Assert.assertEquals(0, index);
          Assert.assertEquals(0x74307430, v0);
          Assert.assertEquals(0x74317431, v1);
          Assert.assertEquals(0x74327432, v2);
        }

        @Override public void eventMeshTrianglesEnded()
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshTrianglesStarted(
          final long count)
          throws Exception
        {
          Assert.assertEquals(1, count);
        }

        @Override public void eventMeshVertexEnded(
          final long index)
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshVertexNormal(
          final long index,
          final RVectorI3F<RSpaceObjectType> normal)
          throws Exception
        {
          Assert.assertEquals(0, index);
          Assert.assertEquals(
            0x4E304E30,
            Float.floatToRawIntBits(normal.getXF()));
          Assert.assertEquals(
            0x4E314E31,
            Float.floatToRawIntBits(normal.getYF()));
          Assert.assertEquals(
            0x4E324E32,
            Float.floatToRawIntBits(normal.getZF()));
        }

        @Override public void eventMeshVertexPosition(
          final long index,
          final RVectorI3F<RSpaceObjectType> position)
          throws Exception
        {
          Assert.assertEquals(0, index);
          Assert.assertEquals(
            0x50305030,
            Float.floatToRawIntBits(position.getXF()));
          Assert.assertEquals(
            0x50315031,
            Float.floatToRawIntBits(position.getYF()));
          Assert.assertEquals(
            0x50325032,
            Float.floatToRawIntBits(position.getZF()));
        }

        @Override public void eventMeshVertexStarted(
          final long index)
          throws Exception
        {
          Assert.assertEquals(0, index);
        }

        @Override public void eventMeshVertexTangent4f(
          final long index,
          final RVectorI4F<RSpaceObjectType> tangent)
          throws Exception
        {
          Assert.assertEquals(0, index);
          Assert.assertEquals(
            0x54305430,
            Float.floatToRawIntBits(tangent.getXF()));
          Assert.assertEquals(
            0x54315431,
            Float.floatToRawIntBits(tangent.getYF()));
          Assert.assertEquals(
            0x54325432,
            Float.floatToRawIntBits(tangent.getZF()));
          Assert.assertEquals(
            0x54335433,
            Float.floatToRawIntBits(tangent.getWF()));
        }

        @Override public void eventMeshVertexUV(
          final long index,
          final RVectorI2F<RSpaceTextureType> uv)
          throws Exception
        {
          Assert.assertEquals(0, index);
          Assert
            .assertEquals(0x55305530, Float.floatToRawIntBits(uv.getXF()));
          Assert
            .assertEquals(0x55315531, Float.floatToRawIntBits(uv.getYF()));
        }

        @Override public void eventMeshVerticesEnded(
          final RVectorI3F<RSpaceObjectType> bounds_lower,
          final RVectorI3F<RSpaceObjectType> bounds_upper)
          throws Exception
        {
          // Nothing
        }

        @Override public void eventMeshVerticesStarted(
          final long count)
          throws Exception
        {
          Assert.assertEquals(1, count);
        }
      };

    RBImporter.parseFromStream(
      RBImporterTest.get("minimal-0.rmb"),
      events,
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests"));
  }
}
