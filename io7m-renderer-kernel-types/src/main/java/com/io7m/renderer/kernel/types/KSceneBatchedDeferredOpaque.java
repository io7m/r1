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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.types.KSceneBatchedCommon.BatchUseCode;
import com.io7m.renderer.types.RException;

/**
 * Opaque instances, batched for deferred rendering.
 */

@EqualityReference public final class KSceneBatchedDeferredOpaque
{
  /**
   * A group of opaque instances, batched by material, lit by a set of lights.
   */

  @EqualityReference public static final class Group
  {
    private final Map<String, Set<KInstanceOpaqueType>> instances;
    private final Set<KLightType>                       lights;

    private Group(
      final Set<KLightType> in_lights,
      final Map<String, Set<KInstanceOpaqueType>> in_instances)
    {
      this.lights = in_lights;
      this.instances = in_instances;
    }

    /**
     * @return The instances, batched by material.
     */

    public Map<String, Set<KInstanceOpaqueType>> getInstances()
    {
      return this.instances;
    }

    /**
     * @return The set of lights for the group.
     */

    public Set<KLightType> getLights()
    {
      return this.lights;
    }
  }

  /**
   * Construct a new batched scene from the given scene.
   *
   * @param in_scene
   *          The scene.
   * @return A batched scene.
   */

  @SuppressWarnings("synthetic-access") public static
    KSceneBatchedDeferredOpaque
    fromScene(
      final KScene in_scene)
  {
    NullCheck.notNull(in_scene, "Scene");

    try {
      final Map<String, KSceneLightGroup> g = in_scene.getLightGroups();
      final List<Group> in_groups = new ArrayList<Group>();

      for (final String name : g.keySet()) {
        assert name != null;
        final KSceneLightGroup lg = g.get(name);
        assert lg != null;

        final Map<String, Set<KInstanceOpaqueType>> by_material =
          new HashMap<String, Set<KInstanceOpaqueType>>();

        for (final KInstanceOpaqueType i : lg.getInstances()) {
          i
            .opaqueAccept(new KInstanceOpaqueVisitorType<Unit, UnreachableCodeException>() {
              @Override public Unit regular(
                final KInstanceOpaqueRegular o)
              {
                final String code = o.getMaterial().materialDeferredGetCode();

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

        in_groups.add(new Group(lg.getLights(), by_material));
      }

      /**
       * The deferred renderers want the lit code with depth in order to
       * select the correct geometry-pass shaders.
       */

      final Map<String, Set<KInstanceOpaqueType>> in_unlit =
        KSceneBatchedCommon.makeUnlitBatches(
          in_scene.getUnlitOpaques(),
          BatchUseCode.BATCH_USE_LIT_CODE_WITH_DEPTH);

      return new KSceneBatchedDeferredOpaque(in_groups, in_unlit);
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final JCGLException e) {
      throw new UnreachableCodeException(e);
    }
  }

  private final List<Group>                           groups;
  private final Map<String, Set<KInstanceOpaqueType>> unlit;

  private KSceneBatchedDeferredOpaque(
    final List<Group> in_groups,
    final Map<String, Set<KInstanceOpaqueType>> in_unlit)
  {
    this.groups = in_groups;
    this.unlit = in_unlit;
  }

  /**
   * @return The list of groups.
   */

  public List<Group> getGroups()
  {
    return this.groups;
  }

  /**
   * @return The unlit instances.
   */

  public Map<String, Set<KInstanceOpaqueType>> getUnlit()
  {
    return this.unlit;
  }
}
