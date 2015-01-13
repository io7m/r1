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
import java.util.List;
import java.util.Set;

import com.io7m.jequality.annotations.EqualityReference;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.exceptions.RException;
import com.io7m.r1.exceptions.RExceptionBuilderInvalid;

/**
 * An immutable set of visible translucent instances.
 */

@EqualityReference public final class KVisibleSetTranslucents
{
  @EqualityReference private static final class Builder implements
    KVisibleSetTranslucentsBuilderWithCreateType
  {
    private final KCamera                camera;
    private boolean                      done;
    private final List<KTranslucentType> instances;

    Builder(
      final KCamera in_camera)
    {
      this.instances = new ArrayList<KTranslucentType>();
      this.done = false;
      this.camera = NullCheck.notNull(in_camera, "Camera");
    }

    private void checkValid()
      throws RExceptionBuilderInvalid
    {
      if (this.done == true) {
        throw new RExceptionBuilderInvalid("Builder has been invalidated");
      }
    }

    @Override public void visibleTranslucentsAddLit(
      final KInstanceTranslucentLitType instance,
      final Set<KLightTranslucentType> lights)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(instance, "Instance");
      NullCheck.notNullAll(lights, "Lights");
      this.checkValid();

      try {
        this.instances
          .add(instance
            .translucentLitAccept(new KInstanceTranslucentLitVisitorType<KTranslucentType, UnreachableCodeException>() {
              @Override public KTranslucentType regular(
                final KInstanceTranslucentRegular i)
              {
                return new KTranslucentRegularLit(i, lights);
              }

              @Override public KTranslucentType specularOnly(
                final KInstanceTranslucentSpecularOnly i)
              {
                return new KTranslucentSpecularOnlyLit(i, lights);
              }
            }));
      } catch (final RException e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public void visibleTranslucentsAddUnlit(
      final KInstanceTranslucentUnlitType instance)
      throws RExceptionBuilderInvalid
    {
      NullCheck.notNull(instance, "Instance");
      this.checkValid();
      this.instances.add(instance);
    }

    @SuppressWarnings("synthetic-access") @Override public
      KVisibleSetTranslucents
      visibleTranslucentsCreate()
        throws RExceptionBuilderInvalid
    {
      this.checkValid();
      this.done = true;
      return new KVisibleSetTranslucents(this.camera, this.instances);
    }
  }

  /**
   * @return A new translucent set builder
   * @param camera
   *          The camera for the visible set
   */

  public static KVisibleSetTranslucentsBuilderWithCreateType newBuilder(
    final KCamera camera)
  {
    return new Builder(camera);
  }

  private final KCamera                camera;
  private final List<KTranslucentType> instances;

  private KVisibleSetTranslucents(
    final KCamera in_camera,
    final List<KTranslucentType> in_instances)
  {
    this.camera = NullCheck.notNull(in_camera, "Camera");
    this.instances =
      NullCheck.notNull(Collections.unmodifiableList(NullCheck.notNullAll(
        in_instances,
        "Instances")));
  }

  /**
   * @return The camera for the visible set
   */

  public KCamera getCamera()
  {
    return this.camera;
  }

  /**
   * @return The list of translucent instances
   */

  public List<KTranslucentType> getInstances()
  {
    return this.instances;
  }
}
