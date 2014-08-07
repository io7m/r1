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
import java.util.Comparator;
import java.util.List;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A list of polygons and inputs.
 */

@EqualityStructural public final class RColladaPolylist
{
  private final List<RColladaInput> inputs;
  private final List<RColladaPoly>  polygons;

  /**
   * Construct a list of polygons.
   *
   * @param in_inputs
   *          The inputs.
   * @param in_polygons
   *          The polygons.
   */

  public RColladaPolylist(
    final List<RColladaInput> in_inputs,
    final List<RColladaPoly> in_polygons)
  {
    this.inputs = NullCheck.notNullAll(in_inputs, "Inputs");
    this.polygons = NullCheck.notNullAll(in_polygons, "Indices");

    Collections.sort(in_inputs, new Comparator<RColladaInput>() {
      @Override public int compare(
        final @Nullable RColladaInput o1,
        final @Nullable RColladaInput o2)
      {
        assert o1 != null;
        assert o2 != null;
        return Integer.compare(o1.getOffset(), o2.getOffset());
      }
    });
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final RColladaPolylist other = (RColladaPolylist) obj;
    if (!this.inputs.equals(other.inputs)) {
      return false;
    }
    if (!this.polygons.equals(other.polygons)) {
      return false;
    }
    return true;
  }

  /**
   * @return A read-only view of the list of inputs for this polylist. The
   *         list of inputs is sorted according to each input's offsets. That
   *         is, the input declared with offset 0 will be at the head of the
   *         list.
   */

  public List<RColladaInput> getInputs()
  {
    final List<RColladaInput> r = Collections.unmodifiableList(this.inputs);
    assert r != null;
    return r;
  }

  /**
   * @return A read-only view of the current list of polygons.
   */

  public List<RColladaPoly> getPolygons()
  {
    final List<RColladaPoly> r = Collections.unmodifiableList(this.polygons);
    assert r != null;
    return r;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.inputs.hashCode();
    result = (prime * result) + this.polygons.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[RColladaPolylist ");
    builder.append(this.inputs);
    builder.append(" (");
    builder.append(this.polygons.size());
    builder.append(" polygons)]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
