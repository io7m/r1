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

package com.io7m.r1.examples.viewer_new;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.RowGroup;

import com.io7m.jlog.LogCallbackType;
import com.io7m.jlog.LogConfigReadableType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleImages;
import com.io7m.r1.examples.ExampleList;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRendererDeferredDefault;
import com.io7m.r1.examples.ExampleRendererName;
import com.io7m.r1.examples.ExampleRenderers;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleTypeEnum;

/**
 * The main window that allows the selection of a specific example with a
 * specific renderer.
 */

public final class VExampleSelectionWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 7107630770143778672L;
  }

  private static JMenu makeDebugMenu(
    final VLogsWindow log_window)
  {
    final JCheckBoxMenuItem log_cb =
      VExampleSelectionWindow.makeWindowCheckbox("Log...", log_window);

    final JMenu debug_menu = new JMenu("Debug");
    debug_menu.add(log_cb);
    return debug_menu;
  }

  private static JMenu makeFileMenu()
  {
    final JMenuItem quit = new JMenuItem("Quit");
    quit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        System.exit(0);
      }
    });

    final JMenu file_menu = new JMenu("File");
    file_menu.add(quit);
    return file_menu;
  }

  private static JCheckBoxMenuItem makeWindowCheckbox(
    final String text,
    final JFrame window)
  {
    final JCheckBoxMenuItem cb = new JCheckBoxMenuItem(text);
    cb.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        if (window.isShowing()) {
          VWindowUtilities.closeWindow(window);
        } else {
          window.setVisible(true);
        }
      }
    });

    final WindowAdapter listener = new WindowAdapter() {
      @Override public void windowClosing(
        final @Nullable WindowEvent e)
      {
        cb.setSelected(false);
      }

      @Override public void windowOpened(
        final @Nullable WindowEvent e)
      {
        cb.setSelected(true);
      }
    };

    window.addWindowListener(listener);
    return cb;
  }

  private final Map<String, Class<? extends ExampleSceneType>>           example_scenes;
  private final Map<String, Class<? extends ExampleImageType>>           example_images;
  private final LogType                                                  logx;
  private final JMenuBar                                                 menu_bar;
  private final JComboBox<ExampleRendererName>                           renderer_list;
  private final Map<ExampleRendererName, ExampleRendererConstructorType> renderers;
  private final ExampleImages<BufferedImage>                             image_cache;
  private final VSwingImageLoader                                        image_loader;

  /**
   * Construct a new menu window.
   *
   * @param config
   *          The viewer configuration
   * @param in_log
   *          A log handle
   */

  // CHECKSTYLE:OFF
  public VExampleSelectionWindow(
    final VExampleConfig config,
    final LogType in_log)
  {
    // CHECKSTYLE:ON
    super("Viewer");
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    this.logx = NullCheck.notNull(in_log, "Log");
    final VLogsWindow logs_window = new VLogsWindow();

    this.logx.setCallback(new LogCallbackType() {
      private final StringBuilder builder = new StringBuilder();

      @Override public void call(
        final LogConfigReadableType log,
        final LogLevel level,
        final String message)
      {
        final Calendar cal = Calendar.getInstance();
        @SuppressWarnings("boxing") final String timestamp =
          String.format(
            "%02d:%02d:%02d ",
            NullCheck.notNull(cal.get(Calendar.HOUR_OF_DAY)),
            NullCheck.notNull(cal.get(Calendar.MINUTE)),
            NullCheck.notNull(cal.get(Calendar.SECOND)));

        this.builder.setLength(0);
        this.builder.append(timestamp);
        this.builder.append(level);
        this.builder.append(": ");
        this.builder.append(log.getAbsoluteDestination());
        this.builder.append(": ");
        this.builder.append(message);
        this.builder.append("\n");
        final String text = this.builder.toString();
        assert text != null;

        logs_window.addText(text);
      }
    });

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        uncaughtException(
          final @Nullable Thread t,
          final @Nullable Throwable e)
      {
        assert e != null;
        VErrorBox.showErrorLater(VExampleSelectionWindow.this.logx, e);
      }
    });

    this.image_loader = new VSwingImageLoader();
    this.image_cache =
      new ExampleImages<BufferedImage>(this.image_loader, this.logx);

    this.renderers = ExampleRenderers.getRenderers();
    this.renderer_list = new JComboBox<ExampleRendererName>();
    for (final ExampleRendererName name : this.renderers.keySet()) {
      this.renderer_list.addItem(name);
    }

    this.example_images = ExampleList.getExamplesImages();
    this.example_scenes = ExampleList.getExamplesScenes();

    // CHECKSTYLE:OFF
    final Vector<String> scenes = new Vector<String>();
    for (final String name : this.example_scenes.keySet()) {
      scenes.add(name);
    }

    final Vector<String> images = new Vector<String>();
    for (final String name : this.example_images.keySet()) {
      images.add(name);
    }
    // CHECKSTYLE:ON

    final JComboBox<ExampleTypeEnum> type_list =
      new JComboBox<ExampleTypeEnum>();
    for (final ExampleTypeEnum v : ExampleTypeEnum.values()) {
      type_list.addItem(v);
    }
    type_list.setSelectedItem(ExampleTypeEnum.EXAMPLE_SCENE);

    final JList<String> scene_list = new JList<String>(scenes);
    scene_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    final JList<String> image_list = new JList<String>(images);
    image_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final JButton execute = new JButton("Execute");
    execute.setEnabled(false);
    execute.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nullable ActionEvent e)
      {
        try {
          switch ((ExampleTypeEnum) type_list.getSelectedItem()) {
            case EXAMPLE_IMAGE:
            {
              final String ename = image_list.getSelectedValue();
              final Class<? extends ExampleImageType> c =
                VExampleSelectionWindow.this.example_images.get(ename);
              final ExampleImageType ex = c.newInstance();
              assert ex != null;

              final ExampleRendererConstructorType r =
                ExampleRendererDeferredDefault.get();

              final VExampleCaseDisplayImage s =
                new VExampleCaseDisplayImage(
                  in_log,
                  config,
                  VExampleSelectionWindow.this.image_cache,
                  ex,
                  r);

              s.pack();
              s.setVisible(true);
              break;
            }
            case EXAMPLE_SCENE:
            {
              final String ename = scene_list.getSelectedValue();
              final Class<? extends ExampleSceneType> c =
                VExampleSelectionWindow.this.example_scenes.get(ename);
              final ExampleSceneType ex = c.newInstance();
              assert ex != null;

              final ExampleRendererName rn =
                (ExampleRendererName) VExampleSelectionWindow.this.renderer_list
                  .getSelectedItem();
              assert rn != null;

              final ExampleRendererConstructorType r =
                VExampleSelectionWindow.this.renderers.get(rn);
              assert r != null;

              final VExampleCaseDisplayScene s =
                new VExampleCaseDisplayScene(
                  in_log,
                  config,
                  VExampleSelectionWindow.this.image_cache,
                  ex,
                  r,
                  rn);
              s.pack();
              s.setVisible(true);
              break;
            }
          }

        } catch (final Exception x) {
          VErrorBox.showErrorLater(VExampleSelectionWindow.this.logx, x);
        }
      }
    });

    scene_list.addListSelectionListener(new ListSelectionListener() {
      @Override public void valueChanged(
        final @Nullable ListSelectionEvent e)
      {
        final int index = scene_list.getSelectedIndex();
        if (index == -1) {
          execute.setEnabled(false);
        } else {
          execute.setEnabled(true);
        }
      }
    });

    final JScrollPane scene_pane = new JScrollPane(scene_list);
    scene_pane
      .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    image_list.addListSelectionListener(new ListSelectionListener() {
      @Override public void valueChanged(
        final @Nullable ListSelectionEvent e)
      {
        final int index = image_list.getSelectedIndex();
        if (index == -1) {
          execute.setEnabled(false);
        } else {
          execute.setEnabled(true);
        }
      }
    });

    final JScrollPane image_pane = new JScrollPane(image_list);
    image_pane
      .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    final JMenu file_menu = VExampleSelectionWindow.makeFileMenu();
    final JMenu debug_menu =
      VExampleSelectionWindow.makeDebugMenu(logs_window);

    this.menu_bar = new JMenuBar();
    this.menu_bar.add(file_menu);
    this.menu_bar.add(debug_menu);
    this.setJMenuBar(this.menu_bar);

    this.setMinimumSize(new Dimension(640, 480));

    final DesignGridLayout dg = new DesignGridLayout(this.getContentPane());

    final RowGroup scene_group = new RowGroup();
    final RowGroup image_group = new RowGroup();

    type_list.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nullable ActionEvent e)
      {
        ExampleTypeEnum s;
        final int i = type_list.getSelectedIndex();
        if (i == -1) {
          s = ExampleTypeEnum.EXAMPLE_SCENE;
        } else {
          s = type_list.getItemAt(i);
        }

        switch (s) {
          case EXAMPLE_IMAGE:
          {
            execute.setEnabled(false);
            scene_group.hide();
            image_group.forceShow();
            break;
          }
          case EXAMPLE_SCENE:
          {
            execute.setEnabled(false);
            image_group.hide();
            scene_group.forceShow();
            break;
          }
        }
      }
    });

    dg.row().grid(new JLabel("Example type")).add(type_list);
    dg
      .row()
      .group(scene_group)
      .grid(new JLabel("Renderer"))
      .add(this.renderer_list);
    dg.row().group(scene_group).grid(new JLabel("Scene")).add(scene_pane);
    dg.row().group(image_group).grid(new JLabel("Image")).add(image_pane);
    dg.row().grid().add(execute);

    scene_group.hide();
    image_group.hide();
    scene_group.forceShow();

    this.logx.debug("initialized");
  }
}
