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

package com.io7m.renderer.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueLitLabel;
import com.io7m.renderer.kernel.types.KMaterialForwardOpaqueUnlitLabel;
import com.io7m.renderer.kernel.types.KScene;
import com.io7m.renderer.kernel.types.KSceneOpaques;
import com.io7m.renderer.kernel.types.KTranslucentType;

@EqualityStructural final class KSceneBatchedForward
{
  /**
   * Given a set of opaque instances batched per light, return the same
   * opaques batched by light and material.
   */

  private static
    Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>>
    makeOpaqueLitBatches(
      final KMaterialForwardOpaqueLitLabelCacheType decider,
      final KSceneBatchedDepth.BuilderType depth_builder,
      final Map<KLightType, List<KInstanceTransformedOpaqueType>> instances_by_light)
  {
    final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> batches_opaque_lit =
      new HashMap<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>>();

    for (final KLightType light : instances_by_light.keySet()) {
      final List<KInstanceTransformedOpaqueType> instances =
        instances_by_light.get(light);

      for (int i = 0; i < instances.size(); ++i) {
        final KInstanceTransformedOpaqueType instance = instances.get(i);
        final KInstanceOpaqueType instance_i = instance.instanceGet();
        final KMaterialForwardOpaqueLitLabel label =
          decider.getForwardLabelOpaqueLit(light, instance_i);

        Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>> by_material;
        if (batches_opaque_lit.containsKey(light)) {
          by_material = batches_opaque_lit.get(light);
        } else {
          by_material =
            new HashMap<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>();
        }

        List<KInstanceTransformedOpaqueType> batch;
        if (by_material.containsKey(label)) {
          batch = by_material.get(label);
        } else {
          batch = new ArrayList<KInstanceTransformedOpaqueType>();
        }

        batch.add(instance);
        by_material.put(label, batch);
        batches_opaque_lit.put(light, by_material);

        depth_builder.sceneAddInstance(instance);
      }
    }

    return batches_opaque_lit;
  }

  private static
    Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>>
    makeOpaqueUnlitBatches(
      final KMaterialForwardOpaqueUnlitLabelCacheType decider,
      final KSceneBatchedDepth.BuilderType depth_builder,
      final Set<KInstanceTransformedOpaqueType> instances)
  {
    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>> forward_map =
      new HashMap<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>>();

    for (final KInstanceTransformedOpaqueType instance : instances) {
      final KInstanceOpaqueType i = instance.instanceGet();
      final KMaterialForwardOpaqueUnlitLabel label =
        decider.getForwardLabelOpaqueUnlit(i);

      List<KInstanceTransformedOpaqueType> forward_batch;
      if (forward_map.containsKey(label)) {
        forward_batch = forward_map.get(label);
      } else {
        forward_batch = new ArrayList<KInstanceTransformedOpaqueType>();
      }
      forward_batch.add(instance);
      forward_map.put(label, forward_batch);

      depth_builder.sceneAddInstance(instance);
    }

    return forward_map;
  }

  static
    <C extends KMaterialForwardOpaqueLitLabelCacheType & KMaterialForwardOpaqueUnlitLabelCacheType>
    KSceneBatchedForward
    newBatchedScene(
      final KMaterialDepthLabelCacheType depth_labels,
      final C forward_labels,
      final KScene scene)
  {
    NullCheck.notNull(forward_labels, "Labels");
    NullCheck.notNull(scene, "Scene");

    final KSceneOpaques opaques = scene.getOpaques();

    final KSceneBatchedDepth.BuilderType depth_builder =
      KSceneBatchedDepth.newBuilder(depth_labels);

    final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> batches_opaque_lit =
      KSceneBatchedForward.makeOpaqueLitBatches(
        forward_labels,
        depth_builder,
        opaques.getLitInstances());

    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>> batches_opaque_unlit =
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

  private final Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>                             batch_depth;
  private final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> batches_opaque_lit;
  private final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>>                batches_opaque_unlit;
  private final KSceneBatchedShadow                                                                        shadows;
  private final List<KTranslucentType>                                                                     translucents;

  private KSceneBatchedForward(
    final Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>> in_batches_opaque_unlit,
    final Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>> in_batches_opaque_lit,
    final Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> in_batch_depth,
    final List<KTranslucentType> in_translucents,
    final KSceneBatchedShadow in_shadows)
  {
    this.batches_opaque_unlit = in_batches_opaque_unlit;
    this.batches_opaque_lit = in_batches_opaque_lit;
    this.translucents = in_translucents;
    this.batch_depth = in_batch_depth;
    this.shadows = in_shadows;
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KSceneBatchedForward other = (KSceneBatchedForward) obj;
    return this.batch_depth.equals(other.batch_depth)
      && this.batches_opaque_lit.equals(other.batches_opaque_lit)
      && this.batches_opaque_unlit.equals(other.batches_opaque_unlit)
      && this.shadows.equals(other.shadows)
      && this.translucents.equals(other.translucents);
  }

  KSceneBatchedShadow getBatchedShadow()
  {
    return this.shadows;
  }

    Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>
    getBatchesDepth()
  {
    return this.batch_depth;
  }

    Map<KLightType, Map<KMaterialForwardOpaqueLitLabel, List<KInstanceTransformedOpaqueType>>>
    getBatchesOpaqueLit()
  {
    return this.batches_opaque_lit;
  }

    Map<KMaterialForwardOpaqueUnlitLabel, List<KInstanceTransformedOpaqueType>>
    getBatchesOpaqueUnlit()
  {
    return this.batches_opaque_unlit;
  }

  List<KTranslucentType> getBatchesTranslucent()
  {
    return this.translucents;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.batch_depth.hashCode();
    result = (prime * result) + this.batches_opaque_lit.hashCode();
    result = (prime * result) + this.batches_opaque_unlit.hashCode();
    result = (prime * result) + this.shadows.hashCode();
    result = (prime * result) + this.translucents.hashCode();
    return result;
  }
}
