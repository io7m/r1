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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.swing.JLabel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

public final class SBErrorField implements SBControls
{
  private final @Nonnull JLabel   error_text;
  private final @Nonnull JLabel   error_icon;
  private final @Nonnull RowGroup group;

  public SBErrorField()
    throws IOException
  {
    this.group = new RowGroup();
    this.error_text = new JLabel("Some informative error text");
    this.error_icon = SBIcons.makeErrorIcon();
    this.error_text.setVisible(false);
    this.error_icon.setVisible(false);
  }

  public void errorSet(
    final @Nonnull String message)
  {
    this.error_text.setText(message);
    this.error_text.setVisible(true);
    this.error_icon.setVisible(true);
  }

  public void errorUnset()
  {
    this.error_text.setVisible(false);
    this.error_icon.setVisible(false);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    layout.row().group(this.group).grid(this.error_icon).add(this.error_text);
  }
}
