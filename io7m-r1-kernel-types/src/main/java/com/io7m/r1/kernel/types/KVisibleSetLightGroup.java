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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksInstances;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksLights;

/**
 * An immutable light group.
 */

@SuppressWarnings("synthetic-access") @EqualityReference public final class KVisibleSetLightGroup
{
  @EqualityReference private static final class Builder implements
    KVisibleSetLightGroupBuilderWithCreateType
  {
    private final Map<String, List<KInstanceOpaqueType>> by_material;
    private boolean                                      done;
    private @Nullable KLightAmbientType                  light_ambient;
    private final Set<KLightLocalType>                   lights;
    private final String                                 name;
    private final KVisibleSetShadowsBuilderType          shadows;
    private final Set<KInstanceOpaqueType>               visible;

    Builder(
      final String in_name,
      final KVisibleSetShadowsBuilderType in_shadow_builder,
      final Set<KInstanceOpaqueType> in_visible)
    {
      this.done = false;
      this.visible = NullCheck.notNull(in_visible, "Visible");
      this.by_material = new HashMap<String, List<KInstanceOpaqueType>>();
      this.lights = new HashSet<KLightLocalType>();
      this.light_ambient = null;
      this.shadows = NullCheck.notNull(in_shadow_builder, "Shadow builder");
      this.name = NullCheck.notNull(in_name, "Name");
    }

    private void checkValid()
      throws RExceptionBuilderInvalid
    {
      if (this.done == true) {
        throw new RExceptionBuilderInvalid("Builder has been invalidated");
      }
    }

    @Override public void groupAddInstance(
      final KInstanceOpaqueType instance)
      throws RExceptionBuilderInvalid,
        RExceptionInstanceAlreadyVisible
    {
      NullCheck.notNull(instance, "Instance");
      this.checkValid();

      if (this.visible.contains(instance)) {
        final String ss =
          String.format("Instance %s is already visible", instance);
        assert ss != null;
        throw new RExceptionInstanceAlreadyVisible(ss);
      }

      List<KInstanceOpaqueType> instances;
      final String code = KVisibleSet.getOpaqueMaterialLitCode(instance);
      if (this.by_material.containsKey(code)) {
        instances = this.by_material.get(code);
      } else {
        instances = new ArrayList<KInstanceOpaqueType>();
      }

      instances.add(instance);
      this.visible.add(instance);
      this.by_material.put(code, instances);
    }

    @Override public void groupAddLight(
      final KLightLocalType light)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(light, "Light");
      this.checkValid();
      this.lights.add(light);

      if (light instanceof KLightWithShadowType) {
        this.shadows.visibleShadowsAddLight((KLightWithShadowType) light);
      }
    }

    @Override public KVisibleSetLightGroup groupCreate()
      throws RExceptionLightGroupLacksInstances,
        RExceptionLightGroupLacksLights,
        RExceptionBuilderInvalid
    {
      this.checkValid();
      this.done = true;
      return new KVisibleSetLightGroup(
        this.name,
        this.by_material,
        this.lights,
        Option.of(this.light_ambient));
    }

    @Override public void groupSetAmbientLight(
      final KLightAmbientType light)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(light, "Ambient light");
      this.checkValid();
      this.light_ambient = light;
    }
  }

  /**
   * Construct a new light group builder.
   *
   * @param in_name
   *          The name of the group
   * @param in_visible
   *          The currently visible opaque instances
   * @param in_shadow_builder
   *          The current shadow set builder
   * @return A new light group builder
   */

  public static KVisibleSetLightGroupBuilderWithCreateType newBuilder(
    final String in_name,
    final KVisibleSetShadowsBuilderType in_shadow_builder,
    final Set<KInstanceOpaqueType> in_visible)
  {
    return new Builder(in_name, in_shadow_builder, in_visible);
  }

  private final Map<String, List<KInstanceOpaqueType>> by_material;
  private final Set<String>                            codes;
  private final OptionType<KLightAmbientType>          light_ambient;
  private final Set<KLightLocalType>                   lights;
  private final String                                 name;

  private KVisibleSetLightGroup(
    final String in_name,
    final Map<String, List<KInstanceOpaqueType>> in_by_material,
    final Set<KLightLocalType> in_lights,
    final OptionType<KLightAmbientType> in_light_ambient)
  {
    this.name = NullCheck.notNull(in_name, "Name");
    this.by_material = NullCheck.notNull(in_by_material, "By material");
    this.codes =
      NullCheck
        .notNull(Collections.unmodifiableSet(this.by_material.keySet()));
    this.lights =
      NullCheck.notNull(Collections.unmodifiableSet(NullCheck.notNull(
        in_lights,
        "Lights")));
    this.light_ambient = NullCheck.notNull(in_light_ambient, "Ambient");
  }

  /**
   * @param code
   *          The material code
   * @return A list of instances with the given material code
   */

  public List<KInstanceOpaqueType> getInstances(
    final String code)
  {
    return NullCheck.notNull(Collections.unmodifiableList(this.by_material
      .get(NullCheck.notNull(code, "Code"))));
  }

  /**
   * @return The ambient light for the group, if any.
   */

  public OptionType<KLightAmbientType> getLightAmbient()
  {
    return this.light_ambient;
  }

  /**
   * @return The set of lights in the group.
   */

  public Set<KLightLocalType> getLights()
  {
    return this.lights;
  }

  /**
   * @return The material codes for all instances in the group.
   */

  public Set<String> getMaterialCodes()
  {
    return this.codes;
  }

  /**
   * @return The name of the group.
   */

  public String getName()
  {
    return this.name;
  }
}
