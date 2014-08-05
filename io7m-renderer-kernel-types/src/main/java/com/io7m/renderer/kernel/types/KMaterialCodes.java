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

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;

@EqualityReference final class KMaterialCodes
{
  public static String makeCodeDeferredGeometryRegular(
    final KMaterialAlphaType in_alpha,
    final KMaterialDepthType in_depth,
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    final StringBuilder b = new StringBuilder();
    b.append(in_alpha.codeGet());
    b.append("_");
    b.append(in_depth.codeGet());
    b.append("_");

    KMaterialCodes.makeRegularLitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeOpaqueRegularLitWithDepth(
    final KMaterialDepthType in_depth,
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    final StringBuilder b = new StringBuilder();
    b.append("O_");
    b.append(in_depth.codeGet());
    b.append("_");

    KMaterialCodes.makeRegularLitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeOpaqueRegularLitWithoutDepth(
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    final StringBuilder b = new StringBuilder();
    b.append("O_");
    KMaterialCodes.makeRegularLitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeOpaqueRegularUnlit(
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal)
  {
    final StringBuilder b = new StringBuilder();
    b.append("O_");

    KMaterialCodes.makeRegularUnlitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  private static void makeRegularLitCode(
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular,
    final StringBuilder b)
  {
    b.append(in_albedo.codeGet());

    {
      final String c = in_emissive.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_environment.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_normal.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_specular.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }
  }

  private static void makeRegularUnlitCode(
    final KMaterialAlbedoType in_albedo,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final StringBuilder b)
  {
    b.append(in_albedo.codeGet());

    {
      final String c = in_emissive.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_environment.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_normal.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }
  }

  public static String makeCodeTranslucentRefractiveUnlit(
    final KMaterialNormalType in_normal,
    final KMaterialRefractiveType in_refractive)
  {
    final StringBuilder b = new StringBuilder();
    b.append("Refractive_");
    b.append(in_refractive.codeGet());

    {
      final String c = in_normal.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeTranslucentRegularLit(
    final KMaterialAlbedoType in_albedo,
    final KMaterialAlphaType in_alpha,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    final StringBuilder b = new StringBuilder();
    b.append(in_alpha.codeGet());
    b.append("_");
    KMaterialCodes.makeRegularLitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      in_specular,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeTranslucentRegularUnlit(
    final KMaterialAlbedoType in_albedo,
    final KMaterialAlphaType in_alpha,
    final KMaterialEmissiveType in_emissive,
    final KMaterialEnvironmentType in_environment,
    final KMaterialNormalType in_normal)
  {
    final StringBuilder b = new StringBuilder();
    b.append(in_alpha.codeGet());
    b.append("_");
    KMaterialCodes.makeRegularUnlitCode(
      in_albedo,
      in_emissive,
      in_environment,
      in_normal,
      b);

    final String r = b.toString();
    assert r != null;
    return r;
  }

  public static String makeCodeTranslucentSpecularOnlyLit(
    final KMaterialAlphaType in_alpha,
    final KMaterialNormalType in_normal,
    final KMaterialSpecularType in_specular)
  {
    final StringBuilder b = new StringBuilder();
    b.append("SpecOnly_");
    b.append(in_alpha.codeGet());

    {
      final String c = in_normal.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    {
      final String c = in_specular.codeGet();
      if (c.isEmpty() == false) {
        b.append("_");
        b.append(c);
      }
    }

    final String r = b.toString();
    assert r != null;
    return r;
  }

  private KMaterialCodes()
  {
    throw new UnreachableCodeException();
  }
}
