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
import com.io7m.jaux.functional.Option.Some;

public final class KSceneBatchedShadow
{
  public static @Nonnull KSceneBatchedShadow newBatchedScene(
    final @Nonnull KScene.KSceneShadows shadows,
    final @Nonnull KMaterialShadowLabelCache shadow_labels)
    throws ConstraintError
  {
    final Map<KLight, List<KMeshInstanceTransformed>> casters =
      shadows.getShadowCasters();
    final Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>> batched_casters =
      new HashMap<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>>();

    for (final KLight light : casters.keySet()) {
      assert light.hasShadow();

      final List<KMeshInstanceTransformed> instances = casters.get(light);
      final HashMap<KMaterialShadowLabel, List<KMeshInstanceTransformed>> by_label =
        new HashMap<KMaterialShadowLabel, List<KMeshInstanceTransformed>>();

      for (final KMeshInstanceTransformed i : instances) {
        final Some<KShadow> shadow = (Some<KShadow>) light.getShadow();

        final KMaterialShadowLabel label =
          shadow_labels.getShadowLabel(i.getInstance(), shadow.value);

        List<KMeshInstanceTransformed> label_casters = null;
        if (by_label.containsKey(label)) {
          label_casters = by_label.get(label);
        } else {
          label_casters = new ArrayList<KMeshInstanceTransformed>();
        }

        label_casters.add(i);
        by_label.put(label, label_casters);
      }

      assert batched_casters.containsKey(light) == false;
      batched_casters.put(light, by_label);
    }

    return new KSceneBatchedShadow(batched_casters);
  }

  private final @Nonnull Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>> shadow_casters;

  private KSceneBatchedShadow(
    final @Nonnull Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>> shadow_casters)
  {
    this.shadow_casters = shadow_casters;
  }

  public @Nonnull
    Map<KLight, Map<KMaterialShadowLabel, List<KMeshInstanceTransformed>>>
    getShadowCasters()
  {
    return this.shadow_casters;
  }
}
