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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.Type;

public final class SBProjectionMatrixControls implements SBControls
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
        new SBExampleWindow("Projection") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBProjectionMatrixControls controls =
              SBProjectionMatrixControls.newControls();
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBProjectionMatrixControls newControls()
    throws ConstraintError
  {
    return new SBProjectionMatrixControls();
  }

  private final @Nonnull EnumMap<Type, SBProjectionMatrixDescriptionControls> controls;
  private final @Nonnull SBProjectionMatrixFrustumControls                    frustum_controls;
  private final @Nonnull SBProjectionMatrixOrthographicControls               ortho_controls;
  private final @Nonnull SBProjectionMatrixPerspectiveControls                perspective_controls;
  private final @Nonnull JComboBox<Type>                                      selector;
  private final @Nonnull RowGroup                                             group;

  private SBProjectionMatrixControls()
    throws ConstraintError
  {
    this.group = new RowGroup();

    this.frustum_controls = SBProjectionMatrixFrustumControls.newControls();
    this.ortho_controls =
      SBProjectionMatrixOrthographicControls.newControls();
    this.perspective_controls =
      SBProjectionMatrixPerspectiveControls.newControls();

    this.controls =
      new EnumMap<SBProjectionDescription.Type, SBProjectionMatrixDescriptionControls>(
        SBProjectionDescription.Type.class);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_FRUSTUM,
      this.frustum_controls);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC,
      this.ortho_controls);
    this.controls.put(
      SBProjectionDescription.Type.PROJECTION_PERSPECTIVE,
      this.perspective_controls);

    this.selector = new JComboBox<SBProjectionDescription.Type>();
    this.selector.addItem(SBProjectionDescription.Type.PROJECTION_FRUSTUM);
    this.selector
      .addItem(SBProjectionDescription.Type.PROJECTION_ORTHOGRAPHIC);
    this.selector
      .addItem(SBProjectionDescription.Type.PROJECTION_PERSPECTIVE);

    this.selector.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        final SBProjectionDescription.Type selected =
          (Type) SBProjectionMatrixControls.this.selector.getSelectedItem();
        SBProjectionMatrixControls.this.controlsShowHide(selected);
      }
    });
  }

  protected void controlsShowHide(
    final Type selected)
  {
    switch (selected) {
      case PROJECTION_FRUSTUM:
      {
        this.frustum_controls.controlsShow();
        this.ortho_controls.controlsHide();
        this.perspective_controls.controlsHide();
        break;
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        this.frustum_controls.controlsHide();
        this.ortho_controls.controlsShow();
        this.perspective_controls.controlsHide();
        break;
      }
      case PROJECTION_PERSPECTIVE:
      {
        this.frustum_controls.controlsHide();
        this.ortho_controls.controlsHide();
        this.perspective_controls.controlsShow();
        break;
      }
    }
  }

  public SBProjectionDescription getDescription()
    throws SBExceptionInputError,
      ConstraintError
  {
    return this.controls
      .get(this.selector.getSelectedItem())
      .getDescription();
  }

  public void setDescription(
    final @Nonnull SBProjectionDescription description)
  {
    this.selector.setSelectedItem(description.getType());

    switch (description.getType()) {
      case PROJECTION_FRUSTUM:
      {
        this.frustum_controls
          .setDescription((SBProjectionDescription.SBProjectionFrustum) description);
        break;
      }
      case PROJECTION_ORTHOGRAPHIC:
      {
        this.ortho_controls
          .setDescription((SBProjectionDescription.SBProjectionOrthographic) description);
        break;
      }
      case PROJECTION_PERSPECTIVE:
      {
        this.perspective_controls
          .setDescription((SBProjectionDescription.SBProjectionPerspective) description);
        break;
      }
    }
  }

  @Override public void controlsHide()
  {
    this.group.hide();

    final Set<Entry<Type, SBProjectionMatrixDescriptionControls>> entries =
      this.controls.entrySet();

    for (final Entry<Type, SBProjectionMatrixDescriptionControls> e : entries) {
      final SBProjectionMatrixDescriptionControls c = e.getValue();
      c.controlsHide();
    }
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    this.controlsShowHide((Type) this.selector.getSelectedItem());
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    final Set<Entry<Type, SBProjectionMatrixDescriptionControls>> entries =
      this.controls.entrySet();

    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Type"))
      .add(this.selector);

    for (final Entry<Type, SBProjectionMatrixDescriptionControls> e : entries) {
      final SBProjectionMatrixDescriptionControls c = e.getValue();
      c.controlsAddToLayout(layout);
      c.controlsHide();
    }

    this.controlsShowHide((Type) SBProjectionMatrixControls.this.selector
      .getSelectedItem());
  }
}
