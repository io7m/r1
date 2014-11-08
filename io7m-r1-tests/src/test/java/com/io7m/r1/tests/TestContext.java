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

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jvvfs.FilesystemType;

public final class TestContext
{
  private final FilesystemType         fs;
  private final JCGLImplementationType gi;
  private final LogUsableType          log;

  public TestContext(
    final FilesystemType in_fs,
    final JCGLImplementationType in_gi,
    final LogUsableType in_log)
  {
    this.fs = in_fs;
    this.gi = in_gi;
    this.log = in_log;
  }

  public FilesystemType getFilesystem()
  {
    return this.fs;
  }

  public JCGLImplementationType getGLImplementation()
  {
    return this.gi;
  }

  public LogUsableType getLog()
  {
    return this.log;
  }
}
