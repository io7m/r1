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
import com.io7m.renderer.kernel.KScene.KSceneOpaques;

@Immutable final class KSceneBatchedForward
{
  /**
   * Given a set of opaque instances batched per light, return the same
   * opaques batched by light and material.
   */

  private static @Nonnull
    Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>>
    makeOpaqueLitBatches(
      final @Nonnull KMaterialForwardLabelCache labels,
      final @Nonnull KSceneBatchedDepth.Builder depth_builder,
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
        final KMeshInstance instance_i = instance.getInstance();
        final KMaterialForwardLabel label =
          labels.getForwardLabel(instance_i);

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

        depth_builder.sceneAddInstance(instance);
      }
    }

    return batches_opaque_lit;
  }

  private static @Nonnull
    Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>
    makeOpaqueUnlitBatches(
      final @Nonnull KMaterialForwardLabelCache forward_labels,
      final @Nonnull KSceneBatchedDepth.Builder depth_builder,
      final @Nonnull Set<KMeshInstanceTransformed> instances)
      throws ConstraintError
  {
    final HashMap<KMaterialForwardLabel, List<KMeshInstanceTransformed>> forward_map =
      new HashMap<KMaterialForwardLabel, List<KMeshInstanceTransformed>>();

    for (final KMeshInstanceTransformed instance : instances) {
      final KMeshInstance i = instance.getInstance();
      final KMaterialForwardLabel forward_label =
        forward_labels.getForwardLabel(i);

      List<KMeshInstanceTransformed> forward_batch;
      if (forward_map.containsKey(forward_label)) {
        forward_batch = forward_map.get(forward_label);
      } else {
        forward_batch = new ArrayList<KMeshInstanceTransformed>();
      }
      forward_batch.add(instance);
      forward_map.put(forward_label, forward_batch);

      depth_builder.sceneAddInstance(instance);
    }

    return forward_map;
  }

  static @Nonnull KSceneBatchedForward newBatchedScene(
    final @Nonnull KMaterialShadowLabelCache shadow_labels,
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull KMaterialForwardLabelCache forward_labels,
    final @Nonnull KScene scene)
    throws ConstraintError
  {
    Constraints.constrainNotNull(forward_labels, "Labels");
    Constraints.constrainNotNull(scene, "Scene");

    final KSceneOpaques opaques = scene.getOpaques();

    final KSceneBatchedDepth.Builder depth_builder =
      KSceneBatchedDepth.newBuilder(depth_labels);

    final Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit =
      KSceneBatchedForward.makeOpaqueLitBatches(
        forward_labels,
        depth_builder,
        opaques.getLitInstances());

    final Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> batches_opaque_unlit =
      KSceneBatchedForward.makeOpaqueUnlitBatches(
        forward_labels,
        depth_builder,
        opaques.getUnlitInstances());

    final KSceneBatchedShadow batched_shadow =
      KSceneBatchedShadow.newBatchedScene(scene.getShadows(), shadow_labels);

    return new KSceneBatchedForward(
      batches_opaque_unlit,
      batches_opaque_lit,
      depth_builder.sceneCreate(),
      scene.getTranslucents(),
      batched_shadow);
  }

  private final @Nonnull Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>>                batch_depth;
  private final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit;
  private final @Nonnull Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>              batches_opaque_unlit;
  private final @Nonnull KSceneBatchedShadow                                                     shadows;
  private final @Nonnull List<KTranslucent>                                                      translucents;

  private KSceneBatchedForward(
    final @Nonnull Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>> batches_opaque_unlit,
    final @Nonnull Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>> batches_opaque_lit,
    final @Nonnull Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>> batch_depth,
    final @Nonnull List<KTranslucent> translucents,
    final @Nonnull KSceneBatchedShadow shadows)
  {
    this.batches_opaque_unlit = batches_opaque_unlit;
    this.batches_opaque_lit = batches_opaque_lit;
    this.translucents = translucents;
    this.batch_depth = batch_depth;
    this.shadows = shadows;
  }

  @Nonnull KSceneBatchedShadow getBatchedShadow()
  {
    return this.shadows;
  }

  @Nonnull
    Map<KMaterialDepthLabel, List<KMeshInstanceTransformed>>
    getBatchesDepth()
  {
    return this.batch_depth;
  }

  @Nonnull
    Map<KLight, Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>>
    getBatchesOpaqueLit()
  {
    return this.batches_opaque_lit;
  }

  @Nonnull
    Map<KMaterialForwardLabel, List<KMeshInstanceTransformed>>
    getBatchesOpaqueUnlit()
  {
    return this.batches_opaque_unlit;
  }

  @Nonnull List<KTranslucent> getBatchesTranslucent()
  {
    return this.translucents;
  }
}
