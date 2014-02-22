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

import javax.annotation.Nonnull;
import javax.swing.JLabel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.RangeInclusive;
import com.io7m.jcanephora.AreaInclusive;
import com.io7m.renderer.kernel.types.KFramebufferDepthVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMapVarianceDescription;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;

final class SBLightShadowMappedVarianceControls implements SBControls
{
  private final @Nonnull SBDepthPrecisionSelector         depth_precision;
  private final @Nonnull SBDepthVariancePrecisionSelector depth_variance_precision;
  private final @Nonnull SBFloatHSlider                   factor_minimum;
  private final @Nonnull SBTextureMagFilterSelector       filter_mag;
  private final @Nonnull SBTextureMinFilterSelector       filter_min;
  private final @Nonnull SBFloatHSlider                   light_bleed_reduction;
  private final @Nonnull SBFloatHSlider                   minimum_variance;
  private final @Nonnull RowGroup                         row_group;
  private final @Nonnull SBIntegerHSlider                 size;

  SBLightShadowMappedVarianceControls()
    throws ConstraintError
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

    this.depth_precision = new SBDepthPrecisionSelector();
    this.depth_variance_precision = new SBDepthVariancePrecisionSelector();
    this.row_group = new RowGroup();
  }

  @Override public void controlsAddToLayout(
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

  @Override public void controlsHide()
  {
    this.row_group.hide();
  }

  @Override public void controlsShow()
  {
    this.row_group.forceShow();
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
  }
}
