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

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;

public final class SBIntegerHSlider
{
  private final int                 minimum;
  private final int                 maximum;
  private final @Nonnull JTextField field;
  private final @Nonnull JSlider    slider;
  private final @Nonnull JLabel     label;

  public SBIntegerHSlider(
    final @Nonnull String label,
    final int minimum,
    final int maximum)
    throws ConstraintError
  {
    this.label = new JLabel(Constraints.constrainNotNull(label, "Label"));
    this.maximum = maximum;
    this.minimum = minimum;

    this.field = new JTextField(Integer.toString(minimum));
    this.slider = new JSlider(SwingConstants.HORIZONTAL);
    this.slider.setMinimum(minimum);
    this.slider.setMaximum(maximum);
    this.slider.setValue(0);

    this.slider.addChangeListener(new ChangeListener() {
      @SuppressWarnings({ "synthetic-access" }) @Override public
        void
        stateChanged(
          final @Nonnull ChangeEvent ev)
      {
        SBIntegerHSlider.this.refreshText();
      }
    });

    this.field.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        try {
          final int actual =
            SBTextFieldUtilities
              .getFieldIntegerOrError(SBIntegerHSlider.this.field);
          SBIntegerHSlider.this.setCurrent(actual);
        } catch (final SBExceptionInputError x) {

        }
      }
    });
  }

  public int getCurrent()
  {
    return this.slider.getValue();
  }

  public @Nonnull JTextField getField()
  {
    return this.field;
  }

  public @Nonnull JLabel getLabel()
  {
    return this.label;
  }

  public int getMaximum()
  {
    return this.maximum;
  }

  public int getMinimum()
  {
    return this.minimum;
  }

  public @Nonnull JSlider getSlider()
  {
    return this.slider;
  }

  @SuppressWarnings("boxing") private void refreshText()
  {
    final String ctext = String.format("%d", this.slider.getValue());
    SBIntegerHSlider.this.field.setText(ctext);
  }

  public void setCurrent(
    final int e)
  {
    this.slider.setValue(e);
    this.refreshText();
  }
}