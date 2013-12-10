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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.KScene.KSceneShadows;

public final class KSceneBatchedShadow
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
    Map<KLight, BatchTranslucentShadow>
    makeTranslucentShadowCasters(
      final @Nonnull KSceneShadows shadows)
  {
    final Map<KLight, List<KMeshInstanceTransformed>> casters =
      shadows.getTranslucentShadowCasters();

    final HashMap<KLight, BatchTranslucentShadow> batches =
      new HashMap<KLight, BatchTranslucentShadow>();

    for (final KLight light : casters.keySet()) {
      final List<KMeshInstanceTransformed> instances = casters.get(light);
      batches.put(light, new BatchTranslucentShadow(light, instances));
    }

    return batches;
  }

  public static @Nonnull KSceneBatchedShadow newBatchedScene(
    final @Nonnull KScene.KSceneShadows shadows,
    final @Nonnull KMaterialShadowLabelCache shadow_labels)
    throws ConstraintError
  {
    final Map<KLight, BatchOpaqueShadow> shadow_opaque =
      KSceneBatchedShadow.makeOpaqueShadowCasters(
        shadow_labels,
        shadows.getOpaqueShadowCasters());

    final Map<KLight, BatchTranslucentShadow> shadow_translucent =
      KSceneBatchedShadow.makeTranslucentShadowCasters(shadows);

    return new KSceneBatchedShadow(
      shadows.getShadowLights(),
      shadow_opaque,
      shadow_translucent);
  }

  private final @Nonnull Map<KLight, BatchOpaqueShadow>      shadow_opaque;
  private final @Nonnull Map<KLight, BatchTranslucentShadow> shadow_translucent;
  private final @Nonnull Set<KLight>                         shadow_lights;

  private KSceneBatchedShadow(
    final @Nonnull Set<KLight> shadow_lights,
    final @Nonnull Map<KLight, BatchOpaqueShadow> shadow_opaque,
    final @Nonnull Map<KLight, BatchTranslucentShadow> shadow_translucent)
  {
    this.shadow_opaque = shadow_opaque;
    this.shadow_translucent = shadow_translucent;
    this.shadow_lights = shadow_lights;
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
