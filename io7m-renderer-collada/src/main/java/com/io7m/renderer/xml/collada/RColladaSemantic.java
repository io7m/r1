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

package com.io7m.renderer.xml.collada;

import com.io7m.jnull.NullCheck;

/**
 * <p>
 * A COLLADA "semantic". This is essentially used to indicate what a given
 * array of data is supposed to mean.
 * </p>
 * <p>
 * Consult the COLLADA specification for the meanings.
 * </p>
 */

public enum RColladaSemantic
{
  // CHECKSTYLE_JAVADOC:OFF
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
  // CHECKSTYLE_JAVADOC:ON

  /**
   * Construct a semantic.
   * 
   * @param name
   *          The name.
   * @return A semantic.
   */

  public static RColladaSemantic fromName(
    final String name)
  {
    NullCheck.notNull(name, "Name");
    final RColladaSemantic c = RColladaSemantic.valueOf("SEMANTIC_" + name);
    NullCheck.notNull(c, "Resulting value");
    return c;
  }

  private final String name;

  private RColladaSemantic(
    final String in_name)
  {
    this.name = in_name;
  }

  /**
   * @return The name of the semantic.
   */

  public String getName()
  {
    return this.name;
  }
}
