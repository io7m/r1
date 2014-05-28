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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeCheck;

/**
 * A COLLADA input specification.
 */

@EqualityStructural public final class ColladaInput
{
  private final int             offset;
  private final ColladaSemantic semantic;
  private final ColladaSourceID source;

  /**
   * Construct an input.
   * 
   * @param in_source
   *          The source.
   * @param in_semantic
   *          The semantic.
   * @param in_offset
   *          The offset.
   */

  public ColladaInput(
    final ColladaSourceID in_source,
    final ColladaSemantic in_semantic,
    final int in_offset)
  {
    this.source = NullCheck.notNull(in_source, "Source");
    this.semantic = NullCheck.notNull(in_semantic, "Semantic");
    this.offset =
      (int) RangeCheck.checkGreaterEqual(
        in_offset,
        "Offset",
        0,
        "Minimum offset");
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
    final ColladaInput other = (ColladaInput) obj;
    if (this.offset != other.offset) {
      return false;
    }
    if (this.semantic != other.semantic) {
      return false;
    }
    if (!this.source.equals(other.source)) {
      return false;
    }
    return true;
  }

  /**
   * @return The offset.
   */

  public int getOffset()
  {
    return this.offset;
  }

  /**
   * @return The semantic.
   */

  public ColladaSemantic getSemantic()
  {
    return this.semantic;
  }

  /**
   * @return The source.
   */

  public ColladaSourceID getSource()
  {
    return this.source;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.offset;
    result = (prime * result) + this.semantic.hashCode();
    result = (prime * result) + this.source.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[ColladaInput ");
    builder.append(this.source);
    builder.append(" ");
    builder.append(this.semantic);
    builder.append(" ");
    builder.append(this.offset);
    builder.append("]");
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
