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

package com.io7m.renderer.kernel.sandbox;

import java.math.BigInteger;

public final class SBCacheStatistics
{
  private final  BigInteger cached_labels;
  private final  BigInteger cached_rgba_framebuffers;
  private final  BigInteger cached_rgba_framebuffers_bytes;
  private final  BigInteger cached_shaders;
  private final  BigInteger cached_shadow_maps;
  private final  BigInteger cached_shadow_maps_bytes;

  public SBCacheStatistics(
    final  BigInteger in_cached_labels,
    final  BigInteger in_cached_shaders,
    final  BigInteger in_cached_shadow_maps,
    final  BigInteger in_cached_shadow_maps_bytes,
    final  BigInteger in_cached_rgba_framebuffers,
    final  BigInteger in_cached_rgba_framebuffers_bytes)
  {
    this.cached_labels = in_cached_labels;
    this.cached_shaders = in_cached_shaders;
    this.cached_shadow_maps = in_cached_shadow_maps;
    this.cached_shadow_maps_bytes = in_cached_shadow_maps_bytes;
    this.cached_rgba_framebuffers = in_cached_rgba_framebuffers;
    this.cached_rgba_framebuffers_bytes = in_cached_rgba_framebuffers_bytes;
  }

  public  BigInteger getCachedLabels()
  {
    return this.cached_labels;
  }

  public  BigInteger getCachedRGBAFramebuffers()
  {
    return this.cached_rgba_framebuffers;
  }

  public  BigInteger getCachedRGBAFramebuffersBytes()
  {
    return this.cached_rgba_framebuffers_bytes;
  }

  public  BigInteger getCachedShaders()
  {
    return this.cached_shaders;
  }

  public  BigInteger getCachedShadowMaps()
  {
    return this.cached_shadow_maps;
  }

  public  BigInteger getCachedShadowMapsBytes()
  {
    return this.cached_shadow_maps_bytes;
  }
}
