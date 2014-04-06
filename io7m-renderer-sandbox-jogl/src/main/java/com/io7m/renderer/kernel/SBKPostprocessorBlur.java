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
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.JCGLImplementation;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.SBException.SBExceptionInputError;
import com.io7m.renderer.kernel.types.KBlurParameters;
import com.io7m.renderer.types.RException;

public final class SBKPostprocessorBlur
{
  @SuppressWarnings("synthetic-access") private static class ControlWindow extends
    JFrame
  {
    private static final long                               serialVersionUID =
                                                                               5947463765253062623L;
    private final @Nonnull JButton                          apply;
    private final @Nonnull SBSceneControllerRendererControl controller;
    private final @Nonnull SBBlurControls                   controls;
    private final @Nonnull JButton                          ok;

    ControlWindow(
      final @Nonnull SBSceneControllerRendererControl in_controller,
      final @Nonnull Postprocessor proc,
      final @Nonnull AtomicReference<KBlurParameters> data)
      throws ConstraintError
    {
      this.controller = in_controller;
      this.controls = new SBBlurControls();
      this.controls.controlsLoadFrom(data.get());

      this.apply = new JButton("Apply");
      this.apply.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            data.set(ControlWindow.this.controls.controlsSave());
            ControlWindow.this.sendPostprocessor(proc);
          } catch (final SBExceptionInputError x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
          } catch (final ConstraintError x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
          }
        }
      });

      this.ok = new JButton("OK");
      this.ok.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          try {
            data.set(ControlWindow.this.controls.controlsSave());
            ControlWindow.this.sendPostprocessor(proc);
            SBWindowUtilities.closeWindow(ControlWindow.this);
          } catch (final SBExceptionInputError x) {
            // TODO Auto-generated catch block
            x.printStackTrace();
          } catch (final ConstraintError x) {
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
    private final @Nonnull AtomicReference<KBlurParameters> data;
    private @CheckForNull KPostprocessorBlurRGBA            proc;
    private @CheckForNull KRegionCopierType                 copier;
    private @CheckForNull KUnitQuad                         quad;

    public Postprocessor(
      final @Nonnull AtomicReference<KBlurParameters> in_data)
    {
      this.proc = null;
      this.data = in_data;
    }

    @Override public void postprocessorClose()
    {
      try {
        if (this.proc != null) {
          this.proc.postprocessorClose();
          this.proc = null;
        }
      } catch (final ConstraintError x) {
        // TODO Auto-generated catch block
        x.printStackTrace();
      } catch (final RException x) {
        // TODO Auto-generated catch block
        x.printStackTrace();
      }
    }

    @Override public void postprocessorInitialize(
      final JCGLImplementation gi,
      final KFramebufferRGBACacheType rgba_cache,
      final KShaderCacheType shader_cache,
      final Log log)
      throws RException,
        ConstraintError
    {
      try {
        if (this.proc != null) {
          this.proc.postprocessorClose();
          this.proc = null;
        }

        this.quad = KUnitQuad.newQuad(gi.getGLCommon(), log);

        this.copier =
          KRegionCopier.newCopier(gi, log, shader_cache, this.quad);

        this.proc =
          KPostprocessorBlurRGBA.postprocessorNew(
            gi,
            this.copier,
            rgba_cache,
            shader_cache,
            this.quad,
            log);

      } catch (final JCGLException e) {
        throw RException.fromJCGLException(e);
      }
    }

    @Override public void postprocessorRun(
      final @Nonnull KFramebufferRGBAUsableType input,
      final @Nonnull KFramebufferRGBAUsableType output)
      throws RException,
        ConstraintError
    {
      try {
        final KBlurParameters params = this.data.get();
        if (this.proc != null) {
          this.proc.postprocessorEvaluateRGBA(params, input, output);
        }
      } catch (final ConstraintError e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private final @Nonnull AtomicReference<KBlurParameters> data;
  private final @Nonnull Postprocessor                    postprocessor;
  private final @Nonnull ControlWindow                    window;

  public SBKPostprocessorBlur(
    final @Nonnull SBSceneControllerRendererControl control)
    throws ConstraintError
  {
    this.data =
      new AtomicReference<KBlurParameters>(KBlurParameters
        .newBuilder()
        .build());

    this.postprocessor = new Postprocessor(this.data);
    this.window = new ControlWindow(control, this.postprocessor, this.data);
  }

  public @Nonnull JFrame getWindow()
  {
    return this.window;
  }

}
