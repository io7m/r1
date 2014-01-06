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
import com.io7m.renderer.kernel.SBLightShadowDescription.SBLightShadowMappedVarianceDescription;

public final class SBLightShadowControls implements IHideable
{
  private static final class SBLightShadowMappedBasicControls
  {
    private final @Nonnull RowGroup                  row_group;
    private final @Nonnull SBIntegerHSlider          size;
    private final @Nonnull SBFloatHSlider            depth_bias;
    private final @Nonnull SBFloatHSlider            factor_maximum;
    private final @Nonnull SBFloatHSlider            factor_minimum;
    private final @Nonnull SBShadowPrecisionSelector precision;
    private final @Nonnull SBShadowFilterSelector    filter;

    private SBLightShadowMappedBasicControls()
      throws ConstraintError
    {
      this.size = new SBIntegerHSlider("Size", 1, 10);
      this.depth_bias = new SBFloatHSlider("Depth bias", 0.0f, 0.001f);
      this.factor_maximum = new SBFloatHSlider("Maximum", 0.0f, 1.0f);
      this.factor_minimum = new SBFloatHSlider("Minimum", 0.0f, 1.0f);
      this.precision = new SBShadowPrecisionSelector();
      this.filter = new SBShadowFilterSelector();
      this.row_group = new RowGroup();
      this.depth_bias.setCurrent(0.0005f);
    }

    public void addToLayout(
      final @Nonnull DesignGridLayout layout)
    {
      layout
        .row()
        .group(this.row_group)
        .grid(this.size.getLabel())
        .add(this.size.getSlider(), 3)
        .add(this.size.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.depth_bias.getLabel())
        .add(this.depth_bias.getSlider(), 3)
        .add(this.depth_bias.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.factor_maximum.getLabel())
        .add(this.factor_maximum.getSlider(), 3)
        .add(this.factor_maximum.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.factor_minimum.getLabel())
        .add(this.factor_minimum.getSlider(), 3)
        .add(this.factor_minimum.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Precision"))
        .add(this.precision, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter"))
        .add(this.filter, 4);
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
    }

    public @Nonnull SBLightShadowMappedBasicDescription getShadow()
      throws ConstraintError
    {
      return SBLightShadowDescription.newShadowMappedBasic(
        this.size.getCurrent(),
        this.depth_bias.getCurrent(),
        this.factor_maximum.getCurrent(),
        this.factor_minimum.getCurrent(),
        (KShadowPrecision) this.precision.getSelectedItem(),
        (KShadowFilter) this.filter.getSelectedItem());
    }

    public void setDescription(
      final SBLightShadowMappedBasicDescription smb)
    {
      this.size.setCurrent(smb.getSize());
      this.depth_bias.setCurrent(smb.getDepthBias());
      this.factor_maximum.setCurrent(smb.getFactorMaximum());
      this.factor_minimum.setCurrent(smb.getFactorMinimum());
      this.precision.setSelectedItem(smb.getPrecision());
      this.filter.setSelectedItem(smb.getFilter());
    }
  }

  private static final class SBLightShadowMappedVarianceControls
  {
    private final @Nonnull RowGroup                  row_group;
    private final @Nonnull SBIntegerHSlider          size;
    private final @Nonnull SBFloatHSlider            factor_maximum;
    private final @Nonnull SBFloatHSlider            factor_minimum;
    private final @Nonnull SBShadowPrecisionSelector precision;
    private final @Nonnull SBShadowFilterSelector    filter;

    private SBLightShadowMappedVarianceControls()
      throws ConstraintError
    {
      this.size = new SBIntegerHSlider("Size", 1, 10);
      this.factor_maximum = new SBFloatHSlider("Maximum", 0.0f, 1.0f);
      this.factor_minimum = new SBFloatHSlider("Minimum", 0.0f, 1.0f);
      this.precision = new SBShadowPrecisionSelector();
      this.filter = new SBShadowFilterSelector();
      this.row_group = new RowGroup();
    }

    public void addToLayout(
      final @Nonnull DesignGridLayout layout)
    {
      layout
        .row()
        .group(this.row_group)
        .grid(this.size.getLabel())
        .add(this.size.getSlider(), 3)
        .add(this.size.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.factor_maximum.getLabel())
        .add(this.factor_maximum.getSlider(), 3)
        .add(this.factor_maximum.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.factor_minimum.getLabel())
        .add(this.factor_minimum.getSlider(), 3)
        .add(this.factor_minimum.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Precision"))
        .add(this.precision, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter"))
        .add(this.filter, 4);
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
    }

    public @Nonnull SBLightShadowMappedVarianceDescription getShadow()
      throws ConstraintError
    {
      return SBLightShadowDescription.newShadowMappedVariance(
        this.size.getCurrent(),
        this.factor_maximum.getCurrent(),
        this.factor_minimum.getCurrent(),
        (KShadowPrecision) this.precision.getSelectedItem(),
        (KShadowFilter) this.filter.getSelectedItem());
    }

    public void setDescription(
      final SBLightShadowMappedVarianceDescription smb)
    {
      this.size.setCurrent(smb.getSize());
      this.factor_maximum.setCurrent(smb.getFactorMaximum());
      this.factor_minimum.setCurrent(smb.getFactorMinimum());
      this.precision.setSelectedItem(smb.getPrecision());
      this.filter.setSelectedItem(smb.getFilter());
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
  private final @Nonnull SBLightShadowMappedVarianceControls mapped_variance_controls;
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
    this.mapped_variance_controls = new SBLightShadowMappedVarianceControls();

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
    this.mapped_variance_controls.addToLayout(layout);
    this.controlsDisableSelector();
  }

  protected void controlsDisableSelector()
  {
    this.type_select.setEnabled(false);
    this.mapped_basic_controls.getRowGroup().hide();
    this.mapped_variance_controls.getRowGroup().hide();
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
    this.mapped_variance_controls.getRowGroup().forceShow();

    switch (type) {
      case SHADOW_MAPPED_BASIC:
      {
        this.mapped_basic_controls.getRowGroup().forceShow();
        this.mapped_variance_controls.getRowGroup().hide();
        break;
      }
      case SHADOW_MAPPED_SOFT:
      {
        this.mapped_variance_controls.getRowGroup().forceShow();
        this.mapped_basic_controls.getRowGroup().hide();
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
        case SHADOW_MAPPED_SOFT:
        {
          return new Option.Some<SBLightShadowDescription>(
            this.mapped_variance_controls.getShadow());
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
          case SHADOW_MAPPED_SOFT:
          {
            this.mapped_variance_controls
              .setDescription((SBLightShadowMappedVarianceDescription) d);
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
