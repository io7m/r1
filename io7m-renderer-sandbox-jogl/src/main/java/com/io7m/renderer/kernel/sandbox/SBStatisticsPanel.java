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
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import net.java.dev.designgridlayout.DesignGridLayout;

final class SBStatisticsPanel extends JPanel
{
  private static final long         serialVersionUID;

  static {
    serialVersionUID = -7319547005472170348L;
  }

  private final @Nonnull JTextField labels;
  private final @Nonnull JTextField shaders;
  private final @Nonnull JTextField shadows;
  private final @Nonnull JTextField shadows_bytes;
  private final @Nonnull JTextField shadows_megabytes;
  private final @Nonnull JTextField rgba_framebuffers;
  private final @Nonnull JTextField rgba_framebuffers_bytes;
  private final @Nonnull JTextField rgba_framebuffers_megabytes;

  public SBStatisticsPanel(
    final @Nonnull SBSceneControllerRendererControl controller)
  {
    this.labels = new JTextField("0");
    this.labels.setEditable(false);
    this.shaders = new JTextField("0");
    this.shaders.setEditable(false);

    this.shadows = new JTextField("0");
    this.shadows.setEditable(false);
    this.shadows_bytes = new JTextField("0");
    this.shadows_bytes.setEditable(false);
    this.shadows_megabytes = new JTextField("0");
    this.shadows_megabytes.setEditable(false);

    this.rgba_framebuffers = new JTextField("0");
    this.rgba_framebuffers.setEditable(false);
    this.rgba_framebuffers_bytes = new JTextField("0");
    this.rgba_framebuffers_bytes.setEditable(false);
    this.rgba_framebuffers_megabytes = new JTextField("0");
    this.rgba_framebuffers_megabytes.setEditable(false);

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid(new JLabel("Labels")).add(this.labels);
    dg.row().grid(new JLabel("Shaders")).add(this.shaders);
    dg.row().grid(new JLabel("Shadow map count")).add(this.shadows);
    dg
      .row()
      .grid(new JLabel("Shadow map bytes"))
      .add(this.shadows_bytes)
      .add(this.shadows_megabytes);

    dg
      .row()
      .grid(new JLabel("RBGA framebuffer count"))
      .add(this.rgba_framebuffers);
    dg
      .row()
      .grid(new JLabel("RGBA framebuffer bytes"))
      .add(this.rgba_framebuffers_bytes)
      .add(this.rgba_framebuffers_megabytes);

    final Timer timer = new Timer(1000, new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent e)
      {
        final SwingWorker<SBCacheStatistics, Void> worker =
          new SwingWorker<SBCacheStatistics, Void>() {
            @Override protected SBCacheStatistics doInBackground()
              throws Exception
            {
              return controller.rendererGetCacheStatistics().get();
            }

            @Override protected void done()
            {
              try {
                SBStatisticsPanel.this.statsReceived(this.get());
              } catch (final InterruptedException x) {
                x.printStackTrace();
              } catch (final ExecutionException x) {
                x.printStackTrace();
              }
            }
          };

        worker.execute();
      }
    });

    timer.start();
  }

  protected void statsReceived(
    final @Nonnull SBCacheStatistics stats)
  {
    this.labels.setText(stats.getCachedLabels().toString());
    this.shaders.setText(stats.getCachedShaders().toString());
    this.shadows.setText(stats.getCachedShadowMaps().toString());
    this.shadows_bytes.setText(stats.getCachedShadowMapsBytes().toString());

    {
      BigDecimal mb = new BigDecimal(stats.getCachedShadowMapsBytes());
      mb = mb.divide(BigDecimal.valueOf(1024.0));
      mb = mb.divide(BigDecimal.valueOf(1024.0));
      this.shadows_megabytes.setText(mb.toString());
    }

    this.rgba_framebuffers.setText(stats.getCachedShadowMaps().toString());
    this.rgba_framebuffers_bytes.setText(stats
      .getCachedShadowMapsBytes()
      .toString());

    {
      BigDecimal mb = new BigDecimal(stats.getCachedShadowMapsBytes());
      mb = mb.divide(BigDecimal.valueOf(1024.0));
      mb = mb.divide(BigDecimal.valueOf(1024.0));
      this.rgba_framebuffers_megabytes.setText(mb.toString());
    }
  }
}
