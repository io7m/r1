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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.KShadow.Type;
import com.io7m.renderer.kernel.SBLightShadowDescription.SBLightShadowMappedBasicDescription;

public final class SBLightShadowControls
{
  private static final class SBLightShadowMappedBasicControls
  {
    private final @Nonnull SBIntegerHSlider slider;

    private SBLightShadowMappedBasicControls()
      throws ConstraintError
    {
      this.slider = new SBIntegerHSlider("Size", 1, 10);
    }

    private void addToGroup(
      final @Nonnull IRowCreator group)
    {
      group
        .grid()
        .add(this.slider.getLabel())
        .add(this.slider.getSlider(), 3)
        .add(this.slider.getField());
    }

    public @Nonnull SBLightShadowMappedBasicDescription getShadow()
      throws ConstraintError
    {
      return SBLightShadowDescription.newShadowMappedBasic(this.slider
        .getCurrent());
    }

    private void loadFrom(
      final @Nonnull SBLightShadowMappedBasicDescription desc)
    {
      this.slider.setCurrent(desc.getSize());
    }
  }

  private final @Nonnull JCheckBox                        shadow;
  private final @Nonnull SBLightShadowTypeSelector        type_select;
  private final @Nonnull SBLightShadowMappedBasicControls mapped_basic_controls;
  private final @Nonnull RowGroup                         mapped_basic_group;

  @SuppressWarnings("synthetic-access") private SBLightShadowControls()
    throws ConstraintError
  {
    this.mapped_basic_controls = new SBLightShadowMappedBasicControls();
    this.mapped_basic_group = new RowGroup();

    this.type_select = new SBLightShadowTypeSelector();
    this.type_select.setEnabled(false);
    this.type_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        SBLightShadowControls.this.showAndHideControls();
      }
    });

    this.shadow = new JCheckBox();
    this.shadow.setSelected(false);
    this.shadow.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        if (SBLightShadowControls.this.shadow.isSelected() == false) {
          SBLightShadowControls.this.type_select.setEnabled(false);
          SBLightShadowControls.this.hideAllControls();
        } else {
          SBLightShadowControls.this.type_select.setEnabled(true);
          SBLightShadowControls.this.showAndHideControls();
        }
      }
    });

    this.hideAllControls();
  }

  protected void hideAllControls()
  {
    this.mapped_basic_group.hide();
  }

  @SuppressWarnings("synthetic-access") public void addToLayout(
    final @Nonnull IRowCreator group)
  {
    group.grid().add(this.shadow).add(new JLabel("Shadow"), 4);
    group.grid().add(new JLabel("Type")).add(this.type_select, 4);

    this.mapped_basic_controls.addToGroup(group
      .group(this.mapped_basic_group));
    this.mapped_basic_group.hide();
  }

  public static @Nonnull SBLightShadowControls newControls()
    throws ConstraintError
  {
    return new SBLightShadowControls();
  }

  public @Nonnull Option<SBLightShadowDescription> getShadow()
    throws ConstraintError
  {
    if (this.shadow.isSelected()) {
      switch ((KShadow.Type) this.type_select.getSelectedItem()) {
        case SHADOW_MAPPED_BASIC:
        {
          return new Option.Some<SBLightShadowDescription>(
            this.mapped_basic_controls.getShadow());
        }
      }
    } else {
      return Option.none();
    }

    throw new UnreachableCodeException();
  }

  @SuppressWarnings("synthetic-access") public void loadFrom(
    final @Nonnull Option<SBLightShadowDescription> o)
  {
    switch (o.type) {
      case OPTION_NONE:
      {
        break;
      }
      case OPTION_SOME:
      {
        final SBLightShadowDescription d =
          ((Option.Some<SBLightShadowDescription>) o).value;
        switch (d.getType()) {
          case SHADOW_MAPPED_BASIC:
          {
            this.mapped_basic_controls
              .loadFrom((SBLightShadowMappedBasicDescription) d);
            break;
          }
        }
        break;
      }
    }
  }

  protected void showAndHideControls()
  {
    final Type selected =
      (KShadow.Type) SBLightShadowControls.this.type_select.getSelectedItem();

    switch (selected) {
      case SHADOW_MAPPED_BASIC:
      {
        SBLightShadowControls.this.mapped_basic_group.forceShow();
        break;
      }
    }
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        try {
          final JFrame frame = new JFrame("Shadow");

          final Container panel = frame.getContentPane();
          final DesignGridLayout layout = new DesignGridLayout(panel);
          final SBLightShadowControls controls =
            SBLightShadowControls.newControls();

          controls.addToLayout(layout.row());

          frame.setPreferredSize(new Dimension(640, 480));
          frame.pack();
          frame.setVisible(true);
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (final ConstraintError x) {
          x.printStackTrace();
          System.exit(1);
        }
      }
    });
  }
}
