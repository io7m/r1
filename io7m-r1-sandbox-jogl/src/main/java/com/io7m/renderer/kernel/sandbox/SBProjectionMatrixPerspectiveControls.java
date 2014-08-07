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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jnull.Nullable;
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
          {
            final SBProjectionMatrixPerspectiveControls controls =
              SBProjectionMatrixPerspectiveControls.newControls();
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static SBProjectionMatrixPerspectiveControls newControls()
  {
    return new SBProjectionMatrixPerspectiveControls();
  }

  private final JTextField        aspect;
  private SBProjectionPerspective current;
  private final JButton           defaults;
  private final JTextField        far;
  private final JTextField        fov;
  private final RowGroup          group;
  private final JTextField        near;

  private SBProjectionMatrixPerspectiveControls()
  {
    this.group = new RowGroup();

    this.near = new JTextField();
    this.far = new JTextField();
    this.aspect = new JTextField();
    this.fov = new JTextField();

    final SBProjectionPerspective p = new SBProjectionPerspective();
    this.current = p;
    this.set(p);

    this.defaults = new JButton("Defaults");
    this.defaults.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nullable ActionEvent e)
      {
        SBProjectionMatrixPerspectiveControls.this.set(p);
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

  @Override public SBProjectionDescription getDescription()
    throws SBExceptionInputError
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
    final SBProjectionPerspective p)
  {
    this.current = p;
    this.aspect.setText(Double.toString(p.getAspect()));
    this.fov.setText(Double.toString(p.getHorizontalFOV()));
    this.near.setText(Double.toString(p.getNear()));
    this.far.setText(Double.toString(p.getFar()));
  }

  public void setDescription(
    final SBProjectionPerspective description)
  {
    this.set(description);
  }
}
