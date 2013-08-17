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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.VectorI2F;
import com.io7m.jtensors.VectorI3F;

@Immutable public abstract class ColladaSource
{
  @Immutable public static final class ColladaSourceArray2F extends
    ColladaSource
  {
    private final @Nonnull ArrayList<VectorI2F> array_2f;

    @SuppressWarnings("synthetic-access") public ColladaSourceArray2F(
      final @Nonnull ColladaSourceID id,
      final @Nonnull ColladaAxis axis)
      throws ConstraintError
    {
      super(Type.SOURCE_TYPE_VECTOR_2F, id, axis);
      this.array_2f = new ArrayList<VectorI2F>();
    }

    public @Nonnull List<VectorI2F> getArray2f()
    {
      return Collections.unmodifiableList(this.array_2f);
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

  @Immutable public static final class ColladaSourceArray3F extends
    ColladaSource
  {
    private final @Nonnull ArrayList<VectorI3F> array_3f;

    @SuppressWarnings("synthetic-access") public ColladaSourceArray3F(
      final @Nonnull ColladaSourceID id,
      final @Nonnull ColladaAxis axis)
      throws ConstraintError
    {
      super(Type.SOURCE_TYPE_VECTOR_3F, id, axis);
      this.array_3f = new ArrayList<VectorI3F>();
    }

    public @Nonnull List<VectorI3F> getArray3f()
    {
      return Collections.unmodifiableList(this.array_3f);
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

  @Immutable public static final class ColladaVertices extends ColladaSource
  {
    private final @Nonnull List<ColladaInput> inputs;

    @SuppressWarnings("synthetic-access") public ColladaVertices(
      final @Nonnull ColladaSourceID id,
      final @Nonnull List<ColladaInput> inputs,
      final @Nonnull ColladaAxis axis)
      throws ConstraintError
    {
      super(Type.SOURCE_VERTICES, id, axis);
      this.inputs = Constraints.constrainNotNull(inputs, "Inputs");
    }

    public @Nonnull List<ColladaInput> getInputs()
    {
      return Collections.unmodifiableList(this.inputs);
    }

    @Override public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("[ColladaVertices ");
      builder.append(this.inputs);
      builder.append("]");
      return builder.toString();
    }
  }

  static enum Type
  {
    SOURCE_TYPE_VECTOR_2F,
    SOURCE_TYPE_VECTOR_3F,
    SOURCE_VERTICES,
  }

  private final @Nonnull ColladaSourceID id;
  private final @Nonnull Type            type;
  private final @Nonnull ColladaAxis     axis;

  private ColladaSource(
    final @Nonnull Type type,
    final @Nonnull ColladaSourceID id,
    final @Nonnull ColladaAxis axis)
    throws ConstraintError
  {
    this.id = Constraints.constrainNotNull(id, "ID");
    this.type = Constraints.constrainNotNull(type, "Type");
    this.axis = Constraints.constrainNotNull(axis, "Axis");
  }

  public @Nonnull ColladaAxis getAxis()
  {
    return this.axis;
  }

  public final @Nonnull ColladaSourceID getID()
  {
    return this.id;
  }

  public final @Nonnull Type getType()
  {
    return this.type;
  }
}
