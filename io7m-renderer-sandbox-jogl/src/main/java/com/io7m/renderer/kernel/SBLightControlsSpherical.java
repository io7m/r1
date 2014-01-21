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

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.kernel.KLight.KSphere;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionSpherical;

public final class SBLightControlsSpherical implements
  SBLightDescriptionControls
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
              SBLightControlsSpherical.newControls(
                this,
                new SBSceneControllerLights() {
                  @Override public void sceneChangeListenerAdd(
                    final SBSceneChangeListener listener)
                  {
                    // TODO Auto-generated method stub
                  }

                  @Override public Collection<SBLight> sceneLightsGetAll()
                  {
                    // TODO Auto-generated method stub
                    return null;
                  }

                  @Override public void sceneLightRemove(
                    final Integer id)
                    throws ConstraintError
                  {
                    // TODO Auto-generated method stub
                  }

                  @Override public SBLight sceneLightGet(
                    final Integer id)
                    throws ConstraintError
                  {
                    // TODO Auto-generated method stub
                    return null;
                  }

                  @Override public Integer sceneLightFreshID()
                  {
                    // TODO Auto-generated method stub
                    return null;
                  }

                  @Override public boolean sceneLightExists(
                    final Integer id)
                    throws ConstraintError
                  {
                    // TODO Auto-generated method stub
                    return false;
                  }

                  @Override public void sceneLightAddByDescription(
                    final SBLightDescription d)
                    throws ConstraintError
                  {
                    // TODO Auto-generated method stub
                  }
                });

            controls.addToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBLightControlsSpherical newControls(
    final @Nonnull JFrame parent,
    final @Nonnull SBSceneControllerLights controller)
    throws ConstraintError
  {
    return new SBLightControlsSpherical(parent, controller);
  }

  private final @Nonnull SBFloatHSlider               falloff;
  private final @Nonnull RowGroup                     group;
  private final @Nonnull SBVector3FInput<RSpaceWorld> position;
  private final @Nonnull SBFloatHSlider               radius;
  private final @Nonnull SBFloatHSlider               intensity;
  private final @Nonnull SBColourInput                colour;
  private final @Nonnull SBSceneControllerLights      controller;
  private final @Nonnull JFrame                       parent;

  private SBLightControlsSpherical(
    final @Nonnull JFrame parent,
    final @Nonnull SBSceneControllerLights controller)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(parent, "Parent");
    this.controller = Constraints.constrainNotNull(controller, "Controller");
    this.group = new RowGroup();
    this.colour = SBColourInput.newInput(parent, "Colour");
    this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);
    this.position = SBVector3FInput.newInput("Position");
    this.radius = new SBFloatHSlider("Radius", 0.0f, 128.0f);
    this.falloff = new SBFloatHSlider("Falloff", 0, 64.0f);

    this.intensity.setCurrent(1.0f);
    this.radius.setCurrent(8.0f);
    this.falloff.setCurrent(1.0f);
  }

  @Override public void addToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    this.intensity.addToLayout(layout.row().group(this.group));
    this.colour.addToLayout(layout.row().group(this.group));
    this.position.addToLayout(layout.row().group(this.group));
    this.falloff.addToLayout(layout.row().group(this.group));
    this.radius.addToLayout(layout.row().group(this.group));
  }

  public void setDescription(
    final SBLightDescriptionSpherical description)
  {
    final KSphere l = description.getLight();
    this.intensity.setCurrent(l.getIntensity());
    this.colour.setColour(l.getColour());
    this.position.setVector(l.getPosition());
    this.falloff.setCurrent(l.getFalloff());
    this.radius.setCurrent(l.getRadius());
  }

  @Override public @Nonnull SBLightDescription getDescription(
    final @CheckForNull Integer id)
    throws SBExceptionInputError,
      ConstraintError
  {
    final Integer new_id =
      id != null ? id : this.controller.sceneLightFreshID();
    return new SBLightDescriptionSpherical(KSphere.make(
      new_id,
      this.colour.getColour(),
      this.intensity.getCurrent(),
      this.position.getVector(),
      this.radius.getCurrent(),
      this.falloff.getCurrent()));
  }

  @Override public void hide()
  {
    this.group.hide();
    this.parent.pack();
  }

  @Override public void show()
  {
    this.group.show();
    this.parent.pack();
  }

  @Override public void forceShow()
  {
    this.group.forceShow();
    this.parent.pack();
  }
}
