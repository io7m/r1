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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jcanephora.JCGLException;
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

@Immutable public final class KScene
{
  private static final class Builder implements KSceneBuilderWithCreateType
  {
    private static final @Nonnull KInstanceTransformedVisitorType<KTranslucentType, ConstraintError> IDENTITY_TRANSLUCENT;

    static {
      IDENTITY_TRANSLUCENT =
        new KInstanceTransformedVisitorType<KTranslucentType, ConstraintError>() {
          @Override public @Nonnull
            KTranslucentType
            transformedVisitOpaqueAlphaDepth(
              final @Nonnull KInstanceTransformedOpaqueAlphaDepth i)
              throws ConstraintError,
                RException,
                JCGLException
          {
            throw new UnreachableCodeException();
          }

          @Override public @Nonnull
            KTranslucentType
            transformedVisitOpaqueRegular(
              final @Nonnull KInstanceTransformedOpaqueRegular i)
              throws ConstraintError,
                RException,
                JCGLException
          {
            throw new UnreachableCodeException();
          }

          @Override public @Nonnull
            KTranslucentType
            transformedVisitTranslucentRefractive(
              final @Nonnull KInstanceTransformedTranslucentRefractive i)
              throws ConstraintError,
                ConstraintError,
                RException,
                JCGLException
          {
            return i;
          }

          @Override public @Nonnull
            KTranslucentType
            transformedVisitTranslucentRegular(
              final @Nonnull KInstanceTransformedTranslucentRegular i)
              throws ConstraintError,
                RException,
                JCGLException
          {
            return i;
          }
        };
    }

    private final @Nonnull KCamera                                                                   camera;
    private @Nonnull MapPSet<KLightType>                                                             lights_all;
    private @Nonnull MapPSet<KInstanceTransformedType>                                               lit;
    private @Nonnull HashPMap<KLightType, List<KInstanceTransformedOpaqueType>>                      lit_opaque;
    private @Nonnull HashPMap<KLightType, List<KInstanceTransformedOpaqueType>>                      shadow_casters;
    private @Nonnull MapPSet<KLightType>                                                             shadow_lights;
    private @Nonnull MapPSet<KInstanceTransformedType>                                               unlit;
    private @Nonnull MapPSet<KInstanceTransformedOpaqueType>                                         unlit_opaque;
    private @Nonnull MapPSet<KInstanceTransformedType>                                               visible_instances;
    private @Nonnull MapPSet<KInstanceTransformedOpaqueType>                                         visible_opaque;
    private @Nonnull PVector<KTranslucentType>                                                       visible_translucent_ordered;

    protected Builder(
      final @Nonnull KCamera in_camera)
      throws ConstraintError
    {
      this.camera = Constraints.constrainNotNull(in_camera, "Camera");
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
      final @Nonnull KLightType light)
    {
      if (light.lightHasShadow()) {
        this.shadow_lights = this.shadow_lights.plus(light);
      }
      this.lights_all = this.lights_all.plus(light);
    }

    private void addOpaqueInstance(
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType instance)
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
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType instance)
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
      final @Nonnull KInstanceTransformedType transformed)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.lit.contains(transformed) == false,
        "Instance not already lit");
    }

    private void checkNotUnlit(
      final @Nonnull KInstanceTransformedType transformed)
      throws ConstraintError
    {
      Constraints.constrainArbitrary(
        this.unlit.contains(transformed) == false,
        "Instance not already unlit");
    }

    private void checkOpaqueLit(
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType transformed)
      throws ConstraintError
    {
      Constraints.constrainNotNull(light, "Light");
      this.checkNotUnlit(transformed);
    }

    @Override public void sceneAddInvisibleWithShadow(
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError
    {
      if (light.lightHasShadow()) {
        this.addShadowCaster(light, instance);
      }
    }

    @Override public void sceneAddOpaqueLitVisibleWithoutShadow(
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError
    {
      this.checkOpaqueLit(light, instance);
      this.addOpaqueInstance(light, instance);
      this.addLight(light);
    }

    @Override public void sceneAddOpaqueLitVisibleWithShadow(
      final @Nonnull KLightType light,
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError
    {
      this.checkOpaqueLit(light, instance);
      this.addOpaqueInstance(light, instance);
      this.sceneAddInvisibleWithShadow(light, instance);
    }

    @Override public void sceneAddOpaqueUnlit(
      final @Nonnull KInstanceTransformedOpaqueType instance)
      throws ConstraintError
    {
      this.checkNotLit(instance);

      this.unlit = this.unlit.plus(instance);
      this.unlit_opaque = this.unlit_opaque.plus(instance);
      this.visible_opaque = this.visible_opaque.plus(instance);
      this.visible_instances = this.visible_instances.plus(instance);
    }

    @Override public void sceneAddTranslucentLit(
      final @Nonnull KInstanceTransformedTranslucentRegular instance,
      final @Nonnull Set<KLightType> lights)
      throws ConstraintError
    {
      this.checkNotUnlit(instance);

      this.visible_translucent_ordered =
        this.visible_translucent_ordered.plus(new KTranslucentRegularLit(
          instance,
          lights));
      this.visible_instances = this.visible_instances.plus(instance);
    }

    @Override public void sceneAddTranslucentUnlit(
      final @Nonnull KInstanceTransformedTranslucentType instance)
      throws ConstraintError
    {
      try {
        this.checkNotLit(instance);
        this.unlit = this.unlit.plus(instance);

        this.visible_translucent_ordered =
          this.visible_translucent_ordered.plus(instance
            .transformedVisitableAccept(Builder.IDENTITY_TRANSLUCENT));

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
   * Information about the opaque instances in the current scene.
   */

  @Immutable public static final class KSceneOpaques
  {
    private final @Nonnull Set<KInstanceTransformedOpaqueType>                   all;
    private final @Nonnull Map<KLightType, List<KInstanceTransformedOpaqueType>> lit;
    private final @Nonnull Set<KInstanceTransformedOpaqueType>                   unlit;

    private KSceneOpaques(
      final @Nonnull Map<KLightType, List<KInstanceTransformedOpaqueType>> in_lit,
      final @Nonnull Set<KInstanceTransformedOpaqueType> in_unlit,
      final @Nonnull Set<KInstanceTransformedOpaqueType> visible)
    {
      this.lit = in_lit;
      this.unlit = in_unlit;
      this.all = visible;
    }

    /**
     * @return A flat list of all the visible opaque objects in the scene.
     */

    public @Nonnull Set<KInstanceTransformedOpaqueType> getAll()
    {
      return this.all;
    }

    /**
     * @return The set of opaque instances affected by each light in the
     *         scene.
     */

    public @Nonnull
      Map<KLightType, List<KInstanceTransformedOpaqueType>>
      getLitInstances()
    {
      return this.lit;
    }

    /**
     * @return The set of unlit opaque instances in the scene.
     */

    public @Nonnull Set<KInstanceTransformedOpaqueType> getUnlitInstances()
    {
      return this.unlit;
    }
  }

  /**
   * Information about the shadow casters in the current scene.
   */

  @Immutable public static final class KSceneShadows
  {
    private final @Nonnull Map<KLightType, List<KInstanceTransformedOpaqueType>> shadow_casters;

    private KSceneShadows(
      final @Nonnull Map<KLightType, List<KInstanceTransformedOpaqueType>> in_shadow_casters)
    {
      this.shadow_casters = in_shadow_casters;
    }

    /**
     * @return The set of shadow casters for each light in the scene.
     */

    public @Nonnull
      Map<KLightType, List<KInstanceTransformedOpaqueType>>
      getShadowCasters()
    {
      return this.shadow_casters;
    }
  }

  /**
   * Retrieve a new {@link KSceneBuilderWithCreateType} with which to
   * construct a scene, rendered from the perspective of <code>camera</code>.
   * 
   * @param camera
   *          The observer of the scene
   * @return A new scene builder
   * @throws ConstraintError
   *           Iff <code>camera == null</code>.
   */

  public static @Nonnull KSceneBuilderWithCreateType newBuilder(
    final @Nonnull KCamera camera)
    throws ConstraintError
  {
    return new Builder(camera);
  }

  private final @Nonnull KCamera                       camera;
  private final @Nonnull KSceneOpaques                 opaques;
  private final @Nonnull KSceneShadows                 shadows;
  private final @Nonnull List<KTranslucentType>        translucents;
  private final @Nonnull Set<KInstanceTransformedType> visible;

  private KScene(
    final @Nonnull KCamera in_camera,
    final @Nonnull KSceneOpaques in_opaques,
    final @Nonnull List<KTranslucentType> in_translucents,
    final @Nonnull KSceneShadows in_shadows,
    final @Nonnull Set<KInstanceTransformedType> in_visible)
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

  public @Nonnull KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @return The set of opaque instances in the current scene.
   */

  public @Nonnull KSceneOpaques getOpaques()
  {
    return this.opaques;
  }

  /**
   * @return The set of shadow casters in the current scene.
   */

  public @Nonnull KSceneShadows getShadows()
  {
    return this.shadows;
  }

  /**
   * @return The set of translucent instances in the current scene.
   */

  public @Nonnull List<KTranslucentType> getTranslucents()
  {
    return this.translucents;
  }

  /**
   * @return The set of all visible instances in the scene (that is, all
   *         instances that were not added as invisible shadow casters).
   */

  public @Nonnull Set<KInstanceTransformedType> getVisibleInstances()
  {
    return this.visible;
  }
}
