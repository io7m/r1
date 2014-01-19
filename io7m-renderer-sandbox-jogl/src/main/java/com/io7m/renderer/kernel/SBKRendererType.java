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

enum SBKRendererType
{
  KRENDERER_DEBUG_DEPTH("debug-depth"),
  KRENDERER_DEBUG_DEPTH_VARIANCE("debug-depth-variance"),
  KRENDERER_DEBUG_UV_VERTEX("debug-uv-vertex"),
  KRENDERER_DEBUG_BITANGENTS_LOCAL("debug-bitangents-local"),
  KRENDERER_DEBUG_BITANGENTS_EYE("debug-bitangents-eye"),
  KRENDERER_DEBUG_TANGENTS_VERTEX_LOCAL("debug-tangents-vertex-local"),
  KRENDERER_DEBUG_TANGENTS_VERTEX_EYE("debug-tangents-vertex-eye"),
  KRENDERER_DEBUG_NORMALS_VERTEX_LOCAL("debug-normals-vertex-local"),
  KRENDERER_DEBUG_NORMALS_VERTEX_EYE("debug-normals-vertex-eye"),
  KRENDERER_DEBUG_NORMALS_MAP_LOCAL("debug-normals-map-local"),
  KRENDERER_DEBUG_NORMALS_MAP_EYE("debug-normals-map-eye"),
  KRENDERER_DEBUG_NORMALS_MAP_TANGENT("debug-normals-map-tangent"),
  KRENDERER_DEBUG_SHADOW_MAP("debug-shadow-map"),
  KRENDERER_FORWARD("forward")

  ;

  private final @Nonnull String name;

  private SBKRendererType(
    final @Nonnull String name)
  {
    this.name = name;
  }

  @Nonnull String getName()
  {
    return this.name;
  }
}
