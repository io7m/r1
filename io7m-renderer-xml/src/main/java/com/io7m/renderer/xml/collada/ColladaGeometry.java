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

import com.io7m.jnull.NullCheck;

public abstract class ColladaGeometry
{
  public static final class ColladaMesh extends ColladaGeometry
  {
    private final ColladaPolylist            polylist;
    private final SortedSet<ColladaSourceID> source_ids;

    @SuppressWarnings("synthetic-access") public ColladaMesh(
      final ColladaGeometryID id,
      final ColladaDocument document,
      final SortedSet<ColladaSourceID> in_source_ids,
      final ColladaPolylist p,
      final ColladaAxis axis)
    {
      super(Type.GEOMETRY_MESH, document, id, axis);
      this.source_ids = NullCheck.notNull(in_source_ids, "Source IDs");
      this.polylist = NullCheck.notNull(p, "Polylist");
    }

    public ColladaPolylist getPolylist()
    {
      return this.polylist;
    }

    public SortedSet<ColladaSourceID> getSourceIDs()
    {
      final SortedSet<ColladaSourceID> r =
        Collections.unmodifiableSortedSet(this.source_ids);
      assert r != null;
      return r;
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
      final String r = builder.toString();
      assert r != null;
      return r;
    }
  }

  public static enum Type
  {
    GEOMETRY_MESH
  }

  private final ColladaGeometryID id;
  private final Type              type;
  private final ColladaAxis       axis;
  private final ColladaDocument   document;

  private ColladaGeometry(
    final Type in_type,
    final ColladaDocument in_document,
    final ColladaGeometryID in_id,
    final ColladaAxis in_axis)
  {
    this.document = NullCheck.notNull(in_document, "Document");
    this.type = NullCheck.notNull(in_type, "Type");
    this.id = NullCheck.notNull(in_id, "ID");
    this.axis = NullCheck.notNull(in_axis, "Axis");
  }

  public ColladaAxis getAxis()
  {
    return this.axis;
  }

  public ColladaDocument getDocument()
  {
    return this.document;
  }

  public ColladaGeometryID getID()
  {
    return this.id;
  }

  public Type getType()
  {
    return this.type;
  }
}
