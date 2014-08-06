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

import java.util.Set;

import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionLightGroupAlreadyAdded;

/**
 * A mutable "builder" interface for creating immutable {@link KScene}
 * snapshots.
 */

public interface KSceneBuilderType extends KSceneBuilderReadableType
{
  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material and is unaffected by lighting.
   * </p>
   * <p>
   * Due to depth buffering, opaque instances may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   *
   * @param instance
   *          The shadow-casting instance
   * @throws RExceptionInstanceAlreadyLit
   *           If the instance has already been added to one or more light
   *           groups.
   *
   * @see #sceneAddOpaqueLitVisibleWithoutShadow(KLightType,
   *      KInstanceOpaqueType)
   * @see #sceneAddOpaqueLitVisibleWithShadow(KLightType, KInstanceOpaqueType)
   */

  void sceneAddOpaqueUnlit(
    final KInstanceOpaqueType instance)
    throws RExceptionInstanceAlreadyLit;

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
   */

  void sceneAddShadowCaster(
    final KLightWithShadowType light,
    final KInstanceOpaqueType instance);

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
   *          The shadow-casting instance
   *
   * @see #sceneAddShadowCaster(KLightType, KInstanceOpaqueType)
   */

  void sceneAddTranslucentLit(
    final KInstanceTranslucentLitType instance,
    final Set<KLightType> lights);

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material and is unaffected by lighting.
   * </p>
   *
   * @param instance
   *          The unlit instance
   */

  void sceneAddTranslucentUnlit(
    final KInstanceTranslucentUnlitType instance);

  /**
   * <p>
   * Construct a new builder to construct a light group.
   * </p>
   *
   * @return A new light group builder.
   * @param name
   *          The name of the new light group.
   *
   * @throws RExceptionLightGroupAlreadyAdded
   *           A light group already exists with the given name.
   */

  KSceneLightGroupBuilderType sceneNewLightGroup(
    final String name)
    throws RExceptionLightGroupAlreadyAdded;
}
