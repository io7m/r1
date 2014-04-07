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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

final class SBInputState
{
  private final @Nonnull AtomicBoolean moving_forward;
  private final @Nonnull AtomicBoolean moving_backward;
  private final @Nonnull AtomicBoolean moving_left;
  private final @Nonnull AtomicBoolean moving_right;
  private final @Nonnull AtomicBoolean moving_up;
  private final @Nonnull AtomicBoolean moving_down;
  private final @Nonnull AtomicBoolean rotating_left;
  private final @Nonnull AtomicBoolean rotating_right;
  private final @Nonnull AtomicBoolean rotating_up;
  private final @Nonnull AtomicBoolean rotating_down;
  private final @Nonnull AtomicBoolean want_next_camera;
  private final @Nonnull AtomicBoolean want_pause_toggle;
  private final @Nonnull AtomicBoolean want_step_one;
  private final @Nonnull AtomicBoolean want_framebuffer_snapshot;
  private final @Nonnull AtomicBoolean want_dump_shadow_maps;

  public SBInputState()
  {
    this.moving_forward = new AtomicBoolean();
    this.moving_backward = new AtomicBoolean();
    this.moving_left = new AtomicBoolean();
    this.moving_right = new AtomicBoolean();
    this.moving_up = new AtomicBoolean();
    this.moving_down = new AtomicBoolean();
    this.rotating_left = new AtomicBoolean();
    this.rotating_right = new AtomicBoolean();
    this.rotating_up = new AtomicBoolean();
    this.rotating_down = new AtomicBoolean();
    this.want_next_camera = new AtomicBoolean();
    this.want_pause_toggle = new AtomicBoolean();
    this.want_step_one = new AtomicBoolean();
    this.want_framebuffer_snapshot = new AtomicBoolean();
    this.want_dump_shadow_maps = new AtomicBoolean();
  }

  boolean isMovingBackward()
  {
    return this.moving_backward.get();
  }

  boolean isMovingDown()
  {
    return this.moving_down.get();
  }

  boolean isMovingForward()
  {
    return this.moving_forward.get();
  }

  boolean isMovingLeft()
  {
    return this.moving_left.get();
  }

  boolean isMovingRight()
  {
    return this.moving_right.get();
  }

  boolean isMovingUp()
  {
    return this.moving_up.get();
  }

  boolean isRotatingDown()
  {
    return this.rotating_down.get();
  }

  boolean isRotatingLeft()
  {
    return this.rotating_left.get();
  }

  boolean isRotatingRight()
  {
    return this.rotating_right.get();
  }

  boolean isRotatingUp()
  {
    return this.rotating_up.get();
  }

  void setMovingBackward(
    final boolean r)
  {
    this.moving_backward.set(r);
  }

  void setMovingDown(
    final boolean in_moving_down)
  {
    this.moving_down.set(in_moving_down);
  }

  void setMovingForward(
    final boolean r)
  {
    this.moving_forward.set(r);
  }

  void setMovingLeft(
    final boolean in_moving_left)
  {
    this.moving_left.set(in_moving_left);
  }

  void setMovingRight(
    final boolean in_moving_right)
  {
    this.moving_right.set(in_moving_right);
  }

  void setMovingUp(
    final boolean in_moving_up)
  {
    this.moving_up.set(in_moving_up);
  }

  void setRotatingDown(
    final boolean r)
  {
    this.rotating_down.set(r);
  }

  void setRotatingLeft(
    final boolean r)
  {
    this.rotating_left.set(r);
  }

  void setRotatingRight(
    final boolean r)
  {
    this.rotating_right.set(r);
  }

  void setRotatingUp(
    final boolean r)
  {
    this.rotating_up.set(r);
  }

  void setWantFramebufferSnapshot(
    final boolean b)
  {
    this.want_framebuffer_snapshot.set(b);
  }

  void setWantNextCamera(
    final boolean b)
  {
    this.want_next_camera.set(b);
  }

  void setWantPauseToggle(
    final boolean b)
  {
    this.want_pause_toggle.set(b);
  }

  void setWantStepOneFrame(
    final boolean b)
  {
    this.want_step_one.set(b);
  }

  boolean wantFramebufferSnaphot()
  {
    return this.want_framebuffer_snapshot.getAndSet(false);
  }

  boolean wantNextCamera()
  {
    return this.want_next_camera.getAndSet(false);
  }

  boolean wantPauseToggle()
  {
    return this.want_pause_toggle.getAndSet(false);
  }

  boolean wantStepOneFrame()
  {
    return this.want_step_one.getAndSet(false);
  }

  boolean wantShadowMapDump()
  {
    return this.want_dump_shadow_maps.getAndSet(false);
  }

  void setWantDumpShadowMaps()
  {
    this.want_dump_shadow_maps.set(true);
  }
}
