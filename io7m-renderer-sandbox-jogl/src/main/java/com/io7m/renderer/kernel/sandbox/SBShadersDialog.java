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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;

public final class SBShadersDialog extends JDialog
{
  private static final long    serialVersionUID;

  static {
    serialVersionUID = 4894778262966895041L;
  }

  private final SBShadersPanel panel;

  public SBShadersDialog(
    final JFrame owner,
    final LogUsableType log,
    final SBSceneControllerShaders controller)
  {
    this.panel = new SBShadersPanel(owner, controller, log);

    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBWindowUtilities.closeDialog(SBShadersDialog.this);
      }
    });

    final JButton ok = new JButton("OK");
    ok.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBWindowUtilities.closeDialog(SBShadersDialog.this);
      }
    });

    final Container pane = this.getContentPane();
    final DesignGridLayout dg = new DesignGridLayout(pane);
    dg.row().grid().add(this.panel);
    dg.row().grid().add(cancel).add(ok);

    this.pack();
  }

  public @Nullable SBShader getSelectedShader()
  {
    return this.panel.getSelectedShader();
  }
}
