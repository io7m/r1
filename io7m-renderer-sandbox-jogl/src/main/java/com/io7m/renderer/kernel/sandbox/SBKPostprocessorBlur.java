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
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.renderer.kernel.KFramebufferRGBACacheType;
import com.io7m.renderer.kernel.KFramebufferRGBAUsableType;
import com.io7m.renderer.kernel.KPostprocessorBlurRGBA;
import com.io7m.renderer.kernel.KPostprocessorBlurRGBAType;
import com.io7m.renderer.kernel.KRegionCopier;
import com.io7m.renderer.kernel.KRegionCopierType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.types.RException;

public final class SBKPostprocessorBlur
{
  @SuppressWarnings("synthetic-access") private static class ControlWindow extends
    JFrame
  {
    private static final long                      serialVersionUID =
                                                                      5947463765253062623L;
    private final JButton                          apply;
    private final SBSceneControllerRendererControl controller;
    private final SBBlurControls                   controls;
    private final JButton                          ok;

    ControlWindow(
      final SBSceneControllerRendererControl in_controller,
      final Postprocessor proc,
      final AtomicReference<KBlurParameters> data)
    {
      this.controller = in_controller;
      this.controls = new SBBlurControls();
      this.controls.controlsLoadFrom(data.get());

      this.apply = new JButton("Apply");
      this.apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          try {
            data.set(ControlWindow.this.controls.controlsSave());
            ControlWindow.this.sendPostprocessor(proc);
          } catch (final SBExceptionInputError x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
          }
        }
      });

      this.ok = new JButton("OK");
      this.ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nullable ActionEvent e)
        {
          try {
            data.set(ControlWindow.this.controls.controlsSave());
            ControlWindow.this.sendPostprocessor(proc);
            SBWindowUtilities.closeWindow(ControlWindow.this);
          } catch (final SBExceptionInputError x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
          }
        }
      });

      final DesignGridLayout layout =
        new DesignGridLayout(this.getContentPane());
      this.controls.controlsAddToLayout(layout);
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
    private final AtomicReference<KBlurParameters> data;
    private @Nullable KPostprocessorBlurRGBAType   proc;
    private @Nullable KRegionCopierType            copier;
    private @Nullable KUnitQuad                    quad;

    public Postprocessor(
      final AtomicReference<KBlurParameters> in_data)
    {
      this.proc = null;
      this.data = in_data;
    }

    @Override public void postprocessorInitialize(
      final JCGLImplementationType gi,
      final KFramebufferRGBACacheType rgba_cache,
      final KShaderCacheType shader_cache,
      final LogUsableType log)
      throws RException
    {
      try {
        final KUnitQuad q = KUnitQuad.newQuad(gi.getGLCommon(), log);

        this.quad = q;
        this.copier =
          KRegionCopier.newCopier(gi, log, shader_cache, this.quad);
        this.proc =
          KPostprocessorBlurRGBA.postprocessorNew(
            gi,
            this.copier,
            rgba_cache,
            shader_cache,
            q,
            log);

      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      }
    }

    @Override public void postprocessorRun(
      final KFramebufferRGBAUsableType input,
      final KFramebufferRGBAUsableType output)
      throws RException
    {
      final KBlurParameters params = this.data.get();
      if (this.proc != null) {
        this.proc.postprocessorEvaluateRGBA(params, input, output);
      }
    }
  }

  private final AtomicReference<KBlurParameters> data;
  private final Postprocessor                    postprocessor;
  private final ControlWindow                    window;

  public SBKPostprocessorBlur(
    final SBSceneControllerRendererControl control)
  {
    this.data =
      new AtomicReference<KBlurParameters>(KBlurParameters
        .newBuilder()
        .build());

    this.postprocessor = new Postprocessor(this.data);
    this.window = new ControlWindow(control, this.postprocessor, this.data);
  }

  public JFrame getWindow()
  {
    return this.window;
  }

}
