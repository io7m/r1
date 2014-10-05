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

package com.io7m.r1.kernel.types;

import com.io7m.r1.types.RExceptionBuilderInvalid;

/**
 * A mutable "builder" interface for creating immutable
 * {@link KVisibleSetShadows} snapshots.
 */

public interface KVisibleSetShadowsBuilderType
{
  /**
   * <p>
   * Add an invisible instance <code>instance</code> associated with light
   * <code>light</code>. The instance will not appear in the rendered scene,
   * but will cast a shadow.
   * </p>
   * <p>
   * Shadow casting is controlled by depth buffering (even for translucent
   * instances) and therefore the order that shadow casting instances are
   * added to the scene has no effect on the rendered image with respect to
   * shadows.
   * </p>
   * <p>
   * Allowing instances to appear in scenes purely as shadow casters is
   * supported in order to allow separate, simplified geometry to be used for
   * shadow casting.
   * </p>
   *
   * @param light
   *          The light
   * @param instance
   *          The shadow-casting instance
   * @throws RExceptionBuilderInvalid
   *           If the builder has already been used.
   */

  void visibleShadowsAddCaster(
    final KLightWithShadowType light,
    final KInstanceOpaqueType instance)
    throws RExceptionBuilderInvalid;

  /**
   * <p>
   * Add a shadow-projecting light.
   * </p>
   * <p>
   * This function is provided because although a given light may be
   * configured to cast a shadow and may be lighting one or more instances, it
   * may not currently have any shadow casting instances associated with it.
   * The shadow map renderer that eventually consumes the visible set needs to
   * know about the light in order to (re)initialize its associated shadow
   * map, to prevent the light being used with an uninitialized shadow map.
   * </p>
   * <p>
   * This function is typically called by builders created via
   * {@link KVisibleSetLightGroup} and it is therefore not necessary for users
   * to call this function directly.
   * </p>
   *
   * @param light
   *          The light
   * @throws RExceptionBuilderInvalid
   *           If the builder has already been used.
   */

  void visibleShadowsAddLight(
    final KLightWithShadowType light)
    throws RExceptionBuilderInvalid;
}
