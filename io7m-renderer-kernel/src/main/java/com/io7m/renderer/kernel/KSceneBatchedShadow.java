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

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.types.KInstanceTransformedOpaque;
import com.io7m.renderer.kernel.types.KLight;
import com.io7m.renderer.kernel.types.KMaterialDepthLabel;
import com.io7m.renderer.kernel.types.KScene;

public final class KSceneBatchedShadow
{
  public static @Nonnull KSceneBatchedShadow newBatchedScene(
    final @Nonnull KScene.KSceneShadows shadows,
    final @Nonnull KMaterialDepthLabelCache depth_labels)
    throws ConstraintError
  {
    final Map<KLight, List<KInstanceTransformedOpaque>> casters =
      shadows.getShadowCasters();
    final Map<KLight, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>> batched_casters =
      new HashMap<KLight, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>>();

    for (final KLight light : casters.keySet()) {
      assert light.lightHasShadow();

      final List<KInstanceTransformedOpaque> instances = casters.get(light);
      final HashMap<KMaterialDepthLabel, List<KInstanceTransformedOpaque>> by_label =
        new HashMap<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>();

      for (final KInstanceTransformedOpaque i : instances) {
        final KMaterialDepthLabel label =
          depth_labels.getDepthLabel(i.instanceGet());

        List<KInstanceTransformedOpaque> label_casters = null;
        if (by_label.containsKey(label)) {
          label_casters = by_label.get(label);
        } else {
          label_casters = new ArrayList<KInstanceTransformedOpaque>();
        }

        label_casters.add(i);
        by_label.put(label, label_casters);
      }

      assert batched_casters.containsKey(light) == false;
      batched_casters.put(light, by_label);
    }

    return new KSceneBatchedShadow(batched_casters);
  }

  private final @Nonnull Map<KLight, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>> shadow_casters;

  private KSceneBatchedShadow(
    final @Nonnull Map<KLight, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>> shadow_casters)
  {
    this.shadow_casters = shadow_casters;
  }

  public @Nonnull
    Map<KLight, Map<KMaterialDepthLabel, List<KInstanceTransformedOpaque>>>
    getShadowCasters()
  {
    return this.shadow_casters;
  }
}
