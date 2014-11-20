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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.r1.kernel.KCopyParameters;
import com.io7m.r1.kernel.KFramebufferDeferred;
import com.io7m.r1.kernel.KFramebufferDeferredType;
import com.io7m.r1.kernel.KImageFilterRGBAType;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescription;
import com.io7m.r1.kernel.types.KFramebufferDeferredDescriptionBuilderType;
import com.io7m.r1.main.R1;
import com.io7m.r1.main.R1BuilderType;
import com.io7m.r1.main.R1Type;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.RFakeShaderControllers;
import com.io7m.r1.tests.TestShaderCaches;

@SuppressWarnings("static-method") public final class KImageFilterRGBACopyTest
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

    final KImageFilterRGBAType<KCopyParameters> copy = r1.getFilterCopyRGBA();

    {
      final KCopyParameters config =
        new KCopyParameters(RFakeGL.SCREEN_AREA, RFakeGL.SCREEN_AREA);
      copy.filterEvaluateRGBA(config, fb, fb);
    }

  }
}
