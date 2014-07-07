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

import com.io7m.jcanephora.ArrayBufferType;
import com.io7m.jcanephora.ArrayBufferUpdateUnmappedType;
import com.io7m.jcanephora.ArrayBufferUsableType;
import com.io7m.jcanephora.ArrayDescriptor;
import com.io7m.jcanephora.IndexBufferType;
import com.io7m.jcanephora.IndexBufferUpdateUnmappedType;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionDeleted;
import com.io7m.jcanephora.JCGLUnsignedType;
import com.io7m.jcanephora.UsageHint;
import com.io7m.jcanephora.api.JCGLArrayBuffersType;
import com.io7m.jcanephora.api.JCGLIndexBuffersType;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;

public final class FakeJCGLArrayAndIndexBuffers implements
  JCGLArrayBuffersType,
  JCGLIndexBuffersType
{
  private @Nullable Integer bound;
  private int               names = 1;

  @Override public ArrayBufferType arrayBufferAllocate(
    final long elements,
    final ArrayDescriptor descriptor,
    final UsageHint usage)
    throws JCGLException
  {
    final RangeInclusiveL in_range = new RangeInclusiveL(0, elements - 1);
    return new FakeArrayBuffer(this.nextName(), in_range, descriptor, usage);
  }

  @Override public void arrayBufferBind(
    final ArrayBufferUsableType buffer)
    throws JCGLException
  {
    this.bound = Integer.valueOf(buffer.getGLName());
  }

  @Override public void arrayBufferDelete(
    final ArrayBufferType id)
    throws JCGLException,
      JCGLExceptionDeleted
  {
    // Nothing.
  }

  @Override public boolean arrayBufferIsBound(
    final ArrayBufferUsableType id)
    throws JCGLException
  {
    final Integer b = this.bound;
    if (b == null) {
      return false;
    }
    return b.intValue() == id.getGLName();
  }

  @Override public void arrayBufferUnbind()
    throws JCGLException
  {
    this.bound = null;
  }

  @Override public void arrayBufferUpdate(
    final ArrayBufferUpdateUnmappedType data)
    throws JCGLException
  {
    // Nothing.
  }

  @Override public IndexBufferType indexBufferAllocate(
    final ArrayBufferUsableType buffer,
    final long indices,
    final UsageHint usage)
    throws JCGLException
  {
    final RangeInclusiveL in_range = new RangeInclusiveL(0, indices - 1);
    return new FakeIndexBuffer(
      this.nextName(),
      in_range,
      usage,
      JCGLUnsignedType.TYPE_UNSIGNED_INT);
  }

  @Override public IndexBufferType indexBufferAllocateType(
    final JCGLUnsignedType type,
    final long indices,
    final UsageHint usage)
    throws JCGLException
  {
    final RangeInclusiveL in_range = new RangeInclusiveL(0, indices - 1);
    return new FakeIndexBuffer(this.nextName(), in_range, usage, type);
  }

  @Override public void indexBufferDelete(
    final IndexBufferType id)
    throws JCGLException
  {
    // Nothing.
  }

  @Override public void indexBufferUpdate(
    final IndexBufferUpdateUnmappedType data)
    throws JCGLException
  {
    // Nothing.
  }

  private int nextName()
  {
    final int r = this.names;
    this.names = this.names + 1;
    return r;
  }
}
