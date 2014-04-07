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

package com.io7m.renderer.kernel.sandbox;

import java.awt.Container;
import java.awt.Dimension;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;

abstract class SBExampleWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 4254766657404535893L;
  }

  public SBExampleWindow(
    final @Nonnull String title)
  {
    super(title);

    final Container content = this.getContentPane();
    final JPanel inner = new JPanel();
    final JScrollPane pane = new JScrollPane(inner);
    content.add(pane);

    final DesignGridLayout layout = new DesignGridLayout(inner);
    try {
      this.addToLayout(layout);
    } catch (final ConstraintError e) {
      e.printStackTrace();
    }
    this.setMinimumSize(new Dimension(640, 480));
    this.pack();
    this.setVisible(true);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public abstract void addToLayout(
    final @Nonnull DesignGridLayout layout)
    throws ConstraintError;
}
