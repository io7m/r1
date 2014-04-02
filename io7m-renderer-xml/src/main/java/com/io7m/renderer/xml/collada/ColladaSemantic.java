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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

public enum ColladaSemantic
{
  SEMANTIC_BINORMAL("BINORMAL"),
  SEMANTIC_COLOR("COLOR"),
  SEMANTIC_CONTINUITY("CONTINUITY"),
  SEMANTIC_IMAGE("IMAGE"),
  SEMANTIC_INPUT("INPUT"),
  SEMANTIC_IN_TANGENT("IN_TANGENT"),
  SEMANTIC_INTERPOLATION("INTERPOLATION"),
  SEMANTIC_INV_BIND_MATRIX("INV_BIND_MATRIX"),
  SEMANTIC_JOINT("JOINT"),
  SEMANTIC_LINEAR_STEPS("LINEAR_STEPS"),
  SEMANTIC_MORPH_TARGET("MORPH_TARGET"),
  SEMANTIC_MORPH_WEIGHT("MORPH_WEIGHT"),
  SEMANTIC_NORMAL("NORMAL"),
  SEMANTIC_OUTPUT("OUTPUT"),
  SEMANTIC_OUT_TANGENT("OUT_TANGENT"),
  SEMANTIC_POSITION("POSITION"),
  SEMANTIC_TANGENT("TANGENT"),
  SEMANTIC_TEXBINORMAL("TEXBINORMAL"),
  SEMANTIC_TEXCOORD("TEXCOORD"),
  SEMANTIC_TEXTANGENT("TEXTANGENT"),
  SEMANTIC_UV("UV"),
  SEMANTIC_VERTEX("VERTEX"),
  SEMANTIC_WEIGHT("WEIGHT");

  public static @Nonnull ColladaSemantic fromName(
    final @Nonnull String name)
    throws ConstraintError
  {
    Constraints.constrainNotNull(name, "Name");
    final ColladaSemantic c = ColladaSemantic.valueOf("SEMANTIC_" + name);
    Constraints.constrainNotNull(c, "Resulting value");
    return c;
  }

  private final @Nonnull String name;

  private ColladaSemantic(
    final @Nonnull String in_name)
  {
    this.name = in_name;
  }

  public @Nonnull String getName()
  {
    return this.name;
  }
}
