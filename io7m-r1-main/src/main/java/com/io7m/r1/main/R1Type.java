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

package com.io7m.r1.main;

import com.io7m.jfunctional.Unit;
import com.io7m.r1.kernel.KPostprocessorDeferredType;
import com.io7m.r1.kernel.KPostprocessorRGBAType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.types.KGlowParameters;

/**
 * The type of friendly kernel interfaces.
 */

public interface R1Type
{
  /**
   * @see com.io7m.r1.kernel.KPostprocessorEmission
   * @return The emission postprocessor.
   */

  KPostprocessorDeferredType<Unit> getPostprocessorEmission();

  /**
   * @see com.io7m.r1.kernel.KPostprocessorEmissionGlow
   * @return The emission and glow postprocessor.
   */

  KPostprocessorDeferredType<KGlowParameters> getPostprocessorEmissionGlow();

  /**
   * @see com.io7m.r1.kernel.KPostprocessorFXAA
   * @return The FXAA postprocessor.
   */

  KPostprocessorRGBAType<Unit> getPostprocessorFXAA();

  /**
   * @return The main deferred renderer.
   */

  KRendererDeferredType getRendererDeferred();
}
