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

import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.jtensors.parameterized.PMatrixM4x4F;
import com.io7m.jtensors.parameterized.PVectorI3F;
import com.io7m.r1.kernel.KFogProgression;
import com.io7m.r1.kernel.KFogYParameters;
import com.io7m.r1.kernel.KFogYParametersBuilderType;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageFilterDeferredType;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescriptionBuilderType;
import com.io7m.r1.kernel.types.KProjectionFOV;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.TestShaderCaches;
import com.io7m.r1.types.RSpaceClipType;
import com.io7m.r1.types.RSpaceEyeType;
import com.io7m.r1.types.RSpaceRGBType;
import com.io7m.r1.types.RSpaceWorldType;

@SuppressWarnings("static-method") public final class KImageFilterFogYTest
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

    final KFramebufferDeferredDescriptionBuilderType fbb =
      KFramebufferDeferredDescription.newBuilder(RFakeGL.SCREEN_AREA);
    final KFramebufferDeferredDescription fb_desc = fbb.build();
    final KFramebufferDeferredType fb =
      KFramebufferDeferred.newFramebuffer(gi, fb_desc);

    final KImageFilterDeferredType<KFogYParameters> f = r1.getFilterFogY();

    for (final KFogProgression prog : KFogProgression.values()) {
      assert prog != null;

      final KProjectionFOV in_projection =
        KProjectionFOV.newProjection(
          new PMatrixM4x4F<RSpaceEyeType, RSpaceClipType>(),
          (float) Math.toRadians(90.0f),
          1.0f,
          1.0f,
          100.0f);
      final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> in_view =
        PMatrixI4x4F.identity();

      final KFogYParametersBuilderType b =
        KFogYParameters.newBuilder(in_view, in_projection);
      b.setColor(new PVectorI3F<RSpaceRGBType>(0.33f, 0.33f, 0.33f));
      b.setUpperY(1.0f);
      b.setLowerY(0.0f);
      b.setProgression(prog);
      b.setProjection(in_projection);
      f.filterEvaluateDeferred(b.build(), fb, fb);
    }
  }

  @Test public void testParameters()
  {
    QuickCheck.forAllVerbose(
      new KFogYParametersGenerator(),
      new AbstractCharacteristic<KFogYParameters>() {
        @Override protected void doSpecify(
          final KFogYParameters p)
          throws Throwable
        {
          assert p != null;
          final KFogYParametersBuilderType b =
            KFogYParameters.newBuilder(p.getView(), p.getProjection());
          b.setColor(p.getColor());
          b.setUpperY(p.getUpperY());
          b.setLowerY(p.getLowerY());
          b.setProgression(p.getProgression());

          final KFogYParameters q = b.build();
          Assert.assertEquals(p, q);
          Assert.assertEquals(p.hashCode(), q.hashCode());
          Assert.assertEquals(p.toString(), q.toString());
        }
      });
  }
}
