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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

/**
 * Opaque instances, batched for forward rendering.
 */

public final class KSceneBatchedForwardOpaque
{
  private static void addInstancesForLight(
    final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> lit_batches,
    final KLightType l,
    final Set<KInstanceOpaqueType> instances)
    throws RException,
      JCGLException
  {
    final Map<String, Set<KInstanceOpaqueType>> by_material;
    if (lit_batches.containsKey(l)) {
      by_material = lit_batches.get(l);
    } else {
      by_material = new HashMap<String, Set<KInstanceOpaqueType>>();
      lit_batches.put(l, by_material);
    }

    for (final KInstanceOpaqueType i : instances) {
      i
        .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit regular(
            final KInstanceOpaqueRegular o)
          {
            final String code =
              o.getMaterial().materialLitGetCodeWithoutDepth();
            Set<KInstanceOpaqueType> batch_instances;
            if (by_material.containsKey(code)) {
              batch_instances = by_material.get(code);
            } else {
              batch_instances = new HashSet<KInstanceOpaqueType>();
              by_material.put(code, batch_instances);
            }

            batch_instances.add(o);
            return Unit.unit();
          }
        });
    }
  }

  /**
   * Construct a new batched scene from the given scene.
   * 
   * @param in_scene
   *          The scene.
   * @return A batched scene.
   */

  public static KSceneBatchedForwardOpaque fromScene(
    final KScene in_scene)
  {
    NullCheck.notNull(in_scene, "Scene");

    try {
      final Map<String, KSceneLightGroup> light_groups =
        in_scene.getLightGroups();

      final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> lit_batches =
        KSceneBatchedForwardOpaque.makeLitBatches(light_groups);

      final Map<String, Set<KInstanceOpaqueType>> unlit_batches =
        KSceneBatchedForwardOpaque.makeUnlitBatches(in_scene
          .getUnlitOpaques());

      return new KSceneBatchedForwardOpaque(lit_batches, unlit_batches);

    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private static
    Map<KLightType, Map<String, Set<KInstanceOpaqueType>>>
    makeLitBatches(
      final Map<String, KSceneLightGroup> light_groups)
      throws RException,
        JCGLException
  {
    final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> lit_batches =
      new HashMap<KLightType, Map<String, Set<KInstanceOpaqueType>>>();

    for (final String name : light_groups.keySet()) {
      assert name != null;

      final KSceneLightGroup g = light_groups.get(name);
      final Set<KInstanceOpaqueType> instances = g.getInstances();

      for (final KLightType l : g.getLights()) {
        assert l != null;

        KSceneBatchedForwardOpaque.addInstancesForLight(
          lit_batches,
          l,
          instances);
      }
    }
    return lit_batches;
  }

  private static Map<String, Set<KInstanceOpaqueType>> makeUnlitBatches(
    final Set<KInstanceOpaqueType> unlits)
    throws RException,
      JCGLException
  {
    final Map<String, Set<KInstanceOpaqueType>> batches =
      new HashMap<String, Set<KInstanceOpaqueType>>();

    for (final KInstanceOpaqueType o : unlits) {
      o
        .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, UnreachableCodeException>() {
          @Override public Unit regular(
            final KInstanceOpaqueRegular or)
          {
            final String code =
              or.getMaterial().materialLitGetCodeWithoutDepth();
            Set<KInstanceOpaqueType> batch_instances;
            if (batches.containsKey(code)) {
              batch_instances = batches.get(code);
            } else {
              batch_instances = new HashSet<KInstanceOpaqueType>();
              batches.put(code, batch_instances);
            }

            batch_instances.add(or);
            return Unit.unit();
          }
        });
    }

    return batches;
  }

  private final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> lit_batches;
  private final Map<String, Set<KInstanceOpaqueType>>                  unlit_batches;

  private KSceneBatchedForwardOpaque(
    final Map<KLightType, Map<String, Set<KInstanceOpaqueType>>> in_lit_batches,
    final Map<String, Set<KInstanceOpaqueType>> in_unlit_batches)
  {
    this.lit_batches = in_lit_batches;
    this.unlit_batches = in_unlit_batches;
  }

  /**
   * @return The lit instances, batched by light and material.
   */

  public
    Map<KLightType, Map<String, Set<KInstanceOpaqueType>>>
    getLitBatches()
  {
    return this.lit_batches;
  }

  /**
   * @return The unlit instances, batched by material.
   */

  public Map<String, Set<KInstanceOpaqueType>> getUnlitBatches()
  {
    return this.unlit_batches;
  }
}
