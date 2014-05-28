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
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyShadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnshadowed;
import com.io7m.renderer.types.RExceptionInstanceAlreadyVisible;
import com.io7m.renderer.types.RExceptionLightMissingShadow;

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
  @SuppressWarnings({ "null", "unchecked", "synthetic-access" }) private static final class Builder implements
    KSceneBuilderWithCreateType
  {
    private static class State
    {
      // CHECKSTYLE_VISIBILITY:OFF
      final MapPSet<KInstanceType>                             instances_all;
      final HashPMap<KLightType, MapPSet<KInstanceOpaqueType>> instances_opaque_by_light;
      final MapPSet<KInstanceOpaqueType>                       instances_opaque_lit;
      final MapPSet<KInstanceOpaqueType>                       instances_opaque_unlit;
      final HashPMap<KLightType, MapPSet<KInstanceOpaqueType>> instances_shadow;
      final MapPSet<KInstanceType>                             instances_visible;
      final MapPSet<KLightType>                                lights_all;
      final MapPSet<KLightType>                                lights_shadow;
      final PVector<KTranslucentType>                          translucents_ordered;

      // CHECKSTYLE_VISIBILITY:ON

      State()
      {
        this.instances_shadow = HashTreePMap.empty();
        this.instances_all = HashTreePSet.empty();
        this.instances_opaque_by_light = HashTreePMap.empty();
        this.instances_opaque_unlit = HashTreePSet.empty();
        this.instances_opaque_lit = HashTreePSet.empty();
        this.instances_visible = HashTreePSet.empty();
        this.lights_all = HashTreePSet.empty();
        this.lights_shadow = HashTreePSet.empty();
        this.translucents_ordered = TreePVector.empty();
      }

      State(
        final HashPMap<KLightType, MapPSet<KInstanceOpaqueType>> in_instances_shadow,
        final MapPSet<KInstanceType> in_instances_all,
        final HashPMap<KLightType, MapPSet<KInstanceOpaqueType>> in_instances_opaque_by_light,
        final MapPSet<KInstanceOpaqueType> in_instances_opaque_lit,
        final MapPSet<KInstanceOpaqueType> in_instances_opaque_unlit,
        final MapPSet<KLightType> in_lights_all,
        final MapPSet<KLightType> in_lights_shadow,
        final PVector<KTranslucentType> in_translucents_ordered,
        final MapPSet<KInstanceType> in_instances_visible)
      {
        this.instances_shadow = in_instances_shadow;
        this.instances_all = in_instances_all;
        this.instances_opaque_by_light = in_instances_opaque_by_light;
        this.instances_opaque_unlit = in_instances_opaque_unlit;
        this.instances_opaque_lit = in_instances_opaque_lit;
        this.lights_all = in_lights_all;
        this.lights_shadow = in_lights_shadow;
        this.translucents_ordered = in_translucents_ordered;
        this.instances_visible = in_instances_visible;
      }
    }

    private final KCamera       camera;
    private final StringBuilder message;
    private State               state;

    Builder(
      final KCamera in_camera)
    {
      this.camera = NullCheck.notNull(in_camera, "Camera");
      this.state = new State();
      this.message = new StringBuilder();
    }

    private RExceptionInstanceAlreadyLit alreadyLit(
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance as unlit, but the instance has already been added previously to one or more lights.\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");

      final HashPMap<KLightType, MapPSet<KInstanceOpaqueType>> map =
        this.state.instances_opaque_by_light;
      for (final KLightType k : map.keySet()) {
        final MapPSet<KInstanceOpaqueType> instances = map.get(k);
        if (instances.contains(instance)) {
          m.append("  Light: ");
          m.append(k);
          m.append("\n");
        }
      }

      return new RExceptionInstanceAlreadyLit(m.toString());
    }

    private RExceptionInstanceAlreadyShadowed alreadyShadowed(
      final KLightType light,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance for a light without a shadow, but the light already has the instance as a shadow caster.\n");
      m.append("  Light: ");
      m.append(light);
      m.append("\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      return new RExceptionInstanceAlreadyShadowed(m.toString());
    }

    private RExceptionInstanceAlreadyUnlit alreadyUnlit(
      final KLightType light,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance for a light but the instance has already been added previously as unlit.\n");
      m.append("  Light: ");
      m.append(light);
      m.append("\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      return new RExceptionInstanceAlreadyUnlit(m.toString());
    }

    private RExceptionInstanceAlreadyUnshadowed alreadyUnshadowed(
      final KLightType light,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an instance for a light with a shadow, but the light already has the instance as a non shadow-caster.\n");
      m.append("  Light: ");
      m.append(light);
      m.append("\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      return new RExceptionInstanceAlreadyUnshadowed(m.toString());
    }

    private RExceptionInstanceAlreadyVisible alreadyVisible(
      final KLightType light,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add an invisible shadow casting instance for a light when the light already has it as a visible instance.\n");
      m.append("  Light: ");
      m.append(light);
      m.append("\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      return new RExceptionInstanceAlreadyVisible(m.toString());
    }

    private RExceptionLightMissingShadow missingShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
    {
      final StringBuilder m = this.message;
      m.setLength(0);
      m
        .append("Attempted to add a shadow-casting light and instance to the scene but using a light that has no shadow.\n");
      m.append("  Light: ");
      m.append(light);
      m.append("\n");
      m.append("  Instance: ");
      m.append(instance);
      m.append("\n");
      return new RExceptionLightMissingShadow(m.toString());
    }

    @Override public void sceneAddInvisibleWithShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyVisible,
        RExceptionLightMissingShadow
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      if (light.lightHasShadow() == false) {
        throw this.missingShadow(light, instance);
      }

      final MapPSet<KInstanceOpaqueType> instances =
        this.state.instances_opaque_by_light.get(light);
      if (instances != null) {
        if (instances.contains(instance)) {
          throw this.alreadyVisible(light, instance);
        }
      }

      MapPSet<KInstanceOpaqueType> instances_shadow =
        this.state.instances_shadow.get(light);
      if (instances_shadow == null) {
        instances_shadow = HashTreePSet.singleton(instance);
      } else {
        instances_shadow = instances_shadow.plus(instance);
      }

      this.state =
        new State(
          this.state.instances_shadow.plus(light, instances_shadow),
          this.state.instances_all.plus(instance),
          this.state.instances_opaque_by_light,
          this.state.instances_opaque_lit,
          this.state.instances_opaque_unlit,
          this.state.lights_all.plus(light),
          this.state.lights_shadow.plus(light),
          this.state.translucents_ordered,
          this.state.instances_visible);
    }

    @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyUnlit,
        RExceptionInstanceAlreadyShadowed
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      if (this.state.instances_opaque_unlit.contains(instance)) {
        throw this.alreadyUnlit(light, instance);
      }

      final MapPSet<KInstanceOpaqueType> instances_shadow =
        this.state.instances_shadow.get(light);
      if (instances_shadow != null) {
        if (instances_shadow.contains(instance)) {
          throw this.alreadyShadowed(light, instance);
        }
      }

      MapPSet<KInstanceOpaqueType> instances =
        this.state.instances_opaque_by_light.get(light);
      if (instances == null) {
        instances = HashTreePSet.singleton(instance);
      } else {
        instances = instances.plus(instance);
      }

      this.state =
        new State(
          this.state.instances_shadow,
          this.state.instances_all.plus(instance),
          this.state.instances_opaque_by_light.plus(light, instances),
          this.state.instances_opaque_lit.plus(instance),
          this.state.instances_opaque_unlit,
          this.state.lights_all.plus(light),
          this.state.lights_shadow,
          this.state.translucents_ordered,
          this.state.instances_visible.plus(instance));
    }

    @Override public void sceneAddOpaqueLitVisibleWithShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyUnlit,
        RExceptionLightMissingShadow,
        RExceptionInstanceAlreadyUnshadowed
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      if (light.lightHasShadow() == false) {
        throw this.missingShadow(light, instance);
      }

      if (this.state.instances_opaque_unlit.contains(instance)) {
        throw this.alreadyUnlit(light, instance);
      }

      MapPSet<KInstanceOpaqueType> instances =
        this.state.instances_opaque_by_light.get(light);
      MapPSet<KInstanceOpaqueType> instances_shadow =
        this.state.instances_shadow.get(light);

      /**
       * If the instance exists for the light, but the instance is not in the
       * current set of shadow casters, then it is unshadowed.
       */

      if (instances != null) {
        final boolean exists = instances.contains(instance);
        final boolean unshadowed =
          (instances_shadow == null)
            || (!instances_shadow.contains(instance));

        if (exists && unshadowed) {
          throw this.alreadyUnshadowed(light, instance);
        }
      }

      if (instances == null) {
        instances = HashTreePSet.singleton(instance);
      } else {
        instances = instances.plus(instance);
      }

      if (instances_shadow == null) {
        instances_shadow = HashTreePSet.singleton(instance);
      } else {
        instances_shadow = instances_shadow.plus(instance);
      }

      this.state =
        new State(
          this.state.instances_shadow.plus(light, instances_shadow),
          this.state.instances_all.plus(instance),
          this.state.instances_opaque_by_light.plus(light, instances),
          this.state.instances_opaque_lit.plus(instance),
          this.state.instances_opaque_unlit,
          this.state.lights_all.plus(light),
          this.state.lights_shadow.plus(light),
          this.state.translucents_ordered,
          this.state.instances_visible.plus(instance));

    }

    @Override public void sceneAddOpaqueUnlit(
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyLit
    {
      NullCheck.notNull(instance, "Instance");

      if (this.state.instances_opaque_lit.contains(instance)) {
        throw this.alreadyLit(instance);
      }

      this.state =
        new State(
          this.state.instances_shadow,
          this.state.instances_all.plus(instance),
          this.state.instances_opaque_by_light,
          this.state.instances_opaque_lit,
          this.state.instances_opaque_unlit.plus(instance),
          this.state.lights_all,
          this.state.lights_shadow,
          this.state.translucents_ordered,
          this.state.instances_visible.plus(instance));
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

        this.state =
          new State(
            this.state.instances_shadow,
            this.state.instances_all.plus(instance),
            this.state.instances_opaque_by_light,
            this.state.instances_opaque_lit,
            this.state.instances_opaque_unlit,
            this.state.lights_all.plusAll(instance_lights),
            this.state.lights_shadow,
            this.state.translucents_ordered.plus(translucent),
            this.state.instances_visible.plus(instance));

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

        this.state =
          new State(
            this.state.instances_shadow,
            this.state.instances_all.plus(instance),
            this.state.instances_opaque_by_light,
            this.state.instances_opaque_lit,
            this.state.instances_opaque_unlit,
            this.state.lights_all,
            this.state.lights_shadow,
            this.state.translucents_ordered.plus(translucent),
            this.state.instances_visible.plus(instance));
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public KScene sceneCreate()
    {
      final Map<KLightType, Set<KInstanceOpaqueType>> opbl =
        (Map<KLightType, Set<KInstanceOpaqueType>>) ((Object) this.state.instances_opaque_by_light);
      final KSceneOpaques in_opaques =
        KSceneOpaques.newOpaques(opbl, this.state.instances_opaque_unlit);

      final List<KTranslucentType> in_translucents =
        this.state.translucents_ordered;

      final Map<KLightType, Set<KInstanceOpaqueType>> in_instances_shadow =
        (Map<KLightType, Set<KInstanceOpaqueType>>) ((Object) this.state.instances_shadow);
      final KSceneShadows in_shadows = new KSceneShadows(in_instances_shadow);

      return new KScene(
        this.camera,
        in_opaques,
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
      Map<KLightType, Set<KInstanceOpaqueType>>
      sceneGetInstancesOpaqueLitVisibleByLight()
    {
      final Object o = this.state.instances_opaque_by_light;
      return (Map<KLightType, Set<KInstanceOpaqueType>>) o;
    }

    @Override public
      Map<KLightType, Set<KInstanceOpaqueType>>
      sceneGetInstancesOpaqueShadowCastingByLight()
    {
      final Object o = this.state.instances_shadow;
      return (Map<KLightType, Set<KInstanceOpaqueType>>) o;
    }

    @Override public Set<KInstanceOpaqueType> sceneGetInstancesOpaqueUnlit()
    {
      return this.state.instances_opaque_unlit;
    }

    @Override public Set<KLightType> sceneGetLights()
    {
      return this.state.lights_all;
    }

    @Override public Set<KLightType> sceneGetLightsShadowCasting()
    {
      return this.state.lights_shadow;
    }

    @Override public List<KTranslucentType> sceneGetTranslucents()
    {
      return this.state.translucents_ordered;
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

  private final KCamera                camera;
  private final KSceneOpaques          opaques;
  private final KSceneShadows          shadows;
  private final List<KTranslucentType> translucents;
  private final Set<KInstanceType>     visible;

  private KScene(
    final KCamera in_camera,
    final KSceneOpaques in_opaques,
    final List<KTranslucentType> in_translucents,
    final KSceneShadows in_shadows,
    final Set<KInstanceType> in_visible)
  {
    this.camera = in_camera;
    this.opaques = in_opaques;
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
   * @return The set of opaque instances in the current scene.
   */

  public KSceneOpaques getOpaques()
  {
    return this.opaques;
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
   * @return The set of all visible instances in the scene (that is, all
   *         instances that were not added as invisible shadow casters).
   */

  public Set<KInstanceType> getVisibleInstances()
  {
    return this.visible;
  }
}
