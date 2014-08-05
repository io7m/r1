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

package com.io7m.renderer.examples;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.renderer.kernel.KShaderCacheDebugType;
import com.io7m.renderer.kernel.KShaderCacheDeferredGeometryType;
import com.io7m.renderer.kernel.KShaderCacheDeferredLightTranslucentType;
import com.io7m.renderer.kernel.KShaderCacheDeferredLightType;
import com.io7m.renderer.kernel.KShaderCacheDepthType;
import com.io7m.renderer.kernel.KShaderCacheDepthVarianceType;
import com.io7m.renderer.kernel.KShaderCacheForwardOpaqueUnlitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentLitType;
import com.io7m.renderer.kernel.KShaderCacheForwardTranslucentUnlitType;
import com.io7m.renderer.kernel.KShaderCachePostprocessingType;
import com.io7m.renderer.types.RException;

/**
 * A forward renderer constructor.
 */

public interface ExampleRendererConstructorDeferredType extends
  ExampleRendererConstructorType
{
  /**
   * Construct a new renderer.
   *
   * @param log
   *          A log handle
   * @param in_shader_debug_cache
   *          A shader cache
   * @param in_shader_translucent_lit_cache
   *          A shader cache
   * @param in_shader_translucent_unlit_cache
   *          A shader cache
   * @param in_shader_forward_opaque_unlit_cache
   *          A shader cache
   * @param in_shader_depth_cache
   *          A shader cache
   * @param in_shader_depth_variance_cache
   *          A shader cache
   * @param in_shader_postprocessing_cache
   *          A shader cache
   * @param in_shader_deferred_geo_cache
   *          A shader cache
   * @param in_shader_deferred_light_cache
   *          A shader cache
   * @param in_shader_deferred_light_translucent_cache
   *          A shader cache
   * @param gi
   *          A GL implementation
   * @return A new renderer
   *
   * @throws JCGLException
   *           If an OpenGL error occurs
   * @throws RException
   *           If any other error occurs
   */

    ExampleRendererType
    newRenderer(
      final LogUsableType log,
      final KShaderCacheDebugType in_shader_debug_cache,
      final KShaderCacheForwardTranslucentLitType in_shader_translucent_lit_cache,
      final KShaderCacheForwardTranslucentUnlitType in_shader_translucent_unlit_cache,
      final KShaderCacheForwardOpaqueUnlitType in_shader_forward_opaque_unlit_cache,
      final KShaderCacheDepthType in_shader_depth_cache,
      final KShaderCacheDepthVarianceType in_shader_depth_variance_cache,
      final KShaderCachePostprocessingType in_shader_postprocessing_cache,
      final KShaderCacheDeferredGeometryType in_shader_deferred_geo_cache,
      final KShaderCacheDeferredLightType in_shader_deferred_light_cache,
      final KShaderCacheDeferredLightTranslucentType in_shader_deferred_light_translucent_cache,
      final JCGLImplementationType gi)
      throws JCGLException,
        RException;
}
