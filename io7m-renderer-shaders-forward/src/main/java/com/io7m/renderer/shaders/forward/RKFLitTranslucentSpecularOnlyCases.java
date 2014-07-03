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
import com.io7m.jfunctional.Pair;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentSpecularOnly;
import com.io7m.renderer.shaders.core.FakeImmutableCapabilities;

@EqualityReference public final class RKFLitTranslucentSpecularOnlyCases
{
  private static
    List<RKFLitCase<KMaterialTranslucentSpecularOnly>>
    makeTranslucentListCases(
      final RKFLightCases in_light_cases,
      final RKFMaterialCases in_material_cases)
  {
    final List<RKFLitCase<KMaterialTranslucentSpecularOnly>> cases =
      new ArrayList<RKFLitCase<KMaterialTranslucentSpecularOnly>>();

    for (final Pair<KLightType, FakeImmutableCapabilities> p : in_light_cases
      .getCases()) {
      assert p != null;

      for (final KMaterialTranslucentSpecularOnly m : in_material_cases
        .getCasesLitTranslucentSpecularOnly()) {
        assert m != null;

        final RKFLitCase<KMaterialTranslucentSpecularOnly> lc =
          new RKFLitCase<KMaterialTranslucentSpecularOnly>(
            p.getLeft(),
            m,
            p.getRight());
        cases.add(lc);
      }
    }

    return cases;
  }

  private final List<RKFLitCase<KMaterialTranslucentSpecularOnly>> cases;
  private final RKFLightCases                                      light_cases;
  private final RKFMaterialCases                                   material_cases;

  public RKFLitTranslucentSpecularOnlyCases()
  {
    this.light_cases = new RKFLightCases();
    this.material_cases = new RKFMaterialCases();

    this.cases =
      RKFLitTranslucentSpecularOnlyCases.makeTranslucentListCases(
        this.light_cases,
        this.material_cases);
  }

  public List<RKFLitCase<KMaterialTranslucentSpecularOnly>> getCases()
  {
    return this.cases;
  }
}
