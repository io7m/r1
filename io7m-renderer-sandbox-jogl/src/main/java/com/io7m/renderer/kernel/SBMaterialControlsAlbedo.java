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
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RSpaceRGBA;
import com.io7m.renderer.types.RVectorI4F;

public final class SBMaterialControlsAlbedo implements
  SBControlsDataType<SBMaterialAlbedoDescription>
{
  private final @Nonnull RowGroup         group;
  protected final @Nonnull JTextField     r;
  protected final @Nonnull JTextField     g;
  protected final @Nonnull JTextField     b;
  protected final @Nonnull JTextField     a;
  protected final @Nonnull JButton        colour_select;
  protected final @Nonnull JTextField     texture;
  protected final @Nonnull JButton        texture_select;
  protected final @Nonnull SBFloatHSlider texture_mix;

  SBMaterialControlsAlbedo(
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull JFrame parent,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.group = new RowGroup();

    this.r = new JTextField("1.0");
    this.g = new JTextField("1.0");
    this.b = new JTextField("1.0");
    this.a = new JTextField("1.0");
    this.colour_select = new JButton("Select...");
    this.colour_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final Color c =
          JColorChooser.showDialog(parent, "Select colour...", Color.WHITE);
        if (c != null) {
          final float[] rgb = c.getRGBColorComponents(null);
          SBMaterialControlsAlbedo.this.r.setText(Float.toString(rgb[0]));
          SBMaterialControlsAlbedo.this.g.setText(Float.toString(rgb[1]));
          SBMaterialControlsAlbedo.this.b.setText(Float.toString(rgb[2]));
        }
      }
    });

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
            SBMaterialControlsAlbedo.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });

    this.texture_mix = new SBFloatHSlider("Mix", 0.0f, 1.0f);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout dg)
  {
    final JLabel label = new JLabel("Albedo");
    label.setForeground(Color.BLUE);
    dg.row().group(this.group).left().add(label, new JSeparator()).fill();

    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Colour"))
      .add(this.r)
      .add(this.g)
      .add(this.b)
      .add(this.a)
      .add(this.colour_select);

    dg
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 4)
      .add(this.texture_select);

    dg
      .row()
      .group(this.group)
      .grid(this.texture_mix.getLabel())
      .add(this.texture_mix.getSlider(), 4)
      .add(this.texture_mix.getField());
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBMaterialAlbedoDescription mat_d)
  {
    this.r.setText(Float.toString(mat_d.getColour().x));
    this.g.setText(Float.toString(mat_d.getColour().y));
    this.b.setText(Float.toString(mat_d.getColour().z));
    this.a.setText(Float.toString(mat_d.getColour().w));

    final PathVirtual t = mat_d.getTexture();
    if (t != null) {
      this.texture.setText(t.toString());
    }

    this.texture_mix.setCurrent(mat_d.getMix());
  }

  @Override public @Nonnull SBMaterialAlbedoDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    final RVectorI4F<RSpaceRGBA> albedo_colour =
      new RVectorI4F<RSpaceRGBA>(
        SBTextFieldUtilities.getFieldFloatOrError(this.r),
        SBTextFieldUtilities.getFieldFloatOrError(this.g),
        SBTextFieldUtilities.getFieldFloatOrError(this.b),
        SBTextFieldUtilities.getFieldFloatOrError(this.a));

    final String tt = this.texture.getText();

    final PathVirtual albedo_texture_value =
      (tt.equals("")) ? null : PathVirtual.ofString(tt);

    final SBMaterialAlbedoDescription albedo =
      new SBMaterialAlbedoDescription(
        albedo_colour,
        this.texture_mix.getCurrent(),
        albedo_texture_value);

    return albedo;
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
