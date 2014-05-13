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

package com.io7m.renderer.tests;

import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;

public final class FakeIndexBuffer implements IndexBufferType
{
  private final UsageHint        hint;
  private final int              name;
  private final RangeInclusiveL  range;
  private final JCGLUnsignedType type;

  public FakeIndexBuffer(
    final int in_name,
    final RangeInclusiveL in_range,
    final UsageHint in_hint,
    final JCGLUnsignedType in_type)
  {
    this.name = in_name;
    this.range = NullCheck.notNull(in_range, "Range");
    this.hint = NullCheck.notNull(in_hint, "Hint");
    this.type = NullCheck.notNull(in_type, "Type");
  }

  @Override public long bufferGetElementSizeBytes()
  {
    return this.type.getSizeBytes();
  }

  @Override public RangeInclusiveL bufferGetRange()
  {
    return this.range;
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
    final FakeIndexBuffer other = (FakeIndexBuffer) obj;
    return (this.hint == other.hint)
      && (this.name == other.name)
      && (this.range.equals(other.range))
      && (this.type == other.type);
  }

  @Override public int getGLName()
  {
    return this.name;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.hint.hashCode();
    result = (prime * result) + this.name;
    result = (prime * result) + this.range.hashCode();
    result = (prime * result) + this.type.hashCode();
    return result;
  }

  @Override public JCGLUnsignedType indexGetType()
  {
    return this.type;
  }

  @Override public UsageHint indexGetUsageHint()
  {
    return this.hint;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.range.getInterval() * this.type.getSizeBytes();
  }

  @Override public boolean resourceIsDeleted()
  {
    return false;
  }
}
