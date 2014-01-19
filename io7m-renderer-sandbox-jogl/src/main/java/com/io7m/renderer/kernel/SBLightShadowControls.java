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
import com.io7m.jaux.RangeInclusive;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Option;
import com.io7m.jaux.functional.PartialFunction;
import com.io7m.jaux.functional.Unit;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.renderer.RException;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.KFramebufferDescription.KFramebufferDepthDescriptionType.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.KShadow.KShadowMappedBasic;
import com.io7m.renderer.kernel.KShadow.KShadowMappedVariance;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.KShadowMapDescription.KShadowMapVarianceDescription;

public final class SBLightShadowControls implements IHideable
{
  private static final class SBLightShadowMappedBasicControls
  {
    private final @Nonnull SBFloatHSlider             depth_bias;
    private final @Nonnull SBFloatHSlider             factor_maximum;
    private final @Nonnull SBFloatHSlider             factor_minimum;
    private final @Nonnull SBDepthPrecisionSelector   depth_precision;
    private final @Nonnull SBTextureMagFilterSelector filter_mag;
    private final @Nonnull SBTextureMinFilterSelector filter_min;
    private final @Nonnull RowGroup                   row_group;
    private final @Nonnull SBIntegerHSlider           size;

    private SBLightShadowMappedBasicControls()
      throws ConstraintError
    {
      this.size = new SBIntegerHSlider("Size", 1, 10);
      this.depth_bias = new SBFloatHSlider("Depth bias", 0.0f, 0.001f);
      this.depth_bias.setCurrent(0.0005f);
      this.factor_maximum = new SBFloatHSlider("Maximum", 0.0f, 1.0f);
      this.factor_maximum.setCurrent(1.0f);
      this.factor_minimum = new SBFloatHSlider("Minimum", 0.0f, 1.0f);
      this.factor_minimum.setCurrent(0.2f);

      this.filter_mag = new SBTextureMagFilterSelector();
      this.filter_min = new SBTextureMinFilterSelector();
      this.depth_precision = new SBDepthPrecisionSelector();

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
        .add(this.depth_precision, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter (Magnification)"))
        .add(this.filter_mag, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter (Minification)"))
        .add(this.filter_min, 4);
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
    }

    public @Nonnull KShadowMappedBasic getShadow(
      final @Nonnull Integer light_id)
      throws ConstraintError
    {
      final int edge_size = ((int) Math.pow(2, this.size.getCurrent())) - 1;
      final RangeInclusive range_x = new RangeInclusive(0, edge_size);
      final RangeInclusive range_y = new RangeInclusive(0, edge_size);
      final AreaInclusive area = new AreaInclusive(range_x, range_y);

      final KFramebufferDepthDescription depth_description =
        new KFramebufferDepthDescription(
          area,
          this.filter_mag.getSelectedItem(),
          this.filter_min.getSelectedItem(),
          this.depth_precision.getSelectedItem());

      final KShadowMapBasicDescription map_description =
        new KShadowMapBasicDescription(
          light_id,
          depth_description,
          this.size.getCurrent());

      return KShadow.newMappedBasic(
        this.depth_bias.getCurrent(),
        this.factor_maximum.getCurrent(),
        this.factor_minimum.getCurrent(),
        map_description);
    }

    public void setDescription(
      final @Nonnull KShadowMappedBasic smb)
    {
      final KFramebufferDepthDescription fbd =
        smb.getDescription().getDescription();

      this.size.setCurrent(smb.getDescription().getSizeExponent());
      this.depth_bias.setCurrent(smb.getDepthBias());
      this.factor_maximum.setCurrent(smb.getFactorMaximum());
      this.factor_minimum.setCurrent(smb.getFactorMinimum());
      this.filter_mag.setSelectedItem(fbd.getFilterMagnification());
      this.filter_min.setSelectedItem(fbd.getFilterMinification());
      this.depth_precision.setSelectedItem(fbd.getDepthPrecision());
    }
  }

  private static final class SBLightShadowMappedVarianceControls
  {
    private final @Nonnull SBFloatHSlider                   factor_maximum;
    private final @Nonnull SBFloatHSlider                   factor_minimum;
    private final @Nonnull SBFloatHSlider                   light_bleed_reduction;
    private final @Nonnull SBFloatHSlider                   minimum_variance;
    private final @Nonnull SBIntegerHSlider                 size;
    private final @Nonnull RowGroup                         row_group;
    private final @Nonnull SBTextureMagFilterSelector       filter_mag;
    private final @Nonnull SBTextureMinFilterSelector       filter_min;
    private final @Nonnull SBDepthPrecisionSelector         depth_precision;
    private final @Nonnull SBDepthVariancePrecisionSelector depth_variance_precision;

    private SBLightShadowMappedVarianceControls()
      throws ConstraintError
    {
      this.size = new SBIntegerHSlider("Size", 1, 10);

      this.factor_maximum = new SBFloatHSlider("Maximum", 0.0f, 1.0f);
      this.factor_maximum.setCurrent(1.0f);
      this.factor_minimum = new SBFloatHSlider("Minimum", 0.0f, 1.0f);
      this.factor_minimum.setCurrent(0.2f);

      this.filter_mag = new SBTextureMagFilterSelector();
      this.filter_min = new SBTextureMinFilterSelector();

      this.minimum_variance =
        new SBFloatHSlider("Minimum variance", 0.0f, 0.01f);
      this.minimum_variance.setCurrent(0.00002f);

      this.light_bleed_reduction =
        new SBFloatHSlider("Light bleed reduction", 0.0f, 1.0f);
      this.light_bleed_reduction.setCurrent(0.2f);

      this.depth_precision = new SBDepthPrecisionSelector();
      this.depth_variance_precision = new SBDepthVariancePrecisionSelector();
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
        .grid(this.minimum_variance.getLabel())
        .add(this.minimum_variance.getSlider(), 3)
        .add(this.minimum_variance.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(this.light_bleed_reduction.getLabel())
        .add(this.light_bleed_reduction.getSlider(), 3)
        .add(this.light_bleed_reduction.getField());
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Precision"))
        .add(this.depth_precision, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Variance precision"))
        .add(this.depth_variance_precision, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter (Magnification)"))
        .add(this.filter_mag, 4);
      layout
        .row()
        .group(this.row_group)
        .grid(new JLabel("Filter (Minification)"))
        .add(this.filter_min, 4);
    }

    public @Nonnull RowGroup getRowGroup()
    {
      return this.row_group;
    }

    public @Nonnull KShadowMappedVariance getShadow(
      final @Nonnull Integer light_id)
      throws ConstraintError
    {
      final int edge_size = ((int) Math.pow(2, this.size.getCurrent())) - 1;
      final RangeInclusive range_x = new RangeInclusive(0, edge_size);
      final RangeInclusive range_y = new RangeInclusive(0, edge_size);
      final AreaInclusive area = new AreaInclusive(range_x, range_y);

      final KFramebufferDepthVarianceDescription depth_description =
        new KFramebufferDepthVarianceDescription(
          area,
          this.filter_mag.getSelectedItem(),
          this.filter_min.getSelectedItem(),
          this.depth_precision.getSelectedItem(),
          this.depth_variance_precision.getSelectedItem());

      final KShadowMapVarianceDescription map_description =
        new KShadowMapVarianceDescription(
          light_id,
          depth_description,
          this.size.getCurrent());

      return KShadow.newMappedVariance(
        this.factor_maximum.getCurrent(),
        this.factor_minimum.getCurrent(),
        this.minimum_variance.getCurrent(),
        this.light_bleed_reduction.getCurrent(),
        map_description);
    }

    public void setDescription(
      final KShadowMappedVariance smv)
    {
      final KFramebufferDepthVarianceDescription fbd =
        smv.getDescription().getDescription();

      this.size.setCurrent(smv.getDescription().getSizeExponent());
      this.minimum_variance.setCurrent(smv.getMinimumVariance());
      this.light_bleed_reduction.setCurrent(smv.getLightBleedReduction());
      this.factor_maximum.setCurrent(smv.getFactorMaximum());
      this.factor_minimum.setCurrent(smv.getFactorMinimum());
      this.filter_mag.setSelectedItem(fbd.getFilterMagnification());
      this.filter_min.setSelectedItem(fbd.getFilterMinification());
      this.depth_variance_precision.setSelectedItem(fbd
        .getDepthVariancePrecision());
      this.depth_precision.setSelectedItem(fbd.getDepthPrecision());
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

  private final @Nonnull RowGroup                            group;
  private final @Nonnull SBLightShadowMappedBasicControls    mapped_basic_controls;
  private final @Nonnull SBLightShadowMappedVarianceControls mapped_variance_controls;
  private final @Nonnull JFrame                              parent;
  private final @Nonnull JCheckBox                           shadow;
  private final @Nonnull SBLightShadowTypeSelector           type_select;

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
        final SBShadowType type =
          SBLightShadowControls.this.type_select.getSelectedItem();
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
    final @Nonnull SBShadowType type)
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
      case SHADOW_MAPPED_VARIANCE:
      {
        this.mapped_variance_controls.getRowGroup().forceShow();
        this.mapped_basic_controls.getRowGroup().hide();
        break;
      }
    }

    this.parent.pack();
  }

  @Override public void forceShow()
  {
    this.group.forceShow();
  }

  public @Nonnull Option<KShadow> getShadow(
    final @Nonnull Integer light_id)
    throws ConstraintError
  {
    if (this.shadow.isSelected()) {
      switch (this.type_select.getSelectedItem()) {
        case SHADOW_MAPPED_BASIC:
        {
          final KShadow s = this.mapped_basic_controls.getShadow(light_id);
          return Option.some(s);
        }
        case SHADOW_MAPPED_VARIANCE:
        {
          final KShadow s = this.mapped_variance_controls.getShadow(light_id);
          return Option.some(s);
        }
      }
    } else {
      return Option.none();
    }

    throw new UnreachableCodeException();
  }

  @Override public void hide()
  {
    this.group.forceShow();
    this.group.hide();
  }

  public void setDescription(
    final @Nonnull Option<KShadow> o)
  {
    try {
      o.mapPartial(new PartialFunction<KShadow, Unit, ConstraintError>() {
        @SuppressWarnings("synthetic-access") @Override public Unit call(
          final KShadow x)
          throws ConstraintError
        {
          SBLightShadowControls.this.shadow.setSelected(true);
          SBLightShadowControls.this.controlsEnableSelector();

          try {
            return x
              .shadowAccept(new KShadowVisitor<Unit, ConstraintError>() {
                @SuppressWarnings("synthetic-access") @Override public
                  Unit
                  shadowVisitBasic(
                    final @Nonnull KShadowMappedBasic s)
                {
                  SBLightShadowControls.this.mapped_basic_controls
                    .setDescription(s);

                  SBLightShadowControls.this.type_select
                    .setSelectedItem(SBShadowType.SHADOW_MAPPED_BASIC);
                  SBLightShadowControls.this
                    .controlsSelectType(SBShadowType.SHADOW_MAPPED_BASIC);
                  return Unit.unit();
                }

                @SuppressWarnings("synthetic-access") @Override public
                  Unit
                  shadowVisitVariance(
                    final @Nonnull KShadowMappedVariance s)
                {
                  SBLightShadowControls.this.mapped_variance_controls
                    .setDescription(s);

                  SBLightShadowControls.this.type_select
                    .setSelectedItem(SBShadowType.SHADOW_MAPPED_VARIANCE);
                  SBLightShadowControls.this
                    .controlsSelectType(SBShadowType.SHADOW_MAPPED_VARIANCE);
                  return Unit.unit();
                }
              });
          } catch (final JCGLException e) {
            throw new UnreachableCodeException(e);
          } catch (final RException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public void show()
  {
    this.group.forceShow();
    this.group.show();
  }
}
