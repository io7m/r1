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

package com.io7m.r1.examples.viewer;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGL;
import com.io7m.jcanephora.jogl.JCGLImplementationJOGLBuilderType;
import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;

final class VGLImplementation
{
  private VGLImplementation()
  {
    throw new UnreachableCodeException();
  }

  static GLProfile getGLProfile(
    final LogUsableType in_log)
  {
    final StringBuilder b = new StringBuilder();
    b.append("Available profiles:\n");
    for (final String p : GLProfile.GL_PROFILE_LIST_ALL) {
      if (GLProfile.isAvailable(p)) {
        b.append("  ");
        b.append(GLProfile.get(p));
        b.append("\n");
      }
    }
    in_log.debug(b.toString());

    if (GLProfile.isAvailable(GLProfile.GL3)) {
      return GLProfile.get(GLProfile.GL3);
    }
    if (GLProfile.isAvailable(GLProfile.GLES3)) {
      return GLProfile.get(GLProfile.GLES3);
    }
    if (GLProfile.isAvailable(GLProfile.GL2GL3)) {
      return GLProfile.get(GLProfile.GL2GL3);
    }

    in_log.debug("No preferred contexts available, falling back to default");
    return GLProfile.getDefault();
  }

  static JCGLImplementationType makeGLImplementation(
    final GLContext ctx,
    final LogUsableType in_log)
  {
    final JCGLImplementationJOGLBuilderType gb =
      JCGLImplementationJOGL.newBuilder();
    gb.setStateCaching(true);
    gb.setDebugging(true);

    final JCGLImplementationType gi = gb.build(ctx, in_log);
    return gi;
  }
}
