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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KFramebufferDepthVariance;
import com.io7m.r1.kernel.KFramebufferDepthVarianceType;
import com.io7m.r1.kernel.types.KDepthPrecision;
import com.io7m.r1.kernel.types.KDepthVariancePrecision;
import com.io7m.r1.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.TestContext;
import com.io7m.r1.tests.TestContract;

public abstract class KFramebufferDepthVarianceContract extends TestContract
{
  @Override @Before public final void checkSupport()
  {
    Assume.assumeTrue(this.isGLSupported());
  }

  @Test public void testCreateFramebuffer_16_16f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_16,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }

  @Test public void testCreateFramebuffer_24_16f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_24,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }

  @Test public void testCreateFramebuffer_32f_16f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_32F,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_16F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }

  @Test public void testCreateFramebuffer_16_32f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_16,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_32F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }

  @Test public void testCreateFramebuffer_24_32f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_24,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_32F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }

  @Test public void testCreateFramebuffer_32f_32f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KFramebufferDepthVarianceDescription description =
      KFramebufferDepthVarianceDescription.newDescription(
        RFakeGL.SCREEN_AREA,
        TextureFilterMagnification.TEXTURE_FILTER_LINEAR,
        TextureFilterMinification.TEXTURE_FILTER_LINEAR,
        KDepthPrecision.DEPTH_PRECISION_32F,
        KDepthVariancePrecision.DEPTH_VARIANCE_PRECISION_32F);

    final KFramebufferDepthVarianceType f =
      KFramebufferDepthVariance.newDepthVarianceFramebuffer(gi, description);
    Assert.assertEquals(RFakeGL.SCREEN_AREA, f.kFramebufferGetArea());

    f.delete(gi);
    Assert.assertTrue(f.resourceIsDeleted());
  }
}
