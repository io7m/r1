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

import java.util.Map;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.ArrayAttributeType;
import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.JCGLExceptionAttributeMissing;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;

public final class FakeArrayBuffer implements ArrayBufferType
{
  private final ArrayDescriptor descriptor;
  private final UsageHint       hint;
  private final int             name;
  private final RangeInclusiveL range;

  public FakeArrayBuffer(
    final int in_name,
    final RangeInclusiveL in_range,
    final ArrayDescriptor in_descriptor,
    final UsageHint in_hint)
  {
    this.name = in_name;
    this.range = NullCheck.notNull(in_range, "Range");
    this.descriptor = NullCheck.notNull(in_descriptor, "Descriptor");
    this.hint = NullCheck.notNull(in_hint, "Hint");
  }

  @Override public ArrayAttributeType arrayGetAttribute(
    final String attribute_name)
    throws JCGLExceptionAttributeMissing
  {
    final Map<String, ArrayAttributeDescriptor> aa =
      this.descriptor.getAttributes();
    if (aa.containsKey(attribute_name) == false) {
      throw JCGLExceptionAttributeMissing.noSuchAttribute(attribute_name);
    }

    return new ArrayAttributeType() {
      @Override public ArrayBufferUsableType getArray()
      {
        return FakeArrayBuffer.this;
      }

      @Override public ArrayAttributeDescriptor getDescriptor()
      {
        final ArrayAttributeDescriptor r = aa.get(attribute_name);
        assert r != null;
        return r;
      }
    };
  }

  @Override public ArrayDescriptor arrayGetDescriptor()
  {
    return this.descriptor;
  }

  @Override public UsageHint arrayGetUsageHint()
  {
    return this.hint;
  }

  @Override public long bufferGetElementSizeBytes()
  {
    return this.descriptor.getElementSizeBytes();
  }

  @Override public RangeInclusiveL bufferGetRange()
  {
    return this.range;
  }

  @Override public boolean equals(
    @Nullable final Object obj)
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
    final FakeArrayBuffer other = (FakeArrayBuffer) obj;
    return this.descriptor.equals(other.descriptor)
      && (this.hint == other.hint)
      && (this.name == other.name)
      && (this.range.equals(other.range));
  }

  @Override public int getGLName()
  {
    return this.name;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.descriptor.hashCode();
    result = (prime * result) + this.hint.hashCode();
    result = (prime * result) + this.name;
    result = (prime * result) + this.range.hashCode();
    return result;
  }

  @Override public long resourceGetSizeBytes()
  {
    return this.range.getInterval() * this.descriptor.getElementSizeBytes();
  }

  @Override public boolean resourceIsDeleted()
  {
    return false;
  }
}
