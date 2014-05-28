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

package com.io7m.renderer.kernel.types;

import com.io7m.jfunctional.OptionType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RSpaceRGBType;
import com.io7m.renderer.types.RVectorI3F;

/**
 * The type of lights.
 */

public interface KLightType extends KTexturesRequiredType
{
  /**
   * Be visited by the given generic visitor.
   * 
   * @param v
   *          The visitor
   * @return The value returned by the visitor
   * 
   * @throws RException
   *           Iff the visitor raises {@link RException}
   * @throws E
   *           Iff the visitor raises <code>E</code
   * 
   * @param <A>
   *          The return type of the visitor
   * @param <E>
   *          The type of exceptions raised by the visitor
   * @param <V>
   *          A specific visitor subtype
   */

  <A, E extends Throwable, V extends KLightVisitorType<A, E>> A lightAccept(
    final V v)
    throws RException,
      E;

  /**
   * @return The code for the light.
   */

  String lightGetCode();

  /**
   * @return The color of the light
   */

  RVectorI3F<RSpaceRGBType> lightGetColor();

  /**
   * @return The intensity of the light
   */

  float lightGetIntensity();

  /**
   * @return <code>Some(s)</code> if the light has a shadow <code>s</code>,
   *         else <code>None</code>
   */

  OptionType<KShadowType> lightGetShadow();

  /**
   * @return <code>true</code> iff the light has a shadow.
   */

  boolean lightHasShadow();
}
