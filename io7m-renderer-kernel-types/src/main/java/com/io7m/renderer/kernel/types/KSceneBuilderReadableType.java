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
import java.util.Map;
import java.util.Set;

/**
 * A readable interface to mutable scene builders.
 */

public interface KSceneBuilderReadableType
{
  /**
   * @return The scene's camera.
   */

  KCamera sceneGetCamera();

  /**
   * @return A read-only set of the instances present in the scene in any
   *         form.
   */

  Set<KInstanceType> sceneGetInstances();

  /**
   * @return A read-only set of the lit (visible) opaque instances present in
   *         the scene.
   */

  Set<KInstanceOpaqueType> sceneGetInstancesOpaqueLitVisible();

  /**
   * @return A read-only map of the current shadow-casting instances present
   *         in the scene, by light.
   */

    Map<KLightType, Set<KInstanceOpaqueType>>
    sceneGetInstancesOpaqueShadowCastingByLight();

  /**
   * @return A read-only set of the unlit opaque instances present in the
   *         scene.
   */

  Set<KInstanceOpaqueType> sceneGetInstancesOpaqueUnlit();

  /**
   * @return A read-only set of the lights present in the scene.
   */

  Set<KLightType> sceneGetLights();

  /**
   * @return A read-only set of the current shadow-casing lights in the scene.
   */

  Set<KLightWithShadowType> sceneGetLightsShadowCasting();

  /**
   * @return A list of the translucents in the scene, in the order in which
   *         they'll be rendered.
   */

  List<KTranslucentType> sceneGetTranslucents();
}
