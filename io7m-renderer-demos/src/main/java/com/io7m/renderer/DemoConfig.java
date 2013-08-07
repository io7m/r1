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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jcanephora.TextureLoader;
import com.io7m.jlog.Log;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorReadable2I;
import com.io7m.jvvfs.FSCapabilityAll;

@Immutable public final class DemoConfig
{
  private final @Nonnull JCGLImplementation gl_implementation;
  private final @Nonnull Log                log;
  private final @Nonnull FSCapabilityAll    filesystem;
  private final @Nonnull TextureLoader      texture_loader;
  private final @Nonnull VectorM2I          window_position;
  private final @Nonnull VectorM2I          window_size;

  public DemoConfig(
    final @Nonnull JCGLImplementation gl,
    final @Nonnull TextureLoader texture_loader,
    final @Nonnull Log log,
    final @Nonnull FSCapabilityAll filesystem,
    final @Nonnull VectorM2I window_position,
    final @Nonnull VectorM2I window_size)
  {
    this.gl_implementation = gl;
    this.log = log;
    this.texture_loader = texture_loader;
    this.filesystem = filesystem;
    this.window_position = window_position;
    this.window_size = window_size;
  }

  public @Nonnull FSCapabilityAll getFilesystem()
  {
    return this.filesystem;
  }

  public @Nonnull JCGLImplementation getGL()
  {
    return this.gl_implementation;
  }

  public @Nonnull Log getLog()
  {
    return this.log;
  }

  public @Nonnull TextureLoader getTextureLoader()
  {
    return this.texture_loader;
  }

  public @Nonnull VectorReadable2I getWindowPosition()
  {
    return this.window_position;
  }

  public @Nonnull VectorReadable2I getWindowSize()
  {
    return this.window_size;
  }
}
