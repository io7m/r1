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

package com.io7m.renderer.kernel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jlog.Log;

final class SBTextures2DPanel extends JPanel
{
  private static final class ImageDisplay extends JPanel
  {
    private static final long           serialVersionUID;

    static {
      serialVersionUID = -5281965547772476438L;
    }

    private @CheckForNull BufferedImage image;

    ImageDisplay()
    {

    }

    @Override protected void paintComponent(
      final Graphics g)
    {
      super.paintComponent(g);
      if (this.image != null) {
        g.drawImage(this.image, 0, 0, null);
      }
    }

    protected void setImage(
      final @Nonnull BufferedImage image)
    {
      this.image = image;
    }
  }

  private static final long                  serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }
  protected final @Nonnull Log               log_textures;
  protected final @Nonnull JComboBox<String> selector;
  protected @Nonnull Map<String, SBTexture2D>  images;
  protected @Nonnull ImageDisplay            image_display;

  public SBTextures2DPanel(
    final @Nonnull JFrame window,
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull JTextField select_result,
    final @Nonnull Log log)
  {
    this.log_textures = new Log(log, "textures");
    this.images = controller.sceneTextures2DGet();

    this.setPreferredSize(new Dimension(640, 480));

    this.image_display = new ImageDisplay();
    final JScrollPane image_pane = new JScrollPane(this.image_display);
    image_pane.setMinimumSize(new Dimension(256, 256));

    this.selector = new JComboBox<String>();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final String name =
          (String) SBTextures2DPanel.this.selector.getSelectedItem();

        if (name != null) {
          final SBTexture2D t = SBTextures2DPanel.this.images.get(name);
          SBTextures2DPanel.this.image_display.setImage(t.getImage());
          SBTextures2DPanel.this.image_display.repaint();
        }
      }
    });
    this.selectorRefresh(this.selector);

    final JButton open = new JButton("Open...");
    open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);

        final int r = chooser.showOpenDialog(SBTextures2DPanel.this);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();

            final SwingWorker<SBTexture2D, Void> worker =
              new SwingWorker<SBTexture2D, Void>() {
                @Override protected @Nonnull SBTexture2D doInBackground()
                  throws Exception
                {
                  return controller.sceneTexture2DLoad(file).get();
                }

                @Override protected void done()
                {
                  try {
                    this.get();

                    SBTextures2DPanel.this.images =
                      controller.sceneTextures2DGet();
                    SBTextures2DPanel.this
                      .selectorRefresh(SBTextures2DPanel.this.selector);

                  } catch (final InterruptedException x) {
                    SBErrorBox.showError(
                      SBTextures2DPanel.this.log_textures,
                      "Error loading image",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showError(
                      SBTextures2DPanel.this.log_textures,
                      "Error loading image",
                      x.getCause());
                  }
                }
              };

            worker.execute();
            break;
          }
          case JFileChooser.CANCEL_OPTION:
          case JFileChooser.ERROR_OPTION:
          {
            break;
          }
        }
      }
    });

    final JButton select = new JButton("Select");
    select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final String name =
          (String) SBTextures2DPanel.this.selector.getSelectedItem();
        if (name != null) {
          select_result.setText(name.toString());
        } else {
          select_result.setText("");
        }

        SBWindowUtilities.closeWindow(window);
      }
    });

    final JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        SBWindowUtilities.closeWindow(window);
      }
    });

    final DesignGridLayout dg = new DesignGridLayout(this);
    dg.row().grid().add(this.selector, 3).add(open);
    dg.row().grid().add(image_pane);
    dg.row().grid().empty(2).add(cancel).add(select);
  }

  protected void imageRefresh(
    final JComboBox<File> select)
  {
    final File file = (File) select.getSelectedItem();
    if (file != null) {
      final SBTexture2D t = SBTextures2DPanel.this.images.get(file);
      this.image_display.setImage(t.getImage());
      this.image_display.repaint();
    }
  }

  protected void selectorRefresh(
    final JComboBox<String> select)
  {
    select.removeAllItems();
    for (final Entry<String, SBTexture2D> e : this.images.entrySet()) {
      select.addItem(e.getKey());
    }
  }
}
