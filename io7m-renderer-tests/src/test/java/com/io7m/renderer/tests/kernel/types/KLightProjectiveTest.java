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

package com.io7m.renderer.tests.kernel.types;

import org.junit.Test;

import com.io7m.renderer.kernel.types.KLightProjective;
import com.io7m.renderer.kernel.types.KLightProjectiveBuilderType;
import com.io7m.renderer.tests.FakeCapabilities;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionLightMissingTexture;
import com.io7m.renderer.types.RExceptionUserError;

@SuppressWarnings("static-method") public final class KLightProjectiveTest
{
  @Test(expected = RExceptionLightMissingTexture.class) public
    void
    testBuilderNoTexture_0()
      throws RExceptionUserError,
        RException
  {
    final FakeCapabilities caps = new FakeCapabilities();
    final KLightProjectiveBuilderType b = KLightProjective.newBuilder();
    b.build(caps);
  }
}
