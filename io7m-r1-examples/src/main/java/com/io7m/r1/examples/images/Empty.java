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

package com.io7m.r1.examples.images;

import com.io7m.jnull.NullCheck;
import com.io7m.r1.examples.ExampleImageBuilderType;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleVisitorType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KFramebufferDeferredType;

/**
 * A demonstration with nothing onscreen.
 */

public final class Empty implements ExampleImageType
{
  /**
   * Construct the example.
   */

  public Empty()
  {

  }

  @Override public <A> A exampleAccept(
    final ExampleVisitorType<A> v)
  {
    return v.image(this);
  }

  @Override public String exampleGetName()
  {
    return NullCheck.notNull(this.getClass().getSimpleName());
  }

  @Override public void exampleImage(
    final ExampleImageBuilderType image,
    final KFramebufferDeferredType fb)
    throws RException
  {
    // Nothing
  }
}
