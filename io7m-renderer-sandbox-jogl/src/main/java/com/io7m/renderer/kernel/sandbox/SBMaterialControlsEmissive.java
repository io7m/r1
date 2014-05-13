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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;

public final class SBMaterialControlsEmissive implements
  SBControlsDataType<SBMaterialEmissiveDescription>
{
  protected final JTextField     texture;
  protected final JButton        texture_select;
  protected final SBFloatHSlider level;
  private final RowGroup         group;

  public SBMaterialControlsEmissive(
    final SBSceneControllerTextures controller,
    final LogUsableType log)
  {
    this.group = new RowGroup();

    this.texture = new JTextField();
    this.texture.setEditable(false);
    this.texture_select = new JButton("Select...");
    this.texture_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        final SBTextures2DWindow twindow =
          new SBTextures2DWindow(
            controller,
            SBMaterialControlsEmissive.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });

    this.level = new SBFloatHSlider("Emission", 0.0f, 1.0f);
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Emissive");
    label.setForeground(Color.BLUE);
    dg.row().group(this.group).left().add(label, new JSeparator()).fill();

    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 3)
      .add(this.texture_select);

    dg
      .row()
      .group(this.group)
      .grid(this.level.getLabel())
      .add(this.level.getSlider(), 3)
      .add(this.level.getField());
  }

  @Override public SBMaterialEmissiveDescription controlsSave()
    throws SBExceptionInputError
  {
    try {
      final String tt = this.texture.getText();
      final PathVirtual path =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      return new SBMaterialEmissiveDescription(this.level.getCurrent(), path);
    } catch (final FilesystemError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

  @Override public void controlsLoadFrom(
    final SBMaterialEmissiveDescription mat_m)
  {
    this.level.setCurrent(mat_m.getEmission());
    final PathVirtual tt = mat_m.getTexture();
    this.texture.setText(tt == null ? "" : tt.toString());
  }
}
