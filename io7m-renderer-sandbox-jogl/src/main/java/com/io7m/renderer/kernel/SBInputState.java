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

import java.util.concurrent.atomic.AtomicBoolean;

final class SBInputState
{
  private final AtomicBoolean moving_left;
  private final AtomicBoolean moving_right;
  private final AtomicBoolean moving_up;
  private final AtomicBoolean moving_down;

  public SBInputState()
  {
    this.moving_left = new AtomicBoolean();
    this.moving_right = new AtomicBoolean();
    this.moving_up = new AtomicBoolean();
    this.moving_down = new AtomicBoolean();
  }

  boolean isMovingDown()
  {
    return this.moving_down.get();
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

  void setMovingDown(
    final boolean moving_down)
  {
    this.moving_down.set(moving_down);
  }

  void setMovingLeft(
    final boolean moving_left)
  {
    this.moving_left.set(moving_left);
  }

  void setMovingRight(
    final boolean moving_right)
  {
    this.moving_right.set(moving_right);
  }

  void setMovingUp(
    final boolean moving_up)
  {
    this.moving_up.set(moving_up);
  }
}
