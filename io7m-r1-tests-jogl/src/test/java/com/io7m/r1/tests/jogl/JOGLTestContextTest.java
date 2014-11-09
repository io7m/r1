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

package com.io7m.r1.tests.jogl;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

@SuppressWarnings("static-method") public class JOGLTestContextTest
{
  @Test public void testGL_AtLeastOne()
  {
    boolean ok = false;

    ok |= JOGLTestContext.isOpenGL21WithExtensionsSupported();
    ok |= JOGLTestContext.isOpenGL30Supported();
    ok |= JOGLTestContext.isOpenGL3pSupported();
    ok |= JOGLTestContext.isOpenGLES2Supported();
    ok |= JOGLTestContext.isOpenGLES3Supported();

    Assert.assertTrue(ok);
  }

  @Test public void testGL_ES2()
  {
    System.out.println("GL_ES2: " + JOGLTestContext.isOpenGLES2Supported());
  }

  @Test public void testGL_ES2_Open()
  {
    Assume.assumeTrue(JOGLTestContext.isOpenGLES2Supported());
    JOGLTestContext.makeContextWithOpenGL_ES2(false);
  }

  @Test public void testGL_ES3()
  {
    System.out.println("GL_ES3: " + JOGLTestContext.isOpenGLES3Supported());
  }

  @Test public void testGL_ES3_Open()
  {
    Assume.assumeTrue(JOGLTestContext.isOpenGLES3Supported());
    JOGLTestContext.makeContextWithOpenGL_ES3(false);
  }

  @Test public void testGL2_1()
  {
    System.out.println("GL2_1: "
      + JOGLTestContext.isOpenGL21WithExtensionsSupported());
  }

  @Test public void testGL2_1_Open()
  {
    Assume.assumeTrue(JOGLTestContext.isOpenGL21WithExtensionsSupported());
    JOGLTestContext.makeContextWithOpenGL2_1(false);
  }

  @Test public void testGL3_0()
  {
    System.out.println("GL3_0: " + JOGLTestContext.isOpenGL30Supported());
  }

  @Test public void testGL3_0_Open()
  {
    Assume.assumeTrue(JOGLTestContext.isOpenGL30Supported());
    JOGLTestContext.makeContextWithOpenGL3_0(false);
  }

  @Test public void testGL3_p()
  {
    System.out.println("GL3_p: " + JOGLTestContext.isOpenGL3pSupported());
  }

  @Test public void testGL3_p_Open()
  {
    Assume.assumeTrue(JOGLTestContext.isOpenGL3pSupported());
    JOGLTestContext.makeContextWithOpenGL3_p(false);
  }
}
