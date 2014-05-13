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
import java.util.Set;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jequality.annotations.EqualityStructural;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.types.RException;

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
    private static final KInstanceTransformedVisitorType<KTranslucentType, UnreachableCodeException> IDENTITY_TRANSLUCENT;

    static {
      IDENTITY_TRANSLUCENT =
        new KInstanceTransformedVisitorType<KTranslucentType, UnreachableCodeException>() {
          @Override public KTranslucentType transformedOpaqueAlphaDepth(
            final KInstanceTransformedOpaqueAlphaDepth i)
            throws RException,
              JCGLException
          {
            throw new UnreachableCodeException();
          }

          @Override public KTranslucentType transformedOpaqueRegular(
            final KInstanceTransformedOpaqueRegular i)
            throws RException,
              JCGLException
          {
            throw new UnreachableCodeException();
          }

          @Override public KTranslucentType transformedTranslucentRefractive(
            final KInstanceTransformedTranslucentRefractive i)
            throws RException,
              JCGLException
          {
            return i;
          }

          @Override public KTranslucentType transformedTranslucentRegular(
            final KInstanceTransformedTranslucentRegular i)
            throws RException,
              JCGLException
          {
            return i;
          }

          @Override public
            KTranslucentType
            transformedTranslucentSpecularOnly(
              final KInstanceTransformedTranslucentSpecularOnly i)
              throws RException,
                JCGLException
          {
            throw new UnreachableCodeException();
          }
        };
    }

    private final KCamera                                                                            camera;
    private MapPSet<KLightType>                                                                      lights_all;
    private MapPSet<KInstanceTransformedType>                                                        lit;
    private HashPMap<KLightType, List<KInstanceTransformedOpaqueType>>                               lit_opaque;
    private HashPMap<KLightType, List<KInstanceTransformedOpaqueType>>                               shadow_casters;
    private MapPSet<KLightType>                                                                      shadow_lights;
    private MapPSet<KInstanceTransformedType>                                                        unlit;
    private MapPSet<KInstanceTransformedOpaqueType>                                                  unlit_opaque;
    private MapPSet<KInstanceTransformedType>                                                        visible_instances;
    private MapPSet<KInstanceTransformedOpaqueType>                                                  visible_opaque;
    private PVector<KTranslucentType>                                                                visible_translucent_ordered;

    @SuppressWarnings("null") protected Builder(
      final KCamera in_camera)
    {
      this.camera = NullCheck.notNull(in_camera, "Camera");
      this.lit = HashTreePSet.empty();
      this.lit_opaque = HashTreePMap.empty();
      this.shadow_lights = HashTreePSet.empty();
      this.shadow_casters = HashTreePMap.empty();
      this.visible_translucent_ordered = TreePVector.empty();
      this.visible_opaque = HashTreePSet.empty();
      this.visible_instances = HashTreePSet.empty();
      this.lights_all = HashTreePSet.empty();
      this.unlit = HashTreePSet.empty();
      this.unlit_opaque = HashTreePSet.empty();
    }

    private void addLight(
      final KLightType light)
    {
      if (light.lightHasShadow()) {
        this.shadow_lights = this.shadow_lights.plus(light);
      }
      this.lights_all = this.lights_all.plus(light);
    }

    private void addOpaqueInstance(
      final KLightType light,
      final KInstanceTransformedOpaqueType instance)
    {
      final PVector<KInstanceTransformedOpaqueType> instances;
      if (this.lit_opaque.containsKey(light)) {
        instances =
          (PVector<KInstanceTransformedOpaqueType>) this.lit_opaque
            .get(light);
      } else {
        instances = TreePVector.empty();
      }
      this.lit_opaque = this.lit_opaque.plus(light, instances.plus(instance));
      this.lit = this.lit.plus(instance);
      this.visible_opaque = this.visible_opaque.plus(instance);
      this.visible_instances = this.visible_instances.plus(instance);
    }

    private void addShadowCaster(
      final KLightType light,
      final KInstanceTransformedOpaqueType instance)
    {
      TreePVector<KInstanceTransformedOpaqueType> casters = null;
      if (this.shadow_casters.containsKey(light)) {
        casters =
          (TreePVector<KInstanceTransformedOpaqueType>) this.shadow_casters
            .get(light);
      } else {
        casters = TreePVector.empty();
      }

      this.shadow_casters =
        this.shadow_casters.plus(light, casters.plus(instance));
    }

    private void checkNotLit(
      final KInstanceTransformedType transformed)
    {
      if (this.lit.contains(transformed)) {
        final String s =
          String.format("Instance %s is already lit", transformed);
        throw new IllegalStateException(s);
      }
    }

    private void checkNotUnlit(
      final KInstanceTransformedType transformed)
    {
      if (this.unlit.contains(transformed)) {
        final String s =
          String.format("Instance %s is already unlit", transformed);
        throw new IllegalStateException(s);
      }
    }

    private void checkOpaqueLit(
      final KLightType light,
      final KInstanceTransformedOpaqueType transformed)
    {
      NullCheck.notNull(light, "Light");
      this.checkNotUnlit(transformed);
    }

    @Override public boolean equals(
      final @Nullable Object other)
    {
      return super.equals(other);
    }

    @Override public int hashCode()
    {
      return super.hashCode();
    }

    @Override public void sceneAddInvisibleWithShadow(
      final KLightType light,
      final KInstanceTransformedOpaqueType instance)
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      if (light.lightHasShadow()) {
        this.addShadowCaster(light, instance);
      }
    }

    @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
      final KLightType light,
      final KInstanceTransformedOpaqueType instance)
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      this.checkOpaqueLit(light, instance);
      this.addOpaqueInstance(light, instance);
      this.addLight(light);
    }

    @Override public void sceneAddOpaqueLitVisibleWithShadow(
      final KLightType light,
      final KInstanceTransformedOpaqueType instance)
    {
      NullCheck.notNull(light, "Light");
      NullCheck.notNull(instance, "Instance");

      this.checkOpaqueLit(light, instance);
      this.addOpaqueInstance(light, instance);
      this.sceneAddInvisibleWithShadow(light, instance);
    }

    @Override public void sceneAddOpaqueUnlit(
      final KInstanceTransformedOpaqueType instance)
    {
      NullCheck.notNull(instance, "Instance");

      this.checkNotLit(instance);
      this.unlit = this.unlit.plus(instance);
      this.unlit_opaque = this.unlit_opaque.plus(instance);
      this.visible_opaque = this.visible_opaque.plus(instance);
      this.visible_instances = this.visible_instances.plus(instance);
    }

    @Override public void sceneAddTranslucentLit(
      final KInstanceTransformedTranslucentLitType instance,
      final Set<KLightType> lights)
    {
      NullCheck.notNull(instance, "Instance");
      NullCheck.notNullAll(lights, "Light");

      try {
        this.checkNotUnlit(instance);

        final KInstanceTransformedTranslucentLitVisitorType<KTranslucentType, UnreachableCodeException> v =
          new KInstanceTransformedTranslucentLitVisitorType<KTranslucentType, UnreachableCodeException>() {
            @Override public
              KTranslucentType
              transformedTranslucentLitRegular(
                final KInstanceTransformedTranslucentRegular i)
            {
              return new KTranslucentRegularLit(i, lights);
            }

            @Override public
              KTranslucentType
              transformedTranslucentLitSpecularOnly(
                final KInstanceTransformedTranslucentSpecularOnly i)
            {
              return new KTranslucentSpecularOnlyLit(i, lights);
            }
          };

        this.visible_translucent_ordered =
          this.visible_translucent_ordered.plus(instance
            .transformedTranslucentLitAccept(v));
        this.visible_instances = this.visible_instances.plus(instance);

      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public void sceneAddTranslucentUnlit(
      final KInstanceTransformedTranslucentUnlitType instance)
    {
      NullCheck.notNull(instance, "Instance");

      try {
        this.checkNotLit(instance);
        this.unlit = this.unlit.plus(instance);

        this.visible_translucent_ordered =
          this.visible_translucent_ordered.plus(instance
            .transformedAccept(Builder.IDENTITY_TRANSLUCENT));

        this.visible_instances = this.visible_instances.plus(instance);
      } catch (final JCGLException e) {
        throw new UnreachableCodeException(e);
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @SuppressWarnings("synthetic-access") @Override public
      KScene
      sceneCreate()
    {
      final KSceneOpaques o =
        new KSceneOpaques(
          this.lit_opaque,
          this.unlit_opaque,
          this.visible_opaque);

      final KSceneShadows s = new KSceneShadows(this.shadow_casters);
      return new KScene(
        this.camera,
        o,
        this.visible_translucent_ordered,
        s,
        this.visible_instances);
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
  private final KSceneOpaques                 opaques;
  private final KSceneShadows                 shadows;
  private final List<KTranslucentType>        translucents;
  private final Set<KInstanceTransformedType> visible;

  private KScene(
    final KCamera in_camera,
    final KSceneOpaques in_opaques,
    final List<KTranslucentType> in_translucents,
    final KSceneShadows in_shadows,
    final Set<KInstanceTransformedType> in_visible)
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

  public Set<KInstanceTransformedType> getVisibleInstances()
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
