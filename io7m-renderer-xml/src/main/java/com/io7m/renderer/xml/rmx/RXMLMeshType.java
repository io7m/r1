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

package com.io7m.renderer.xml.rmx;

import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable final class RXMLMeshType
{
  private final @Nonnull EnumSet<RXMLMeshAttribute> attributes;

  RXMLMeshType(
    final @Nonnull EnumSet<RXMLMeshAttribute> attributes)
  {
    this.attributes = attributes;
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
    if (!this.attributes.equals(other.attributes)) {
      return false;
    }
    return true;
  }

  public boolean hasBitangent()
  {
    return this.attributes.contains(RXMLMeshAttribute.BITANGENT_3F);
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result =
      (prime * result)
        + ((this.attributes == null) ? 0 : this.attributes.hashCode());
    return result;
  }

  public boolean hasNormal()
  {
    return this.attributes.contains(RXMLMeshAttribute.NORMAL_3F);
  }

  public boolean hasTangent()
  {
    return this.attributes.contains(RXMLMeshAttribute.TANGENT_3F);
  }

  public boolean hasUV()
  {
    return this.attributes.contains(RXMLMeshAttribute.UV_2F);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[RXMLMeshType ");
    builder.append(this.attributes);
    builder.append("]");
    return builder.toString();
  }
}
