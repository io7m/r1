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

package com.io7m.renderer.examples.viewer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogPolicyProperties;
import com.io7m.jlog.LogPolicyType;
import com.io7m.jlog.LogType;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jvvfs.Filesystem;
import com.io7m.jvvfs.FilesystemError;
import com.io7m.jvvfs.FilesystemType;

/**
 * The main viewer.
 */

public final class ViewerSingleMain
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
      System.getProperty("com.io7m.renderer.examples.viewer.no_console");
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
    if (ViewerSingleMain.hasConsole() == false) {
      try {
        @SuppressWarnings("resource") final PrintStream out =
          new PrintStream(new BufferedOutputStream(new FileOutputStream(
            "viewer.log",
            true)));
        System.setErr(out);
        System.setOut(out);
      } catch (final FileNotFoundException e) {
        ViewerSingleMain.showFatalErrorAndExit(
          ViewerSingleMain.makeEmptyLog(),
          "Could not open log file",
          e);
      }
    }

    ViewerSingleMain.announceTime();

    if (args.length < 3) {
      ViewerSingleMain
        .showFatalErrorAndExitWithoutException(
          ViewerSingleMain.makeEmptyLog(),
          "No configuration file",
          "Required arguments: viewer.conf example-renderer-name example-class-name");
    }

    try {
      final Properties p = new Properties();
      final FileInputStream s = new FileInputStream(new File(args[0]));
      p.load(s);
      s.close();
      ViewerSingleMain.run(p, args[1], args[2]);
    } catch (final FileNotFoundException e) {
      ViewerSingleMain.showFatalErrorAndExit(
        ViewerSingleMain.makeEmptyLog(),
        "Could not find config file",
        e);
    } catch (final IOException e) {
      ViewerSingleMain.showFatalErrorAndExit(
        ViewerSingleMain.makeEmptyLog(),
        "Error reading config file",
        e);
    } catch (final JPropertyException e) {
      ViewerSingleMain.showFatalErrorAndExit(
        ViewerSingleMain.makeEmptyLog(),
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
    final String example_name)
    throws JPropertyException
  {
    final LogPolicyType policy =
      LogPolicyProperties.newPolicy(props, "com.io7m.renderer.examples");
    final LogType log = Log.newLog(policy, "viewer");
    ViewerSingleMain.runWithConfig(
      ViewerConfig.fromProperties(props),
      log,
      renderer_name,
      example_name);
  }

  /**
   * Run the viewer with the given config.
   * 
   * @param config
   *          The config.
   * @param log
   *          A log interface.
   * @param renderer_name
   *          The unqualified name of the renderer.
   * @param example_name
   *          The unqualified name of the example.
   */

  public static void runWithConfig(
    final ViewerConfig config,
    final LogType log,
    final String renderer_name,
    final String example_name)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run()
      {
        log.debug("starting");

        try {
          final FilesystemType fs =
            Filesystem.makeWithoutArchiveDirectory(log);

          VShaderFilesystem.setupShaderFilesystem(config, fs, log);

          final ViewerSingleMainWindow w =
            new ViewerSingleMainWindow(
              config,
              log,
              fs,
              renderer_name,
              example_name);
          w.run();
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
        } catch (final FilesystemError e) {
          log.critical("Filesystem error: " + e.getMessage());
          e.printStackTrace();
          System.exit(1);
        }
      }
    });
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
    if (ViewerSingleMain.hasConsole()) {
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
          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable WindowEvent _)
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
    if (ViewerSingleMain.hasConsole()) {
      System.err.println("fatal: " + title + ": " + message);
      System.exit(1);
    }

    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        @Override public void run()
        {
          final JDialog r =
            VErrorBox.showErrorWithoutException(log, title, message);

          r.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(
              final @Nullable WindowEvent _)
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

  private ViewerSingleMain()
  {
    throw new UnreachableCodeException();
  }
}
