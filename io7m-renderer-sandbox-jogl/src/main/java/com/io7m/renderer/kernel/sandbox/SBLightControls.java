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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jaux.functional.Unit;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RException;

public final class SBLightControls implements
  SBControlsDataType<SBLightDescription>
{
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
            final SBExampleController controller = new SBExampleController();
            final SBLightControls controls =
              SBLightControls.newControls(
                this,
                controller,
                Integer.valueOf(23),
                jlog);
            controls.controlsAddToLayout(layout);

            final JButton hide = new JButton("Hide");
            hide.addActionListener(new ActionListener() {
              @Override public void actionPerformed(
                final ActionEvent e)
              {
                controls.controlsHide();
              }
            });
            final JButton show = new JButton("Show");
            show.addActionListener(new ActionListener() {
              @Override public void actionPerformed(
                final ActionEvent e)
              {
                controls.controlsShow();
              }
            });
            layout.row().grid().add(hide).add(show);
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
      final @Nonnull Integer id,
      final @Nonnull Log log)
      throws ConstraintError
  {
    return new SBLightControls(parent, controller, id, log);
  }

  private final @Nonnull EnumMap<SBLightType, SBLightControlsType> controls;
  private final @Nonnull SBLightControlsDirectional                directional_controls;
  private final @Nonnull RowGroup                                  group;
  private @Nonnull Integer                                         id;
  private final @Nonnull JTextField                                id_field;
  private final @Nonnull JFrame                                    parent;
  private final @Nonnull SBLightControlsProjective                 projective_controls;
  private final @Nonnull SBLightTypeSelector                       selector;
  private final @Nonnull SBLightControlsSpherical                  spherical_controls;

  private <C extends SBSceneControllerLights & SBSceneControllerTextures> SBLightControls(
    final @Nonnull JFrame in_parent,
    final @Nonnull C controller,
    final @Nonnull Integer in_id,
    final @Nonnull Log log)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(in_parent, "Parent");

    this.group = new RowGroup();
    this.id_field = new JTextField(in_id.toString());
    this.id_field.setEditable(false);
    this.id = in_id;

    this.directional_controls =
      SBLightControlsDirectional.newControls(in_parent, in_id);
    this.spherical_controls =
      SBLightControlsSpherical.newControls(in_parent, in_id);
    this.projective_controls =
      SBLightControlsProjective
        .newControls(in_parent, controller, in_id, log);

    this.controls =
      new EnumMap<SBLightType, SBLightControlsType>(SBLightType.class);
    this.controls.put(
      SBLightType.LIGHT_DIRECTIONAL,
      this.directional_controls);
    this.controls.put(SBLightType.LIGHT_SPHERICAL, this.spherical_controls);
    this.controls.put(SBLightType.LIGHT_PROJECTIVE, this.projective_controls);

    this.selector = new SBLightTypeSelector();
    this.selector.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        final SBLightType selected =
          SBLightControls.this.selector.getSelectedItem();
        SBLightControls.this.controlsShowHide(selected);
      }
    });
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout.row().group(this.group).grid(new JLabel("ID")).add(this.id_field);
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Light"))
      .add(this.selector);

    final Set<Entry<SBLightType, SBLightControlsType>> entries =
      this.controls.entrySet();

    for (final Entry<SBLightType, SBLightControlsType> e : entries) {
      e.getValue().controlsAddToLayout(layout);
    }

    this.controlsShowHide(this.selector.getSelectedItem());
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.directional_controls.controlsHide();
    this.spherical_controls.controlsHide();
    this.projective_controls.controlsHide();
  }

  @Override @SuppressWarnings("synthetic-access") public
    void
    controlsLoadFrom(
      final @Nonnull SBLightDescription d)
  {
    this.id = d.lightGetID();
    this.id_field.setText(this.id.toString());

    try {
      d
        .lightDescriptionVisitableAccept(new SBLightDescriptionVisitor<Unit, ConstraintError>() {
          @Override public Unit lightVisitDirectional(
            final @Nonnull SBLightDescriptionDirectional l)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            SBLightControls.this.selector
              .setSelectedItem(SBLightType.LIGHT_DIRECTIONAL);
            SBLightControls.this.directional_controls.controlsLoadFrom(l);
            return Unit.unit();
          }

          @Override public Unit lightVisitProjective(
            final @Nonnull SBLightDescriptionProjective l)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            SBLightControls.this.selector
              .setSelectedItem(SBLightType.LIGHT_PROJECTIVE);
            SBLightControls.this.projective_controls.controlsLoadFrom(l);
            return Unit.unit();
          }

          @Override public Unit lightVisitSpherical(
            final @Nonnull SBLightDescriptionSpherical l)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            SBLightControls.this.selector
              .setSelectedItem(SBLightType.LIGHT_SPHERICAL);
            SBLightControls.this.spherical_controls.controlsLoadFrom(l);
            return Unit.unit();
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public SBLightDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    switch (this.selector.getSelectedItem()) {
      case LIGHT_DIRECTIONAL:
        return this.directional_controls.controlsSave();
      case LIGHT_PROJECTIVE:
        return this.projective_controls.controlsSave();
      case LIGHT_SPHERICAL:
        return this.spherical_controls.controlsSave();
    }

    throw new UnreachableCodeException();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.controlsShowHide(this.selector.getSelectedItem());
  }

  protected void controlsShowHide(
    final @Nonnull SBLightType selected)
  {
    final Set<Entry<SBLightType, SBLightControlsType>> entries =
      this.controls.entrySet();

    for (final Entry<SBLightType, SBLightControlsType> e : entries) {
      final SBLightControlsType c = e.getValue();
      c.controlsHide();
    }

    final SBLightControlsType c = this.controls.get(selected);
    c.controlsShow();
    this.parent.pack();
  }
}
