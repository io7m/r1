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

package com.io7m.renderer.kernel;

import javax.annotation.Nonnull;

import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorM3F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.renderer.RMatrixI4x4F;
import com.io7m.renderer.RTransformView;

final class SBFirstPersonCamera
{
  private static final @Nonnull VectorI3F UP;

  static {
    UP = new VectorI3F(0, 1, 0);
  }

  static void makePitchYawVector(
    final double pitch,
    final double yaw,
    final @Nonnull VectorM3F out)
  {
    out.x = (float) ((-Math.cos(pitch)) * -Math.sin(yaw));
    out.y = (float) Math.sin(pitch);
    out.z = (float) (Math.cos(pitch) * -Math.cos(yaw));
  }

  static void makeYawVector(
    final double yaw,
    final @Nonnull VectorM3F out)
  {
    out.x = (float) Math.sin(yaw);
    out.y = 0.0f;
    out.z = (float) -Math.cos(yaw);
  }

  private double                             input_yaw;
  private double                             input_pitch;
  private final @Nonnull VectorM3F           input_position;

  private final @Nonnull VectorM3F           derived_target;
  private final @Nonnull VectorM3F           derived_forward;
  private final @Nonnull VectorM3F           derived_right;
  private final @Nonnull MatrixM4x4F         derived_matrix;
  private final @Nonnull MatrixM4x4F.Context context;

  SBFirstPersonCamera(
    final float x,
    final float y,
    final float z)
  {
    this.input_position = new VectorM3F(x, y, z);
    this.input_yaw = 0.0;
    this.input_pitch = 0.0;

    this.derived_forward = new VectorM3F();
    this.derived_right = new VectorM3F();
    this.derived_target = new VectorM3F();
    this.derived_matrix = new MatrixM4x4F();
    this.context = new MatrixM4x4F.Context();
  }

  @Nonnull VectorReadable3F getPosition()
  {
    return this.input_position;
  }

  @Nonnull RMatrixI4x4F<RTransformView> makeViewMatrix()
  {
    SBFirstPersonCamera.makePitchYawVector(
      this.input_pitch,
      this.input_yaw,
      this.derived_forward);
    VectorM3F.normalizeInPlace(this.derived_forward);

    VectorM3F.copy(this.input_position, this.derived_target);
    VectorM3F.addInPlace(this.derived_target, this.derived_forward);

    MatrixM4x4F.lookAtWithContext(
      this.context,
      this.input_position,
      this.derived_target,
      SBFirstPersonCamera.UP,
      this.derived_matrix);

    return new RMatrixI4x4F<RTransformView>(this.derived_matrix);
  }

  void moveBackward(
    final double r)
  {
    this.moveForward(-r);
  }

  void moveDown(
    final double r)
  {
    this.input_position.y -= r;
  }

  void moveForward(
    final double r)
  {
    SBFirstPersonCamera.makePitchYawVector(
      this.input_pitch,
      this.input_yaw,
      this.derived_forward);
    VectorM3F.normalizeInPlace(this.derived_forward);
    VectorM3F.scaleInPlace(this.derived_forward, r);
    VectorM3F.addInPlace(this.input_position, this.derived_forward);
  }

  void moveRotateDown(
    final double r)
  {
    this.input_pitch -= r;
  }

  void moveRotateLeft(
    final double r)
  {
    this.moveRotateRight(-r);
  }

  void moveRotateRight(
    final double r)
  {
    this.input_yaw += r;
  }

  void moveRotateUp(
    final double r)
  {
    this.input_pitch += r;
  }

  void moveStrafeLeft(
    final double r)
  {
    this.moveStrafeRight(-r);
  }

  void moveStrafeRight(
    final double r)
  {
    SBFirstPersonCamera.makePitchYawVector(
      this.input_pitch,
      this.input_yaw,
      this.derived_forward);
    VectorM3F.normalizeInPlace(this.derived_forward);
    VectorM3F.crossProduct(
      this.derived_forward,
      SBFirstPersonCamera.UP,
      this.derived_right);

    VectorM3F.scaleInPlace(this.derived_right, r);
    VectorM3F.addInPlace(this.input_position, this.derived_right);
  }

  void moveUp(
    final double r)
  {
    this.input_position.y += r;
  }

  void setPosition(
    final @Nonnull VectorReadable3F position)
  {
    VectorM3F.copy(position, this.input_position);
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[SBFPSCamera [yaw ");
    builder.append(this.input_yaw);
    builder.append("] [pitch ");
    builder.append(this.input_pitch);
    builder.append("] [position ");
    builder.append(this.input_position);
    builder.append("]");
    return builder.toString();
  }
}
