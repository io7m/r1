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

import java.awt.Dimension;

import javax.annotation.Nonnull;
import javax.swing.JFrame;

final class SBLogsWindow extends JFrame
{
  private static final long         serialVersionUID;

  static {
    serialVersionUID = 4283712509235461406L;
  }

  private final @Nonnull SBLogPanel log_panel;

  public SBLogsWindow()
  {
    super("Logs");
    this.log_panel = new SBLogPanel();

    this.getContentPane().add(this.log_panel);
    this.setPreferredSize(new Dimension(800, 600));
    this.pack();
  }

  void addText(
    final @Nonnull String text)
  {
    this.log_panel.addText(text);
  }
}
