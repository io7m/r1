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

import javax.annotation.Nonnull;

enum SBRendererType
{
  RENDERER_DEBUG_DEPTH_ONLY("debug-depth-only"),
  RENDERER_DEBUG_UV_ONLY("debug-uv-only"),
  RENDERER_DEBUG_NORMALS_ONLY("debug-normals-only"),
  RENDERER_FLAT_TEXTURED("flat-textured"),
  RENDERER_FORWARD_DIFFUSE_ONLY("forward-diffuse-only"),
  RENDERER_FORWARD_DIFFUSE_SPECULAR("forward-diffuse-specular");

  private final @Nonnull String name;

  private SBRendererType(
    final @Nonnull String name)
  {
    this.name = name;
  }

  @Nonnull String getName()
  {
    return this.name;
  }
}
