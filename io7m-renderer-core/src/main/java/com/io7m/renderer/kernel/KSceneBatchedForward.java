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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.KScene.KSceneTranslucents;

@Immutable final class KSceneBatchedForward
{
  @Immutable static final class BatchOpaqueShadow
  {
    private final @Nonnull List<KMeshInstanceTransformed> instances;
    private final @Nonnull KMaterialShadowLabel           label;
    private final @Nonnull KLight                         light;

    private BatchOpaqueShadow(
      final @Nonnull KLight light,
      final @Nonnull KMaterialShadowLabel label,
      final @Nonnull List<KMeshInstanceTransformed> instances)
    {
      this.light = light;
      this.label = label;
      this.instances = instances;
    }

    @Nonnull List<KMeshInstanceTransformed> getInstances()
    {
      return this.instances;
    }

    @Nonnull KMaterialShadowLabel getLabel()
    {
      return this.label;
    }

    @Nonnull KLight getLight()
    {
      return this.light;
    }
  }

  @Immutable static final class BatchTranslucentLit
  {
    private final @Nonnull KMeshInstanceTransformed instance;
    private final @Nonnull KMaterialForwardLabel    label;
    private final @Nonnull List<KLight>             lights;

    private BatchTranslucentLit(
      final @Nonnull KMeshInstanceTransformed instance,
      final @Nonnull KMaterialForwardLabel label,
      final @Nonnull List<KLight> lights)
    {
      this.instance = instance;
      this.label = label;
      this.lights = lights;
    }

    @Nonnull KMeshInstanceTransformed getInstance()
    {
      return this.instance;
    }

    @Nonnull KMaterialForwardLabel getLabel()
    {
      return this.label;
    }

    @Nonnull List<KLight> getLights()
    {
      return this.lights;
    }
  }

  @Immutable static final class BatchTranslucentShadow
  {
    private final @Nonnull List<KMeshInstanceTransformed> instances;
    private final @Nonnull KLight                         light;

    private BatchTranslucentShadow(
      final @Nonnull KLight light,
      final @Nonnull List<KMeshInstanceTransformed> instances)
    {
      this.light = light;
      this.instances = instances;
    }

    @Nonnull List<KMeshInstanceTransformed> getInstances()
    {
      return this.instances;
    }

    @Nonnull KLight getLight()
    {
      return this.light;
    }
  }

  /**
   * Given a set of opaque instances batched per light, return the same
   * opaques batched by light and material.
   */

  private static @Nonnull
    Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>>
    makeOpaqueBatches(
      final @Nonnull KMaterialForwardLabelCache labels,
      final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> instances_by_light)
      throws ConstraintError
  {
    final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit =
      new HashMap<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>>();

    for (final KLight light : instances_by_light.keySet()) {
      final List<KMeshInstanceTransformed> instances =
        instances_by_light.get(light);

      for (int i = 0; i < instances.size(); ++i) {
        final KMeshInstanceTransformed instance = instances.get(i);
        final KMaterialForwardLabel label =
          labels.getForwardLabel(instance.getInstance());

        Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> by_material;
        if (batches_opaque_lit.containsKey(light)) {
          by_material = batches_opaque_lit.get(light);
        } else {
          by_material =
            new HashMap<KMaterialForwardLabel, List<KMeshInstanceTransformed>>();
        }

        List<KMeshInstanceTransformed> batch;
        if (by_material.containsKey(label)) {
          batch = by_material.get(label);
        } else {
          batch = new ArrayList<KMeshInstanceTransformed>();
        }

        batch.add(instance);
        by_material.put(label, batch);
        batches_opaque_lit.put(light, by_material);
      }
    }

    return batches_opaque_lit;
  }

  /**
   * Calculate labels for all shadow casters and batch.
   */

  @SuppressWarnings("synthetic-access") private static @Nonnull
    Map<KLight, BatchOpaqueShadow>
    makeOpaqueShadowCasters(
      final @Nonnull KMaterialShadowLabelCache labels,
      final @Nonnull Map<KLight, List<KMeshInstanceTransformed>> casters)
      throws ConstraintError
  {
    final HashMap<KLight, BatchOpaqueShadow> m =
      new HashMap<KLight, BatchOpaqueShadow>();

    for (final KLight light : casters.keySet()) {
      final List<KMeshInstanceTransformed> instances = casters.get(light);

      /**
       * Labels will be identical for all opaque casters for a given light.
       */

      assert instances.size() >= 1;
      final KMeshInstanceTransformed instance = instances.get(0);

      final Option<KShadow> shadow_opt = light.getShadow();
      assert shadow_opt.isSome();
      final KShadow shadow = ((Option.Some<KShadow>) shadow_opt).value;

      final KMaterialShadowLabel label =
        labels.getShadowLabel(instance.getInstance(), shadow);
      m.put(light, new BatchOpaqueShadow(light, label, instances));
    }

    return m;
  }

  @SuppressWarnings("synthetic-access") private static @Nonnull
    List<BatchTranslucentLit>
    makeTranslucentBatches(
      final @Nonnull KMaterialForwardLabelCache forward_labels,
      final @Nonnull KSceneTranslucents translucents)
      throws ConstraintError
  {
    final List<KMeshInstanceTransformed> ordered =
      translucents.getInstancesOrdered();
    final Map<KMeshInstanceTransformed, List<KLight>> lit =
      translucents.getLightsByInstance();

    final ArrayList<BatchTranslucentLit> batches =
      new ArrayList<BatchTranslucentLit>();

    for (int index = 0; index < ordered.size(); ++index) {
      final KMeshInstanceTransformed instance = ordered.get(index);
      final KMaterialForwardLabel label =
        forward_labels.getForwardLabel(instance.getInstance());

      assert lit.containsKey(instance);
      final List<KLight> lights = lit.get(instance);

      batches.add(new BatchTranslucentLit(instance, label, lights));
    }

    return batches;
  }

  @SuppressWarnings("synthetic-access") private static @Nonnull
    Map<KLight, BatchTranslucentShadow>
    makeTranslucentShadowCasters(
      final @Nonnull KSceneTranslucents translucents)
  {
    final Map<KLight, List<KMeshInstanceTransformed>> casters =
      translucents.getShadowCastersByLight();

    final HashMap<KLight, BatchTranslucentShadow> batches =
      new HashMap<KLight, BatchTranslucentShadow>();

    for (final KLight light : casters.keySet()) {
      final List<KMeshInstanceTransformed> instances = casters.get(light);
      batches.put(light, new BatchTranslucentShadow(light, instances));
    }

    return batches;
  }

  static @Nonnull KSceneBatchedForward newBatchedScene(
    final @Nonnull KMaterialShadowLabelCache shadow_labels,
    final @Nonnull KMaterialForwardLabelCache forward_labels,
    final @Nonnull KScene scene)
    throws ConstraintError
  {
    Constraints.constrainNotNull(forward_labels, "Labels");
    Constraints.constrainNotNull(scene, "Scene");

    final KSceneOpaques opaques = scene.getOpaques();
    final KSceneTranslucents translucents = scene.getTranslucents();

    final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit =
      KSceneBatchedForward.makeOpaqueBatches(
        forward_labels,
        opaques.getInstances());

    final Map<KLight, BatchOpaqueShadow> shadow_opaque =
      KSceneBatchedForward.makeOpaqueShadowCasters(
        shadow_labels,
        opaques.getShadowCasters());

    final List<BatchTranslucentLit> batches_translucent =
      KSceneBatchedForward.makeTranslucentBatches(
        forward_labels,
        translucents);

    final Map<KLight, BatchTranslucentShadow> shadow_translucent =
      KSceneBatchedForward.makeTranslucentShadowCasters(translucents);

    return new KSceneBatchedForward(
      batches_opaque_lit,
      batches_translucent,
      scene.getShadowLights(),
      shadow_opaque,
      shadow_translucent);
  }

  private final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit;
  private final @Nonnull List<BatchTranslucentLit>                                               batches_translucent;
  private final @Nonnull Set<KLight>                                                             shadow_lights;
  private final @Nonnull Map<KLight, BatchOpaqueShadow>                                          shadow_opaque;
  private final @Nonnull Map<KLight, BatchTranslucentShadow>                                     shadow_translucent;

  private KSceneBatchedForward(
    final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit,
    final @Nonnull List<BatchTranslucentLit> batches_translucent,
    final @Nonnull Set<KLight> shadow_lights,
    final @Nonnull Map<KLight, BatchOpaqueShadow> shadow_opaque,
    final @Nonnull Map<KLight, BatchTranslucentShadow> shadow_translucent)
  {
    this.batches_opaque_lit = batches_opaque_lit;
    this.batches_translucent = batches_translucent;
    this.shadow_lights = shadow_lights;
    this.shadow_opaque = shadow_opaque;
    this.shadow_translucent = shadow_translucent;
  }

  @Nonnull
    Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>>
    getBatchesOpaqueLit()
  {
    return this.batches_opaque_lit;
  }

  @Nonnull List<BatchTranslucentLit> getBatchesTranslucent()
  {
    return this.batches_translucent;
  }

  @Nonnull Map<KLight, BatchOpaqueShadow> getShadowCastersOpaque()
  {
    return this.shadow_opaque;
  }

  @Nonnull Map<KLight, BatchTranslucentShadow> getShadowCastersTranslucent()
  {
    return this.shadow_translucent;
  }

  @Nonnull Set<KLight> getShadowLights()
  {
    return this.shadow_lights;
  }
}
