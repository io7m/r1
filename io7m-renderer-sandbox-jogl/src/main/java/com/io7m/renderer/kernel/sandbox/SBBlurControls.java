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

import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.kernel.types.KBlurParameters.BuilderType;

public final class SBBlurControls implements
  SBControlsDataType<KBlurParameters>
{
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
          {
            final SBBlurControls c = new SBBlurControls();
            c.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  private final SBFloatHSlider   blur_size;
  private final RowGroup         group;
  private final SBIntegerHSlider passes;
  private final SBFloatHSlider   scale;

  SBBlurControls()
  {
    this.group = new RowGroup();
    this.blur_size = new SBFloatHSlider("Blur size", 0.0f, 16.0f);
    this.passes = new SBIntegerHSlider("Passes", 0, 32);
    this.scale = new SBFloatHSlider("Scale", 1.0f / 32.0f, 1.0f);
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    layout
      .row()
      .group(this.group)
      .grid(this.blur_size.getLabel())
      .add(this.blur_size.getSlider())
      .add(this.blur_size.getField());
    layout
      .row()
      .group(this.group)
      .grid(this.passes.getLabel())
      .add(this.passes.getSlider())
      .add(this.passes.getField());
    layout
      .row()
      .group(this.group)
      .grid(this.scale.getLabel())
      .add(this.scale.getSlider())
      .add(this.scale.getField());
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsLoadFrom(
    final KBlurParameters t)
  {
    this.blur_size.setCurrent(t.getBlurSize());
    this.passes.setCurrent(t.getPasses());
    this.scale.setCurrent(t.getScale());
  }

  @Override public KBlurParameters controlsSave()
    throws SBExceptionInputError
  {
    final BuilderType b = KBlurParameters.newBuilder();
    b.setBlurSize(this.blur_size.getCurrent());
    b.setPasses(this.passes.getCurrent());
    b.setScale(this.scale.getCurrent());
    return b.build();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }
}
