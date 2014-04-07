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
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionFrustum;

public final class SBProjectionMatrixFrustumControls implements
  SBProjectionMatrixDescriptionControls
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Frustum") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
            throws ConstraintError
          {
            final SBProjectionMatrixFrustumControls controls =
              SBProjectionMatrixFrustumControls.newControls();
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static @Nonnull SBProjectionMatrixFrustumControls newControls()
    throws ConstraintError
  {
    return new SBProjectionMatrixFrustumControls();
  }

  private final @Nonnull JTextField    bottom;
  private @Nonnull SBProjectionFrustum current;
  private final @Nonnull JButton       defaults;
  private final @Nonnull JTextField    far;
  private final @Nonnull RowGroup      group;
  private final @Nonnull JTextField    left;
  private final @Nonnull JTextField    near;
  private final @Nonnull JTextField    right;
  private final @Nonnull JTextField    top;

  private SBProjectionMatrixFrustumControls()
    throws ConstraintError
  {
    this.group = new RowGroup();

    this.left = new JTextField();
    this.right = new JTextField();
    this.top = new JTextField();
    this.bottom = new JTextField();
    this.near = new JTextField();
    this.far = new JTextField();

    this.set(new SBProjectionFrustum());

    this.defaults = new JButton("Defaults");
    this.defaults.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nonnull ActionEvent e)
      {
        try {
          SBProjectionMatrixFrustumControls.this
            .set(new SBProjectionFrustum());
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException(x);
        }
      }
    });
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    layout.row().group(this.group).grid(new JLabel("Left")).add(this.left, 3);
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Right"))
      .add(this.right, 3);
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Bottom"))
      .add(this.bottom, 3);
    layout.row().group(this.group).grid(new JLabel("Top")).add(this.top, 3);
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
      new SBProjectionFrustum(
        SBTextFieldUtilities.getFieldFloatOrError(this.left),
        SBTextFieldUtilities.getFieldFloatOrError(this.right),
        SBTextFieldUtilities.getFieldFloatOrError(this.bottom),
        SBTextFieldUtilities.getFieldFloatOrError(this.top),
        SBTextFieldUtilities.getFieldFloatOrError(this.near),
        SBTextFieldUtilities.getFieldFloatOrError(this.far));
    return this.current;
  }

  private void set(
    final @Nonnull SBProjectionFrustum p)
  {
    this.current = p;
    this.left.setText(Double.toString(p.getLeft()));
    this.right.setText(Double.toString(p.getRight()));
    this.top.setText(Double.toString(p.getTop()));
    this.bottom.setText(Double.toString(p.getBottom()));
    this.near.setText(Double.toString(p.getNear()));
    this.far.setText(Double.toString(p.getFar()));
  }

  public void setDescription(
    final @Nonnull SBProjectionFrustum description)
  {
    this.set(description);
  }
}
