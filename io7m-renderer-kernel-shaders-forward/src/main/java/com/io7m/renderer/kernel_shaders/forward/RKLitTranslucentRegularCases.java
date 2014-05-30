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

import java.util.ArrayList;
import java.util.List;

import com.io7m.jfunctional.Pair;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialTranslucentRegular;
import com.io7m.renderer.kernel_shaders.core.FakeImmutableCapabilities;

public final class RKLitTranslucentRegularCases
{
  private static
    List<RKLitCase<KMaterialTranslucentRegular>>
    makeTranslucentListCases(
      final RKLightCases in_light_cases,
      final RKMaterialCases in_material_cases)
  {
    final List<RKLitCase<KMaterialTranslucentRegular>> cases =
      new ArrayList<RKLitCase<KMaterialTranslucentRegular>>();

    for (final Pair<KLightType, FakeImmutableCapabilities> p : in_light_cases
      .getCases()) {
      assert p != null;

      for (final KMaterialTranslucentRegular m : in_material_cases
        .getCasesLitTranslucentRegular()) {
        assert m != null;

        final RKLitCase<KMaterialTranslucentRegular> lc =
          new RKLitCase<KMaterialTranslucentRegular>(
            p.getLeft(),
            m,
            p.getRight());
        cases.add(lc);
      }
    }

    return cases;
  }

  private final RKLightCases                               light_cases;
  private final RKMaterialCases                            material_cases;
  private final List<RKLitCase<KMaterialTranslucentRegular>> cases;

  public RKLitTranslucentRegularCases()
  {
    this.light_cases = new RKLightCases();
    this.material_cases = new RKMaterialCases();

    this.cases =
      RKLitTranslucentRegularCases.makeTranslucentListCases(
        this.light_cases,
        this.material_cases);
  }

  public List<RKLitCase<KMaterialTranslucentRegular>> getCases()
  {
    return this.cases;
  }
}
