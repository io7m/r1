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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;

/**
 * The type of COLLADA sources.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public abstract class RColladaSource
{
  /**
   * A source array consisting of two-element floating point vectors.
   */

  @EqualityReference public static final class ColladaSourceArray2F extends
    RColladaSource
  {
    private final List<VectorI2F> array_2f;

    /**
     * Construct a source array.
     * 
     * @param id
     *          The source ID.
     * @param axis
     *          The axis.
     */

    public ColladaSourceArray2F(
      final RColladaSourceID id,
      final RColladaAxis axis)
    {
      super(Type.SOURCE_TYPE_VECTOR_2F, id, axis);
      this.array_2f = new ArrayList<VectorI2F>();
    }

    /**
     * @return A read-only view of the current list of values.
     */

    public List<VectorI2F> getArray2f()
    {
      final List<VectorI2F> r = Collections.unmodifiableList(this.array_2f);
      assert r != null;
      return r;
    }

    /**
     * Add a value.
     * 
     * @param x
     *          The X component.
     * @param y
     *          The Y component.
     */

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

  /**
   * A source array consisting of three-element floating point vectors.
   */

  @EqualityReference public static final class ColladaSourceArray3F extends
    RColladaSource
  {
    private final List<VectorI3F> array_3f;

    /**
     * Construct a source array.
     * 
     * @param id
     *          The source ID.
     * @param axis
     *          The axis.
     */

    public ColladaSourceArray3F(
      final RColladaSourceID id,
      final RColladaAxis axis)
    {
      super(Type.SOURCE_TYPE_VECTOR_3F, id, axis);
      this.array_3f = new ArrayList<VectorI3F>();
    }

    /**
     * @return A read-only view of the current list of values.
     */

    public List<VectorI3F> getArray3f()
    {
      final List<VectorI3F> r = Collections.unmodifiableList(this.array_3f);
      assert r != null;
      return r;
    }

    /**
     * Add a value.
     * 
     * @param x
     *          The X component.
     * @param y
     *          The Y component.
     * @param z
     *          The Z component.
     */

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

  @EqualityReference public static final class ColladaVertices extends
    RColladaSource
  {
    private final List<RColladaInput> inputs;

    /**
     * Construct a source array.
     * 
     * @param id
     *          The source ID.
     * @param in_inputs
     *          The list of inputs.
     * @param axis
     *          The axis.
     */

    public ColladaVertices(
      final RColladaSourceID id,
      final List<RColladaInput> in_inputs,
      final RColladaAxis axis)
    {
      super(Type.SOURCE_VERTICES, id, axis);
      this.inputs = NullCheck.notNullAll(in_inputs, "Inputs");
    }

    /**
     * @return A read-only view of the current list of inputs.
     */

    public List<RColladaInput> getInputs()
    {
      final List<RColladaInput> r = Collections.unmodifiableList(this.inputs);
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

  private final RColladaAxis     axis;
  private final RColladaSourceID id;
  private final Type            type;

  private RColladaSource(
    final Type in_type,
    final RColladaSourceID in_id,
    final RColladaAxis in_axis)
  {
    this.id = NullCheck.notNull(in_id, "ID");
    this.type = NullCheck.notNull(in_type, "Type");
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
   * @return The identifier of the source.
   */

  public final RColladaSourceID getID()
  {
    return this.id;
  }

  /**
   * @return The source type.
   */

  public final Type getType()
  {
    return this.type;
  }
}
