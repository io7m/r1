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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.annotation.Nonnull;
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
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RTransformTexture;

public final class SBMaterialControls implements
  SBControlsDataType<SBMaterialDescription>
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Material") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final Log log =
              new Log(new Properties(), "com.io7m.renderer", "sandbox");
            final SBMaterialControls controls =
              SBMaterialControls.newControls(
                this,
                log,
                null,
                Integer.valueOf(0));
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBMaterialControls newControls(
    final @Nonnull JFrame parent,
    final @Nonnull Log log,
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull Integer id)
    throws ConstraintError
  {
    return new SBMaterialControls(parent, log, controller, id);
  }

  private final @Nonnull SBMaterialControlsNormal                controls_normal;
  private final @Nonnull SBMaterialControlsOpaqueAlphaToDepth    controls_opaque_alpha_depth;
  private final @Nonnull SBMaterialControlsOpaqueRegular         controls_opaque_regular;
  private final @Nonnull SBMaterialControlsTranslucentRefractive controls_translucent_refractive;
  private final @Nonnull SBMaterialControlsTranslucentRegular    controls_translucent_regular;
  private final @Nonnull SBMatrix3x3Controls<RTransformTexture>  controls_uv;
  private final @Nonnull RowGroup                                group;
  private final @Nonnull Integer                                 id;
  private final @Nonnull JTextField                              id_field;
  private final @Nonnull JTextField                              name;
  private final @Nonnull JFrame                                  parent;
  private final @Nonnull SBMaterialTypeSelector                  selector;

  private SBMaterialControls(
    final @Nonnull JFrame in_parent,
    final @Nonnull Log log,
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull Integer in_id)
    throws ConstraintError
  {
    this.parent = Constraints.constrainNotNull(in_parent, "Parent");
    this.id = Constraints.constrainNotNull(in_id, "ID");
    this.group = new RowGroup();
    this.id_field = new JTextField(in_id.toString());
    this.id_field.setEditable(false);
    this.name = new JTextField("");
    this.controls_uv =
      new SBMatrix3x3Controls<RTransformTexture>("UV matrix");
    this.controls_normal = new SBMaterialControlsNormal(controller, log);

    this.controls_opaque_regular =
      new SBMaterialControlsOpaqueRegular(
        this.name,
        new SBMaterialControlsAlbedo(controller, in_parent, log),
        new SBMaterialControlsEmissive(controller, log),
        new SBMaterialControlsEnvironment(controller, log),
        this.controls_normal,
        new SBMaterialControlsSpecular(in_parent, controller, log),
        this.controls_uv);
    this.controls_opaque_alpha_depth =
      new SBMaterialControlsOpaqueAlphaToDepth(
        this.name,
        new SBMaterialControlsAlbedo(controller, in_parent, log),
        new SBMaterialControlsEmissive(controller, log),
        new SBMaterialControlsEnvironment(controller, log),
        this.controls_normal,
        new SBMaterialControlsSpecular(in_parent, controller, log),
        this.controls_uv);
    this.controls_translucent_regular =
      new SBMaterialControlsTranslucentRegular(
        this.name,
        new SBMaterialControlsAlbedo(controller, in_parent, log),
        new SBMaterialControlsAlpha(),
        new SBMaterialControlsEmissive(controller, log),
        new SBMaterialControlsEnvironment(controller, log),
        this.controls_normal,
        new SBMaterialControlsSpecular(in_parent, controller, log),
        this.controls_uv);
    this.controls_translucent_refractive =
      new SBMaterialControlsTranslucentRefractive(
        this.name,
        this.controls_normal,
        this.controls_uv);

    this.selector = new SBMaterialTypeSelector();
    this.selector.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        final SBMaterialType type =
          SBMaterialControls.this.selector.getSelectedItem();
        SBMaterialControls.this.controlsShowForType(type);
      }
    });
  }

  @Override public void controlsAddToLayout(
    final @Nonnull DesignGridLayout layout)
  {
    layout.row().group(this.group).grid(new JLabel("ID")).add(this.id_field);
    layout.row().group(this.group).grid(new JLabel("Name")).add(this.name);
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Type"))
      .add(this.selector);

    this.controls_uv.controlsAddToLayout(layout);
    this.controls_normal.controlsAddToLayout(layout);
    this.controls_opaque_alpha_depth.controlsAddToLayout(layout);
    this.controls_opaque_regular.controlsAddToLayout(layout);
    this.controls_translucent_regular.controlsAddToLayout(layout);
    this.controls_translucent_refractive.controlsAddToLayout(layout);
    this.controlsShowForType(this.selector.getSelectedItem());
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.controls_uv.controlsHide();
    this.controls_normal.controlsHide();
    this.controls_opaque_alpha_depth.controlsHide();
    this.controls_opaque_regular.controlsHide();
    this.controls_translucent_regular.controlsHide();
    this.controls_translucent_refractive.controlsHide();
  }

  @SuppressWarnings("synthetic-access") @Override public
    void
    controlsLoadFrom(
      final @Nonnull SBMaterialDescription t)
  {
    try {
      this.name.setText(t.materialDescriptionGetName());

      t
        .materialDescriptionVisitableAccept(new SBMaterialDescriptionVisitor<Unit, ConstraintError>() {
          @Override public Unit materialDescriptionVisitOpaque(
            final @Nonnull SBMaterialDescriptionOpaque m)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            return m
              .materialDescriptionOpaqueVisitableAccept(new SBMaterialDescriptionOpaqueVisitor<Unit, ConstraintError>() {
                @Override public
                  Unit
                  materialDescriptionVisitOpaqueAlphaDepth(
                    final @Nonnull SBMaterialDescriptionOpaqueAlphaToDepth ma)
                    throws ConstraintError,
                      RException,
                      ConstraintError
                {
                  SBMaterialControls.this.controls_opaque_alpha_depth
                    .controlsLoadFrom(ma);
                  SBMaterialControls.this.selector
                    .setSelectedItem(SBMaterialType.MATERIAL_OPAQUE_ALPHA_DEPTH);
                  return Unit.unit();
                }

                @Override public Unit materialDescriptionVisitOpaqueRegular(
                  final @Nonnull SBMaterialDescriptionOpaqueRegular mr)
                  throws ConstraintError,
                    RException,
                    ConstraintError
                {
                  SBMaterialControls.this.controls_opaque_regular
                    .controlsLoadFrom(mr);
                  SBMaterialControls.this.selector
                    .setSelectedItem(SBMaterialType.MATERIAL_OPAQUE_REGULAR);
                  return Unit.unit();
                }
              });
          }

          @Override public Unit materialDescriptionVisitTranslucent(
            final @Nonnull SBMaterialDescriptionTranslucent m)
            throws ConstraintError,
              RException,
              ConstraintError
          {
            return m
              .materialDescriptionTranslucentVisitableAccept(new SBMaterialDescriptionTranslucentVisitor<Unit, ConstraintError>() {
                @Override public
                  Unit
                  materialDescriptionVisitTranslucentRefractive(
                    final @Nonnull SBMaterialDescriptionTranslucentRefractive mr)
                    throws ConstraintError,
                      RException,
                      ConstraintError
                {
                  SBMaterialControls.this.controls_translucent_refractive
                    .controlsLoadFrom(mr);
                  SBMaterialControls.this.selector
                    .setSelectedItem(SBMaterialType.MATERIAL_TRANSLUCENT_REFRACTIVE);
                  return Unit.unit();
                }

                @Override public
                  Unit
                  materialDescriptionVisitTranslucentRegular(
                    final @Nonnull SBMaterialDescriptionTranslucentRegular mr)
                    throws ConstraintError,
                      RException,
                      ConstraintError
                {
                  SBMaterialControls.this.controls_translucent_regular
                    .controlsLoadFrom(mr);
                  SBMaterialControls.this.selector
                    .setSelectedItem(SBMaterialType.MATERIAL_TRANSLUCENT_REGULAR);
                  return Unit.unit();
                }
              });
          }
        });
    } catch (final RException e) {
      throw new UnreachableCodeException(e);
    } catch (final ConstraintError e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Override public SBMaterialDescription controlsSave()
    throws SBExceptionInputError,
      ConstraintError
  {
    switch (this.selector.getSelectedItem()) {
      case MATERIAL_OPAQUE_ALPHA_DEPTH:
      {
        return this.controls_opaque_alpha_depth.controlsSave();
      }
      case MATERIAL_OPAQUE_REGULAR:
      {
        return this.controls_opaque_regular.controlsSave();
      }
      case MATERIAL_TRANSLUCENT_REFRACTIVE:
      {
        return this.controls_translucent_refractive.controlsSave();
      }
      case MATERIAL_TRANSLUCENT_REGULAR:
      {
        return this.controls_translucent_regular.controlsSave();
      }
    }

    throw new UnreachableCodeException();
  }

  @Override public void controlsShow()
  {
    this.group.show();
    this.controls_uv.controlsShow();
    this.controlsShowForType(this.selector.getSelectedItem());
  }

  protected void controlsShowForType(
    final @Nonnull SBMaterialType type)
  {
    switch (type) {
      case MATERIAL_OPAQUE_ALPHA_DEPTH:
      {
        this.controls_opaque_alpha_depth.controlsShow();
        this.controls_opaque_regular.controlsHide();
        this.controls_translucent_regular.controlsHide();
        this.controls_translucent_refractive.controlsHide();
        break;
      }
      case MATERIAL_OPAQUE_REGULAR:
      {
        this.controls_opaque_alpha_depth.controlsHide();
        this.controls_opaque_regular.controlsShow();
        this.controls_translucent_regular.controlsHide();
        this.controls_translucent_refractive.controlsHide();
        break;
      }
      case MATERIAL_TRANSLUCENT_REFRACTIVE:
      {
        this.controls_opaque_alpha_depth.controlsHide();
        this.controls_opaque_regular.controlsHide();
        this.controls_translucent_regular.controlsHide();
        this.controls_translucent_refractive.controlsShow();
        break;
      }
      case MATERIAL_TRANSLUCENT_REGULAR:
      {
        this.controls_opaque_alpha_depth.controlsHide();
        this.controls_opaque_regular.controlsHide();
        this.controls_translucent_regular.controlsShow();
        this.controls_translucent_refractive.controlsHide();
        break;
      }
    }
  }

  public @Nonnull Integer getID()
  {
    return this.id;
  }

  public @Nonnull String getName()
  {
    return this.name.getText();
  }
}
