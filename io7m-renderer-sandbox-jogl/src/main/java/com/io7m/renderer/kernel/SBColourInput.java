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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IRowCreator;

import com.io7m.renderer.RSpaceRGB;
import com.io7m.renderer.RVectorI3F;
import com.io7m.renderer.RVectorReadable3F;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBColourInput
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        final JFrame frame = new JFrame("Colour");

        final Container panel = frame.getContentPane();
        final DesignGridLayout layout = new DesignGridLayout(panel);

        final SBColourInput input = SBColourInput.newInput(frame, "Colour");
        input.addToLayout(layout.row());

        frame.setPreferredSize(new Dimension(640, 480));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      }
    });
  }

  public static @Nonnull SBColourInput newInput(
    final @Nonnull JFrame parent,
    final @Nonnull String text)
  {
    return new SBColourInput(parent, text);
  }

  private final @Nonnull JTextField field_x;
  private final @Nonnull JTextField field_y;
  private final @Nonnull JTextField field_z;
  private final @Nonnull JLabel     label;
  private final @Nonnull JButton    colour;

  private SBColourInput(
    final @Nonnull JFrame parent,
    final @Nonnull String text)
  {
    this.label = new JLabel(text);
    this.field_x = new JTextField("0.0");
    this.field_y = new JTextField("0.0");
    this.field_z = new JTextField("0.0");

    this.colour = new JButton(" ");
    this.colour.setToolTipText("Click to select a colour");
    this.colour.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final Color c =
          JColorChooser.showDialog(parent, "Select colour...", Color.WHITE);
        if (c != null) {
          final float[] rgb = c.getRGBColorComponents(null);
          SBColourInput.this.setColour(new RVectorI3F<RSpaceRGB>(
            rgb[0],
            rgb[1],
            rgb[2]));
        }
      }
    });

    this.setColour(new RVectorI3F<RSpaceRGB>(1.0f, 1.0f, 1.0f));
  }

  public void addToLayout(
    final @Nonnull IRowCreator row)
  {
    row
      .grid(this.label)
      .add(this.field_x)
      .add(this.field_y)
      .add(this.field_z)
      .add(this.colour);
  }

  public @Nonnull RVectorI3F<RSpaceRGB> getColour()
    throws SBExceptionInputError
  {
    return new RVectorI3F<RSpaceRGB>(
      SBTextFieldUtilities.getFieldFloatOrError(this.field_x),
      SBTextFieldUtilities.getFieldFloatOrError(this.field_y),
      SBTextFieldUtilities.getFieldFloatOrError(this.field_z));
  }

  @SuppressWarnings("boxing") public void setColour(
    final @Nonnull RVectorReadable3F<RSpaceRGB> v)
  {
    this.field_x.setText(String.format("%.6f", v.getXF()));
    this.field_y.setText(String.format("%.6f", v.getYF()));
    this.field_z.setText(String.format("%.6f", v.getZF()));

    this.colour.setBackground(new Color(v.getXF(), v.getYF(), v.getZF()));
    this.colour.setForeground(new Color(v.getXF(), v.getYF(), v.getZF()));
  }
}
