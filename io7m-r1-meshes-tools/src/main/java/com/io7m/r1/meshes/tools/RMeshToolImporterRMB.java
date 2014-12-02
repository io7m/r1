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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogUsableType;
import com.io7m.jtensors.parameterized.PVectorI2F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.jtensors.parameterized.PVectorI4F;
import com.io7m.r1.meshes.RMeshParserEventsType;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.meshes.RMeshTangentsBuilderType;
import com.io7m.r1.rmb.RBImporter;
import com.io7m.r1.rmb.RBInfo;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionIO;
import com.io7m.r1.types.RExceptionMeshNameInvalid;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;

/**
 * RMB mesh importer.
 */

public final class RMeshToolImporterRMB implements RMeshToolImporterType
{
  static RMeshTangents parseMeshTangents(
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log,
    final InputStream stream)
    throws RExceptionMeshNameInvalid,
      RException
  {
    final RMeshTangentsBuilderType b =
      RMeshTangents.newBuilder(mesh_change_name
        .accept(new OptionVisitorType<String, String>() {
          @Override public String none(
            final None<String> n)
          {
            return mesh_name;
          }

          @Override public String some(
            final Some<String> s)
          {
            return s.get();
          }
        }));

    final RMeshParserEventsType<RException> events =
      new RMeshParserEventsType<RException>() {
        private PVectorI3F<RSpaceObjectType>  normal   = PVectorI3F.zero();
        private PVectorI3F<RSpaceObjectType>  position = PVectorI3F.zero();
        private PVectorI4F<RSpaceObjectType>  tangent  = PVectorI4F.zero();
        private PVectorI2F<RSpaceTextureType> uv       = PVectorI2F.zero();

        @Override public void eventError(
          final Exception e)
          throws RException
        {
          // Nothing
        }

        @Override public void eventMeshEnded()
          throws RException
        {
          // Nothing
        }

        @Override public void eventMeshName(
          final String name)
          throws RException
        {
          // Nothing
        }

        @Override public void eventMeshStarted()
          throws RException
        {
          // Nothing
        }

        @Override public void eventMeshTriangle(
          final long index,
          final long v0,
          final long v1,
          final long v2)
          throws RException
        {
          b.addTriangle(v0, v1, v2);
        }

        @Override public void eventMeshTrianglesEnded()
          throws RException
        {
          // Nothing.
        }

        @Override public void eventMeshTrianglesStarted(
          final long count)
          throws RException
        {
          // Nothing.
        }

        @Override public void eventMeshVertexEnded(
          final long index)
          throws RException
        {
          final PVectorI3F<RSpaceObjectType> bitangents = PVectorI3F.zero();
          b.addVertex(
            this.position,
            this.normal,
            this.tangent,
            bitangents,
            this.uv);
        }

        @Override public void eventMeshVertexNormal(
          final long index,
          final PVectorI3F<RSpaceObjectType> in_normal)
          throws RException
        {
          this.normal = in_normal;
        }

        @Override public void eventMeshVertexPosition(
          final long index,
          final PVectorI3F<RSpaceObjectType> in_position)
          throws RException
        {
          this.position = in_position;
        }

        @Override public void eventMeshVertexStarted(
          final long index)
          throws RException
        {
          // Nothing
        }

        @Override public void eventMeshVertexTangent4f(
          final long index,
          final PVectorI4F<RSpaceObjectType> in_tangent)
          throws RException
        {
          this.tangent = in_tangent;
        }

        @Override public void eventMeshVertexUV(
          final long index,
          final PVectorI2F<RSpaceTextureType> in_uv)
          throws RException
        {
          this.uv = in_uv;
        }

        @Override public void eventMeshVerticesEnded(
          final PVectorI3F<RSpaceObjectType> bounds_lower,
          final PVectorI3F<RSpaceObjectType> bounds_upper)
          throws RException
        {
          // Nothing.
        }

        @Override public void eventMeshVerticesStarted(
          final long count)
          throws RException
        {
          // Nothing.
        }
      };

    RBImporter.parseFromStream(stream, events, log);
    return b.build();
  }

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
    final File file,
    final String mesh_name,
    final OptionType<String> mesh_change_name,
    final LogUsableType log)
    throws RException
  {
    InputStream stream = null;

    try {
      stream = new FileInputStream(file);

      return RMeshToolImporterRMB.parseMeshTangents(
        mesh_name,
        mesh_change_name,
        log,
        stream);
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
