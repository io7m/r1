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

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;

import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RSpaceType;
import com.io7m.renderer.types.RSpaceWorldType;
import com.io7m.renderer.types.RVectorI4F;
import com.io7m.renderer.types.RVectorReadable4FType;

public final class SBVector4FInput<R extends RSpaceType>
{
  public static  <R extends RSpaceType> SBVector4FInput<R> newInput(
    final  String text)
  {
    return new SBVector4FInput<R>(text);
  }

  private final  JLabel     label;
  private final  JTextField field_w;
  private final  JTextField field_x;
  private final  JTextField field_y;
  private final  JTextField field_z;

  private SBVector4FInput(
    final  String text)
  {
    this.label = new JLabel(text);
    this.field_x = new JTextField("0.0");
    this.field_y = new JTextField("0.0");
    this.field_z = new JTextField("0.0");
    this.field_w = new JTextField("0.0");
  }

  public  RVectorI4F<R> getVector()
    throws SBExceptionInputError
  {
    return new RVectorI4F<R>(
      SBTextFieldUtilities.getFieldFloatOrError(this.field_x),
      SBTextFieldUtilities.getFieldFloatOrError(this.field_y),
      SBTextFieldUtilities.getFieldFloatOrError(this.field_z),
      SBTextFieldUtilities.getFieldFloatOrError(this.field_w));
  }

  @SuppressWarnings("boxing") public void setVector(
    final  RVectorReadable4FType<R> v)
  {
    this.field_x.setText(String.format("%.6f", v.getXF()));
    this.field_y.setText(String.format("%.6f", v.getYF()));
    this.field_z.setText(String.format("%.6f", v.getZF()));
    this.field_w.setText(String.format("%.6f", v.getWF()));
  }

  public void addToLayout(
    final  IRowCreator row)
  {
    row
      .grid()
      .add(this.label)
      .add(this.field_x)
      .add(this.field_y)
      .add(this.field_z)
      .add(this.field_w);
  }

  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        final JFrame frame = new JFrame("Vector4f");

        final Container panel = frame.getContentPane();
        final DesignGridLayout layout = new DesignGridLayout(panel);

        final SBVector4FInput<RSpaceWorldType> input =
          SBVector4FInput.newInput("World");
        input.addToLayout(layout.row());

        frame.setPreferredSize(new Dimension(640, 480));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      }
    });
  }
}
