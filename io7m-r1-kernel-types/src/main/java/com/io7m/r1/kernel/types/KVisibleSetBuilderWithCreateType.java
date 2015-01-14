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

import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksInstances;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksLights;

/**
 * An extension of the {@link KVisibleSetBuilderType} interface that allows
 * for final visible set creation.
 */

public interface KVisibleSetBuilderWithCreateType extends
  KVisibleSetBuilderType
{
  /**
   * Construct a {@link KVisibleSet} from the currently added instances and
   * light groups.
   *
   * @return A newly constructed scene
   * @throws RExceptionLightGroupLacksLights
   *           One or more light groups lack lights.
   * @throws RExceptionLightGroupLacksInstances
   *           One or more light groups lack instances.
   * @throws RExceptionBuilderInvalid
   *           If the builder has been invalidated.
   */

  KVisibleSet visibleCreate()
    throws RExceptionLightGroupLacksInstances,
      RExceptionLightGroupLacksLights,
      RExceptionBuilderInvalid;
}
