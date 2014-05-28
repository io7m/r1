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
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * A set of opaque instances batched by their depth material code.
 * 
 * @see com.io7m.renderer.kernel.types.KMaterialDepthType
 */

@EqualityReference public final class KSceneBatchedDepth
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
   * Construct batches for depth rendering from the given opaque instances.
   * 
   * @param opaques
   *          The opaque instances.
   * @return New batches.
   */

  public static KSceneBatchedDepth newBatches(
    final KSceneOpaques opaques)
  {
    try {
      final Map<String, List<KInstanceOpaqueType>> instances_by_code =
        new HashMap<String, List<KInstanceOpaqueType>>();

      final Set<KInstanceOpaqueType> unlit = opaques.getUnlitInstances();
      for (final KInstanceOpaqueType o : unlit) {
        final String code = o.opaqueAccept(KSceneBatchedDepth.GET_CODE);
        final List<KInstanceOpaqueType> instances;
        if (instances_by_code.containsKey(code)) {
          instances = instances_by_code.get(code);
        } else {
          instances = new ArrayList<KInstanceOpaqueType>();
        }
        instances.add(o);
        instances_by_code.put(code, instances);
      }

      final Map<KLightType, Set<KInstanceOpaqueType>> lit =
        opaques.getLitInstances();
      for (final KLightType l : lit.keySet()) {
        final Set<KInstanceOpaqueType> lit_instances = lit.get(l);

        for (final KInstanceOpaqueType o : lit_instances) {
          final String code = o.opaqueAccept(KSceneBatchedDepth.GET_CODE);
          final List<KInstanceOpaqueType> instances;
          if (instances_by_code.containsKey(code)) {
            instances = instances_by_code.get(code);
          } else {
            instances = new ArrayList<KInstanceOpaqueType>();
          }
          instances.add(o);
          instances_by_code.put(code, instances);
        }
      }

      return new KSceneBatchedDepth(instances_by_code);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final Map<String, List<KInstanceOpaqueType>> instances_by_code;

  private KSceneBatchedDepth(
    final Map<String, List<KInstanceOpaqueType>> ii)
  {
    final Map<String, List<KInstanceOpaqueType>> m =
      Collections.unmodifiableMap(ii);
    assert m != null;
    this.instances_by_code = m;
  }

  /**
   * @return A read-only view of the instances, batched by depth code.
   */

  public Map<String, List<KInstanceOpaqueType>> getInstancesByCode()
  {
    return this.instances_by_code;
  }
}
