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

package com.io7m.r1.tests;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLApi;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLSLVersionNumber;
import com.io7m.jcanephora.JCGLVersionNumber;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.fake.FakeContext;
import com.io7m.jcanephora.fake.FakeDefaultFramebuffer;
import com.io7m.jcanephora.fake.FakeShaderControlType;
import com.io7m.jcanephora.fake.JCGLImplementationFake;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogUsableType;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.junreachable.UnimplementedCodeException;

public final class RFakeGL
{
  public static final AreaInclusive SCREEN_AREA;

  static {
    SCREEN_AREA =
      new AreaInclusive(new RangeInclusiveL(0, 639), new RangeInclusiveL(
        0,
        479));
  }

  private static JCGLImplementationType newFakeGL(
    final LogUsableType in_log,
    final JCGLVersionNumber number,
    final JCGLApi api,
    final JCGLSLVersionNumber glsl_version,
    final FakeShaderControlType shader_control)
  {
    try {
      final FakeDefaultFramebuffer fb =
        FakeDefaultFramebuffer.newFramebuffer(
          RFakeGL.SCREEN_AREA,
          8,
          8,
          8,
          24,
          8);
      final FakeContext context =
        FakeContext.newContext(fb, number, api, glsl_version);

      return JCGLImplementationFake.newBuilder().build(
        context,
        shader_control,
        in_log);
    } catch (final JCGLException e) {
      throw new UnimplementedCodeException(e);
    }
  }

  public static JCGLImplementationType newFakeGL21(
    final LogUsableType log,
    final FakeShaderControlType shader_control)
  {
    final JCGLVersionNumber number = new JCGLVersionNumber(2, 1, 0);
    final JCGLSLVersionNumber glsl_version = new JCGLSLVersionNumber(1, 10);
    return RFakeGL.newFakeGL(
      log,
      number,
      JCGLApi.JCGL_FULL,
      glsl_version,
      shader_control);
  }

  public static JCGLImplementationType newFakeGL30(
    final FakeShaderControlType shader_control)
  {
    final LogUsableType log =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");

    final JCGLVersionNumber number = new JCGLVersionNumber(3, 0, 0);
    final JCGLSLVersionNumber glsl_version = new JCGLSLVersionNumber(1, 50);
    return RFakeGL.newFakeGL(
      log,
      number,
      JCGLApi.JCGL_FULL,
      glsl_version,
      shader_control);
  }

  public static JCGLImplementationType newFakeGL30WithLog(
    final LogUsableType log,
    final FakeShaderControlType shader_control)
  {
    final JCGLVersionNumber number = new JCGLVersionNumber(3, 0, 0);
    final JCGLSLVersionNumber glsl_version = new JCGLSLVersionNumber(1, 50);
    return RFakeGL.newFakeGL(
      log,
      number,
      JCGLApi.JCGL_FULL,
      glsl_version,
      shader_control);
  }

  public static JCGLImplementationType newFakeGLES2(
    final LogUsableType log,
    final FakeShaderControlType shader_control)
  {
    final JCGLVersionNumber number = new JCGLVersionNumber(2, 0, 0);
    final JCGLSLVersionNumber glsl_version = new JCGLSLVersionNumber(1, 0);
    return RFakeGL.newFakeGL(
      log,
      number,
      JCGLApi.JCGL_ES,
      glsl_version,
      shader_control);
  }

  public static JCGLImplementationType newFakeGLES3(
    final LogUsableType log,
    final FakeShaderControlType shader_control)
  {
    final JCGLVersionNumber number = new JCGLVersionNumber(3, 0, 0);
    final JCGLSLVersionNumber glsl_version = new JCGLSLVersionNumber(3, 0);
    return RFakeGL.newFakeGL(
      log,
      number,
      JCGLApi.JCGL_ES,
      glsl_version,
      shader_control);
  }
}
