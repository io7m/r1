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

import java.awt.Dimension;
import java.awt.Font;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.java.dev.designgridlayout.DesignGridLayout;

final class SBLogPanel extends JPanel
{
  private static final long        serialVersionUID;

  static {
    serialVersionUID = -7319547005472170348L;
  }

  private final @Nonnull JTextArea log_area;

  public SBLogPanel()
  {
    this.log_area = new JTextArea();
    this.log_area.setEditable(false);
    this.log_area.setFont(Font.decode(Font.MONOSPACED + " 9"));

    final JScrollPane log_pane = new JScrollPane(this.log_area);
    log_pane.setPreferredSize(new Dimension(640, 480));

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(log_pane);
  }

  void addText(
    final @Nonnull String text)
  {
    this.log_area.append(text);
  }
}
