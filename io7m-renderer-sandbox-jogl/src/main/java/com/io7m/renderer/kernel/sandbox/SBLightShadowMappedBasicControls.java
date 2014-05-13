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

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.renderer.kernel.types.KFramebufferDepthDescription;
import com.io7m.renderer.kernel.types.KShadowMapBasicDescription;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;

final class SBLightShadowMappedBasicControls implements SBControls
{
  private final SBFloatHSlider             depth_bias;
  private final SBDepthPrecisionSelector   depth_precision;
  private final SBFloatHSlider             factor_minimum;
  private final SBTextureMagFilterSelector filter_mag;
  private final SBTextureMinFilterSelector filter_min;
  private final RowGroup                   row_group;
  private final SBIntegerHSlider           size;

  SBLightShadowMappedBasicControls()
  {
    this.size = new SBIntegerHSlider("Size", 1, 10);
    this.depth_bias = new SBFloatHSlider("Depth bias", 0.0f, 0.001f);
    this.depth_bias.setCurrent(0.0005f);
    this.factor_minimum = new SBFloatHSlider("Minimum", 0.0f, 1.0f);
    this.factor_minimum.setCurrent(0.2f);

    this.filter_mag = new SBTextureMagFilterSelector();
    this.filter_min = new SBTextureMinFilterSelector();
    this.depth_precision = new SBDepthPrecisionSelector();

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
      .grid(this.depth_bias.getLabel())
      .add(this.depth_bias.getSlider(), 3)
      .add(this.depth_bias.getField());
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

  @Override public void controlsHide()
  {
    this.row_group.hide();
  }

  @Override public void controlsShow()
  {
    this.row_group.forceShow();
  }

  public KShadowMappedBasic getShadow(
    final Integer light_id)
  {
    final int edge_size = ((int) Math.pow(2, this.size.getCurrent())) - 1;
    final RangeInclusiveL range_x = new RangeInclusiveL(0, edge_size);
    final RangeInclusiveL range_y = new RangeInclusiveL(0, edge_size);
    final AreaInclusive area = new AreaInclusive(range_x, range_y);

    final KFramebufferDepthDescription depth_description =
      KFramebufferDepthDescription.newDescription(
        area,
        this.filter_mag.getSelectedItem(),
        this.filter_min.getSelectedItem(),
        this.depth_precision.getSelectedItem());

    final KShadowMapBasicDescription map_description =
      KShadowMapBasicDescription.newDescription(
        light_id,
        depth_description,
        this.size.getCurrent());

    return KShadowMappedBasic.newMappedBasic(
      this.depth_bias.getCurrent(),
      this.factor_minimum.getCurrent(),
      map_description);
  }

  public void setDescription(
    final KShadowMappedBasic smb)
  {
    final KFramebufferDepthDescription fbd =
      smb.getDescription().getDescription();

    this.size.setCurrent(smb.getDescription().mapGetSizeExponent());
    this.depth_bias.setCurrent(smb.getDepthBias());
    this.factor_minimum.setCurrent(smb.getFactorMinimum());
    this.filter_mag.setSelectedItem(fbd.getFilterMagnification());
    this.filter_min.setSelectedItem(fbd.getFilterMinification());
    this.depth_precision.setSelectedItem(fbd.getDepthPrecision());
  }
}
