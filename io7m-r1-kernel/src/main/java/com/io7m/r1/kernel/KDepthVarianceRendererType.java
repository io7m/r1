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

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jfunctional.OptionType;
import com.io7m.jtensors.parameterized.PMatrixI4x4F;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KDepthInstancesType;
import com.io7m.r1.kernel.types.KFaceSelection;
import com.io7m.r1.kernel.types.KProjectionType;
import com.io7m.r1.spaces.RSpaceEyeType;
import com.io7m.r1.spaces.RSpaceWorldType;

/**
 * The type of depth-variance renderers.
 */

public interface KDepthVarianceRendererType extends KRendererType
{
  /**
   * Bind the given framebuffer and then call
   * {@link #rendererEvaluateDepthVarianceWithBoundFramebuffer(PMatrixI4x4F, KProjectionType, KDepthInstancesType, AreaInclusive, OptionType)}
   * unbinding the framebuffer after use.
   *
   * @param view
   *          The current view matrix
   * @param projection
   *          The current projection
   * @param instances
   *          The instances
   * @param framebuffer
   *          The output framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   *
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepthVariance(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final KFramebufferDepthVarianceUsableType framebuffer,
    final OptionType<KFaceSelection> faces)
    throws RException;

  /**
   * <p>
   * Evaluate the given batches with the renderer, assuming a depth-variance
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
   * @param instances
   *          The instances
   * @param framebuffer_area
   *          The inclusive area of the bound framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * @throws RException
   *           If an error occurs during rendering
   */

  void rendererEvaluateDepthVarianceWithBoundFramebuffer(
    final PMatrixI4x4F<RSpaceWorldType, RSpaceEyeType> view,
    final KProjectionType projection,
    final KDepthInstancesType instances,
    final AreaInclusive framebuffer_area,
    final OptionType<KFaceSelection> faces)
    throws RException;
}
