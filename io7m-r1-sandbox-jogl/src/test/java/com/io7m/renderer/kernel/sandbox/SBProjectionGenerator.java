/*
 * Copyright © 2013 <code@io7m.com> http://io7m.com
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

package com.io7m.renderer.kernel.sandbox;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.support.IntegerGenerator;

import com.io7m.junreachable.UnreachableCodeException;

public final class SBProjectionGenerator implements
  Generator<SBProjectionDescription>
{
  private final IntegerGenerator index_gen;

  public SBProjectionGenerator()
  {
    this.index_gen =
      new IntegerGenerator(
        0,
        SBProjectionDescription.Type.values().length - 1);
  }

  @Override public SBProjectionDescription next()
  {
    switch (SBProjectionDescription.Type.values()[this.index_gen
      .next()
      .intValue()]) {
      case PROJECTION_FRUSTUM:
      {
        final double left = Math.random() * -100.0;
        final double right = Math.random() * 100.0;
        final double bottom = Math.random() * -100.0;
        final double top = Math.random() * 100.0;
        final double near = Math.random();
        final double far = (Math.random() * 100.0) + near;
        return new SBProjectionDescription.SBProjectionFrustum(
          left,
          right,
          bottom,
          top,
          near,
          far);
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        final double left = Math.random() * -100.0;
        final double right = Math.random() * 100.0;
        final double bottom = Math.random() * -100.0;
        final double top = Math.random() * 100.0;
        final double near = Math.random();
        final double far = (Math.random() * 100.0) + near;
        return new SBProjectionDescription.SBProjectionOrthographic(
          left,
          right,
          bottom,
          top,
          near,
          far);
      }
      case PROJECTION_PERSPECTIVE:
      {
        final double near = Math.random();
        final double far = (Math.random() * 100.0) + near;
        final double aspect = Math.random() * 2.0;
        final double horizontal_fov = Math.random() * (Math.PI * 2);
        return new SBProjectionDescription.SBProjectionPerspective(
          near,
          far,
          aspect,
          horizontal_fov);
      }
    }

    throw new UnreachableCodeException();
  }
}
