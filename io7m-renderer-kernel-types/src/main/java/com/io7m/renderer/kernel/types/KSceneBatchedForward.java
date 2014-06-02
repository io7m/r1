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

package com.io7m.renderer.kernel.types;

import java.util.List;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * A scene, batched for forward rendering.
 */

@EqualityReference public final class KSceneBatchedForward
{
  /**
   * Construct a new batched scene from the given scene.
   * 
   * @param in_scene
   *          The scene.
   * @return A batched scene.
   */

  public static KSceneBatchedForward fromScene(
    final KScene in_scene)
  {
    NullCheck.notNull(in_scene, "Scene");

    final KSceneOpaques o = in_scene.getOpaques();
    final KSceneBatchedDepth in_depth = KSceneBatchedDepth.newBatches(o);
    final KSceneBatchedOpaque in_opaques = KSceneBatchedOpaque.newBatches(o);
    final KSceneBatchedShadow in_shadows =
      KSceneBatchedShadow.newBatches(in_scene.getShadows());

    return new KSceneBatchedForward(
      in_scene.getCamera(),
      in_depth,
      in_opaques,
      in_shadows,
      in_scene.getTranslucents());
  }

  private final KCamera                camera;
  private final KSceneBatchedDepth     depth;
  private final KSceneBatchedOpaque    opaques;
  private final KSceneBatchedShadow    shadows;
  private final List<KTranslucentType> translucents;

  private KSceneBatchedForward(
    final KCamera in_camera,
    final KSceneBatchedDepth in_depth,
    final KSceneBatchedOpaque in_opaques,
    final KSceneBatchedShadow in_shadows,
    final List<KTranslucentType> in_translucents)
  {
    this.camera = NullCheck.notNull(in_camera, "Camera");
    this.depth = NullCheck.notNull(in_depth, "Depth");
    this.opaques = NullCheck.notNull(in_opaques, "Opaques");
    this.shadows = NullCheck.notNull(in_shadows, "Shadows");
    this.translucents = NullCheck.notNull(in_translucents, "Translucents");
  }

  /**
   * @return The camera to use when rendering.
   */

  public KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @return The instances for depth-rendering, by material.
   */

  public KSceneBatchedDepth getDepthInstances()
  {
    return this.depth;
  }

  /**
   * @return The batched opaque instances.
   */

  public KSceneBatchedOpaque getOpaques()
  {
    return this.opaques;
  }

  /**
   * @return The instances for shadow map rendering.
   */

  public KSceneBatchedShadow getShadows()
  {
    return this.shadows;
  }

  /**
   * @return The translucent instances.
   */

  public List<KTranslucentType> getTranslucents()
  {
    return this.translucents;
  }
}
