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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import javax.media.nativewindow.WindowClosingProtocol.WindowClosingMode;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.r1.examples.ExampleClasses;
import com.io7m.r1.examples.ExampleImageType;
import com.io7m.r1.examples.ExampleRendererConstructorType;
import com.io7m.r1.examples.ExampleRenderers;
import com.io7m.r1.examples.ExampleSceneType;
import com.io7m.r1.examples.ExampleType;
import com.io7m.r1.examples.ExampleTypeEnum;
import com.io7m.r1.examples.ExampleVisitorType;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

/**
 * The main viewer.
 */

public final class VExampleSingleMain
{
  private static void announceTime()
  {
    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    final SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS Z");
    System.err.println();
    System.err.println("== Started at " + d.format(c.getTime()));
    System.err.println();
  }

  private static boolean hasConsole()
  {
    final String gui =
      System.getProperty("com.io7m.r1.examples.viewer.no_console");
    if (gui == null) {
      return true;
    }
    if ("true".equals(gui)) {
      return false;
    }
    return true;
  }

  /**
   * Main program.
   *
   * @param args
   *          Command line arguments.
   */

  public static void main(
    final String[] args)
  {
    if (VExampleSingleMain.hasConsole() == false) {
      try {
        @SuppressWarnings("resource") final PrintStream out =
          new PrintStream(new BufferedOutputStream(new FileOutputStream(
            "viewer.log",
            true)));
        System.setErr(out);
        System.setOut(out);
      } catch (final FileNotFoundException e) {
        VExampleSingleMain.showFatalErrorAndExit(
          VExampleSingleMain.makeEmptyLog(),
          "Could not open log file",
          e);
      }
    }

    VExampleSingleMain.announceTime();

    if (args.length < 4) {
      VExampleSingleMain
        .showFatalErrorAndExitWithoutException(
          VExampleSingleMain.makeEmptyLog(),
          "No configuration file",
          "Required arguments: viewer.conf example-renderer-name example-type example-class-name");
    }

    try {
      final Properties p = new Properties();
      final FileInputStream s = new FileInputStream(new File(args[0]));
      p.load(s);
      s.close();

      final String renderer_name = NullCheck.notNull(args[1]);
      final ExampleTypeEnum example_type =
        ExampleTypeEnum.valueOf(NullCheck.notNull(args[2]));
      final String example_name = NullCheck.notNull(args[3]);

      VExampleSingleMain.run(p, renderer_name, example_type, example_name);
    } catch (final FileNotFoundException e) {
      VExampleSingleMain.showFatalErrorAndExit(
        VExampleSingleMain.makeEmptyLog(),
        "Could not find config file",
        e);
    } catch (final IOException e) {
      VExampleSingleMain.showFatalErrorAndExit(
        VExampleSingleMain.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final JPropertyException e) {
      VExampleSingleMain.showFatalErrorAndExit(
        VExampleSingleMain.makeEmptyLog(),
        "Error reading config file",
        e);
    }
  }

  private static LogType makeEmptyLog()
  {
    return Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "viewer");
  }

  /**
   * Run the viewer.
   *
   * @param props
   *          The configuration properties.
   * @param type
   *          The type of example.
   * @throws JPropertyException
   *           If an error occurs whilst parsing properties.
   * @param renderer_name
   *          The unqualified name of the renderer.
   * @param example_name
   *          The unqualified name of the example.
   */

  public static void run(
    final Properties props,
    final String renderer_name,
    final ExampleTypeEnum type,
    final String example_name)
    throws JPropertyException
  {
    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(props, "com.io7m.r1.examples");
    final LogType log = Log.newLog(policy, "viewer");
    VExampleSingleMain.runWithConfig(
      VExampleConfig.fromProperties(props),
      log,
      renderer_name,
      type,
      example_name);
  }

  /**
   * Run the viewer with the given config.
   *
   * @param config
   *          The config.
   * @param type
   *          The type of example.
   * @param log
   *          A log interface.
   * @param renderer_name
   *          The unqualified name of the renderer.
   * @param example_name
   *          The unqualified name of the example.
   */

  public static void runWithConfig(
    final VExampleConfig config,
    final LogType log,
    final String renderer_name,
    final ExampleTypeEnum type,
    final String example_name)
  {
    log.debug("starting");

    try {
      final GLProfile profile = VGLImplementation.getGLProfile(log);
      final GLCapabilities caps = new GLCapabilities(profile);
      final GLWindow window = GLWindow.create(caps);

      final ExampleRendererConstructorType renderer =
        ExampleRenderers.getRenderer(renderer_name);
      final ExampleType example =
        ExampleClasses.getExample(type, example_name);

      example.exampleAccept(new ExampleVisitorType<Unit>() {
        @Override public Unit scene(
          final ExampleSceneType s)
        {
          final VExampleRunnerScene runner =
            new VExampleRunnerScene(config, s, renderer, window, log);
          window.addGLEventListener(runner);
          window.addKeyListener(new VExampleRunnerSceneKeyListener(runner));
          window
            .addMouseListener(new VExampleRunnerSceneMouseListener(runner));
          return Unit.unit();
        }

        @Override public Unit image(
          final ExampleImageType i)
        {
          final VExampleRunnerImage runner =
            new VExampleRunnerImage(config, i, renderer, window, log);
          window.addGLEventListener(runner);
          return Unit.unit();
        }
      });

      final Animator animator = new Animator();

      window.addWindowListener(new WindowAdapter() {
        @Override public void windowDestroyNotify(
          final @Nullable WindowEvent e)
        {
          log.debug("Closing window");
          animator.stop();
        }
      });

      window.setSize(640, 480);
      window.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
      window.setTitle(example.exampleGetName());
      window.setVisible(true);

      animator.add(window);
      animator.start();

    } catch (final ClassNotFoundException e) {
      log.critical("Scene error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } catch (final InstantiationException e) {
      log.critical("Scene error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } catch (final IllegalAccessException e) {
      log.critical("Scene error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Crash.
   *
   * @param log
   *          A log interface.
   * @param title
   *          The message title.
   * @param e
   *          The exception.
   */

  public static void showFatalErrorAndExit(
    final LogType log,
    final String title,
    final Throwable e)
  {
    if (VExampleSingleMain.hasConsole()) {
      System.err.println("fatal: "
        + title
        + ": "
        + e.getClass().getCanonicalName()
        + ": "
        + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final StringBuilder m = new StringBuilder();
          m.append(title);
          m.append(" (");
          m.append(e.getClass().getName());
          m.append(")");
          final String s = m.toString();
          assert s != null;

          final JDialog r = VErrorBox.showErrorWithTitle(log, s, e);
          r.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable java.awt.event.WindowEvent _)
            {
              System.exit(1);
            }
          });
        }
      });
    } catch (final InvocationTargetException x) {
      x.printStackTrace();
    } catch (final InterruptedException x) {
      x.printStackTrace();
    }
  }

  /**
   * Crash.
   *
   * @param log
   *          A log interface.
   * @param title
   *          The message title.
   * @param message
   *          The error message text.
   */

  public static void showFatalErrorAndExitWithoutException(
    final LogType log,
    final String title,
    final String message)
  {
    if (VExampleSingleMain.hasConsole()) {
      System.err.println("fatal: " + title + ": " + message);
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final JDialog r =
            VErrorBox.showErrorWithoutException(log, title, message);

          r.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable java.awt.event.WindowEvent _)
            {
              System.exit(1);
            }
          });
        }
      });
    } catch (final InvocationTargetException x) {
      x.printStackTrace();
    } catch (final InterruptedException x) {
      x.printStackTrace();
    }
  }

  private VExampleSingleMain()
  {
    throw new UnreachableCodeException();
  }
}
