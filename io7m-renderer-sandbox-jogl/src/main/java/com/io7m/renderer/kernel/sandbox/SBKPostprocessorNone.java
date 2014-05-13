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

import javax.swing.JButton;
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.KFramebufferRGBACacheType;
import com.io7m.renderer.kernel.KFramebufferRGBAUsableType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.types.RException;

public final class SBKPostprocessorNone
{
  private static class ControlWindow extends JFrame
  {
    private static final long                      serialVersionUID =
                                                                      5947463765253062623L;
    private final JButton                          apply;
    private final SBSceneControllerRendererControl controller;
    private final JButton                          ok;

    ControlWindow(
      final SBSceneControllerRendererControl in_controller,
      final Postprocessor proc)
    {
      this.controller = in_controller;
      this.apply = new JButton("Apply");
      this.apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          ControlWindow.this.sendPostprocessor(proc);
        }
      });

      this.ok = new JButton("OK");
      this.ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
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

    @Override public void postprocessorInitialize(
      final JCGLImplementationType gi,
      final KFramebufferRGBACacheType rgba_cache,
      final KShaderCacheType shader_cache,
      final LogUsableType log)
      throws RException
    {
      // Nothing
    }

    @Override public void postprocessorRun(
      final KFramebufferRGBAUsableType input,
      final KFramebufferRGBAUsableType output)
      throws RException
    {
      // Nothing
    }
  }

  private final Postprocessor postprocessor;
  private final ControlWindow window;

  public SBKPostprocessorNone(
    final SBSceneControllerRendererControl control)
  {
    this.postprocessor = new Postprocessor();
    this.window = new ControlWindow(control, this.postprocessor);
  }

  public JFrame getWindow()
  {
    return this.window;
  }

}
