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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import com.io7m.jlog.Callbacks;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.jogamp.opengl.util.FPSAnimator;

final class SBMainWindow extends JFrame
{
  private static final long             serialVersionUID;

  static {
    serialVersionUID = -4478810823021843490L;
  }

  protected final @Nonnull SBGLRenderer renderer;

  public SBMainWindow(
    final @Nonnull SBSceneController controller,
    final @Nonnull SBGLRenderer renderer,
    final @Nonnull Log log)
  {
    this.renderer = renderer;

    final SBLightsWindow lights_window = new SBLightsWindow(controller, log);
    final SBLogsWindow logs_window = new SBLogsWindow();
    final SBObjectsWindow objects_window =
      new SBObjectsWindow(controller, log);

    log.setCallback(new Callbacks() {
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

    final GLProfile profile = GLProfile.getDefault();
    final GLCapabilities caps = new GLCapabilities(profile);
    final GLCanvas canvas = new GLCanvas(caps);
    canvas.addGLEventListener(renderer);

    final FPSAnimator animator = new FPSAnimator(canvas, 60);
    animator.start();

    final Container pane = this.getContentPane();
    pane.add(canvas);

    this.setJMenuBar(SBMainWindow.makeMenuBar(
      controller,
      this,
      lights_window,
      logs_window,
      objects_window,
      log));
  }

  private static @Nonnull JMenuBar makeMenuBar(
    final @Nonnull SBSceneControllerIO controller,
    final @Nonnull SBMainWindow window,
    final @Nonnull SBLightsWindow lights_window,
    final @Nonnull SBLogsWindow logs_window,
    final @Nonnull SBObjectsWindow objects_window,
    final @Nonnull Log log)
  {
    final JMenuBar bar = new JMenuBar();
    bar.add(SBMainWindow.makeMenuFile(controller, window, log));
    bar.add(SBMainWindow.makeMenuEdit(lights_window, objects_window));
    bar.add(SBMainWindow.makeMenuDebug(logs_window));
    return bar;
  }

  private static @Nonnull JMenu makeMenuEdit(
    final @Nonnull SBLightsWindow lights_window,
    final @Nonnull SBObjectsWindow objects_window)
  {
    final JMenu menu = new JMenu("Edit");

    final JCheckBoxMenuItem objects =
      SBMainWindow.makeMenuEditObjectsMenuItem(objects_window);
    final JCheckBoxMenuItem lights =
      SBMainWindow.makeMenuEditLightsMenuItem(lights_window);

    menu.add(lights);
    menu.add(objects);
    return menu;
  }

  private static @Nonnull JMenu makeMenuDebug(
    final @Nonnull SBLogsWindow log_window)
  {
    final JMenu menu = new JMenu("Debug");
    final JCheckBoxMenuItem logs =
      SBMainWindow.makeMenuDebugLogsMenuItem(log_window);

    menu.add(logs);
    return menu;
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
          SBWindowUtilities.closeWindow(window);
        } else {
          window.setVisible(true);
        }
      }
    });

    final WindowAdapter listener = new WindowAdapter() {
      @Override public void windowOpened(
        final WindowEvent e)
      {
        cb.setSelected(true);
      }

      @Override public void windowClosing(
        final WindowEvent e)
      {
        cb.setSelected(false);
      }
    };

    window.addWindowListener(listener);
    return cb;
  }

  private static JCheckBoxMenuItem makeMenuDebugLogsMenuItem(
    final @Nonnull SBLogsWindow log_window)
  {
    return SBMainWindow.makeWindowCheckbox("Logs...", log_window);
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditLightsMenuItem(
    final @Nonnull SBLightsWindow lights_window)
  {
    return SBMainWindow.makeWindowCheckbox("Lights...", lights_window);
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditObjectsMenuItem(
    final @Nonnull SBObjectsWindow objects_window)
  {
    return SBMainWindow.makeWindowCheckbox("Objects...", objects_window);
  }

  protected final static @Nonnull FileFilter SCENE_FILTER;

  static {
    SCENE_FILTER = new FileFilter() {
      @Override public String getDescription()
      {
        return "Compressed scenes (*.zs)";
      }

      @Override public boolean accept(
        final File f)
      {
        return f.isDirectory() || f.toString().endsWith(".zs");
      }
    };
  }

  private static @Nonnull JMenu makeMenuFile(
    final @Nonnull SBSceneControllerIO controller,
    final @Nonnull SBMainWindow window,
    final @Nonnull Log log)
  {
    final JMenu menu = new JMenu("File");

    final JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
    open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(SBMainWindow.SCENE_FILTER);
        chooser.showOpenDialog(window);
      }
    });

    final JMenuItem save = new JMenuItem("Save...", KeyEvent.VK_S);
    save.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(SBMainWindow.SCENE_FILTER);

        final int r = chooser.showSaveDialog(window);
        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final SwingWorker<Void, Void> worker =
              new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground()
                  throws Exception
                {
                  return controller
                    .ioSaveScene(chooser.getSelectedFile())
                    .get();
                }

                @Override protected void done()
                {
                  try {
                    this.get();
                  } catch (final InterruptedException x) {
                    SBErrorBox.showError(log, "Interrupted", x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showError(log, "I/O error", x.getCause());
                  }
                }
              };

            worker.execute();
            break;
          }
          case JFileChooser.CANCEL_OPTION:
          {
            break;
          }
          case JFileChooser.ERROR_OPTION:
          {
            break;
          }
        }
      }
    });

    final JMenuItem quit = new JMenuItem("Quit");
    quit.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        SBWindowUtilities.closeWindow(window);
      }
    });

    menu.add(open);
    menu.add(save);
    menu.add(new JSeparator());
    menu.add(quit);
    return menu;
  }
}
