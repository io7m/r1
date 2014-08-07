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

package com.io7m.r1.kernel.types;

import java.util.Set;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;

/**
 * <p>
 * A set opaque instances lit by a set of lights.
 * </p>
 */

@EqualityReference public final class KSceneLightGroup
{
  private final Set<KInstanceOpaqueType> instances;
  private final Set<KLightType>          lights;
  private final String                   name;

  KSceneLightGroup(
    final String in_name,
    final Set<KLightType> in_lights,
    final Set<KInstanceOpaqueType> in_instances)
  {
    this.name = NullCheck.notNull(in_name, "Name");
    this.lights = NullCheck.notNull(in_lights, "Lights");
    this.instances = NullCheck.notNull(in_instances, "Instances");
  }

  /**
   * @return The set of instances in the group.
   */

  public Set<KInstanceOpaqueType> getInstances()
  {
    return this.instances;
  }

  /**
   * @return The set of lights in the group.
   */

  public Set<KLightType> getLights()
  {
    return this.lights;
  }

  /**
   * @return The name of the group.
   */

  public String getName()
  {
    return this.name;
  }
}
