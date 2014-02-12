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
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBMaterialControlsEnvironment implements
  SBControlsDataType<SBMaterialEnvironmentDescription>
{
  protected final @Nonnull JTextField                   texture;
  protected final @Nonnull JButton                      texture_select;
  protected final @Nonnull SBFloatHSlider               mix;
  protected final @Nonnull SBEnvironmentMixTypeSelector mix_type;
  private final @Nonnull RowGroup                       group;

  public SBMaterialControlsEnvironment(
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.group = new RowGroup();

    this.texture = new JTextField();
    this.texture.setEditable(false);
    this.texture_select = new JButton("Select...");
    this.texture_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final SBTexturesCubeWindow twindow =
          new SBTexturesCubeWindow(
            controller,
            SBMaterialControlsEnvironment.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });

    this.mix = new SBFloatHSlider("Mix", 0.0f, 1.0f);
    this.mix_type = new SBEnvironmentMixTypeSelector();
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Environment mapping");
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
      .grid(this.mix.getLabel())
      .add(this.mix.getSlider(), 3)
      .add(this.mix.getField());

    dg.emptyRow();
    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Mix type"))
      .add(this.mix_type);
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialEnvironmentDescription mat_e)
  {
    final PathVirtual tt = mat_e.getTexture();
    this.texture.setText(tt == null ? "" : tt.toString());
    this.mix.setCurrent(mat_e.getMix());
    this.mix_type.setSelectedItem(mat_e.getMixType());
  }

  @Override public @Nonnull SBMaterialEnvironmentDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    final String tt = this.texture.getText();
    final PathVirtual environment_texture_value =
      (tt.equals("")) ? null : PathVirtual.ofString(tt);

    final SBMaterialEnvironmentDescription environment =
      new SBMaterialEnvironmentDescription(
        environment_texture_value,
        this.mix.getCurrent(),
        this.mix_type.getSelectedItem());

    return environment;
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}
