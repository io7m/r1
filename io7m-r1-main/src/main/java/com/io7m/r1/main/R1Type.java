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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jfunctional.Unit;
import com.io7m.r1.kernel.KCopyParameters;
import com.io7m.r1.kernel.KFXAAParameters;
import com.io7m.r1.kernel.KFogYParameters;
import com.io7m.r1.kernel.KFogZParameters;
import com.io7m.r1.kernel.KFramebufferDepthVarianceCacheType;
import com.io7m.r1.kernel.KImageFilterDeferredType;
import com.io7m.r1.kernel.KImageFilterDepthVarianceType;
import com.io7m.r1.kernel.KImageFilterRGBAType;
import com.io7m.r1.kernel.KImageSourceDepthVarianceType;
import com.io7m.r1.kernel.KImageSourceRGBAType;
import com.io7m.r1.kernel.KRendererDeferredType;
import com.io7m.r1.kernel.KTextureMixParameters;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KGlowParameters;

/**
 * The type of friendly kernel interfaces.
 */

public interface R1Type
{
  /**
   * @see com.io7m.r1.kernel.KFramebufferDepthVarianceCache
   * @return The depth variance cache
   */

  KFramebufferDepthVarianceCacheType getDepthVarianceCache();

  /**
   * @see com.io7m.r1.kernel.KImageFilterBlurDepthVariance
   * @return The depth-variance blur filter.
   */

  KImageFilterDepthVarianceType<KBlurParameters> getFilterBlurDepthVariance();

  /**
   * @see com.io7m.r1.kernel.KImageFilterBlurRGBA
   * @return The RGBA blur filter.
   */

  KImageFilterRGBAType<KBlurParameters> getFilterBlurRGBA();

  /**
   * @see com.io7m.r1.kernel.KImageFilterCopyRGBA
   * @return The RGBA copy filter
   */

  KImageFilterRGBAType<KCopyParameters> getFilterCopyRGBA();

  /**
   * @see com.io7m.r1.kernel.KImageFilterEmission
   * @return The emission filter.
   */

  KImageFilterDeferredType<Unit> getFilterEmission();

  /**
   * @see com.io7m.r1.kernel.KImageFilterEmissionGlow
   * @return The emission and glow filter.
   */

  KImageFilterDeferredType<KGlowParameters> getFilterEmissionGlow();

  /**
   * @see com.io7m.r1.kernel.KImageFilterFogY
   * @return The Y fog filter.
   */

  KImageFilterDeferredType<KFogYParameters> getFilterFogY();

  /**
   * @see com.io7m.r1.kernel.KImageFilterFogZ
   * @return The Z fog filter.
   */

  KImageFilterDeferredType<KFogZParameters> getFilterFogZ();

  /**
   * @see com.io7m.r1.kernel.KImageFilterFXAA
   * @return The FXAA filter.
   */

  KImageFilterRGBAType<KFXAAParameters> getFilterFXAA();

  /**
   * @return The underlying {@link JCGLImplementationType}
   */

  JCGLImplementationType getJCGLImplementation();

  /**
   * @return The main deferred renderer.
   */

  KRendererDeferredType getRendererDeferred();

  /**
   * @see com.io7m.r1.kernel.KImageSourceDepthVarianceMix
   * @return The texture mix source
   */

    KImageSourceDepthVarianceType<KTextureMixParameters>
    getSourceDepthVarianceTextureMix();

  /**
   * @see com.io7m.r1.kernel.KImageSourceRGBAMix
   * @return The texture mix source
   */

  KImageSourceRGBAType<KTextureMixParameters> getSourceRGBATextureMix();
}
