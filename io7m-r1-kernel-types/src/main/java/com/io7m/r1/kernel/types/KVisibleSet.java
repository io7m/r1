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

import java.util.Set;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.types.RException;
import com.io7m.r1.types.RExceptionBuilderInvalid;
import com.io7m.r1.types.RExceptionInstanceAlreadyVisible;
import com.io7m.r1.types.RExceptionLightGroupAlreadyAdded;
import com.io7m.r1.types.RExceptionLightGroupLacksInstances;
import com.io7m.r1.types.RExceptionLightGroupLacksLights;

/**
 * <p>
 * An immutable visible set.
 * </p>
 * <p>
 * The <code>KVisibleSet</code> type separates opaque and translucent objects
 * and batches them by <i>lights</i> and <i>materials</i> for rendering. A
 * visible set consists of:
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
 * The <code>KVisibleSet</code> class preserves the order of insertion for
 * translucent objects, and delegates responsibility for doing spatial
 * partitioning and sorting objects by position to the creator of the set.
 * </p>
 */

@EqualityReference public final class KVisibleSet
{
  @EqualityReference private static final class Builder implements
    KVisibleSetBuilderWithCreateType
  {
    private final KCamera                                      camera;
    private final KVisibleSetOpaquesBuilderWithCreateType      opaque_builder;
    private final KVisibleSetShadowsBuilderWithCreateType      shadow_builder;
    private final KVisibleSetTranslucentsBuilderWithCreateType translucent_builder;

    Builder(
      final KCamera in_camera)
    {
      this.shadow_builder = KVisibleSetShadows.newBuilder();
      this.opaque_builder =
        KVisibleSetOpaques.newBuilder(in_camera, this.shadow_builder);
      this.translucent_builder =
        KVisibleSetTranslucents.newBuilder(in_camera);
      this.camera = NullCheck.notNull(in_camera, "Camera");
    }

    @SuppressWarnings("synthetic-access") @Override public
      KVisibleSet
      visibleCreate()
        throws RExceptionLightGroupLacksInstances,
          RExceptionLightGroupLacksLights,
          RExceptionBuilderInvalid
    {
      final KVisibleSetOpaques ob = this.opaque_builder.visibleOpaqueCreate();
      return new KVisibleSet(
        this.camera,
        ob,
        ob.getShadows(),
        this.translucent_builder.visibleTranslucentsCreate());
    }

    @Override public void visibleShadowsAddCaster(
      final KLightWithShadowType light,
      final KInstanceOpaqueType instance)
      throws RExceptionBuilderInvalid
    {
      this.shadow_builder.visibleShadowsAddCaster(light, instance);
    }

    @Override public void visibleOpaqueAddUnlit(
      final KInstanceOpaqueType instance)
      throws RExceptionBuilderInvalid,
        RExceptionInstanceAlreadyVisible
    {
      this.opaque_builder.visibleOpaqueAddUnlit(instance);
    }

    @Override public
      KVisibleSetLightGroupBuilderType
      visibleOpaqueNewLightGroup(
        final String name)
        throws RExceptionLightGroupAlreadyAdded,
          RExceptionBuilderInvalid
    {
      return this.opaque_builder.visibleOpaqueNewLightGroup(name);
    }

    @Override public void visibleTranslucentsAddLit(
      final KInstanceTranslucentLitType instance,
      final Set<KLightTranslucentType> lights)
      throws RExceptionBuilderInvalid
    {
      this.translucent_builder.visibleTranslucentsAddLit(instance, lights);
    }

    @Override public void visibleTranslucentsAddUnlit(
      final KInstanceTranslucentUnlitType instance)
      throws RExceptionBuilderInvalid
    {
      this.translucent_builder.visibleTranslucentsAddUnlit(instance);
    }

    @Override public void visibleShadowsAddLight(
      final KLightWithShadowType light)
      throws RExceptionBuilderInvalid
    {
      this.shadow_builder.visibleShadowsAddLight(light);
    }
  }

  static String getOpaqueMaterialUnlitCode(
    final KInstanceOpaqueType instance)
  {
    try {
      return instance
        .opaqueAccept(new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
          @Override public String regular(
            final KInstanceOpaqueRegular o)
          {
            return o.getMaterial().materialGetUnlitCode();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  static String getOpaqueMaterialLitCode(
    final KInstanceOpaqueType instance)
  {
    try {
      return instance
        .opaqueAccept(new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
          @Override public String regular(
            final KInstanceOpaqueRegular o)
          {
            return o.getMaterial().materialGetLitCode();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  static String getOpaqueMaterialDepthCode(
    final KInstanceOpaqueType instance)
  {
    try {
      return instance
        .opaqueAccept(new KInstanceOpaqueVisitorType<String, UnreachableCodeException>() {
          @Override public String regular(
            final KInstanceOpaqueRegular o)
          {
            return o.getMaterial().materialOpaqueGetDepth().codeGet();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    }
  }

  /**
   * @return A new visible set builder.
   * @param in_camera
   *          The camera for the visible set
   */

  public static KVisibleSetBuilderWithCreateType newBuilder(
    final KCamera in_camera)
  {
    return new Builder(in_camera);
  }

  private final KCamera                 camera;
  private final KVisibleSetOpaques      opaques;
  private final KVisibleSetShadows      shadows;
  private final KVisibleSetTranslucents translucents;

  private KVisibleSet(
    final KCamera in_camera,
    final KVisibleSetOpaques in_opaques,
    final KVisibleSetShadows in_shadows,
    final KVisibleSetTranslucents in_translucents)
  {
    this.camera = NullCheck.notNull(in_camera, "Camera");
    this.opaques = NullCheck.notNull(in_opaques, "Opaques");
    this.shadows = NullCheck.notNull(in_shadows, "Shadows");
    this.translucents = NullCheck.notNull(in_translucents, "Translucents");
  }

  /**
   * @return The camera for the visible set
   */

  public KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @return The opaque instances
   */

  public KVisibleSetOpaques getOpaques()
  {
    return this.opaques;
  }

  /**
   * @return The shadow casting instances
   */

  public KVisibleSetShadows getShadows()
  {
    return this.shadows;
  }

  /**
   * @return The translucent instances
   */

  public KVisibleSetTranslucents getTranslucents()
  {
    return this.translucents;
  }
}
