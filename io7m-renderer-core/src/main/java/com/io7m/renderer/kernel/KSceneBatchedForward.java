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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.KScene.KSceneTranslucents;

@Immutable final class KSceneBatchedForward
{
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

    final List<BatchTranslucentLit> batches_translucent =
      KSceneBatchedForward.makeTranslucentBatches(
        forward_labels,
        translucents);

    final KSceneBatchedShadow ksbs =
      KSceneBatchedShadow.newBatchedScene(scene.getShadows(), shadow_labels);

    return new KSceneBatchedForward(
      batches_opaque_lit,
      batches_translucent,
      ksbs);
  }

  private final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit;
  private final @Nonnull List<BatchTranslucentLit>                                               batches_translucent;
  private final @Nonnull KSceneBatchedShadow                                                     shadows;

  private KSceneBatchedForward(
    final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit,
    final @Nonnull List<BatchTranslucentLit> batches_translucent,
    final @Nonnull KSceneBatchedShadow shadows)
  {
    this.batches_opaque_lit = batches_opaque_lit;
    this.batches_translucent = batches_translucent;
    this.shadows = shadows;
  }

  @Nonnull KSceneBatchedShadow getBatchedShadow()
  {
    return this.shadows;
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
}
