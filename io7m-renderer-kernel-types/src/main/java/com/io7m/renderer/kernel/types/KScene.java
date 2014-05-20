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

import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RExceptionInstanceAlreadyLit;
import com.io7m.renderer.types.RExceptionInstanceAlreadyUnlit;
import com.io7m.renderer.types.RExceptionInstancesNotDistinct;
import com.io7m.renderer.types.RExceptionLightsNotDistinct;

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

@EqualityStructural public final class KScene
{
  private static final class Builder implements KSceneBuilderWithCreateType
  {
    private static void checkInstanceDistinct(
      final HashPMap<KInstanceID, KInstanceType> instances,
      final KInstanceType i)
      throws RExceptionInstancesNotDistinct
    {
      final KInstanceID id = i.instanceGetID();
      if (instances.containsKey(id)) {
        final KInstanceType other = instances.get(id);
        final StringBuilder m = new StringBuilder();
        m.append("Attempted to add a non-distinct instance:\n");
        m.append("  Existing: ");
        m.append(other);
        m.append("\n");
        m.append("  Incoming: ");
        m.append(i);
        m.append("\n");
        final String s = m.toString();
        assert s != null;
        throw new RExceptionInstancesNotDistinct(s);
      }
    }

    private static void checkLightDistinct(
      final HashPMap<KLightID, KLightType> lights,
      final KLightType light)
      throws RExceptionLightsNotDistinct
    {
      final KLightID id = light.lightGetID();
      if (lights.containsKey(id)) {
        final KLightType other = lights.get(id);
        if (other.equals(light) == false) {
          final StringBuilder m = new StringBuilder();
          m.append("Attempted to add a non-distinct light:\n");
          m.append("  Existing: ");
          m.append(other);
          m.append("\n");
          m.append("  Incoming: ");
          m.append(light);
          m.append("\n");
          final String s = m.toString();
          assert s != null;
          throw new RExceptionLightsNotDistinct(s);
        }
      }
    }

    private final KCamera                        camera;
    private HashPMap<KInstanceID, KInstanceType> instances;
    private HashPMap<KLightID, KInstanceID>      instances_shadow;
    private HashPMap<KLightID, KLightType>       lights;
    private MapPSet<KLightID>                    lights_shadow;

    Builder(
      final KCamera in_camera)
    {
      this.camera = NullCheck.notNull(in_camera, "Camera");
      this.lights = HashTreePMap.empty();
      this.instances = HashTreePMap.empty();
      this.instances_shadow = HashTreePMap.empty();
      this.lights_shadow = HashTreePSet.empty();
    }

    private void addInstance(
      final KInstanceType instance)
    {
      final HashPMap<KInstanceID, KInstanceType> instances_next =
        this.instances.plus(instance.instanceGetID(), instance);
      assert instances_next != null;
      this.instances = instances_next;
    }

    private void addLight(
      final KLightType light)
    {
      final HashPMap<KLightID, KLightType> lights_next =
        this.lights.plus(light.lightGetID(), light);
      assert lights_next != null;
      this.lights = lights_next;
    }

    @Override public void sceneAddInvisibleWithShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionLightsNotDistinct,
        RExceptionInstancesNotDistinct
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      Builder.checkLightDistinct(this.lights, light);
      Builder.checkInstanceDistinct(this.instances, instance);

      try {
        this.addLight(light);
        this.addInstance(instance);

        final KLightID lid = light.lightGetID();
        final KInstanceID iid = instance.instanceGetID();
        this.lights_shadow = this.lights_shadow.plus(lid);
        this.instances_shadow = this.instances_shadow.plus(lid, iid);
      } catch (final Throwable x) {
        throw new UnreachableCodeException(x);
      }
    }

    @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionLightsNotDistinct,
        RExceptionInstanceAlreadyUnlit,
        RExceptionInstancesNotDistinct
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      Builder.checkLightDistinct(this.lights, light);
      Builder.checkInstanceDistinct(this.instances, instance);

      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void sceneAddOpaqueLitVisibleWithShadow(
      final KLightType light,
      final KInstanceOpaqueType instance)
      throws RExceptionLightsNotDistinct,
        RExceptionInstanceAlreadyUnlit,
        RExceptionInstancesNotDistinct
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      Builder.checkLightDistinct(this.lights, light);
      Builder.checkInstanceDistinct(this.instances, instance);

      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void sceneAddOpaqueUnlit(
      final KInstanceOpaqueType instance)
      throws RExceptionInstanceAlreadyLit
    {
      NullCheck.notNull(instance, "Instance");

      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void sceneAddTranslucentLit(
      final KInstanceTranslucentLitType instance,
      final Set<KLightType> instance_lights)
      throws RExceptionLightsNotDistinct
    {
      NullCheck.notNull(instance, "Instance");
      NullCheck.notNullAll(instance_lights, "Lights");

      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public void sceneAddTranslucentUnlit(
      final KInstanceTranslucentUnlitType instance)
    {
      NullCheck.notNull(instance, "Instance");

      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public KScene sceneCreate()
    {
      // TODO Auto-generated method stub
      throw new UnimplementedCodeException();
    }

    @Override public Map<KInstanceID, KInstanceType> sceneGetInstances()
    {
      return this.instances;
    }

    @Override public
      Map<KLightID, KInstanceID>
      sceneGetInstancesShadowCasting()
    {
      return this.instances_shadow;
    }

    @Override public Map<KLightID, KLightType> sceneGetLights()
    {
      return this.lights;
    }

    @Override public Set<KLightID> sceneGetLightsShadowCasting()
    {
      return this.lights_shadow;
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

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final KScene other = (KScene) obj;
    return (this.camera.equals(other.camera))
      && (this.opaques.equals(other.opaques))
      && (this.shadows.equals(other.shadows))
      && (this.translucents.equals(other.translucents))
      && (this.visible.equals(other.visible));
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

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.camera.hashCode();
    result = (prime * result) + this.opaques.hashCode();
    result = (prime * result) + this.shadows.hashCode();
    result = (prime * result) + this.translucents.hashCode();
    result = (prime * result) + this.visible.hashCode();
    return result;
  }
}
