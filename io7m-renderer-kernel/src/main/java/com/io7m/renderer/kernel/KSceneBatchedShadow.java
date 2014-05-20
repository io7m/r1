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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.types.KInstanceOpaqueType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KSceneShadows;

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
   */

  public static KSceneBatchedShadow newBatchedScene(
    final KSceneShadows shadows,
    final KMaterialDepthLabelCacheType depth_labels)
  {
    NullCheck.notNull(shadows, "Shadows");
    NullCheck.notNull(depth_labels, "Depth label cache");

    final Map<KLightType, List<KInstanceOpaqueType>> casters =
      shadows.getShadowCasters();
    final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>> batched_casters =
      new HashMap<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>>();

    for (final KLightType light : casters.keySet()) {
      assert light.lightHasShadow();

      final List<KInstanceOpaqueType> instances =
        casters.get(light);
      final Map<KMaterialDepthLabel, List<KInstanceOpaqueType>> by_label =
        new HashMap<KMaterialDepthLabel, List<KInstanceOpaqueType>>();

      for (final KInstanceOpaqueType i : instances) {
        final KMaterialDepthLabel label =
          depth_labels.getDepthLabel(i.instanceGetMeshWithMaterial());

        List<KInstanceOpaqueType> label_casters = null;
        if (by_label.containsKey(label)) {
          label_casters = by_label.get(label);
        } else {
          label_casters = new ArrayList<KInstanceOpaqueType>();
        }

        label_casters.add(i);
        by_label.put(label, label_casters);
      }

      assert batched_casters.containsKey(light) == false;
      batched_casters.put(light, by_label);
    }

    return new KSceneBatchedShadow(batched_casters);
  }

  private final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>> shadow_casters;

  private KSceneBatchedShadow(
    final Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>> in_shadow_casters)
  {
    this.shadow_casters = in_shadow_casters;
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
    final KSceneBatchedShadow other = (KSceneBatchedShadow) obj;
    return (this.shadow_casters.equals(other.shadow_casters));
  }

  /**
   * @return All shadow casting instances in the scene, organized by label,
   *         for each light
   */

  public
    Map<KLightType, Map<KMaterialDepthLabel, List<KInstanceOpaqueType>>>
    getShadowCasters()
  {
    return this.shadow_casters;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.shadow_casters.hashCode();
    return result;
  }
}
