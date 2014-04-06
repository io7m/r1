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

package com.io7m.renderer.kernel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.types.RException;

public final class SBKPostprocessorNone
{
  private static class ControlWindow extends JFrame
  {
    private static final long                               serialVersionUID =
                                                                               5947463765253062623L;
    private final @Nonnull JButton                          apply;
    private final @Nonnull SBSceneControllerRendererControl controller;
    private final @Nonnull JButton                          ok;

    ControlWindow(
      final @Nonnull SBSceneControllerRendererControl in_controller,
      final @Nonnull Postprocessor proc)
    {
      this.controller = in_controller;
      this.apply = new JButton("Apply");
      this.apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          ControlWindow.this.sendPostprocessor(proc);
        }
      });

      this.ok = new JButton("OK");
      this.ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          ControlWindow.this.sendPostprocessor(proc);
          SBWindowUtilities.closeWindow(ControlWindow.this);
        }
      });

      final DesignGridLayout layout =
        new DesignGridLayout(this.getContentPane());
      layout.row().grid().add(this.apply).add(this.ok);
      this.pack();
    }

    protected void sendPostprocessor(
      final Postprocessor proc)
    {
      this.controller.rendererPostprocessorSet(proc);
    }
  }

  private static final class Postprocessor implements SBKPostprocessor
  {
    public Postprocessor()
    {

    }

    @Override public void postprocessorClose()
    {
      // Nothing
    }

    @Override public void postprocessorInitialize(
      final JCGLImplementation gi,
      final KFramebufferRGBACacheType rgba_cache,
      final KShaderCacheType shader_cache,
      final Log log)
      throws RException,
        ConstraintError
    {
      // Nothing
    }

    @Override public void postprocessorRun(
      final @Nonnull KFramebufferRGBAUsableType input,
      final @Nonnull KFramebufferRGBAUsableType output)
      throws RException,
        ConstraintError
    {
      // Nothing
    }
  }

  private final @Nonnull Postprocessor postprocessor;
  private final @Nonnull ControlWindow window;

  public SBKPostprocessorNone(
    final @Nonnull SBSceneControllerRendererControl control)
  {
    this.postprocessor = new Postprocessor();
    this.window = new ControlWindow(control, this.postprocessor);
  }

  public @Nonnull JFrame getWindow()
  {
    return this.window;
  }

}
