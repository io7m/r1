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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IHideable;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.renderer.kernel.KShadow.Type;
import com.io7m.renderer.kernel.SBLightShadowDescription.SBLightShadowMappedBasicDescription;

public final class SBLightShadowControls implements IHideable
{
  private static final class SBLightShadowMappedBasicControls
  {
    private final @Nonnull RowGroup         row_group;
    private final @Nonnull SBIntegerHSlider slider;

    private SBLightShadowMappedBasicControls()
      throws ConstraintError
    {
      this.slider = new SBIntegerHSlider("Size", 1, 10);
      this.row_group = new RowGroup();
    }

    public void addToLayout(
      final @Nonnull DesignGridLayout layout)
    {
      layout
        .row()
        .group(this.row_group)
        .grid(this.slider.getLabel())
        .add(this.slider.getSlider(), 3)
        .add(this.slider.getField());
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
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

    public void setDescription(
      final SBLightShadowMappedBasicDescription smb)
    {
      this.slider.setCurrent(smb.getSize());
    }
  }

  private static final class SBLightShadowMappedFilteredControls
  {
    private final @Nonnull RowGroup         row_group;
    private final @Nonnull SBIntegerHSlider slider;

    private SBLightShadowMappedFilteredControls()
      throws ConstraintError
    {
      this.slider = new SBIntegerHSlider("Size", 1, 10);
      this.row_group = new RowGroup();
    }

    public void addToLayout(
      final @Nonnull DesignGridLayout layout)
    {
      layout
        .row()
        .group(this.row_group)
        .grid(this.slider.getLabel())
        .add(this.slider.getSlider(), 3)
        .add(this.slider.getField());
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
    }

    public @Nonnull SBLightShadowMappedBasicDescription getShadow()
      throws ConstraintError
    {
      return SBLightShadowDescription.newShadowMappedBasic(this.slider
        .getCurrent());
    }

    void setDescription(
      final @Nonnull SBLightShadowMappedBasicDescription desc)
    {
      this.slider.setCurrent(desc.getSize());
    }
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Shadow") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBLightShadowControls controls =
              SBLightShadowControls.newControls(this);
            controls.addToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBLightShadowControls newControls(
    final @Nonnull JFrame parent)
    throws ConstraintError
  {
    return new SBLightShadowControls(parent);
  }

  private final @Nonnull SBLightShadowMappedBasicControls    mapped_basic_controls;
  private final @Nonnull SBLightShadowMappedFilteredControls mapped_filtered_controls;
  private final @Nonnull JCheckBox                           shadow;
  private final @Nonnull SBLightShadowTypeSelector           type_select;
  private final @Nonnull JFrame                              parent;
  private final @Nonnull RowGroup                            group;

  @SuppressWarnings("synthetic-access") private SBLightShadowControls(
    final @Nonnull JFrame parent)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(parent, "Parent");

    this.group = new RowGroup();
    this.mapped_basic_controls = new SBLightShadowMappedBasicControls();
    this.mapped_filtered_controls = new SBLightShadowMappedFilteredControls();

    this.shadow = new JCheckBox("Shadow");
    this.shadow.setSelected(false);
    this.shadow.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        if (SBLightShadowControls.this.shadow.isSelected()) {
          SBLightShadowControls.this.controlsEnableSelector();
        } else {
          SBLightShadowControls.this.controlsDisableSelector();
        }
      }
    });

    this.type_select = new SBLightShadowTypeSelector();
    this.type_select.setEnabled(false);
    this.type_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final KShadow.Type type =
          (KShadow.Type) SBLightShadowControls.this.type_select
            .getSelectedItem();
        SBLightShadowControls.this.controlsSelectType(type);
      }
    });
  }

  public void addToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout
      .row()
      .group(this.group)
      .left()
      .add(this.shadow, new JSeparator())
      .fill();
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Type"))
      .add(this.type_select);

    layout.emptyRow();
    this.mapped_basic_controls.addToLayout(layout);
    this.mapped_filtered_controls.addToLayout(layout);
    this.controlsDisableSelector();
  }

  protected void controlsDisableSelector()
  {
    this.type_select.setEnabled(false);
    this.mapped_basic_controls.getRowGroup().hide();
    this.mapped_filtered_controls.getRowGroup().hide();
    this.parent.pack();
  }

  protected void controlsEnableSelector()
  {
    this.type_select.setEnabled(true);
    this.type_select.setSelectedItem(this.type_select.getSelectedItem());
    this.parent.pack();
  }

  protected void controlsSelectType(
    final @Nonnull Type type)
  {
    this.mapped_basic_controls.getRowGroup().forceShow();
    this.mapped_filtered_controls.getRowGroup().forceShow();

    switch (type) {
      case SHADOW_MAPPED_BASIC:
      {
        this.mapped_basic_controls.getRowGroup().forceShow();
        this.mapped_filtered_controls.getRowGroup().hide();
        break;
      }
      case SHADOW_MAPPED_FILTERED:
      {
        this.mapped_basic_controls.getRowGroup().hide();
        this.mapped_filtered_controls.getRowGroup().forceShow();
        break;
      }
    }

    this.parent.pack();
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
        case SHADOW_MAPPED_FILTERED:
        {
          return new Option.Some<SBLightShadowDescription>(
            this.mapped_filtered_controls.getShadow());
        }
      }
    } else {
      return Option.none();
    }

    throw new UnreachableCodeException();
  }

  public void setDescription(
    final @Nonnull Option<SBLightShadowDescription> o)
  {
    switch (o.type) {
      case OPTION_NONE:
      {
        this.shadow.setSelected(false);
        this.controlsDisableSelector();
        break;
      }
      case OPTION_SOME:
      {
        final SBLightShadowDescription d =
          ((Option.Some<SBLightShadowDescription>) o).value;

        this.shadow.setSelected(true);
        this.controlsEnableSelector();

        switch (d.getType()) {
          case SHADOW_MAPPED_BASIC:
          {
            this.mapped_basic_controls
              .setDescription((SBLightShadowMappedBasicDescription) d);
            break;
          }
          case SHADOW_MAPPED_FILTERED:
          {
            this.mapped_filtered_controls
              .setDescription((SBLightShadowMappedBasicDescription) d);
            break;
          }
        }
        break;
      }
    }
  }

  @Override public void hide()
  {
    this.group.forceShow();
    this.group.hide();
  }

  @Override public void show()
  {
    this.group.forceShow();
    this.group.show();
  }

  @Override public void forceShow()
  {
    this.group.forceShow();
  }
}
