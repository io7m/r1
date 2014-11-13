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

import com.io7m.jcanephora.Texture2DStaticUsableType;
import com.io7m.r1.types.RException;

/**
 * The type of mutable builders for spherical lights with basic shadows.
 */

public interface KLightSpherePseudoWithShadowBasicBuilderType extends
  KLightSphereBuilderType,
  KLightSpherePseudoWithShadowBuilderType
{
  /**
   * <p>
   * Set all values in the builder to those in the given existing light.
   * </p>
   *
   * @param s
   *          The light
   */

  void copyFromPseudo(
    final KLightSpherePseudoType s);

  /**
   * Construct a light.
   *
   * @param context
   *          The transform context.
   * @param texture
   *          The projected texture.
   * @return A new light
   * @throws RException
   *           On errors
   */

  KLightSpherePseudoWithShadowBasic build(
    final KTransformContext context,
    final Texture2DStaticUsableType texture)
    throws RException;

  /**
   * Set the shadow.
   *
   * @param s
   *          The shadow.
   */

  void setShadow(
    KShadowMappedBasic s);
}
