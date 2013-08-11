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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.ArrayBufferAttributeDescriptor;
import com.io7m.jcanephora.JCGLScalarType;

/**
 * Standard names and types for attributes in vertex data.
 */

public class KMeshAttributes
{
  static {
    try {
      ATTRIBUTE_POSITION =
        new ArrayBufferAttributeDescriptor(
          "position",
          JCGLScalarType.TYPE_FLOAT,
          3);

      ATTRIBUTE_NORMAL =
        new ArrayBufferAttributeDescriptor(
          "normal",
          JCGLScalarType.TYPE_FLOAT,
          3);

      ATTRIBUTE_TANGENT =
        new ArrayBufferAttributeDescriptor(
          "tangent",
          JCGLScalarType.TYPE_FLOAT,
          3);

      ATTRIBUTE_UV =
        new ArrayBufferAttributeDescriptor("uv", JCGLScalarType.TYPE_FLOAT, 2);

      ATTRIBUTE_COLOUR =
        new ArrayBufferAttributeDescriptor(
          "colour",
          JCGLScalarType.TYPE_FLOAT,
          4);

    } catch (final ConstraintError x) {
      throw new UnreachableCodeException();
    }
  }

  public static final @Nonnull ArrayBufferAttributeDescriptor ATTRIBUTE_POSITION;
  public static final @Nonnull ArrayBufferAttributeDescriptor ATTRIBUTE_NORMAL;
  public static final @Nonnull ArrayBufferAttributeDescriptor ATTRIBUTE_TANGENT;
  public static final @Nonnull ArrayBufferAttributeDescriptor ATTRIBUTE_UV;
  public static final @Nonnull ArrayBufferAttributeDescriptor ATTRIBUTE_COLOUR;
}
