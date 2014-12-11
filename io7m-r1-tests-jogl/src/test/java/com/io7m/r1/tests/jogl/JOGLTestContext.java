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

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLExceptionUnsupported;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.api.JCGLSoftRestrictionsType;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGL;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGLBuilderType;
import com.io7m.jfunctional.Option;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.r1.tests.TestContext;
import com.jogamp.common.util.VersionNumber;

public final class JOGLTestContext
{
  private static GLOffscreenAutoDrawable buffer;
  static final String                    LOG_DESTINATION_OPENGL_2_1;
  static final String                    LOG_DESTINATION_OPENGL_3_0;
  static final String                    LOG_DESTINATION_OPENGL_3_p;
  static final String                    LOG_DESTINATION_OPENGL_ES_2_0;
  static final String                    LOG_DESTINATION_OPENGL_ES_3_0;

  static {
    LOG_DESTINATION_OPENGL_ES_3_0 = "jogl_es_3_0-test";
    LOG_DESTINATION_OPENGL_ES_2_0 = "jogl_es_2_0-test";
    LOG_DESTINATION_OPENGL_3_0 = "jogl_3_0-test";
    LOG_DESTINATION_OPENGL_3_p = "jogl_3_p-test";
    LOG_DESTINATION_OPENGL_2_1 = "jogl_2_1-test";
  }

  private static GLOffscreenAutoDrawable createOffscreenDrawable(
    final GLProfile profile,
    final int width,
    final int height)
  {
    final GLCapabilities cap = new GLCapabilities(profile);
    cap.setFBO(true);

    final GLDrawableFactory f = GLDrawableFactory.getFactory(profile);
    final GLOffscreenAutoDrawable k =
      f.createOffscreenAutoDrawable(null, cap, null, width, height);
    k.display();

    return k;
  }

  private static GLContext getContext(
    final GLProfile profile)
  {
    if (JOGLTestContext.buffer != null) {
      final GLContext context = JOGLTestContext.buffer.getContext();
      if (context.isCurrent()) {
        context.release();
      }
      JOGLTestContext.buffer.destroy();
    }

    JOGLTestContext.buffer =
      JOGLTestContext.createOffscreenDrawable(profile, 640, 480);
    assert JOGLTestContext.buffer != null;
    final GLContext context = JOGLTestContext.buffer.getContext();
    assert context != null;

    final int r = context.makeCurrent();
    if (r == GLContext.CONTEXT_NOT_CURRENT) {
      throw new AssertionError("Could not make context current");
    }

    return context;
  }

  private static FilesystemType getFS(
    final LogUsableType log)
  {
    try {
      final FilesystemType fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(TestContext.class, PathVirtual.ROOT);
      return fs;
    } catch (final FilesystemError e) {
      throw new java.lang.RuntimeException(e);
    }
  }

  private static LogType getLog(
    final String destination)
  {
    return Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "tests");
  }

  /**
   * Return <code>true</code> if the implementation supports OpenGL 2.1, and
   * additionally the extensions needed to make 2.1 act somewhat like 3.0.
   */

  public static boolean isOpenGL21WithExtensionsSupported()
  {
    if (GLProfile.isAvailable(GLProfile.GL2)) {
      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));

      final VersionNumber v = ctx.getGLVersionNumber();
      if (v.getMajor() != 2) {
        System.err
          .println("isOpenGL21WithExtensionsSupported: unavailable: GL2 profile available, but version is "
            + v);
        return false;
      }
      if (ctx.hasFullFBOSupport() == false) {
        System.err
          .println("isOpenGL21WithExtensionsSupported: unavailable: GL2 profile and 2.1 available, but full FBO support is missing");
        return false;
      }

      System.err
        .println("isOpenGL21WithExtensionsSupported: available: GL2 profile and 2.1 available");
      return true;
    }

    System.err
      .println("isOpenGL21WithExtensionsSupported: unavailable: GL2 profile not available");
    return false;
  }

  /**
   * Return <code>true</code> if the implementation supports OpenGL 3.0.
   */

  public static boolean isOpenGL30Supported()
  {
    if (GLProfile.isAvailable(GLProfile.GL2)) {
      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));

      final VersionNumber v = ctx.getGLVersionNumber();
      if (v.getMajor() == 3) {
        System.err
          .println("isOpenGL30Supported: available: GL2 profile available, major version is 3");
        return true;
      }

      System.err
        .println("isOpenGL30Supported: unavailable: GL2 profile available, major version is "
          + v.getMajor());
      return false;
    }

    System.err
      .println("isOpenGL30Supported: unavailable: GL2 profile not available");
    return false;
  }

  /**
   * Return <code>true</code> if the implementation supports OpenGL 3.p, where
   * p >= 1.
   */

  public static boolean isOpenGL3pSupported()
  {
    if (GLProfile.isAvailable(GLProfile.GL3)) {
      System.err
        .println("isOpenGL3pSupported: available: GL3 profile available");
      return true;
    }

    System.err
      .println("isOpenGL3pSupported: unavailable: GL3 profile not available");
    return false;
  }

  /**
   * Return <code>true</code> if the implementation supports OpenGL ES2.
   */

  public static boolean isOpenGLES2Supported()
  {
    /**
     * Checking for GLES3 is required because JOGL considers GLES3 and GLES2
     * to be interchangeable (they aren't). If GLES3 is available, then JOGL
     * will return a GLES3 context when asked for GLES2 (it may not actually
     * have any choice in the matter on broken contexts like Mesa).
     */

    if (GLProfile.isAvailable(GLProfile.GLES2)) {
      if (GLProfile.isAvailable(GLProfile.GLES3)) {
        System.err
          .println("isOpenGLES2Supported: unavailable: GLES2 profile is available, but GLES3 is also available, so GLES2 is being suppressed");
        return false;
      }

      System.err
        .println("isOpenGLES2Supported: available: GLES2 profile available");
      return true;
    }

    System.err
      .println("isOpenGLES2Supported: unavailable: GLES2 profile not available");
    return false;
  }

  /**
   * Return <code>true</code> if the implementation supports OpenGL ES3.
   */

  public static boolean isOpenGLES3Supported()
  {
    if (GLProfile.isAvailable(GLProfile.GLES3)) {
      System.err
        .println("isOpenGLES3Supported: available: GLES3 profile available");
      return true;
    }

    System.err
      .println("isOpenGLES3Supported: unavailable: GLES3 profile not available");
    return false;
  }

  public static TestContext makeContextWithOpenGL_ES2(
    final boolean caching)
  {
    return JOGLTestContext.makeContextWithOpenGL_ES2_Actual(null, caching);
  }

  private static TestContext makeContextWithOpenGL_ES2_Actual(
    final @Nullable JCGLSoftRestrictionsType r,
    final boolean caching)
  {
    try {
      final LogType log =
        JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_ES_2_0);
      final FilesystemType fs = JOGLTestContext.getFS(log);

      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GLES2));
      final JCGLImplementationType gi =
        JOGLTestContext.makeImplementation(r, log, ctx, caching);

      return new TestContext(fs, gi, log);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static TestContext makeContextWithOpenGL_ES2_WithRestrictions(
    final JCGLSoftRestrictionsType r)
  {
    return JOGLTestContext.makeContextWithOpenGL_ES2_Actual(r, false);
  }

  public static TestContext makeContextWithOpenGL_ES3(
    final boolean caching)
  {
    return JOGLTestContext.makeContextWithOpenGL_ES3_Actual(null, caching);
  }

  private static TestContext makeContextWithOpenGL_ES3_Actual(
    final @Nullable JCGLSoftRestrictionsType r,
    final boolean caching)
  {
    try {
      final LogType log =
        JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_ES_3_0);
      final FilesystemType fs = JOGLTestContext.getFS(log);

      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GLES3));
      final JCGLImplementationType gi =
        JOGLTestContext.makeImplementation(r, log, ctx, caching);

      final VersionNumber version = ctx.getGLVersionNumber();
      if (version.getMajor() != 3) {
        throw new JCGLExceptionUnsupported("GLES3 profile "
          + version
          + " is not 3.*");
      }

      return new TestContext(fs, gi, log);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static TestContext makeContextWithOpenGL_ES3_WithRestrictions(
    final JCGLSoftRestrictionsType r)
  {
    return JOGLTestContext.makeContextWithOpenGL_ES3_Actual(r, false);
  }

  public static TestContext makeContextWithOpenGL2_1(
    final boolean caching)
  {
    return JOGLTestContext.makeContextWithOpenGL2_1_Actual(null, caching);
  }

  private static TestContext makeContextWithOpenGL2_1_Actual(
    final @Nullable JCGLSoftRestrictionsType r,
    final boolean caching)
  {
    try {
      final LogType log =
        JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_2_1);
      final FilesystemType fs = JOGLTestContext.getFS(log);

      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));
      final JCGLImplementationType gi =
        JOGLTestContext.makeImplementation(r, log, ctx, caching);

      return new TestContext(fs, gi, log);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static TestContext makeContextWithOpenGL2_1_WithRestrictions(
    final JCGLSoftRestrictionsType r)
  {
    return JOGLTestContext.makeContextWithOpenGL2_1_Actual(r, false);
  }

  public static TestContext makeContextWithOpenGL3_0(
    final boolean caching)
  {
    return JOGLTestContext.makeContextWithOpenGL3_0_Actual(null, caching);
  }

  private static TestContext makeContextWithOpenGL3_0_Actual(
    final @Nullable JCGLSoftRestrictionsType r,
    final boolean caching)
  {
    try {
      final LogType log =
        JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_3_0);
      final FilesystemType fs = JOGLTestContext.getFS(log);

      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));
      final JCGLImplementationType gi =
        JOGLTestContext.makeImplementation(r, log, ctx, caching);

      final VersionNumber version = ctx.getGLVersionNumber();
      if (version.getMajor() != 3) {
        throw new JCGLExceptionUnsupported("GL2 profile is not 3.0!");
      }

      return new TestContext(fs, gi, log);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static TestContext makeContextWithOpenGL3_0_WithRestrictions(
    final JCGLSoftRestrictionsType r)
  {
    return JOGLTestContext.makeContextWithOpenGL3_0_Actual(r, false);
  }

  public static TestContext makeContextWithOpenGL3_p(
    final boolean caching)
  {
    return JOGLTestContext.makeContextWithOpenGL3_p_Actual(null, caching);
  }

  private static TestContext makeContextWithOpenGL3_p_Actual(
    final @Nullable JCGLSoftRestrictionsType r,
    final boolean caching)
  {
    try {
      final LogType log =
        JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_3_p);
      final FilesystemType fs = JOGLTestContext.getFS(log);

      final GLContext ctx =
        JOGLTestContext.getContext(GLProfile.get(GLProfile.GL3));

      final JCGLImplementationType gi =
        JOGLTestContext.makeImplementation(r, log, ctx, caching);

      final VersionNumber version = ctx.getGLVersionNumber();
      if (version.getMajor() != 3) {
        throw new JCGLExceptionUnsupported("GL3 profile "
          + version
          + " is not 3.p");
      }
      if (version.getMinor() == 0) {
        throw new JCGLExceptionUnsupported("GL3 profile "
          + version
          + " is not 3.p");
      }

      return new TestContext(fs, gi, log);
    } catch (final Throwable x) {
      throw new UnreachableCodeException(x);
    }
  }

  public static TestContext makeContextWithOpenGL3_p_WithRestrictions(
    final JCGLSoftRestrictionsType r)
  {
    return JOGLTestContext.makeContextWithOpenGL3_p_Actual(r, false);
  }

  private static JCGLImplementationType makeImplementation(
    final JCGLSoftRestrictionsType r,
    final LogUsableType log,
    final GLContext ctx,
    final boolean caching)
    throws JCGLException
  {
    final JCGLImplementationJOGLBuilderType b =
      JCGLImplementationJOGL.newBuilder();
    b.setDebugging(true);
    b.setRestrictions(Option.of(r));
    b.setStateCaching(caching);
    return b.build(ctx, log);
  }

  private JOGLTestContext()
  {
    throw new UnreachableCodeException();
  }
}
