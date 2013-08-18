/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceObject;
import com.io7m.renderer.RSpaceTexture;
import com.io7m.renderer.RVectorI2F;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.xml.RXMLException;

public interface RXMLMeshParserEvents<E extends Throwable>
{
  public void eventError(
    final @Nonnull RXMLException e)
    throws E,
      ConstraintError;

  /**
   * Called when parsing of the mesh finishes. This function is called
   * regardless of whether any error was raised during parsing.
   */

  public void eventMeshEnded()
    throws E,
      ConstraintError;

  public void eventMeshName(
    final @Nonnull String name)
    throws E,
      ConstraintError;

  /**
   * Called when parsing of the mesh starts. This function is called
   * unconditionally.
   */

  public void eventMeshStarted()
    throws E,
      ConstraintError;

  public void eventMeshTriangle(
    final int index,
    final int v0,
    final int v1,
    final int v2)
    throws E,
      ConstraintError;

  public void eventMeshTrianglesEnded()
    throws E,
      ConstraintError;

  public void eventMeshTrianglesStarted(
    final int count)
    throws E,
      ConstraintError;

  public void eventMeshType(
    final @Nonnull RXMLMeshType mt)
    throws E,
      ConstraintError;

  public void eventMeshVertexBitangent(
    final int index,
    final @Nonnull RVectorI3F<RSpaceObject> bitangent)
    throws E,
      ConstraintError;

  public void eventMeshVertexEnded(
    final int index)
    throws E,
      ConstraintError;

  public void eventMeshVertexNormal(
    final int index,
    final @Nonnull RVectorI3F<RSpaceObject> normal)
    throws E,
      ConstraintError;

  public void eventMeshVertexPosition(
    final int index,
    final @Nonnull RVectorI3F<RSpaceObject> position)
    throws E,
      ConstraintError;

  public void eventMeshVertexStarted(
    final int index)
    throws E,
      ConstraintError;

  public void eventMeshVertexTangent(
    final int index,
    final @Nonnull RVectorI3F<RSpaceObject> tangent)
    throws E,
      ConstraintError;

  public void eventMeshVertexUV(
    final int index,
    final @Nonnull RVectorI2F<RSpaceTexture> uv)
    throws E,
      ConstraintError;

  public void eventMeshVerticesEnded()
    throws E,
      ConstraintError;

  public void eventMeshVerticesStarted(
    final int count)
    throws E,
      ConstraintError;
}
