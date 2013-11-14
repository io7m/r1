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

package com.io7m.renderer.kernel;

import java.io.File;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.PropertyUtils;
import com.io7m.jaux.PropertyUtils.ValueIncorrectType;
import com.io7m.jaux.PropertyUtils.ValueNotFound;

public final class SandboxConfig
{
  public static @Nonnull SandboxConfig fromProperties(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound,
      ValueIncorrectType
  {
    return new SandboxConfig(props);
  }
  private final boolean             opengl_debug;
  private final boolean             opengl_trace;
  private final @Nonnull Properties props;

  private final @Nonnull File       shader_archive_file;

  public SandboxConfig(
    final @Nonnull Properties props)
    throws ConstraintError,
      ValueNotFound,
      ValueIncorrectType
  {
    this.props = Constraints.constrainNotNull(props, "Properties");

    this.shader_archive_file =
      new File(PropertyUtils.getString(
        props,
        "com.io7m.renderer.sandbox.shader_archive"));

    this.opengl_debug =
      PropertyUtils.getOptionalBoolean(
        props,
        "com.io7m.renderer.sandbox.opengl.debug",
        true);
    this.opengl_trace =
      PropertyUtils.getOptionalBoolean(
        props,
        "com.io7m.renderer.sandbox.opengl.trace",
        false);
  }

  public @Nonnull File getShaderArchiveFile()
  {
    return this.shader_archive_file;
  }

  public boolean isOpenglDebug()
  {
    return this.opengl_debug;
  }

  public boolean isOpenglTrace()
  {
    return this.opengl_trace;
  }
}
