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

import java.util.ArrayList;
import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;

@EqualityReference public final class RKFLitTranslucentRegularCases
{
  private static
    List<RKFLitCase<KMaterialTranslucentRegular>>
    makeTranslucentListCases(
      final RKFLightCases in_light_cases,
      final RKFMaterialCases in_material_cases)
  {
    final List<RKFLitCase<KMaterialTranslucentRegular>> cases =
      new ArrayList<RKFLitCase<KMaterialTranslucentRegular>>();

    for (final KLightType p : in_light_cases.getCases()) {
      assert p != null;

      for (final KMaterialTranslucentRegular m : in_material_cases
        .getCasesLitTranslucentRegular()) {
        assert m != null;

        final RKFLitCase<KMaterialTranslucentRegular> lc =
          new RKFLitCase<KMaterialTranslucentRegular>(p, m);
        cases.add(lc);
      }
    }

    return cases;
  }

  private final List<RKFLitCase<KMaterialTranslucentRegular>> cases;
  private final RKFLightCases                                 light_cases;
  private final RKFMaterialCases                              material_cases;

  public RKFLitTranslucentRegularCases()
  {
    this.light_cases = new RKFLightCases();
    this.material_cases = new RKFMaterialCases();

    this.cases =
      RKFLitTranslucentRegularCases.makeTranslucentListCases(
        this.light_cases,
        this.material_cases);
  }

  public List<RKFLitCase<KMaterialTranslucentRegular>> getCases()
  {
    return this.cases;
  }
}
