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
import com.io7m.renderer.types.RExceptionInstanceAlreadyShadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnshadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyVisible;
import com.io7m.renderer.types.RExceptionLightMissingShadow;

/**
 * A mutable "builder" interface for creating immutable {@link KScene}
 * snapshots.
 */

public interface KSceneBuilderType extends KSceneBuilderReadableType
{
  /**
   * <p>
   * Add an invisible instance <code>instance</code> associated with light
   * <code>light</code>. The instance will not appear in the rendered scene,
   * but will cast a shadow if <code>light</code> is configured to produce
   * shadows.
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
   * 
   * @throws RExceptionInstanceAlreadyVisible
   *           If the given instance is already a visible instance for the
   *           given light - an instance cannot be both visible and invisible
   *           for a given light!
   * @throws RExceptionLightMissingShadow
   *           If the light has no shadow.
   */

  void sceneAddInvisibleWithShadow(
    final KLightType light,
    final KInstanceOpaqueType instance)
    throws RExceptionInstanceAlreadyVisible,
      RExceptionLightMissingShadow;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material lit by light <code>light</code>. The instance will not cast a
   * shadow, even if <code>light</code> is configured to produce them.
   * </p>
   * <p>
   * Due to depth buffering, opaque instances may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * <p>
   * Allowing instances to appear in scenes without casting shadows is
   * supported in order to allow separate, simplified geometry to be used for
   * shadow casting.
   * </p>
   * 
   * @param light
   *          The light
   * @param instance
   *          The instance
   * @throws RExceptionInstanceAlreadyUnlit
   *           If <code>instance</code> has already been added to the scene
   *           without lighting.
   * @throws RExceptionInstanceAlreadyShadowed
   *           If <code>instance</code> has already been added to the scene
   *           without a shadow.
   * 
   * @see #sceneAddInvisibleWithShadow(KLightType, KInstanceOpaqueType)
   */

  void sceneAddOpaqueLitVisibleWithoutShadow(
    final KLightType light,
    final KInstanceOpaqueType instance)
    throws RExceptionInstanceAlreadyUnlit,
      RExceptionInstanceAlreadyShadowed;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material lit by light <code>light</code>.
   * </p>
   * <p>
   * Due to depth buffering, opaque instances may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * <p>
   * The instance will cast a shadow if <code>light</code> is configured to
   * produce shadows.
   * </p>
   * 
   * @param light
   *          The light
   * @param instance
   *          The instance
   * 
   * @throws RExceptionInstanceAlreadyUnlit
   *           If <code>instance</code> has already been added to the scene
   *           without lighting.
   * @throws RExceptionLightMissingShadow
   *           If the light has no shadow.
   * @throws RExceptionInstanceAlreadyUnshadowed
   *           If <code>instance</code> is already a non shadow-caster for the
   *           given light.
   * 
   * @see #sceneAddInvisibleWithShadow(KLightType, KInstanceOpaqueType)
   */

  void sceneAddOpaqueLitVisibleWithShadow(
    final KLightType light,
    final KInstanceOpaqueType instance)
    throws RExceptionInstanceAlreadyUnlit,
      RExceptionLightMissingShadow,
      RExceptionInstanceAlreadyUnshadowed;

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
   *           If the instance has already been added to the scene with
   *           lighting.
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
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material lit by lights <code>lights</code>. The instance will
   * not cast a shadow, even if any <code>lights</code> are configured to
   * produce them. To make the instance cast shadows for a particular light,
   * it must also be added to the scene with
   * {@link #sceneAddInvisibleWithShadow(KLightType, KInstanceOpaqueType)} .
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
   * @see #sceneAddInvisibleWithShadow(KLightType, KInstanceOpaqueType)
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
}
