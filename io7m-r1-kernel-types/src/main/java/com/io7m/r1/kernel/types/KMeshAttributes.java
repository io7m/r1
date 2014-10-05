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

package com.io7m.r1.kernel.types;

import com.io7m.jcanephora.ArrayAttributeDescriptor;
import com.io7m.jcanephora.JCGLScalarType;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Standard names and types for attributes in vertex data.
 */

@EqualityReference public final class KMeshAttributes
{
  /**
   * The name and type of per-vertex color data attributes
   */

  public static final ArrayAttributeDescriptor ATTRIBUTE_COLOR;

  /**
   * The name and type of per-vertex normal vector attributes
   */

  public static final ArrayAttributeDescriptor ATTRIBUTE_NORMAL;

  /**
   * The name and type of per-vertex object-space position attributes
   */

  public static final ArrayAttributeDescriptor ATTRIBUTE_POSITION;

  /**
   * The name and type of per-vertex tangent vector attributes, where the
   * fourth component contains <code>1.0</code> if the original normal,
   * tangent, and bitangent vectors formed a right-handed basis, and
   * <code>-1.0</code> otherwise. The bitangent vector can therefore be
   * reconstructed by calculating <code>cross (N, T.xyz) * T.w</code> (where
   * <code>N</code> is the original normal vector, and <code>T</code> is the
   * four-component tangent vector).
   */

  public static final ArrayAttributeDescriptor ATTRIBUTE_TANGENT4;

  /**
   * The name and type of per-vertex UV coordinates
   */

  public static final ArrayAttributeDescriptor ATTRIBUTE_UV;

  static {
    ATTRIBUTE_COLOR =
      ArrayAttributeDescriptor.newAttribute(
        "color",
        JCGLScalarType.TYPE_FLOAT,
        4);

    ATTRIBUTE_NORMAL =
      ArrayAttributeDescriptor.newAttribute(
        "normal",
        JCGLScalarType.TYPE_FLOAT,
        3);

    ATTRIBUTE_POSITION =
      ArrayAttributeDescriptor.newAttribute(
        "position",
        JCGLScalarType.TYPE_FLOAT,
        3);

    ATTRIBUTE_TANGENT4 =
      ArrayAttributeDescriptor.newAttribute(
        "tangent4",
        JCGLScalarType.TYPE_FLOAT,
        4);

    ATTRIBUTE_UV =
      ArrayAttributeDescriptor.newAttribute(
        "uv",
        JCGLScalarType.TYPE_FLOAT,
        2);
  }

  private KMeshAttributes()
  {
    throw new UnreachableCodeException();
  }
}
