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
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;

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
      final @Nonnull BufferedImage in_image)
    {
      this.image = in_image;
    }
  }

  private static final class TextureParameters extends JPanel
  {
    private static final long                                    serialVersionUID;

    static {
      serialVersionUID = 1094205333772042554L;
    }

    private final @Nonnull JComboBox<TextureWrapS>               wrap_s;
    private final @Nonnull JComboBox<TextureWrapT>               wrap_t;
    private final @Nonnull JComboBox<TextureFilterMinification>  filter_min;
    private final @Nonnull JComboBox<TextureFilterMagnification> filter_mag;

    TextureParameters()
    {
      this.wrap_s = new JComboBox<TextureWrapS>();
      for (final TextureWrapS v : TextureWrapS.values()) {
        this.wrap_s.addItem(v);
      }
      this.wrap_s.setSelectedItem(TextureWrapS.TEXTURE_WRAP_REPEAT);

      this.wrap_t = new JComboBox<TextureWrapT>();
      for (final TextureWrapT v : TextureWrapT.values()) {
        this.wrap_t.addItem(v);
      }
      this.wrap_t.setSelectedItem(TextureWrapT.TEXTURE_WRAP_REPEAT);

      this.filter_min = new JComboBox<TextureFilterMinification>();
      for (final TextureFilterMinification v : TextureFilterMinification
        .values()) {
        this.filter_min.addItem(v);
      }
      this.filter_min
        .setSelectedItem(TextureFilterMinification.TEXTURE_FILTER_LINEAR);

      this.filter_mag = new JComboBox<TextureFilterMagnification>();
      for (final TextureFilterMagnification v : TextureFilterMagnification
        .values()) {
        this.filter_mag.addItem(v);
      }
      this.filter_mag
        .setSelectedItem(TextureFilterMagnification.TEXTURE_FILTER_LINEAR);

      final DesignGridLayout d = new DesignGridLayout(this);
      d.row().grid().add(new JLabel("Wrap S")).add(this.wrap_s);
      d.row().grid().add(new JLabel("Wrap T")).add(this.wrap_t);
      d.row().grid().add(new JLabel("Minification")).add(this.filter_min);
      d.row().grid().add(new JLabel("Magnification")).add(this.filter_mag);
    }

    TextureFilterMagnification getMagnification()
    {
      return (TextureFilterMagnification) this.filter_mag.getSelectedItem();
    }

    TextureFilterMinification getMinification()
    {
      return (TextureFilterMinification) this.filter_min.getSelectedItem();
    }

    TextureWrapS getWrapS()
    {
      return (TextureWrapS) this.wrap_s.getSelectedItem();
    }

    TextureWrapT getWrapT()
    {
      return (TextureWrapT) this.wrap_t.getSelectedItem();
    }
  }

  private static final long                           serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }

  protected final @Nonnull Log                        log_textures;
  protected final @Nonnull JComboBox<PathVirtual>     selector;
  protected @Nonnull Map<PathVirtual, SBTexture2D<?>> images;
  protected final @Nonnull ImageDisplay               image_display;
  protected final @Nonnull JTextField                 t_filter_min;
  protected final @Nonnull JTextField                 t_filter_mag;
  protected final @Nonnull JTextField                 t_wrap_s;
  protected final @Nonnull JTextField                 t_wrap_t;

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

    this.t_filter_mag = new JTextField();
    this.t_filter_min = new JTextField();
    this.t_wrap_s = new JTextField();
    this.t_wrap_t = new JTextField();

    this.t_filter_mag.setEditable(false);
    this.t_filter_min.setEditable(false);
    this.t_wrap_s.setEditable(false);
    this.t_wrap_t.setEditable(false);

    this.selector = new JComboBox<PathVirtual>();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final PathVirtual name =
          (PathVirtual) SBTextures2DPanel.this.selector.getSelectedItem();

        if (name != null) {
          final SBTexture2D<?> t = SBTextures2DPanel.this.images.get(name);
          SBTextures2DPanel.this.image_display.setImage(t.getImage());
          SBTextures2DPanel.this.image_display.repaint();
          final SBTexture2DDescription d = t.getDescription();
          SBTextures2DPanel.this.t_filter_mag.setText(d
            .getTextureMag()
            .toString());
          SBTextures2DPanel.this.t_filter_min.setText(d
            .getTextureMin()
            .toString());
          SBTextures2DPanel.this.t_wrap_s
            .setText(d.getWrapModeS().toString());
          SBTextures2DPanel.this.t_wrap_t
            .setText(d.getWrapModeT().toString());
        }
      }
    });
    this.selectorRefresh(this.selector);

    final JButton clear = new JButton("Clear");
    clear.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final ActionEvent e)
      {
        SBTextures2DPanel.this.selector.setSelectedItem(null);
      }
    });

    final JButton open = new JButton("Open...");
    open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final TextureParameters params = new TextureParameters();
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setAccessory(params);

        final int r = chooser.showOpenDialog(SBTextures2DPanel.this);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();
            final TextureWrapS wrap_s = params.getWrapS();
            final TextureWrapT wrap_t = params.getWrapT();
            final TextureFilterMinification filter_min =
              params.getMinification();
            final TextureFilterMagnification filter_mag =
              params.getMagnification();

            final SwingWorker<SBTexture2D<?>, Void> worker =
              new SwingWorker<SBTexture2D<?>, Void>() {
                @Override protected @Nonnull SBTexture2D<?> doInBackground()
                  throws Exception
                {
                  try {
                    return controller.sceneTexture2DLoad(
                      file,
                      wrap_s,
                      wrap_t,
                      filter_min,
                      filter_mag).get();
                  } catch (final ConstraintError x) {
                    throw new IOException(x);
                  }
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
                    SBErrorBox.showErrorWithTitleLater(
                      SBTextures2DPanel.this.log_textures,
                      "Error loading image",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showErrorWithTitleLater(
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
        final PathVirtual name =
          (PathVirtual) SBTextures2DPanel.this.selector.getSelectedItem();

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
    dg.row().grid().add(this.selector, 2).add(clear).add(open);
    dg.row().grid().add(new JLabel("Wrap S")).add(this.t_wrap_s, 2);
    dg.row().grid().add(new JLabel("Wrap T")).add(this.t_wrap_t, 2);
    dg.row().grid().add(new JLabel("Minification")).add(this.t_filter_min, 2);
    dg
      .row()
      .grid()
      .add(new JLabel("Magnification"))
      .add(this.t_filter_mag, 2);
    dg.row().grid().add(image_pane);
    dg.row().grid().empty(2).add(cancel).add(select);
  }

  protected void selectorRefresh(
    final JComboBox<PathVirtual> select)
  {
    select.removeAllItems();
    for (final Entry<PathVirtual, SBTexture2D<?>> e : this.images.entrySet()) {
      select.addItem(e.getKey());
    }
  }
}
