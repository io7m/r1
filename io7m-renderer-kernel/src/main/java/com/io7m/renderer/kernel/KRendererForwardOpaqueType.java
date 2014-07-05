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

import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.DepthFunction;
import com.io7m.jfunctional.OptionType;
import com.io7m.renderer.kernel.KMutableMatrices.MatricesObserverType;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.types.RException;

/**
 * The type of opaque renderers that use forward rendering.
 */

public interface KRendererForwardOpaqueType
{
  /**
   * <p>
   * Evaluate the given list of opaque lit instances.
   * </p>
   * <p>
   * The renderer will not modify the contents of the current framebuffer's
   * depth buffer, but will perform depth testing against the depth buffer for
   * rendered objects if a {@link DepthFunction} is provided.
   * </p>
   * 
   * @see KDepthRendererType
   * 
   * @param depth_function
   *          The optional depth test function.
   * @param shadow_context
   *          The shadow map context
   * @param mwo
   *          The current observer matrices
   * @param batches
   *          The opaque batches.
   * 
   * @throws RException
   *           If an error occurs.
   */

  void rendererEvaluateOpaqueLit(
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> batches)
    throws RException;

  /**
   * <p>
   * Evaluate the given list of opaque unlit instances.
   * </p>
   * <p>
   * The renderer will not modify the contents of the current framebuffer's
   * depth buffer, but will perform depth testing against the depth buffer for
   * rendered objects if a {@link DepthFunction} is provided.
   * </p>
   * 
   * @see KDepthRendererType
   * 
   * @param depth_function
   *          The optional depth test function.
   * @param shadow_context
   *          The shadow map context
   * @param mwo
   *          The current observer matrices
   * @param batches
   *          The opaque batches.
   * 
   * @throws RException
   *           If an error occurs.
   */

  void rendererEvaluateOpaqueUnlit(
    final KShadowMapContextType shadow_context,
    final OptionType<DepthFunction> depth_function,
    final MatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> batches)
    throws RException;
}