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

package com.io7m.renderer.xml.collada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;

public abstract class ColladaSource
{
  public static final class ColladaSourceArray2F extends ColladaSource
  {
    private final ArrayList<VectorI2F> array_2f;

    @SuppressWarnings("synthetic-access") public ColladaSourceArray2F(
      final ColladaSourceID id,
      final ColladaAxis axis)
    {
      super(Type.SOURCE_TYPE_VECTOR_2F, id, axis);
      this.array_2f = new ArrayList<VectorI2F>();
    }

    public List<VectorI2F> getArray2f()
    {
      final List<VectorI2F> r = Collections.unmodifiableList(this.array_2f);
      assert r != null;
      return r;
    }

    public void put2f(
      final float x,
      final float y)
    {
      this.array_2f.add(new VectorI2F(x, y));
    }

    @Override public String toString()
    {
      return "[ColladaSourceArray2F]";
    }
  }

  public static final class ColladaSourceArray3F extends ColladaSource
  {
    private final ArrayList<VectorI3F> array_3f;

    @SuppressWarnings("synthetic-access") public ColladaSourceArray3F(
      final ColladaSourceID id,
      final ColladaAxis axis)
    {
      super(Type.SOURCE_TYPE_VECTOR_3F, id, axis);
      this.array_3f = new ArrayList<VectorI3F>();
    }

    public List<VectorI3F> getArray3f()
    {
      final List<VectorI3F> r = Collections.unmodifiableList(this.array_3f);
      assert r != null;
      return r;
    }

    public void put3f(
      final float x,
      final float y,
      final float z)
    {
      this.array_3f.add(new VectorI3F(x, y, z));
    }

    @Override public String toString()
    {
      return "[ColladaSourceArray3F]";
    }
  }

  /**
   * The COLLADA 1.4 specification calls "vertices" a "sink" element and then
   * uses it as a "source" throughout.
   */

  public static final class ColladaVertices extends ColladaSource
  {
    private final List<ColladaInput> inputs;

    @SuppressWarnings("synthetic-access") public ColladaVertices(
      final ColladaSourceID id,
      final List<ColladaInput> in_inputs,
      final ColladaAxis axis)
    {
      super(Type.SOURCE_VERTICES, id, axis);
      this.inputs = NullCheck.notNullAll(in_inputs, "Inputs");
    }

    public List<ColladaInput> getInputs()
    {
      final List<ColladaInput> r = Collections.unmodifiableList(this.inputs);
      assert r != null;
      return r;
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[ColladaVertices ");
      builder.append(this.inputs);
      builder.append("]");
      final String r = builder.toString();
      assert r != null;
      return r;
    }
  }

  static enum Type
  {
    SOURCE_TYPE_VECTOR_2F,
    SOURCE_TYPE_VECTOR_3F,
    SOURCE_VERTICES,
  }

  private final ColladaSourceID id;
  private final Type            type;
  private final ColladaAxis     axis;

  private ColladaSource(
    final Type in_type,
    final ColladaSourceID in_id,
    final ColladaAxis in_axis)
  {
    this.id = NullCheck.notNull(in_id, "ID");
    this.type = NullCheck.notNull(in_type, "Type");
    this.axis = NullCheck.notNull(in_axis, "Axis");
  }

  public ColladaAxis getAxis()
  {
    return this.axis;
  }

  public final ColladaSourceID getID()
  {
    return this.id;
  }

  public final Type getType()
  {
    return this.type;
  }
}
