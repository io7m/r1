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

package com.io7m.renderer.kernel.sandbox;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jnull.NullCheck;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KLightDirectional;
import com.io7m.renderer.types.RSpaceWorldType;

public final class SBLightControlsDirectional implements
  SBControlsDataType<SBLightDescriptionDirectional>,
  SBLightControlsType
{
  /**
   * Simple test app.
   */

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Directional") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
          {
            final SBLightControlsDirectional controls =
              SBLightControlsDirectional.newControls(this, Integer.valueOf(0));
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static SBLightControlsDirectional newControls(
    final JFrame parent,
    final Integer id)
  {
    return new SBLightControlsDirectional(parent, id);
  }

  private final SBColourInput                    colour;
  private final SBVector3FInput<RSpaceWorldType> direction;
  private final RowGroup                         group;
  private final SBFloatHSlider                   intensity;
  private final JFrame                           parent;
  private final Integer                          id;

  private SBLightControlsDirectional(
    final JFrame in_parent,
    final Integer in_id)
  {
    this.parent = NullCheck.notNull(in_parent, "Parent");
    this.group = new RowGroup();
    this.colour = SBColourInput.newInput(in_parent, "Colour");
    this.direction = SBVector3FInput.newInput("Direction");
    this.id = in_id;
    this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);
    this.intensity.setCurrent(1.0f);
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    this.intensity.controlsAddToLayout(layout);
    this.colour.controlsAddToLayout(layout);
    this.direction.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.intensity.controlsHide();
    this.colour.controlsHide();
    this.direction.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.intensity.controlsShow();
    this.colour.controlsShow();
    this.direction.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final SBLightDescriptionDirectional d)
  {
    final KLightDirectional l = d.getLight();
    this.colour.controlsLoadFrom(l.lightGetColour());
    this.intensity.setCurrent(l.lightGetIntensity());
    this.direction.setVector(l.getDirection());
  }

  @Override public SBLightDescriptionDirectional controlsSave()
    throws SBExceptionInputError
  {
    return new SBLightDescriptionDirectional(
      this.id,
      KLightDirectional.newDirectional(
        this.direction.getVector(),
        this.colour.controlsSave(),
        this.intensity.getCurrent()));
  }
}
