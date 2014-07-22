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
import com.io7m.renderer.types.RException;

/**
 * The type of opaque unlit renderers that use forward rendering.
 */

public interface KRendererForwardOpaqueUnlitType
{
  /**
   * <p>
   * Evaluate the given list of opaque unlit instances.
   * </p>
   * <p>
   * The renderer will perform depth testing against the depth buffer for
   * rendered objects if a {@link DepthFunction} is provided.
   * </p>
   * <p>
   * The renderer will modify the contents of the depth buffer iff
   * <code>depth_write</code> is <code>true</code>. This is intended for use
   * when called from different renderers that may or may not have
   * pre-populated the depth buffer with unlit objects.
   * </p>
   *
   * @see KDepthRendererType
   *
   * @param depth_function
   *          The optional depth test function.
   * @param depth_write
   *          <code>true</code> if the depth buffer should be modified.
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
    final boolean depth_write,
    final MatricesObserverType mwo,
    final Map<String, Set<KInstanceOpaqueType>> batches)
    throws RException;
}
