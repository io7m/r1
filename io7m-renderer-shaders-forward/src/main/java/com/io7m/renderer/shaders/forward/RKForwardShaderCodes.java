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

package com.io7m.renderer.shaders.forward;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;

@EqualityReference public final class RKForwardShaderCodes
{
  public static String fromLitTranslucentRegular(
    final KLightType l,
    final KMaterialTranslucentRegular m)
  {
    final String lcode = l.lightGetCode();
    final String mcode = m.materialGetCode();
    final String scode = String.format("%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromLitTranslucentRegularCase(
    final RKFLitCase<KMaterialTranslucentRegular> c)
  {
    return RKForwardShaderCodes.fromLitTranslucentRegular(
      c.getLight(),
      c.getMaterial());
  }

  public static String fromLitTranslucentSpecularOnly(
    final KLightType l,
    final KMaterialTranslucentSpecularOnly m)
  {
    final String lcode = l.lightGetCode();
    final String mcode = m.materialGetCode();
    final String scode = String.format("%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromLitTranslucentSpecularOnlyCase(
    final RKFLitCase<KMaterialTranslucentSpecularOnly> c)
  {
    return RKForwardShaderCodes.fromLitTranslucentSpecularOnly(
      c.getLight(),
      c.getMaterial());
  }

  public static String fromUnlitTranslucentRefractive(
    final KMaterialTranslucentRefractive m)
  {
    return m.materialGetCode();
  }

  public static String fromUnlitTranslucentRegular(
    final KMaterialTranslucentRegular m)
  {
    return m.materialGetCode();
  }
}
