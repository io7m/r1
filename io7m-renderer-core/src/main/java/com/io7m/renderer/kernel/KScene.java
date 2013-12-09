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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;

/**
 * <p>
 * An immutable scene.
 * </p>
 * <p>
 * The <code>KScene</code> type separates opaque and translucent objects and
 * batches them by <i>lights</i> for rendering (it is the responsibility of
 * renderers to do additional per-material batching). A scene consists of:
 * </p>
 * <ul>
 * <li>Opaque instances that will appear in the final rendered image</li>
 * <li>Translucent instances that will appear in the final rendered image</li>
 * <li>Opaque shadow-casting instances that may not appear in the final
 * rendered image</li>
 * <li>Translucent shadow-casting instances that may not appear in the final
 * rendered image</li>
 * </ul>
 * <p>
 * Due to depth buffering, opaque objects may be rendered in any order without
 * affecting the final image. Consequently, opaque shadow-casting objects may
 * also be rendered in any order with respect to their lights. Translucent
 * objects, however, must be rendered in a specific order (typically, objects
 * furthest from the observer will be rendered prior to nearer objects). The
 * order is obviously observer-dependent, and therefore the order that
 * translucent objects will be rendered from the perspective of the
 * <i>camera</i> is typically different to the order required for rendering
 * from the perspective of the <i>lights</i> in the scene. Consider the
 * following scene:
 * </p>
 * 
 * <pre>
 * O---[instance0]---[instance1]---[instance2]---L
 * </pre>
 * <p>
 * Assuming that <code>instance0</code>, <code>instance1</code>, and
 * <code>instance2</code> are translucent, and given observer <code>O</code>
 * and shadow-producing light <code>L</code>, then when rendering from the
 * observer, the instances should be rendered in the order:
 * <code>instance2</code> <code>instance1</code> <code>instance0</code>. When
 * rendering from the perspective of <code>L</code> (to produce a shadow map),
 * however, the instances should be rendered in the order
 * <code>instance0</code> <code>instance1</code> <code>instance2</code>.
 * </p>
 * <p>
 * The <code>KScene</code> class preserves the order of insertion for
 * translucent objects, and delegates responsibility for doing spatial
 * partitioning and sorting objects by position to the creator of the scene.
 * </p>
 */

@Immutable public final class KScene
{
  private static class Builder<L extends KMaterialAlphaLabelCache> implements
    KSceneBuilder
  {
    private @Nonnull final MapPSet<KLight>                            all_lights;
    private final @Nonnull KCamera                                    camera;
    private final @Nonnull L                                          labels;
    private @Nonnull HashPMap<KLight, List<KMeshInstanceTransformed>> lit_opaque;
    private @Nonnull HashPMap<KMeshInstanceTransformed, List<KLight>> lit_translucent;
    private @Nonnull HashPMap<KLight, List<KMeshInstanceTransformed>> shadow_casters_opaque;
    private @Nonnull HashPMap<KLight, List<KMeshInstanceTransformed>> shadow_casters_translucent;
    private @Nonnull MapPSet<KLight>                                  shadow_lights;
    private @Nonnull PVector<KMeshInstanceTransformed>                translucent_ordered;
    private @Nonnull MapPSet<KMeshInstanceTransformed>                visible_opaque;
    private @Nonnull MapPSet<KMeshInstanceTransformed>                visible_instances;

    Builder(
      final @Nonnull KCamera camera,
      final @Nonnull L labels)
    {
      this.camera = camera;
      this.lit_opaque = HashTreePMap.empty();
      this.lit_translucent = HashTreePMap.empty();
      this.shadow_lights = HashTreePSet.empty();
      this.shadow_casters_opaque = HashTreePMap.empty();
      this.shadow_casters_translucent = HashTreePMap.empty();
      this.translucent_ordered = TreePVector.empty();
      this.visible_opaque = HashTreePSet.empty();
      this.labels = labels;
      this.visible_instances = HashTreePSet.empty();
      this.all_lights = HashTreePSet.empty();
    }

    private void addOpaqueInstance(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
    {
      final PVector<KMeshInstanceTransformed> instances;
      if (this.lit_opaque.containsKey(light)) {
        instances =
          (PVector<KMeshInstanceTransformed>) this.lit_opaque.get(light);
      } else {
        instances = TreePVector.empty();
      }
      this.lit_opaque = this.lit_opaque.plus(light, instances.plus(instance));
      this.visible_opaque = this.visible_opaque.plus(instance);
    }

    private void addOpaqueShadowCaster(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
    {
      final PVector<KMeshInstanceTransformed> instances;
      if (this.shadow_casters_opaque.containsKey(light)) {
        instances =
          (PVector<KMeshInstanceTransformed>) this.shadow_casters_opaque
            .get(light);
      } else {
        instances = TreePVector.empty();
      }
      this.shadow_casters_opaque =
        this.shadow_casters_opaque.plus(light, instances.plus(instance));
    }

    private void addTranslucentShadowCaster(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
    {
      final PVector<KMeshInstanceTransformed> instances;
      if (this.shadow_casters_translucent.containsKey(light)) {
        instances =
          (PVector<KMeshInstanceTransformed>) this.shadow_casters_translucent
            .get(light);
      } else {
        instances = TreePVector.empty();
      }
      this.shadow_casters_translucent =
        this.shadow_casters_translucent.plus(light, instances.plus(instance));
    }

    private void checkOpaque(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed transformed)
      throws ConstraintError
    {
      Constraints.constrainNotNull(light, "Light");
      Constraints.constrainNotNull(transformed, "Instance");

      final KMaterialAlphaLabel alpha =
        this.labels.getAlphaLabel(transformed.getInstance());

      Constraints.constrainArbitrary(
        alpha == KMaterialAlphaLabel.ALPHA_OPAQUE,
        "Instance material is opaque");
    }

    private void checkTranslucent(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed transformed)
      throws ConstraintError
    {
      Constraints.constrainNotNull(light, "Light");
      Constraints.constrainNotNull(transformed, "Instance");

      final KMaterialAlphaLabel alpha =
        this.labels.getAlphaLabel(transformed.getInstance());

      Constraints.constrainArbitrary(
        alpha == KMaterialAlphaLabel.ALPHA_TRANSLUCENT,
        "Instance material is opaque");
    }

    @Override public void sceneAddOpaqueInvisibleWithShadow(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      this.checkOpaque(light, instance);

      if (light.hasShadow()) {
        this.shadow_lights = this.shadow_lights.plus(light);
        this.addOpaqueShadowCaster(light, instance);
      }
    }

    @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      this.checkOpaque(light, instance);
      this.addOpaqueInstance(light, instance);
      this.visible_instances = this.visible_instances.plus(instance);
    }

    @Override public void sceneAddOpaqueLitVisibleWithShadow(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      this.checkOpaque(light, instance);
      this.addOpaqueInstance(light, instance);

      if (light.hasShadow()) {
        this.shadow_lights = this.shadow_lights.plus(light);
        this.addOpaqueShadowCaster(light, instance);
      }
      this.visible_instances = this.visible_instances.plus(instance);
    }

    @Override public void sceneAddTranslucentInvisibleShadowCaster(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      this.checkTranslucent(light, instance);

      if (light.hasShadow()) {
        this.shadow_lights = this.shadow_lights.plus(light);
        this.addTranslucentShadowCaster(light, instance);
      }
    }

    @Override public void sceneAddTranslucentLitVisibleWithoutShadow(
      final @Nonnull KLight light,
      final @Nonnull KMeshInstanceTransformed instance)
      throws ConstraintError
    {
      this.checkTranslucent(light, instance);

      this.translucent_ordered = this.translucent_ordered.plus(instance);

      final PVector<KLight> lights;
      if (this.lit_translucent.containsKey(instance)) {
        lights = (PVector<KLight>) this.lit_translucent.get(instance);
      } else {
        lights = TreePVector.empty();
      }
      this.lit_translucent =
        this.lit_translucent.plus(instance, lights.plus(light));

      this.visible_instances = this.visible_instances.plus(instance);
    }

    @SuppressWarnings("synthetic-access") @Override public
      KScene
      sceneCreate()
    {
      final KSceneOpaques o =
        new KSceneOpaques(
          this.lit_opaque,
          this.shadow_casters_opaque,
          this.visible_opaque);

      final KSceneTranslucents t =
        new KSceneTranslucents(
          this.shadow_casters_translucent,
          this.translucent_ordered,
          this.lit_translucent);

      return new KScene(
        this.camera,
        this.shadow_lights,
        o,
        t,
        this.visible_instances);
    }
  }

  /**
   * Information about the opaque instances in the current scene.
   */

  @Immutable public static class KSceneOpaques
  {
    private final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> lit;
    private final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> shadow_casters;
    private final @Nonnull Set<KMeshInstanceTransformed>               all;

    private KSceneOpaques(
      final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> lit_opaque,
      final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> shadow_casters_opaque,
      final @Nonnull Set<KMeshInstanceTransformed> visible_opaque)
    {
      this.lit = lit_opaque;
      this.shadow_casters = shadow_casters_opaque;
      this.all = visible_opaque;
    }

    /**
     * Retrieve a flat list of all the visible opaque objects in the scene.
     */

    public @Nonnull Set<KMeshInstanceTransformed> getAll()
    {
      return this.all;
    }

    /**
     * Retrieve the set of opaque instances affected by each light in the
     * scene.
     */

    public @Nonnull
      Map<KLight, List<KMeshInstanceTransformed>>
      getInstances()
    {
      return this.lit;
    }

    /**
     * Retrieve the set of opaque shadow casters for each light in the scene.
     */

    public @Nonnull
      Map<KLight, List<KMeshInstanceTransformed>>
      getShadowCasters()
    {
      return this.shadow_casters;
    }
  }

  /**
   * Information about the translucent instances in the current scene.
   */

  @Immutable public static class KSceneTranslucents
  {
    private final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> shadow_translucent_casters;
    private final @Nonnull Map<KMeshInstanceTransformed, List<KLight>> translucent_lights;
    private final @Nonnull List<KMeshInstanceTransformed>              translucent_ordered;

    private KSceneTranslucents(
      final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> shadow_translucent_casters,
      final @Nonnull List<KMeshInstanceTransformed> translucent_ordered,
      final @Nonnull Map<KMeshInstanceTransformed, List<KLight>> translucent_lights)
    {
      this.shadow_translucent_casters = shadow_translucent_casters;
      this.translucent_ordered = translucent_ordered;
      this.translucent_lights = translucent_lights;
    }

    /**
     * Retrieve the set of translucent instances in the scene that will appear
     * in the final rendered image. The instances are returned in insertion
     * order (with the oldest instance appearing first).
     */

    public @Nonnull List<KMeshInstanceTransformed> getInstancesOrdered()
    {
      return this.translucent_ordered;
    }

    /**
     * Retrieve the set of lights that affect each instance.
     */

    public @Nonnull
      Map<KMeshInstanceTransformed, List<KLight>>
      getLightsByInstance()
    {
      return this.translucent_lights;
    }

    /**
     * Retrieve the set of translucent shadow casters for each light in the
     * scene. The shadow casters for each light are in insertion order (with
     * the oldest instance appearing first).
     */

    public @Nonnull
      Map<KLight, List<KMeshInstanceTransformed>>
      getShadowCastersByLight()
    {
      return this.shadow_translucent_casters;
    }
  }

  /**
   * Retrieve a new {@link KSceneBuilder} with which to construct a scene,
   * rendered from the perspective of <code>camera</code>.
   * 
   * @throws ConstraintError
   *           Iff <code>labels == null || camera == null</code>.
   */

  public static @Nonnull
    <L extends KMaterialAlphaLabelCache>
    KSceneBuilder
    newBuilder(
      final @Nonnull L labels,
      final @Nonnull KCamera camera)
      throws ConstraintError
  {
    Constraints.constrainNotNull(camera, "Camera");
    Constraints.constrainNotNull(labels, "Label cache");
    return new Builder<KMaterialAlphaLabelCache>(camera, labels);
  }

  private final @Nonnull KCamera                       camera;
  private final @Nonnull KSceneOpaques                 opaques;
  private final @Nonnull Set<KLight>                   shadow_lights;
  private final @Nonnull KSceneTranslucents            translucents;
  private final @Nonnull Set<KMeshInstanceTransformed> visible;

  private KScene(
    final @Nonnull KCamera camera,
    final @Nonnull Set<KLight> shadow_lights,
    final @Nonnull KSceneOpaques opaques,
    final @Nonnull KSceneTranslucents translucents,
    final @Nonnull Set<KMeshInstanceTransformed> visible)
  {
    this.camera = camera;
    this.opaques = opaques;
    this.translucents = translucents;
    this.shadow_lights = shadow_lights;
    this.visible = visible;
  }

  public @Nonnull KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * Retrieve the set of opaque instances in the current scene.
   */

  public @Nonnull KSceneOpaques getOpaques()
  {
    return this.opaques;
  }

  /**
   * Retrieve the set of shadow casting lights in the current scene.
   */

  public @Nonnull Set<KLight> getShadowLights()
  {
    return this.shadow_lights;
  }

  /**
   * Retrieve the set of translucent instances in the current scene.
   */

  public @Nonnull KSceneTranslucents getTranslucents()
  {
    return this.translucents;
  }

  /**
   * Retrieve the set of all visible instances in the scene (that is, all
   * instances that were not added as invisible shadow casters).
   */

  public @Nonnull Set<KMeshInstanceTransformed> getVisibleInstances()
  {
    return this.visible;
  }
}
