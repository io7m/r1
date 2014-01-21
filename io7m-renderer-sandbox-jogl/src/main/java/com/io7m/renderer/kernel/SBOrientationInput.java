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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jtensors.QuaternionI4F;
import com.io7m.jtensors.VectorI3F;
import com.io7m.jtensors.VectorReadable3F;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBOrientationInput
{
  static final class RotationDialog extends JFrame
  {
    private static final long             serialVersionUID;
    private static final VectorReadable3F AXIS_X;
    private static final VectorReadable3F AXIS_Y;
    private static final VectorReadable3F AXIS_Z;

    static {
      serialVersionUID = 5132901751477130242L;
      AXIS_X = new VectorI3F(1.0f, 0.0f, 0.0f);
      AXIS_Y = new VectorI3F(0.0f, 1.0f, 0.0f);
      AXIS_Z = new VectorI3F(0.0f, 0.0f, 1.0f);
    }

    private final @Nonnull SBFloatHSlider slider_x;
    private final @Nonnull SBFloatHSlider slider_y;
    private final @Nonnull SBFloatHSlider slider_z;
    private final @Nonnull JButton        cancel;
    private final @Nonnull JButton        ok;
    private boolean                       selected = false;

    RotationDialog()
      throws ConstraintError
    {
      this.slider_x = new SBFloatHSlider("X", -360.0f, 360.0f);
      this.slider_y = new SBFloatHSlider("Y", -360.0f, 360.0f);
      this.slider_z = new SBFloatHSlider("Z", -360.0f, 360.0f);

      this.slider_x.setCurrent(0.0f);
      this.slider_y.setCurrent(0.0f);
      this.slider_z.setCurrent(0.0f);

      this.cancel = new JButton("Cancel");
      this.cancel.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final ActionEvent e)
        {
          SBWindowUtilities.closeWindow(RotationDialog.this);
        }
      });

      this.ok = new JButton("OK");
      this.ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final ActionEvent e)
        {
          RotationDialog.this.selected = true;
          SBWindowUtilities.closeWindow(RotationDialog.this);
        }
      });

      final DesignGridLayout dg = new DesignGridLayout(this.getContentPane());
      this.slider_x.addToLayout(dg.row());
      this.slider_y.addToLayout(dg.row());
      this.slider_z.addToLayout(dg.row());
      dg.row().right().add(this.cancel).add(this.ok);
    }

    @Nonnull QuaternionI4F getQuaternion()
    {
      final QuaternionI4F q = new QuaternionI4F();
      final QuaternionI4F qx =
        QuaternionI4F.makeFromAxisAngle(
          RotationDialog.AXIS_X,
          Math.toRadians(this.slider_x.getCurrent()));
      final QuaternionI4F qy =
        QuaternionI4F.makeFromAxisAngle(
          RotationDialog.AXIS_Y,
          Math.toRadians(this.slider_y.getCurrent()));
      final QuaternionI4F qz =
        QuaternionI4F.makeFromAxisAngle(
          RotationDialog.AXIS_Z,
          Math.toRadians(this.slider_z.getCurrent()));

      return QuaternionI4F.multiply(qz, QuaternionI4F.multiply(qy, qx));
    }

    public boolean isSelected()
    {
      return this.selected;
    }
  }

  private final @Nonnull JLabel     label;
  private final @Nonnull JTextField orientation_x;
  private final @Nonnull JTextField orientation_y;
  private final @Nonnull JTextField orientation_z;
  private final @Nonnull JTextField orientation_w;
  private final @Nonnull JButton    select;

  public static @Nonnull SBOrientationInput newInput()
  {
    return new SBOrientationInput();
  }

  private SBOrientationInput()
  {
    this.label = new JLabel("Orientation");
    this.orientation_x = new JTextField("0.0");
    this.orientation_y = new JTextField("0.0");
    this.orientation_z = new JTextField("0.0");
    this.orientation_w = new JTextField("0.0");

    this.select = new JButton("Select...");
    this.select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        try {
          final RotationDialog d = new RotationDialog();
          d.pack();
          d.setVisible(true);
          d.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final WindowEvent _)
            {
              if (d.isSelected()) {
                final QuaternionI4F o = d.getQuaternion();
                SBOrientationInput.this.orientation_x.setText(Float
                  .toString(o.getXF()));
                SBOrientationInput.this.orientation_y.setText(Float
                  .toString(o.getYF()));
                SBOrientationInput.this.orientation_z.setText(Float
                  .toString(o.getZF()));
                SBOrientationInput.this.orientation_w.setText(Float
                  .toString(o.getWF()));
              }
            }
          });
        } catch (final ConstraintError x) {
          throw new UnreachableCodeException(x);
        }

      }
    });
  }

  public @Nonnull JLabel getLabel()
  {
    return this.label;
  }

  @Nonnull QuaternionI4F getOrientation()
    throws SBExceptionInputError
  {
    return new QuaternionI4F(
      SBTextFieldUtilities.getFieldFloatOrError(this.orientation_x),
      SBTextFieldUtilities.getFieldFloatOrError(this.orientation_y),
      SBTextFieldUtilities.getFieldFloatOrError(this.orientation_z),
      SBTextFieldUtilities.getFieldFloatOrError(this.orientation_w));
  }

  public @Nonnull JTextField getOrientationW()
  {
    return this.orientation_w;
  }

  public @Nonnull JTextField getOrientationX()
  {
    return this.orientation_x;
  }

  public @Nonnull JTextField getOrientationY()
  {
    return this.orientation_y;
  }

  public @Nonnull JTextField getOrientationZ()
  {
    return this.orientation_z;
  }

  public @Nonnull JButton getSelect()
  {
    return this.select;
  }

  void setOrientation(
    final @Nonnull QuaternionI4F q)
  {
    this.orientation_x.setText(Float.toString(q.getXF()));
    this.orientation_y.setText(Float.toString(q.getYF()));
    this.orientation_z.setText(Float.toString(q.getZF()));
    this.orientation_w.setText(Float.toString(q.getWF()));
  }

  public void addToLayout(
    final @Nonnull IRowCreator row)
  {
    row
      .grid(this.label)
      .add(this.orientation_x)
      .add(this.orientation_y)
      .add(this.orientation_z)
      .add(this.orientation_w)
      .add(this.select);
  }
}
