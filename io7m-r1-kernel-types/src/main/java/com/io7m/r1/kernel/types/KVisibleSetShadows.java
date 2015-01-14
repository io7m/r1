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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionLightNonexistent;
import com.io7m.r1.exceptions.RExceptionMaterialNonexistent;

/**
 * The set of shadow-casting instances.
 */

@EqualityReference public final class KVisibleSetShadows
{
  @EqualityReference private static final class Builder implements
    KVisibleSetShadowsBuilderWithCreateType
  {
    private final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> casters_by_light_material;
    private boolean                                                                 done;

    Builder()
    {
      this.casters_by_light_material =
        new HashMap<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>>();
      this.done = false;
    }

    private void checkValid()
      throws RExceptionBuilderInvalid
    {
      if (this.done == true) {
        throw new RExceptionBuilderInvalid("Builder has been invalidated");
      }
    }

    @Override public void visibleShadowsAddCaster(
      final KLightWithShadowType light,
      final KInstanceOpaqueType instance)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");
      this.checkValid();

      Map<String, List<KInstanceOpaqueType>> by_material;
      if (this.casters_by_light_material.containsKey(light)) {
        by_material = this.casters_by_light_material.get(light);
      } else {
        by_material = new HashMap<String, List<KInstanceOpaqueType>>();
      }

      final String code = KVisibleSet.getOpaqueMaterialDepthCode(instance);
      List<KInstanceOpaqueType> instances;
      if (by_material.containsKey(code)) {
        instances = by_material.get(code);
      } else {
        instances = new ArrayList<KInstanceOpaqueType>();
      }

      instances.add(instance);
      by_material.put(code, instances);
      this.casters_by_light_material.put(light, by_material);
    }

    @SuppressWarnings("synthetic-access") @Override public
      KVisibleSetShadows
      visibleShadowsCreate()
        throws RExceptionBuilderInvalid
    {
      this.checkValid();
      this.done = true;
      return new KVisibleSetShadows(this.casters_by_light_material);
    }

    @Override public void visibleShadowsAddLight(
      final KLightWithShadowType light)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(light, "Light");
      this.checkValid();

      Map<String, List<KInstanceOpaqueType>> by_material;
      if (this.casters_by_light_material.containsKey(light)) {
        by_material = this.casters_by_light_material.get(light);
      } else {
        by_material = new HashMap<String, List<KInstanceOpaqueType>>();
      }

      this.casters_by_light_material.put(light, by_material);
    }
  }

  /**
   * @return A new builder for shadow-casting visible instance sets
   */

  public static KVisibleSetShadowsBuilderWithCreateType newBuilder()
  {
    return new Builder();
  }

  private final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> light_material;
  private final Set<KLightWithShadowType>                                         lights;

  private KVisibleSetShadows(
    final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> in_light_material)
  {
    this.light_material = NullCheck.notNull(in_light_material, "Instances");
    this.lights =
      NullCheck.notNull(Collections.unmodifiableSet(in_light_material
        .keySet()));
  }

  /**
   * @param in_light
   *          The light
   * @param code
   *          The material code
   * @return The list of instances with the given material code for the given
   *         light
   * @throws RExceptionMaterialNonexistent
   *           If the material does not exist
   * @throws RExceptionLightNonexistent
   *           If the light does not exist
   */

  public List<KInstanceOpaqueType> getInstances(
    final KLightWithShadowType in_light,
    final String code)
    throws RExceptionMaterialNonexistent,
      RExceptionLightNonexistent
  {
    NullCheck.notNull(in_light, "Light");
    NullCheck.notNull(code, "Material code");

    if (this.light_material.containsKey(in_light)) {
      final Map<String, List<KInstanceOpaqueType>> r =
        this.light_material.get(in_light);
      if (r.containsKey(code)) {
        return NullCheck.notNull(r.get(code));
      }

      final String ss = String.format("Material %s does not exist", code);
      assert ss != null;
      throw new RExceptionMaterialNonexistent(ss);
    }

    final String ss = String.format("Light %s does not exist", in_light);
    assert ss != null;
    throw new RExceptionLightNonexistent(ss);
  }

  /**
   * @param in_light
   *          The light
   * @return The instances as a {@link KDepthInstancesType}.
   * @throws RExceptionLightNonexistent
   *           If the light does not exist
   */

  public KDepthInstancesType getInstancesForLight(
    final KLightWithShadowType in_light)
    throws RExceptionLightNonexistent
  {
    NullCheck.notNull(in_light, "Light");

    final Map<KLightWithShadowType, Map<String, List<KInstanceOpaqueType>>> lm =
      this.light_material;

    if (lm.containsKey(in_light)) {
      final Map<String, List<KInstanceOpaqueType>> r = lm.get(in_light);
      final Set<String> ks =
        NullCheck.notNull(Collections.unmodifiableSet(r.keySet()));

      return new KDepthInstancesType() {
        @Override public List<KInstanceOpaqueType> getInstancesForMaterial(
          final String code)
          throws RExceptionMaterialNonexistent
        {
          if (r.containsKey(NullCheck.notNull(code, "Code"))) {
            return NullCheck.notNull(r.get(code));
          }

          final String ss = String.format("Material %s does not exist", code);
          assert ss != null;
          throw new RExceptionMaterialNonexistent(ss);
        }

        @Override public Set<String> getMaterialCodes()
        {
          return ks;
        }
      };
    }

    final String ss = String.format("Light %s does not exist", in_light);
    assert ss != null;
    throw new RExceptionLightNonexistent(ss);
  }

  /**
   * @return The shadow casting lights
   */

  public Set<KLightWithShadowType> getLights()
  {
    return this.lights;
  }

  /**
   * @return The set of materials used by the instances for the given light
   * @param in_light
   *          The light
   * @throws RExceptionLightNonexistent
   *           If the given light is not present
   */

  @SuppressWarnings("null") public Set<String> getMaterialsForLight(
    final KLightWithShadowType in_light)
    throws RExceptionLightNonexistent
  {
    NullCheck.notNull(in_light, "Light");

    if (this.light_material.containsKey(in_light)) {
      return NullCheck.notNull(Collections
        .unmodifiableSet(this.light_material.get(in_light).keySet()));
    }

    throw new RExceptionLightNonexistent(String.format(
      "Light %s does not exist",
      in_light));
  }
}
