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

package com.io7m.r1.xml.collada;

import java.util.Collections;
import java.util.SortedSet;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * <p>
 * The type of COLLADA geometries.
 * </p>
 */

@EqualityReference public abstract class RColladaGeometry
{
  /**
   * A polygon mesh.
   */

  @EqualityReference public static final class RColladaMesh extends
    RColladaGeometry
  {
    private final RColladaPolylist            polylist;
    private final SortedSet<RColladaSourceID> source_ids;

    /**
     * Construct a polygon mesh.
     * 
     * @param id
     *          The geometry ID.
     * @param document
     *          The COLLADA document.
     * @param in_source_ids
     *          The list of source IDs.
     * @param p
     *          The polygon list.
     * @param axis
     *          The axis type.
     */

    @SuppressWarnings("synthetic-access") public RColladaMesh(
      final RColladaGeometryID id,
      final RColladaDocument document,
      final SortedSet<RColladaSourceID> in_source_ids,
      final RColladaPolylist p,
      final RColladaAxis axis)
    {
      super(Type.GEOMETRY_MESH, document, id, axis);
      this.source_ids = NullCheck.notNull(in_source_ids, "Source IDs");
      this.polylist = NullCheck.notNull(p, "Polylist");
    }

    /**
     * @return The list of polygons.
     */

    public RColladaPolylist getPolylist()
    {
      return this.polylist;
    }

    /**
     * @return The set of source IDs.
     */

    public SortedSet<RColladaSourceID> getSourceIDs()
    {
      final SortedSet<RColladaSourceID> r =
        Collections.unmodifiableSortedSet(this.source_ids);
      assert r != null;
      return r;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[RColladaMesh ");
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

  /**
   * An enum representing the different types of geometries.
   */

  public static enum Type
  {
    /**
     * A polygon mesh.
     */

    GEOMETRY_MESH
  }

  private final RColladaAxis       axis;
  private final RColladaDocument   document;
  private final RColladaGeometryID id;
  private final Type              type;

  private RColladaGeometry(
    final Type in_type,
    final RColladaDocument in_document,
    final RColladaGeometryID in_id,
    final RColladaAxis in_axis)
  {
    this.document = NullCheck.notNull(in_document, "Document");
    this.type = NullCheck.notNull(in_type, "Type");
    this.id = NullCheck.notNull(in_id, "ID");
    this.axis = NullCheck.notNull(in_axis, "Axis");
  }

  /**
   * @return The axis type.
   */

  public RColladaAxis getAxis()
  {
    return this.axis;
  }

  /**
   * @return The COLLADA document.
   */

  public RColladaDocument getDocument()
  {
    return this.document;
  }

  /**
   * @return The geometry ID.
   */

  public RColladaGeometryID getID()
  {
    return this.id;
  }

  /**
   * @return The geometry type.
   */

  public Type getType()
  {
    return this.type;
  }
}
