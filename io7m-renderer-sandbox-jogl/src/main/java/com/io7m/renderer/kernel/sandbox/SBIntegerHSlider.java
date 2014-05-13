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

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;

public final class SBIntegerHSlider
{
  private final int        minimum;
  private final int        maximum;
  private final JTextField field;
  private final JSlider    slider;
  private final JLabel     label;

  public SBIntegerHSlider(
    final String in_label,
    final int in_minimum,
    final int in_maximum)
  {
    this.label = new JLabel(NullCheck.notNull(in_label, "Label"));
    this.maximum = in_maximum;
    this.minimum = in_minimum;

    this.field = new JTextField(Integer.toString(in_minimum));
    this.slider = new JSlider(SwingConstants.HORIZONTAL);
    this.slider.setMinimum(in_minimum);
    this.slider.setMaximum(in_maximum);
    this.slider.setValue(0);

    this.slider.addChangeListener(new ChangeListener() {
      @SuppressWarnings({ "synthetic-access" }) @Override public
        void
        stateChanged(
          final @Nullable ChangeEvent ev)
      {
        SBIntegerHSlider.this.refreshText();
      }
    });

    this.field.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nullable ActionEvent e)
      {
        try {
          final int actual =
            SBTextFieldUtilities
              .getFieldIntegerOrError(SBIntegerHSlider.this.field);
          SBIntegerHSlider.this.setCurrent(actual);
        } catch (final SBExceptionInputError x) {
          // Silently fail
        }
      }
    });
  }

  public int getCurrent()
  {
    return this.slider.getValue();
  }

  public JTextField getField()
  {
    return this.field;
  }

  public JLabel getLabel()
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

  public JSlider getSlider()
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
