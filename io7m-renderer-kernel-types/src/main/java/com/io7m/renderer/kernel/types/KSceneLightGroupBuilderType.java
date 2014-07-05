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

import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionLightGroupLacksInstances;
import com.io7m.renderer.types.RExceptionLightGroupLacksLights;

/**
 * The type of mutable builders for scene light groups.
 */

public interface KSceneLightGroupBuilderType
{
  /**
   * Add an instance to the group.
   * 
   * @param o
   *          The instance.
   * @throws RExceptionInstanceAlreadyUnlit
   *           If the given instance has already been added to the scene
   *           without lighting.
   */

  void groupAddInstance(
    final KInstanceOpaqueType o)
    throws RExceptionInstanceAlreadyUnlit;

  /**
   * Add a light to the group.
   * 
   * @param light
   *          The light.
   */

  void groupAddLight(
    final KLightType light);

  /**
   * @return A group consisting of all the instances and lights added so far.
   * 
   * @throws RExceptionLightGroupLacksInstances
   *           If no instances have been added.
   * @throws RExceptionLightGroupLacksLights
   *           If no lights have been added.
   */

  KSceneLightGroup groupBuild()
    throws RExceptionLightGroupLacksInstances,
      RExceptionLightGroupLacksLights;
}