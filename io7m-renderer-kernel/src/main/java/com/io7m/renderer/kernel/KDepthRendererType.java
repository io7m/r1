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

import java.util.List;
import java.util.Map;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jfunctional.OptionType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KProjectionType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The type of depth renderers.
 */

public interface KDepthRendererType extends KRendererType
{
  /**
   * Bind the given framebuffer and then call
   * {@link #rendererEvaluateDepthWithBoundFramebuffer(RMatrixI4x4F, KProjectionType, Map, AreaInclusive, OptionType)}
   * , unbinding the framebuffer after use.
   *
   * @param view
   *          The current view matrix
   * @param projection
   *          The current projection
   * @param batches
   *          The batches
   * @param framebuffer
   *          The output framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepth(
    final RMatrixI4x4F<RTransformViewType> view,
    final KProjectionType projection,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final KFramebufferDepthUsableType framebuffer,
    final OptionType<KFaceSelection> faces)
    throws RException;

  /**
   * <p>
   * Evaluate the given batches with the renderer, assuming a depth-only
   * framebuffer is currently bound.
   * </p>
   * <p>
   * The framebuffer will <i>not</i> be cleared prior to use.
   * </p>
   *
   * @param view
   *          The current view matrix
   * @param projection
   *          The current projection
   * @param batches
   *          The batches
   * @param framebuffer_area
   *          The inclusive area of the bound framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepthWithBoundFramebuffer(
    final RMatrixI4x4F<RTransformViewType> view,
    final KProjectionType projection,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces)
    throws RException;
}
