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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.Nullable;

/**
 * Information about the shadow casters in the current scene.
 */

@EqualityStructural public final class KSceneShadows
{
  private final Map<KLightID, KLightType>                           lights;
  private final Map<KLightID, List<KInstanceOpaqueType>> shadow_casters;

  KSceneShadows(
    final Map<KLightID, KLightType> in_lights,
    final Map<KLightID, List<KInstanceOpaqueType>> in_shadow_casters)
  {
    this.lights = in_lights;
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
    final KSceneShadows other = (KSceneShadows) obj;
    return this.lights.equals(other.lights)
      && this.shadow_casters.equals(other.shadow_casters);
  }

  /**
   * @return The lights present.
   */

  public Map<KLightID, KLightType> getLights()
  {
    return this.lights;
  }

  /**
   * @return The set of shadow casters for each light in the scene.
   */

  public
    Map<KLightID, List<KInstanceOpaqueType>>
    getShadowCasters()
  {
    return this.shadow_casters;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.lights.hashCode();
    result = (prime * result) + this.shadow_casters.hashCode();
    return result;
  }
}
