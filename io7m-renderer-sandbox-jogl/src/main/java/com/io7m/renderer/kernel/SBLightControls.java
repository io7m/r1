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
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
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
import com.io7m.renderer.kernel.KLight.Type;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionDirectional;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionProjective;
import com.io7m.renderer.kernel.SBLightDescription.SBLightDescriptionSpherical;

public final class SBLightControls
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
        new SBExampleWindow("Lights") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBLightControls controls =
              SBLightControls.newControls(this, new K(), jlog);
            controls.addToLayout(layout);
            controls.setID(Integer.valueOf(23));
          }
        };
      }
    });
  }

  public static @Nonnull
    <C extends SBSceneControllerLights & SBSceneControllerTextures>
    SBLightControls
    newControls(
      final @Nonnull JFrame parent,
      final @Nonnull C controller,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new SBLightControls(parent, controller, log);
  }

  private final @Nonnull EnumMap<Type, SBLightDescriptionControls> controls;
  private final @Nonnull SBLightControlsDirectional                directional_controls;
  private final @Nonnull SBLightControlsSpherical                  spherical_controls;
  private final @Nonnull SBLightControlsProjective                 projective_controls;
  private final @Nonnull JComboBox<Type>                           selector;
  private final @Nonnull JFrame                                    parent;
  private final @Nonnull RowGroup                                  id_group;
  private final @Nonnull JTextField                                id;
  private @CheckForNull Integer                                    id_actual;

  private <C extends SBSceneControllerLights & SBSceneControllerTextures> SBLightControls(
    final @Nonnull JFrame parent,
    final @Nonnull C controller,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(parent, "Parent");

    this.id_group = new RowGroup();
    this.id = new JTextField();
    this.id.setEditable(false);
    this.id_actual = null;

    this.directional_controls =
      SBLightControlsDirectional.newControls(parent, controller);
    this.spherical_controls =
      SBLightControlsSpherical.newControls(parent, controller);
    this.projective_controls =
      SBLightControlsProjective.newControls(parent, controller, log);

    this.controls = new EnumMap<Type, SBLightDescriptionControls>(Type.class);
    this.controls.put(Type.LIGHT_DIRECTIONAL, this.directional_controls);
    this.controls.put(Type.LIGHT_SPHERE, this.spherical_controls);
    this.controls.put(Type.LIGHT_PROJECTIVE, this.projective_controls);

    this.selector = new SBLightTypeSelector();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final Type selected =
          (Type) SBLightControls.this.selector.getSelectedItem();
        SBLightControls.this.controlsShowHide(selected);
      }
    });
  }

  public void addToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout.row().group(this.id_group).grid(new JLabel("ID")).add(this.id);
    this.id_group.hide();

    layout.row().grid(new JLabel("Light")).add(this.selector);

    final Set<Entry<Type, SBLightDescriptionControls>> entries =
      this.controls.entrySet();

    for (final Entry<Type, SBLightDescriptionControls> e : entries) {
      e.getValue().addToLayout(layout);
    }

    this.controlsShowHide((Type) this.selector.getSelectedItem());
  }

  protected void controlsShowHide(
    final @Nonnull Type selected)
  {
    final Set<Entry<Type, SBLightDescriptionControls>> entries =
      this.controls.entrySet();

    for (final Entry<Type, SBLightDescriptionControls> e : entries) {
      final SBLightDescriptionControls c = e.getValue();
      c.forceShow();
      c.hide();
    }

    final SBLightDescriptionControls c = this.controls.get(selected);
    c.forceShow();
    this.parent.pack();
  }

  public SBLightDescription getDescription()
    throws SBExceptionInputError,
      ConstraintError
  {
    return this.controls.get(this.selector.getSelectedItem()).getDescription(
      this.id_actual);
  }

  public void setID(
    final @Nonnull Integer id)
  {
    this.id_group.forceShow();
    this.id.setText(id.toString());
    this.id_actual = id;
  }

  public void setDescription(
    final @Nonnull SBLightDescription description)
  {
    this.setID(description.getID());
    this.selector.setSelectedItem(description.getType());

    switch (description.getType()) {
      case LIGHT_DIRECTIONAL:
      {
        this.directional_controls
          .setDescription((SBLightDescriptionDirectional) description);
        break;
      }
      case LIGHT_SPHERE:
      {
        this.spherical_controls
          .setDescription((SBLightDescriptionSpherical) description);
        break;
      }
      case LIGHT_PROJECTIVE:
      {
        this.projective_controls
          .setDescription((SBLightDescriptionProjective) description);
        break;
      }
    }
  }
}
