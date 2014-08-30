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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jcanephora.AreaInclusive;
import com.io7m.jcanephora.JCGLException;
import com.io7m.jcanephora.api.JCGLImplementationType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.renderer.kernel.KCopyParameters;
import com.io7m.renderer.kernel.KFramebufferRGBACacheType;
import com.io7m.renderer.kernel.KFramebufferRGBAUsableType;
import com.io7m.renderer.kernel.KPostprocessorCopyRGBA;
import com.io7m.renderer.kernel.KRegionCopier;
import com.io7m.renderer.kernel.KRegionCopierType;
import com.io7m.renderer.kernel.KShaderCacheType;
import com.io7m.renderer.kernel.KUnitQuad;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.io7m.renderer.types.RException;
import com.io7m.renderer.types.RExceptionJCGL;

public final class SBKPostprocessorCopy
{
  private static final class AreaControls implements
    SBControlsDataType<KCopyParameters>
  {
    private final SBIntegerHSlider source_x0;
    private final SBIntegerHSlider source_y0;
    private final SBIntegerHSlider source_x1;
    private final SBIntegerHSlider source_y1;
    private final SBIntegerHSlider target_x0;
    private final SBIntegerHSlider target_y0;
    private final SBIntegerHSlider target_x1;
    private final SBIntegerHSlider target_y1;
    private final JCheckBox        blit;
    private final RowGroup         group;

    public AreaControls()
    {
      this.group = new RowGroup();
      this.source_x0 = new SBIntegerHSlider("Source X0", 0, 1000);
      this.source_x1 = new SBIntegerHSlider("Source X1", 0, 1000);
      this.source_y0 = new SBIntegerHSlider("Source Y0", 0, 1000);
      this.source_y1 = new SBIntegerHSlider("Source Y1", 0, 1000);
      this.target_x0 = new SBIntegerHSlider("Target X0", 0, 1000);
      this.target_x1 = new SBIntegerHSlider("Target X1", 0, 1000);
      this.target_y0 = new SBIntegerHSlider("Target Y0", 0, 1000);
      this.target_y1 = new SBIntegerHSlider("Target Y1", 0, 1000);
      this.blit = new JCheckBox();

      this.source_x0.setCurrent(0);
      this.source_y0.setCurrent(0);
      this.source_x1.setCurrent(789);
      this.source_y1.setCurrent(548);

      this.target_x0.setCurrent(0);
      this.target_y0.setCurrent(0);
      this.target_x1.setCurrent(789);
      this.target_y1.setCurrent(548);
    }

    @Override public void controlsAddToLayout(
      final DesignGridLayout layout)
    {
      layout
        .row()
        .group(this.group)
        .grid(this.source_x0.getLabel())
        .add(this.source_x0.getSlider())
        .add(this.source_x0.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.source_y0.getLabel())
        .add(this.source_y0.getSlider())
        .add(this.source_y0.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.source_x1.getLabel())
        .add(this.source_x1.getSlider())
        .add(this.source_x1.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.source_y1.getLabel())
        .add(this.source_y1.getSlider())
        .add(this.source_y1.getField());

      layout
        .row()
        .group(this.group)
        .grid(this.target_x0.getLabel())
        .add(this.target_x0.getSlider())
        .add(this.target_x0.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.target_y0.getLabel())
        .add(this.target_y0.getSlider())
        .add(this.target_y0.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.target_x1.getLabel())
        .add(this.target_x1.getSlider())
        .add(this.target_x1.getField());
      layout
        .row()
        .group(this.group)
        .grid(this.target_y1.getLabel())
        .add(this.target_y1.getSlider())
        .add(this.target_y1.getField());

      layout.row().group(this.group).grid(new JLabel("Blit")).add(this.blit);
    }

    @Override public void controlsHide()
    {
      this.group.hide();
    }

    @Override public void controlsShow()
    {
      this.group.forceShow();
    }

    @Override public void controlsLoadFrom(
      final KCopyParameters t)
    {
      final AreaInclusive ss = t.getSourceSelect();
      final AreaInclusive ts = t.getTargetSelect();

      this.source_x0.setCurrent((int) ss.getRangeX().getLower());
      this.source_x1.setCurrent((int) ss.getRangeX().getUpper());
      this.source_y0.setCurrent((int) ss.getRangeY().getLower());
      this.source_y1.setCurrent((int) ss.getRangeY().getUpper());

      this.target_x0.setCurrent((int) ts.getRangeX().getLower());
      this.target_x1.setCurrent((int) ts.getRangeX().getUpper());
      this.target_y0.setCurrent((int) ts.getRangeY().getLower());
      this.target_y1.setCurrent((int) ts.getRangeY().getUpper());

      this.blit.setSelected(t.useBlitting());
    }

    @Override public KCopyParameters controlsSave()
      throws SBExceptionInputError
    {
      final AreaInclusive ss =
        new AreaInclusive(new RangeInclusiveL(
          this.source_x0.getCurrent(),
          this.source_x1.getCurrent()), new RangeInclusiveL(
          this.source_y0.getCurrent(),
          this.source_y1.getCurrent()));

      final AreaInclusive ts =
        new AreaInclusive(new RangeInclusiveL(
          this.target_x0.getCurrent(),
          this.target_x1.getCurrent()), new RangeInclusiveL(
          this.target_y0.getCurrent(),
          this.target_y1.getCurrent()));

      return new KCopyParameters(ss, ts, this.blit.isSelected());
    }
  }

  @SuppressWarnings("synthetic-access") private static final class ControlWindow extends
    JFrame
  {
    private static final long                      serialVersionUID =
                                                                      5947463765253062623L;
    private final JButton                          apply;
    private final SBSceneControllerRendererControl controller;
    private final AreaControls                     controls;
    private final JButton                          ok;

    ControlWindow(
      final SBSceneControllerRendererControl in_controller,
      final Postprocessor proc,
      final AtomicReference<KCopyParameters> data)
      throws SBExceptionInputError
    {
      this.controller = in_controller;
      this.controls = new AreaControls();
      data.set(this.controls.controlsSave());

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
    private final AtomicReference<KCopyParameters> data;
    private @Nullable KPostprocessorCopyRGBA       proc;
    private @Nullable KUnitQuad                    quad;
    private @Nullable KRegionCopierType            copier;

    public Postprocessor(
      final AtomicReference<KCopyParameters> in_data)
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
        this.quad = KUnitQuad.newQuad(gi.getGLCommon(), log);

        this.copier =
          KRegionCopier.newCopier(gi, log, shader_cache, this.quad);

        this.proc =
          KPostprocessorCopyRGBA.postprocessorNew(
            gi,
            this.copier,
            rgba_cache,
            shader_cache,
            log);

      } catch (final JCGLException e) {
        throw RExceptionJCGL.fromJCGLException(e);
      }
    }

    @Override public void postprocessorRun(
      final KFramebufferRGBAUsableType input,
      final KFramebufferRGBAUsableType output)
      throws RException
    {
      final KCopyParameters params = this.data.get();
      if (this.proc != null) {
        this.proc.postprocessorEvaluateRGBA(params, input, output);
      }
    }
  }

  private final AtomicReference<KCopyParameters> data;
  private final Postprocessor                    postprocessor;
  private final ControlWindow                    window;

  public SBKPostprocessorCopy(
    final SBSceneControllerRendererControl control)
    throws SBExceptionInputError
  {
    this.data = new AtomicReference<KCopyParameters>(null);
    this.postprocessor = new Postprocessor(this.data);
    this.window = new ControlWindow(control, this.postprocessor, this.data);
  }

  public JFrame getWindow()
  {
    return this.window;
  }

}