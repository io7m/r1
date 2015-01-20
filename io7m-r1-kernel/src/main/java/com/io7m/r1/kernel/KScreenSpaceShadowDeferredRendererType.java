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
import com.io7m.jcanephora.TextureUnitType;
import com.io7m.jcanephora.api.JCGLInterfaceCommonType;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.kernel.types.KLightWithScreenSpaceShadowType;

/**
 * The type of renderers that can produce a screen-space shadow for a light
 * and a populated depth buffer.
 */

public interface KScreenSpaceShadowDeferredRendererType
{
  /**
   * Render a screen-space shadow for the given light, and pass the rendered
   * shadow to <code>f</code>.
   *
   * @param gc
   *          An OpenGL interface
   * @param area
   *          The inclusive area of the screen
   * @param t_map_depth_stencil
   *          The texture unit to which is bound the current depth buffer
   * @param view_rays
   *          The current view rays
   * @param mdp
   *          The current projective light matrices
   * @param shadow_map_context
   *          The current shadow map context
   * @param lpss
   *          The light
   * @param f
   *          The function to be evaluated
   * @throws RException
   *           On errors
   * @throws E
   *           If <code>f</code> throws <code>E</code>
   *
   * @param <A>
   *          The type of values returned by <code>f</code>
   * @param <E>
   *          The type of exceptions raised by <code>f</code>
   */

  <A, E extends Exception> void withScreenSpaceShadow(
    final JCGLInterfaceCommonType gc,
    final AreaInclusive area,
    final TextureUnitType t_map_depth_stencil,
    final KViewRays view_rays,
    final KMatricesProjectiveLightType mdp,
    final KShadowMapContextType shadow_map_context,
    final KLightWithScreenSpaceShadowType lpss,
    final KScreenSpaceShadowDeferredWithType<A, E> f)
    throws RException,
      E;
}
