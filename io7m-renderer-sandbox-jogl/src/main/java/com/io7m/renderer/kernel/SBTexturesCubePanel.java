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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jcanephora.TextureFilterMagnification;
import com.io7m.jcanephora.TextureFilterMinification;
import com.io7m.jcanephora.TextureWrapR;
import com.io7m.jcanephora.TextureWrapS;
import com.io7m.jcanephora.TextureWrapT;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.PathVirtual;

final class SBTexturesCubePanel extends JPanel
{
  private static final class ImageDisplay extends JPanel
  {
    private static final long           serialVersionUID;

    static {
      serialVersionUID = -5281965547772476438L;
    }

    private @CheckForNull BufferedImage positive_z;

    private @CheckForNull BufferedImage negative_z;
    private @CheckForNull BufferedImage positive_y;
    private @CheckForNull BufferedImage negative_y;
    private @CheckForNull BufferedImage positive_x;
    private @CheckForNull BufferedImage negative_x;

    ImageDisplay()
    {

    }

    @Override public Dimension getPreferredSize()
    {
      if (this.positive_z != null) {
        final int size = this.positive_z.getWidth();
        return new Dimension(size * 3, size * 2);
      }
      return this.getSize();
    }

    @Override protected void paintComponent(
      final Graphics g)
    {
      super.paintComponent(g);

      if (this.positive_z != null) {
        final int size = this.positive_z.getWidth();

        g.drawImage(this.positive_y, size, 0, null);
        g.drawImage(this.negative_x, 0, size, null);
        g.drawImage(this.negative_z, size, size, null);
        g.drawImage(this.positive_x, size + size, size, null);
        g.drawImage(this.positive_z, size + size + size, size, null);
        g.drawImage(this.negative_y, size, size + size, null);
      }
    }

    public void setImages(
      final @Nonnull SBTextureCube t)
    {
      this.positive_z = t.getPositiveZ();
      this.positive_y = t.getPositiveY();
      this.positive_x = t.getPositiveX();
      this.negative_z = t.getNegativeZ();
      this.negative_y = t.getNegativeY();
      this.negative_x = t.getNegativeX();
    }
  }

  private static final class TextureParameters extends JPanel
  {
    private static final long                                    serialVersionUID;

    static {
      serialVersionUID = 1094205333772042554L;
    }

    private final @Nonnull JComboBox<TextureWrapR>               wrap_r;
    private final @Nonnull JComboBox<TextureWrapS>               wrap_s;
    private final @Nonnull JComboBox<TextureWrapT>               wrap_t;
    private final @Nonnull JComboBox<TextureFilterMinification>  filter_min;
    private final @Nonnull JComboBox<TextureFilterMagnification> filter_mag;

    TextureParameters()
    {
      this.wrap_r = new JComboBox<TextureWrapR>();
      for (final TextureWrapR v : TextureWrapR.values()) {
        this.wrap_r.addItem(v);
      }
      this.wrap_r.setSelectedItem(TextureWrapR.TEXTURE_WRAP_REPEAT);

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
      d.row().grid().add(new JLabel("Wrap R")).add(this.wrap_r);
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

    TextureWrapR getWrapR()
    {
      return (TextureWrapR) this.wrap_r.getSelectedItem();
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

  static final @Nonnull FileFilter                   CUBE_MAP_FILTER;

  static {
    CUBE_MAP_FILTER = new FileFilter() {
      @Override public boolean accept(
        final File f)
      {
        return f.isDirectory() || f.toString().endsWith(".rxc");
      }

      @Override public String getDescription()
      {
        return "Cube map definitions (*.rxc)";
      }
    };
  }

  private static final long                          serialVersionUID;

  static {
    serialVersionUID = -941448169051827275L;
  }
  protected final @Nonnull Log                       log_textures;
  protected final @Nonnull JComboBox<PathVirtual>    selector;
  protected @Nonnull Map<PathVirtual, SBTextureCube> images;
  protected @Nonnull ImageDisplay                    image_display;
  private final @Nonnull JTextField                  t_filter_mag;
  private final @Nonnull JTextField                  t_filter_min;
  private final @Nonnull JTextField                  t_wrap_r;
  private final @Nonnull JTextField                  t_wrap_s;
  private final @Nonnull JTextField                  t_wrap_t;

  public SBTexturesCubePanel(
    final @Nonnull JFrame window,
    final @Nonnull SBSceneControllerTextures controller,
    final @Nonnull JTextField select_result,
    final @Nonnull Log log)
  {
    this.log_textures = new Log(log, "textures-cube");
    this.images = controller.sceneTexturesCubeGet();

    this.setPreferredSize(new Dimension(640, 480));

    this.image_display = new ImageDisplay();
    final JScrollPane image_pane = new JScrollPane(this.image_display);
    image_pane.setMinimumSize(new Dimension(256, 256));
    image_pane
      .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    image_pane
      .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.t_filter_mag = new JTextField();
    this.t_filter_min = new JTextField();
    this.t_wrap_r = new JTextField();
    this.t_wrap_s = new JTextField();
    this.t_wrap_t = new JTextField();

    this.t_filter_mag.setEditable(false);
    this.t_filter_min.setEditable(false);
    this.t_wrap_r.setEditable(false);
    this.t_wrap_s.setEditable(false);
    this.t_wrap_t.setEditable(false);

    this.selector = new JComboBox<PathVirtual>();
    this.selector.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final PathVirtual name =
          (PathVirtual) SBTexturesCubePanel.this.selector.getSelectedItem();

        if (name != null) {
          final SBTextureCube t = SBTexturesCubePanel.this.images.get(name);
          SBTexturesCubePanel.this.image_display.setImages(t);
          SBTexturesCubePanel.this.image_display.repaint();

          final SBTextureCubeDescription d = t.getDescription();
          SBTexturesCubePanel.this.t_filter_mag.setText(d
            .getTextureMag()
            .toString());
          SBTexturesCubePanel.this.t_filter_min.setText(d
            .getTextureMin()
            .toString());
          SBTexturesCubePanel.this.t_wrap_r.setText(d
            .getWrapModeR()
            .toString());
          SBTexturesCubePanel.this.t_wrap_s.setText(d
            .getWrapModeS()
            .toString());
          SBTexturesCubePanel.this.t_wrap_t.setText(d
            .getWrapModeT()
            .toString());
        }
      }
    });
    this.selectorRefresh(this.selector);

    final JButton open = new JButton("Open...");
    open.addActionListener(new ActionListener() {

      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final TextureParameters params = new TextureParameters();
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(SBTexturesCubePanel.CUBE_MAP_FILTER);
        chooser.setAccessory(params);

        final int r = chooser.showOpenDialog(SBTexturesCubePanel.this);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final File file = chooser.getSelectedFile();
            final TextureWrapR wrap_r = params.getWrapR();
            final TextureWrapS wrap_s = params.getWrapS();
            final TextureWrapT wrap_t = params.getWrapT();
            final TextureFilterMinification filter_min =
              params.getMinification();
            final TextureFilterMagnification filter_mag =
              params.getMagnification();

            final SwingWorker<SBTextureCube, Void> worker =
              new SwingWorker<SBTextureCube, Void>() {
                @Override protected @Nonnull SBTextureCube doInBackground()
                  throws Exception
                {
                  try {
                    return controller.sceneTextureCubeLoad(
                      file,
                      wrap_r,
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

                    SBTexturesCubePanel.this.images =
                      controller.sceneTexturesCubeGet();
                    SBTexturesCubePanel.this
                      .selectorRefresh(SBTexturesCubePanel.this.selector);

                  } catch (final InterruptedException x) {
                    SBErrorBox.showError(
                      SBTexturesCubePanel.this.log_textures,
                      "Error loading image",
                      x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showError(
                      SBTexturesCubePanel.this.log_textures,
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

    final JButton clear = new JButton("Clear");
    clear.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        SBTexturesCubePanel.this.selector.setSelectedItem(null);
      }
    });

    final JButton select = new JButton("Select");
    select.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final PathVirtual name =
          (PathVirtual) SBTexturesCubePanel.this.selector.getSelectedItem();
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
    dg.row().grid().add(image_pane);
    dg.row().grid().empty(2).add(cancel).add(select);
  }

  protected void selectorRefresh(
    final JComboBox<PathVirtual> select)
  {
    select.removeAllItems();
    for (final Entry<PathVirtual, SBTextureCube> e : this.images.entrySet()) {
      select.addItem(e.getKey());
    }
  }
}
