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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

@EqualityStructural public final class RXMLMeshType
{
  private final EnumSet<RXMLMeshAttribute> attributes;

  public RXMLMeshType(
    final EnumSet<RXMLMeshAttribute> in_attributes)
  {
    NullCheck.notNull(in_attributes, "Attributes");

    if (in_attributes.contains(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F)) {
      if (in_attributes.contains(RXMLMeshAttribute.TANGENT_4F)) {
        throw new IllegalArgumentException(
          "Mesh cannot contain both 3D and 4D tangents");
      }
    }

    if (in_attributes.contains(RXMLMeshAttribute.TANGENT_4F)) {
      if (in_attributes.contains(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F)) {
        throw new IllegalArgumentException(
          "Mesh cannot contain both 3D and 4D tangents");
      }
    }

    this.attributes = in_attributes;
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
    final RXMLMeshType other = (RXMLMeshType) obj;
    if (!this.attributes.equals(other.attributes)) {
      return false;
    }
    return true;
  }

  public boolean hasBitangent()
  {
    return this.attributes
      .contains(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F);
  }

  @Override public int hashCode()
  {
    return this.attributes.hashCode();
  }

  public boolean hasNormal()
  {
    return this.attributes.contains(RXMLMeshAttribute.NORMAL_3F);
  }

  public boolean hasTangent3f()
  {
    return this.attributes
      .contains(RXMLMeshAttribute.TANGENT_3F_BITANGENT_3F);
  }

  public boolean hasTangent4f()
  {
    return this.attributes.contains(RXMLMeshAttribute.TANGENT_4F);
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
    final String r = builder.toString();
    assert r != null;
    return r;
  }
}
