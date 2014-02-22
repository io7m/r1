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
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;

final class SBInstancesWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -674862326268846104L;
  }

  public <C extends SBSceneControllerInstances & SBSceneControllerTextures & SBSceneControllerMeshes & SBSceneControllerMaterials> SBInstancesWindow(
    final @Nonnull C controller,
    final @Nonnull Log log)
  {
    super("Instances");
    final SBInstancesControls controls =
      new SBInstancesControls(controller, log);
    final DesignGridLayout layout =
      new DesignGridLayout(this.getContentPane());
    controls.controlsAddToLayout(layout);
    this.pack();
  }
}
