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
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Opaque scene instances, batched by light and material.
 */

public final class KSceneBatchedOpaque
{
  private static final KInstanceOpaqueVisitorType<String, UnreachableCodeException> GET_LIT_CODE   =
                                                                                                     KSceneBatchedOpaque
                                                                                                       .makeUnlitGetCode();
  private static final KInstanceOpaqueVisitorType<String, UnreachableCodeException> GET_UNLIT_CODE =
                                                                                                     KSceneBatchedOpaque
                                                                                                       .makeGetUnlitCode();

  private static
    KInstanceOpaqueVisitorType<String, UnreachableCodeException>
    makeGetUnlitCode()
  {
    return new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
      @Override public String regular(
        final KInstanceOpaqueRegular o)
        throws RException
      {
        return o.getMaterial().opaqueAccept(
          new KMaterialOpaqueVisitorType<String, UnreachableCodeException>() {
            @Override public String materialOpaqueRegular(
              final KMaterialOpaqueRegular m)
            {
              return m.materialUnlitGetCode();
            }
          });
      }
    };
  }

  private static
    Map<KLightType, Map<String, List<KInstanceOpaqueType>>>
    makeLitBatches(
      final KSceneOpaques o)
      throws RException,
        JCGLException
  {
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> lm =
      new HashMap<KLightType, Map<String, List<KInstanceOpaqueType>>>();

    final Map<KLightType, Set<KInstanceOpaqueType>> olit =
      o.getLitInstances();

    for (final KLightType light : olit.keySet()) {
      final Set<KInstanceOpaqueType> for_light = olit.get(light);
      final Map<String, List<KInstanceOpaqueType>> by_mat =
        new HashMap<String, List<KInstanceOpaqueType>>();

      for (final KInstanceOpaqueType i : for_light) {
        final String code = i.opaqueAccept(KSceneBatchedOpaque.GET_LIT_CODE);
        final List<KInstanceOpaqueType> instances;
        if (by_mat.containsKey(code)) {
          instances = by_mat.get(code);
        } else {
          instances = new ArrayList<KInstanceOpaqueType>();
        }
        instances.add(i);
        by_mat.put(code, instances);
      }

      lm.put(light, by_mat);
    }
    return lm;
  }

  private static Map<String, List<KInstanceOpaqueType>> makeUnlitBatches(
    final KSceneOpaques o)
    throws RException,
      JCGLException
  {
    final Map<String, List<KInstanceOpaqueType>> um =
      new HashMap<String, List<KInstanceOpaqueType>>();

    for (final KInstanceOpaqueType i : o.getUnlitInstances()) {
      final String code = i.opaqueAccept(KSceneBatchedOpaque.GET_UNLIT_CODE);
      List<KInstanceOpaqueType> instances;
      if (um.containsKey(code)) {
        instances = um.get(code);
      } else {
        instances = new ArrayList<KInstanceOpaqueType>();
      }
      instances.add(i);
      um.put(code, instances);
    }
    return um;
  }

  private static
    KInstanceOpaqueVisitorType<String, UnreachableCodeException>
    makeUnlitGetCode()
  {
    return new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
      @Override public String regular(
        final KInstanceOpaqueRegular o)
        throws RException
      {
        return o.getMaterial().opaqueAccept(
          new KMaterialOpaqueVisitorType<String, UnreachableCodeException>() {
            @Override public String materialOpaqueRegular(
              final KMaterialOpaqueRegular m)
            {
              return m.materialLitGetCodeWithoutDepth();
            }
          });
      }
    };
  }

  /**
   * Batch the given opaque instances.
   * 
   * @param o
   *          The instances.
   * @return Batched instances.
   */

  public static KSceneBatchedOpaque newBatches(
    final KSceneOpaques o)
  {
    NullCheck.notNull(o, "Opaques");

    try {
      final Map<String, List<KInstanceOpaqueType>> um =
        KSceneBatchedOpaque.makeUnlitBatches(o);
      final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> lm =
        KSceneBatchedOpaque.makeLitBatches(o);

      return new KSceneBatchedOpaque(lm, um);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> lit;
  private final Map<String, List<KInstanceOpaqueType>>                  unlit;

  private KSceneBatchedOpaque(
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> in_lit,
    final Map<String, List<KInstanceOpaqueType>> in_unlit)
  {
    this.lit = NullCheck.notNull(in_lit, "Lit");
    this.unlit = NullCheck.notNull(in_unlit, "Unlit");
  }

  /**
   * @return A read-only view of the lit instances, batched by light and
   *         material.
   */

  public Map<KLightType, Map<String, List<KInstanceOpaqueType>>> getLit()
  {
    final Map<KLightType, Map<String, List<KInstanceOpaqueType>>> r =
      Collections.unmodifiableMap(this.lit);
    assert r != null;
    return r;
  }

  /**
   * @return A read-only view of the unlit instances, batched by material.
   */

  public Map<String, List<KInstanceOpaqueType>> getUnlit()
  {
    final Map<String, List<KInstanceOpaqueType>> r =
      Collections.unmodifiableMap(this.unlit);
    assert r != null;
    return r;
  }
}
