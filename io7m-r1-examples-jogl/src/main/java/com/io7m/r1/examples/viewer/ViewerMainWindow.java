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

package com.io7m.r1.examples.viewer;

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
import java.util.concurrent.ExecutionException;

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
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.java.dev.designgridlayout.DesignGridLayout;

import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogCallbackType;
import com.io7m.jlog.LogConfigReadableType;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.r1.examples.ExampleImages;
import com.io7m.r1.examples.ExampleList;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRenderers;
import com.io7m.r1.examples.ExampleSceneType;

/**
 * The main example menu window.
 */

final class ViewerMainWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 7107630770143778672L;
  }

  private static JMenu makeDebugMenu(
    final VLogsWindow log_window)
  {
    final JCheckBoxMenuItem log_cb =
      ViewerMainWindow.makeWindowCheckbox("Log...", log_window);

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

  private final JList<String>                                  example_list;
  private final Map<String, Class<? extends ExampleSceneType>> examples;
  private final LogType                                        logx;
  private final JMenuBar                                       menu_bar;
  private final JComboBox<String>                              renderer_list;
  private final Map<String, ExampleRendererConstructorType>    renderers;

  /**
   * Construct a new menu window.
   *
   * @param in_log
   *          A log handle
   * @param in_controller
   *          The viewer controller interface
   * @throws ConstraintError
   *           If any parameter is <code>null</code>
   */

  public ViewerMainWindow(
    final ViewerConfig config,
    final LogType in_log)
  {
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
        final String timestamp =
          String.format(
            "%02d:%02d:%02d ",
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND));

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
        VErrorBox.showErrorLater(ViewerMainWindow.this.logx, e);
      }
    });

    this.renderers = ExampleRenderers.getRenderers();
    this.renderer_list = new JComboBox<String>();
    for (final String name : this.renderers.keySet()) {
      this.renderer_list.addItem(name);
    }

    this.examples = ExampleList.getExamples();
    // CHECKSTYLE:OFF
    final Vector<String> example_vector = new Vector<String>();
    // CHECKSTYLE:ON
    for (final String name : this.examples.keySet()) {
      example_vector.add(name);
    }
    this.example_list = new JList<String>(example_vector);
    this.example_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    final JButton execute = new JButton("Execute");
    execute.setEnabled(false);
    execute.addActionListener(new ActionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        actionPerformed(
          final @Nullable ActionEvent e)
      {
        final JComboBox<String> rlist = ViewerMainWindow.this.renderer_list;
        final JList<String> elist = ViewerMainWindow.this.example_list;
        final String renderer_name = (String) rlist.getSelectedItem();
        final String example_name = elist.getSelectedValue();

        final ExampleRendererConstructorType r =
          ViewerMainWindow.this.renderers.get(renderer_name);
        final Class<? extends ExampleSceneType> ex =
          ViewerMainWindow.this.examples.get(example_name);
        final ExampleImages<BufferedImage> example_images =
          new ExampleImages<BufferedImage>(
            new VSwingImageLoader(),
            ViewerMainWindow.this.logx);
        final VExample v =
          new VExample(in_log, config, example_images, r, ex);

        final SwingWorker<Unit, Unit> worker = new SwingWorker<Unit, Unit>() {
          @Override protected Unit doInBackground()
            throws Exception
          {
            v.call();
            return Unit.unit();
          }

          @Override protected void done()
          {
            try {
              this.get();
            } catch (final InterruptedException x) {
              VErrorBox.showErrorLater(ViewerMainWindow.this.logx, x);
            } catch (final ExecutionException x) {
              VErrorBox.showErrorLater(ViewerMainWindow.this.logx, x);
            }
          }
        };

        worker.execute();
      }
    });

    this.example_list.addListSelectionListener(new ListSelectionListener() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        valueChanged(
          final @Nullable ListSelectionEvent e)
      {
        final int index =
          ViewerMainWindow.this.example_list.getSelectedIndex();
        if (index == -1) {
          execute.setEnabled(false);
        } else {
          execute.setEnabled(true);
        }
      }
    });

    final JScrollPane example_list_pane = new JScrollPane(this.example_list);
    example_list_pane
      .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    final JMenu file_menu = ViewerMainWindow.makeFileMenu();
    final JMenu debug_menu = ViewerMainWindow.makeDebugMenu(logs_window);

    this.menu_bar = new JMenuBar();
    this.menu_bar.add(file_menu);
    this.menu_bar.add(debug_menu);
    this.setJMenuBar(this.menu_bar);

    this.setMinimumSize(new Dimension(640, 480));

    final DesignGridLayout layout =
      new DesignGridLayout(this.getContentPane());

    layout.row().grid(new JLabel("Renderer")).add(this.renderer_list);
    layout.row().grid().add(example_list_pane);
    layout.row().grid().add(execute);

    this.logx.debug("initialized");
  }
}
