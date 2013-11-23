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
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import javax.annotation.CheckForNull;
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
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;
import com.io7m.renderer.RSpaceWorld;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionProjective;

public final class SBLightControlsProjective implements
  SBLightDescriptionControls
{
  static class K implements
    SBSceneControllerLights,
    SBSceneControllerTextures
  {
    @Override public void sceneChangeListenerAdd(
      final SBSceneChangeListener listener)
    {
      // TODO Auto-generated method stub
    }

    @Override public
      <T extends SBTexture2DKind>
      Future<SBTexture2D<T>>
      sceneTexture2DLoad(
        final File file,
        final TextureWrapS wrap_s,
        final TextureWrapT wrap_t,
        final TextureFilterMinification filter_min,
        final TextureFilterMagnification filter_mag)
        throws ConstraintError
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override public Future<SBTextureCube> sceneTextureCubeLoad(
      final File file,
      final TextureWrapR wrap_r,
      final TextureWrapS wrap_s,
      final TextureWrapT wrap_t,
      final TextureFilterMinification filter_min,
      final TextureFilterMagnification filter_mag)
      throws ConstraintError
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override public Map<PathVirtual, SBTexture2D<?>> sceneTextures2DGet()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override public Map<PathVirtual, SBTextureCube> sceneTexturesCubeGet()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override public void sceneLightAddByDescription(
      final SBLightDescription d)
      throws ConstraintError
    {
      // TODO Auto-generated method stub

    }

    @Override public boolean sceneLightExists(
      final Integer id)
      throws ConstraintError
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override public Integer sceneLightFreshID()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override public SBLight sceneLightGet(
      final Integer id)
      throws ConstraintError
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

    @Override public Collection<SBLight> sceneLightsGetAll()
    {
      // TODO Auto-generated method stub
      return null;
    }
  }

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
            final SBLightControlsProjective controls =
              SBLightControlsProjective.newControls(this, new K(), jlog);
            controls.addToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull
    <C extends SBSceneControllerTextures & SBSceneControllerLights>
    SBLightControlsProjective
    newControls(
      final @Nonnull JFrame parent,
      final @Nonnull C controller,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new SBLightControlsProjective(parent, controller, log);
  }

  private final @Nonnull SBFloatHSlider               falloff;
  private final @Nonnull RowGroup                     group;
  private final @Nonnull SBOrientationInput           orientation;
  private final @Nonnull SBVector3FInput<RSpaceWorld> position;
  private final @Nonnull JTextField                   texture;
  private final @Nonnull JButton                      texture_select;
  private final @Nonnull SBProjectionMatrixControls   projection;
  private final @Nonnull SBFloatHSlider               intensity;
  private final @Nonnull SBColourInput                colour;
  private final @Nonnull SBLightShadowControls        shadow;
  private final @Nonnull SBSceneControllerTextures    texture_controller;
  private final @Nonnull SBSceneControllerLights      light_controller;
  private final @Nonnull JFrame                       parent;

  private <C extends SBSceneControllerTextures & SBSceneControllerLights> SBLightControlsProjective(
    final @Nonnull JFrame parent,
    final @Nonnull C controller,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(parent, "Parent");
    this.texture_controller =
      Constraints.constrainNotNull(controller, "Controller");
    this.light_controller = controller;

    this.group = new RowGroup();
    this.colour = SBColourInput.newInput(parent, "Colour");
    this.orientation = SBOrientationInput.newInput();
    this.position = SBVector3FInput.newInput("Position");
    this.falloff = new SBFloatHSlider("Falloff", 0, 64.0f);
    this.projection = SBProjectionMatrixControls.newControls();
    this.intensity = new SBFloatHSlider("Intensity", 0.0f, 2.0f);
    this.shadow = SBLightShadowControls.newControls(parent);

    this.texture = new JTextField();
    this.texture.setEditable(false);
    this.texture_select = new JButton("Select...");
    this.texture_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
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

  @Override public void addToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    this.intensity.addToLayout(layout.row().group(this.group));
    this.colour.addToLayout(layout.row().group(this.group));
    this.position.addToLayout(layout.row().group(this.group));
    this.orientation.addToLayout(layout.row().group(this.group));
    this.falloff.addToLayout(layout.row().group(this.group));

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Texture"))
      .add(this.texture, 3)
      .add(this.texture_select);

    this.projection.addToLayout(layout);
    this.shadow.addToLayout(layout);
  }

  public void setDescription(
    final @Nonnull SBLightDescriptionProjective description)
  {
    this.intensity.setCurrent(description.getIntensity());
    this.colour.setColour(description.getColour());
    this.position.setVector(description.getPosition());
    this.orientation.setOrientation(description.getOrientation());
    this.falloff.setCurrent(description.getFalloff());
    this.texture.setText(description.getTexture().toString());
    this.projection.setDescription(description.getProjection());
    this.shadow.setDescription(description.getShadow());
  }

  @Override public SBLightDescription getDescription(
    final @CheckForNull Integer id)
    throws SBExceptionInputError,
      ConstraintError
  {
    final Integer new_id =
      id != null ? id : this.light_controller.sceneLightFreshID();

    final PathVirtual path =
      PathVirtual.ofString(SBTextFieldUtilities
        .getFieldNonEmptyStringOrError(this.texture));

    return new SBLightDescriptionProjective(
      this.orientation.getOrientation(),
      this.position.getVector(),
      this.falloff.getCurrent(),
      this.projection.getDescription(),
      path,
      this.colour.getColour(),
      this.intensity.getCurrent(),
      this.shadow.getShadow(),
      new_id);
  }

  @Override public void hide()
  {
    this.projection.forceShow();
    this.projection.hide();
    this.shadow.forceShow();
    this.shadow.hide();
    this.group.hide();
    this.parent.pack();
  }

  @Override public void show()
  {
    this.projection.forceShow();
    this.shadow.forceShow();
    this.group.show();
    this.parent.pack();
  }

  @Override public void forceShow()
  {
    this.projection.forceShow();
    this.shadow.forceShow();
    this.group.forceShow();
    this.parent.pack();
  }
}
