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

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionPerspective;

public final class SBProjectionMatrixPerspectiveControls implements
  SBProjectionMatrixDescriptionControls
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Perspective") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBProjectionMatrixPerspectiveControls controls =
              SBProjectionMatrixPerspectiveControls.newControls();
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBProjectionMatrixPerspectiveControls newControls()
    throws ConstraintError
  {
    return new SBProjectionMatrixPerspectiveControls();
  }

  private final @Nonnull JTextField        aspect;
  private @Nonnull SBProjectionPerspective current;
  private final @Nonnull JButton           defaults;
  private final @Nonnull JTextField        far;
  private final @Nonnull JTextField        fov;
  private final @Nonnull RowGroup          group;
  private final @Nonnull JTextField        near;

  private SBProjectionMatrixPerspectiveControls()
    throws ConstraintError
  {
    this.group = new RowGroup();

    this.near = new JTextField();
    this.far = new JTextField();
    this.aspect = new JTextField();
    this.fov = new JTextField();

    this.set(new SBProjectionPerspective());

    this.defaults = new JButton("Defaults");
    this.defaults.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        try {
          SBProjectionMatrixPerspectiveControls.this
            .set(new SBProjectionPerspective());
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException(x);
        }
      }
    });
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Aspect"))
      .add(this.aspect, 3);
    layout.row().group(this.group).grid(new JLabel("FOV")).add(this.fov, 3);
    layout.row().group(this.group).grid(new JLabel("Near")).add(this.near, 3);
    layout.row().group(this.group).grid(new JLabel("Far")).add(this.far, 3);
    layout.row().group(this.group).right().add(this.defaults);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

  @Override public @Nonnull SBProjectionDescription getDescription()
    throws SBExceptionInputError,
      ConstraintError
  {
    this.current =
      new SBProjectionPerspective(
        SBTextFieldUtilities.getFieldFloatOrError(this.near),
        SBTextFieldUtilities.getFieldFloatOrError(this.far),
        SBTextFieldUtilities.getFieldFloatOrError(this.aspect),
        SBTextFieldUtilities.getFieldFloatOrError(this.fov));
    return this.current;
  }

  private void set(
    final @Nonnull SBProjectionPerspective p)
  {
    this.current = p;
    this.aspect.setText(Double.toString(p.getAspect()));
    this.fov.setText(Double.toString(p.getHorizontalFOV()));
    this.near.setText(Double.toString(p.getNear()));
    this.far.setText(Double.toString(p.getFar()));
  }

  public void setDescription(
    final @Nonnull SBProjectionPerspective description)
  {
    this.set(description);
  }
}
