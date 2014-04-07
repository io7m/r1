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

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KLightSphere;
import com.io7m.renderer.types.RSpaceWorldType;

public final class SBLightControlsSpherical implements
  SBControlsDataType<SBLightDescriptionSpherical>,
  SBLightControlsType
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Spherical") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBLightControlsSpherical controls =
              SBLightControlsSpherical.newControls(this, Integer.valueOf(23));
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBLightControlsSpherical newControls(
    final @Nonnull JFrame parent,
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return new SBLightControlsSpherical(parent, id);
  }

  private final @Nonnull SBColourInput                colour;
  private final @Nonnull SBFloatHSlider               falloff;
  private final @Nonnull SBFloatHSlider               intensity;
  private final @Nonnull JFrame                       parent;
  private final @Nonnull SBVector3FInput<RSpaceWorldType> position;
  private final @Nonnull SBFloatHSlider               radius;
  private final @Nonnull Integer                      id;

  private SBLightControlsSpherical(
    final @Nonnull JFrame in_parent,
    final @Nonnull Integer in_id)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(in_parent, "Parent");
    this.id = Constraints.constrainNotNull(in_id, "ID");
    this.colour = SBColourInput.newInput(in_parent, "Colour");
    this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);
    this.position = SBVector3FInput.newInput("Position");
    this.radius = new SBFloatHSlider("Radius", 0.0f, 128.0f);
    this.falloff = new SBFloatHSlider("Falloff", 0, 64.0f);

    this.intensity.setCurrent(1.0f);
    this.radius.setCurrent(8.0f);
    this.falloff.setCurrent(1.0f);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    this.intensity.controlsAddToLayout(layout);
    this.colour.controlsAddToLayout(layout);
    this.position.controlsAddToLayout(layout);
    this.falloff.controlsAddToLayout(layout);
    this.radius.controlsAddToLayout(layout);
  }

  @Override public void controlsHide()
  {
    this.colour.controlsHide();
    this.intensity.controlsHide();
    this.position.controlsHide();
    this.radius.controlsHide();
    this.falloff.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.colour.controlsShow();
    this.intensity.controlsShow();
    this.position.controlsShow();
    this.radius.controlsShow();
    this.falloff.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final @Nonnull SBLightDescriptionSpherical d)
  {
    final KLightSphere l = d.getLight();
    this.intensity.setCurrent(l.lightGetIntensity());
    this.colour.controlsLoadFrom(l.lightGetColour());
    this.position.setVector(l.getPosition());
    this.falloff.setCurrent(l.getFalloff());
    this.radius.setCurrent(l.getRadius());
  }

  @Override public @Nonnull SBLightDescriptionSpherical controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    return new SBLightDescriptionSpherical(KLightSphere.newSpherical(
      this.id,
      this.colour.controlsSave(),
      this.intensity.getCurrent(),
      this.position.getVector(),
      this.radius.getCurrent(),
      this.falloff.getCurrent()));
  }
}
