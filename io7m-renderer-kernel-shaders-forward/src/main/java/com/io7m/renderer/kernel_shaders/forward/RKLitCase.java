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

package com.io7m.renderer.kernel_shaders.forward;

import com.io7m.renderer.kernel.types.KGraphicsCapabilitiesType;
import com.io7m.renderer.kernel.types.KLightType;
import com.io7m.renderer.kernel.types.KMaterialType;

public final class RKLitCase<M extends KMaterialType>
{
  private final FakeImmutableCapabilities caps;
  private final KLightType            light;
  private final M                     material;

  public RKLitCase(
    final KLightType in_light,
    final M in_material,
    final FakeImmutableCapabilities in_caps)
  {
    this.light = in_light;
    this.material = in_material;
    this.caps = in_caps;
  }

  public KGraphicsCapabilitiesType getCapabilities()
  {
    return this.caps;
  }

  public KLightType getLight()
  {
    return this.light;
  }

  public M getMaterial()
  {
    return this.material;
  }
}
