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

import javax.annotation.concurrent.Immutable;

/**
 * A polygon specification, indexing into the arrays in {@link ColladaMesh}.
 */

@Immutable final class ColladaPoly
{
  private final int position_0;
  private final int position_1;
  private final int position_2;
  private final int normal_0;
  private final int normal_1;
  private final int normal_2;
  private final int uv_0;
  private final int uv_1;
  private final int uv_2;

  ColladaPoly(
    final int position_0,
    final int position_1,
    final int position_2,
    final int normal_0,
    final int normal_1,
    final int normal_2,
    final int uv_0,
    final int uv_1,
    final int uv_2)
  {
    this.position_0 = position_0;
    this.position_1 = position_1;
    this.position_2 = position_2;
    this.normal_0 = normal_0;
    this.normal_1 = normal_1;
    this.normal_2 = normal_2;
    this.uv_0 = uv_0;
    this.uv_1 = uv_1;
    this.uv_2 = uv_2;
  }

  public int getNormal0()
  {
    return this.normal_0;
  }

  public int getNormal1()
  {
    return this.normal_1;
  }

  public int getNormal2()
  {
    return this.normal_2;
  }

  public int getPosition0()
  {
    return this.position_0;
  }

  public int getPosition1()
  {
    return this.position_1;
  }

  public int getPosition2()
  {
    return this.position_2;
  }

  public int getUV0()
  {
    return this.uv_0;
  }

  public int getUV1()
  {
    return this.uv_1;
  }

  public int getUV2()
  {
    return this.uv_2;
  }
}
