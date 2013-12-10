/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

/**
 * A mutable "builder" interface for creating immutable {@link KScene}
 * snapshots.
 */

public interface KSceneBuilder
{
  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material lit by light <code>light</code>. The object will not appear in
   * the rendered scene, but will cast a shadow if <code>light</code> is
   * configured to produce shadows.
   * </p>
   * <p>
   * Due to depth buffering, opaque objects may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * <p>
   * Allowing objects to appear in scenes purely as shadow casters is
   * supported in order to allow separate, simplified geometry to be used for
   * shadow casting.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is translucent</li>
   *           </ul>
   */

  public void sceneAddOpaqueInvisibleWithShadow(
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material lit by light <code>light</code>. The object will not cast a
   * shadow, even if <code>light</code> is configured to produce them.
   * </p>
   * <p>
   * Due to depth buffering, opaque objects may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * <p>
   * Allowing objects to appear in scenes without casting shadows is supported
   * in order to allow separate, simplified geometry to be used for shadow
   * casting.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is translucent</li>
   *           </ul>
   */

  public void sceneAddOpaqueLitVisibleWithoutShadow(
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material lit by light <code>light</code>.
   * </p>
   * <p>
   * Due to depth buffering, opaque objects may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * <p>
   * The instance will cast a shadow if <code>light</code> is configured to
   * produce shadows.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is translucent</li>
   *           </ul>
   */

  public void sceneAddOpaqueLitVisibleWithShadow(
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have an opaque
   * material and is unaffected by lighting.
   * </p>
   * <p>
   * Due to depth buffering, opaque objects may be rendered in any order and
   * therefore the order that they are added to the scene has no effect on the
   * rendered image.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>instance == null</code></li>
   *           <li>The material used for <code>instance</code> is translucent</li>
   *           </ul>
   */

  public void sceneAddOpaqueUnlit(
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code>, which is expected to have a
   * translucent material, that will cast shadows with respect to
   * <code>light</code> and will not appear in the final rendered image.
   * Because translucent objects are rendering-order dependent, translucent
   * shadow casters must be explicitly added in the correct order for each
   * light.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is not
   *           translucent</li>
   *           </ul>
   */

  public void sceneAddTranslucentInvisibleShadowCaster(
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material lit by light <code>light</code>. The object will not
   * cast a shadow, even if <code>light</code> is configured to produce them.
   * Because translucent objects are rendering-order dependent, translucent
   * shadow casters must be explicitly added in the correct order for each
   * light. See
   * {@link #sceneAddTranslucentInvisibleShadowCaster(KLight, KMeshInstanceTransformed)}
   * .
   * </p>
   * <p>
   * Translucent objects are rendered in the order that they are added to the
   * scene. If translucent object <code>B</code> is added to the scene after
   * translucent object <code>A</code>, then <code>B</code> will be rendered
   * on top of <code>A</code>, even if <code>A</code> is closer to the
   * observer than <code>B</code>. Due to depth buffering, translucent objects
   * will correctly overlap and be overlapped by opaque objects, without
   * needing to be added to the scene in any particular order with respect to
   * opaque objects.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is not
   *           translucent</li>
   *           </ul>
   * 
   * @see #sceneAddTranslucentInvisibleShadowCaster(KLight,
   *      KMeshInstanceTransformed)
   */

  public void sceneAddTranslucentLitVisibleWithoutShadow(
    final @Nonnull KLight light,
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * <p>
   * Add an instance <code>instance</code> which is expected to have a
   * translucent material and is unaffected by lighting.
   * </p>
   * 
   * @throws ConstraintError
   *           If any of the following hold:
   *           <ul>
   *           <li><code>light == null || instance == null</code></li>
   *           <li>The material used for <code>instance</code> is not
   *           translucent</li>
   *           </ul>
   * 
   * @see #sceneAddTranslucentLitVisibleWithoutShadow(KLight,
   *      KMeshInstanceTransformed)
   */

  public void sceneAddTranslucentUnlit(
    final @Nonnull KMeshInstanceTransformed instance)
    throws ConstraintError;

  /**
   * Construct a {@link KScene} from the currently added instances and lights.
   */

  public @Nonnull KScene sceneCreate();
}
