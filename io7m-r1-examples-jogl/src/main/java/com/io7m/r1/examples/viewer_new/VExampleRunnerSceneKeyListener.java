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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

final class VExampleRunnerSceneKeyListener implements KeyListener
{
  private final VExampleRunnerSceneType runner;

  public VExampleRunnerSceneKeyListener(
    final VExampleRunnerSceneType in_runner)
  {
    this.runner = NullCheck.notNull(in_runner);
  }

  @Override public void keyReleased(
    final @Nullable KeyEvent e)
  {
    assert e != null;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_A:
      {
        this.runner.viewSetMovingLeft(false);
        break;
      }
      case KeyEvent.VK_D:
      {
        this.runner.viewSetMovingRight(false);
        break;
      }
      case KeyEvent.VK_W:
      {
        this.runner.viewSetMovingForward(false);
        break;
      }
      case KeyEvent.VK_S:
      {
        this.runner.viewSetMovingBackward(false);
        break;
      }
      case KeyEvent.VK_E:
      {
        this.runner.viewSetMovingUp(false);
        break;
      }
      case KeyEvent.VK_Q:
      {
        this.runner.viewSetMovingDown(false);
        break;
      }

      case KeyEvent.VK_N:
      {
        this.runner.viewNext();
        break;
      }
      case KeyEvent.VK_P:
      {
        this.runner.viewPrevious();
        break;
      }
      case KeyEvent.VK_M:
      {
        this.runner.viewMouseToggle();
        break;
      }
    }
  }

  @Override public void keyPressed(
    final @Nullable KeyEvent e)
  {
    assert e != null;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_A:
      {
        this.runner.viewSetMovingLeft(true);
        break;
      }
      case KeyEvent.VK_D:
      {
        this.runner.viewSetMovingRight(true);
        break;
      }
      case KeyEvent.VK_W:
      {
        this.runner.viewSetMovingForward(true);
        break;
      }
      case KeyEvent.VK_S:
      {
        this.runner.viewSetMovingBackward(true);
        break;
      }
      case KeyEvent.VK_E:
      {
        this.runner.viewSetMovingUp(true);
        break;
      }
      case KeyEvent.VK_Q:
      {
        this.runner.viewSetMovingDown(true);
        break;
      }
    }
  }
}
