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
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jnull.Nullable;
import com.io7m.jtensors.MatrixM4x4F;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RMatrixI4x4F;
import com.io7m.renderer.types.RTransformProjectionType;

public final class SBCameraPanel extends JPanel
{
  private static final long                  serialVersionUID;

  static {
    serialVersionUID = -55144553467054055L;
  }

  protected final JLabel                     error_icon;
  protected final JLabel                     error_text;
  protected final SBProjectionMatrixControls controls;
  protected final MatrixM4x4F                temporary;

  public SBCameraPanel(
    final JFrame owner,
    final SBSceneControllerRendererControl controller)
    throws IOException
  {
    this.controls = SBProjectionMatrixControls.newControls();
    this.temporary = new MatrixM4x4F();

    this.error_text = new JLabel("Some informative error text");
    this.error_icon = SBIcons.makeErrorIcon();
    this.error_text.setVisible(false);
    this.error_icon.setVisible(false);

    final DesignGridLayout dg = new DesignGridLayout(this);
    this.controls.controlsAddToLayout(dg);

    final JButton reset = new JButton("Reset");
    reset.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        controller.rendererUnsetCustomProjection();
      }
    });

    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBWindowUtilities.closeWindow(owner);
      }
    });

    final JButton apply = new JButton("Apply");
    apply.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBCameraPanel.this.trySendMatrix(controller);
      }
    });

    final JButton ok = new JButton("OK");
    ok.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        SBCameraPanel.this.trySendMatrix(controller);
        SBWindowUtilities.closeWindow(owner);
      }
    });

    dg.row().grid().add(new JSeparator());
    dg.row().grid().add(reset).add(cancel).add(apply).add(ok);
  }

  protected void setError(
    final String message)
  {
    this.error_icon.setVisible(true);
    this.error_text.setText(message);
    this.error_text.setVisible(true);
  }

  protected void trySendMatrix(
    final SBSceneControllerRendererControl controller)
  {
    try {
      final SBProjectionDescription m = this.controls.getDescription();
      final RMatrixI4x4F<RTransformProjectionType> mat =
        m.makeProjectionMatrix(this.temporary);
      controller.rendererSetCustomProjection(mat);
      SBCameraPanel.this.unsetError();
    } catch (final SBExceptionInputError x) {
      SBCameraPanel.this.setError(x.getMessage());
    }
  }

  protected void unsetError()
  {
    this.error_icon.setVisible(false);
    this.error_text.setVisible(false);
  }

}
