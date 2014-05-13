/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
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

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.NullCheckException;
import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KShadowMappedBasic;
import com.io7m.renderer.kernel.types.KShadowMappedVariance;
import com.io7m.renderer.kernel.types.KShadowType;
import com.io7m.renderer.kernel.types.KShadowVisitorType;
import com.io7m.renderer.types.RException;

public final class SBLightShadowControls implements SBControls
{
  public static void main(
    final String args[])
  {
    SwingUtilities.invokeLater(new Runnable() {
      @SuppressWarnings("unused") @Override public void run()
      {
        new SBExampleWindow("Shadow") {
          private static final long serialVersionUID = 6048725370293855922L;

          @Override public void addToLayout(
            final DesignGridLayout layout)
          {
            final SBLightShadowControls controls =
              SBLightShadowControls.newControls(this);
            controls.controlsAddToLayout(layout);
          }
        };
      }
    });
  }

  public static SBLightShadowControls newControls(
    final JFrame parent)
  {
    return new SBLightShadowControls(parent);
  }

  private final RowGroup                            group;
  private final SBLightShadowMappedBasicControls    mapped_basic_controls;
  private final SBLightShadowMappedVarianceControls mapped_variance_controls;
  private final JFrame                              parent;
  private final JCheckBox                           shadow;
  private final SBLightShadowTypeSelector           type_select;

  @SuppressWarnings("synthetic-access") private SBLightShadowControls(
    final JFrame in_parent)
  {
    this.parent = NullCheck.notNull(in_parent, "Parent");

    this.group = new RowGroup();
    this.mapped_basic_controls = new SBLightShadowMappedBasicControls();
    this.mapped_variance_controls = new SBLightShadowMappedVarianceControls();

    this.shadow = new JCheckBox("Shadow");
    this.shadow.setSelected(false);
    this.shadow.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        if (SBLightShadowControls.this.shadow.isSelected()) {
          SBLightShadowControls.this.controlsEnableSelector();
        } else {
          SBLightShadowControls.this.controlsDisableSelector();
        }
      }
    });

    this.type_select = new SBLightShadowTypeSelector();
    this.type_select.setEnabled(false);
    this.type_select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        final SBShadowType type =
          SBLightShadowControls.this.type_select.getSelectedItem();
        SBLightShadowControls.this.controlsShowHideForType(type);
      }
    });
  }

  @Override public void controlsAddToLayout(
    final DesignGridLayout layout)
  {
    layout
      .row()
      .group(this.group)
      .left()
      .add(this.shadow, new JSeparator())
      .fill();
    layout
      .row()
      .group(this.group)
      .grid(new JLabel("Type"))
      .add(this.type_select);

    layout.emptyRow();
    this.mapped_basic_controls.controlsAddToLayout(layout);
    this.mapped_variance_controls.controlsAddToLayout(layout);
    this.controlsDisableSelector();
  }

  protected void controlsDisableSelector()
  {
    this.type_select.setEnabled(false);
    this.mapped_basic_controls.controlsHide();
    this.mapped_variance_controls.controlsHide();
  }

  protected void controlsEnableSelector()
  {
    this.type_select.setEnabled(true);
    this.type_select.setSelectedItem(this.type_select.getSelectedItem());
    this.parent.pack();
  }

  @Override public void controlsHide()
  {
    this.group.hide();
    this.mapped_basic_controls.controlsHide();
    this.mapped_variance_controls.controlsHide();
  }

  @Override public void controlsShow()
  {
    this.group.forceShow();
    if (this.shadow.isSelected()) {
      this.controlsShowHideForType(this.type_select.getSelectedItem());
    }
  }

  protected void controlsShowHideForType(
    final SBShadowType type)
  {
    switch (type) {
      case SHADOW_MAPPED_BASIC:
      {
        this.mapped_basic_controls.controlsShow();
        this.mapped_variance_controls.controlsHide();
        break;
      }
      case SHADOW_MAPPED_VARIANCE:
      {
        this.mapped_variance_controls.controlsShow();
        this.mapped_basic_controls.controlsHide();
        break;
      }
    }
  }

  public OptionType<KShadowType> getShadow(
    final Integer light_id)
    throws SBExceptionInputError
  {
    if (this.shadow.isSelected()) {
      switch (this.type_select.getSelectedItem()) {
        case SHADOW_MAPPED_BASIC:
        {
          final KShadowType s =
            this.mapped_basic_controls.getShadow(light_id);
          return Option.some(s);
        }
        case SHADOW_MAPPED_VARIANCE:
        {
          final KShadowType s =
            this.mapped_variance_controls.getShadow(light_id);
          return Option.some(s);
        }
      }
    } else {
      return Option.none();
    }

    throw new UnreachableCodeException();
  }

  public void setDescription(
    final OptionType<KShadowType> o)
  {
    o
      .mapPartial(new PartialFunctionType<KShadowType, Unit, NullCheckException>() {
        @SuppressWarnings("synthetic-access") @Override public Unit call(
          final KShadowType x)
        {
          SBLightShadowControls.this.shadow.setSelected(true);
          SBLightShadowControls.this.controlsEnableSelector();

          try {
            return x
              .shadowAccept(new KShadowVisitorType<Unit, NullCheckException>() {
                @Override public Unit shadowMappedBasic(
                  final KShadowMappedBasic s)
                  throws JCGLException,
                    RException
                {
                  SBLightShadowControls.this.mapped_basic_controls
                    .setDescription(s);

                  SBLightShadowControls.this.type_select
                    .setSelectedItem(SBShadowType.SHADOW_MAPPED_BASIC);
                  SBLightShadowControls.this
                    .controlsShowHideForType(SBShadowType.SHADOW_MAPPED_BASIC);
                  return Unit.unit();
                }

                @Override public Unit shadowMappedVariance(
                  final KShadowMappedVariance s)
                  throws JCGLException,
                    RException
                {
                  SBLightShadowControls.this.mapped_variance_controls
                    .setDescription(s);

                  SBLightShadowControls.this.type_select
                    .setSelectedItem(SBShadowType.SHADOW_MAPPED_VARIANCE);
                  SBLightShadowControls.this
                    .controlsShowHideForType(SBShadowType.SHADOW_MAPPED_VARIANCE);
                  return Unit.unit();
                }
              });
          } catch (final JCGLException e) {
            throw new UnreachableCodeException(e);
          } catch (final RException e) {
            throw new UnreachableCodeException(e);
          }
        }
      });
  }
}
