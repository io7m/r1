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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;

public final class SBMaterialControlsNormal implements
  SBControlsDataType<SBMaterialNormalDescription>
{
  private final @Nonnull RowGroup     group;
  protected final @Nonnull JTextField texture;
  protected final @Nonnull JButton    texture_select;

  public SBMaterialControlsNormal(
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull Log log)
  {
    this.group = new RowGroup();

    this.texture = new JTextField();
    this.texture.setEditable(false);
    this.texture_select = new JButton("Select...");
    this.texture_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final SBTextures2DWindow twindow =
          new SBTextures2DWindow(
            controller,
            SBMaterialControlsNormal.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Normal mapping");
    label.setForeground(Color.BLUE);
    dg.row().group(this.group).left().add(label, new JSeparator()).fill();

    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 3)
      .add(this.texture_select);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialNormalDescription mat_n)
  {
    final PathVirtual tt = mat_n.getTexture();
    this.texture.setText(tt == null ? "" : tt.toString());
  }

  @Override public @Nonnull SBMaterialNormalDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    final String tt = this.texture.getText();
    final PathVirtual texture_normal =
      (tt.equals("")) ? null : PathVirtual.ofString(tt);

    return new SBMaterialNormalDescription(texture_normal);
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

}
