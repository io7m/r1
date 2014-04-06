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

import javax.annotation.Nonnull;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaqueType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KScene;

/**
 * A scene with the instances batched for shadow map rendering.
 */

public final class KSceneBatchedShadow
{
  /**
   * Batch all of the given shadow-casting instances by material label.
   * 
   * @param shadows
   *          The shadow-casting instances
   * @param depth_labels
   *          The label cache
   * @return Batched instances
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public static @Nonnull KSceneBatchedShadow newBatchedScene(
    final @Nonnull KScene.KSceneShadows shadows,
    final @Nonnull KMaterialDepthLabelCacheType depth_labels)
    throws ConstraintError
  {
    Constraints.constrainNotNull(shadows, "Shadows");
    Constraints.constrainNotNull(depth_labels, "Depth label cache");

    final Map<KLightType, List<KInstanceTransformedOpaqueType>> casters =
      shadows.getShadowCasters();
    final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>> batched_casters =
      new HashMap<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>>();

    for (final KLightType light : casters.keySet()) {
      assert light.lightHasShadow();

      final List<KInstanceTransformedOpaqueType> instances =
        casters.get(light);
      final Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>> by_label =
        new HashMap<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>();

      for (final KInstanceTransformedOpaqueType i : instances) {
        final KMaterialDepthLabel label =
          depth_labels.getDepthLabel(i.instanceGet());

        List<KInstanceTransformedOpaqueType> label_casters = null;
        if (by_label.containsKey(label)) {
          label_casters = by_label.get(label);
        } else {
          label_casters = new ArrayList<KInstanceTransformedOpaqueType>();
        }

        label_casters.add(i);
        by_label.put(label, label_casters);
      }

      assert batched_casters.containsKey(light) == false;
      batched_casters.put(light, by_label);
    }

    return new KSceneBatchedShadow(batched_casters);
  }

  private final @Nonnull Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>> shadow_casters;

  private KSceneBatchedShadow(
    final @Nonnull Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>> in_shadow_casters)
  {
    this.shadow_casters = in_shadow_casters;
  }

  /**
   * @return All shadow casting instances in the scene, organized by label,
   *         for each light
   */

  public @Nonnull
    Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaqueType>>>
    getShadowCasters()
  {
    return this.shadow_casters;
  }
}
