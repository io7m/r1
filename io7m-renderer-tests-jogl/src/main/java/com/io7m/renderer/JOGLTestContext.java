/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer;

import java.util.Properties;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLImplementationJOGL;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityAll;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.jogamp.common.util.VersionNumber;

public final class JOGLTestContext
{
  private static GLOffscreenAutoDrawable buffer;
  static final String                    LOG_DESTINATION_OPENGL;
  static final String                    LOG_DESTINATION_OPENGL_ES_3_0;
  static final String                    LOG_DESTINATION_OPENGL_ES_2_0;
  static final String                    LOG_DESTINATION_OPENGL_3_0;
  static final String                    LOG_DESTINATION_OPENGL_3_p;
  static final String                    LOG_DESTINATION_OPENGL_2_1;

  static {
    LOG_DESTINATION_OPENGL = "jogl-test";
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
      f.createOffscreenAutoDrawable(null, cap, null, width, height, null);

    return k;
  }

  private static GLContext getContext(
    final GLProfile profile)
  {
    if (JOGLTestContext.buffer != null) {
      final GLContext context = JOGLTestContext.buffer.getContext();
      context.release();
      JOGLTestContext.buffer.destroy();
    }

    JOGLTestContext.buffer =
      JOGLTestContext.createOffscreenDrawable(profile, 640, 480);

    final GLContext context = JOGLTestContext.buffer.getContext();
    final int r = context.makeCurrent();
    if (r == GLContext.CONTEXT_NOT_CURRENT) {
      throw new AssertionError("Could not make context current");
    }

    return context;
  }

  private static FSCapabilityAll getFS(
    final Log log)
  {
    try {
      final Filesystem fs = Filesystem.makeWithoutArchiveDirectory(log);
      fs.mountClasspathArchive(TestContext.class, PathVirtual.ROOT);
      return fs;
    } catch (final FilesystemError e) {
      throw new java.lang.RuntimeException(e);
    } catch (final ConstraintError e) {
      throw new java.lang.RuntimeException(e);
    }
  }

  private static Log getLog(
    final String destination)
  {
    final Properties properties = new Properties();
    return new Log(properties, "com.io7m.jcanephora", destination);
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
    if (GLProfile.isAvailable(GLProfile.GLES2)) {
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

  public static TestContext makeContextWithOpenGL_ES2()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_ES_2_0);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx =
      JOGLTestContext.getContext(GLProfile.get(GLProfile.GLES2));
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);

    return new TestContext(fs, gi, log);
  }

  public static TestContext makeContextWithOpenGL_ES3()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_ES_3_0);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx =
      JOGLTestContext.getContext(GLProfile.get(GLProfile.GLES3));
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);

    final VersionNumber version = ctx.getGLVersionNumber();
    if (version.getMajor() != 3) {
      throw new JCGLUnsupportedException("GLES3 profile "
        + version
        + " is not 3.*");
    }

    return new TestContext(fs, gi, log);
  }

  public static TestContext makeContextWithOpenGL2_1()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_2_1);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx =
      JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);

    return new TestContext(fs, gi, log);
  }

  public static TestContext makeContextWithOpenGL3_0()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_3_0);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx =
      JOGLTestContext.getContext(GLProfile.get(GLProfile.GL2));
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);

    final VersionNumber version = ctx.getGLVersionNumber();
    if (version.getMajor() != 3) {
      throw new JCGLUnsupportedException("GL2 profile is not 3.0!");
    }

    return new TestContext(fs, gi, log);
  }

  public static TestContext makeContextWithOpenGL3_p()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL_3_p);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx =
      JOGLTestContext.getContext(GLProfile.get(GLProfile.GL3));
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);

    final VersionNumber version = ctx.getGLVersionNumber();
    if (version.getMajor() != 3) {
      throw new JCGLUnsupportedException("GL3 profile "
        + version
        + " is not 3.p");
    }
    if (version.getMinor() == 0) {
      throw new JCGLUnsupportedException("GL3 profile "
        + version
        + " is not 3.p");
    }

    return new TestContext(fs, gi, log);
  }

  public static TestContext makeContextWithDefault()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    final Log log =
      JOGLTestContext.getLog(JOGLTestContext.LOG_DESTINATION_OPENGL);
    final FSCapabilityAll fs = JOGLTestContext.getFS(log);

    final GLContext ctx = JOGLTestContext.getContext(GLProfile.getDefault());
    final JCGLImplementation gi = new JCGLImplementationJOGL(ctx, log);
    return new TestContext(fs, gi, log);
  }

  private JOGLTestContext()
  {
    throw new UnreachableCodeException();
  }
}
