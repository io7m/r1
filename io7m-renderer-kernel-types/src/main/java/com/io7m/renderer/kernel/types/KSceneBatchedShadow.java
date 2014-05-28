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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.JCGLException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * A set of opaque instances for shadow map rendering, batched by their depth
 * material code.
 * 
 * @see com.io7m.renderer.kernel.types.KMaterialDepthType
 */

public final class KSceneBatchedShadow
{
  private static final KInstanceOpaqueVisitorType<String, UnreachableCodeException> GET_CODE;

  static {
    GET_CODE =
      new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
        @Override public String regular(
          final KInstanceOpaqueRegular io)
          throws RException
        {
          return io.getMaterial().materialOpaqueGetDepth().codeGet();
        }
      };
  }

  /**
   * Construct new batches from the given scene shadows.
   * 
   * @param s
   *          The scene shadows.
   * @return New batches.
   */

  public static KSceneBatchedShadow newBatches(
    final KSceneShadows s)
  {
    try {
      final Map<KLightType, Set<KInstanceOpaqueType>> ss =
        s.getShadowCasters();
      final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> batches =
        new HashMap<KLightType, Map<String, List<KInstanceOpaqueType>>>();

      for (final KLightType light : ss.keySet()) {
        assert light.lightHasShadow();
        final Set<KInstanceOpaqueType> instances = ss.get(light);

        final Map<String, List<KInstanceOpaqueType>> light_batches =
          new HashMap<String, List<KInstanceOpaqueType>>();

        for (final KInstanceOpaqueType i : instances) {
          final String code = i.opaqueAccept(KSceneBatchedShadow.GET_CODE);
          final List<KInstanceOpaqueType> batch;
          if (light_batches.containsKey(code)) {
            batch = light_batches.get(code);
          } else {
            batch = new ArrayList<KInstanceOpaqueType>();
          }
          batch.add(i);
          light_batches.put(code, batch);
        }

        batches.put(light, light_batches);
      }

      return new KSceneBatchedShadow(batches);

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> batches;

  private KSceneBatchedShadow(
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> in_batches)
  {
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> b =
      Collections.unmodifiableMap(in_batches);
    assert b != null;
    this.batches = b;
  }

  /**
   * @return A read-only view of the shadow casting instance batches, batched
   *         by light and by material code.
   */

  public Map<KLightType, Map<String, List<KInstanceOpaqueType>>> getBatches()
  {
    return this.batches;
  }
}
