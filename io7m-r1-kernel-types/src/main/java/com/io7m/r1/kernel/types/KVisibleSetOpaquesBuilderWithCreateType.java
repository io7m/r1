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

import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionLightGroupLacksInstances;
import com.io7m.r1.types.RExceptionLightGroupLacksLights;

/**
 * A {@link KVisibleSetOpaquesBuilderType} with a create function.
 */

public interface KVisibleSetOpaquesBuilderWithCreateType extends
  KVisibleSetOpaquesBuilderType
{
  /**
   * <p>
   * A set of opaque instances.
   * </p>
   *
   * @return A set of opaque instances.
   *
   * @throws RExceptionBuilderInvalid
   *           The builder has been invalidated.
   * @throws RExceptionLightGroupLacksLights
   *           A created light group contains no lights.
   * @throws RExceptionLightGroupLacksInstances
   *           A created light group contains no instances.
   */

  KVisibleSetOpaques visibleOpaqueCreate()
    throws RExceptionBuilderInvalid,
      RExceptionLightGroupLacksInstances,
      RExceptionLightGroupLacksLights;
}
