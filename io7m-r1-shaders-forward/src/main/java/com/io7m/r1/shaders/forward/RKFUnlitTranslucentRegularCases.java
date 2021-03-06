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

package com.io7m.r1.shaders.forward;

import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.r1.kernel.types.KMaterialTranslucentRegular;

@EqualityReference public final class RKFUnlitTranslucentRegularCases
{
  private final List<KMaterialTranslucentRegular> cases;
  private final RKFMaterialCases                  material_cases;

  public RKFUnlitTranslucentRegularCases()
  {
    this.material_cases = new RKFMaterialCases();
    this.cases = this.material_cases.getCasesUnlitTranslucentRegular();
  }

  public List<KMaterialTranslucentRegular> getCases()
  {
    return this.cases;
  }
}
