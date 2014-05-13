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

public interface RXMLMeshParserEvents<E extends Throwable>
{
  public void eventError(
    final RXMLException e)
    throws E;

  /**
   * Called when parsing of the mesh finishes. This function is called
   * regardless of whether any error was raised during parsing.
   */

  public void eventMeshEnded()
    throws E;

  public void eventMeshName(
    final String name)
    throws E;

  /**
   * Called when parsing of the mesh starts. This function is called
   * unconditionally.
   */

  public void eventMeshStarted()
    throws E;

  public void eventMeshTriangle(
    final int index,
    final int v0,
    final int v1,
    final int v2)
    throws E;

  public void eventMeshTrianglesEnded()
    throws E;

  public void eventMeshTrianglesStarted(
    final int count)
    throws E;

  public void eventMeshType(
    final RXMLMeshType mt)
    throws E;

  public void eventMeshVertexBitangent(
    final int index,
    final RVectorI3F<RSpaceObjectType> bitangent)
    throws E;

  public void eventMeshVertexEnded(
    final int index)
    throws E;

  public void eventMeshVertexNormal(
    final int index,
    final RVectorI3F<RSpaceObjectType> normal)
    throws E;

  public void eventMeshVertexPosition(
    final int index,
    final RVectorI3F<RSpaceObjectType> position)
    throws E;

  public void eventMeshVertexStarted(
    final int index)
    throws E;

  public void eventMeshVertexTangent3f(
    final int index,
    final RVectorI3F<RSpaceObjectType> tangent)
    throws E;

  public void eventMeshVertexTangent4f(
    final int index,
    final RVectorI4F<RSpaceObjectType> tangent)
    throws E;

  public void eventMeshVertexUV(
    final int index,
    final RVectorI2F<RSpaceTextureType> uv)
    throws E;

  public void eventMeshVerticesEnded(
    final RVectorI3F<RSpaceObjectType> bounds_lower,
    final RVectorI3F<RSpaceObjectType> bounds_upper)
    throws E;

  public void eventMeshVerticesStarted(
    final int count)
    throws E;
}
