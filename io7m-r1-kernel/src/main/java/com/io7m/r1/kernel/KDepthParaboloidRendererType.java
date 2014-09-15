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

package com.io7m.r1.kernel;

import java.util.List;
import java.util.Map;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jfunctional.OptionType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KInstanceOpaqueType;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RMatrixI4x4F;
import com.io7m.r1.types.RTransformViewType;

/**
 * The type of paraboloid depth renderers.
 */

public interface KDepthParaboloidRendererType extends KRendererType
{
  /**
   * Bind the given framebuffer and then call
   * {@link #rendererEvaluateDepthParaboloidWithBoundFramebuffer(RMatrixI4x4F, Map, AreaInclusive, OptionType)}
   * , unbinding it after use.
   *
   * @param view
   *          The current view matrix
   * @param batches
   *          The batches
   * @param framebuffer
   *          The output framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * @param z_near
   *          The distance to the near plane
   * @param z_far
   *          The distance to the far plane
   *
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepthParaboloid(
    final RMatrixI4x4F<RTransformViewType> view,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final KFramebufferDepthUsableType framebuffer,
    final OptionType<KFaceSelection> faces,
    final float z_near,
    final float z_far)
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
   * @param batches
   *          The batches
   * @param framebuffer_area
   *          The inclusive area of the bound framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * @param z_near
   *          The distance to the near plane
   * @param z_far
   *          The distance to the far plane
   *
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepthParaboloidWithBoundFramebuffer(
    final RMatrixI4x4F<RTransformViewType> view,
    final Map<String, List<KInstanceOpaqueType>> batches,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces,
    final float z_near,
    final float z_far)
    throws RException;
}
