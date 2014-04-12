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

package com.io7m.renderer.kernel.examples.viewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
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

import com.io7m.jaux.Constraints;
import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.functional.Unit;
import com.io7m.jlog.Callbacks;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.jvvfs.FSCapabilityRead;
import com.io7m.renderer.kernel.examples.ExampleList;
import com.io7m.renderer.kernel.examples.ExampleRendererConstructorType;
import com.io7m.renderer.kernel.examples.ExampleRenderers;
import com.io7m.renderer.kernel.examples.ExampleSceneType;

/**
 * The main example menu window.
 */

final class ViewerMainWindow extends JFrame
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 7107630770143778672L;
  }

  private static @Nonnull JMenu makeDebugMenu(
    final @Nonnull VLogsWindow log_window)
  {
    final JCheckBoxMenuItem log_cb =
      ViewerMainWindow.makeWindowCheckbox("Log...", log_window);

    final JMenu debug_menu = new JMenu("Debug");
    debug_menu.add(log_cb);
    return debug_menu;
  }

  private static @Nonnull JMenu makeFileMenu()
  {
    final JMenuItem quit = new JMenuItem("Quit");
    quit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        System.exit(0);
      }
    });

    final JMenu file_menu = new JMenu("File");
    file_menu.add(quit);
    return file_menu;
  }

  private static @Nonnull JCheckBoxMenuItem makeWindowCheckbox(
    final @Nonnull String text,
    final @Nonnull JFrame window)
  {
    final JCheckBoxMenuItem cb = new JCheckBoxMenuItem(text);
    cb.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
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
        final WindowEvent e)
      {
        cb.setSelected(false);
      }

      @Override public void windowOpened(
        final WindowEvent e)
      {
        cb.setSelected(true);
      }
    };

    window.addWindowListener(listener);
    return cb;
  }

  private final @Nonnull JList<String>                                  example_list;
  private final @Nonnull Map<String, Class<? extends ExampleSceneType>> examples;
  private final @Nonnull Log                                            logx;
  private final @Nonnull JMenuBar                                       menu_bar;
  private final @Nonnull JComboBox<String>                              renderer_list;
  private final @Nonnull Map<String, ExampleRendererConstructorType>    renderers;

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
    final @Nonnull ViewerConfig config,
    final @Nonnull Log in_log,
    final @Nonnull FSCapabilityRead fs)
    throws ConstraintError
  {
    super("Viewer");
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    this.logx = Constraints.constrainNotNull(in_log, "Log");
    final VLogsWindow logs_window = new VLogsWindow();

    this.logx.setCallback(new Callbacks() {
      private final @Nonnull StringBuilder builder = new StringBuilder();

      @SuppressWarnings("boxing") @Override public void call(
        final @Nonnull OutputStream out,
        final @Nonnull String destination,
        final @Nonnull Level level,
        final @Nonnull String message)
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
        this.builder.append(destination);
        this.builder.append(": ");
        this.builder.append(message);
        this.builder.append("\n");

        logs_window.addText(this.builder.toString());
        System.err.print(this.builder.toString());
      }
    });

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @SuppressWarnings("synthetic-access") @Override public
        void
        uncaughtException(
          final Thread t,
          final Throwable e)
      {
        VErrorBox.showErrorLater(ViewerMainWindow.this.logx, e);
      }
    });

    this.renderers = ExampleRenderers.getRenderers();
    this.renderer_list = new JComboBox<String>();
    for (final String name : this.renderers.keySet()) {
      this.renderer_list.addItem(name);
    }

    this.examples = ExampleList.getExamples();
    final Vector<String> example_vector = new Vector<String>();
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
          final @Nonnull ActionEvent e)
      {
        final JComboBox<String> rlist = ViewerMainWindow.this.renderer_list;
        final JList<String> elist = ViewerMainWindow.this.example_list;
        final String renderer_name = (String) rlist.getSelectedItem();
        final String example_name = elist.getSelectedValue();

        final VExample v =
          new VExample(in_log, config, fs, ViewerMainWindow.this.renderers
            .get(renderer_name), ViewerMainWindow.this.examples
            .get(example_name));

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
          final @Nonnull ListSelectionEvent e)
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