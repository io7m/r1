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

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.KGeometryBuffer;
import com.io7m.r1.kernel.KGeometryBufferType;
import com.io7m.r1.kernel.types.KGeometryBufferDescription;
import com.io7m.r1.kernel.types.KGeometryBufferDescriptionBuilderType;
import com.io7m.r1.kernel.types.KNormalPrecision;
import com.io7m.r1.tests.RFakeGL;
import com.io7m.r1.tests.TestContext;
import com.io7m.r1.tests.TestContract;

public abstract class KGeometryBufferContract extends TestContract
{
  @Override @Before public final void checkSupport()
  {
    Assume.assumeTrue(this.isGLSupported());
  }

  @Test public void testCreateGeometryBuffer_16f()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KGeometryBufferDescriptionBuilderType b =
      KGeometryBufferDescription.newBuilder(RFakeGL.SCREEN_AREA);
    b.setNormalPrecision(KNormalPrecision.NORMAL_PRECISION_16F);

    final KGeometryBufferDescription description = b.build();

    final KGeometryBufferType f =
      KGeometryBuffer.newGeometryBuffer(gi, description);

    f.geomDelete(gi);
  }

  @Test public void testCreateGeometryBuffer_8()
    throws RException
  {
    final TestContext tc = this.newTestContext();
    final JCGLImplementationType gi = tc.getGLImplementation();

    final KGeometryBufferDescriptionBuilderType b =
      KGeometryBufferDescription.newBuilder(RFakeGL.SCREEN_AREA);
    b.setNormalPrecision(KNormalPrecision.NORMAL_PRECISION_8);

    final KGeometryBufferDescription description = b.build();

    final KGeometryBufferType f =
      KGeometryBuffer.newGeometryBuffer(gi, description);

    f.geomDelete(gi);
  }
}
