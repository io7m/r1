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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.meshes.RMeshTangents;
import com.io7m.r1.meshes.RMeshTangentsVertex;
import com.io7m.r1.meshes.RMeshTriangle;
import com.io7m.r1.types.RSpaceObjectType;
import com.io7m.r1.types.RSpaceTextureType;
import com.io7m.r1.types.RVectorI2F;
import com.io7m.r1.types.RVectorI3F;
import com.io7m.r1.types.RVectorI4F;

/**
 * An exporter to serialize {@link RMeshTangents} meshes to a stream in a
 * simple binary format.
 */

@EqualityReference public final class RBExporter
{
  /**
   * @return A new exporter.
   */

  public static RBExporter newExporter()
  {
    return new RBExporter();
  }

  private static void writeMagicNumber(
    final OutputStream s)
    throws IOException
  {
    s.write('R');
    s.write('M');
    s.write('B');
    s.write(0x0A);
  }

  private final ByteBuffer temp;

  private RBExporter()
  {
    final ByteBuffer m = ByteBuffer.allocate(8);
    assert m != null;
    this.temp = m;
    this.temp.order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Serialize the given mesh to the stream.
   *
   * @param m
   *          The mesh
   * @param s
   *          The stream
   * @throws IOException
   *           On I/O errors
   */

  public void toStream(
    final RMeshTangents m,
    final OutputStream s)
    throws IOException
  {
    NullCheck.notNull(m, "Mesh");
    NullCheck.notNull(s, "Output stream");

    RBExporter.writeMagicNumber(s);
    this.writeUnsigned32(RBConstants.RMB_VERSION, s);

    this.writeName(m, s);
    this.writeVerticesSize(s, m.verticesGet());
    this.writeTrianglesSize(s, m.trianglesGet());

    this.writeVertices(m, s);
    this.writeTriangles(m, s);
  }

  private void writeFloat32(
    final float x,
    final OutputStream s)
    throws IOException
  {
    this.temp.rewind();
    this.temp.putFloat(0, x);
    final byte[] a = this.temp.array();
    s.write(a, 0, 4);
  }

  private void writeName(
    final RMeshTangents m,
    final OutputStream s)
    throws IOException
  {
    final String original = m.getName();
    final String name = new String(original.getBytes(), "UTF-8");
    final byte[] name_bytes = name.getBytes();
    final int name_length = name_bytes.length;

    final int offset = 2;
    final long name_aligned_size =
      RBAlign.alignedSize(name_length + offset, 4);
    assert name_aligned_size < 65536;
    final int pad_size = (int) (name_aligned_size - (name_length + offset));

    this.writeUnsigned16(name_length, s);
    s.write(name_bytes);

    for (int index = 0; index < pad_size; ++index) {
      s.write(0x0);
    }
  }

  private void writeNormal(
    final OutputStream s,
    final List<RVectorI3F<RSpaceObjectType>> normals,
    final RMeshTangentsVertex v)
    throws IOException
  {
    final RVectorI3F<RSpaceObjectType> n = normals.get(v.getNormal());
    this.writeFloat32(n.getXF(), s);
    this.writeFloat32(n.getYF(), s);
    this.writeFloat32(n.getZF(), s);
  }

  private void writePosition(
    final OutputStream s,
    final List<RVectorI3F<RSpaceObjectType>> positions,
    final RMeshTangentsVertex v)
    throws IOException
  {
    final RVectorI3F<RSpaceObjectType> p = positions.get(v.getPosition());
    this.writeFloat32(p.getXF(), s);
    this.writeFloat32(p.getYF(), s);
    this.writeFloat32(p.getZF(), s);
  }

  private void writeTangent(
    final OutputStream s,
    final List<RVectorI4F<RSpaceObjectType>> tangents,
    final RMeshTangentsVertex v)
    throws IOException
  {
    final RVectorI4F<RSpaceObjectType> t = tangents.get(v.getTangent());
    this.writeFloat32(t.getXF(), s);
    this.writeFloat32(t.getYF(), s);
    this.writeFloat32(t.getZF(), s);
    this.writeFloat32(t.getWF(), s);
  }

  private void writeTriangles(
    final RMeshTangents m,
    final OutputStream s)
    throws IOException
  {
    final List<RMeshTriangle> triangles = m.trianglesGet();

    for (final RMeshTriangle t : triangles) {
      this.writeUnsigned32(t.getV0(), s);
      this.writeUnsigned32(t.getV1(), s);
      this.writeUnsigned32(t.getV2(), s);
    }
  }

  private void writeTrianglesSize(
    final OutputStream s,
    final List<RMeshTriangle> triangles)
    throws IOException
  {
    this.writeUnsigned32(triangles.size(), s);
  }

  private void writeUnsigned16(
    final int x,
    final OutputStream s)
    throws IOException
  {
    this.temp.rewind();
    Unsigned16.packToBuffer(x, this.temp, 0);
    final byte[] a = this.temp.array();
    s.write(a, 0, 2);
  }

  private void writeUnsigned32(
    final long x,
    final OutputStream s)
    throws IOException
  {
    this.temp.rewind();
    Unsigned32.packToBuffer(x, this.temp, 0);
    final byte[] a = this.temp.array();
    s.write(a, 0, 4);
  }

  private void writeUV(
    final OutputStream s,
    final List<RVectorI2F<RSpaceTextureType>> uvs,
    final RMeshTangentsVertex v)
    throws IOException
  {
    {
      final RVectorI2F<RSpaceTextureType> u = uvs.get(v.getUV());
      this.writeFloat32(u.getXF(), s);
      this.writeFloat32(u.getYF(), s);
    }
  }

  private void writeVertices(
    final RMeshTangents m,
    final OutputStream s)
    throws IOException
  {
    final List<RMeshTangentsVertex> vertices = m.verticesGet();
    final List<RVectorI3F<RSpaceObjectType>> positions = m.positionsGet();
    final List<RVectorI3F<RSpaceObjectType>> normals = m.normalsGet();
    final List<RVectorI2F<RSpaceTextureType>> uvs = m.uvsGet();
    final List<RVectorI4F<RSpaceObjectType>> tangents = m.tangentsGet();

    for (final RMeshTangentsVertex v : vertices) {
      assert v != null;
      this.writePosition(s, positions, v);
      this.writeNormal(s, normals, v);
      this.writeTangent(s, tangents, v);
      this.writeUV(s, uvs, v);
    }
  }

  private void writeVerticesSize(
    final OutputStream s,
    final List<RMeshTangentsVertex> vertices)
    throws IOException
  {
    this.writeUnsigned32(vertices.size(), s);
  }
}
