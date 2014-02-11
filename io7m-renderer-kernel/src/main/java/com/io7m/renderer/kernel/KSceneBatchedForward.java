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
import com.io7m.renderer.kernel.types.KInstanceOpaque;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardRegularLabel;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KScene.KSceneOpaques;
import com.io7m.renderer.kernel.types.KTranslucent;

@Immutable final class KSceneBatchedForward
{
  /**
   * Given a set of opaque instances batched per light, return the same
   * opaques batched by light and material.
   */

  private static @Nonnull
    Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>>
    makeOpaqueLitBatches(
      final @Nonnull KMaterialForwardOpaqueLabelCache labels,
      final @Nonnull KSceneBatchedDepth.Builder depth_builder,
      final @Nonnull Map<KLight, List<KInstanceTransformedOpaque>> instances_by_light)
      throws ConstraintError
  {
    final Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>> batches_opaque_lit =
      new HashMap<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>>();

    for (final KLight light : instances_by_light.keySet()) {
      final List<KInstanceTransformedOpaque> instances =
        instances_by_light.get(light);

      for (int i = 0; i < instances.size(); ++i) {
        final KInstanceTransformedOpaque instance = instances.get(i);
        final KInstanceOpaque instance_i = instance.getInstance();
        final KMaterialForwardRegularLabel label =
          labels.getForwardLabelOpaque(instance_i);

        Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>> by_material;
        if (batches_opaque_lit.containsKey(light)) {
          by_material = batches_opaque_lit.get(light);
        } else {
          by_material =
            new HashMap<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>();
        }

        List<KInstanceTransformedOpaque> batch;
        if (by_material.containsKey(label)) {
          batch = by_material.get(label);
        } else {
          batch = new ArrayList<KInstanceTransformedOpaque>();
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
    Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>
    makeOpaqueUnlitBatches(
      final @Nonnull KMaterialForwardOpaqueLabelCache forward_labels,
      final @Nonnull KSceneBatchedDepth.Builder depth_builder,
      final @Nonnull Set<KInstanceTransformedOpaque> instances)
      throws ConstraintError
  {
    final HashMap<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>> forward_map =
      new HashMap<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>();

    for (final KInstanceTransformedOpaque instance : instances) {
      final KInstanceOpaque i = instance.getInstance();
      final KMaterialForwardRegularLabel forward_label =
        forward_labels.getForwardLabelOpaque(i);

      List<KInstanceTransformedOpaque> forward_batch;
      if (forward_map.containsKey(forward_label)) {
        forward_batch = forward_map.get(forward_label);
      } else {
        forward_batch = new ArrayList<KInstanceTransformedOpaque>();
      }
      forward_batch.add(instance);
      forward_map.put(forward_label, forward_batch);

      depth_builder.sceneAddInstance(instance);
    }

    return forward_map;
  }

  static @Nonnull KSceneBatchedForward newBatchedScene(
    final @Nonnull KMaterialDepthLabelCache depth_labels,
    final @Nonnull KMaterialForwardOpaqueLabelCache forward_labels,
    final @Nonnull KScene scene)
    throws ConstraintError
  {
    Constraints.constrainNotNull(forward_labels, "Labels");
    Constraints.constrainNotNull(scene, "Scene");

    final KSceneOpaques opaques = scene.getOpaques();

    final KSceneBatchedDepth.Builder depth_builder =
      KSceneBatchedDepth.newBuilder(depth_labels);

    final Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>> batches_opaque_lit =
      KSceneBatchedForward.makeOpaqueLitBatches(
        forward_labels,
        depth_builder,
        opaques.getLitInstances());

    final Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>> batches_opaque_unlit =
      KSceneBatchedForward.makeOpaqueUnlitBatches(
        forward_labels,
        depth_builder,
        opaques.getUnlitInstances());

    final KSceneBatchedShadow batched_shadow =
      KSceneBatchedShadow.newBatchedScene(scene.getShadows(), depth_labels);

    return new KSceneBatchedForward(
      batches_opaque_unlit,
      batches_opaque_lit,
      depth_builder.sceneCreate(),
      scene.getTranslucents(),
      batched_shadow);
  }

  private final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>                      batch_depth;
  private final @Nonnull Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>> batches_opaque_lit;
  private final @Nonnull Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>              batches_opaque_unlit;
  private final @Nonnull KSceneBatchedShadow                                                             shadows;
  private final @Nonnull List<KTranslucent>                                                              translucents;

  private KSceneBatchedForward(
    final @Nonnull Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>> batches_opaque_unlit,
    final @Nonnull Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>> batches_opaque_lit,
    final @Nonnull Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> batch_depth,
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
    Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>
    getBatchesDepth()
  {
    return this.batch_depth;
  }

  @Nonnull
    Map<KLight, Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>>
    getBatchesOpaqueLit()
  {
    return this.batches_opaque_lit;
  }

  @Nonnull
    Map<KMaterialForwardRegularLabel, List<KInstanceTransformedOpaque>>
    getBatchesOpaqueUnlit()
  {
    return this.batches_opaque_unlit;
  }

  @Nonnull List<KTranslucent> getBatchesTranslucent()
  {
    return this.translucents;
  }
}
