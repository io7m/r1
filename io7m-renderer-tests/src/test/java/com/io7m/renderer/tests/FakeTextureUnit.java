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

import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jnull.Nullable;

public final class FakeTextureUnit implements TextureUnitType
{
  public static TextureUnitType newUnit(
    final int index)
  {
    return new FakeTextureUnit(index);
  }

  private final int index;

  private FakeTextureUnit(
    final int i)
  {
    this.index = i;
  }

  @Override public int compareTo(
    final @Nullable TextureUnitType o)
  {
    assert o != null;
    return Integer.compare(this.index, o.unitGetIndex());
  }

  @Override public int unitGetIndex()
  {
    return this.index;
  }
}
