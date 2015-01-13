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

import java.util.Set;

import com.io7m.r1.exceptions.RExceptionBuilderInvalid;

/**
 * A mutable "builder" interface for creating immutable lists of translucent
 * instances.
 */

public interface KVisibleSetTranslucentsBuilderType
{
  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material lit by lights <code>lights</code>. The instance will
   * not cast a shadow, even if any <code>lights</code> are configured to
   * produce them.
   * </p>
   * <p>
   * Translucent instances are rendered in the order that they are added to
   * the scene. If translucent instance <code>B</code> is added to the scene
   * after translucent instance <code>A</code>, then <code>B</code> will be
   * rendered on top of <code>A</code>, even if <code>A</code> is closer to
   * the observer than <code>B</code>. Due to depth buffering, translucent
   * instances will correctly overlap and be overlapped by opaque instances,
   * without needing to be added to the scene in any particular order with
   * respect to opaque instances.
   * </p>
   *
   * @param lights
   *          The set of lights affecting the instance
   * @param instance
   *          The instance
   * @throws RExceptionBuilderInvalid
   *           If the builder has been invalidated
   */

  void visibleTranslucentsAddLit(
    KInstanceTranslucentLitType instance,
    Set<KLightTranslucentType> lights)
    throws RExceptionBuilderInvalid;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material and is unaffected by lighting.
   * </p>
   *
   * @param instance
   *          The unlit instance
   * @throws RExceptionBuilderInvalid
   *           If the builder has been invalidated
   */

  void visibleTranslucentsAddUnlit(
    KInstanceTranslucentUnlitType instance)
    throws RExceptionBuilderInvalid;
}
