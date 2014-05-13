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
import javax.swing.JFrame;
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

public final class SBMaterialControlsSpecular implements
  SBControlsDataType<SBMaterialSpecularDescription>
{
  protected final JTextField     texture;
  protected final JButton        texture_select;
  protected final SBFloatHSlider exponent;
  private final RowGroup         group;
  private final SBColourInput    colour;

  public SBMaterialControlsSpecular(
    final JFrame parent,
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
            SBMaterialControlsSpecular.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });

    this.exponent = new SBFloatHSlider("Exponent", 1.0f, 128.0f);
    this.colour = SBColourInput.newInput(parent, "Colour");
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Specularity");
    label.setForeground(Color.BLUE);
    dg.row().group(this.group).left().add(label, new JSeparator()).fill();

    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 3)
      .add(this.texture_select);

    this.colour.controlsAddToLayout(dg);

    dg
      .row()
      .group(this.group)
      .grid(this.exponent.getLabel())
      .add(this.exponent.getSlider(), 3)
      .add(this.exponent.getField());
  }

  @Override public void controlsLoadFrom(
    final SBMaterialSpecularDescription mat_s)
  {
    final PathVirtual tt = mat_s.getTexture();
    this.texture.setText(tt == null ? "" : tt.toString());
    this.exponent.setCurrent(mat_s.getExponent());
    this.colour.controlsLoadFrom(mat_s.getColour());
  }

  @Override public SBMaterialSpecularDescription controlsSave()
    throws SBExceptionInputError
  {
    try {
      final String tt = this.texture.getText();
      final PathVirtual specular_texture_value =
        (tt.equals("")) ? null : PathVirtual.ofString(tt);

      final SBMaterialSpecularDescription specular =
        new SBMaterialSpecularDescription(
          specular_texture_value,
          this.colour.controlsSave(),
          this.exponent.getCurrent());

      return specular;
    } catch (final FilesystemError x) {
      throw new UnreachableCodeException(x);
    }
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.colour.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.colour.controlsShow();
  }
}
