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

package com.io7m.renderer.kernel;

import java.math.BigInteger;

import javax.annotation.Nonnull;

public final class SBCacheStatistics
{
  private final @Nonnull BigInteger cached_labels;
  private final @Nonnull BigInteger cached_rgba_framebuffers;
  private final @Nonnull BigInteger cached_rgba_framebuffers_bytes;
  private final @Nonnull BigInteger cached_shaders;
  private final @Nonnull BigInteger cached_shadow_maps;
  private final @Nonnull BigInteger cached_shadow_maps_bytes;

  public SBCacheStatistics(
    final @Nonnull BigInteger cached_labels,
    final @Nonnull BigInteger cached_shaders,
    final @Nonnull BigInteger cached_shadow_maps,
    final @Nonnull BigInteger cached_shadow_maps_bytes,
    final @Nonnull BigInteger cached_rgba_framebuffers,
    final @Nonnull BigInteger cached_rgba_framebuffers_bytes)
  {
    this.cached_labels = cached_labels;
    this.cached_shaders = cached_shaders;
    this.cached_shadow_maps = cached_shadow_maps;
    this.cached_shadow_maps_bytes = cached_shadow_maps_bytes;
    this.cached_rgba_framebuffers = cached_rgba_framebuffers;
    this.cached_rgba_framebuffers_bytes = cached_rgba_framebuffers_bytes;
  }

  public @Nonnull BigInteger getCachedLabels()
  {
    return this.cached_labels;
  }

  public @Nonnull BigInteger getCachedRGBAFramebuffers()
  {
    return this.cached_rgba_framebuffers;
  }

  public @Nonnull BigInteger getCachedRGBAFramebuffersBytes()
  {
    return this.cached_rgba_framebuffers_bytes;
  }

  public @Nonnull BigInteger getCachedShaders()
  {
    return this.cached_shaders;
  }

  public @Nonnull BigInteger getCachedShadowMaps()
  {
    return this.cached_shadow_maps;
  }

  public @Nonnull BigInteger getCachedShadowMapsBytes()
  {
    return this.cached_shadow_maps_bytes;
  }
}
