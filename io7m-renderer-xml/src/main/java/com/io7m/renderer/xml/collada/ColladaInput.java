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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

@Immutable public final class ColladaInput
{
  private final @Nonnull ColladaSourceID source;
  private final @Nonnull ColladaSemantic semantic;
  private final int                      offset;

  public ColladaInput(
    final @Nonnull ColladaSourceID in_source,
    final @Nonnull ColladaSemantic in_semantic,
    final int in_offset)
    throws ConstraintError
  {
    this.source = Constraints.constrainNotNull(in_source, "Source");
    this.semantic = Constraints.constrainNotNull(in_semantic, "Semantic");
    this.offset =
      Constraints.constrainRange(in_offset, 0, Integer.MAX_VALUE, "Offset");
  }

  @Override public boolean equals(
    final Object obj)
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

  public int getOffset()
  {
    return this.offset;
  }

  public @Nonnull ColladaSemantic getSemantic()
  {
    return this.semantic;
  }

  public @Nonnull ColladaSourceID getSource()
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
    return builder.toString();
  }
}
