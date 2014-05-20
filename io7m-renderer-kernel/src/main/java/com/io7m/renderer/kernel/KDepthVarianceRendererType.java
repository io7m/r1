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

import com.io7m.jfunctional.OptionType;
import com.io7m.renderer.kernel.types.KFaceSelection;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;
import com.io7m.renderer.types.RTransformViewType;

/**
 * The type of depth-variance renderers.
 */

public interface KDepthVarianceRendererType extends KRendererType
{
  /**
   * Evaluate the given batches with the current depth renderer.
   * 
   * @param view
   *          The current view matrix
   * @param projection
   *          The current projection matrix
   * @param batches
   *          The batches
   * @param framebuffer
   *          The output framebuffer
   * @param faces
   *          The face selection override (to force all instances to render
   *          using front faces, back faces, or both, if specified)
   * 
   * @throws RException
   *           If an error occurs during rendering
   */

    void
    rendererEvaluateDepthVariance(
      final RMatrixI4x4F<RTransformViewType> view,
      final RMatrixI4x4F<RTransformProjectionType> projection,
      final Map<KMaterialDepthVarianceLabel, List<KInstanceOpaqueType>> batches,
      final KFramebufferDepthVarianceUsableType framebuffer,
      final OptionType<KFaceSelection> faces)
      throws RException;
}
