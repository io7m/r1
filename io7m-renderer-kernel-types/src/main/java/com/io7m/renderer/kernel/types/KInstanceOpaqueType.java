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

package com.io7m.renderer.kernel.types;

import javax.annotation.Nonnull;

/**
 * <p>
 * An instance that will be rendered in opaque mode.
 * </p>
 * <p>
 * Opaque instances are rendered in an arbitrary order, using the depth buffer
 * to ensure that objects appear at the correct perceived distance.
 * </p>
 */

public interface KInstanceOpaqueType extends KInstanceRegularType
{
  /**
   * @return The regular material of this instance
   */

  @Override @Nonnull KMaterialOpaqueType instanceGetMaterial();

  /**
   * @return The faces that will be rendered
   */

  @Nonnull KFaceSelection instanceGetFaces();
}
