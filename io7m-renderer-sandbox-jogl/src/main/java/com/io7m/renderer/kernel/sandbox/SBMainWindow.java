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

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.annotation.Nonnull;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jaux.UnreachableCodeException;
import com.io7m.jlog.Callbacks;
import com.io7m.jlog.Level;
import com.io7m.jlog.Log;
import com.io7m.renderer.kernel.sandbox.SBException.SBExceptionInputError;
import com.jogamp.opengl.util.Animator;

final class SBMainWindow extends JFrame
{
  protected final static @Nonnull FileFilter SCENE_FILTER;

  private static final long                  serialVersionUID;

  static {
    serialVersionUID = -4478810823021843490L;
  }

  static {
    SCENE_FILTER = new FileFilter() {
      @Override public boolean accept(
        final File f)
      {
        return f.isDirectory() || f.toString().endsWith(".zs");
      }

      @Override public String getDescription()
      {
        return "Compressed scenes (*.zs)";
      }
    };
  }

  private static @Nonnull GLProfile getGLProfile(
    final @Nonnull SandboxConfig config)
  {
    switch (config.getOpenGLProfile()) {
      case OPENGL_PROFILE_DEFAULT:
        return GLProfile.getDefault();
      case OPENGL_PROFILE_GL2:
        return GLProfile.get(GLProfile.GL2);
      case OPENGL_PROFILE_GL3:
        return GLProfile.get(GLProfile.GL3);
      case OPENGL_PROFILE_GL4:
        return GLProfile.get(GLProfile.GL4);
      case OPENGL_PROFILE_GLES2:
        return GLProfile.get(GLProfile.GLES2);
      case OPENGL_PROFILE_GLES3:
        return GLProfile.get(GLProfile.GLES3);
    }

    throw new UnreachableCodeException();
  }

  private static @Nonnull
    <C extends SBSceneControllerRendererControl & SBSceneControllerIO & SBSceneControllerShaders>
    JMenuBar
    makeMenuBar(
      final @Nonnull C controller,
      final @Nonnull SBMainWindow window,
      final @Nonnull SBCameraWindow camera_window,
      final @Nonnull SBLightsWindow lights_window,
      final @Nonnull SBLogsWindow logs_window,
      final @Nonnull SBStatisticsWindow stats_window,
      final @Nonnull SBMaterialsWindow materials_window,
      final @Nonnull SBInstancesWindow instances_window,
      final @Nonnull Log log)
      throws ConstraintError,
        SBExceptionInputError
  {
    final JMenuBar bar = new JMenuBar();
    bar.add(SBMainWindow.makeMenuFile(controller, window, log));
    bar.add(SBMainWindow.makeMenuEdit(
      lights_window,
      materials_window,
      instances_window));
    bar.add(SBMainWindow.makeMenuRenderer(controller));
    bar.add(SBMainWindow.makeMenuPostprocessor(controller));
    bar.add(SBMainWindow.makeMenuView(camera_window, controller));
    bar.add(SBMainWindow.makeMenuDebug(logs_window, stats_window));
    return bar;
  }

  private static @Nonnull JMenu makeMenuDebug(
    final @Nonnull SBLogsWindow log_window,
    final @Nonnull SBStatisticsWindow stats_window)
  {
    final JMenu menu = new JMenu("Debug");

    final JCheckBoxMenuItem logs =
      SBMainWindow.makeMenuDebugLogsMenuItem(log_window);
    final JCheckBoxMenuItem stats =
      SBMainWindow.makeMenuDebugStatisticsMenuItem(stats_window);

    menu.add(logs);
    menu.add(stats);
    return menu;
  }

  private static JCheckBoxMenuItem makeMenuDebugLogsMenuItem(
    final @Nonnull SBLogsWindow log_window)
  {
    return SBMainWindow.makeWindowCheckbox("Logs...", log_window);
  }

  private static JCheckBoxMenuItem makeMenuDebugStatisticsMenuItem(
    final @Nonnull SBStatisticsWindow stats_window)
  {
    return SBMainWindow.makeWindowCheckbox("Statistics...", stats_window);
  }

  private static @Nonnull JMenu makeMenuEdit(
    final @Nonnull SBLightsWindow lights_window,
    final @Nonnull SBMaterialsWindow materials_window,
    final @Nonnull SBInstancesWindow instances_window)
  {
    final JMenu menu = new JMenu("Edit");

    final JCheckBoxMenuItem instances =
      SBMainWindow.makeMenuEditInstancesMenuItem(instances_window);
    final JCheckBoxMenuItem lights =
      SBMainWindow.makeMenuEditLightsMenuItem(lights_window);
    final JCheckBoxMenuItem materials =
      SBMainWindow.makeMenuEditMaterialsMenuItem(materials_window);

    menu.add(materials);
    menu.add(lights);
    menu.add(instances);
    return menu;
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditLightsMenuItem(
    final @Nonnull SBLightsWindow lights_window)
  {
    return SBMainWindow.makeWindowCheckbox("Lights...", lights_window);
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditInstancesMenuItem(
    final @Nonnull SBInstancesWindow instances_window)
  {
    return SBMainWindow.makeWindowCheckbox("Instances...", instances_window);
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditMaterialsMenuItem(
    final @Nonnull SBMaterialsWindow materials_window)
  {
    return SBMainWindow.makeWindowCheckbox("Materials...", materials_window);
  }

  private static @Nonnull JCheckBoxMenuItem makeMenuEditShadersMenuItem(
    final @Nonnull SBShadersWindow shaders_window)
  {
    return SBMainWindow.makeWindowCheckbox(
      "ForwardShaders...",
      shaders_window);
  }

  private static @Nonnull JMenu makeMenuFile(
    final @Nonnull SBSceneControllerIO controller,
    final @Nonnull SBMainWindow window,
    final @Nonnull Log log)
  {
    final JMenu menu = new JMenu("File");

    final JMenu open_recent = new JMenu("Open recent");
    SBMainWindow.makeOpenRecentMenu(controller, log, open_recent);

    final JMenuItem open = new JMenuItem("Open...", KeyEvent.VK_O);
    open.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(SBMainWindow.SCENE_FILTER);
        final int r = chooser.showOpenDialog(window);
        final File selected = chooser.getSelectedFile();

        switch (r) {
          case JFileChooser.APPROVE_OPTION:
          {
            final SwingWorker<Void, Void> worker =
              new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground()
                  throws Exception
                {
                  try {
                    return controller.ioLoadScene(selected).get();
                  } catch (final ConstraintError x) {
                    throw new IOException(x);
                  }
                }

                @SuppressWarnings("synthetic-access") @Override protected
                  void
                  done()
                {
                  try {
                    this.get();
                    SBMainWindow.recentlyUsedSave(selected);
                    SBMainWindow.makeOpenRecentMenu(
                      controller,
                      log,
                      open_recent);
                  } catch (final InterruptedException x) {
                    SBErrorBox.showErrorWithTitleLater(log, "Interrupted", x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showErrorWithTitleLater(
                      log,
                      "I/O error",
                      x.getCause());
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
                  try {
                    return controller
                      .ioSaveScene(chooser.getSelectedFile())
                      .get();
                  } catch (final ConstraintError x) {
                    throw new IOException(x);
                  }
                }

                @Override protected void done()
                {
                  try {
                    this.get();
                  } catch (final InterruptedException x) {
                    SBErrorBox.showErrorWithTitleLater(log, "Interrupted", x);
                  } catch (final ExecutionException x) {
                    SBErrorBox.showErrorWithTitleLater(
                      log,
                      "I/O error",
                      x.getCause());
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
    menu.add(open_recent);
    menu.add(new JSeparator());
    menu.add(save);
    menu.add(new JSeparator());
    menu.add(quit);
    return menu;
  }

  private static @Nonnull
    <C extends SBSceneControllerRendererControl & SBSceneControllerShaders>
    JMenu
    makeMenuPostprocessor(
      final @Nonnull C controller)
      throws ConstraintError,
        SBExceptionInputError
  {
    final ButtonGroup renderer_group = new ButtonGroup();
    final JMenu menu = new JMenu("Postprocessor");

    {
      final SBKPostprocessorNone p = new SBKPostprocessorNone(controller);
      final JCheckBoxMenuItem c =
        SBMainWindow.makeWindowCheckbox("none", p.getWindow());
      menu.add(c);
      renderer_group.add(c);
    }

    {
      final SBKPostprocessorBlur p = new SBKPostprocessorBlur(controller);
      final JCheckBoxMenuItem c =
        SBMainWindow.makeWindowCheckbox("blur", p.getWindow());
      menu.add(c);
      renderer_group.add(c);
    }

    {
      final SBKPostprocessorCopy p = new SBKPostprocessorCopy(controller);
      final JCheckBoxMenuItem c =
        SBMainWindow.makeWindowCheckbox("copy", p.getWindow());
      menu.add(c);
      renderer_group.add(c);
    }

    return menu;
  }

  private static @Nonnull
    <C extends SBSceneControllerRendererControl & SBSceneControllerShaders>
    JMenu
    makeMenuRenderer(
      final @Nonnull C controller)
  {
    final ButtonGroup renderer_group = new ButtonGroup();
    final JMenu menu = new JMenu("Renderer");

    for (final SBKRendererSelectionType type : SBKRendererSelectionType.values()) {
      final JRadioButtonMenuItem b = new JRadioButtonMenuItem(type.getName());
      b.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          controller.rendererSetType(type);
        }
      });

      renderer_group.add(b);
      menu.add(b);
    }

    return menu;
  }

  private static @Nonnull JMenu makeMenuView(
    final @Nonnull SBCameraWindow camera_window,
    final @Nonnull SBSceneControllerRendererControl controller)
  {
    final JMenu menu = new JMenu("View");

    final JMenuItem bg_colour = new JMenuItem("Background colour...");
    bg_colour.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        final Color c =
          JColorChooser
            .showDialog(bg_colour, "Select colour...", Color.BLACK);
        final float[] rgb = c.getColorComponents(null);
        controller.rendererSetBackgroundColour(rgb[0], rgb[1], rgb[2]);
      }
    });

    final JCheckBoxMenuItem axes = new JCheckBoxMenuItem("Show axes");
    axes.setSelected(true);
    axes.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        controller.rendererShowAxes(axes.isSelected());
      }
    });

    final JCheckBoxMenuItem grid = new JCheckBoxMenuItem("Show grid");
    grid.setSelected(true);
    grid.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        controller.rendererShowGrid(grid.isSelected());
      }
    });

    final JCheckBoxMenuItem lights_radii =
      new JCheckBoxMenuItem("Show light radii");
    final JCheckBoxMenuItem lights = new JCheckBoxMenuItem("Show lights");
    lights.setSelected(true);
    lights.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        controller.rendererShowLights(lights.isSelected());
        lights_radii.setEnabled(lights.isSelected());
      }
    });
    lights_radii.addActionListener(new ActionListener() {
      @Override public void actionPerformed(
        final @Nonnull ActionEvent e)
      {
        controller.rendererShowLightRadii(lights_radii.isSelected());
      }
    });

    final JCheckBoxMenuItem camera =
      SBMainWindow.makeWindowCheckbox("Camera...", camera_window);

    menu.add(axes);
    menu.add(grid);
    menu.add(lights);
    menu.add(lights_radii);
    menu.add(new JSeparator());
    menu.add(bg_colour);
    menu.add(camera);
    return menu;
  }

  private static void makeOpenRecentMenu(
    final @Nonnull SBSceneControllerIO controller,
    final @Nonnull Log log,
    final @Nonnull JMenu open_recent)
  {
    final List<File> recent_items = SBMainWindow.recentlyUsedLoad();
    open_recent.removeAll();

    for (final File file : recent_items) {
      final JMenuItem item = new JMenuItem(file.toString());
      item.addActionListener(new ActionListener() {
        @Override public void actionPerformed(
          final @Nonnull ActionEvent e)
        {
          final SwingWorker<Void, Void> worker =
            new SwingWorker<Void, Void>() {
              @Override protected Void doInBackground()
                throws Exception
              {
                try {
                  return controller.ioLoadScene(file).get();
                } catch (final ConstraintError x) {
                  throw new IOException(x);
                }
              }

              @Override protected void done()
              {
                try {
                  this.get();
                } catch (final InterruptedException x) {
                  SBErrorBox.showErrorWithTitleLater(log, "Interrupted", x);
                } catch (final ExecutionException x) {
                  SBErrorBox.showErrorWithTitleLater(
                    log,
                    "I/O error",
                    x.getCause());
                }
              }
            };

          worker.execute();
        }
      });
      open_recent.add(item);
    }

    open_recent.setEnabled(recent_items.size() > 0);
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

  protected static @Nonnull List<File> recentlyUsedLoad()
  {
    final Preferences p =
      Preferences.userRoot().node("/com/io7m/renderer/kernel/sandbox");
    final String data = p.get("recently-used", "");
    if (data.equals("")) {
      return new ArrayList<File>();
    }

    final String[] items = data.split(":");
    final ArrayList<File> files = new ArrayList<File>();
    for (final String name : items) {
      files.add(new File(name));
    }
    return files;
  }

  protected static void recentlyUsedSave(
    final @Nonnull File file)
  {
    final StringBuilder b = new StringBuilder();

    final List<File> files = SBMainWindow.recentlyUsedLoad();
    files.add(file);
    if (files.size() > 10) {
      files.remove(0);
    }

    for (final File f : files) {
      b.append(f);
      b.append(":");
    }

    final Preferences p =
      Preferences.userRoot().node("/com/io7m/renderer/kernel/sandbox");
    p.put("recently-used", b.toString());
  }

  protected final @Nonnull SBGLRenderer renderer;

  public SBMainWindow(
    final @Nonnull SandboxConfig config,
    final @Nonnull SBSceneController controller,
    final @Nonnull SBGLRenderer in_renderer,
    final @Nonnull Log log)
    throws ConstraintError,
      SBExceptionInputError
  {
    this.renderer = in_renderer;

    final SBLightsWindow lights_window = new SBLightsWindow(controller, log);
    final SBLogsWindow logs_window = new SBLogsWindow();
    final SBInstancesWindow instances_window =
      new SBInstancesWindow(controller, log);
    final SBMaterialsWindow materials_window =
      new SBMaterialsWindow(controller, log);
    final SBCameraWindow camera_window = new SBCameraWindow(controller, log);
    final SBStatisticsWindow stats_window =
      new SBStatisticsWindow(controller);

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

    try {
      final GLProfile profile = SBMainWindow.getGLProfile(config);
      final GLCapabilities caps = new GLCapabilities(profile);

      log.debug("caps: " + caps);
      final GLCanvas canvas = new GLCanvas(caps);
      canvas.addGLEventListener(this.renderer);
      canvas.addKeyListener(new KeyListener() {
        private final @Nonnull SBInputState input =
                                                    SBMainWindow.this.renderer
                                                      .getInputState();

        @Override public void keyPressed(
          final @Nonnull KeyEvent e)
        {
          switch (e.getKeyChar()) {
            case 'a':
              this.input.setMovingLeft(true);
              break;
            case 'd':
              this.input.setMovingRight(true);
              break;

            case 'w':
              this.input.setMovingForward(true);
              break;
            case 's':
              this.input.setMovingBackward(true);
              break;

            case 'f':
              this.input.setRotatingUp(true);
              break;
            case 'v':
              this.input.setRotatingDown(true);
              break;

            case 'g':
              this.input.setMovingUp(true);
              break;
            case 'b':
              this.input.setMovingDown(true);
              break;

            case 'q':
              this.input.setRotatingLeft(true);
              break;
            case 'e':
              this.input.setRotatingRight(true);
              break;
          }
        }

        @Override public void keyReleased(
          final @Nonnull KeyEvent e)
        {
          switch (e.getKeyChar()) {
            case 'a':
              this.input.setMovingLeft(false);
              break;
            case 'd':
              this.input.setMovingRight(false);
              break;

            case 'w':
              this.input.setMovingForward(false);
              break;
            case 's':
              this.input.setMovingBackward(false);
              break;

            case 'g':
              this.input.setMovingUp(false);
              break;
            case 'b':
              this.input.setMovingDown(false);
              break;

            case 'f':
              this.input.setRotatingUp(false);
              break;
            case 'v':
              this.input.setRotatingDown(false);
              break;

            case 'q':
              this.input.setRotatingLeft(false);
              break;
            case 'e':
              this.input.setRotatingRight(false);
              break;

            case 't':
              this.input.setWantFramebufferSnapshot(true);
              break;
            case 'p':
              this.input.setWantPauseToggle(true);
              break;
            case 'o':
              this.input.setWantStepOneFrame(true);
              break;
            case 'n':
              this.input.setWantNextCamera(true);
              break;
            case 'm':
              this.input.setWantDumpShadowMaps();
              break;
          }
        }

        @Override public void keyTyped(
          final @Nonnull KeyEvent e)
        {
          // Nothing
        }
      });

      final Animator animator = new Animator(canvas);
      animator.start();

      final Container pane = this.getContentPane();
      pane.add(canvas);

      this.addWindowFocusListener(new WindowAdapter() {
        @Override public void windowGainedFocus(
          final WindowEvent e)
        {
          canvas.requestFocusInWindow();
        }
      });

    } catch (final GLException e) {
      SBErrorBox.showErrorWithTitleLater(log, "Renderer disabled", e);
    }

    this.setJMenuBar(SBMainWindow.makeMenuBar(
      controller,
      this,
      camera_window,
      lights_window,
      logs_window,
      stats_window,
      materials_window,
      instances_window,
      log));
  }
}
