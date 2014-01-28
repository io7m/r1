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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.QuaternionM4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.RTransformModel;
import com.io7m.renderer.RVectorI3F;

@Immutable abstract class KTransform implements KTransformVisitable
{
  @Immutable static final class KTransformMatrix4x4 extends KTransform
  {
    private final @Nonnull RMatrixI4x4F<RTransformModel> model;

    KTransformMatrix4x4(
      final @Nonnull RMatrixI4x4F<RTransformModel> model)
    {
      this.model = model;
    }

    public @Nonnull RMatrixI4x4F<RTransformModel> getModel()
    {
      return this.model;
    }

    @Override void transformMakeMatrix4x4F(
      final @Nonnull KTransformContext context,
      final @Nonnull MatrixM4x4F m)
    {
      this.model.makeMatrixM4x4F(m);
    }

    @Override public
      <A, E extends Throwable, V extends KTransformVisitor<A, E>>
      A
      transformVisitableAccept(
        final @Nonnull V v)
        throws E
    {
      return v.transformMatrix4x4Visit(this);
    }
  }

  @Immutable static final class KTransformOST extends KTransform
  {
    private final @Nonnull QuaternionI4F           orientation;
    private final @Nonnull VectorI3F               scale;
    private final @Nonnull RVectorI3F<RSpaceWorld> translation;

    KTransformOST(
      final @Nonnull QuaternionI4F orientation,
      final @Nonnull VectorI3F scale,
      final @Nonnull RVectorI3F<RSpaceWorld> translation)
    {
      this.translation = translation;
      this.scale = scale;
      this.orientation = orientation;
    }

    public @Nonnull QuaternionI4F getOrientation()
    {
      return this.orientation;
    }

    public @Nonnull VectorI3F getScale()
    {
      return this.scale;
    }

    public @Nonnull RVectorI3F<RSpaceWorld> getTranslation()
    {
      return this.translation;
    }

    @Override void transformMakeMatrix4x4F(
      final @Nonnull KTransformContext context,
      final @Nonnull MatrixM4x4F m)
    {
      MatrixM4x4F.setIdentity(m);
      MatrixM4x4F.translateByVector3FInPlace(m, this.translation);

      MatrixM4x4F.set(m, 0, 0, m.get(0, 0) * this.scale.x);
      MatrixM4x4F.set(m, 1, 1, m.get(1, 1) * this.scale.y);
      MatrixM4x4F.set(m, 2, 2, m.get(2, 2) * this.scale.z);

      QuaternionM4F.makeRotationMatrix4x4(
        this.orientation,
        context.t_matrix4x4);
      MatrixM4x4F.multiplyInPlace(m, context.t_matrix4x4);
    }

    @Override public
      <A, E extends Throwable, V extends KTransformVisitor<A, E>>
      A
      transformVisitableAccept(
        final @Nonnull V v)
        throws E
    {
      return v.transformOSTVisit(this);
    }
  }

  public static @Nonnull KTransform newMatrixTransform(
    final @Nonnull RMatrixI4x4F<RTransformModel> model)
  {
    return new KTransformMatrix4x4(model);
  }

  public static @Nonnull KTransform newOSTTransform(
    final @Nonnull QuaternionI4F orientation,
    final @Nonnull VectorI3F scale,
    final @Nonnull RVectorI3F<RSpaceWorld> translation)
  {
    return new KTransformOST(orientation, scale, translation);
  }

  /**
   * Produce a 4x4 matrix for the current transformation, writing the
   * resulting matrix to <code>m</code>.
   * 
   * @throws ConstraintError
   *           Iff <code>m == null</code>.
   */

  abstract void transformMakeMatrix4x4F(
    final @Nonnull KTransformContext context,
    final @Nonnull MatrixM4x4F m);
}
