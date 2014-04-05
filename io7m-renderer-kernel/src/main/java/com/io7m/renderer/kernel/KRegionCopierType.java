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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.renderer.types.RException;

interface KRegionCopierType
{
  /**
   * Close the copier, releasing any resources the copier has allocated.
   * 
   * @throws RException
   *           If an error occurs
   */

  void copierClose()
    throws RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the colour buffer of
   * <code>source</code> to the given <code>target_area</code> of the colour
   * buffer of <code>target</code>. The colour buffer is expected to contain
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>, or if
   *           <code>source == target</code>
   * @throws RException
   *           If an error occurs
   */

  void copierCopyDepthVarianceOnly(
    final @Nonnull KFramebufferDepthVarianceUsableType source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferDepthVarianceUsableType target,
    final @Nonnull AreaInclusive target_area)
    throws ConstraintError,
      RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the colour buffer of
   * <code>source</code> to the given <code>target_area</code> of the colour
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>, or if
   *           <code>source == target</code>
   * @throws RException
   *           If an error occurs
   */

  void copierCopyRGBAOnly(
    final @Nonnull KFramebufferRGBAUsableType source,
    final @Nonnull AreaInclusive source_area,
    final @Nonnull KFramebufferRGBAUsableType target,
    final @Nonnull AreaInclusive target_area)
    throws ConstraintError,
      RException;

  /**
   * <p>
   * Copy the given <code>source_area</code> from the colour and depth buffers
   * of <code>source</code> to the given <code>target_area</code> of the
   * colour and depth buffers of <code>target</code>.
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
   * @throws ConstraintError
   *           If any parameter is <code>null</code>, or if
   *           <code>source == target</code>
   * @throws RException
   *           If an error occurs
   */

    <F extends KFramebufferRGBAUsableType & KFramebufferDepthUsableType>
    void
    copierCopyRGBAWithDepth(
      final @Nonnull F source,
      final @Nonnull AreaInclusive source_area,
      final @Nonnull F target,
      final @Nonnull AreaInclusive target_area)
      throws ConstraintError,
        RException;
}
