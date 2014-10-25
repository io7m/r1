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
import com.io7m.r1.types.RException;

/**
 * The type of region copiers.
 */

public interface KRegionCopierType
{
  /**
   * <p>
   * Copy the given <code>source_area</code> from the color buffer of
   * <code>source</code> to the given <code>target_area</code> of the color
   * buffer of <code>target</code>. The color buffer is expected to contain
   * depth-variance data, but the copying operation is functionally identical
   * to
   * {@link #copierCopyRGBAOnly(KFramebufferRGBAUsableType, AreaInclusive, KFramebufferRGBAUsableType, AreaInclusive)}
   * .
   * </p>
   * <p>
   * If the sizes of the two areas differ, the region will be scaled to the
   * correct size.
   * </p>
   *
   * @param source
   *          The source framebuffer
   * @param source_area
   *          The source area
   * @param target
   *          The target framebuffer
   * @param target_area
   *          The target area
   *
   * @throws RException
   *           If an error occurs
   */

  void copierCopyDepthVarianceOnly(
    final KFramebufferDepthVarianceUsableType source,
    final AreaInclusive source_area,
    final KFramebufferDepthVarianceUsableType target,
    final AreaInclusive target_area)
    throws RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the color buffer of
   * <code>source</code> to the given <code>target_area</code> of the color
   * buffer of <code>target</code>.
   * </p>
   * <p>
   * If the sizes of the two areas differ, the region will be scaled to the
   * correct size.
   * </p>
   *
   * @param source
   *          The source framebuffer
   * @param source_area
   *          The source area
   * @param target
   *          The target framebuffer
   * @param target_area
   *          The target area
   *
   * @throws RException
   *           If an error occurs
   */

  void copierCopyRGBAOnly(
    final KFramebufferRGBAUsableType source,
    final AreaInclusive source_area,
    final KFramebufferRGBAUsableType target,
    final AreaInclusive target_area)
    throws RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the color and depth buffers
   * of <code>source</code> to the given <code>target_area</code> of the color
   * and depth buffers of <code>target</code>.
   * </p>
   * <p>
   * If the sizes of the two areas differ, the region will be scaled to the
   * correct size.
   * </p>
   *
   * @param source
   *          The source framebuffer
   * @param source_area
   *          The source area
   * @param target
   *          The target framebuffer
   * @param target_area
   *          The target area
   *
   * @throws RException
   *           If an error occurs
   */

  void copierCopyRGBAWithDepth(
    final KFramebufferRGBAUsableType source,
    final AreaInclusive source_area,
    final KFramebufferRGBAUsableType target,
    final AreaInclusive target_area)
    throws RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the depth buffer of
   * <code>source</code> to the given <code>target_area</code> of the depth
   * buffer of <code>target</code>.
   * </p>
   * <p>
   * If the sizes of the two areas differ, the region will be scaled to the
   * correct size.
   * </p>
   *
   * @param source
   *          The source framebuffer
   * @param source_area
   *          The source area
   * @param target
   *          The target framebuffer
   * @param target_area
   *          The target area
   *
   * @throws RException
   *           If an error occurs
   */

  void copierCopyDepthOnly(
    final KFramebufferRGBAUsableType source,
    final AreaInclusive source_area,
    final KFramebufferRGBAUsableType target,
    final AreaInclusive target_area)
    throws RException;
}
