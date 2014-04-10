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
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;

public final class SBFloatHSlider implements SBControls
{
  private static float convertFromSlider(
    final int x,
    final float min,
    final float max)
  {
    final float factor = x / 100.0f;
    return (factor * (max - min)) + min;
  }

  private static int convertToSlider(
    final float f,
    final float min,
    final float max)
  {
    return (int) (((f - min) / (max - min)) * 100);
  }

  private float                     current;
  private final @Nonnull JTextField field;
  private final @Nonnull RowGroup   group;
  private final @Nonnull JLabel     label;
  private final float               maximum;
  private final float               minimum;
  private final @Nonnull JSlider    slider;

  public SBFloatHSlider(
    final @Nonnull String in_label,
    final float in_minimum,
    final float in_maximum)
    throws ConstraintError
  {
    this.label =
      new JLabel(Constraints.constrainNotNull(in_label, "ForwardLabel"));
    this.group = new RowGroup();

    this.maximum = in_maximum;
    this.minimum = in_minimum;

    this.field = new JTextField(Float.toString(in_minimum));
    this.slider = new JSlider(SwingConstants.HORIZONTAL);
    this.slider.setMinimum(0);
    this.slider.setMaximum(100);
    this.slider.setValue(0);

    this.slider.addChangeListener(new ChangeListener() {
      @SuppressWarnings({ "synthetic-access" }) @Override public
        void
        stateChanged(
          final @Nonnull ChangeEvent ev)
      {
        final int slider_current = SBFloatHSlider.this.slider.getValue();
        SBFloatHSlider.this.current =
          SBFloatHSlider.convertFromSlider(
            slider_current,
            in_minimum,
            in_maximum);
        SBFloatHSlider.this.refreshText();
      }
    });

    this.field.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final ActionEvent e)
      {
        try {
          final float actual =
            SBTextFieldUtilities
              .getFieldFloatOrError(SBFloatHSlider.this.field);
          SBFloatHSlider.this.setCurrent(actual);
        } catch (final SBExceptionInputError x) {
          // Fail silently
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
      .grid(this.label)
      .add(this.slider, 3)
      .add(this.field);
  }

  @Override public void controlsHide()
  {
    this.group.hide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
  }

  public float getCurrent()
  {
    return this.current;
  }

  public @Nonnull JTextField getField()
  {
    return this.field;
  }

  public @Nonnull JLabel getLabel()
  {
    return this.label;
  }

  public float getMaximum()
  {
    return this.maximum;
  }

  public float getMinimum()
  {
    return this.minimum;
  }

  public @Nonnull JSlider getSlider()
  {
    return this.slider;
  }

  @SuppressWarnings("boxing") private void refreshText()
  {
    final String ctext = String.format("%.6f", SBFloatHSlider.this.current);
    SBFloatHSlider.this.field.setText(ctext);
  }

  public void setCurrent(
    final float e)
  {
    this.slider.setValue(SBFloatHSlider.convertToSlider(
      e,
      this.minimum,
      this.maximum));
    this.current = e;
    this.refreshText();
  }
}