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

package com.io7m.renderer.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.JCGLInterfaceCommon;
import com.io7m.jcanephora.JCGLUnsupportedException;
import com.io7m.jcanephora.UsageHint;
import com.io7m.renderer.JOGLTestContext;
import com.io7m.renderer.TestContext;

public final class RXMLMeshParserVBOTest extends
  RXMLMeshParserVBOContract<JCGLException>
{
  public @Override RXMLMeshParser<JCGLException> parseFromStream(
    final @Nonnull TestContext context,
    final @Nonnull InputStream stream)
    throws JCGLException,
      IOException,
      RXMLException,
      ConstraintError
  {
    final JCGLImplementation gi = context.getGLImplementation();
    return RXMLMeshParser.parseFromStreamValidating(
      stream,
      new RXMLMeshParserVBO<JCGLInterfaceCommon>(
        gi.getGLCommon(),
        UsageHint.USAGE_STATIC_DRAW));
  }

  @Override public @Nonnull TestContext newTestContext()
    throws JCGLException,
      JCGLUnsupportedException,
      ConstraintError
  {
    return JOGLTestContext.makeContextWithDefault();
  }
}
