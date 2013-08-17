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
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable public final class ColladaPolylist
{
  private final @Nonnull List<ColladaInput> inputs;
  private final @Nonnull List<ColladaPoly>  polygons;

  public ColladaPolylist(
    final @Nonnull List<ColladaInput> inputs,
    final @Nonnull List<ColladaPoly> polygons)
    throws ConstraintError
  {
    this.inputs = Constraints.constrainNotNull(inputs, "Inputs");
    this.polygons = Constraints.constrainNotNull(polygons, "Indices");

    Collections.sort(inputs, new Comparator<ColladaInput>() {
      @Override public int compare(
        final @Nonnull ColladaInput o1,
        final @Nonnull ColladaInput o2)
      {
        return Integer.compare(o1.getOffset(), o2.getOffset());
      }
    });
  }

  /**
   * Retrieve the list of inputs for this polylist. The list of inputs is
   * sorted according to each input's offsets. That is, the input declared
   * with offset 0 will be at the head of the list.
   */

  public @Nonnull List<ColladaInput> getInputs()
  {
    return Collections.unmodifiableList(this.inputs);
  }

  public @Nonnull List<ColladaPoly> getPolygons()
  {
    return Collections.unmodifiableList(this.polygons);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[ColladaPolylist ");
    builder.append(this.inputs);
    builder.append(" (");
    builder.append(this.polygons.size());
    builder.append(" polygons)]");
    return builder.toString();
  }

}
