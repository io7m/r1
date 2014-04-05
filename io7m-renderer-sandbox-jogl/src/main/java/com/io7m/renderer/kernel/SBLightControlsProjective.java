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
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RSpaceWorldType;

public final class SBLightControlsProjective implements
  SBControlsDataType<SBLightDescriptionProjective>,
  SBLightControlsType
{
  /**
   * Simple test app.
   */

  public static void main(
    final String args[])
  {
    final Properties properties = new Properties();
    final Log jlog = new Log(properties, "", "");

    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Projective") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBExampleController controller = new SBExampleController();
            final SBLightControlsProjective controls =
              SBLightControlsProjective.newControls(
                this,
                controller,
                Integer.valueOf(0),
                jlog);
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull
    <C extends SBSceneControllerTextures>
    SBLightControlsProjective
    newControls(
      final @Nonnull JFrame parent,
      final @Nonnull C controller,
      final @Nonnull Integer id,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new SBLightControlsProjective(parent, controller, id, log);
  }

  private final @Nonnull SBFloatHSlider               falloff;
  private final @Nonnull RowGroup                     group;
  private final @Nonnull SBOrientationInput           orientation;
  private final @Nonnull SBVector3FInput<RSpaceWorldType> position;
  private final @Nonnull JTextField                   texture;
  private final @Nonnull JButton                      texture_select;
  private final @Nonnull SBProjectionMatrixControls   projection;
  private final @Nonnull SBFloatHSlider               intensity;
  private final @Nonnull SBColourInput                colour;
  private final @Nonnull SBLightShadowControls        shadow;
  private final @Nonnull SBSceneControllerTextures    texture_controller;
  private final @Nonnull JFrame                       parent;
  private final @Nonnull Integer                      id;

  private <C extends SBSceneControllerTextures> SBLightControlsProjective(
    final @Nonnull JFrame in_parent,
    final @Nonnull C controller,
    final @Nonnull Integer in_id,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(in_parent, "Parent");
    this.texture_controller =
      Constraints.constrainNotNull(controller, "Controller");
    this.id = Constraints.constrainNotNull(in_id, "ID field");

    this.group = new RowGroup();
    this.colour = SBColourInput.newInput(in_parent, "Colour");
    this.orientation = SBOrientationInput.newInput();
    this.position = SBVector3FInput.newInput("Position");
    this.falloff = new SBFloatHSlider("Falloff", 0, 64.0f);
    this.projection = SBProjectionMatrixControls.newControls();
    this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);
    this.shadow = SBLightShadowControls.newControls(in_parent);

    this.texture = new JTextField();
    this.texture.setEditable(false);
    this.texture_select = new JButton("Select...");
    this.texture_select.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        final SBTextures2DWindow twindow =
          new SBTextures2DWindow(
            controller,
            SBLightControlsProjective.this.texture,
            log);
        twindow.pack();
        twindow.setVisible(true);
      }
    });

    this.falloff.setCurrent(1.0f);
    this.intensity.setCurrent(1.0f);
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    this.intensity.controlsAddToLayout(layout);
    this.colour.controlsAddToLayout(layout);
    this.position.controlsAddToLayout(layout);
    this.orientation.controlsAddToLayout(layout);
    this.falloff.controlsAddToLayout(layout);

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 3)
      .add(this.texture_select);

    this.projection.controlsAddToLayout(layout);
    this.shadow.controlsAddToLayout(layout);
  }

  public void setDescription(
    final @Nonnull SBLightDescriptionProjective description)
  {
    this.intensity.setCurrent(description.getIntensity());
    this.colour.controlsLoadFrom(description.getColour());
    this.position.setVector(description.getPosition());
    this.orientation.setOrientation(description.getOrientation());
    this.falloff.setCurrent(description.getFalloff());
    this.texture.setText(description.getTexture().toString());
    this.projection.setDescription(description.getProjection());
    this.shadow.setDescription(description.getShadow());
  }

  @Override public void controlsHide()
  {
    this.intensity.controlsHide();
    this.colour.controlsHide();
    this.position.controlsHide();
    this.orientation.controlsHide();
    this.falloff.controlsHide();
    this.group.hide();
    this.projection.controlsHide();
    this.shadow.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.intensity.controlsShow();
    this.colour.controlsShow();
    this.position.controlsShow();
    this.orientation.controlsShow();
    this.falloff.controlsShow();
    this.group.forceShow();
    this.projection.controlsShow();
    this.shadow.controlsShow();
  }

  @Override public void controlsLoadFrom(
    final SBLightDescriptionProjective t)
  {
    this.intensity.setCurrent(t.getIntensity());
    this.colour.controlsLoadFrom(t.getColour());
    this.position.setVector(t.getPosition());
    this.orientation.setOrientation(t.getOrientation());
    this.falloff.setCurrent(t.getFalloff());
    this.projection.setDescription(t.getProjection());
    this.shadow.setDescription(t.getShadow());
    this.texture.setText(t.getTexture().toString());
  }

  @Override public SBLightDescriptionProjective controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    final PathVirtual path =
      PathVirtual.ofString(SBTextFieldUtilities
        .getFieldNonEmptyStringOrError(this.texture));

    return new SBLightDescriptionProjective(
      this.orientation.getOrientation(),
      this.position.getVector(),
      this.falloff.getCurrent(),
      this.projection.getDescription(),
      path,
      this.colour.controlsSave(),
      this.intensity.getCurrent(),
      this.shadow.getShadow(this.id),
      this.id);
  }
}
