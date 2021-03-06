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

package com.io7m.r1.tests.kernel;

import org.junit.Test;

import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.kernel.KFramebufferDepthVariance;
import com.io7m.r1.kernel.KFramebufferDepthVarianceType;
import com.io7m.r1.kernel.KImageFilterDepthVarianceType;
import com.io7m.r1.kernel.types.KBlurParameters;
import com.io7m.r1.kernel.types.KBlurParametersBuilderType;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KDepthVariancePrecision;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.TestShaderCaches;

@SuppressWarnings("static-method") public final class KImageFilterDepthVarianceBlurTest
{
  @Test public void testRenderer()
    throws Exception
  {
    final LogUsableType log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
    final OptionType<JCGLSoftRestrictionsType> none = Option.none();
    final JCGLImplementationType gi =
      RFakeGL.newFakeGL30WithLog(log, RFakeShaderControllers.newNull(), none);

    final R1BuilderType r1b = R1.newBuilder(gi, log);
    r1b.setShaderCacheSet(TestShaderCaches.newCachesFromArchives(gi, log));
    final R1Type r1 = r1b.build();

    final KFramebufferDepthVarianceDescription fbd =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_16,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);

    final KFramebufferDepthVarianceType fb =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, fbd);

    final KImageFilterDepthVarianceType<KBlurParameters> blur =
      r1.getFilterBlurDepthVariance();

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(0.0f);
      bb.setPasses(0);
      bb.setScale(1.0f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(1.0f);
      bb.setPasses(0);
      bb.setScale(1.0f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(0.0f);
      bb.setPasses(1);
      bb.setScale(1.0f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(0.0f);
      bb.setPasses(1);
      bb.setScale(0.5f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(1.0f);
      bb.setPasses(1);
      bb.setScale(0.5f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(1.0f);
      bb.setPasses(2);
      bb.setScale(0.5f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }

    {
      final KBlurParametersBuilderType bb = KBlurParameters.newBuilder();
      bb.setBlurSize(1.0f);
      bb.setPasses(2);
      bb.setScale(1.0f);
      final KBlurParameters config = bb.build();
      blur.filterEvaluateDepthVariance(config, fb, fb);
    }
  }
}
