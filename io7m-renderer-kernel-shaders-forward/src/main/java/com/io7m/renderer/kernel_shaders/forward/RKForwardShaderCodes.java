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

package com.io7m.renderer.kernel_shaders.forward;

import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialOpaqueRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRefractive;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;

public final class RKForwardShaderCodes
{
  public static String fromLitOpaqueRegular(
    final KLightType l,
    final KMaterialOpaqueRegular m)
  {
    final String lcode = l.lightGetCode();
    final String mcode = m.materialLitGetCode();
    final String scode = String.format("Fwd_%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromLitOpaqueRegularCase(
    final RKLitCase<KMaterialOpaqueRegular> c)
  {
    return RKForwardShaderCodes.fromLitOpaqueRegular(
      c.getLight(),
      c.getMaterial());
  }

  public static String fromLitTranslucentRegular(
    final RKLitCase<KMaterialTranslucentRegular> c)
  {
    final String lcode = c.getLight().lightGetCode();
    final String mcode = c.getMaterial().materialLitGetCode();
    final String scode = String.format("Fwd_%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromLitTranslucentSpecularOnly(
    final RKLitCase<KMaterialTranslucentSpecularOnly> c)
  {
    final String lcode = c.getLight().lightGetCode();
    final String mcode = c.getMaterial().materialLitGetCode();
    final String scode = String.format("Fwd_%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromUnlitOpaqueRegular(
    final KMaterialOpaqueRegular m)
  {
    final String code = String.format("Fwd_%s", m.materialUnlitGetCode());
    assert code != null;
    return code;
  }

  public static String fromUnlitOpaqueRegular(
    final RKLitCase<KMaterialOpaqueRegular> c)
  {
    final String lcode = c.getLight().lightGetCode();
    final String mcode = c.getMaterial().materialLitGetCode();
    final String scode = String.format("Fwd_%s_%s", lcode, mcode);
    assert scode != null;
    return scode;
  }

  public static String fromUnlitTranslucentRefractive(
    final KMaterialTranslucentRefractive m)
  {
    final String code = String.format("Fwd_%s", m.materialUnlitGetCode());
    assert code != null;
    return code;
  }

  public static String fromUnlitTranslucentRegular(
    final KMaterialTranslucentRegular m)
  {
    final String code = String.format("Fwd_%s", m.materialUnlitGetCode());
    assert code != null;
    return code;
  }
}
