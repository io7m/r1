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

package com.io7m.renderer.tests;

import javax.annotation.Nonnull;

import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityAll;

public final class TestContext
{
  private final @Nonnull FSCapabilityAll    fs;
  private final @Nonnull JCGLImplementation gi;
  private final @Nonnull Log                log;

  public TestContext(
    final @Nonnull FSCapabilityAll in_fs,
    final @Nonnull JCGLImplementation in_gi,
    final @Nonnull Log in_log)
  {
    this.fs = in_fs;
    this.gi = in_gi;
    this.log = in_log;
  }

  public FSCapabilityAll getFilesystem()
  {
    return this.fs;
  }

  public JCGLImplementation getGLImplementation()
  {
    return this.gi;
  }

  public Log getLog()
  {
    return this.log;
  }
}