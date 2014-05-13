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

import javax.swing.JLabel;
import javax.swing.JSeparator;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;

final class SBLightShadowMappedVarianceControls implements SBControls
{
  private final SBDepthPrecisionSelector         depth_precision;
  private final SBDepthVariancePrecisionSelector depth_variance_precision;
  private final SBFloatHSlider                   factor_minimum;
  private final SBTextureMagFilterSelector       filter_mag;
  private final SBTextureMinFilterSelector       filter_min;
  private final SBFloatHSlider                   light_bleed_reduction;
  private final SBFloatHSlider                   minimum_variance;
  private final RowGroup                         row_group;
  private final SBIntegerHSlider                 size;
  private final SBBlurControls                   blur_controls;

  SBLightShadowMappedVarianceControls()
  {
    this.size = new SBIntegerHSlider("Size", 1, 10);

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

    this.blur_controls = new SBBlurControls();
    this.depth_precision = new SBDepthPrecisionSelector();
    this.depth_variance_precision = new SBDepthVariancePrecisionSelector();
    this.row_group = new RowGroup();
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
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

    layout
      .row()
      .group(this.row_group)
      .left()
      .add(new JLabel("Blur"))
      .add(new JSeparator())
      .fill();
    this.blur_controls.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.row_group.hide();
    this.blur_controls.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.row_group.forceShow();
    this.blur_controls.controlsShow();
  }

  public RowGroup getRowGroup()
  {
    return this.row_group;
  }

  public KShadowMappedVariance getShadow(
    final Integer light_id)
    throws SBExceptionInputError
  {
    final int edge_size = ((int) Math.pow(2, this.size.getCurrent())) - 1;
    final RangeInclusiveL range_x = new RangeInclusiveL(0, edge_size);
    final RangeInclusiveL range_y = new RangeInclusiveL(0, edge_size);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);

    final KFramebufferDepthVarianceDescription depth_description =
      KFramebufferDepthVarianceDescription.newDescription(
        area,
        this.filter_mag.getSelectedItem(),
        this.filter_min.getSelectedItem(),
        this.depth_precision.getSelectedItem(),
        this.depth_variance_precision.getSelectedItem());

    final KShadowMapVarianceDescription map_description =
      KShadowMapVarianceDescription.newDescription(
        light_id,
        depth_description,
        this.size.getCurrent());

    return KShadowMappedVariance.newMappedVariance(
      this.factor_minimum.getCurrent(),
      this.minimum_variance.getCurrent(),
      this.light_bleed_reduction.getCurrent(),
      this.blur_controls.controlsSave(),
      map_description);
  }

  public void setDescription(
    final KShadowMappedVariance smv)
  {
    final KFramebufferDepthVarianceDescription fbd =
      smv.getDescription().getDescription();

    this.size.setCurrent(smv.getDescription().mapGetSizeExponent());
    this.minimum_variance.setCurrent(smv.getMinimumVariance());
    this.light_bleed_reduction.setCurrent(smv.getLightBleedReduction());
    this.factor_minimum.setCurrent(smv.getFactorMinimum());
    this.filter_mag.setSelectedItem(fbd.getFilterMagnification());
    this.filter_min.setSelectedItem(fbd.getFilterMinification());
    this.depth_variance_precision.setSelectedItem(fbd
      .getDepthVariancePrecision());
    this.depth_precision.setSelectedItem(fbd.getDepthPrecision());
    this.blur_controls.controlsLoadFrom(smv.getBlur());
  }
}
