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

package com.io7m.renderer.xml.rmx;

import com.io7m.renderer.types.RSpaceObjectType;
import com.io7m.renderer.types.RSpaceTextureType;
import com.io7m.renderer.types.RVectorI2F;
import com.io7m.renderer.types.RVectorI3F;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RXMLException;

/**
 * An interface that accepts events from a mesh parser.
 * 
 * @param <E>
 *          The type of exceptions raised.
 */

public interface RXMLMeshParserEventsType<E extends Throwable>
{
  /**
   * Called upon XML parser errors.
   * 
   * @param e
   *          The exception.
   * @throws E
   *           If required.
   */

  void eventError(
    final RXMLException e)
    throws E;

  /**
   * Called when parsing of the mesh finishes. This function is called
   * regardless of whether any error was raised during parsing.
   * 
   * @throws E
   *           If required.
   */

  void eventMeshEnded()
    throws E;

  /**
   * Called when the mesh name is encountered.
   * 
   * @param name
   *          The name.
   * @throws E
   *           If required.
   */

  void eventMeshName(
    final String name)
    throws E;

  /**
   * Called when parsing of the mesh starts. This function is called
   * unconditionally.
   * 
   * @throws E
   *           If required.
   */

  void eventMeshStarted()
    throws E;

  /**
   * Called when a triangle is encountered. Guaranteed not to be called before
   * {@link #eventMeshTrianglesStarted(int)}.
   * 
   * @param index
   *          The triangle index.
   * @param v0
   *          The index of the first vertex.
   * @param v1
   *          The index of the second vertex.
   * @param v2
   *          The index of the third vertex.
   * @throws E
   *           If required.
   */

  void eventMeshTriangle(
    final int index,
    final int v0,
    final int v1,
    final int v2)
    throws E;

  /**
   * Called when parsing of triangles has ended. Guaranteed not to be called
   * before {@link #eventMeshTrianglesStarted(int)}.
   * 
   * @throws E
   *           If required.
   */

  void eventMeshTrianglesEnded()
    throws E;

  /**
   * Called when parsing of triangles is about to begin.
   * 
   * @param count
   *          The number of triangles to be parsed.
   * @throws E
   *           If required.
   */

  void eventMeshTrianglesStarted(
    final int count)
    throws E;

  /**
   * Called when parsing of a mesh vertex has ended. Guaranteed not to be
   * called before {@link #eventMeshVertexStarted(int)} for the given index.
   * 
   * @param index
   *          The index of the vertex.
   * @throws E
   *           If required.
   */

  void eventMeshVertexEnded(
    final int index)
    throws E;

  /**
   * Called when parsing the normal vector for a vertex.
   * 
   * @param index
   *          The vertex index.
   * @param normal
   *          The normal vector.
   * @throws E
   *           If required.
   */

  void eventMeshVertexNormal(
    final int index,
    final RVectorI3F<RSpaceObjectType> normal)
    throws E;

  /**
   * Called when parsing the position for a vertex.
   * 
   * @param index
   *          The vertex index.
   * @param position
   *          The position.
   * @throws E
   *           If required.
   */

  void eventMeshVertexPosition(
    final int index,
    final RVectorI3F<RSpaceObjectType> position)
    throws E;

  /**
   * Called when parsing of a mesh vertex has started.
   * 
   * @param index
   *          The index of the vertex.
   * @throws E
   *           If required.
   */

  void eventMeshVertexStarted(
    final int index)
    throws E;

  /**
   * Called when parsing the tangent for a vertex.
   * 
   * @param index
   *          The vertex index.
   * @param tangent
   *          The tangent.
   * @throws E
   *           If required.
   */

  void eventMeshVertexTangent4f(
    final int index,
    final RVectorI4F<RSpaceObjectType> tangent)
    throws E;

  /**
   * Called when parsing the UV coordinates for a vertex.
   * 
   * @param index
   *          The vertex index.
   * @param uv
   *          The UV coordinates.
   * @throws E
   *           If required.
   */

  void eventMeshVertexUV(
    final int index,
    final RVectorI2F<RSpaceTextureType> uv)
    throws E;

  /**
   * Called when parsing of mesh vertices has ended.
   * 
   * @param bounds_lower
   *          The accumulated lower bounds of all of the vertex positions.
   * @param bounds_upper
   *          The accumulated upper bounds of all of the vertex positions.
   * @throws E
   *           If required.
   */

  void eventMeshVerticesEnded(
    final RVectorI3F<RSpaceObjectType> bounds_lower,
    final RVectorI3F<RSpaceObjectType> bounds_upper)
    throws E;

  /**
   * Called when parsing of mesh vertices has started.
   * 
   * @param count
   *          The number of vertices to be parsed.
   * @throws E
   *           If required.
   */

  void eventMeshVerticesStarted(
    final int count)
    throws E;
}
