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
import com.io7m.jnull.NullCheck;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;
import com.io7m.r1.exceptions.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.exceptions.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksInstances;
import com.io7m.r1.exceptions.RExceptionLightGroupLacksLights;
import com.io7m.r1.exceptions.RExceptionLightGroupNonexistent;
import com.io7m.r1.exceptions.RExceptionMaterialNonexistent;

/**
 * The set of visible instances.
 */

@EqualityReference public final class KVisibleSetOpaques
{
  @EqualityReference private static final class Builder implements
    KVisibleSetOpaquesBuilderWithCreateType
  {
    private final KCamera                                                 camera;
    private final boolean                                                 done;
    private final Map<String, KVisibleSetLightGroupBuilderWithCreateType> group_builders;
    private final KVisibleSetShadowsBuilderWithCreateType                 shadows;
    private final Map<String, List<KInstanceOpaqueType>>                  unlit_by_material;
    private final Set<KInstanceOpaqueType>                                visible;

    Builder(
      final KCamera in_camera,
      final KVisibleSetShadowsBuilderWithCreateType in_shadows)
    {
      this.unlit_by_material =
        new HashMap<String, List<KInstanceOpaqueType>>();
      this.group_builders =
        new HashMap<String, KVisibleSetLightGroupBuilderWithCreateType>();
      this.visible = new HashSet<KInstanceOpaqueType>();
      this.shadows = NullCheck.notNull(in_shadows, "Shadows");
      this.camera = NullCheck.notNull(in_camera, "Camera");
      this.done = false;
    }

    private void checkValid()
      throws RExceptionBuilderInvalid
    {
      if (this.done == true) {
        throw new RExceptionBuilderInvalid("Builder has been invalidated");
      }
    }

    @Override public void visibleOpaqueAddUnlit(
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
      final String code = KVisibleSet.getOpaqueMaterialUnlitCode(instance);
      if (this.unlit_by_material.containsKey(code)) {
        instances = this.unlit_by_material.get(code);
      } else {
        instances = new ArrayList<KInstanceOpaqueType>();
      }

      instances.add(instance);
      this.visible.add(instance);
      this.unlit_by_material.put(code, instances);
    }

    @SuppressWarnings("synthetic-access") @Override public
      KVisibleSetOpaques
      visibleOpaqueCreate()
        throws RExceptionBuilderInvalid,
          RExceptionLightGroupLacksInstances,
          RExceptionLightGroupLacksLights
    {
      final Set<String> names = this.group_builders.keySet();
      final Map<String, KVisibleSetLightGroup> groups =
        new HashMap<String, KVisibleSetLightGroup>();

      for (final String name : names) {
        final KVisibleSetLightGroupBuilderWithCreateType gb =
          this.group_builders.get(name);
        final KVisibleSetLightGroup g = gb.groupCreate();
        groups.put(name, g);
      }

      final KVisibleSetShadows s = this.shadows.visibleShadowsCreate();
      return new KVisibleSetOpaques(
        this.camera,
        s,
        this.unlit_by_material,
        groups);
    }

    @Override public
      KVisibleSetLightGroupBuilderType
      visibleOpaqueNewLightGroup(
        final String name)
        throws RExceptionLightGroupAlreadyAdded,
          RExceptionBuilderInvalid
    {
      NullCheck.notNull(name, "Name");
      this.checkValid();

      if (this.group_builders.containsKey(name)) {
        final String s = String.format("Group %s already exists", name);
        assert s != null;
        throw new RExceptionLightGroupAlreadyAdded(s);
      }

      final KVisibleSetLightGroupBuilderWithCreateType g =
        KVisibleSetLightGroup.newBuilder(name, this.shadows, this.visible);
      this.group_builders.put(name, g);
      return g;
    }

    @Override public void visibleShadowsAddCaster(
      final KLightWithShadowType light,
      final KInstanceOpaqueType instance)
      throws RExceptionBuilderInvalid
    {
      this.shadows.visibleShadowsAddCaster(light, instance);
    }

    @Override public void visibleShadowsAddLight(
      final KLightWithShadowType light)
      throws RExceptionBuilderInvalid
    {
      this.shadows.visibleShadowsAddLight(light);
    }
  }

  /**
   * @return A new opaques builder
   * @param in_camera
   *          The visible set camera
   * @param in_shadows
   *          A shadow set builder
   */

  public static KVisibleSetOpaquesBuilderWithCreateType newBuilder(
    final KCamera in_camera,
    final KVisibleSetShadowsBuilderWithCreateType in_shadows)
  {
    return new Builder(in_camera, in_shadows);
  }

  private final KCamera                                camera;
  private final Set<String>                            group_names;
  private final Map<String, KVisibleSetLightGroup>     groups;
  private final KVisibleSetShadows                     shadows;
  private final Map<String, List<KInstanceOpaqueType>> unlit_by_material;
  private final Set<String>                            unlit_codes;

  private KVisibleSetOpaques(
    final KCamera in_camera,
    final KVisibleSetShadows in_shadows,
    final Map<String, List<KInstanceOpaqueType>> in_unlit_by_material,
    final Map<String, KVisibleSetLightGroup> in_groups)
  {
    this.shadows = NullCheck.notNull(in_shadows, "Shadows");
    this.unlit_by_material = NullCheck.notNull(in_unlit_by_material, "Unlit");
    this.groups = NullCheck.notNull(in_groups, "Groups");
    this.unlit_codes =
      NullCheck.notNull(Collections.unmodifiableSet(in_unlit_by_material
        .keySet()));
    this.group_names =
      NullCheck.notNull(Collections.unmodifiableSet(this.groups.keySet()));
    this.camera = NullCheck.notNull(in_camera, "Camera");
  }

  /**
   * @return The camera for the visible set
   */

  public KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @param name
   *          The name
   * @return The light group with the given name
   * @throws RExceptionLightGroupNonexistent
   *           If the group does not exist
   */

  public KVisibleSetLightGroup getGroup(
    final String name)
    throws RExceptionLightGroupNonexistent
  {
    NullCheck.notNull(name, "Name");
    if (this.groups.containsKey(name)) {
      return NullCheck.notNull(this.groups.get(name));
    }

    final String s = String.format("Light group %s does not exist", name);
    assert s != null;
    throw new RExceptionLightGroupNonexistent(s);
  }

  /**
   * @return The set of light group names
   */

  public Set<String> getGroupNames()
  {
    return this.group_names;
  }

  /**
   * @return The set of shadow casting instances for the scene
   */

  public KVisibleSetShadows getShadows()
  {
    return this.shadows;
  }

  /**
   * @param code
   *          The material code
   * @return The list of instances with the given code
   * @throws RExceptionMaterialNonexistent
   *           If the code does not exist
   */

  @SuppressWarnings("null") public
    List<KInstanceOpaqueType>
    getUnlitInstancesByCode(
      final String code)
      throws RExceptionMaterialNonexistent
  {
    NullCheck.notNull(code, "Code");
    if (this.unlit_by_material.containsKey(code)) {
      return this.unlit_by_material.get(code);
    }
    throw new RExceptionMaterialNonexistent(String.format(
      "Material %s does not exist",
      code));
  }

  /**
   * @return The set of unlit material codes
   */

  public Set<String> getUnlitMaterialCodes()
  {
    return this.unlit_codes;
  }
}
