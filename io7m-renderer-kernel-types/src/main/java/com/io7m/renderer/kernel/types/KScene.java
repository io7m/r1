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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionInstanceAlreadyInGroup;
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.renderer.types.RExceptionLightGroupLacksInstances;
import com.io7m.renderer.types.RExceptionLightGroupLacksLights;

/**
 * <p>
 * An immutable scene.
 * </p>
 * <p>
 * The <code>KScene</code> type separates opaque and translucent objects and
 * batches them by <i>lights</i> for rendering (it is the responsibility of
 * renderers to do additional per-material batching). A scene consists of:
 * </p>
 * <ul>
 * <li>Opaque instances that will appear in the final rendered image</li>
 * <li>Translucent instances that will appear in the final rendered image</li>
 * <li>Opaque shadow-casting instances that may not appear in the final
 * rendered image</li>
 * </ul>
 * <p>
 * Due to depth buffering, opaque objects may be rendered in any order without
 * affecting the final image. Consequently, opaque shadow-casting objects may
 * also be rendered in any order with respect to their lights. Translucent
 * objects, however, must be rendered in a specific order (typically, objects
 * furthest from the observer will be rendered prior to nearer objects).
 * </p>
 * <p>
 * The <code>KScene</code> class preserves the order of insertion for
 * translucent objects, and delegates responsibility for doing spatial
 * partitioning and sorting objects by position to the creator of the scene.
 * </p>
 */

@EqualityReference public final class KScene
{
  @SuppressWarnings({ "null", "unchecked", "synthetic-access" }) @EqualityReference private static final class Builder implements
    KSceneBuilderWithCreateType
  {
    private static RExceptionLightGroupAlreadyAdded lightGroupAlreadyAdded(
      final String name)
    {
      final StringBuilder m = new StringBuilder();
      m.append("The light group '");
      m.append(name);
      m.append("' has already been added to the scene");
      return new RExceptionLightGroupAlreadyAdded(m.toString());
    }

    private final KCamera       camera;
    private final StringBuilder message;
    private BuilderState        state;

    Builder(
      final KCamera in_camera)
    {
      this.camera = NullCheck.notNull(in_camera, "Camera");
      this.state = new BuilderState();
      this.message = new StringBuilder();
    }

    private RExceptionInstanceAlreadyInGroup alreadyInGroup(
      final KInstanceOpaqueType o,
      final String existing_group,
      final String new_group)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance to a group, but the instance has already been added previously to a light group.\n");
      m.append("  Instance: ");
      m.append(o);
      m.append("\n");
      m.append("  Existing group: ");
      m.append(existing_group);
      m.append("\n");
      m.append("  New group: ");
      m.append(new_group);
      m.append("\n");
      return new RExceptionInstanceAlreadyInGroup(m.toString());
    }

    private RExceptionInstanceAlreadyLit alreadyLit(
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance as unlit, but the instance has already been added previously to a light group.\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");

      final HashPMap<String, LightGroupBuilder> map =
        this.state.light_group_builders;
      for (final String name : map.keySet()) {
        final LightGroupBuilder g = map.get(name);
        final Set<KInstanceOpaqueType> instances = g.group_state.instances;
        if (instances.contains(instance)) {
          m.append("  Light group: ");
          m.append(name);
          m.append("\n");
        }
      }

      return new RExceptionInstanceAlreadyLit(m.toString());
    }

    private RExceptionInstanceAlreadyUnlit alreadyUnlit(
      final String group_name,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance to a light group but the instance has already been added previously as unlit.\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      m.append("  Light group: ");
      m.append(group_name);
      m.append("\n");
      return new RExceptionInstanceAlreadyUnlit(m.toString());
    }

    @Override public void sceneAddOpaqueUnlit(
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyLit
    {
      NullCheck.notNull(instance, "Instance");

      final BuilderState previous = this.state;
      if (previous.instances_opaque_lit.contains(instance)) {
        throw this.alreadyLit(instance);
      }

      this.state =
        new BuilderState(
          previous.instances_shadow,
          previous.instances_all.plus(instance),
          previous.instances_opaque_lit,
          previous.instances_opaque_unlit.plus(instance),
          previous.lights_all,
          previous.lights_shadow,
          previous.translucents_ordered,
          previous.instances_visible.plus(instance),
          previous.light_group_builders,
          previous.groups_by_instance);
    }

    @Override public void sceneAddShadowCaster(
      final KLightWithShadowType light,
      final KInstanceOpaqueType instance)
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      MapPSet<KInstanceOpaqueType> instances_shadow =
        this.state.instances_shadow.get(light);
      if (instances_shadow == null) {
        instances_shadow = HashTreePSet.singleton(instance);
      } else {
        instances_shadow = instances_shadow.plus(instance);
      }

      final BuilderState previous = this.state;

      this.state =
        new BuilderState(
          previous.instances_shadow.plus(light, instances_shadow),
          previous.instances_all.plus(instance),
          previous.instances_opaque_lit,
          previous.instances_opaque_unlit,
          previous.lights_all.plus(light),
          previous.lights_shadow.plus(light),
          previous.translucents_ordered,
          previous.instances_visible,
          previous.light_group_builders,
          previous.groups_by_instance);
    }

    @Override public void sceneAddTranslucentLit(
      final KInstanceTranslucentLitType instance,
      final Set<KLightType> instance_lights)
    {
      NullCheck.notNull(instance, "Instance");
      NullCheck.notNullAll(instance_lights, "Lights");

      try {
        final KInstanceTranslucentLitVisitorType<KTranslucentType, UnreachableCodeException> visitor =
          new KInstanceTranslucentLitVisitorType<KTranslucentType, UnreachableCodeException>() {
            @Override public KTranslucentType regular(
              final KInstanceTranslucentRegular i)
              throws RException
            {
              return new KTranslucentRegularLit(i, instance_lights);
            }

            @Override public KTranslucentType specularOnly(
              final KInstanceTranslucentSpecularOnly i)
              throws RException
            {
              return new KTranslucentSpecularOnlyLit(i, instance_lights);
            }
          };

        final KTranslucentType translucent =
          instance.translucentLitAccept(visitor);

        final BuilderState previous = this.state;

        this.state =
          new BuilderState(
            previous.instances_shadow,
            previous.instances_all.plus(instance),
            previous.instances_opaque_lit,
            previous.instances_opaque_unlit,
            previous.lights_all.plusAll(instance_lights),
            previous.lights_shadow,
            previous.translucents_ordered.plus(translucent),
            previous.instances_visible.plus(instance),
            previous.light_group_builders,
            previous.groups_by_instance);

      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public void sceneAddTranslucentUnlit(
      final KInstanceTranslucentUnlitType instance)
    {
      NullCheck.notNull(instance, "Instance");

      try {
        final KInstanceTranslucentUnlitVisitorType<KTranslucentType, UnreachableCodeException> visitor =
          new KInstanceTranslucentUnlitVisitorType<KTranslucentType, UnreachableCodeException>() {
            @Override public KTranslucentType refractive(
              final KInstanceTranslucentRefractive i)
              throws RException
            {
              return i;
            }

            @Override public KTranslucentType regular(
              final KInstanceTranslucentRegular i)
              throws RException
            {
              return i;
            }
          };

        final KTranslucentType translucent =
          instance.translucentUnlitAccept(visitor);

        final BuilderState previous = this.state;

        this.state =
          new BuilderState(
            previous.instances_shadow,
            previous.instances_all.plus(instance),
            previous.instances_opaque_lit,
            previous.instances_opaque_unlit,
            previous.lights_all,
            previous.lights_shadow,
            previous.translucents_ordered.plus(translucent),
            previous.instances_visible.plus(instance),
            previous.light_group_builders,
            previous.groups_by_instance);

      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public KScene sceneCreate()
      throws RExceptionLightGroupLacksInstances,
        RExceptionLightGroupLacksLights
    {
      final List<KTranslucentType> in_translucents =
        this.state.translucents_ordered;

      /**
       * If a light with a shadow has been added, but the light doesn't
       * actually have any shadow casters, then add an empty set of instances
       * (because the light still needs to have its shadow map initialized
       * each frame, and the shadow map renderer works only with the shadow
       * instances map).
       */

      for (final KLightWithShadowType l : this.state.lights_shadow) {
        final HashPMap<KLightWithShadowType, MapPSet<KInstanceOpaqueType>> is =
          this.state.instances_shadow;

        if (is.containsKey(l) == false) {
          final BuilderState previous = this.state;
          final MapPSet<KInstanceOpaqueType> none = HashTreePSet.empty();
          this.state =
            new BuilderState(
              previous.instances_shadow.plus(l, none),
              previous.instances_all,
              previous.instances_opaque_lit,
              previous.instances_opaque_unlit,
              previous.lights_all,
              previous.lights_shadow,
              previous.translucents_ordered,
              previous.instances_visible,
              previous.light_group_builders,
              previous.groups_by_instance);
        }
      }

      final Map<KLightWithShadowType, Set<KInstanceOpaqueType>> instances_shadow =
        (Map<KLightWithShadowType, Set<KInstanceOpaqueType>>) ((Object) this.state.instances_shadow);
      final KSceneShadows in_shadows = new KSceneShadows(instances_shadow);

      HashPMap<String, KSceneLightGroup> gs = HashTreePMap.empty();
      for (final String name : this.state.light_group_builders.keySet()) {
        final LightGroupBuilder b = this.state.light_group_builders.get(name);
        gs = gs.plus(name, b.groupBuild());
      }

      return new KScene(
        this.camera,
        gs,
        this.state.instances_opaque_unlit,
        in_translucents,
        in_shadows,
        this.state.instances_visible);
    }

    @Override public KCamera sceneGetCamera()
    {
      return this.camera;
    }

    @Override public Set<KInstanceType> sceneGetInstances()
    {
      return this.state.instances_all;
    }

    @Override public
      Set<KInstanceOpaqueType>
      sceneGetInstancesOpaqueLitVisible()
    {
      return this.state.instances_opaque_lit;
    }

    @Override public
      Map<KLightWithShadowType, Set<KInstanceOpaqueType>>
      sceneGetInstancesOpaqueShadowCastingByLight()
    {
      final Object o = this.state.instances_shadow;
      return (Map<KLightWithShadowType, Set<KInstanceOpaqueType>>) o;
    }

    @Override public Set<KInstanceOpaqueType> sceneGetInstancesOpaqueUnlit()
    {
      return this.state.instances_opaque_unlit;
    }

    @Override public Set<KLightType> sceneGetLights()
    {
      return this.state.lights_all;
    }

    @Override public Set<KLightWithShadowType> sceneGetLightsShadowCasting()
    {
      return this.state.lights_shadow;
    }

    @Override public List<KTranslucentType> sceneGetTranslucents()
    {
      return this.state.translucents_ordered;
    }

    @Override public KSceneLightGroupBuilderType sceneNewLightGroup(
      final String name)
      throws RExceptionLightGroupAlreadyAdded
    {
      NullCheck.notNull(name, "Name");

      if (this.state.light_group_builders.containsKey(name)) {
        throw Builder.lightGroupAlreadyAdded(name);
      }

      final LightGroupBuilder g = new LightGroupBuilder(name, this);

      final BuilderState previous = this.state;

      this.state =
        new BuilderState(
          previous.instances_shadow,
          previous.instances_all,
          previous.instances_opaque_lit,
          previous.instances_opaque_unlit,
          previous.lights_all,
          previous.lights_shadow,
          previous.translucents_ordered,
          previous.instances_visible,
          previous.light_group_builders.plus(name, g),
          previous.groups_by_instance);

      return g;
    }
  }

  @SuppressWarnings({ "null" }) @EqualityReference private static final class BuilderState
  {
    // CHECKSTYLE_VISIBILITY:OFF
    final HashPMap<KInstanceOpaqueType, LightGroupBuilder>             groups_by_instance;
    final MapPSet<KInstanceType>                                       instances_all;
    final MapPSet<KInstanceOpaqueType>                                 instances_opaque_lit;
    final MapPSet<KInstanceOpaqueType>                                 instances_opaque_unlit;
    final HashPMap<KLightWithShadowType, MapPSet<KInstanceOpaqueType>> instances_shadow;
    final MapPSet<KInstanceType>                                       instances_visible;
    final HashPMap<String, LightGroupBuilder>                          light_group_builders;
    final MapPSet<KLightType>                                          lights_all;
    final MapPSet<KLightWithShadowType>                                lights_shadow;
    final PVector<KTranslucentType>                                    translucents_ordered;

    // CHECKSTYLE_VISIBILITY:ON

    BuilderState()
    {
      this.instances_shadow = HashTreePMap.empty();
      this.instances_all = HashTreePSet.empty();
      this.instances_opaque_unlit = HashTreePSet.empty();
      this.instances_opaque_lit = HashTreePSet.empty();
      this.instances_visible = HashTreePSet.empty();
      this.lights_all = HashTreePSet.empty();
      this.lights_shadow = HashTreePSet.empty();
      this.translucents_ordered = TreePVector.empty();
      this.light_group_builders = HashTreePMap.empty();
      this.groups_by_instance = HashTreePMap.empty();
    }

    BuilderState(
      final HashPMap<KLightWithShadowType, MapPSet<KInstanceOpaqueType>> in_instances_shadow,
      final MapPSet<KInstanceType> in_instances_all,
      final MapPSet<KInstanceOpaqueType> in_instances_opaque_lit,
      final MapPSet<KInstanceOpaqueType> in_instances_opaque_unlit,
      final MapPSet<KLightType> in_lights_all,
      final MapPSet<KLightWithShadowType> in_lights_shadow,
      final PVector<KTranslucentType> in_translucents_ordered,
      final MapPSet<KInstanceType> in_instances_visible,
      final HashPMap<String, LightGroupBuilder> in_light_groups,
      final HashPMap<KInstanceOpaqueType, LightGroupBuilder> in_groups_by_instance)
    {
      this.instances_shadow = in_instances_shadow;
      this.instances_all = in_instances_all;
      this.instances_opaque_unlit = in_instances_opaque_unlit;
      this.instances_opaque_lit = in_instances_opaque_lit;
      this.lights_all = in_lights_all;
      this.lights_shadow = in_lights_shadow;
      this.translucents_ordered = in_translucents_ordered;
      this.instances_visible = in_instances_visible;
      this.light_group_builders = in_light_groups;
      this.groups_by_instance = in_groups_by_instance;
    }
  }

  @SuppressWarnings({ "null", "synthetic-access" }) @EqualityReference private static final class LightGroupBuilder implements
    KSceneLightGroupBuilderType
  {
    private LightGroupState group_state;
    private final String    name;
    private final Builder   parent;

    LightGroupBuilder(
      final String in_name,
      final Builder in_parent)
    {
      this.name = in_name;
      this.group_state = new LightGroupState();
      this.parent = in_parent;
    }

    @Override public void groupAddInstance(
      final KInstanceOpaqueType o)
      throws RExceptionInstanceAlreadyUnlit,
        RExceptionInstanceAlreadyInGroup
    {
      if (this.parent.state.instances_opaque_unlit.contains(o)) {
        throw this.parent.alreadyUnlit(this.name, o);
      }
      if (this.parent.state.groups_by_instance.containsKey(o)) {
        throw this.parent.alreadyInGroup(
          o,
          this.parent.state.groups_by_instance.get(o).name,
          this.name);
      }

      this.group_state =
        new LightGroupState(
          this.group_state.lights_all,
          this.group_state.instances.plus(o));

      final BuilderState previous = this.parent.state;

      this.parent.state =
        new BuilderState(
          previous.instances_shadow,
          previous.instances_all.plus(o),
          previous.instances_opaque_lit.plus(o),
          previous.instances_opaque_unlit,
          previous.lights_all,
          previous.lights_shadow,
          previous.translucents_ordered,
          previous.instances_visible.plus(o),
          previous.light_group_builders,
          previous.groups_by_instance.plus(o, this));
    }

    @Override public void groupAddLight(
      final KLightType light)
    {
      final BuilderState previous = this.parent.state;

      if (light instanceof KLightWithShadowType) {
        this.parent.state =
          new BuilderState(
            previous.instances_shadow,
            previous.instances_all,
            previous.instances_opaque_lit,
            previous.instances_opaque_unlit,
            previous.lights_all.plus(light),
            previous.lights_shadow.plus((KLightWithShadowType) light),
            previous.translucents_ordered,
            previous.instances_visible,
            previous.light_group_builders,
            previous.groups_by_instance);
      } else {
        this.parent.state =
          new BuilderState(
            previous.instances_shadow,
            previous.instances_all,
            previous.instances_opaque_lit,
            previous.instances_opaque_unlit,
            previous.lights_all.plus(light),
            previous.lights_shadow,
            previous.translucents_ordered,
            previous.instances_visible,
            previous.light_group_builders,
            previous.groups_by_instance);
      }

      this.group_state =
        new LightGroupState(
          this.group_state.lights_all.plus(light),
          this.group_state.instances);
    }

    @Override public KSceneLightGroup groupBuild()
      throws RExceptionLightGroupLacksInstances,
        RExceptionLightGroupLacksLights
    {
      if (this.group_state.instances.isEmpty()) {
        throw this.noInstances();
      }
      if (this.group_state.lights_all.isEmpty()) {
        throw this.noLights();
      }

      return new KSceneLightGroup(
        this.name,
        this.group_state.lights_all,
        this.group_state.instances);
    }

    private RExceptionLightGroupLacksInstances noInstances()
    {
      final StringBuilder m = new StringBuilder();
      m.append("No instances have been added to light group '");
      m.append(this.name);
      m.append("'");
      final String r = m.toString();
      assert r != null;
      return new RExceptionLightGroupLacksInstances(r);
    }

    private RExceptionLightGroupLacksLights noLights()
    {
      final StringBuilder m = new StringBuilder();
      m.append("No lights have been added to light group '");
      m.append(this.name);
      m.append("'");
      final String r = m.toString();
      assert r != null;
      return new RExceptionLightGroupLacksLights(r);
    }
  }

  @SuppressWarnings({ "null" }) @EqualityReference private static final class LightGroupState
  {
    // CHECKSTYLE_VISIBILITY:OFF
    final MapPSet<KInstanceOpaqueType> instances;
    final MapPSet<KLightType>          lights_all;

    // CHECKSTYLE_VISIBILITY:ON

    LightGroupState()
    {
      this.lights_all = HashTreePSet.empty();
      this.instances = HashTreePSet.empty();
    }

    LightGroupState(
      final MapPSet<KLightType> in_lights_all,
      final MapPSet<KInstanceOpaqueType> in_instances)
    {
      this.lights_all = in_lights_all;
      this.instances = in_instances;
    }
  }

  /**
   * Retrieve a new {@link KSceneBuilderWithCreateType} with which to
   * construct a scene, rendered from the perspective of <code>camera</code>.
   *
   * @param camera
   *          The observer of the scene
   * @return A new scene builder
   */

  public static KSceneBuilderWithCreateType newBuilder(
    final KCamera camera)
  {
    return new Builder(camera);
  }

  private final KCamera                       camera;
  private final Map<String, KSceneLightGroup> groups;
  private final KSceneShadows                 shadows;
  private final List<KTranslucentType>        translucents;
  private final Set<KInstanceOpaqueType>      unlit_opaque;
  private final Set<KInstanceType>            visible;

  private KScene(
    final KCamera in_camera,
    final Map<String, KSceneLightGroup> in_groups,
    final Set<KInstanceOpaqueType> in_unlit_opaque,
    final List<KTranslucentType> in_translucents,
    final KSceneShadows in_shadows,
    final Set<KInstanceType> in_visible)
  {
    this.camera = in_camera;
    this.groups = in_groups;
    this.unlit_opaque = in_unlit_opaque;
    this.translucents = in_translucents;
    this.shadows = in_shadows;
    this.visible = in_visible;
  }

  /**
   * @return The scene's camera
   */

  public KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @return The light groups in the scene.
   */

  public Map<String, KSceneLightGroup> getLightGroups()
  {
    return this.groups;
  }

  /**
   * @return The set of shadow casters in the current scene.
   */

  public KSceneShadows getShadows()
  {
    return this.shadows;
  }

  /**
   * @return The set of translucent instances in the current scene.
   */

  public List<KTranslucentType> getTranslucents()
  {
    return this.translucents;
  }

  /**
   * @return The set of unlit opaque instances.
   */

  public Set<KInstanceOpaqueType> getUnlitOpaques()
  {
    return this.unlit_opaque;
  }

  /**
   * @return The set of all visible instances in the scene (that is, all
   *         instances that were not added as invisible shadow casters).
   */

  public Set<KInstanceType> getVisibleInstances()
  {
    return this.visible;
  }
}
