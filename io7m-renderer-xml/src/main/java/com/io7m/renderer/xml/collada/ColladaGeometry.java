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

package com.io7m.renderer.xml.collada;

import java.util.Collections;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable public abstract class ColladaGeometry
{
  @Immutable public static final class ColladaMesh extends ColladaGeometry
  {
    private final @Nonnull ColladaPolylist            polylist;
    private final @Nonnull SortedSet<ColladaSourceID> source_ids;

    @SuppressWarnings("synthetic-access") public ColladaMesh(
      final @Nonnull ColladaGeometryID id,
      final @Nonnull ColladaDocument document,
      final @Nonnull SortedSet<ColladaSourceID> in_source_ids,
      final @Nonnull ColladaPolylist p,
      final @Nonnull ColladaAxis axis)
      throws ConstraintError
    {
      super(Type.GEOMETRY_MESH, document, id, axis);
      this.source_ids =
        Constraints.constrainNotNull(in_source_ids, "Source IDs");
      this.polylist = Constraints.constrainNotNull(p, "Polylist");
    }

    public @Nonnull ColladaPolylist getPolylist()
    {
      return this.polylist;
    }

    public @Nonnull SortedSet<ColladaSourceID> getSourceIDs()
    {
      return Collections.unmodifiableSortedSet(this.source_ids);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[ColladaMesh ");
      builder.append(this.getID());
      builder.append(" ");
      builder.append(this.source_ids);
      builder.append(" ");
      builder.append(this.polylist);
      builder.append("]");
      return builder.toString();
    }
  }

  public static enum Type
  {
    GEOMETRY_MESH
  }

  private final @Nonnull ColladaGeometryID id;
  private final @Nonnull Type              type;
  private final @Nonnull ColladaAxis       axis;
  private final @Nonnull ColladaDocument   document;

  private ColladaGeometry(
    final @Nonnull Type in_type,
    final @Nonnull ColladaDocument in_document,
    final @Nonnull ColladaGeometryID in_id,
    final @Nonnull ColladaAxis in_axis)
    throws ConstraintError
  {
    this.document = Constraints.constrainNotNull(in_document, "Document");
    this.type = Constraints.constrainNotNull(in_type, "Type");
    this.id = Constraints.constrainNotNull(in_id, "ID");
    this.axis = Constraints.constrainNotNull(in_axis, "Axis");
  }

  public @Nonnull ColladaAxis getAxis()
  {
    return this.axis;
  }

  public @Nonnull ColladaDocument getDocument()
  {
    return this.document;
  }

  public @Nonnull ColladaGeometryID getID()
  {
    return this.id;
  }

  public @Nonnull Type getType()
  {
    return this.type;
  }
}
