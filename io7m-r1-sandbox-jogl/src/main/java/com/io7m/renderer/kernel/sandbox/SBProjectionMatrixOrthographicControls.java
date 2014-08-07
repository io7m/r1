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
import com.io7m.renderer.kernel.sandbox.SBProjectionDescription.SBProjectionOrthographic;

public final class SBProjectionMatrixOrthographicControls implements
  SBProjectionMatrixDescriptionControls
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
        new SBExampleWindow("Orthographic") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
          {
            final SBProjectionMatrixOrthographicControls controls =
              SBProjectionMatrixOrthographicControls.newControls();
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static SBProjectionMatrixOrthographicControls newControls()
  {
    return new SBProjectionMatrixOrthographicControls();
  }

  private final JTextField         bottom;
  private SBProjectionOrthographic current;
  private final JButton            defaults;
  private final JTextField         far;
  private final RowGroup           group;
  private final JTextField         left;
  private final JTextField         near;
  private final JTextField         right;
  private final JTextField         top;

  private SBProjectionMatrixOrthographicControls()
  {
    this.group = new RowGroup();

    this.left = new JTextField();
    this.right = new JTextField();
    this.top = new JTextField();
    this.bottom = new JTextField();
    this.near = new JTextField();
    this.far = new JTextField();

    final SBProjectionOrthographic p = new SBProjectionOrthographic();
    this.current = p;
    this.set(p);

    this.defaults = new JButton("Defaults");
    this.defaults.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nullable ActionEvent e)
      {
        SBProjectionMatrixOrthographicControls.this.set(p);
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

  @Override public SBProjectionDescription getDescription()
    throws SBExceptionInputError
  {
    this.current =
      new SBProjectionOrthographic(
        SBTextFieldUtilities.getFieldFloatOrError(this.left),
        SBTextFieldUtilities.getFieldFloatOrError(this.right),
        SBTextFieldUtilities.getFieldFloatOrError(this.bottom),
        SBTextFieldUtilities.getFieldFloatOrError(this.top),
        SBTextFieldUtilities.getFieldFloatOrError(this.near),
        SBTextFieldUtilities.getFieldFloatOrError(this.far));
    return this.current;
  }

  private void set(
    final SBProjectionOrthographic p)
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
    final SBProjectionOrthographic description)
  {
    this.set(description);
  }
}
