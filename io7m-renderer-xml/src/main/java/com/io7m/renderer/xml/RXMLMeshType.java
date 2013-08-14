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

package com.io7m.renderer.xml;

import javax.annotation.concurrent.Immutable;

@Immutable final class RXMLMeshType
{
  private final boolean normal;
  private final boolean tangent;
  private final boolean uv;

  RXMLMeshType(
    final boolean normal,
    final boolean tangent,
    final boolean uv)
  {
    this.normal = normal;
    this.tangent = tangent;
    this.uv = uv;
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
    final RXMLMeshType other = (RXMLMeshType) obj;
    if (this.normal != other.normal) {
      return false;
    }
    if (this.tangent != other.tangent) {
      return false;
    }
    if (this.uv != other.uv) {
      return false;
    }
    return true;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (this.normal ? 1231 : 1237);
    result = (prime * result) + (this.tangent ? 1231 : 1237);
    result = (prime * result) + (this.uv ? 1231 : 1237);
    return result;
  }

  public boolean hasNormal()
  {
    return this.normal;
  }

  public boolean hasTangent()
  {
    return this.tangent;
  }

  public boolean hasUV()
  {
    return this.uv;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[RXMLMeshType [normal ");
    builder.append(this.normal);
    builder.append("] [tangent ");
    builder.append(this.tangent);
    builder.append("] [uv ");
    builder.append(this.uv);
    builder.append("]");
    return builder.toString();
  }
}
