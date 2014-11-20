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

package com.io7m.r1.examples.viewer_new;

/**
 * The type of runners for {@link com.io7m.r1.examples.ExampleSceneType}.
 */

public interface VExampleRunnerSceneType extends VExampleRunnerType
{
  /**
   * @return The number or views in the scene.
   */

  int viewGetCount();

  /**
   * @return The index of the current view.
   */

  int viewGetIndex();

  /**
   * Indicate that the mouse moved and is now at <code>(x, y)</code>.
   *
   * @param x
   *          The x coordinate
   * @param y
   *          The y coordinate
   */

  void viewMouseMoved(
    int x,
    int y);

  /**
   * Enable/disable mouse viewing.
   */

  void viewMouseToggle();

  /**
   * Move to the next view.
   */

  void viewNext();

  /**
   * Move to the previous view.
   */

  void viewPrevious();

  /**
   * Indicate that the camera should start/stop moving backward.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingBackward(
    boolean b);

  /**
   * Indicate that the camera should start/stop moving down.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingDown(
    boolean b);

  /**
   * Indicate that the camera should start/stop moving forward.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingForward(
    boolean b);

  /**
   * Indicate that the camera should start/stop moving left.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingLeft(
    boolean b);

  /**
   * Indicate that the camera should start/stop moving right.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingRight(
    boolean b);

  /**
   * Indicate that the camera should start/stop moving up.
   * 
   * @param b
   *          <code>true</code> if the camera should start moving.
   */

  void viewSetMovingUp(
    boolean b);
}
