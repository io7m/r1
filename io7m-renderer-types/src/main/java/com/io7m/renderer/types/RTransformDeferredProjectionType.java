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

package com.io7m.renderer.types;

/**
 * <p>
 * A transformation from eye-space ({@link RSpaceEyeType}), back to world (
 * {@link RSpaceWorldType}) space, to eye-space ({@link RSpaceEyeType}) from
 * the perspective of a projective light source, and finally to clip-space (
 * {@link RSpaceClipType}) from the perspective of the same light source.
 * </p>
 * <p>
 * This transform is primarily used during <i>deferred rendering</i> of
 * projective lights. The eye-space position of an arbitrary object is
 * reconstructed in a fragment shader, and that eye-space position is
 * transformed to projective-clip-space for projective lighting.
 * </p>
 */

public interface RTransformDeferredProjectionType extends RTransformType
{
  // No value-level representation.
}